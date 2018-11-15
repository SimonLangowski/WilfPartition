//
// Created by Slang on 8/18/2018.
//
#include "main.h"
#include "distFrom.h"

//I should move to one linked list for each thread
//if possible I should just figure out an upper bound of array usage and then statically allocate arrays
//2*cutoff - 5
//cacheEntry cache[NUMTHREADS][2*CUTOFF-5];
//cacheEntry* cacheFreeListHeads[NUMTHREADS];
fmpz finalCounts[NUMTHREADS][MAXIMUM + 1];
fmpz tempPoly[NUMTHREADS][MAXIMUM+1];

//if PIE is faster, then these will turn into threads that compute various consecutive inclusion exclusion pairs
//Each thread will take specific combinations of inclusion/exclusion
//They can start work when indexesReady
//have some sort of lock on each line to when they can apply their values to the inclusion exclusion
//and then post on valuesReady when done
//so each line would need a counter and a mutex


fmpz precomputedData[POWEROFTWO][MAXIMUM + 1];

/*void addShifted(fmpz*, fmpz*, int);

void distFromGlobalInit(){
    precomputedData[0][0] = 1;
    for (int jump = 1; jump <= 3; jump++){
        for (int j = jump; j <= MAXIMUM; j+= jump){
            precomputedData[jump][j] = 1;
        }
    }
    //1,2
    addShifted(precomputedData[4], precomputedData[1], 3);
    addShifted(precomputedData[4], precomputedData[2], 3);
    //1,3
    addShifted(precomputedData[5], precomputedData[1], 4);
    addShifted(precomputedData[5], precomputedData[3], 4);
    //2,3
    addShifted(precomputedData[6], precomputedData[2], 5);
    addShifted(precomputedData[6], precomputedData[3], 5);
    //1,2,3
    addShifted(precomputedData[7], precomputedData[4], 6);
    addShifted(precomputedData[7], precomputedData[5], 6);
    addShifted(precomputedData[7], precomputedData[6], 6);

    //cummulative additions
    for (int i = 1; i < 7; i++){
        addShifted(precomputedData[7], precomputedData[i], 0);
    }
    addShifted(precomputedData[4], precomputedData[1], 0);
    addShifted(precomputedData[4], precomputedData[2], 0);
    addShifted(precomputedData[5], precomputedData[1], 0);
    addShifted(precomputedData[5], precomputedData[3], 0);
    addShifted(precomputedData[6], precomputedData[2], 0);
    addShifted(precomputedData[6], precomputedData[3], 0);
    for (int i = 0; i <= 6; i++){ //add the zero array
        precomputedData[i][0] = 1;
    }

}

void addShifted(fmpz* dest, fmpz* src, int shift){
    for (int i = shift; i <= MAXIMUM; i++){
        fmpz_add(&dest[i], &dest[i], &src[i - shift]);
    }
}

void distFromThreadInit(int myThreadNum){
    cacheFreeListHeads[myThreadNum] = cache[myThreadNum];
    for (int i = 0; i < 2*CUTOFF-5; i++){
        cache[myThreadNum][i].next = &(cache[myThreadNum][i+1]);
    }
    cache[myThreadNum][2*CUTOFF-6].next = NULL;
}*/

void multiplyArrays(int myThreadNum, int minSum, fmpz* distTo, fmpz* distFrom){
    //multiply
    //dTo is zero below minSum -> shift pointer to distTo+minSum so from minSum to MAXIMUM is 0 to MAXIMUM - minSum
    //multiply by distFrom, which goes from 0 to MAXIMUM-minSum
    _fmpz_poly_mullow(tempPoly[myThreadNum], distTo + minSum, MAXIMUM - minSum + 1, distFrom, MAXIMUM - minSum + 1, MAXIMUM - minSum + 1);
    //then shift the values when adding over by minSum when adding to counts
    for (int i = 0; i <= MAXIMUM - minSum; i++){
        fmpz_add(finalCounts[myThreadNum] + minSum + i, finalCounts[myThreadNum] + minSum + i, tempPoly[myThreadNum] + i);
    }

    //mpz_vec_add is just a for loop so we can omit the extraneous function call
    //_fmpz_vec_add(finalCounts[myThreadNum] + minSum, finalCounts[myThreadNum] + minSum, tempPoly[myThreadNum], MAXIMUM - minSum + 1);
}

