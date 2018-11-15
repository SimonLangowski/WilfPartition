//
// Created by Slang on 9/6/2018.
//

#include "main.h"
#include "memoryManager.h"
#include "outputThread.h"
#include "computeBorders.h"
#include "distToAndData.h"

pthread_t allocatorThread;
sem_t allocationNeeded;
sem_t allocationComplete;
mmapSection mappingSentinel1;
unsigned long mappingEnd1;
mmapSection mappingSentinel2;
unsigned long mappingEnd2;

double averageEntrySize;
int currentMapping;
int allocationMapping;
int numWaiting = 0;
sem_t waitingComplete;
sem_t readyToAllocate;
pthread_mutex_t waitingSynchronization = PTHREAD_MUTEX_INITIALIZER;
//mmapSection* currentBlocksByTermDeleted[NUMTHREADS][MAXNUMTERMS];

//fills in pointers to blocks in sequential order and passes along
//allocated half of memory for current use, and half for buffer ahead
//when border reached, pauses program by not passing semaphore
//then switches pointers to other mapping and tells allocation thread to
//deallocate and reallocate on the old

#define NUMGROUPSPERPAGE (PAGESIZE/sizeof(indexOffsetData))
#define NUMINDEXESPERPAGE (NUMGROUPSPERPAGE * INDEXOFFSETSIZE)
#define getPage(x,y) (((y) - getFirstIndex(x))/NUMINDEXESPERPAGE)
#define BLOCKSIZE ((((maximumIndexes[targetNumTerms - 1]/NUMBLOCKS)/INDEXOFFSETSIZE) + 1)*INDEXOFFSETSIZE)
unsigned long blockSize;
#define getBlock(x) ((unsigned int)((x)/blockSize))
#define getFirstIndex(x) (blockSize * (x))

void memoryMappingInit(){
    //determine average length with sizeof(file)/ numIndexes in file
    blockSize = BLOCKSIZE;
    sem_init(&allocationNeeded,0,0);
    sem_init(&allocationComplete,0,0);
    sem_init(&waitingComplete,0,0);
    sem_init(&readyToAllocate,0,0);
    char* averageBlock = getAbsoluteFilePath(targetNumTerms - 1, 0, LENGTH_FILE);
    FILE* avgBlockFile = fopen(averageBlock, "r");
    unsigned long indexesPerBlock = blockSize;
    unsigned long bytesInFile;
    int ok = fscanf(avgBlockFile, "%lu", &bytesInFile);
    if (ok <= 0){
        printf("Error reading length file\n");
        exit(1);
    }
    fclose(avgBlockFile);
    free(averageBlock);
    averageEntrySize = ((double)bytesInFile)/indexesPerBlock;
    pthread_create(&allocatorThread, NULL, memoryAllocationThreadRun, NULL);
    //send request for initial mapping, and make it small since first request and we don't want to just sit here
    allocationMapping = 0;
    unsigned long m = MININDEX;
    int tempB[MAXNUMTERMS];
    findIndex(tempB, m, targetNumTerms);
    tempB[targetNumTerms - 2]--; //targetNumTerms-2 is the second to last position
    incrementBases(tempB, targetNumTerms - 2);
    m = getIndex2(tempB, targetNumTerms);
    mappingEnd1 = m;
    mappingEnd2 = m;
    sem_post(&allocationNeeded);
    //wait for allocation to finish
    sem_wait(&allocationComplete);
    allocationMapping = 3;
    sem_post(&allocationNeeded);
    currentMapping = 1;
}

void memoryMappingThreadInit(mmapSection** currentBlocksByTermDeleted){
    for (int i = 0; i < targetNumTerms; i++){
        currentBlocksByTermDeleted[i] = &mappingSentinel1;
    }
}

