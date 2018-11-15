//
// Created by Slang on 9/7/2018.
//

#ifndef WILF6_COMPUTEBORDERS_H
#define WILF6_COMPUTEBORDERS_H
typedef struct indexInfo{
    unsigned long index;
    unsigned short minSum;
} indexInfo;

void getIndex(ThreadVariables*, int, indexInfo*);
unsigned long getIndex3(unsigned short*, int);
unsigned long getIndex2(int*, int);
unsigned long getValueFromTree(int, int, int);
void makeTree(int);
#ifdef targetNumTerms
#define maxMaxNumTerms (targetNumTerms+1)
#else
#define maxMaxNumTerms 40
#endif

#define masterCutoff CUTOFF
extern unsigned long maximumIndexes[maxMaxNumTerms];
#endif //WILF6_COMPUTEBORDERS_H
