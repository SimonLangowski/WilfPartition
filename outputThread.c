//
// Created by Slang on 9/6/2018.
//

#include "main.h"
#include "outputThread.h"

FILE* outputDataFile;
FILE* outputIndexFile;
cmp c;
unsigned long buf[2*MAXIMUM + 1];

void* outputThreadRun(void* arg){
    c.beginning.ulongp = buf;
    fmpz t;
    fmpz_init(&t);
    //open output files
    char* outputDataName = getAbsoluteFilePath(targetNumTerms, myBlockNumber, DATA_FILE);
    outputDataFile = fopen(outputDataName, "a");
    free(outputDataName);
    char* outputIndexName = getAbsoluteFilePath(targetNumTerms, myBlockNumber, INDEX_FILE);
    outputIndexFile = fopen(outputIndexName, "a");
    free(outputIndexName);
    unsigned long currentDataOffset = ftell(outputDataFile);
    int inputIndex = 0;
    unsigned long currentOutputIndex = MININDEX;
    int currentLocationInGrouping = 0;
    indexOffsetData currentGrouping;
    memset(&currentGrouping, 0, sizeof(currentGrouping));
    while(1){
        LineEntry* current = &(buffer[inputIndex]);
        sem_wait(&(current->countsComputed));
        if(current->quit){
            //index thread is to ensure index offset size alignment when quiting before max index
            //no need to ensure alignment because we aren't done writing to the files yet (the next process in this block)
            fclose(outputIndexFile);
            fclose(outputDataFile);
            return 0;
        }
        int bsum = current->bsum;
        //smallest base not included
        int smallestNotIncluded = 1;
        for (int i = 0; i < targetNumTerms; i++){
            if (current->bases[i] == smallestNotIncluded){
                smallestNotIncluded++;
            } else {
                break;
            }
        }
        int nextLastUsage = MAXIMUM - bsum - CUTOFF*(smallestNotIncluded);
        /*pthread_mutex_lock(&debugMutex);
        printf ("M: ");
        printMPZArray(currentOutputIndex, current->distFromValues, 0, MAXIMUM - current->minSum, targetNumTerms);
        printf("C: ");
        printMPZArray(currentOutputIndex, current->distToValues, current->minSum, nextLastUsage, targetNumTerms);
        pthread_mutex_unlock(&debugMutex);*/
        if (nextLastUsage >= current->minSum) {
            compress2(&c, current->distToValues, t, current->minSum, nextLastUsage);
            //ensure word alignment
            if (c.size % 8 != 0){
                c.size = c.size - (c.size % 8) + 8;
            }
            fwrite(c.beginning.ulongp, c.size, 1, outputDataFile);
            //update location
            currentDataOffset += c.size;
        } else { //this value won't be used again, and does not need to be even written to the data
            c.size = 0;
        }
        if (currentLocationInGrouping == 0){
            //write offset into grouping
            writeOffset(&currentGrouping, currentDataOffset - c.size);
            if (c.size > USHRT_MAX){
                fprintf(logFile, "Error: data size too large: %lu was %d\n", currentOutputIndex, c.size);
                exit(1);
            }
            currentGrouping.lengths[currentLocationInGrouping] = (unsigned short) c.size;
            currentLocationInGrouping++;
        } else if (currentLocationInGrouping == INDEXOFFSETSIZE - 1){
            //append grouping to file
            fwrite(&currentGrouping, sizeof(currentGrouping), 1, outputIndexFile);
            currentLocationInGrouping = 0;
        } else {
            //write length as offset
            if (c.size > USHRT_MAX){
                fprintf(logFile, "Error: data size too large: %lu was %d\n", currentOutputIndex, c.size);
                exit(1);
            }
            currentGrouping.lengths[currentLocationInGrouping] = (unsigned short) c.size;
            currentLocationInGrouping++;
        }
        if (current->cth->lastIndex == currentOutputIndex){
            sem_post(&cycleHoldersFree);
        }
        sem_post(&bufferEntriesFree);
        inputIndex++;
        currentOutputIndex++;
        if (inputIndex >= BUFFERSIZE){
            inputIndex = 0;
        }
        if (currentOutputIndex >= MAXINDEX){
            if (currentLocationInGrouping == 0){
                unsigned long finalOffset = ftell(outputDataFile);
                writeOffset(&currentGrouping, finalOffset);
            }
            cleanup(currentDataOffset, &currentGrouping);
            return 0;
        }
    }
}

