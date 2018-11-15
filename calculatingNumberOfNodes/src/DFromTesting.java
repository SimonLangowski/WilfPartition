import java.util.Arrays;
import java.util.Random;

public class DFromTesting {

    static int cutoff = 5;
    static int MAX = 100;
    static int NumToCheck = 100;
    static long[] all;

    public static void main(String[] args){
    ThreadVariables t = new ThreadVariables(MAX + 1);
    check(t);
    t.addSorted(1, 0);
    check(t);
    t.deleteAtIndex(1);
    /*
    t.addSorted(2,0);
    check(t);
    t.deleteAtIndex(2);
    t.addSorted(3, 0);
    check(t);
    t.deleteAtIndex(3);
    t.addSorted(10,0);
    check(t);
    t.deleteAtIndex(10);
    t.addSorted(1,0);
    t.addSorted(2, 1);
    check(t);
    t.addSorted(3, 2);
    check(t);
    t.deleteAtIndex(3);
    t.deleteAtIndex(2);
    t.addSorted(4, 1);
    check(t);
    ThreadVariables e = new ThreadVariables(MAX + 1);
    e.addSorted(1,0);
    e.addSorted(3,1);
    check(e);
    checkRandom();*/
}
    static void checkRandom(){
        Random r = new Random();
        for (int i = 0; i < NumToCheck; i++) {
            int numBases = r.nextInt((int) Math.sqrt(MAX));
            int[] bases = new int[numBases];
            for (int j = 0; j < bases.length; j++){
                bases[j] = r.nextInt(MAX - 1) + 1;
            }
            Arrays.sort(bases);
            ThreadVariables t = new ThreadVariables(MAX + 1);
            int lastBase = 0;
            for (int j = 0; j < bases.length; j++){
                if (lastBase != bases[j]){
                    t.addSorted(bases[j], lastBase);
                    lastBase = bases[j];
                }
            }
            if (!check(t)){
                break;
            }
        }
    }

    static boolean check(ThreadVariables t){
        long[] results = alternativeDFrom(MAX, t);
        long[] answers = lowerRecursionWrapper(MAX, t);
        int failCount = 0;
        for (int i = 0; i <= NumToCheck; i++){
            if (results[i] != answers[i]){
                failCount++;
            }
        }
        if (failCount > 0){
            System.out.println(t + " failed " + failCount);
            int c = Q2AllTest.NumToPrint;
            for (int i = 0; i <= NumToCheck; i++){
                if (c <= 0){
                    break;
                }
                if (results[i] != answers[i]){
                    System.out.println("At " + i + " should be " + answers[i] + " but is " + results[i]);
                    c--;
                }
            }
            return false;
        } else {
            return true;
        }
    }


    //iteration - worst case cutoff^numTerms
    //following dTo cache method - requires a MAX by cutoff array to be transferred/read
    //is there a dFrom cache method?

    static long[] alternativeDFrom(int maxDistanceLeft, ThreadVariables t){
        ThreadVariables empty = new ThreadVariables(MAX + 1);
        if (all == null){
            all = lowerRecursionWrapper(MAX, empty);
        }
        //if the index order is known, it is probably not necessary to do PIE from nothing
        long[] totals = new long[maxDistanceLeft + 1];
        for (int i = 0; i <= maxDistanceLeft; i++){
            totals[i] = all[i];
        }
        PIEiterate(0, empty, t, true, maxDistanceLeft, totals);
        return totals;
    }

    static void PIEiterate(int lastBase, ThreadVariables currentBases, ThreadVariables totalBases, boolean subtract, int maxDistanceLeft, long[] totals){
        int myCurrentBase = totalBases.increasing[lastBase];
        while (myCurrentBase != totalBases.size){
            currentBases.addSorted(myCurrentBase, lastBase);
            long[] vals = shortDToFrom(maxDistanceLeft, currentBases);
            if (subtract){
                for (int i = 0; i <= maxDistanceLeft; i++){
                    totals[i] -= vals[i];
                }
            } else {
                for (int i = 0; i <= maxDistanceLeft; i++){
                    totals[i] += vals[i];
                }
            }
            PIEiterate(myCurrentBase, currentBases, totalBases, !subtract, maxDistanceLeft, totals);
            currentBases.deleteAtIndex(myCurrentBase);
            lastBase = myCurrentBase;
            myCurrentBase = totalBases.increasing[myCurrentBase];
        }
    }

