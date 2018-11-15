//
// Created by Slang on 10/5/2018.
//

#include "distFromPoly.h"
#include "precompute.h"
#include "distToAndData.h"

polyThreadCache ptcs[NUMTHREADS];
layerCache layers[NUMTHREADS];

//have index thread create cycle caches before inserting jobs (and wait on cyclc cache holder if restricting memory)
//have output thread return cycle cache holders to pool


//when I get to 10000 I can measure the precompute size and specifically code caches for it
//and parallelize specifically for the case - how many threads and helper threads, and where the semaphores are

//but in general assume each thread has enough space for what it needs
//and I can store the cycles for the current threads
//and the cycle two back

void initLayerCaches(){
    for (int i = 0; i < NUMTHREADS; i++){
        ptcs[i].cyclicCache = &layers[i];
        layers[i].dataBeginning = (signed char*) malloc(CACHESIZE);
        layers[i].dataEnding = layers[i].dataBeginning + CACHESIZE;
        resetLayerCache(&layers[i]);
    }
}

fmpz* distFromPoly(int myThread, unsigned short* bases, cycleCacheHolder* cth, unsigned short maxDistance){
    //if in current cycle, use current cycle and last base to compute dist from
    polyThreadCache* ptc = &ptcs[myThread];
    if (targetNumTerms >= 2) {
        addLastBaseAndMultiply(ptc, bases[0], bases[targetNumTerms - 2], bases[targetNumTerms - 1], cth->cache, maxDistance);
    } else {
        addLastBaseAndMultiply(ptc, bases[0], bases[0], bases[targetNumTerms - 1], cth->cache, maxDistance);
    }
    return ptc->totals;
}

void compressToCache(layerCache* cache, fmpz* data, unsigned short minDegree, unsigned short maxDegree, unsigned int index){
    if (cache->dataEnding < cache->nextData + MINLINESIZE){
        //if close to end, ensure size will fit
        unsigned int size = getSize(data, minDegree, maxDegree);
        if (cache->nextData + size >= cache->dataEnding){
            if (cache->lastData > cache->nextData){
                printf("Cyclic cache full!\n");
                exit(1);
            }
            cache->nextData = cache->dataBeginning;
        }
    }
    cmp c;
    c.beginning.schar = cache->nextData;
    fmpz t;
    fmpz_init(&t);
    compress2(&c, data, t, 0, maxDegree - minDegree);
    fmpz_clear(&t);
    if ((cache->lastData >= cache->nextData) && (cache->nextData + c.size < cache->lastData)){
        printf("Cyclic cache full!\n");
        exit(1);
    }
    cache->polys[index] = c.beginning;
    cache->nextData += c.size;
}

void goToNextLayer(layerCache* l){
    l->lastData = l->currentData;
    l->currentData = l->nextData;
}

void resetLayerCache(layerCache* l){
    l->nextData = l->dataBeginning;
    l->currentData = l->dataBeginning;
    l->lastData = l->dataBeginning;
}

