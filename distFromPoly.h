//
// Created by Slang on 10/5/2018.
//

#ifndef WILF6_DISTFROMPOLY_H
#define WILF6_DISTFROMPOLY_H
#include "main.h"
#define MINLINESIZE ((MAXIMUM + 1)*sizeof(fmpz))

typedef struct layerCache{
    signed char* dataBeginning;
    signed char* dataEnding;
    signed char* lastData;
    signed char* currentData;
    signed char* nextData;
    pointer polys[POWEROFTWO];
} layerCache;

typedef struct polyThreadCache{
    fmpz totals[MAXIMUM + 1];
    fmpz resultTempStorage[MAXIMUM + 1];
    fmpz decompressTempStorage[MAXIMUM + 1];
    fmpz currentPoly[MAXIMUM + 1];
    layerCache* cyclicCache;
} polyThreadCache;

pointer getCyclicData(cycleCache*, unsigned int, int);
pointer getLayerData(layerCache*, unsigned int);
void compressToCyclicCache(cycleCache*, fmpz*, int, int, unsigned int);
void updateCycleOneBase(polyThreadCache*, unsigned short, unsigned short, unsigned short, cycleCache*, cycleCache*, unsigned short);
void updateCycle(unsigned short*, int, polyThreadCache*, cycleCache**, cycleCache**, unsigned short);
cycleCache* getNewCache();
void trimCache(cycleCache*);
fmpz* distFromPoly(int, unsigned short*, cycleCacheHolder*, unsigned short);
void addLastBaseAndMultiply(polyThreadCache*, unsigned short, unsigned short, unsigned short, cycleCache*, unsigned short);
unsigned long getSize(fmpz*, int, int);
void polyMultiplyClassicSparse(fmpz*, const fmpz*, int, const fmpz*, int, int);
void polyMultiplyTrivialSparse(fmpz*, const fmpz*, int, int, unsigned int);
void resetLayerCache(layerCache*);
void initLayerCaches();
#endif //WILF6_DISTFROMPOLY_H
