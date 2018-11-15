//
// Created by Slang on 9/6/2018.
//

#include "distToAndData.h"
#include "main.h"
#include "compress.h"
#include "outputThread.h"

unsigned long readOffset(indexOffsetData* iod){
    unsigned long offset = 0;
    offset += iod->higherOrderBytes;
    offset = offset << 32;
    offset += iod->lowerOrderBytes;
    return offset;
}

void decompressAdd2(fmpz* dest, pointer compressed, int start, int end){
    fmpz summand;
    fmpz_init(&summand);
    unsigned int currentSizeType = 1;
    unsigned int lastLocation = 0;
    for (int j = start; j <= end; j++){
        switch(currentSizeType){
            case 1: //signed char
                if (compressed.schar[lastLocation] == SCHAR_MIN){
                    currentSizeType = 2;
                    lastLocation = (lastLocation + 1 + 1) / 2;
                } else {
                    fmpz_add_si(&summand, &summand, (long)(compressed.schar[lastLocation]));
                    fmpz_add(dest + j, dest + j, &summand);
                    lastLocation++;
                    break;
                }
                //go to next case
            case 2: //short
                if (compressed.sshort[lastLocation] == SHRT_MIN){
                    currentSizeType = 4;
                    lastLocation = (lastLocation + 1 + 1) / 2;
                } else {
                    fmpz_add_si(&summand, &summand, (long)(compressed.sshort[lastLocation]));
                    fmpz_add(dest + j, dest + j, &summand);
                    lastLocation++;
                    break;
                }
                //go to next case
            case 4: //int
                if (compressed.sint[lastLocation] == INT_MIN){
                    currentSizeType = 8;
                    lastLocation = (lastLocation + 1 + 1) / 2;
                } else {
                    fmpz_add_si(&summand, &summand, (long)(compressed.sint[lastLocation]));
                    fmpz_add(dest + j, dest + j, &summand);
                    lastLocation++;
                    break;
                }
                //go to next case
            case 8: //long
                if (compressed.slongp[lastLocation] == LONG_MIN){
                    currentSizeType = 16;
                    lastLocation++;
                } else {
                    fmpz_add_si(&summand, &summand, compressed.slongp[lastLocation]);
                    fmpz_add(dest + j, dest + j, &summand);
                    lastLocation++;
                    break;
                }
                //go to next case
            default: ;
                int max = 1;
                fmpz tempTemp;
                fmpz tempTemp2;
                fmpz_init(&tempTemp);
                fmpz_init(&tempTemp2);
                for (unsigned int bytes = 0; bytes < currentSizeType; bytes += 8){
                    fmpz_set_ui(&tempTemp2, compressed.ulongp[lastLocation++]);
                    fmpz_mul_2exp(&tempTemp2, &tempTemp2, bytes * 8);
                    fmpz_add(&tempTemp, &tempTemp, &tempTemp2);
                    if(fmpz_get_ui(&tempTemp2) != ULONG_MAX){
                        max = 0;
                    }
                }
                if (!max){
                    fmpz_add(&summand, &summand, &tempTemp2);
                    fmpz_add(dest + j, dest + j, &summand);
                } else {
                    currentSizeType *= 2;
                    j--;
                }
                fmpz_clear(&tempTemp);
                fmpz_clear(&tempTemp2);
        }

    }
    fmpz_clear(&summand);
}
/*
mpz_t debugArrays[NUMDATATHREADS][MAXIMUM + 1];

void debugCopy(int t, mpz_t* a){
    if (debugArrays[t][0] == NULL){
        for (int i = 0; i <= MAXIMUM; i++){
            mpz_init(debugArrays[t][i]);
        }
    }
    for (int i = 0; i <= MAXIMUM; i++){
        mpz_set(debugArrays[t][i], a[i]);
    }
}

void debugSubtract(int t, mpz_t* a, unsigned long index, int start, int end, int shift){
    for (int i = 0; i <= MAXIMUM; i++){
        mpz_sub(debugArrays[t][i], a[i], debugArrays[t][i]);
    }
    pthread_mutex_lock(&debugMutex);
    printf("D: (%d)", shift);
    printMPZArray(index, debugArrays[t], start, end, targetNumTerms - 1);
    pthread_mutex_unlock(&debugMutex);
}
*/