void addLastBaseAndMultiply(polyThreadCache* ptc, unsigned short smallestBase, unsigned short secondLargestBase, unsigned short lastBase, cycleCache* currentCycles, unsigned short maxDistanceLeft){
    resetLayerCache(ptc->cyclicCache);
    getPrecomputedData(ptc->totals, LCUTOFF, 0, maxDistanceLeft);
    //add the last base to the polynomials in current cycles and store in my storage
    //multiply the polynomial by the corresponding precomputed data and PIE add or subtract
    for (unsigned short i = 0; i < LCUTOFF; i++){
        pointer p = getCyclicData(currentCycles, i, 1);
        unsigned short multToAdd = i + 1;
        unsigned short minDegree = smallestBase * multToAdd;
        if (minDegree > maxDistanceLeft){
            continue;
        }
        unsigned short maxDegree = MIN(lastBase * multToAdd, maxDistanceLeft);
        unsigned short decompressMaxDegree = MIN(secondLargestBase * multToAdd, maxDistanceLeft);
        decompress(ptc->currentPoly, p, decompressMaxDegree - minDegree);
        for (int k = decompressMaxDegree + 1 - minDegree; k <= maxDegree - minDegree; k++){
            fmpz_zero(ptc->currentPoly+k);
        }
        ptc->currentPoly[lastBase*multToAdd - minDegree] = 1;
        compressToCache(ptc->cyclicCache, ptc->currentPoly, minDegree, maxDegree, i + cycleLayerShifts[1]);
        unsigned int complementIndex = pascalTable[LCUTOFF][1] - i - 1;
        int length = maxDegree - minDegree + 1;
        int complementLength = maxDistanceLeft - minDegree + 1;
        int outputLength = maxDistanceLeft - minDegree + 1;
        getPrecomputedData(ptc->decompressTempStorage, LCUTOFF - 1, complementIndex, complementLength);
        if (complementLength + 1 > length) {
            _fmpz_poly_mullow(ptc->resultTempStorage, ptc->decompressTempStorage, complementLength + 1, ptc->currentPoly, length, outputLength + 1);
        } else {
            _fmpz_poly_mullow(ptc->resultTempStorage, ptc->currentPoly, length, ptc->decompressTempStorage, complementLength + 1, outputLength + 1);
        }
        _fmpz_vec_sub(ptc->totals + minDegree, ptc->totals + minDegree, ptc->resultTempStorage, outputLength);

    }

    //add shifted the polynomials in my cache with the current cycle and store in my cache
    //multiply the polynomial by the corresponding precomputed data and PIE add or subtract

    for (int numTerms = 2; numTerms <= LCUTOFF; numTerms++){
        goToNextLayer(ptc->cyclicCache);
        for (unsigned int r = 0; r < pascalTable[LCUTOFF][numTerms]; r++) {
            //use stored minDegree and shift with first value to find my minDegree and max degree
            polyRecipe* myRecipe = &(recipes[numTerms][r*numTerms]);
            unsigned short myMSum = polyMsums[r + cycleLayerShifts[numTerms]];
            unsigned short minDegree = smallestBase * myMSum;
            if (minDegree > maxDistanceLeft) {
                continue;
            }
            unsigned short maxDegree = MIN(lastBase * myMSum, maxDistanceLeft);
            unsigned short decompressMaxDegree = MIN(secondLargestBase * myMSum, maxDistanceLeft);
            pointer p = getCyclicData(currentCycles, r, numTerms);
            //decompress directly to copy
            decompress(ptc->currentPoly, p, decompressMaxDegree - minDegree);
            for (int k = decompressMaxDegree + 1 - minDegree; k <= maxDegree - minDegree; k++){
                fmpz_zero(ptc->currentPoly+k);
            }
            for (int j = 0; j < numTerms; j++) {
                unsigned int index = (myRecipe+j)->index;
                unsigned short minD = (myRecipe+j)->shift * (lastBase - smallestBase);
                //minD = minD + minDegree, but subtracting minDegree when parameterizing for decompress
                p = getLayerData(ptc->cyclicCache, index + cycleLayerShifts[numTerms - 1]);
                decompressAdd2(ptc->currentPoly, p, minD, maxDegree - minDegree);
            }
            compressToCache(ptc->cyclicCache, ptc->currentPoly, minDegree, maxDegree, r + cycleLayerShifts[numTerms]);
            unsigned int complementIndex = pascalTable[LCUTOFF][numTerms] - r - 1;
            int length = maxDegree - minDegree + 1;
            int complementLength = maxDistanceLeft - minDegree + 1;
            int outputLength = maxDistanceLeft - minDegree + 1;
            //each complement begins at zero, so we need to decompress only up to maxDistanceLeft - minDegree
            //multiply ptc->resultTempStorage = ptc->decompressTempStorage *  ptc->currentPoly;
            switch(numTerms){
                case LCUTOFF:
                    //cutoff is complement with constant polynomial so no multiplication necessary
                    // add or subtract ptc->totals =  ptc->totals + ptc->resultTempStorage
                    if(LCUTOFF % 2 == 0){
                        _fmpz_vec_add(ptc->totals + minDegree, ptc->totals + minDegree, ptc->currentPoly, length);
                    } else {
                        _fmpz_vec_sub(ptc->totals + minDegree, ptc->totals + minDegree, ptc->currentPoly, length);
                    }
                    return;
/*                case (LCUTOFF - 1):
                    //cutoff - 1 has trivial complements
                    polyMultiplyTrivialSparse(ptc->resultTempStorage, ptc->currentPoly, length, outputLength, r);
                    break;
#if (LCUTOFF - 1) > 3
                case 3:
#endif
#if (LCUTOFF - 1) > 2
                case 2:
                    //polynomial is sparse so spare multiply
                    getPrecomputedData(ptc->decompressTempStorage, LCUTOFF - numTerms, complementIndex, complementLength);
                    polyMultiplyClassicSparse(ptc->resultTempStorage, ptc->currentPoly, length, ptc->decompressTempStorage, complementLength, outputLength);
                    break;
#endif*/
                default:
                    //len1 >= len2 > 0 and 0 < n <= len1 + len2 - 1.
                    getPrecomputedData(ptc->decompressTempStorage, LCUTOFF - numTerms, complementIndex, complementLength);
                    if (complementLength + 1 > length) {
                        _fmpz_poly_mullow(ptc->resultTempStorage, ptc->decompressTempStorage, complementLength + 1, ptc->currentPoly, length, outputLength + 1);
                    } else {
                        _fmpz_poly_mullow(ptc->resultTempStorage, ptc->currentPoly, length, ptc->decompressTempStorage, complementLength + 1, outputLength + 1);
                    }

            }
            // add or subtract ptc->totals =  ptc->totals + ptc->resultTempStorage
            if(numTerms % 2 == 0){
                _fmpz_vec_add(ptc->totals + minDegree, ptc->totals + minDegree, ptc->resultTempStorage, outputLength);
            } else {
                _fmpz_vec_sub(ptc->totals + minDegree, ptc->totals + minDegree, ptc->resultTempStorage, outputLength);
            }
        }
    }

}

