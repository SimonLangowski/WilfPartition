import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Wilf2 {

    static int[] multiplicities;
    static int n;
    static int baseNotAllowed;
    static int max;
    static int maxNumTerms;
    static long[][] mSumMultiplicities;
    static BPA[][] bpaResidues;
    static long[][][][] valuemSumbSumMultiplicities;

    public static void main(String[] args){
        n = Integer.parseInt(args[0]);
        max = n;
        multiplicities = new int[n + 1];
        long[][] residues = new long[n + 1][n + 1];
        for (int i = 0; i <= n; i++){
            multiplicities[i] = 1;
        }
        maxNumTerms = 1;
        while (getFirstValueWithNTerms(maxNumTerms) <= n){
            maxNumTerms++;
        }
        maxNumTerms--;
        valuemSumbSumMultiplicities = new long[n + 1][n + 1][n + 1][maxNumTerms + 1];
        mSumMultiplicities = new long[n + 1][n + 1];
        bpaResidues = new BPA[n + 1][n + 1];
        for (int i = 0; i < bpaResidues.length; i++){
            for (int j = 0; j < bpaResidues[0].length; j++){
                bpaResidues[i][j] = new BPA();
            }
        }
        //numTermsBsumMsumValue = new int[maxNumTerms][n + 1][n + 1][n + 1];

        //first term is (1,1)
        multiplicities[1] = 0;
        baseNotAllowed = 1;
        //System.out.printf("Computing (1,1) possiblities\n");
        long startTime = System.currentTimeMillis();
        computeWilf(1, 1, 1, 1, 1);
        valuemSumbSumMultiplicities[1][1][1][1]++;

        for (int i = 2; i <= n; i++){
            baseNotAllowed = i;
            for (int j = 2; j<= n; j++){  //first term is (i, 1); second term is (1, j)
                if (i + j <= n) {
                    valuemSumbSumMultiplicities[i + j][j + 1][i + 1][2]++;
                    multiplicities[j] = 0;
                    computeWilf(i + j, 1, j + 1, i + 1, 2);
                    multiplicities[j] = 1;
                } else {
                    break;
                }
            }
        }
        //DEbug.printValMsumBsumNumTerms(valuemSumbSumMultiplicities);
        long elapsedTime = System.currentTimeMillis() - startTime;
        System.out.printf("Possibilties made: %d ms\n", elapsedTime);
        startTime = System.currentTimeMillis();
        makeSumMultiplicities();
        elapsedTime = System.currentTimeMillis() - startTime;
        System.out.printf("Iteration complete: %d ms\n", elapsedTime);
        //DEbug.printBPA(bpaResidues);
        //DEbug.printValueMsun(mSumMultiplicities);
        for (int num = 1; num <= n; num++){
            long myValue = 0;
            for (int k = 1; k <= num; k++){
                myValue += mSumMultiplicities[num][k];  //get the total number of my paritions with 1 as a base by summing those stored at my value for each possible msum
            }
            for (int r = 1; r <= num; r++){
                myValue += residues[r][num % r];        //add to myvalue the remainder module msum of num modulo msum (the total of all previous partitions of the same remainder with respect to msum)
            }
            System.out.printf("%d: %d\n", num, myValue);
            for (int u = 1; u <= num; u++){
                residues[u][num % u] += mSumMultiplicities[num][u];   //update the residue table with my unique residues by adding to each modulo msum the remainder for my value the number of my partitions with that msum
            }
        }
        /*DEbug debug = new DEbug(n);
        debug.printValueMsun(mSumMultiplicities);
        debug.printBPA(bpaResidues);
        debug.printValMsumBsumNumTerms(valuemSumbSumMultiplicities);
        */try {
            PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter("Genfunc1.txt")));
            for (int j = 1; j < mSumMultiplicities.length; j++){
                printWriter.printf("(1 / (1- z^%d)*[", j, j);
                for (int i = 1; i < mSumMultiplicities.length; i++){
                    printWriter.printf("%dz^%d", mSumMultiplicities[i][j], i);
                    if (i != mSumMultiplicities.length - 1){
                        printWriter.printf(" + ");
                    }
                }
                printWriter.printf("] + \n");
            }
            printWriter.flush();
        } catch (IOException e){
            e.printStackTrace();
        }
        try {
            int lastPrintedMsum = 1;
            int lastPrintedBsum = 1;
            int lastPrintedNumTerms = 1;
            boolean printed = false;
            PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter("Genfunc2.txt")));
            printWriter.printf("(1 / (1 - z)) * [(1 / (1-z)) * [z *[ ");
            for (int msum = 1; msum < mSumMultiplicities.length; msum++){
                if (msum != lastPrintedMsum){
                    printWriter.printf("(1 / (1 - z^%d)*[", msum);
                }
                for (int bSum = 1; bSum < mSumMultiplicities.length; bSum++) {
                    if (bSum != lastPrintedBsum) {
                        if (lastPrintedBsum != 1){
                            printWriter.printf("] + ");
                        }
                        printWriter.printf("(1 / (1 - z^%d)*[", bSum);
                    }
                    for (int numTerms = 1; numTerms <= Wilf2.maxNumTerms; numTerms++) {
                        if (lastPrintedNumTerms != numTerms) {
                            if (lastPrintedNumTerms != 1){
                                printWriter.printf("] + ");
                            }
                            printWriter.printf("z^%d * [", numTerms);
                        }
                        for (int value = 1; value < Wilf2.max; value++) {
                            if (valuemSumbSumMultiplicities[value][msum][bSum][numTerms] != 0) {
                                printWriter.printf("%dz^%d + ", valuemSumbSumMultiplicities[value][msum][bSum][numTerms], value);
                                printed = true;
                            } else {
                                printed = false;
                            }
                        }
                        if (printed){
                            printWriter.printf("]");
                            lastPrintedNumTerms = numTerms;
                        }
                    }
                    if (printed){
                        printWriter.printf("]");
                        lastPrintedBsum = bSum;
                    }
                }
                if (printed){
                    printWriter.printf("] + \n");
                    lastPrintedMsum = msum;
                }
            }
            printWriter.flush();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void makeSumMultiplicities(){
        for (int value = 1; value <= max; value++){
            for (int baseSum = 1; baseSum <= value; baseSum++) {
                bpaResidues[baseSum][value % baseSum].addMeMsum(mSumMultiplicities[value]); //shifts based on num terms
            }
            for (int baseSum = 1; baseSum <= value; baseSum++){
                for (int msum = 1; msum <= value; msum++){
                    for (int numTerms = 1; numTerms <= maxNumTerms; numTerms++){
                        if (valuemSumbSumMultiplicities[value][msum][baseSum][numTerms] > 0) { //adds 1,1, base partitions
                            bpaResidues[baseSum][value % baseSum].addNewTerm(numTerms, msum, valuemSumbSumMultiplicities[value][msum][baseSum][numTerms]);
                            mSumMultiplicities[value][msum] += valuemSumbSumMultiplicities[value][msum][baseSum][numTerms];
                        }
                    }
                }
            }
        }
    }
