#include <stdio.h>
#include <stdlib.h>
#include <gmp.h>

#include "main.h"

int main(){
    mpz_t* totals = (mpz_t*) malloc(sizeof(mpz_t) * (MAXIMUM + 1));
    for (int i = 0; i <= MAXIMUM; i++){
        mpz_init(totals[i]);
    }
    mpz_t tmp;
    mpz_init(tmp);
    FILE* counts = NULL;
    int numTerms = 0;
    int blockNumber = 0;
    while(1){
        char* nextFile = getAbsoluteFilePath(numTerms, blockNumber, COUNT_FILE);
        if (counts != NULL){
            fclose(counts);
        }
        counts = fopen(nextFile, "r");
        free(nextFile);
        if (counts == NULL){
            if (blockNumber == 0){ //maximum number of terms found
                break;
            } else { //go to next number of terms
                numTerms++;
                blockNumber = 0;
            }
        } else {
            int q = 1;
            while(q) {
                for (int i = 0; i <= MAXIMUM; i++) {
                    int read = gmp_fscanf(counts, " %Zd ", tmp);
                    if (read <= 0){
                        q = 0;
                        break;
                    }
                    mpz_add(totals[i], totals[i], tmp);
                }
            }
            printf("Added %d, block%d\n", numTerms, blockNumber);
            blockNumber++;
        }
    }
    char* finalLocation = getAbsoluteFilePath(0,0,FINAL_FILE);
    FILE* outFile = fopen(finalLocation, "w");
    for (int i = 0; i <= MAXIMUM; i++){
        gmp_printf("%d: %Zd\n", i, totals[i]);
        gmp_fprintf(outFile, "%d: %Zd\n", i, totals[i]);
    }
    fclose(outFile);
}

char* getAbsoluteFilePath(int numTerms, unsigned int block, enum fileType ft){
    char* path = (char*) malloc(sizeof(BASEFILEPATH)*2);
    char* fileName;
    switch(ft){
        case INDEX_FILE:
            fileName = "indexes.data";
            break;
        case DATA_FILE:
            fileName = "block.data";
            break;
        case LOG_FILE:
            fileName = "log.txt";
            break;
        case STAT_FILE:
            fileName = "stats.txt";
            break;
        case COUNT_FILE:
            fileName = "counts.txt";
            break;
        case LENGTH_FILE:
            fileName = "length.txt";
        case FINAL_FILE:
            fileName = "finalCounts.txt";
    }
    sprintf(path, BASEFILEPATH, MAXIMUM, CUTOFF, numTerms, block, fileName);
    return path;
}