    //need to find all things that include the bases in t with multiplicities less than cutoff - the rest can be anything
    //once I choose a base the rest is Dfrom?

    //Let's assume this will use precomputed information.  So to make 1,2 I will start with 1 and 2?  Then I can solve because it should be (total) - 1 - 2
    //is there a way to calculate all the numbers when doing the initial lowerRecursion for all?  Like each time you add a count, you increment a table of the bases it uses and the values they are at
   //I'm already going to touch all the base series combinations when running the total recursion -> so I might as well count as I go
    //This gets more complicated when using middle recursion

    static long[] shortDToFrom(int maxDistanceLeft, ThreadVariables t){
        //first include all of bases
        //long[] with = shortDTo(maxDistanceLeft, t);
        //then figure out ways to make without included bases
        //long[] without = shortDFrom(maxDistanceLeft, t);
        //error multiplicity overlap!  (but this does present an opportunity for recursion and cache levels
        return shortDTo(maxDistanceLeft, t);
    }

    static void shortDFrom(int currentSum, ThreadVariables basesUsed, boolean[] multiplicitiesUsed, long[] counts, int maxDistanceLeft){
        //this can cache on the multiplicities used since the basesUsed will be a constant for the first round
        counts[currentSum]++;
        int lastSeen = 0;
        for (int b = 1; b <= (maxDistanceLeft - currentSum); b++){
            if (basesUsed.increasing[b] == 0){
                basesUsed.addSorted(b, lastSeen);
                int total = b + currentSum;
                for (int m = 1; m < cutoff; m++){
                    if (multiplicitiesUsed[m] == false){
                        multiplicitiesUsed[m] = true;
                        shortDFrom(currentSum, basesUsed, multiplicitiesUsed, counts, maxDistanceLeft);
                        multiplicitiesUsed[m] = false;
                    }
                    total += b;
                    if (total > maxDistanceLeft){
                        break;
                    }
                }
            } else {
                lastSeen = b;
            }
        }
    }

    //computes distanceTo, under the cutoff for use with PIE
    static long[] shortDTo(int maxDistanceLeft, ThreadVariables t){
        //this should be cached or gotten from file (but it might be fast enough ok)
        long[] array = new long[maxDistanceLeft + 1];
        boolean[] multiplicitiesUsed = new boolean[cutoff + 1];
        distanceToInternal(array, t, t.increasing[0], 0, multiplicitiesUsed, maxDistanceLeft);
        if (t.increasing[0] == t.size) {
            array[0] = 1;
        }
        return array;
    }

    //if this actually has to count by 1 beyond the size of unsigned long, it will take forever
//assuming I will make this more efficient in the future
//can't reuse multiplicities lol
    static void distanceToInternal(long[] counts, ThreadVariables t, int base, int currentSum, boolean[] multiplicities, int maxDistanceLeft) {
        if (base == t.size) {
            shortDFrom(currentSum, t, multiplicities, counts, maxDistanceLeft);
            return;
        }
        currentSum += base; //add minimum number of times
        for (int m = 1; m < cutoff; m++) {
            if (currentSum > maxDistanceLeft){
                break;
            }
            if (multiplicities[m] == false) {
                multiplicities[m] = true;
                distanceToInternal(counts, t, t.increasing[base], currentSum, multiplicities, maxDistanceLeft);
                multiplicities[m] = false;
            }
            currentSum += base;
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
                Testing.combineFunction(counts, values, maxDistanceLeft, baseDistanceLeft);
                if (moreValues != null) {
                    Testing.combineFunction(counts, moreValues, maxDistanceLeft, baseDistanceLeft);
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
    }
}