void polyMultiplyClassicSparse(fmpz* dest, const fmpz* sparse, int length1, const fmpz* other, int length2,  int lengthDest){
    _fmpz_vec_zero(dest, lengthDest);
    for (int i = 0; i < length1; i++) {
        if (sparse[i] != 0) {
            for (int j = 0; j < MIN(lengthDest - i, length2); j++) {
                fmpz_addmul(dest + i + j, sparse + i, other + j);
            }
        }
    }
}

void polyMultiplyTrivialSparse(fmpz* dest, const fmpz* poly, int polyLength, int lengthDest, unsigned int increment){
    increment++;
    int a =  MIN(lengthDest, polyLength);
    for (int i = 0; i < a; i++){
        fmpz_set(dest + i, poly + i);
    }
    for (int i = a; i < lengthDest; i++){
        fmpz_zero(dest + i);
    }
    for (int i = increment; i < lengthDest; i++){
        fmpz_add(dest + i, dest + i, dest + i - increment);
    }
}

pointer getCyclicData(cycleCache* cache, unsigned int index, int numTerms){
    index += cycleLayerShifts[numTerms];
    pointer p;
    p.schar = &(cache->data[cache->indexes[index]]);
    return p;
}

pointer getLayerData(layerCache* l, unsigned int index){
    return l->polys[index];
}

//add shifted through the bases and update the currentCycles to the next set
//no multiplication or precomputed data necessary

//can't compress and decompress to the same block since it will get longer
//need to alternate between two blocks

//the first time the cycle is needed is the largest max dist
void updateCycle(unsigned short* bases, int length, polyThreadCache* ptc, cycleCache** c1, cycleCache** c2, unsigned short maxDist){
    unsigned short smallestBase = bases[0];
    cycleCache* tempCache = *c1;
    cycleCache* tempCache2 = *c2;
    memset(tempCache->indexes, 0, sizeof(unsigned int) * POWEROFTWO);
    tempCache->data[0] = 0;
    memset(tempCache2->indexes, 0, sizeof(unsigned int) * POWEROFTWO);
    tempCache2->data[0] = 0;
    tempCache->current = tempCache->data;
    tempCache2->current = tempCache2->data;
    for (int base = 0; base < length; base++){
        if (base >= 1) {
            updateCycleOneBase(ptc, smallestBase, bases[base - 1], bases[base], tempCache, tempCache2, maxDist);
        } else {
            updateCycleOneBase(ptc, smallestBase, bases[0], bases[0], tempCache, tempCache2, maxDist);
        }
        cycleCache* temptemptemp = tempCache2;
        tempCache2 = tempCache;
        tempCache = temptemptemp;
    }
    trimCache(tempCache);
    trimCache(tempCache2);
    *c1 = tempCache;
    *c2 = tempCache2;
}