void cleanup(unsigned long currentDataOffset, indexOffsetData* currentGrouping){
    //ensure page alignment for data
    if(currentDataOffset % PAGESIZE != 0){
        unsigned long zero = 0;
        for (int i = 0; i < (PAGESIZE - (currentDataOffset % PAGESIZE))/sizeof(unsigned long); i++) {
            fwrite(&zero, sizeof(unsigned long), 1, outputDataFile);
        }
    }
    //finish current grouping and output
    fwrite(currentGrouping, sizeof(indexOffsetData), 1, outputIndexFile);
    //ensure page alignment for indexes
    unsigned long currentIndexLocation = ftell(outputIndexFile);
    if (currentIndexLocation % PAGESIZE != 0){
        unsigned long zero = 0;
        for (int i = 0; i < (PAGESIZE - (currentIndexLocation % PAGESIZE))/sizeof(unsigned long); i++) {
            fwrite(&zero, sizeof(unsigned long), 1, outputIndexFile);
        }
    }
    fclose(outputDataFile);
    fclose(outputIndexFile);
}

void writeOffset(indexOffsetData* iod, unsigned long offset){
    iod->higherOrderBytes = (unsigned short) (offset >> 32);
    iod->lowerOrderBytes = (unsigned int) offset;
}

void compress2(cmp* dest, fmpz* original, fmpz tempTemp, int start, int end){
    unsigned int currentSizeType = 1;
    unsigned int currentLocation = 0;
    fmpz_zero(&tempTemp);
    pointer location = dest->beginning;
    for (int i = start; i <= end; i++) {
        if (i > start) {
            fmpz_sub(&tempTemp, original + i, original + i - 1);
        } else {
            fmpz_set(&tempTemp, original + i);
        }

        if ((currentSizeType > 8) && (fmpz_sgn(&tempTemp) == -1)) {
            printf("Large negative: ");
            fmpz_print(&tempTemp);
        }
        signed long v;
        if (fmpz_fits_si(&tempTemp)){
            v = fmpz_get_si(&tempTemp);
        } else {
            v = LONG_MIN; //force trigger size increase
        }
        switch (currentSizeType) {
            case 1:
                if ((v > SCHAR_MIN) && (v <= SCHAR_MAX)) {
                    signed char value = (signed char) v;//fmpz_get_si(&tempTemp);
                    //if (value != SCHAR_MIN) {
                        location.schar[currentLocation] = value;
                        currentLocation++;
                        break;
                    //}
                }
                location.schar[currentLocation] = SCHAR_MIN;
                currentLocation++;
                currentLocation = (currentLocation + 1) / 2;
                currentSizeType = 2;
            case 2:
                if ((v > SHRT_MIN) && (v <= SHRT_MAX)) {
                    short value = (short) v;//fmpz_get_si(&tempTemp);
                    //if (value != SHRT_MIN) {
                        location.sshort[currentLocation] = value;
                        currentLocation++;
                        break;
                    //}
                }
                location.sshort[currentLocation] = SHRT_MIN;
                currentLocation++;
                currentLocation = (currentLocation + 1) / 2;
                currentSizeType = 4;
            case 4:
                if ((v > INT_MIN) && (v <= INT_MAX)) {
                    int value = (int) v;//fmpz_get_si(&tempTemp);
                    //if (value != INT_MIN) {
                        location.sint[currentLocation] = value;
                        currentLocation++;
                        break;
                    //}
                }
                location.sint[currentLocation] = INT_MIN;
                currentLocation++;
                currentLocation = (currentLocation + 1) / 2;
                currentSizeType = 8;
            case 8:
                if ((v > LONG_MIN) && (v <= LONG_MAX)) {
                    signed long value = v;//fmpz_get_si(&tempTemp);
                    //if (value != LONG_MIN) {
                        location.slongp[currentLocation] = value;
                        currentLocation++;
                        break;
                    //}
                }
                location.slongp[currentLocation] = LONG_MIN;
                currentLocation++;
                currentSizeType = 16;
            default:;//a for loop could allow arbitrary sizing, get number of bits and determine what is necessary
                unsigned int loc = currentLocation;
                if (fmpz_sizeinbase(&tempTemp, 2) <= 8 * currentSizeType) {
                    int max = 1;
                    for (unsigned int bytes = 0; bytes < currentSizeType; bytes += 8) {
                        unsigned long value = fmpz_get_ui(&tempTemp);
                        if (value != ULONG_MAX) {
                            max = 0;
                        }
                        location.ulongp[currentLocation++] = value;
                        fmpz_tdiv_q_2exp(&tempTemp, &tempTemp, sizeof(unsigned long) * 8);
                    }
                    if (!max) {
                        break;
                    } else {
                        currentLocation = loc; //try again
                    }
                }
                for (int bytes = 0; bytes < currentSizeType; bytes += 8) {
                    location.ulongp[currentLocation++] = ULONG_MAX;
                }
                currentSizeType *= 2;
                i--;
        }
    }
    if(currentSizeType > 8){
        currentSizeType = 8;
    }
    dest->size = currentSizeType*currentLocation;
}