import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;

public class Testing {

    static int MAX = 300;
    static int cutoff = 4;
    static long[][][] tree;
    static BigInteger[][][] table = Calculate2.table;
    static int maxNumTerms;
    static long[] finalCounts = new long[MAX + 1];
    static long[][] storedValues;
    static byte[] termsRemaining;
    static ArrayList<ArrayDeque<JobObject>> jobs;

    public static void main(String[] args){
        String[] passArgs = new String[] {Integer.toString(MAX), Integer.toString(cutoff)};
        Calculate2.main(passArgs);
        table = Calculate2.table;
        maxNumTerms = Calculate2.computeInverse(MAX, cutoff);
        tree = new long[maxNumTerms + 1][][];
        for (int i = 1; i <= maxNumTerms; i++){
            makeTreeFromTable(i);
        }
        //table = null;
        long startTime = System.nanoTime();
        calculatePartitions();
        long endTime = System.nanoTime();
        System.out.println("Dist to time: " + (endTime - startTime) + "ns, " + ((endTime - startTime)/1000000000) + "s");
    }

    static void calculatePartitions() {
        jobs = new ArrayList<>(maxNumTerms + 1);
        for (int i = 0; i <= maxNumTerms; i++){
            jobs.add(new ArrayDeque<JobObject>());
        }
        JobObject zero;
        ThreadVariables empty = new ThreadVariables(MAX + 1);
        long[] zeroValues = new long[MAX + 1];
        zeroValues[0] = 1;
        int zeroMinSum = 0;
        zero = new JobObject(empty, zeroValues, zeroMinSum);
        computeCounts(zeroMinSum, zeroValues, empty);
        jobs.get(0).add(zero);
        for (int i = 1; i <= maxNumTerms; i++) {
            int numJobs = Calculate2.computeValue(1, MAX, i).intValue();
            System.out.printf("Computing %d numbers for %d terms from %d jobs\n", numJobs, i, jobs.get(i-1).size());
            storedValues = new long[numJobs][];
            termsRemaining = new byte[numJobs];
            while (!jobs.get(i - 1).isEmpty()) {
                JobObject j = jobs.get(i - 1).removeFirst();
                computeJob(j, i);
            }
            for (int j = 0; j < termsRemaining.length; j++){
                if (termsRemaining[j] != 0){
                    System.out.printf("Index %d has %d/%d terms left\n", j, termsRemaining[j], i);
                    int[] bases = Calculate2.findIndex(new BigInteger(Integer.toString(j)), i);
                    Calculate2.printArray(bases);

                }
            }
        }
        for (int i = 0; i < finalCounts.length; i++){
            System.out.printf("%d: %d\n", i, finalCounts[i]);
        }
    }

    static void computeJob(JobObject r, int numTerms){
        int bsum = r.bases.computeBsum();
        int minBase = r.bases.decreasing[r.bases.size];
        int lastSeen = 0;
        int total = r.minSum + bsum;
        for (int b = 1; b <= MAX; b++) {
            total += cutoff;
            //System.out.println(r.bases + " + " + b + " total: " + total);
            if (r.bases.increasing[b] == 0) {
                r.bases.addSorted(b, lastSeen);
                IndexInfo indexInfo1 = getIndex(r.bases, numTerms);
                if (indexInfo1.minSum > MAX){
                    r.bases.deleteAtIndex(b);
                    break;
                } else if (total > MAX){
                    termsRemaining[(int) indexInfo1.index]--;
                    if (termsRemaining[(int) indexInfo1.index] == 0) { //another thread might make 0 before check and two threads would run
                        computeCounts(indexInfo1.minSum, storedValues[(int) indexInfo1.index], r.bases);
                        printHumanReadable(r.bases, storedValues[(int) indexInfo1.index], MAX, indexInfo1.minSum, numTerms); //does this need to be a copy of stored values?
                        storedValues[(int) indexInfo1.index] = null;
                    }
                } else if (indexInfo1.index >= 0) {
                    long[] temp = new long[MAX + 1];
                    if (storedValues[(int) indexInfo1.index] == null) {
                        add(temp, r.values, bsum + b, b, minBase);
                        storedValues[(int) indexInfo1.index] = temp;
                        termsRemaining[(int) indexInfo1.index] = (byte) numTerms;
                        //b is already in r.bases
                        //for each base that cannot be the minimum and still be valid under the MAX, need to subtract one from termsRemaining


                    } else {
                        temp = storedValues[(int) indexInfo1.index];
                        add(temp, r.values, bsum + b, b, minBase);
                        if (termsRemaining[(int) indexInfo1.index] != 1) {
                            storedValues[(int) indexInfo1.index] = temp; //add new compressed data
                        }
                    }
                    termsRemaining[(int) indexInfo1.index]--;
                    if (termsRemaining[(int) indexInfo1.index] == 0) { //another thread might make 0 before check and two threads would run
                        computeCounts(indexInfo1.minSum, temp, r.bases);
                        printHumanReadable(r.bases, temp, MAX, indexInfo1.minSum, numTerms);
                        storedValues[(int) indexInfo1.index] = null;
                    }
                } else {
                    //continue
                }
                r.bases.deleteAtIndex(b);
            } else {
                lastSeen = b;
            }
        }
    }