void trimCache(cycleCache* c){
    unsigned long size = c->current - c->data;
    if (c->length - size < MINLINESIZE){
        //close enough
        return;
    }
    unsigned long asize;
    if (size < 2*MINLINESIZE){
        asize = 2*MINLINESIZE;
    } else {
        asize = size;
    }
    c->data = realloc(c->data, asize);
    c->current = c->data + size;
    c->length = asize;
}

cycleCache* getNewCache(){
    cycleCache* tempCache = (cycleCache*) malloc(sizeof(cycleCache));
    memset(tempCache, 0, sizeof(cycleCache));
    tempCache->data = (signed char*) malloc(PRECOMPUTEFILELENGTH);
    tempCache->current = tempCache->data;
    tempCache->length = PRECOMPUTEFILELENGTH;
    return tempCache;
}

//need to decompress base on size at compression or else reading garbage
//need to zero ptc->currentPoly

void updateCycleOneBase(polyThreadCache* ptc, unsigned short smallestBase, unsigned short secondLargestBase, unsigned short lastBase, cycleCache* currentCycles, cycleCache* next, unsigned short maxDistanceLeft){
    //add the last base to the polynomials in current cycles and store in my storage
    //multiply the polynomial by the corresponding precomputed data and PIE add or subtract
    next->current = next->data;
    for (unsigned short i = 0; i < LCUTOFF; i++){
        pointer p = getCyclicData(currentCycles, i, 1);
        unsigned short multToAdd = i + 1;
        unsigned short minDegree = smallestBase * multToAdd;
        if (minDegree > maxDistanceLeft){
            continue;
        }
        unsigned short maxDegree = MIN(lastBase * multToAdd, maxDistanceLeft);
        unsigned short decompressMaxDegree = MIN(secondLargestBase * multToAdd, maxDistanceLeft);
        decompress(ptc->currentPoly, p, decompressMaxDegree - minDegree);
        for (int k = decompressMaxDegree + 1 - minDegree; k <= maxDegree - minDegree; k++){
            fmpz_zero(ptc->currentPoly+k);
        }
        ptc->currentPoly[lastBase*multToAdd - minDegree] = 1;
        compressToCyclicCache(next, ptc->currentPoly, minDegree, maxDegree, i + cycleLayerShifts[1]);
    }
    //add shifted the polynomials in my cache with the current cycle and store in my cache
    //multiply the polynomial by the corresponding precomputed data and PIE add or subtract
    for (int numTerms = 2; numTerms <= LCUTOFF; numTerms++){
        for (unsigned int r = 0; r < pascalTable[LCUTOFF][numTerms]; r++) {
            //use stored minDegree and shift with first value to find my minDegree and max degree
            polyRecipe* myRecipe = &(recipes[numTerms][r*numTerms]);
            unsigned short myMSum = polyMsums[r + cycleLayerShifts[numTerms]];
            unsigned short minDegree = smallestBase * myMSum;
            if (minDegree > maxDistanceLeft) {
                continue;
            }
            unsigned short maxDegree = MIN(lastBase * myMSum, maxDistanceLeft);
            unsigned short decompressMaxDegree = MIN(secondLargestBase * myMSum, maxDistanceLeft);
            pointer p = getCyclicData(currentCycles, r, numTerms);
            //decompress directly to copy
            decompress(ptc->currentPoly, p, decompressMaxDegree - minDegree);
            for (int k = decompressMaxDegree + 1 - minDegree; k <= maxDegree - minDegree; k++){
                fmpz_zero(ptc->currentPoly+k);
            }
            for (int j = 0; j < numTerms; j++) {
                unsigned int index = (myRecipe+j)->index;
                unsigned short minD = (myRecipe+j)->shift * (lastBase - smallestBase);
                //minD = minD + minDegree, but subtracting minDegree when parameterizing for decompress
                p = getCyclicData(next, index, numTerms-1);
                decompressAdd2(ptc->currentPoly, p, minD, maxDegree - minDegree);
            }
            compressToCyclicCache(next, ptc->currentPoly, minDegree, maxDegree, r + cycleLayerShifts[numTerms]);
        }
    }
}

