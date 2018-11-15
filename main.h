//
// Created by Slang on 9/6/2018.
//

#ifndef WILF6_MAIN_H
#define WILF6_MAIN_H

#define _GNU_SOURCE

#include <stdio.h>
#include <gmp.h>
#include <string.h>
#include <pthread.h>
#include <semaphore.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/resource.h>
#include <fcntl.h>
#include <unistd.h>
#include <time.h>
#include <errno.h>
#include <flint.h>
#include "fmpz.h"
#include "fmpz_poly.h"
#include "fmpz_vec.h"

#define debug

//number of blocks in previous layer!
#ifndef NUMBLOCKS
#define NUMBLOCKS 1
#endif

#ifndef targetNumTerms
extern int targetNumTerms;
#endif

#define MAXIMUM 1000
#define CUTOFF 11
#define NUMTHREADS 19
#define LCUTOFF (CUTOFF - 1)

#define NUMSTOREDCYCLES NUMTHREADS

#ifdef targetNumTerms
#define MAXNUMTERMS targetNumTerms
#else
#define MAXNUMTERMS 16
#endif

#define BUFFERSIZE (3*NUMTHREADS)
#define THREADVARSIZE (MAXIMUM+1)
#define HOURS 3
#define MINUTES 50
//#define NULL 0
//#define BASEFILEPATH "/scratch/rice/s/slangows/wilf/%d-%d/%d/%d/%s"
//#define BASEFILEPATH "/scratch/mentors/mdw/simon/wilf6/%d-%d/%d/%d/%s"
#define BASEFILEPATH "/scratch/mentors/mdw/simon/polyWilf7/%d-%d/%d/%d/%s"
//2^lcutoff
#define POWEROFTWO 1024
//size for each thread's cache
#define CACHESIZE (262144*PAGESIZE)

#define PRECOMPUTEFILELENGTH 2703080
#define PRECOMPUTEINDEXLENGTH (sizeof(unsigned int)*POWEROFTWO)

typedef struct ThreadVariables{
    //these structures store the bases used in a doubly linked list in an array
    //ex store 1 4 5 7
    //index        0 1 2 3 4 5 6 7    8 ... MAXIMUM
    //increasing   1 4 0 0 5 7 0 MAXIMUM  0 ... MAXIMUM
    //decreasing   0 0 0 0 1 4 0 5    0 ...  7
    int increasing[THREADVARSIZE + 1]; //each index contains the value of the next base in increasing order
    int decreasing[THREADVARSIZE + 1]; //each index contains the value of the next base in decreasing order
} ThreadVariables;

extern unsigned long MININDEX;
extern unsigned long MAXINDEX;
extern time_t endTime;
extern unsigned int myBlockNumber;
extern FILE* logFile;
extern pthread_mutex_t debugMutex;

extern sem_t bufferEntriesFree;
extern sem_t jobsInQueue;

extern sem_t cycleHoldersFree;

#include "memoryManager.h"

typedef struct cycleCache{
    signed char* data;
    signed char* current;
    unsigned int length;
    unsigned int indexes[POWEROFTWO];
} cycleCache;

typedef struct cycleCacheHolder{
    cycleCache* cache;
    unsigned long lastIndex;
} cycleCacheHolder;

typedef struct LineEntry{
    //this struct stores shared data
    sem_t countsComputed;
    unsigned long outputIndex;
    cycleCacheHolder* cth;
    fmpz distToValues[MAXIMUM + 1];
    unsigned short bases[MAXNUMTERMS];
    unsigned short minSum;
    unsigned short bsum;
    char quit;
} LineEntry;

extern LineEntry buffer[BUFFERSIZE];

void initThreadVariables(ThreadVariables*);
void copyThreadVariables(ThreadVariables*, ThreadVariables*);


void addSorted(ThreadVariables*,int,int);
void deleteAtIndex(ThreadVariables*,int);
int computeInverse();
int getMinVal(int, int);
void printThreadVariables(ThreadVariables*);
void printMPZArray(unsigned long, mpz_t*, int, int, int);
int computeBsum(ThreadVariables*);
void printFinalCounts();
void printMetaData();
void printZero();
void initializeLineEntry(LineEntry*);
void makeNextJob();
void resetThreadVariables(ThreadVariables*);
void getIndexDeletedData(LineEntry*, int, unsigned long*, int*);
void* computeThread(void*);

enum fileType{
    INDEX_FILE,
    DATA_FILE,
    LOG_FILE,
    STAT_FILE,
    COUNT_FILE,
    LENGTH_FILE,
    FINAL_FILE
};

char* getAbsoluteFilePath(int, unsigned int, enum fileType);
#endif //WILF6_MAIN_H