void memoryMappingRun(unsigned long outputIndex, mmapSection** currentBlocksByTermDeleted, const unsigned long* indexes){
    switch(currentMapping){
        case 1:
            if (outputIndex == mappingEnd1){
                //wait for all computation to finish
                sem_wait(&readyToAllocate);
                sem_wait(&allocationComplete);
                currentMapping = 2;
                allocationMapping = 1;
                numWaiting = 0;
                sem_post(&allocationNeeded);
                for (int i = 0; i < NUMTHREADS - 1; i++){
                    sem_post(&waitingComplete);
                }
                //set block pointers to new list
                for (int i = 0; i < targetNumTerms; i++){
                    currentBlocksByTermDeleted[i] = &mappingSentinel2;
                }
            } else if (outputIndex > mappingEnd1) {
                pthread_mutex_lock(&waitingSynchronization);
                numWaiting++;
                if (numWaiting >= NUMTHREADS - 1) {
                    sem_post(&readyToAllocate);
                }
                pthread_mutex_unlock(&waitingSynchronization);
                sem_wait(&waitingComplete);
                for (int i = 0; i < targetNumTerms; i++){
                    currentBlocksByTermDeleted[i] = &mappingSentinel2;
                }
            }
            break;
        case 2:
            if (outputIndex == mappingEnd2){
                //wait for all computation to finish
                sem_wait(&readyToAllocate);
                sem_wait(&allocationComplete);
                currentMapping = 1;
                allocationMapping = 2;
                numWaiting = 0;
                sem_post(&allocationNeeded);
                for (int i = 0; i < NUMTHREADS - 1; i++){
                    sem_post(&waitingComplete);
                }
                //set block pointers to new list
                for (int i = 0; i < targetNumTerms; i++){
                    currentBlocksByTermDeleted[i] = &mappingSentinel1;
                }
            } else if (outputIndex > mappingEnd2) {
                pthread_mutex_lock(&waitingSynchronization);
                numWaiting++;
                if (numWaiting >= NUMTHREADS - 1) {
                    sem_post(&readyToAllocate);
                }
                pthread_mutex_unlock(&waitingSynchronization);
                sem_wait(&waitingComplete);
                //set block pointers to new list
                for (int i = 0; i < targetNumTerms; i++){
                    currentBlocksByTermDeleted[i] = &mappingSentinel1;
                }
            }
            break;
    }
    //use mapping to fill in pointers to correct blocks in current line entry
    for(int i = 0; i < targetNumTerms; i++){
        //first check current block
        if((currentBlocksByTermDeleted[i]->minIndex <= indexes[i]) && (currentBlocksByTermDeleted[i]->maxIndex >= indexes[i])){
            //block found
            continue;
        } else if (currentBlocksByTermDeleted[i]->maxIndex < indexes[i]){
            currentBlocksByTermDeleted[i] = currentBlocksByTermDeleted[i]->nextInList;
            i--;
            continue;
        } else if (currentBlocksByTermDeleted[i]->minIndex > indexes[i]){
            currentBlocksByTermDeleted[i] = currentBlocksByTermDeleted[i]->prevInList;
            i--;
            continue;
        } else {
            printf("Error finding data block");
            exit(1);
        }
    }
}



int indexFiles[2][NUMBLOCKS];
int dataFiles[2][NUMBLOCKS];
mmapSection* lastInserted[MAXNUMTERMS];

void* memoryAllocationThreadRun(void* arg){
    double dynamicAllocationFactor = 1;
    unsigned long currentAllocationSize = INITIALALLOCATION;
    while(1){
        sem_wait(&allocationNeeded);
        fprintf(logFile, "Allocation cycle started: ");
        printTimestamp();
        switch(allocationMapping){
            case 0:
                for (int i = 0; i < NUMBLOCKS; i++){
                    indexFiles[0][i] = -1;
                    indexFiles[1][i] = -1;
                    dataFiles[0][i] = -1;
                    dataFiles[1][i] = -1;
                }
                //small allocation
                clearSentinel(&mappingSentinel1);
                dynamicAllocationFactor = allocateMapping(&mappingSentinel1, currentAllocationSize, dynamicAllocationFactor, 1);
                break;
            case 1:
                //first deallocate 1
                deallocateMapping(&mappingSentinel1, 1);
                clearSentinel(&mappingSentinel1);
                //then allocate 1
                dynamicAllocationFactor = allocateMapping(&mappingSentinel1, currentAllocationSize, dynamicAllocationFactor, 1);
                break;
            case 2:
                //first deallocate 2
                deallocateMapping(&mappingSentinel2, 2);
            case 3:
                clearSentinel(&mappingSentinel2);
                //then allocate 2
                dynamicAllocationFactor = allocateMapping(&mappingSentinel2, currentAllocationSize, dynamicAllocationFactor, 2);
                break;
            case 4:
                return 0;
        }
        sem_post(&allocationComplete);
        currentAllocationSize *= 2;
        if (currentAllocationSize > (MAXNUMPAGES / 2)){
            currentAllocationSize = MAXNUMPAGES/2;
        }
        fflush(logFile);
    }
}

