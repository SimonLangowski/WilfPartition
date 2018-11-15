//
// Created by Slang on 9/6/2018.
//

#include "indexThread.h"
#include "main.h"
#include "outputThread.h"
#include "computeBorders.h"

unsigned long stopIndex;

unsigned long getLastIndex(int, unsigned long);

unsigned short bases[MAXNUMTERMS];
int indexMakerHead = 0;

cycleCache* secondBaseCache;
sem_t cycleHoldersFree;
cycleCacheHolder storedCycles[NUMSTOREDCYCLES];
int currentCycleHolder = NUMSTOREDCYCLES - 1;
polyThreadCache myPTC;

void* indexThreadRun(void* arg){
    sem_init(&cycleHoldersFree, 0, NUMSTOREDCYCLES);
    //find min index
    goToMinIndexI();
    int startingNumber = targetNumTerms + CUTOFF - 1;
    unsigned short total = 0;
    for (int i = 0; i < targetNumTerms; i++){
        total += bases[i] * startingNumber;
        startingNumber--;
    }
    secondBaseCache = getNewCache();
    for (int i = 0; i < NUMSTOREDCYCLES; i++){
        storedCycles[i].cache = getNewCache();
    }

    //update secondBaseCache
    //generate cycle
    cycleCacheHolder* currentCTH = getNextHolder(0);
    currentCTH->lastIndex = getLastIndex(total, MININDEX);
    unsigned short mS = total;
    //check for lower minsums
    if (targetNumTerms > 2){
        unsigned short tempB[targetNumTerms];
        memcpy(tempB, bases, sizeof(unsigned short) * targetNumTerms);
        tempB[targetNumTerms - 2]++;
        tempB[targetNumTerms - 1] = tempB[targetNumTerms - 2] + 1;
        unsigned short possibleMsum = 0;
        startingNumber = targetNumTerms + CUTOFF - 1;
        for (int i = 0; i < targetNumTerms; i++){
            possibleMsum += tempB[i] * startingNumber;
            startingNumber--;
        }
        if (possibleMsum < mS){
            mS = possibleMsum;
        }
    }

    updateCycle(bases, targetNumTerms - 1, &myPTC, &currentCTH->cache, &secondBaseCache, MAXIMUM - mS);
    currentCTH->lastIndex = getLastIndex(total, MININDEX);
    unsigned long currentIndex = MININDEX;
    while(1){
        //wait for space
        sem_wait(&bufferEntriesFree);
        //generate thread variables and indexes
        memcpy(buffer[indexMakerHead].bases, bases, targetNumTerms*sizeof(unsigned short));
        buffer[indexMakerHead].outputIndex = currentIndex;
        buffer[indexMakerHead].minSum = total;
        buffer[indexMakerHead].cth = currentCTH;
        sem_post(&jobsInQueue);
        //go to next index
        currentIndex++;
        if (currentIndex >= MAXINDEX){
            stopIndex = MAXINDEX;
            break;
        }
        time_t currentTime;
        time(&currentTime);
        if (currentTime > endTime){
            //continue until multiple of index grouping is completed
            if (currentIndex % INDEXOFFSETSIZE == INDEXOFFSETSIZE) {
                stopIndex = currentIndex;
                break;
            }
        }
        indexMakerHead++;
        if (indexMakerHead >= BUFFERSIZE){
            indexMakerHead = 0;
        }
        //do increment
        int currentIncrementPosition = targetNumTerms - 1;
        while(1){
            //try increment
            unsigned short baseNumber = bases[currentIncrementPosition] + (unsigned short) 1;
            for (int i = currentIncrementPosition; i < targetNumTerms; i++){
                bases[i] = baseNumber;
                baseNumber++;
            }
            startingNumber = targetNumTerms + CUTOFF - 1;
            total = 0;
            for (int i = 0; i < targetNumTerms; i++){
                total += bases[i] * startingNumber;
                startingNumber--;
            }
            if (total <= MAXIMUM){
                break;
            }
            //move increment position
            currentIncrementPosition--;
            if (currentIncrementPosition < 0){
                printf("Reached end of index possibilities\n");
                return NULL;
            }
        }
        //use current increment position to determine things
        if (currentIncrementPosition < targetNumTerms - 2){
            //update second cycle
            currentCTH = getNextHolder(currentIndex - 1);
            updateCycle(bases, targetNumTerms - 1, &myPTC, &currentCTH->cache, &secondBaseCache, MAXIMUM - total);
            currentCTH->lastIndex = getLastIndex(total, currentIndex);
        } else if (currentIncrementPosition < targetNumTerms - 1){
            //use next cycle holder and update
            currentCTH = getNextHolder(currentIndex - 1);
            if (targetNumTerms - 3 >= 0) {
                updateCycleOneBase(&myPTC, bases[0], bases[targetNumTerms - 3], bases[targetNumTerms - 2], secondBaseCache, currentCTH->cache, MAXIMUM - total);
            } else {
                updateCycleOneBase(&myPTC, bases[0], bases[0], bases[0], secondBaseCache, currentCTH->cache, MAXIMUM - total);
            }
            currentCTH->lastIndex = getLastIndex(total, currentIndex);
        }
    }
    stop(1);
}

unsigned long getLastIndex(int minSum, unsigned long currentIndex){
    int numIncrements = ((MAXIMUM - minSum) / CUTOFF);
    return currentIndex + numIncrements;
}

void stop(int s){
    for (int i = 0; i < BUFFERSIZE; i++){
        indexMakerHead++;
        if (indexMakerHead >= BUFFERSIZE){
            indexMakerHead = 0;
        }
        sem_wait(&bufferEntriesFree);
        buffer[indexMakerHead].quit = s;
        sem_post(&jobsInQueue);
    }
}

cycleCacheHolder* getNextHolder(unsigned long last){
    //storedCycles[currentCycleHolder].lastIndex = last;
    currentCycleHolder++;
    if (currentCycleHolder >= NUMSTOREDCYCLES){
        currentCycleHolder = 0;
    }
    sem_wait(&cycleHoldersFree);
    cycleCacheHolder* cth = &storedCycles[currentCycleHolder];
    cycleCache* c = cth->cache;
    c->current = c->data;
    return cth;
}

void goToMinIndexI(){
    finishGuessI(1, 0);
    unsigned short minimumBaseGuess = 1;
    for (unsigned short currentTermIndex = 0; currentTermIndex < targetNumTerms; currentTermIndex++) {
        unsigned short currentBaseGuess = minimumBaseGuess;
        while (1) {
            finishGuessI(currentBaseGuess, currentTermIndex);
            unsigned long currentIndex = getIndex3(bases, targetNumTerms);
            if (currentIndex == MININDEX){
                return;
            } else if (currentIndex > MININDEX){
                currentBaseGuess--;
                finishGuessI(currentBaseGuess, currentTermIndex);
                break;
            } else {
                currentBaseGuess++;
            }
        }
        minimumBaseGuess = currentBaseGuess + (unsigned short)1;
    }
}

void finishGuessI(unsigned short minBase, unsigned short location){
    for (unsigned short i = location; i < targetNumTerms; i++){
        bases[i] = minBase + (i - location);
    }
}