/*
    public static void makeSumMultiplicities(){
        for (int valToUpdate = 1; valToUpdate <= max; valToUpdate++) {
            for (int msum = 1; msum <= valToUpdate; msum++) {
                long myValue = 0;
                for (int r = 1; r <= valToUpdate; r++) { //get my current msum totals from each residue
                    myValue += bpaResidues[r][valToUpdate % r].getMsum(msum);
                }
                mSumMultiplicities[valToUpdate][msum] += myValue;
            }
            for (int bsum = 1; bsum <= valToUpdate; bsum++) {
                bpaResidues[bsum][valToUpdate % bsum].increment(valToUpdate);
            }
            for (int msum = 1; msum <= valToUpdate; msum++) {
                for (int bsum = 1; bsum <= valToUpdate; bsum++) { //update any residues that have a base base pair at my value
                    for (int n = 1; n <= maxNumTerms; n++) {
                        if (valuemSumbSumMultiplicities[valToUpdate][msum][bsum][n] > 0) {
                            bpaResidues[bsum][valToUpdate % bsum].addNewTerm(n, msum);
                            mSumMultiplicities[valToUpdate][msum] += valuemSumbSumMultiplicities[valToUpdate][msum][bsum][n];
                        }
                    }
                }
            }
        }
    }
    */


    public static int getFirstValueWithNTerms(int n){
        return (int) ((((long) n) * (n + 1) * (n + 2)) / 6);
    }

    public static void computeWilf(int sumSoFar, int maxBase, int mSumSoFar, int bSumSoFar, int numTerms){
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
                valuemSumbSumMultiplicities[currentSum][currentMsum][currentBsum][currentNumTerms]++; //swapping msum and bsum changes nothing (symmetry) -> should be exploitable
                multiplicities[m] = 0;
                computeWilf(currentSum, base, currentMsum, currentBsum,currentNumTerms);
                multiplicities[m] = 1;
            }
        }
    }

}