void deallocateMapping(mmapSection* mappingBeginning, int whichOne){
    mmapSection* ending = mappingBeginning;
    mmapSection* current = mappingBeginning->nextInList;
    void* lastIndexMapping = NULL;
    while(current != ending){
        munmap(current->dataLocation, current->numPagesData*PAGESIZE);
        if (current->indexLocation != lastIndexMapping){
            munmap(current->indexLocation, current->numPagesIndex*PAGESIZE);
        }
        lastIndexMapping = current->indexLocation;
        mmapSection* oldMapStruct = current;
        current = current->nextInList;
        free(oldMapStruct);
    }
    for (int i = 0; i < NUMBLOCKS; i++){
        if (indexFiles[whichOne - 1][i] != -1){
            close(indexFiles[whichOne - 1][i]);
            indexFiles[whichOne - 1][i] = -1;
        }
        if (dataFiles[whichOne - 1][i] != -1){
            close(dataFiles[whichOne - 1][i]);
            dataFiles[whichOne - 1][i] = -1;
        }
    }
    fprintf(logFile, "Deallocation complete: ");
    printTimestamp();
}

double allocateMapping(mmapSection* mappingLocation, unsigned long pagesToMap, double dynamicAllocationFactor, int whichOne){
    for (int i = 0; i < targetNumTerms; i++){
        lastInserted[i] = mappingLocation;
    }
    unsigned long indexesToMap = (unsigned long) (pagesToMap/dynamicAllocationFactor * PAGESIZE / (averageEntrySize + sizeof(indexOffsetData)/(double)(INDEXOFFSETSIZE)));
    unsigned long maxGapSizeInIndexes = (unsigned long)(MAXGAPSIZE * PAGESIZE /dynamicAllocationFactor /averageEntrySize);

    unsigned long indexesUsed = 0;
    //compute mapping and merging, using the average index length assumption
    //iterate through second to last cycles, merge if indexes less than maxGapSizeInIndexes apart
    //need to start at one less than second to last cycle
    int beginningBases[MAXNUMTERMS];
    int endingBases[MAXNUMTERMS];
    memset(beginningBases,0, sizeof(int) * MAXNUMTERMS);
    unsigned long startLoc;
    if (whichOne == 1){
        startLoc = mappingEnd2;
    } else {
        startLoc = mappingEnd1;
    }
    if (startLoc >= MAXINDEX){
        return dynamicAllocationFactor;
    }
    findIndex(beginningBases, startLoc, targetNumTerms);
    unsigned long lastOkIndex;
    if (targetNumTerms > 1) {
        //force decrement
        beginningBases[targetNumTerms - 2]--; //targetNumTerms-2 is the second to last position
        unsigned long currentIndex = startLoc - 1;
        while (1) {
            //move by smallest cycles
            int minSum = incrementBases(beginningBases, targetNumTerms - 2);
            memcpy(endingBases, beginningBases, sizeof(int) * targetNumTerms);
            //do division to figure out last digit of endingBases?
            int numIncrements = ((MAXIMUM - minSum) / CUTOFF);
            endingBases[targetNumTerms - 1] += numIncrements; //targetNumTerms-1 is the last position
            currentIndex += (numIncrements + 1);
            for (int col = 0; col < targetNumTerms; col++) {
                //delete and find my beginning and ending in this cycle
                unsigned long min = getIndexDeleted(beginningBases, col);
                unsigned long max = getIndexDeleted(endingBases, col);
                indexesUsed += insertOrMergeInList(min, max, maxGapSizeInIndexes, col);
            }
            if (indexesUsed > indexesToMap) {
                lastOkIndex = currentIndex; //I guess we went ahead and mapped it anyway! oops
                break;
            } else if (currentIndex >= MAXINDEX - 1) {
                lastOkIndex = currentIndex;
                break;
            } else {
                lastOkIndex = currentIndex; //just keep track of how much you jumped when incrementing?
            }
        }
    } else {
        insertOrMergeInList(0, maximumIndexes[0], maxGapSizeInIndexes, 0);
        lastOkIndex = maximumIndexes[1];
    }
    if (whichOne == 1){
        mappingEnd1 = lastOkIndex + 1;
    } else {
        mappingEnd2 = lastOkIndex + 1;
    }
    fprintf(logFile, "Allocation computation complete: ");
    printTimestamp();
    //actually allocate
    unsigned long pagesMapped = 0;
    //first, allocate index mappings, combining nearby and pointing to them multiple times

    //traverse list
    //compute how far away indexes are, and merge and allocate a pointer to all
    //fill in block numbers for sections, and split blocks that go over block boundaries
    mmapSection* current = mappingLocation->nextInList;
    current->block = getBlock(current->minIndex);
    unsigned long startPage = getPage(current->block, current->minIndex);
    unsigned long endPage = getPage(current->block, current->maxIndex + 1);
    mmapSection* beginningOfCurrentMap = current;
    int indexFile = -1;
    while (current != mappingLocation){
        unsigned int beginningBlock = getBlock(current->minIndex);
        unsigned int endingBlock = getBlock(current->maxIndex + 1);
        //assert same
        //if not split block and make allocation
        if ((beginningBlock == endingBlock) && (beginningBlock == beginningOfCurrentMap->block)){
            current->block = beginningBlock;
            unsigned long beginningPage = getPage(current->block, current->minIndex);
            unsigned long endingPage = getPage(current->block, current->maxIndex + 1);
            //check if minPage within range of last page, and "merge"
            if (beginningPage <= endPage + INDEXPAGEGAP){
                endPage = endingPage; //combine mappings
            } else {
                //otherwise allocate previous at beginningOfCurrentMap, traverse list from there to here
                unsigned long pagesInMapping = endPage - startPage + 1;
                unsigned long indexOffset = getFirstIndex(beginningOfCurrentMap->block) + NUMINDEXESPERPAGE * startPage; //first index on first page
                unsigned long fileOffset = startPage * PAGESIZE; //startPage is actually the page index into the block
                if (indexFile == -1){
                    char* indexFileName = getAbsoluteFilePath(targetNumTerms - 1, beginningOfCurrentMap->block, INDEX_FILE);
                    indexFile = open(indexFileName, O_RDONLY);
                    indexFiles[whichOne - 1][beginningOfCurrentMap->block] = indexFile;
                    free(indexFileName);
                }
                indexOffsetData* indexMapping = (indexOffsetData*) mmap(NULL, pagesInMapping*PAGESIZE, PROT_READ, MAP_SHARED | MAP_POPULATE | MAP_FILE, indexFile, fileOffset);
                pagesMapped += pagesInMapping;
                while (beginningOfCurrentMap != current){
                    beginningOfCurrentMap->indexLocation = indexMapping;
                    beginningOfCurrentMap->numPagesIndex = pagesInMapping;
                    beginningOfCurrentMap->indexOffset = indexOffset;
                    beginningOfCurrentMap = beginningOfCurrentMap->nextInList;
                }
                //I am now beginningOfcurrentMap
                startPage = beginningPage;
                endPage = endingPage;
            }
        } else {
            //split block on file border if necessary
            if (beginningBlock != endingBlock) {
                unsigned int nextBlock = getBlock(current->maxIndex + 1);
                unsigned long fileBorderIndex = getFirstIndex(nextBlock);
                mmapSection *newBlockToAdd = (mmapSection *) malloc(sizeof(mmapSection));
                newBlockToAdd->minIndex = fileBorderIndex;
                newBlockToAdd->maxIndex = current->maxIndex;
                current->maxIndex = fileBorderIndex - 1;
                mmapSection *neighbor = current->nextInList;
                neighbor->prevInList = newBlockToAdd;
                newBlockToAdd->nextInList = neighbor;
                newBlockToAdd->prevInList = current;
                current->nextInList = newBlockToAdd;
                newBlockToAdd->block = nextBlock;
                current->block = getBlock(current->minIndex);
                endPage = getPage(current->block, current->maxIndex + 1);

                current = current->nextInList;
                beginningBlock = getBlock(current->minIndex);
                endingBlock = getBlock(current->maxIndex + 1);
            } else {
                current->block = beginningBlock;
            }
            //allocate to end of previous block
            //otherwise allocate previous at beginningOfCurrentMap, traverse list from there to here
            unsigned long pagesInMapping = endPage - startPage + 1;
            unsigned long indexOffset = getFirstIndex(beginningOfCurrentMap->block) + NUMINDEXESPERPAGE * startPage; //first index on first page
            unsigned long fileOffset = startPage * PAGESIZE; //startPage is actually the page index into the block
            if (indexFile == -1){
                char* indexFileName = getAbsoluteFilePath(targetNumTerms - 1, beginningOfCurrentMap->block, INDEX_FILE);
                indexFile = open(indexFileName, O_RDONLY);
                free(indexFileName);
            }
            indexOffsetData* indexMapping = (indexOffsetData*) mmap(NULL, pagesInMapping*PAGESIZE, PROT_READ, MAP_SHARED | MAP_POPULATE | MAP_FILE, indexFile, fileOffset);
            pagesMapped += pagesInMapping;
            while (beginningOfCurrentMap != current){
                beginningOfCurrentMap->indexLocation = indexMapping;
                beginningOfCurrentMap->numPagesIndex = pagesInMapping;
                beginningOfCurrentMap->indexOffset = indexOffset;
                beginningOfCurrentMap = beginningOfCurrentMap->nextInList;
            }
            //start with next file and this block as beginningOfCurrentMap
            //open the next block's file, or reset to -1
            beginningOfCurrentMap = current;
            startPage = getPage(beginningBlock, current->minIndex);
            endPage = getPage(endingBlock, current->maxIndex + 1);
            indexFile = -1;
        }
        current = current->nextInList;
    }
    //do final allocation
    if ((beginningOfCurrentMap != &mappingSentinel1) && (beginningOfCurrentMap != &mappingSentinel2)) {
        unsigned long pagesInMapping = endPage - startPage + 1;
        unsigned long indexOffset =
                getFirstIndex(beginningOfCurrentMap->block) + NUMINDEXESPERPAGE * startPage; //first index on first page
        unsigned long fileOffset = startPage * PAGESIZE; //startPage is actually the page index into the block
        if (indexFile == -1) {
            char *indexFileName = getAbsoluteFilePath(targetNumTerms - 1, beginningOfCurrentMap->block, INDEX_FILE);
            indexFile = open(indexFileName, O_RDONLY);
            free(indexFileName);
        }
        indexOffsetData *indexMapping = (indexOffsetData *) mmap(NULL, pagesInMapping * PAGESIZE, PROT_READ,
                                                                 MAP_SHARED | MAP_POPULATE | MAP_FILE, indexFile,
                                                                 fileOffset);
        if (indexMapping == MAP_FAILED){
            perror("mmap");
            exit(1);
        }
        pagesMapped += pagesInMapping;
        while (beginningOfCurrentMap != current) {
            beginningOfCurrentMap->indexLocation = indexMapping;
            beginningOfCurrentMap->numPagesIndex = pagesInMapping;
            beginningOfCurrentMap->indexOffset = indexOffset;
            beginningOfCurrentMap = beginningOfCurrentMap->nextInList;
        }
    }

    //then allocate data mappings (you need the file offsets found on the index mappings)
    //don't need to check blocks this time
    //merge things dynamically if less than gap size in pages apart?? (then check block for that)
    current = mappingLocation->nextInList;
    int currentBlockFile = -1;
    int currentBlockFileDescriptor = -1;
    while (current != mappingLocation){
        if (current->block != currentBlockFile){
            char* blockDataFileName = getAbsoluteFilePath(targetNumTerms - 1, current->block, DATA_FILE);
            currentBlockFileDescriptor = open(blockDataFileName, O_RDONLY);
            dataFiles[whichOne - 1][current->block] = currentBlockFileDescriptor;
            free(blockDataFileName);
            currentBlockFile = current->block;
        }
        unsigned long minOffset = readOffsetFromMap(current, current->minIndex);
        unsigned long maxOffset;
        if (current->maxIndex + 1 < maximumIndexes[targetNumTerms - 1]) {
            maxOffset = readOffsetFromMap(current, current->maxIndex + 1);
        } else {
            char* blockDataFileName = getAbsoluteFilePath(targetNumTerms - 1, current->block, DATA_FILE);
            struct stat s;
            stat(blockDataFileName, &s);
            maxOffset = (unsigned long) (s.st_size - 1);
            free(blockDataFileName);
        }

        unsigned long firstPage = minOffset / PAGESIZE;
        unsigned long lastPage = maxOffset / PAGESIZE;
        unsigned long pages = lastPage - firstPage + 1;
        current->dataOffset = firstPage * PAGESIZE;
        current->numPagesData = pages;
        current->dataLocation = mmap(NULL, pages*PAGESIZE, PROT_READ, MAP_SHARED | MAP_POPULATE | MAP_FILE, currentBlockFileDescriptor, current->dataOffset);
        if (current->dataLocation == MAP_FAILED){
            perror("mmap");
            exit(1);
        }
        pagesMapped += pages;
        current = current->nextInList;
    }
    fprintf(logFile, "Allocation complete: ");
    printTimestamp();
    fprintf(logFile, "Good until index %lu\n", lastOkIndex);
    double newFactor = ((double)pagesMapped) / pagesToMap;
    newFactor *= dynamicAllocationFactor;
    fprintf(logFile, "New Allocation factor is %lf\n", newFactor);
    return newFactor;
}

