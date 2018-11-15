//
// Created by Slang on 10/5/2018.
//
#include "main.h"
#include "precompute.h"

unsigned int pascalTable[LCUTOFF + 1][LCUTOFF + 1];
polyRecipe* recipes[LCUTOFF+1];
unsigned int cycleLayerShifts[LCUTOFF + 1];
unsigned short polyMsums[POWEROFTWO];
unsigned int* precomputeDataIndexes;
signed char* precomputeDataData;

void makePascalTable(){
    pascalTable[0][0] = 1;
    for (int i = 1; i <= LCUTOFF; i++){
        pascalTable[i][0] = 1;
        pascalTable[i][i] = 1;
        for (int j = 1; j <= i - 1; j++){
            pascalTable[i][j] = pascalTable[i - 1][j] + pascalTable[i - 1][j - 1];
        }
    }
    cycleLayerShifts[0] = 0;
    for (int i = 1; i <= LCUTOFF; i++) {
        cycleLayerShifts[i] = cycleLayerShifts[i - 1] + pascalTable[LCUTOFF][i - 1];
    }

}

//generate "recipes"
void generateRecipes(){
    makePascalTable();
    for (int numT = 0; numT <= LCUTOFF; numT++){
        //recipe is numterms indexes with the numterms offsets
        recipes[numT] = (polyRecipe*) malloc(sizeof(polyRecipe) * numT * pascalTable[LCUTOFF][numT]);
    }
    unsigned int bases[LCUTOFF];
    int c = 0;
    for (int numT = 0; numT <= LCUTOFF; numT++){
        for (unsigned int i = 0; i < numT; i++){
            bases[i] = i + 1;
        }
        int counter = 0;
        while(1){
            unsigned short msum = 0;
            for (int i = 0; i < numT; i++){
                unsigned int index = getDeletedPrecomputeIndex(bases, i, numT);
                unsigned int correspondingMult = bases[i];
                msum += bases[i];
                recipes[numT][counter].index = index;
                recipes[numT][counter].shift = correspondingMult;
                counter++;
            }
            polyMsums[c++] = msum;
            int done = incrementBasesPrecompute(bases, numT);
            if (!done){
                //printf("Made %d recipes at %d terms\n", counter, numT);
                break;
            }
        }
    }
}


void readPrecompute(){
    char* outputDataName = getAbsoluteFilePath(0, 1, DATA_FILE);
    int outputDataFile = open(outputDataName, O_RDONLY);
    free(outputDataName);
    char* outputIndexName = getAbsoluteFilePath(0, 1, INDEX_FILE);
    int outputIndexFile = open(outputIndexName, O_RDONLY);
    free(outputIndexName);
    precomputeDataData = (signed char*) mmap(NULL, PRECOMPUTEFILELENGTH, PROT_READ, MAP_SHARED | MAP_POPULATE | MAP_FILE, outputDataFile,0);
    precomputeDataIndexes = (unsigned int *) mmap(NULL, PRECOMPUTEINDEXLENGTH, PROT_READ, MAP_SHARED | MAP_POPULATE | MAP_FILE, outputIndexFile,0);
    generateRecipes();
}


//this should only be run once, on a machine with lots of ram, and the files transfer
fmpzArray* precomputeDataInternal[LCUTOFF + 1];
fmpzArray* precomputeData[LCUTOFF + 1];
unsigned long buf2[2*MAXIMUM + 1];

void printFMPZarray(fmpzArray* a){
    printf("[");
    for (int i = 0; i < MAXIMUM; i++){
        fmpz_print(&a->a[i]);
        printf(", ");
    }
    fmpz_print(&a->a[MAXIMUM]);
    printf("]\n");
}

