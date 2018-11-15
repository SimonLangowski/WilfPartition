//
// Created by Slang on 9/6/2018.
//


#ifndef WILF6_DISTTOANDDATA_H
#define WILF6_DISTTOANDDATA_H
#include "main.h"
#include "outputThread.h"
#include "compress.h"

unsigned long readOffset(indexOffsetData*);
void decompressAdd2(fmpz*, pointer, int, int);
void debugCopy(int, mpz_t*);
void debugSubtract(int, mpz_t*, unsigned long, int, int, int);
#endif //WILF6_DISTTOANDDATA_H
