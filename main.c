#include "main.h"
#include "computeBorders.h"
#include "distFrom.h"
#include "indexThread.h"
#include "distToAndData.h"
#include "precompute.h"

#ifndef targetNumTerms
int targetNumTerms;
#endif

pthread_mutex_t debugMutex = PTHREAD_MUTEX_INITIALIZER;

unsigned long MININDEX;
unsigned long MAXINDEX;
time_t endTime;
unsigned int myBlockNumber;
unsigned int myProcNumber;
FILE* logFile;
LineEntry buffer[BUFFERSIZE];

int currentJob = 0;
pthread_mutex_t jobsMutex = PTHREAD_MUTEX_INITIALIZER;

sem_t bufferEntriesFree;
sem_t jobsInQueue;

pthread_t indexThread;
pthread_t computeThreads[NUMTHREADS];
pthread_t outputThread;

//startingIndex, maximumIndex, numberOfTerms, blockNumber, processNumber
int main(int argc, char** argv) {
    if (sysconf(_SC_PAGESIZE) != PAGESIZE){
        printf("Page size should be: %lu", sysconf(_SC_PAGESIZE));
        exit(1);
    }
    if (argc < 6) {
        printf("Error: startingIndex maximumIndex numberOfTerms blockNumber processNumber\n");
        exit(1);
    }
    MININDEX = strtoul(argv[1], NULL, 10);
    MAXINDEX = strtoul(argv[2], NULL, 10);
    int r = atoi(argv[3]);
#ifdef targetNumTerms
    if (r != targetNumTerms){
        printf("Error: number of terms (%d) does not equal compiled version(%d)\n", r, targetNumTerms);
        exit(1);
    }
#else
    targetNumTerms = r;
#endif
    myBlockNumber = (unsigned int) atoi(argv[4]);
    myProcNumber = (unsigned int) atoi(argv[5]);
    sem_init(&bufferEntriesFree, 0, BUFFERSIZE);
    sem_init(&jobsInQueue, 0, 0);
    for (int i = 0; i < BUFFERSIZE; i++){
        initializeLineEntry(&(buffer[i]));
    }
    char* logFilePath = getAbsoluteFilePath(targetNumTerms, myBlockNumber, LOG_FILE);
    logFile = fopen(logFilePath, "a");
    free(logFilePath);
    time_t startTime;
    time(&startTime);
    endTime = startTime + 60 * 60 * HOURS + 60 * MINUTES;
    fprintf(logFile, "Process %u started at time ", myProcNumber);
    printTimestamp();
    if (r == 0){
        if (myBlockNumber == 1){
            precompute();
            exit(0);
        }
        readPrecompute();
        printZero();
        exit(0);
    }
    makeTree(targetNumTerms);
    if (MAXINDEX > maximumIndexes[targetNumTerms]){
        MAXINDEX = maximumIndexes[targetNumTerms];
        fprintf(logFile, "Corrected max to %lu\n", MAXINDEX);
    }
    fflush(logFile);
    readPrecompute();
    pthread_create(&indexThread, NULL, indexThreadRun, NULL);
    memoryMappingInit();
    initLayerCaches();
    for (int i = 0; i < NUMTHREADS; i++){
        pthread_create(&(computeThreads[i]), NULL, computeThread, (void*)(long) i);
    }
    pthread_create(&outputThread, NULL, outputThreadRun, NULL);
    pthread_join(indexThread, NULL);
    for (int i = 0; i < NUMTHREADS; i++){
        pthread_join(computeThreads[i], NULL);
    }
    pthread_join(outputThread, NULL);
    printFinalCounts();
    printMetaData();
    makeNextJob();
}