unsigned long readOffsetFromMap(mmapSection* map, unsigned long indexOnMap){
    unsigned long indexGroupingNumber = (indexOnMap - map->indexOffset)/INDEXOFFSETSIZE;
    indexOffsetData* iod = &(map->indexLocation[indexGroupingNumber]);
    unsigned long offset = readOffset(iod);
    for (int i = 0; i < indexOnMap % INDEXOFFSETSIZE; i++){
        offset += iod->lengths[i];
    }
    return offset;
}

unsigned long insertOrMergeInList(unsigned long min, unsigned long maxIndex, unsigned long mergeAmount, int placementHint){
    //find closest block, as determined through min
    mmapSection * current = lastInserted[placementHint];

    //things to the left of mapping sentinel but before the first index need to still be checked for right merged

    while(1) {
        if ((current->minIndex <= min) && (current->nextInList->minIndex > min)){
            break;
        } else if (current->minIndex > min){
            current = current->prevInList;
        } else {
            current = current->nextInList;
        }
        if ((current == &mappingSentinel1) || (current == &mappingSentinel2)){
            break;
        }
    }
    //check overlap on each side and combine
    //check if within close distance on each side and combine
    if ((current->minIndex <= min) && (current->maxIndex >= maxIndex) && (current != &mappingSentinel1) && (current != &mappingSentinel2)){
        //fully contained already
        lastInserted[placementHint] = current;
        return 0;
    }
    unsigned long numIndexesAdded = 0;
    int merged = 0;
    if ((current->maxIndex + mergeAmount >= min) && (current != &mappingSentinel1) && (current != &mappingSentinel2)){
        //merge left
        numIndexesAdded += maxIndex - current->maxIndex;
        current->maxIndex = maxIndex;
        merged = 1;
    }
    if ((current->nextInList->minIndex <= maxIndex + mergeAmount) && (current->nextInList != & mappingSentinel1) && (current->nextInList != &mappingSentinel2)){
        //merge right
        if (merged){
            //I've already been added to the current block, so the two blocks must be merged
            numIndexesAdded += current->nextInList->minIndex - current->maxIndex - 1;
            current->maxIndex = current->nextInList->maxIndex;
            //remove block
            mmapSection* blockToRemove = current->nextInList;
            current->nextInList->nextInList->prevInList = current;
            current->nextInList = current->nextInList->nextInList;
            for (int i = 0; i < targetNumTerms; i++){
                if (lastInserted[i] == blockToRemove){
                    lastInserted[i] = current;
                }
            }
            free(blockToRemove);
        } else {
            current = current->nextInList;
            numIndexesAdded += current->minIndex - min;
            current->minIndex = min;
            merged = 1;
        }
    }
    //need to make full allocation
    if(!merged){
        mmapSection* section = (mmapSection*) malloc(sizeof(mmapSection));
        section->nextInList = current->nextInList;
        current->nextInList->prevInList = section;
        section->prevInList = current;
        current->nextInList = section;
        section->minIndex = min;
        section->maxIndex = maxIndex;
        numIndexesAdded = maxIndex - min + 1;
        lastInserted[placementHint] = section;
    } else {
        lastInserted[placementHint] = current;
    }

    //return number of indexes actually added to list
    return numIndexesAdded;
}