void precompute(){
    generateRecipes();
    for (int i = 0; i <= LCUTOFF; i++){
        precomputeDataInternal[i] = (fmpzArray*) malloc(sizeof(fmpzArray) * pascalTable[LCUTOFF][i]);
        memset(precomputeDataInternal[i], 0, sizeof(fmpzArray) * pascalTable[LCUTOFF][i]);
        precomputeData[i] = (fmpzArray*) malloc(sizeof(fmpzArray) * pascalTable[LCUTOFF][i]);
        memset(precomputeData[i], 0, sizeof(fmpzArray) * pascalTable[LCUTOFF][i]);
    }
    precomputeDataInternal[0][0].a[0] = 1;
    for (int i = 1; i <= LCUTOFF; i++){
        for (int j = 0; j < pascalTable[LCUTOFF][i]; j++){
            precomputeFollowRecipe(&recipes[i][j*i], precomputeDataInternal[i][j].a, i, polyMsums[cycleLayerShifts[i]+j]);
            //printf("Internal:%d-%d", i, j);
            //printFMPZarray(&precomputeDataInternal[i][j]);
        }
    }
    //this can be completely parallelized since I'm reacding and writing to different arrays
    for (int i = LCUTOFF; i >= 1; i--){
        for (int j = 0; j < pascalTable[LCUTOFF][i]; j++){
            //sum children into each one (Exactly once per child)
            //printf("Construction %d at %d terms: ", j, i);
            precomputeAddChildren(&recipes[i][j*i], precomputeData[i][j].a, i);
            //printf("\n");
            //printf("Total:%d-%d",i,j);
            //printFMPZarray(&precomputeData[i][j]);
        }
    }
    precomputeData[0][0].a[0] = 1;
    //write out compressed data
    cmp c;
    c.beginning.ulongp = buf2;
    fmpz t;
    fmpz_init(&t);
    unsigned int currentDataOffset = 0;
    char* outputDataName = getAbsoluteFilePath(0, 1, DATA_FILE);
    FILE* outputDataFile = fopen(outputDataName, "a");
    free(outputDataName);
    char* outputIndexName = getAbsoluteFilePath(0, 1, INDEX_FILE);
    FILE* outputIndexFile = fopen(outputIndexName, "a");
    free(outputIndexName);
    for (int i = 0; i <= LCUTOFF; i++){
        for (int j = 0; j < pascalTable[LCUTOFF][i]; j++){
            compress2(&c, precomputeData[i][j].a, t, 0, MAXIMUM);
            //ensure word alignment
            if (c.size % 8 != 0){
                c.size = c.size - (c.size % 8) + 8;
            }
            fwrite(c.beginning.ulongp, c.size, 1, outputDataFile);
            fwrite(&currentDataOffset, sizeof(unsigned int), 1, outputIndexFile);
            //update location
            currentDataOffset += c.size;
        }
    }
    fclose(outputDataFile);
    fclose(outputIndexFile);
}



void precomputeFollowRecipe(polyRecipe* recipe, fmpz* dest, int numTerms, int bsum){
    for(int i = 0; i < numTerms; i++){
        addShifted(dest, precomputeDataInternal[numTerms - 1][(recipe+i)->index].a, bsum, MAXIMUM + 1);
    }
    for (int i = bsum; i <= MAXIMUM; i++){
        fmpz_add(dest + i, dest + i, dest + i - bsum);
    }
}

void addShifted(fmpz* dest, fmpz* source, int shift, int length){
    for (int i = shift; i < length; i++){
        fmpz_add(dest + i, dest + i, source + i - shift);
    }
}

void precomputeAddChildren(polyRecipe* recipe, fmpz* dest, int numTerms){
    unsigned short b[numTerms];
    unsigned short c[numTerms];
    for (int i = 0; i < numTerms; i++){
        b[i] = (recipe+i)->shift;
    }
    recursiveAdd(dest, 0, b, c, 0, numTerms);
}