void* computeThread(void* arg){
    int myThreadNum = (int) (long) arg;
    unsigned long indexes[MAXNUMTERMS];
    int startValues[MAXNUMTERMS];
    mmapSection* dataSegments[MAXNUMTERMS];
    memoryMappingThreadInit(dataSegments);
    //distFromThreadInit(myThreadNum);
    while(1){
        sem_wait(&jobsInQueue);
        pthread_mutex_lock(&jobsMutex);
        LineEntry* current = &buffer[currentJob];
        currentJob++;
        if (currentJob >= BUFFERSIZE){
            currentJob = 0;
        }
        pthread_mutex_unlock(&jobsMutex);
        if (current->quit){
            sem_post(&current->countsComputed);
            return 0;
        }
        for (int col = 0; col < targetNumTerms; col++){
            getIndexDeletedData(current, col, &indexes[col], &startValues[col]);
        }
        memoryMappingRun(current->outputIndex, dataSegments, indexes);
        for (int i = 0; i <= MAXIMUM; i++){
            fmpz_zero(current->distToValues + i);
        }
        for (int col = 0; col < targetNumTerms; col++){
            mmapSection* m = dataSegments[col];
            unsigned long indexDistance = indexes[col] - m->indexOffset;
            //indexOffset should be a multiple of INDEXOFFSETSIZE + 1 because it should fit exactly in a page and the mapping starts at the beginning of the page
            unsigned long groupingNumber = indexDistance / INDEXOFFSETSIZE;
            indexOffsetData* group = &(m->indexLocation[groupingNumber]);
            unsigned long baseOffset = readOffset(group);
            for (int i = 0; i < indexDistance % INDEXOFFSETSIZE; i++){
                baseOffset += group->lengths[i];
            }
            unsigned long dataOffset = baseOffset - m->dataOffset;
            signed char* dataLocation = ((signed char*) m->dataLocation) + dataOffset;
            pointer p;
            p.schar = dataLocation;
            decompressAdd2(current->distToValues, p, startValues[col], MAXIMUM);
        }
        unsigned short totalBSum = 0;
        for (int i = 0; i < targetNumTerms; i++){
            totalBSum += current->bases[i];
        }
        current->bsum = totalBSum;
        unsigned short totalMinSum = current->minSum;
        for (int i = totalMinSum + totalBSum; i <= MAXIMUM; i++){
            fmpz_add(current->distToValues + i, current->distToValues + i, current->distToValues + i-totalBSum);
        }
        //cacheEntry* result = lowerRecursionWrapper(MAXIMUM - totalMinSum, &current->bases, myThreadNum);
        fmpz* distFrom = distFromPoly(myThreadNum, current->bases, current->cth, MAXIMUM - current->minSum);
        multiplyArrays(myThreadNum, totalMinSum, current->distToValues, distFrom);
        /*printf("%lu :[", current->outputIndex);
        for (int i = 0; i <= MAXIMUM - totalMinSum; i++){
            fmpz_print(distFrom+i);
            printf(",");
        }
        printf("]\n");*/
        sem_post(&(current->countsComputed));
    }

}

void getIndexDeletedData(LineEntry* current, int col, unsigned long* indexLoc, int* startValLoc){
    int numTerms = targetNumTerms - 1;
    int bsum = 0;
    int excludedBase = 0;
    int startingMultiplicity = numTerms + masterCutoff - 1;
    int currentDistLeft = MAXIMUM;
    unsigned long spacesSkipped = 0;
    int minBase = 1;
    for (int i = 0; i < targetNumTerms; i++){
        unsigned short currentBase = current->bases[i];
        if (i != col) {
            bsum += currentBase;
            for (int j = minBase; j < currentBase; j++){
                spacesSkipped += getValueFromTree(numTerms - 1, j + 1, currentDistLeft - startingMultiplicity * j);
            }
            currentDistLeft -= currentBase * startingMultiplicity;
            startingMultiplicity--;
            numTerms--;
            minBase = currentBase + 1;
        } else {
            excludedBase = currentBase;
        }
    }
    *indexLoc = spacesSkipped;
    int minSum = MAXIMUM - currentDistLeft;
    *startValLoc = minSum + bsum + CUTOFF * excludedBase;
}

void makeNextJob(){
    //qsub with parameters
    /*
    char* b = (char*) malloc();
    if (stopIndex < maxIndex) {
        sprintf(b, "", stopIndex, maxIndex, targetNumTerms, myBlockNumber, myProcNumber + 1);
        fprintf(logFile, "Next job should be %lu %lu %d %u %u\n", stopIndex, MAXINDEX, targetNumTerms, myBlockNumber,
                myProcNumber + 1);
    }*/
    if (stopIndex < MAXINDEX) {
        printf("Next job should be %lu %lu %d %u %u\n", stopIndex, MAXINDEX, targetNumTerms, myBlockNumber,
               myProcNumber + 1);
        fprintf(logFile, "Next job should be %lu %lu %d %u %u\n", stopIndex, MAXINDEX, targetNumTerms, myBlockNumber,
                myProcNumber + 1);
    } else {
        printf("Final job complete\n");
    }
}

char* getAbsoluteFilePath(int numTerms, unsigned int block, enum fileType ft){
    char* path = (char*) malloc(sizeof(BASEFILEPATH)*2);
    char* fileName;
    switch(ft){
        case INDEX_FILE:
            fileName = "indexes.data";
            break;
        case DATA_FILE:
            fileName = "block.data";
            break;
        case LOG_FILE:
            fileName = "log.txt";
            break;
        case STAT_FILE:
            fileName = "stats.txt";
            break;
        case COUNT_FILE:
            fileName = "counts.txt";
            break;
        case LENGTH_FILE:
            fileName = "length.txt";
    }
    sprintf(path, BASEFILEPATH, MAXIMUM, CUTOFF, numTerms, block, fileName);
    return path;
}

void addSorted(ThreadVariables* t, int index, int lastFull){
    t->increasing[index] = t->increasing[lastFull];
    t->decreasing[t->increasing[lastFull]] = index;
    t->decreasing[index] = lastFull;
    t->increasing[lastFull] = index;
}

void deleteAtIndex(ThreadVariables* t, int index){
    t->decreasing[t->increasing[index]] = t->decreasing[index];
    t->increasing[t->decreasing[index]] = t->increasing[index];
    t->increasing[index] = 0;
    t->decreasing[index] = 0;
}