    static void printHumanReadable(ThreadVariables t, long[] values, int maxSize, int minSize, int numTerms){
        JobObject j = new JobObject(t.copyThreadVariables(), values, minSize);
        jobs.get(numTerms).add(j);
    }

    static void add(long[] destination, long[] input, int bsum, int base, int minBase){
        long[] cummulative = new long[bsum];
        int shift = bsum + (cutoff - 1) * base;
        int initial = (shift + minBase) % bsum;
        for (int i = shift + minBase; i < destination.length; i++){
            cummulative[initial] += input[i - shift];
            destination[i] += cummulative[initial];
            if(++initial == bsum){
                initial = 0;
            }
        }
    }

    static void computeCounts(int minSum, long[] distToCounts, ThreadVariables t){
        long[] distanceFromCounts = lowerRecursionWrapper(MAX - minSum, t);
        for (int i = 0; i <= MAX; i++){
            for (int j = minSum; j <= i; j++){
                finalCounts[i] += distToCounts[j] * distanceFromCounts[i - j];
            }
        }
    }

    static IndexInfo getIndex(ThreadVariables t, int numTerms){
        int startingMultiplicity = numTerms + cutoff - 1;
        int currentDistLeft = MAX;
        long spacesSkipped = 0;
        int minBase = 1;
        int currentBase = t.increasing[0];
        while (currentBase < t.size){
            for (int j = minBase; j < currentBase; j++){
                //spacesSkipped += computeValue(j + 1, currentDistLeft - startingMultiplicity * j, numTerms - 1);
                spacesSkipped += getValueFromTree(numTerms - 1, j + 1, currentDistLeft - startingMultiplicity * j);
            }
            currentDistLeft -= currentBase * startingMultiplicity;
            startingMultiplicity--;
            numTerms--;
            minBase = currentBase + 1;
            currentBase = t.increasing[currentBase];
        }
        return new IndexInfo(spacesSkipped, MAX - currentDistLeft);
    }

    static long getValueFromTree(int numTerms, int minBase, int maxDistLeft){
        if (numTerms == 0){
            //System.out.printf("0 Terms: Requested nTerms: %d minBase: %d distLeft: %d\n", numTerms, minBase, maxDistLeft);
            return 1;
        }
        long offset = tree[numTerms][minBase - 1][0];
        if (maxDistLeft < offset){
            //System.out.printf("Offset Error: Requested nTerms: %d minBase: %d distLeft: %d (offset %d)\n", numTerms, minBase, maxDistLeft, offset);
            return 0;
        }
        try {
            return tree[numTerms][minBase - 1][(int) (maxDistLeft - offset)];
        } catch (ArrayIndexOutOfBoundsException e){
            if ((minBase - 1) > tree[numTerms].length) {
                System.out.printf("MinBase Error: Requested nTerms: %d minBase: %d distLeft: %d\n", numTerms, minBase, maxDistLeft);
            } else if (maxDistLeft - offset > tree[numTerms][minBase - 1].length){
                System.out.printf("Large max error: Requested nTerms: %d minBase: %d distLeft: %d (offset %d)\n", numTerms, minBase, maxDistLeft, offset);
            } else {
                System.out.printf("Unknown error: Requested nTerms: %d minBase: %d distLeft: %d\n",  numTerms, minBase, maxDistLeft);
            }
            return 0;
        }
    }

    static void makeTreeFromTable(int numTerms){
        long[][] tempMinBaseArray = new long[(MAX / cutoff) + 1][];
        boolean allZeroRows = false;
        for (int j = 1; j <= MAX / cutoff; j++){
            boolean allZeroColumns = true;
            for (int k = 1; k <= MAX; k++){
                if ((allZeroColumns) && (table[numTerms][j][k] != null)){
                    allZeroColumns = false;
                    allZeroRows = true;
                    long offset = k - 1;
                    tempMinBaseArray[j - 1] = new long[(MAX - k + 1 + 1)];
                    tempMinBaseArray[j - 1][0] = offset;
                    while (k <= MAX){
                        if (table[numTerms][j][k] != null) {
                            tempMinBaseArray[j - 1][(int) (k - offset)] = table[numTerms][j][k].longValue();
                        }
                        k++;
                    }
                    break;
                }
            }
            if (allZeroColumns && allZeroRows){
                tree[numTerms] = tempMinBaseArray;
                break;
            }
        }
    }

