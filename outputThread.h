//
// Created by Slang on 9/6/2018.
//

#ifndef WILF6_OUTPUTTHREAD_H
#define WILF6_OUTPUTTHREAD_H

#include "main.h"
#include "compress.h"

#define INDEXOFFSETSIZE 6

typedef struct indexOffsetData{
    unsigned int lowerOrderBytes;
    unsigned short higherOrderBytes;
    unsigned short lengths[INDEXOFFSETSIZE - 1];
} indexOffsetData;

void* outputThreadRun(void*);
void cleanup(unsigned long, indexOffsetData*);
void writeOffset(indexOffsetData*, unsigned long);
void compress2(cmp*, fmpz*, fmpz, int, int);

#endif //WILF6_OUTPUTTHREAD_H
