#include <stdio.h>
#include <math.h>
#include <stdlib.h>

void computeWilf(int, int, int, int, int);

typedef unsigned long** bpa;

int* multiplicities;
int n;
unsigned long** msumMultiplicities;
int baseNotAllowed;
int maxNumTerms;
bpa** bpaResidues;
unsigned long**** valueMsumBsumNumterms;

int getFirstValueWithNTerms(int n){
    return (n * (n + 1) * (n + 2)) / 6;
}

void addNewTerm(bpa b, int numTerms, int msum, unsigned long numTimes){
    if (msum + numTerms <= n){
        (b)[numTerms][msum + numTerms] += numTimes;
    }
}

void addMeMsum(bpa b, unsigned long* addTo){
    for (int numTerms = maxNumTerms; numTerms >= 1; numTerms--){
            for (int msum = n; msum >= 1; msum--){
                addTo[msum] += (b)[numTerms][msum];
                if (msum + numTerms <= n) {
                    b[numTerms][msum + numTerms] = b[numTerms][msum];
                }
                b[numTerms][msum] = 0;
            }
        
    }
}

int main(int argc, char * argv[]){
  if (argc < 1){
    printf("Number required\n");
    return 1;
  }
  n = strtol(argv[1], NULL, 0);
  
  maxNumTerms = 1;
  while (getFirstValueWithNTerms(maxNumTerms) <= n){
      maxNumTerms++;
  }
  maxNumTerms--;
  
  multiplicities = (int*) malloc(sizeof(int) * (n + 1));
  msumMultiplicities = (unsigned long**) malloc(sizeof(unsigned long*) * (n + 1));
  unsigned long** residues = (unsigned long**) malloc(sizeof(unsigned long*) * (n+1));
  valueMsumBsumNumterms = (unsigned long****) malloc(sizeof(unsigned long***) * (n + 1));
  bpaResidues = (bpa**) malloc(sizeof(bpa*) * (n + 1));
  
  for (int i = 0; i <= n; i++){
     *(valueMsumBsumNumterms + i) = (unsigned long***) malloc(sizeof(unsigned long**) * (n + 1));
     *(bpaResidues + i) = (bpa*) malloc(sizeof(bpa) * (n + 1));
     for (int j = 0; j<=n; j++){
         bpaResidues[i][j] = (unsigned long **) malloc(sizeof(unsigned long*) * (maxNumTerms + 1));
        for (int k = 0; k <= maxNumTerms; k++){
            *(bpaResidues[i][j] + k) = (unsigned long *) malloc(sizeof(unsigned long) * (n + 1));
            for (int l = 0; l <= n; l++){
                bpaResidues[i][j][k][l] = 0;
            }
        }
        valueMsumBsumNumterms[i][j] = (unsigned long**) malloc(sizeof(unsigned long*) * (n + 1));
        for (int k = 0; k <= n; k++){
            valueMsumBsumNumterms[i][j][k] = (unsigned long *) malloc(sizeof(unsigned long) * (maxNumTerms + 1));
            for (int l = 0; l <= maxNumTerms; l++){
                valueMsumBsumNumterms[i][j][k][l] = 0;
            }
        }
     }
  }
  
  for (int i = 0; i <= n; i++){
     *(msumMultiplicities + i) = (unsigned long*) malloc(sizeof(unsigned long) * (n + 1));
     *(residues + i) = (unsigned long*) malloc(sizeof(unsigned long) * (i + 1));
     for (int j = 0; j <= i; j++){
        residues[i][j] = 0;
     }
     for (int j = 0; j <= n; j++){
        msumMultiplicities[i][j] = 0;
     }
     multiplicities[i] = 1;
  }
  
  
     baseNotAllowed = 1;
     computeWilf(1, 1, 1, 1, 1);
     valueMsumBsumNumterms[1][1][1][1]++;

     for (int i = 2; i <= n; i++){
        baseNotAllowed = i;
        for (int j = 2; j<= n; j++){  //first term is (i, 1); second term is (1, j)
            if (i + j <= n) {
                valueMsumBsumNumterms[i + j][j + 1][i + 1][2]++;
                multiplicities[j] = 0;
                computeWilf(i + j, 1, j + 1, i + 1, 2);
                multiplicities[j] = 1;
            } else {
                break;
            }
        }
     }
     
     printf("Base partitions made\n");
     
     for (int value = 1; value <= n; value++){
            for (int baseSum = 1; baseSum <= value; baseSum++) {
                addMeMsum(bpaResidues[baseSum][value % baseSum], msumMultiplicities[value]);
            }
            for (int baseSum = 1; baseSum <= value; baseSum++){
                for (int msum = 1; msum <= value; msum++){
                    for (int numTerms = 1; numTerms <= maxNumTerms; numTerms++){
                        if (valueMsumBsumNumterms[value][msum][baseSum][numTerms] > 0) {
                            addNewTerm(bpaResidues[baseSum][value % baseSum], numTerms, msum, valueMsumBsumNumterms[value][msum][baseSum][numTerms]);
                            msumMultiplicities[value][msum] += valueMsumBsumNumterms[value][msum][baseSum][numTerms];
                        }
                    }
                }
            }
        }
     
   for (int num = 1; num <= n; num++){
        long myValue = 0;
        for (int k = 1; k <= num; k++){
            myValue += msumMultiplicities[num][k];  //get the total number of my paritions with 1 as a base by summing those stored at my value for each possible msum
        }
        for (int r = 1; r <= num; r++){
            myValue += residues[r][num % r];        //add to myvalue the remainder module msum of num modulo msum (the total of all previous partitions of the same remainder with respect to msum)
        }
        printf("%d: %lu\n", num, myValue);
        for (int u = 1; u <= num; u++){
             residues[u][num % u] += msumMultiplicities[num][u];   //update the residue table with my unique residues by adding to each modulo msum the remainder for my value the number of my partitions with that msum
        }
   }
}

void computeWilf(int sumSoFar, int maxBase, int mSumSoFar, int bSumSoFar, int numTerms){
        for (int base = maxBase + 1; base <= n; base++){
            if (base == baseNotAllowed){
                continue;
            }
            if (sumSoFar + base * 2 > n){
                break; //no more bases will work so don't even check the second loop
            }
            for (int m = 2; m <= n; m++){
                if (multiplicities[m] == 0){
                    continue;
                }
                int currentSum = sumSoFar + base * m;
                if (currentSum > n){
                    break;
                }
                int currentMsum = mSumSoFar + m;
                int currentBsum = bSumSoFar + base;
                int currentNumTerms = numTerms + 1;
                valueMsumBsumNumterms[currentSum][currentMsum][currentBsum][currentNumTerms]++; //swapping msum and bsum changes nothing (symmetry) -> should be exploitable
                multiplicities[m] = 0;
                computeWilf(currentSum, base, currentMsum, currentBsum,currentNumTerms); //currentSum instead of currentBsum also works??
                multiplicities[m] = 1;
            }
        }
    
}