void compressToCyclicCache(cycleCache* cache, fmpz* data, int minDegree, int maxDegree, unsigned int loc){
    if (cache->data + cache->length < cache->current + MINLINESIZE){
        //if close to end, ensure size will fit
        unsigned long size = cache->current - cache->data;
        cache->length += MINLINESIZE * ((POWEROFTWO - loc)/2 + 1);
        cache->data = (signed char*) realloc(cache->data, cache->length);
        cache->current = cache->data + size;
    }
    cmp c;
    c.beginning.schar = cache->current;
    fmpz t;
    fmpz_init(&t);
    compress2(&c, data, t, 0, maxDegree-minDegree);
    fmpz_clear(&t);
    cache->indexes[loc] = cache->current - cache->data;
    cache->current += c.size;
}

unsigned long getSize(fmpz* original, int start, int end){
    unsigned int currentSizeType = 1;
    unsigned int currentLocation = 0;
    fmpz tempTemp;
    fmpz_zero(&tempTemp);
    for (int i = start; i <= end; i++) {
        if (i > start) {
            fmpz_sub(&tempTemp, original + i, original + i - 1);
        } else {
            fmpz_set(&tempTemp, original + i);
        }
        if ((currentSizeType > 8) && (fmpz_sgn(&tempTemp) == -1)) {
            printf("Large negative: ");
            fmpz_print(&tempTemp);
        }
        signed long v;
        if (fmpz_fits_si(&tempTemp)){
            v = fmpz_get_si(&tempTemp);
        } else {
            v = LONG_MIN; //force trigger size increase
        }
        switch (currentSizeType) {
            case 1:
                if ((v > SCHAR_MIN) && (v <= SCHAR_MAX)) {
                    currentLocation++;
                    break;
                    //}
                }
                currentLocation++;
                currentLocation = (currentLocation + 1) / 2;
                currentSizeType = 2;
            case 2:
                if ((v > SHRT_MIN) && (v <= SHRT_MAX)) {
                    currentLocation++;
                    break;
                    //}
                }
                currentLocation++;
                currentLocation = (currentLocation + 1) / 2;
                currentSizeType = 4;
            case 4:
                if ((v > INT_MIN) && (v <= INT_MAX)) {
                    currentLocation++;
                    break;
                    //}
                }
                currentLocation++;
                currentLocation = (currentLocation + 1) / 2;
                currentSizeType = 8;
            case 8:
                if ((v > LONG_MIN) && (v <= LONG_MAX)) {
                    currentLocation++;
                    break;
                    //}
                }
                currentLocation++;
                currentSizeType = 16;
            default:;//a for loop could allow arbitrary sizing, get number of bits and determine what is necessary
                unsigned int loc = currentLocation;
                if (fmpz_sizeinbase(&tempTemp, 2) <= 8 * currentSizeType) {
                    int max = 1;
                    for (unsigned int bytes = 0; bytes < currentSizeType; bytes += 8) {
                        unsigned long value = fmpz_get_ui(&tempTemp);
                        if (value != ULONG_MAX) {
                            max = 0;
                        }
                        fmpz_tdiv_q_2exp(&tempTemp, &tempTemp, sizeof(unsigned long) * 8);
                    }
                    if (!max) {
                        break;
                    } else {
                        currentLocation = loc; //try again
                    }
                }
                currentSizeType *= 2;
                i--;
        }
    }
    if(currentSizeType > 8){
        currentSizeType = 8;
    }
    fmpz_clear(&tempTemp);
    return currentSizeType*currentLocation;
}