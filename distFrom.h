//
// Created by Slang on 9/6/2018.
//

#ifndef WILF6_DISTFROM_H
#define WILF6_DISTFROM_H
#include "main.h"
#include <gmp.h>
#define masterCutoff CUTOFF
typedef struct cacheEntry{
    fmpz array[MAXIMUM + 1];
    struct cacheEntry* next;
} cacheEntry;
//mpz_t* middleRecursionWrapper(int, int, ThreadVariables*, ThreadVariables*, ThreadCache*);
//mpz_t* middleRecursion(int, int, ThreadVariables*, ThreadVariables*, ThreadCache*);
/*cacheEntry* lowerRecursionWrapper(int,ThreadVariables*,int);
cacheEntry* lowerRecursion(int,int,ThreadVariables*,int);
cacheEntry* q2All(int, ThreadVariables*,int);
//int q2(int, ThreadVariables*);
cacheEntry* getZeroArray(int, int);
void freeMPZArray(cacheEntry*, int);
cacheEntry* getArray(int);
int computeMinSum(ThreadVariables*);*/
void distFromThreadInit(int);
void multiplyArrays(int, int, fmpz*, fmpz*);
void initCounts();
#endif //WILF6_DISTFROM_H
