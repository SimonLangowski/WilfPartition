//
// Created by Slang on 10/5/2018.
//

#ifndef WILF6_PRECOMPUTE_H
#define WILF6_PRECOMPUTE_H

#include "main.h"
#include "distFromPoly.h"


typedef struct polyRecipe{
    unsigned int index;
    unsigned short shift;
} polyRecipe;

typedef struct fmpzArray{
    fmpz a[MAXIMUM + 1];
} fmpzArray;

void getPrecomputedData(fmpz*, int, unsigned int, int);
extern unsigned int pascalTable[LCUTOFF + 1][LCUTOFF + 1];
extern polyRecipe* recipes[LCUTOFF+1];

extern unsigned int cycleLayerShifts[LCUTOFF + 1];
extern unsigned short polyMsums[POWEROFTWO];
void decompress(fmpz*, pointer, int);
int incrementBasesPrecompute(unsigned int*, int);
unsigned int getDeletedPrecomputeIndex(const unsigned int*, int, int);
void precomputeFollowRecipe(polyRecipe*, fmpz*, int, int);
void addShifted(fmpz*, fmpz*, int, int);
void precomputeAddChildren(polyRecipe*, fmpz*, int);
void recursiveAdd(fmpz*, int, unsigned short*, unsigned short*, int, int);
void readPrecompute();
void precompute();
unsigned int getPrecomputeIndex(const unsigned short*, int);
#endif //WILF6_PRECOMPUTE_H
