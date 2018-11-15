public class Wilf {

    static int[] multiplicities;
    static int n;
    static long[][][] valuemSumbSumMultiplicities;
    static int baseNotAllowed;

    public static void main(String[] args){
        n = 10;
        multiplicities = new int[n + 1];
        valuemSumbSumMultiplicities = new long[n + 1][n + 1][n + 1];
        long[][] residuesMsum = new long[n + 1][n + 1];  //msum remainder
        long[][] residuesBsum = new long[n + 1][n + 1];  //bsum remainder
        //this table contains both of the above tables somehow;
        //long[][][][] residuesBsumMsum = new long[n + 1][n + 1][n + 1][n + 1];  //b, m, b remainder, m remainder
        for (int i = 0; i <= n; i++){
            multiplicities[i] = 1;
        }
        /*
        int maxNumTerms = 1;
        while (getFirstValueWithNTerms(maxNumTerms) <= n){
            maxNumTerms++;
        }
        maxNumTerms--;
        //numTermsBsumMsumValue = new int[maxNumTerms][n + 1][n + 1][n + 1];
*/
        //first term is (1,1)
        multiplicities[1] = 0;
        baseNotAllowed = 1;
        computeWilf(1, 1, 1, 1);
        valuemSumbSumMultiplicities[1][1][1]++;

        for (int i = 2; i <= n; i++){
            baseNotAllowed = i;
            for (int j = 2; j<= n; j++){  //first term is (i, 1); second term is (1, j)
                if (i + j <= n) {
                    valuemSumbSumMultiplicities[i + j][j + 1][i + 1]++;
                    multiplicities[j] = 0;
                    computeWilf(i + j, 1, j + 1, i + 1);
                    multiplicities[j] = 1;
                } else {
                    break;
                }
            }
        }

        /*
        for (int num = 1; num <= n; num++){
            long myValue = 0;
            for (int k = 1; k <= num; k++){
                myValue += mSumMultiplicities[k][num];  //get the total number of my paritions with 1 as a base by summing those stored at my value for each possible msum
            }
            for (int r = 1; r <= num; r++){
                myValue += residues[r][num % r];        //add to myvalue the remainder module msum of num modulo msum (the total of all previous partitions of the same remainder with respect to msum)
            }
            System.out.printf("%d: %d\n", num, myValue);
            for (int u = 1; u <= num; u++){
                residues[u][num % u] += mSumMultiplicities[u][num];   //update the residue table with my unique residues by adding to each modulo msum the remainder for my value the number of my partitions with that msum
            }
        }
        */
        System.out.println("Possibilties made\n");
        /*
        for (int num = 1; num <= n; num++){
            System.out.printf("~~Begin %d ~~\n", num);
            long myValue = 0;
            for (int m = 1; m <= num; m++){
                for (int b = 1; b <= num; b++){  //get the total number of my partitions with 1 as a base and multiplicity by sum over all my unique partitions with a one as a base and a one as a multiplicity
                    myValue += valuemSumbSumMultiplicities[num][b][m];
                    long read = valuemSumbSumMultiplicities[num][b][m];
                    if (read > 0){
                        System.out.printf("Loop 1:\t\t\tRead %d from unique partitions value %d bsum %d msum %d\n", read, num ,b ,m);
                    }
                }
            }
            for (int m = 1; m <= num; m++){ //add those from the residue table with the same modulo msum
                myValue += residuesMsum[m][num % m];
                long read = residuesMsum[m][num % m];
                if (read > 0){
                    System.out.printf("Loop 2:\t\t\tRead %d from msum residue %d with remainder %d\n", read,m, num % m);
                }
                for (int b = 1; b <= num; b++) {
                    residuesMsum[m][num % m] += valuemSumbSumMultiplicities[num][b][m]; //update the values of the msum residues
                    read = valuemSumbSumMultiplicities[num][b][m];
                    if (read > 0){
                        System.out.printf("Loop 2 (inner):\tUpdated msum residues modulo %d with remainder %d by %d to %d \n", m, num % m, read,residuesMsum[m][num % m]);
                    }
                }
            }
            for (int b = 1; b <= num; b++){ //add those from the residue table with the same modulo bsum
                myValue += residuesBsum[b][num % b];
                long read = residuesMsum[b][num % b];
                if (read > 0){
                    System.out.printf("Loop 3:\t\t\tRead %d from bsum residue %d with remainder %d\n", read,b, num % b);
                }
                for (int m = 1; m <= num; m++){
                    residuesBsum[b][num % b] += valuemSumbSumMultiplicities[num][b][m];  //update the values of the bsum residues
                    read = valuemSumbSumMultiplicities[num][b][m];
                    if (read > 0){
                        System.out.printf("Loop 3 (inner):\tUpdated bsum residues modulo %d with remainder %d by %d to %d \n", b, num % b, read,residuesMsum[b][num % b]);
                    }
                }
            }
            for (int m = 2; m <= num; m++){
                for (int b = 2; b <= num; b++){  //subtract those from the residue table for each modulo msum modulo bsum
                    myValue -= residuesBsumMsum[b][m][num % b][num % m];
                    long read = residuesBsumMsum[b][m][num % b][num % m];
                    if (read > 0){
                        System.out.printf("Loop 4 pt 1:\tDecremented value by %d from bsum %d with remainder %d and msum %d with remainder %d\n", read, b, num % b, m, num % m);
                    }
                    residuesBsumMsum[b][m][num % b][num % m] += valuemSumbSumMultiplicities[num][b][m];
                    read = valuemSumbSumMultiplicities[num][b][m];
                    if (read > 0){
                        System.out.printf("Loop 4 pt 2:\tUpdated residue totals for bsum %d with remainder %d and msum %d with remainder %d by %d\n", b, num % b, m, num % m, read);
                    }

                }
            }
            System.out.printf("%d: %d\n", num, myValue);
        }
        */
    }

    public static int getFirstValueWithNTerms(int n){
        return (int) ((((long) n) * (n + 1) * (n + 2)) / 6);
    }

    public static void computeWilf(int sumSoFar, int maxBase, int mSumSoFar, int bSumSoFar){
        for (int base = maxBase + 1; base < baseNotAllowed; base ++){
            if (sumSoFar + base * 2 > n){
                return; //no more bases will work so don't even check the second loop
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
                valuemSumbSumMultiplicities[currentSum][currentBsum][currentMsum]++;
                multiplicities[m] = 0;
                computeWilf(currentSum, base, currentMsum, currentSum);
                multiplicities[m] = 1;
            }
        }
        for (int base = baseNotAllowed + 1; base <= n; base++){
            if (sumSoFar + base * 2 > n){
                break; //(could also be return)
            }
            for (int m = 2; m <= n; m++){
                int currentSum = sumSoFar + base * m;
                if (currentSum > n){
                    break;
                }
                int currentMsum = mSumSoFar + m;
                int currentBsum = bSumSoFar + base;
                valuemSumbSumMultiplicities[currentSum][currentBsum][currentMsum]++;
                multiplicities[m] = 0;
                computeWilf(currentSum, base, currentMsum, currentSum);
                multiplicities[m] = 1;
            }
        }
    }
/*
    public static void computeWilf(int sumSoFar, int maxBase, int mSumSoFar, int bSumSoFar, int index, boolean oneHasBeenIncluded){
        for (int base = maxBase + 1; base <= n; base++){
            for (int m = 2; m <= n; m++){
                if (index == locationOfOne) {
                    if (oneHasBeenIncluded) {
                        break;
                    } else {
                        m = 1;
                        oneHasBeenIncluded = true;
                    }
                } else if (multiplicities[m] == 0){
                    continue;
                }
                int currentSum = sumSoFar + base * m;
                if (currentSum > n){
                    break;
                }
                int currentMsum = mSumSoFar + m;
                int currentBsum = bSumSoFar + base;
                valuemSumbSumMultiplicities[currentSum][currentBsum][currentMsum]++;


                if (oneHasBeenIncluded){
                    //do something
                    if (currentSum + base + 1 <= n) { //do simple check
                        multiplicities[m] = 0;
                        computeWilf(currentSum, base, currentMsum, currentBsum, index + 1, oneHasBeenIncluded);
                        multiplicities[m] = 1;
                    }
                } else if ((index <= 3) && (!canIncludeOne(sumSoFar, maxBase, index))){
                    break;
                } else {
                    if (currentSum + base + 1 <= n) { //do simple check
                        multiplicities[m] = 0;
                        computeWilf(currentSum, base, currentMsum, currentBsum, index + 1, oneHasBeenIncluded);
                        multiplicities[m] = 1;
                    }
                }
            }
        }
    }

    public static boolean canIncludeOne(int currentTotal, int minBase, int index){
        resetLast();
        while (index < locationOfOne){
            currentTotal = currentTotal + (minBase++ + 1) * getLowestMultLeft();
            index++;
        }
        currentTotal = currentTotal + getLowestMultLeft();
        return currentTotal <= n;
    }

    static int last;
    public static void resetLast(){
        last = 2;
    }

    public static int getLowestMultLeft(){
        for (int i = last; i <= multiplicities.length; i++){
            if (multiplicities[i] == 1){
                last = i + 1;
                return i;
            }
        }
        return multiplicities.length;
    }
*/
}