void printFinalCounts(){
    for (int j = 1; j < NUMTHREADS; j++){
        for (int i = 0; i <= MAXIMUM; i++){
            fmpz_add(finalCounts[0] + i, finalCounts[0] + i, finalCounts[j] + i);
        }
    }
    char* countsFileName = getAbsoluteFilePath(targetNumTerms, myBlockNumber, COUNT_FILE);
    FILE* countsFile = fopen(countsFileName, "a");
    for (int i = 0; i <= MAXIMUM; i++){
        fmpz_fprint(countsFile, finalCounts[0] + i);
        fprintf(countsFile, " ");
    }
    fprintf(countsFile, "\n");
    fclose(countsFile);
    free(countsFileName);
}

/*
void add(mpz_t* destination, mpz_t* input, int bsum, int base, ThreadCache* tc, int minBase){
    for (int i = 0; i < bsum; i++){
        mpz_set_ui(tc->cummulative[i], 0);
    }
    int shift = bsum + (masterCutoff - 1) * base;
    int initial = (shift + minBase) % bsum;
    for (int i = shift + minBase; i <= MAXIMUM; i++){
        mpz_add(tc->cummulative[initial], tc->cummulative[initial], input[i - shift]);
        mpz_add(destination[i], destination[i], tc->cummulative[initial]);
        if(++initial == bsum){
            initial = 0;
        }
    }
}*/
/*
cacheEntry* lowerRecursionWrapper(int maxDistanceLeft, ThreadVariables* t, int threadNum){
    cacheEntry* cacheEntry1 = q2All(maxDistanceLeft, t, threadNum);
    fmpz* a = cacheEntry1->array;
    for (int c = 3; c < masterCutoff; c++) {
        cacheEntry* cacheEntry2 = lowerRecursion(maxDistanceLeft, c, t, threadNum);
        fmpz* b = cacheEntry2->array;
        for (int j = 0; j <= maxDistanceLeft; j++) {
            fmpz_add(a + j, a + j, b + j);
        }
        freeMPZArray(cacheEntry2, threadNum);
    }
    return cacheEntry1;
}

void combineFunction(fmpz* destinationCounts, fmpz* inputCounts, int maxDistanceLeft, int currentDistanceLeft){
    int distanceTraveled = maxDistanceLeft - currentDistanceLeft;
    //_fmpz_vec_add(destinationCounts + distanceTraveled, destinationCounts + distanceTraveled, inputCounts, maxDistanceLeft - distanceTraveled + 1);
    for (int j = 0; j <= maxDistanceLeft - distanceTraveled; j++){
        fmpz_add(destinationCounts + distanceTraveled + j, destinationCounts + distanceTraveled + j, inputCounts + j);
    }
}

//mini caching will catch the same base being added with different multiplicities (ex 3 and 4)

//lower recursion does no caching, and calls q2 where the others call the recursion for the cutoff below them
cacheEntry* lowerRecursion(int maxDistanceLeft, int myMultiplicity, ThreadVariables* t, int threadNum){
    cacheEntry* cacheEntry1 = getZeroArray(maxDistanceLeft, threadNum);
    fmpz* counts = cacheEntry1->array;
    int lastSeen = 0;
    int baseDistanceLeft = maxDistanceLeft - myMultiplicity;
    for (int b = 1; b <= maxDistanceLeft; b++, baseDistanceLeft -= myMultiplicity){
        if (baseDistanceLeft < 0){
            break;
        }
        if(!t->increasing[b]){
            addSorted(t, b, lastSeen);
            cacheEntry* cacheEntry2 = q2All(baseDistanceLeft, t, threadNum);
            fmpz* values = cacheEntry2->array;
            //increment m
            //need to run on all values of m up to cutoff
            if (myMultiplicity + 1 < masterCutoff){ //check if m + 1 is within my range
                //recurse for m + 1
                cacheEntry* cacheEntry3 = lowerRecursion(baseDistanceLeft, myMultiplicity + 1, t, threadNum);
                fmpz* moreValues=cacheEntry3->array;
                for (int k = myMultiplicity + 2; k < masterCutoff; k++){
                    cacheEntry* cacheEntry4 = lowerRecursion(baseDistanceLeft, k, t, threadNum);
                    fmpz* moreMoreValues = cacheEntry4->array;
                    for (int l = 0; l <= baseDistanceLeft; l++){
                        fmpz_add(moreValues + l, moreValues + l, moreMoreValues + l);
                    }
                    freeMPZArray(cacheEntry4, threadNum);
                }
                combineFunction(counts, moreValues, maxDistanceLeft, baseDistanceLeft);
                freeMPZArray(cacheEntry3, threadNum);
            }
            combineFunction(counts, values, maxDistanceLeft, baseDistanceLeft);
            freeMPZArray(cacheEntry2, threadNum);
            deleteAtIndex(t,b);
        } else {
            lastSeen = b;
        }
    }
    return cacheEntry1;
}

cacheEntry* q2All(int maxDistanceLeft, ThreadVariables*t, int threadNum){
    cacheEntry* cacheEntry1 = getArray(threadNum);
    fmpz* counts = cacheEntry1->array;
    int cummulativeCount = 1;
    int evenBaseCount = 0;
    int oddBaseCount = 0;
    for (int d = 1; d <= (maxDistanceLeft / 2); d++) {
        //no need to add for odd
        //no need to divide by 2 to check for odd (since 2*base will always first trigger at even
        //odd bases
        if (t->increasing[2*d-1] != 0){
            oddBaseCount++;
        }
        fmpz_set_si(counts + (2*d - 1), (cummulativeCount - oddBaseCount));

        //divide by 2, add 1
        //have counter that increments every other loop (or loop unroll twice)
        cummulativeCount++;
        //for each base subtract one from the numbers greater than equal to 2 * base
        if (t->increasing[d] != 0){
            cummulativeCount--;
        }
        //for bases of the same parity (loop unroll twice)
        //for each base subtract one from the numbers greater than equal to base
        //even bases
        if (t->increasing[2*d] != 0){
            evenBaseCount++;
        }
        fmpz_set_si(counts + (2*d), (cummulativeCount - evenBaseCount));

    }
    if ((maxDistanceLeft % 2) == 1){
        if (t->increasing[maxDistanceLeft] != 0){
            oddBaseCount++;
        }
        fmpz_set_si(counts + maxDistanceLeft, cummulativeCount - oddBaseCount);
    }

    //add one for each number that is 2 * a base plus another base - also need to add one back to each base * 3 (or not subtract one later)
    int currentBase = t->increasing[0];
    while (currentBase != THREADVARSIZE){
        int nextBase = t->increasing[0];
        while (nextBase != THREADVARSIZE){
            if (2*currentBase + nextBase <= maxDistanceLeft) {
                fmpz_add_ui(counts + (2 * currentBase + nextBase), counts + (2 * currentBase + nextBase), (unsigned long) 1);
            } else {
                break;
            }
            nextBase = t->increasing[nextBase];
        }
        if (3 * currentBase <= maxDistanceLeft){
            fmpz_add_ui(counts + (3 * currentBase), counts + (3 * currentBase), (unsigned long) 1);
        } else if (2 * currentBase > maxDistanceLeft){
            break;
        }
        currentBase = t->increasing[currentBase];
    }
    //subtract one if multiple of three - even if no bases? - yes because counts a pair of (a)(1)+(a)(2)
    //temporary decrement if multiple of three (or loop unroll thrice)
    for (int i = 3; i <= maxDistanceLeft; i = i + 3){
        fmpz_sub_ui(counts + i, counts + i, (unsigned long) 1);
    }
    fmpz_set_ui(counts + 0, (unsigned long) 1);
    return cacheEntry1;
}


cacheEntry* getArray(int cacheNum){
    cacheEntry* nextFree = cacheFreeListHeads[cacheNum];
    cacheFreeListHeads[cacheNum]=nextFree->next;
    return nextFree;
}

cacheEntry* getZeroArray(int size, int cacheNum){
    cacheEntry* array = getArray(cacheNum);
    for (int i = 0; i <= size; i++){
        fmpz_zero(array->array + i);
    }
    return array;
}

void freeMPZArray(cacheEntry* array, int cacheNum){
    array->next = cacheFreeListHeads[cacheNum];
    cacheFreeListHeads[cacheNum] = array;
}

int computeMinSum(ThreadVariables* myBases){
    int startingMultiplicity = targetNumTerms - 1 + CUTOFF;
    int currentBase = myBases->increasing[0];
    int minSum = 0;
    while (currentBase != THREADVARSIZE){
        minSum += (currentBase) * (startingMultiplicity);
        startingMultiplicity --;
        currentBase = myBases->increasing[currentBase];
    }
    return minSum;
}
*/
void initCounts(){
    //no initialization of fmpz necessary - it just sets the slong value to 0 and its global already
    /*for (int myThreadNum = 0; myThreadNum < NUMCOMPUTETHREADS; myThreadNum++) {
        for (int i = 0; i <= MAXIMUM; i++) {
            mpz_init(finalCounts[myThreadNum][i]);
        }
        for (int i = 0; i < 2 * CUTOFF - 5; i++) {
            for (int j = 0; j <= MAXIMUM; j++) {
                mpz_init(cache[myThreadNum][i].array[j]);
            }
        }
    }*/
}