void clearSentinel(mmapSection* m){
    m->maxIndex = 0;
    m->minIndex = ULONG_MAX; //or max index
    m->dataLocation = NULL;
    m->indexLocation = NULL;
    m->nextInList = m;
    m->prevInList = m;
}

void printTimestamp(){
    time_t t;
    time(&t);
    fprintf(logFile, "%s\n", asctime(localtime(&t)));
}

void findIndex(int* guess, unsigned long target, int numTerms){
    finishGuess(guess, 1, 0, numTerms);
    int minimumBaseGuess = 1;
    for (int currentTermIndex = 0; currentTermIndex < numTerms; currentTermIndex++) {
        int currentBaseGuess = minimumBaseGuess;
        while (1) {
            finishGuess(guess, currentBaseGuess, currentTermIndex, numTerms);
            unsigned long ind = getIndex2(guess, numTerms);
            if (ind == target){
                return;
            } else if (ind > target){
                currentBaseGuess--;
                finishGuess(guess, currentBaseGuess, currentTermIndex, numTerms);
                break;
            } else {
                currentBaseGuess++;
            }
        }
        minimumBaseGuess = currentBaseGuess + 1;
    }
}

unsigned long getIndexDeleted(const int* original, int column){
    int bases[MAXNUMTERMS];
    for (int i = 0; i < column; i++){
        bases[i] = original[i];
    }
    for (int i = column + 1; i < targetNumTerms; i++){
        bases[i - 1] = original[i];
    }
    return getIndex2(bases, targetNumTerms - 1);
}

void finishGuess(int* guess, int minBase, int location, int length){
    for (int i = location; i < length; i++){
        guess[i] = minBase + i - location;
    }
}

int incrementBases(int* bases, int currentIncrementPosition){
    while(1){
        //try increment
        int baseNumber = bases[currentIncrementPosition] + 1;
        for (int i = currentIncrementPosition; i < targetNumTerms; i++){
            bases[i] = baseNumber;
            baseNumber++;
        }
        int startingNumber = targetNumTerms + CUTOFF - 1;
        int total = 0;
        for (int i = 0; i < targetNumTerms; i++){
            total += bases[i] * startingNumber;
            startingNumber--;
        }
        if (total <= MAXIMUM){
            return total;
        }
        //move increment position
        currentIncrementPosition--;
        if (currentIncrementPosition < 0){
            break;
        }
    }
}