//initialize arrays
void initThreadVariables(ThreadVariables* t){
    t->increasing[0] = THREADVARSIZE;
    t->decreasing[0] = 0;
    t->decreasing[THREADVARSIZE] = 0;
    t->increasing[THREADVARSIZE] = THREADVARSIZE;
}

void printMetaData(){
    char* statFilePath = getAbsoluteFilePath(targetNumTerms, myBlockNumber, STAT_FILE);
    FILE* statFile = fopen(statFilePath, "a");
    struct rusage rusage;
    getrusage(RUSAGE_SELF, &rusage);
    fprintf(statFile, "(%d terms, block %u)\n", targetNumTerms, myBlockNumber);
    fprintf(statFile, "Block complete: %lu cpu seconds\n", rusage.ru_utime.tv_sec + rusage.ru_stime.tv_sec);
    fprintf(statFile, "User time: %lu s, %lu ms\n", rusage.ru_utime.tv_sec, rusage.ru_utime.tv_usec / 1000);
    fprintf(statFile, "System time: %lu s, %lu ms \n", rusage.ru_stime.tv_sec, rusage.ru_stime.tv_usec / 1000);
    fprintf(statFile, "Maximum resident set size: %lu bytes\n", rusage.ru_maxrss);
    fprintf(statFile, "Page faults: %lu (%lu caught)\n", rusage.ru_majflt, rusage.ru_minflt);
    fprintf(statFile, "Disk writes out: %lu\n", rusage.ru_oublock);
    fprintf(statFile, "Disk reads in: %lu\n", rusage.ru_inblock);
    fprintf(statFile, "Voluntary Context Switches: %lu\n", rusage.ru_nvcsw);
    fprintf(statFile, "Other Context Switches: %lu\n", rusage.ru_nivcsw);
    //fprintf(logFile, "Files opened for data: %lu\n", fileCount);
    fclose(statFile);
    free(statFilePath);
}

void initializeLineEntry(LineEntry* l){
    sem_init(&(l->countsComputed), 0, 0);
    //initThreadVariables(&(l->bases));
    /*for (int i = 0; i <= MAXIMUM; i++){
        mpz_init2(l->distToValues[i], twoLimbSize);
    }*/
}

void printZero(){
    pthread_create(&outputThread, NULL, outputThreadRun, NULL);
    LineEntry* l = &(buffer[0]);
    l->outputIndex = 0;//initial value should correspond to zero
    l->minSum = 0;
    cycleCacheHolder temp;
    l->cth = &temp;
    l->cth->lastIndex = 100;
    //fake data for index 0
    fmpz_set_ui(&l->distToValues[0], 1);
    sem_post(&(l->countsComputed));
    //compute distFrom value
    int totalMinSum = 0;
    LineEntry* current = l;
    int myThreadNum = 0;
    //distFromThreadInit(myThreadNum);
    fmpzArray distFrom;
    memset(&distFrom, 0, sizeof(distFrom));
    getPrecomputedData(distFrom.a, LCUTOFF, 0, MAXIMUM);
    multiplyArrays(myThreadNum, totalMinSum, current->distToValues, distFrom.a);
    //quit after this one
    buffer[1].quit = 1;
    sem_post(&(buffer[1].countsComputed));
    pthread_join(outputThread, NULL);
    printFinalCounts();
    printMetaData();
    printf("Final job complete\n");
}

void copyThreadVariables(ThreadVariables* original, ThreadVariables* copy){
    memcpy(copy->increasing, original->increasing, sizeof(int) * (THREADVARSIZE + 1));
    memcpy(copy->decreasing, original->decreasing, sizeof(int) * (THREADVARSIZE + 1));
}

int computeBsum(ThreadVariables* t){
    int bsum = 0;
    int currentBase = t->increasing[0];
    while (currentBase != THREADVARSIZE){
        bsum += currentBase;
        currentBase = t->increasing[currentBase];
    }
    return bsum;
}

void resetThreadVariables(ThreadVariables* t){
    memset(t->increasing, 0, sizeof(int) * (THREADVARSIZE + 1));
    memset(t->decreasing, 0, sizeof(int) * (THREADVARSIZE + 1));
    t->increasing[0] = THREADVARSIZE;
    t->decreasing[0] = 0;
    t->decreasing[THREADVARSIZE] = 0;
    t->increasing[THREADVARSIZE] = THREADVARSIZE;
}
/*
void printMPZArray(unsigned long index, mpz_t* array, int start, int end, int numTerms){
    printf("%d: %lu [(%d) ", numTerms, index, start);
    for (int i = start; i < end; i++){
        gmp_printf("%Zd ", array[i]);
    }
    if (end >= start) {
        gmp_printf("%Zd (%d)]\n", array[end], end);
    } else {
        printf("(%d)]\n", end);
    }
}*/

void printThreadVariables(ThreadVariables* t) {
    printf("[");
    int currentBase = t->increasing[0];
    while(currentBase != THREADVARSIZE){
        printf("%d", currentBase);
        if (t->increasing[currentBase] != THREADVARSIZE){
            printf(" ");
        } else {
            printf("]");
        }
        currentBase = t->increasing[currentBase];
    }
    printf("\n");
}