    static long[] lowerRecursionWrapper(int maxDistanceLeft, ThreadVariables t){
        long[] a = q2All(maxDistanceLeft, t);
        for (int c = 3; c < cutoff; c++){
            long[] b = lowerRecursion(maxDistanceLeft, c, t);
            for (int j = 0; j <= maxDistanceLeft; j++){
                a[j] += b[j];
            }
        }
        return a;
    }

    static long[] lowerRecursion(int maxDistanceLeft, int myMultiplicity, ThreadVariables t) {
        long[] counts = new long[maxDistanceLeft + 1];
        int lastSeen = 0;
        int baseDistanceLeft = maxDistanceLeft - myMultiplicity;
        for (int b = 1; b <= maxDistanceLeft; b++, baseDistanceLeft -= myMultiplicity) {
            if (baseDistanceLeft < 0) {
                break;
            }
            if (t.increasing[b] == 0) {
                t.addSorted(b, lastSeen);
                long[] values = q2All(baseDistanceLeft, t);
                long[] moreValues = null;
                //increment m
                //need to run on all values of m up to cutoff
                if (myMultiplicity + 1 < cutoff) { //check if m + 1 is within my range
                    //recurse for m + 1
                    moreValues = lowerRecursion(baseDistanceLeft, myMultiplicity + 1, t);
                    for (int k = myMultiplicity + 2; k < cutoff; k++) {
                        long[] moreMoreValues = lowerRecursion(baseDistanceLeft, k, t);
                        for (int l = 0; l <= baseDistanceLeft; l++) {
                            moreValues[l] = moreValues[l] + moreMoreValues[l];
                        }
                    }
                }
                combineFunction(counts, values, maxDistanceLeft, baseDistanceLeft);
                if (moreValues != null) {
                    combineFunction(counts, moreValues, maxDistanceLeft, baseDistanceLeft);
                }
                t.deleteAtIndex(b);
            } else {
                lastSeen = b;
            }
        }
        return counts;
    }

    static long[] q2All(int maxDistanceLeft, ThreadVariables t) {
        return Q2AllTest.q2AllNew(maxDistanceLeft, t);
        /*long[] counts = new long[maxDistanceLeft + 1];
        for (int i = 0; i <= maxDistanceLeft; i++) {
            counts[i] = q2(i, t); //this can be done much more efficiently
        }
        return counts;*/
    }

    static int q2(int distanceLeft, ThreadVariables t) {
        //count of numbers of the form (a)(1) + (2)(b) = distanceLeft
        //if distanceLeft is even have form {(0, distanceLeft/2), (2, distanceLeft/2 - 1), ... (distanceLeft, 0)}
        //if distanceLeft is odd have form {(1, floor(distanceLeft/2)), (3, floor(distanceLeft/2) - 1) ... (distanceLeft, 0)}
        //in either case there are floor(distanceLeft/2)) + 1terms
        //subtract those with invalid a,b from previously used bases   (increasing[base] != 0 if the base has been used)
        int count = (distanceLeft / 2) + 1;
        int currentBase = t.increasing[0]; //get first base
        int parity = distanceLeft % 2;
        while (currentBase <= (distanceLeft / 2)) {
            //one (a,b) value pair must be removed for the b value being included
            count--;
            int checkValue = distanceLeft - 2 * currentBase;
            if ((checkValue > 0) && (t.increasing[checkValue] != 0)) { //check if both a and b values
                count++; //don't double count
            }
            //one (a,b) value pair must be removed if there is an a value with the same parity
            if (currentBase % 2 == parity) {
                count--;
            }
            currentBase = t.increasing[currentBase];
        }
        while (currentBase <= distanceLeft) {
            //one (a,b) value pair must be removed if there is an a value with the same parity
            if (currentBase % 2 == parity) {
                count--;
            }
            currentBase = t.increasing[currentBase];
        }
        //final check if there is an (a,b) pair with a = b that has not already been removed for being a base
        if ((distanceLeft % 3 == 0) && (t.increasing[distanceLeft / 3] == 0)) {
            count--;
        }
        //printf("q2 of %d returned %d\n", distanceLeft, count);
        return count;
    }

    static void combineFunction(long[] destinationCounts, long[] inputCounts, int maxDistanceLeft, int currentDistanceLeft){
        int distanceTraveled = maxDistanceLeft - currentDistanceLeft;
        for (int j = distanceTraveled; j <= maxDistanceLeft; j++){
            destinationCounts[j] += inputCounts[j - distanceTraveled];
        }
    }

}
