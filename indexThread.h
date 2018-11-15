//
// Created by Slang on 9/6/2018.
//

#ifndef WILF6_INDEXTHREAD_H
#define WILF6_INDEXTHREAD_H

#include "distFromPoly.h"

extern unsigned long stopIndex; //index where computation runs out of time
void* indexThreadRun(void*);
void stop(int);
void goToMinIndexI();
void finishGuessI(unsigned short, unsigned short);
cycleCacheHolder* getNextHolder(unsigned long);
#endif //WILF6_INDEXTHREAD_H
