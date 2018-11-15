#include "main.h"
#include "computeBorders.h"
unsigned long* table;
unsigned long** indexLookupTree[maxMaxNumTerms];
unsigned long maximumIndexes[maxMaxNumTerms];

unsigned long getValueFromTable(int, int, int);
void setValueInTable(int,int,int,unsigned long);
unsigned long computeValue(int,int,int);
void makeTreeFromTable(int);

void makeTree(int maxNumTerms){
    table = (unsigned long*) calloc((maxNumTerms + 1) * (MAXIMUM / masterCutoff + 1) * (MAXIMUM + 1), sizeof(unsigned long));
    if (table == NULL){
        printf("Not enough memory for table\n");
        exit(1);
    }
    maximumIndexes[0] = 1;
    for (int i = 1; i <= maxNumTerms; i++){
        maximumIndexes[i] = computeValue(1, MAXIMUM, i);
    }
    for (int i = 1; i <= maxNumTerms; i++){
        makeTreeFromTable(i);
    }
    free(table);
}

unsigned long getValueFromTree(int numTerms, int minBase, int maxDistLeft){
    if (numTerms == 0){
        return 1;
    }
    unsigned long offset = indexLookupTree[numTerms][minBase - 1][0];
    if (maxDistLeft < offset){
        return 0;
    }
    return indexLookupTree[numTerms][minBase - 1][maxDistLeft - offset];
}

unsigned long getValueFromTable(int numTerms, int minBase, int maxDistLeft){
    return table[numTerms * ((MAXIMUM/masterCutoff + 1)*(MAXIMUM + 1)) + minBase * (MAXIMUM + 1) + maxDistLeft];
}

void setValueInTable(int numTerms, int minBase, int maxDistLeft, unsigned long value){
    table[numTerms * ((MAXIMUM/masterCutoff + 1)*(MAXIMUM + 1)) + minBase * (MAXIMUM + 1) + maxDistLeft] = value;
}

void makeTreeFromTable(int numTerms){
    unsigned long** tempMinBaseArray = (unsigned long**) malloc(sizeof(unsigned long*) * ((MAXIMUM / masterCutoff) + 1));
    int allZeroRows = 0;
    for (int j = 1; j <= MAXIMUM / masterCutoff; j++){
        int allZeroColumns = 1;
        for (int k = 1; k <= MAXIMUM; k++){
            if ((allZeroColumns) && (getValueFromTable(numTerms, j, k) != 0)){
                allZeroColumns = 0;
                allZeroRows = 1;
                unsigned long offset = k - 1;
                tempMinBaseArray[j - 1] = (unsigned long*) malloc(sizeof(unsigned long) * (MAXIMUM - k + 1 + 1));
                tempMinBaseArray[j - 1][0] = offset;
                while (k <= MAXIMUM){
                    tempMinBaseArray[j - 1][k-offset] = getValueFromTable(numTerms, j, k);
                    k++;
                }
                break;
            }
        }
        if (allZeroColumns && allZeroRows){
            indexLookupTree[numTerms] = (unsigned long**) realloc(tempMinBaseArray, sizeof(unsigned long*) * (j - 1));
            break;
        }
    }
}

void getIndex(ThreadVariables* t, int numTerms, indexInfo* i){
    int startingMultiplicity = numTerms + masterCutoff - 1;
    int currentDistLeft = MAXIMUM;
    unsigned long spacesSkipped = 0;
    int minBase = 1;
    int currentBase = t->increasing[0];
    while (currentBase < THREADVARSIZE){
        for (int j = minBase; j < currentBase; j++){
            //spacesSkipped += computeValue(j + 1, currentDistLeft - startingMultiplicity * j, numTerms - 1);
            spacesSkipped += getValueFromTree(numTerms - 1, j + 1, currentDistLeft - startingMultiplicity * j);
        }
        currentDistLeft -= currentBase * startingMultiplicity;
        startingMultiplicity--;
        numTerms--;
        minBase = currentBase + 1;
        currentBase = t->increasing[currentBase];
    }
    i->index = spacesSkipped;
    i->minSum = MAXIMUM - currentDistLeft;
}

unsigned long getIndex2(int* bases, int numTerms){
    int startingMultiplicity = numTerms + masterCutoff - 1;
    int currentDistLeft = MAXIMUM;
    unsigned long spacesSkipped = 0;
    int minBase = 1;
    int totalTerms = numTerms;
    for (int i = 0; i < totalTerms; i++){
        int currentBase = bases[i];
        for (int j = minBase; j < currentBase; j++){
            spacesSkipped += getValueFromTree(numTerms - 1, j + 1, currentDistLeft - startingMultiplicity * j);
        }
        currentDistLeft -= currentBase * startingMultiplicity;
        startingMultiplicity--;
        numTerms--;
        minBase = currentBase + 1;
    }
    return spacesSkipped;
}

unsigned long getIndex3(unsigned short* bases, int numTerms){
    int startingMultiplicity = numTerms + masterCutoff - 1;
    int currentDistLeft = MAXIMUM;
    unsigned long spacesSkipped = 0;
    int minBase = 1;
    int totalTerms = numTerms;
    for (int i = 0; i < totalTerms; i++){
        int currentBase = bases[i];
        for (int j = minBase; j < currentBase; j++){
            spacesSkipped += getValueFromTree(numTerms - 1, j + 1, currentDistLeft - startingMultiplicity * j);
        }
        currentDistLeft -= currentBase * startingMultiplicity;
        startingMultiplicity--;
        numTerms--;
        minBase = currentBase + 1;
    }
    return spacesSkipped;
}

unsigned long computeValue(int minBase, int maxDistLeft, int numTerms){
    if (getValueFromTable(numTerms, minBase, maxDistLeft) != 0){
        //System.out.println("Returned " + getValueFromTable(numTerms, minBase, maxDistLeft) + " for " + numTerms + " terms, minBase " + minBase + ", distLeft " + maxDistLeft);
        return getValueFromTable(numTerms, minBase, maxDistLeft);
    }
    unsigned long val = 0;
    if (numTerms == 0){
        return 1;
    } else if (numTerms == 1){
        int val2;
        val2 = (maxDistLeft / masterCutoff) - minBase + 1;
        if (val2 < 0){
            val = 0;
        } else {
            val = (unsigned long) val2;
        }
    } else {
        int boundary = maxDistLeft / (masterCutoff + numTerms - 1);
        for (int k = minBase; k <= boundary; k++){
            unsigned long possibilities = computeValue(k + 1, maxDistLeft - ((masterCutoff + numTerms - 1) * k), numTerms - 1);
            if (possibilities == 0){
                break;
            }
            val += possibilities;
        }
    }
    setValueInTable(numTerms, minBase, maxDistLeft, val);
    //System.out.println("Returned " + getValueFromTable(numTerms, minBase, maxDistLeft) + " for " + numTerms + " terms, minBase " + minBase + ", distLeft " + maxDistLeft);
    return val;
}