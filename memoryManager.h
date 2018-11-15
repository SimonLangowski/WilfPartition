//
// Created by Slang on 9/6/2018.
//

#ifndef WILF6_MEMORYMANAGER_H
#define WILF6_MEMORYMANAGER_H

#include <sys/mman.h>
#define PAGESIZE 4096
//40GB
//#define MAXNUMPAGES 10485760
//5gb
#define MAXNUMPAGES 1310720
//0.5MB
#define MAXGAPSIZE 128
//1GB
#define INITIALALLOCATION 262144

#define INDEXPAGEGAP 20

#include "outputThread.h"

typedef struct mmapSection{
    void* dataLocation; //data pointer from mmaping
    indexOffsetData* indexLocation;
    unsigned long minIndex;
    unsigned long maxIndex;
    unsigned long dataOffset;  //offset in bytes from beginning of file
    unsigned long indexOffset; //offset in indexes -- the first index of pointed by indexLocation
    struct mmapSection* nextInList; //doubly linkedlist
    struct mmapSection* prevInList;
    unsigned int block;
    unsigned long numPagesData;
    unsigned long numPagesIndex;
} mmapSection;

void memoryMappingInit();
void memoryMappingThreadInit(mmapSection**);
void memoryMappingRun(unsigned long, mmapSection**, const unsigned long*);
void* memoryAllocationThreadRun(void*);
void deallocateMapping(mmapSection*, int);
double allocateMapping(mmapSection*, unsigned long, double, int);
unsigned long readOffsetFromMap(mmapSection*, unsigned long);
unsigned long insertOrMergeInList(unsigned long, unsigned long, unsigned long, int);
void clearSentinel(mmapSection*);
void printTimestamp();
void findIndex(int*, unsigned long, int);
unsigned long getIndexDeleted(const int*, int);
void finishGuess(int*, int, int, int);
int incrementBases(int*, int);
#endif //WILF6_MEMORYMANAGER_H