void recursiveAdd(fmpz* dest, int myIndex, unsigned short* bases, unsigned short* current, int len, int maxlen){
    if (myIndex == maxlen){
        int index = getPrecomputeIndex(current, len);
        //printf("%d ", index);
        _fmpz_vec_add(dest, dest, precomputeDataInternal[len][index].a, MAXIMUM + 1);
        return;
    }
    recursiveAdd(dest, myIndex + 1, bases, current, len, maxlen); //continue without this index
    current[len] = bases[myIndex];
    recursiveAdd(dest, myIndex + 1, bases, current, len + 1, maxlen); //add with this index
}


//read from disk
//min degree is always zero, maxdegree is request by program and precompute data goes through max
void getPrecomputedData(fmpz* dest, int numTerms, unsigned int index, int maxDistanceLeft){
    index += cycleLayerShifts[numTerms];
    pointer p;
    p.schar = precomputeDataData + precomputeDataIndexes[index];
    decompress(dest, p, maxDistanceLeft);
}

int incrementBasesPrecompute(unsigned int* bases, int length){
    int incrementPosition = length - 1;
    while(incrementPosition >= 0){
        bases[incrementPosition]++;
        for (int i = incrementPosition + 1; i < length; i++){
            bases[i] = bases[incrementPosition] + (i - incrementPosition);
        }
        if (bases[incrementPosition] > LCUTOFF - (length - 1 - incrementPosition)){
            incrementPosition--;
        } else {
            return 1;
        }
    }
    return 0;
}

unsigned int getPrecomputeIndex(const unsigned short* bases, int length){
    unsigned int index = 0;
    for (int i = 0; i < length; i++){
        if (i > 0){
            for (int j = bases[i - 1] + 1; j < bases[i]; j++){
                index += pascalTable[LCUTOFF - j][length - i - 1];
            }
        } else {
            for (int j = 1; j < bases[i]; j++){
                index += pascalTable[LCUTOFF - j][length - i - 1];
            }
        }
    }
    return index;
}

unsigned int getDeletedPrecomputeIndex(const unsigned int* bases, int toSkip, int length){
    unsigned int index = 0;
    for (int i = 0; i < toSkip; i++){
        if (i > 0){
            for (int j = bases[i - 1] + 1; j < bases[i]; j++){
                index += pascalTable[LCUTOFF - j][length - i - 2]; //-2 because this length is one more than the length of the data to be used
            }
        } else {
            for (int j = 1; j < bases[i]; j++){
                index += pascalTable[LCUTOFF - j][length - i - 2];
            }
        }
    }
    for (int i = toSkip + 1; i < length; i++){
        if (i == toSkip + 1){
            if (i >= 2){
                for (int j = bases[i - 2] + 1; j < bases[i]; j++){
                    index += pascalTable[LCUTOFF - j][length - i - 1];
                }
            } else {
                for (int j = 1; j < bases[i]; j++){
                    index += pascalTable[LCUTOFF - j][length - i - 1];
                }
            }
        } else {
            for (int j = bases[i - 1] + 1; j < bases[i]; j++) {
                index += pascalTable[LCUTOFF - j][length - i - 1]; //offset by one because of skipped base so i->i-1
            }
        }
    }
    return index;
}

void decompress(fmpz* dest, pointer compressed, int end){
    fmpz summand;
    fmpz_init(&summand);
    unsigned int currentSizeType = 1;
    unsigned int lastLocation = 0;
    for (int j = 0; j <= end; j++){
        switch(currentSizeType){
            case 1: //signed char
                if (compressed.schar[lastLocation] == SCHAR_MIN){
                    currentSizeType = 2;
                    lastLocation = (lastLocation + 1 + 1) / 2;
                } else {
                    fmpz_add_si(&summand, &summand, (long)(compressed.schar[lastLocation]));
                    fmpz_set(dest + j, &summand);
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
                    fmpz_set(dest + j, &summand);
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
                    fmpz_set(dest + j, &summand);
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
                    fmpz_set(dest + j, &summand);
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
                    fmpz_set(dest + j, &summand);
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
