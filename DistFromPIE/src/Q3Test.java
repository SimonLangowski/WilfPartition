import java.util.Arrays;
import java.util.Random;

public class Q3Test {

    //I can say q3 is bounded by 6 * MAX for (1)(MAX)+(2)(MAX)+(3)*(MAX) (so actually it fits in short) - lol no because it is the count of ways to make - expand the generating function

    //I can have an array of the cummulative lengths of the rows to skip to the correct location and minimize ram usage efficiently
    //actually the ordering does matter, and I'll have three arrays for the multiplicities - 1 and 2, 1 and 3, 2 and 3

    //is it supposed to be PIE in terms of numTerms??

    //what if instead I try to build q3 that accepts consecutive indexes from distTo - and so only has to change numTerms things
    //it would remember certain positions for each future jump - so that they would only require one number substitution as well.
    //it could even still be efficient to compute the arrays you must add and subtract

    static int NumToCheck = 10;
    static int NumToPrint = 10;
    static boolean viewOnly = false;

    public static void main(String[] args){
        printArray(allWaysCache(12));
        printArray(allWaysCache(13));
        printArray(allWaysCache(23));
        printArray(allWaysCache(123));
        ThreadVariables t = new ThreadVariables(NumToCheck + 1);
        //int a = 2;
        //int b = 4;
        //long[] all = q3(NumToCheck, t);
        //t.addSorted(a,0);
        //long[] with1 = q3(NumToCheck, t);
        //t.deleteAtIndex(a);
        //t.addSorted(b,0);
        //long[] with2 = q3(NumToCheck, t);
        //t.deleteAtIndex(b);
        //t.addSorted(a,0);
        //t.addSorted(b,a);
        //long[] with12 = q3(NumToCheck, t);
        //printArray(difference(all, with1));
        //printArray(difference(all, with2));
        //printArray(difference(all, with12));
        //long[] together = sum(difference(all, with1), difference(all, with2));
        //printArray(together);
        //printArray(difference(together, difference(all, with12)));
        t = new ThreadVariables(NumToCheck + 1);
        //check(t);
        //t.addSorted(1, 0);
        //check(t);
        //t.deleteAtIndex(1);
        //t.addSorted(2,0);
        //check(t);
        //t.deleteAtIndex(2);
        //t.addSorted(3, 0);
        //check(t);
        //t.deleteAtIndex(3);
        //t.addSorted(10,0);
        //check(t);
        //t.deleteAtIndex(10);
        t.addSorted(1,0);
        //t.addSorted(2, 1);
        //check(t);
        //t.addSorted(3, 2);
        check(t);
        System.out.println(Arrays.toString(q3HardcodedFlipped(NumToCheck, t)));
        System.out.println(Arrays.toString(q3PolyMul(NumToCheck, t)));
        //t.deleteAtIndex(3);
        //t.deleteAtIndex(2);

        t.addSorted(4, 1);
        check(t);

        ThreadVariables e = new ThreadVariables(NumToCheck + 1);
        e.addSorted(1,0);
        e.addSorted(3,1);
        check(e);
        //checkRandom();
    }

    static long[] difference(long[] a, long[] b){
        long[] output = new long[NumToCheck + 1];
        for (int i = 0; i <= NumToCheck; i++){
            output[i] = a[i] - b[i];
        }
        return output;
    }

    static long[] sum(long[] a, long[] b){
        long[] output = new long[NumToCheck + 1];
        for (int i = 0; i <= NumToCheck; i++){
            output[i] = a[i] + b[i];
        }
        return output;
    }


    static void checkRandom(){
        Random r = new Random();
        for (int i = 0; i < NumToCheck; i++) {
            int numBases = 7;//r.nextInt((int) Math.sqrt(NumToCheck));
            int[] bases = new int[numBases];
            for (int j = 0; j < bases.length; j++){
                bases[j] = r.nextInt((NumToCheck - 1) / numBases^2) + 1;
            }
            Arrays.sort(bases);
            int m = 1;
            int total = 0;
            for (int j = 0; j < bases.length; j++){
                total += bases[j] * m;
                m++;
            }
            if (total > NumToCheck){
                i--;
                continue;
            }

            ThreadVariables t = new ThreadVariables(NumToCheck + 1);
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

    static void printArray(long[] a){
        Calculate3.printArray(a);
    }

    static boolean check(ThreadVariables t){
        if (viewOnly){
            long startTime = System.nanoTime();
            long[] answers = q3(NumToCheck, t);
            long oldTime = System.nanoTime() - startTime;
            System.out.println(t + " " + oldTime + "ns");
            Calculate3.printArray(answers);
            return true;
        }
        long startTime = System.nanoTime();
        long[] results = q3New(NumToCheck, t);
        long newTime = System.nanoTime() - startTime;
        startTime = System.nanoTime();
        long[] answers = q3(NumToCheck, t);
        long oldTime = System.nanoTime() - startTime;
        int failCount = 0;
        for (int i = 0; i <= NumToCheck; i++){
            if (results[i] != answers[i]){
                failCount++;
            }
        }
        if (failCount > 0){
            System.out.println(t + " failed " + failCount);
            int c = NumToPrint;
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
            if (newTime < oldTime){
                System.out.print("FASTER: ");
            } else {
                System.out.print("SLOWER: ");
            }
            System.out.println(t + " passed in " + newTime + " ns (" + oldTime + "ns before)");
            return true;
        }
    }

    static long[] q3(int maxDistanceLeft, ThreadVariables t){

        return q3HardcodedFlipped(maxDistanceLeft, t);
        //return middleRecursionWrapper(maxDistanceLeft, t, 4);
    }

    static long[] middleRecursionWrapper(int maxDistanceLeft, ThreadVariables t, int cutoff) {
        //it would be a lot less confusing if you just cached here at the top of the method, but the issue is with node iteration and excess get calls for each multiplicity
        //but you don't even do it in that order right now
        if (cutoff < 3){
            throw new IllegalArgumentException();
        }
        long [] vals;
        long[] a = Q2Test.q2AllNew(maxDistanceLeft, t);
        for (int c = 3; c < cutoff; c++) {
            long[] b = lowerRecursion(maxDistanceLeft, c, t, cutoff);
            for (int j = 0; j <= maxDistanceLeft; j++) {
                a[j] = a[j] + b[j];
            }
        }
        vals = a;
        return vals;
    }

    static long[] lowerRecursion(int maxDistanceLeft, int myMultiplicity, ThreadVariables t, int cutoff) {
        long[] counts = new long[maxDistanceLeft + 1];
        int lastSeen = 0;
        int baseDistanceLeft = maxDistanceLeft - myMultiplicity;
        for (int b = 1; b <= maxDistanceLeft; b++, baseDistanceLeft -= myMultiplicity) {
            if (baseDistanceLeft < 0) {
                break;
            }
            if (t.increasing[b] == 0) {
                t.addSorted(b, lastSeen);
                long[] values = Q2Test.q2AllNew(baseDistanceLeft, t);
                long[] moreValues = null;
                //increment m
                //need to run on all values of m up to cutoff
                if (myMultiplicity + 1 < cutoff) { //check if m + 1 is within my range
                    //recurse for m + 1
                    moreValues = lowerRecursion(baseDistanceLeft, myMultiplicity + 1, t, cutoff);
                    for (int k = myMultiplicity + 2; k < cutoff; k++) {
                        long[] moreMoreValues = lowerRecursion(baseDistanceLeft, k, t, cutoff);
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

    static void combineFunction(long[] destinationCounts, long[] inputCounts, int maxDistanceLeft, int currentDistanceLeft){
        int distanceTraveled = maxDistanceLeft - currentDistanceLeft;
        for (int j = distanceTraveled; j <= maxDistanceLeft; j++){
            destinationCounts[j] += inputCounts[j - distanceTraveled];
        }
    }

    /*static long[][] q3OneCache;

    static long[] q3OneIncludeCache(int baseIncluded){
        if (q3OneCache == null){
            q3OneCache = new long[NumToCheck + 1][];
        }
        if (q3OneCache[baseIncluded] == null){
            q3OneCache[baseIncluded] = q3OneIncludeCalculate(baseIncluded);
        }
        return q3OneCache[baseIncluded];
    }

    //This is just PIE - eventually the PIE will be redone based on the fact that consecutive requests should vary by one number
    static long[] q3NewAttempt1(int maxDistanceLeft, ThreadVariables t){
        long[] total = new long[NumToCheck + 1]; //maxDistLeft??
        System.arraycopy(allWaysCache(123), 0, total, 0, total.length); //maxDistLeft??
        int currentBase = t.increasing[0];
        //single exclusion
        while (currentBase != t.size) {
            long[] possibilities = q3OneIncludeCalculate(currentBase);
            for (int j = 0; j < total.length; j++){
                total[j] -= possibilities[j];
            }
            currentBase = t.increasing[currentBase];
        }
        //double inclusion
        //all of the iterations through the variables can be combined
        applyDoubleExclusion(total, t, 1, 2, 3);
        applyDoubleExclusion(total, t, 2, 3, 1);
        applyDoubleExclusion(total, t, 1, 3, 2);
        applyTripleInclusion(total, t, 1, 2, 3);
        return total;
    }

    //the outer loop here can be merged with the loop in the single
    static void applyDoubleExclusion(long[] target, ThreadVariables t, int m, int n, int thirdBase){
        int currentBase = t.increasing[0];
        while (currentBase != t.size) {
            int secondBase = t.increasing[0];
            while (secondBase != t.size) {
                if (secondBase == currentBase) {
                    secondBase = t.increasing[secondBase];
                    continue;
                }
                int offset = m * currentBase + n * secondBase;
                for (int j = offset; j <= NumToCheck; j += thirdBase){
                    target[j]--;
                }
                secondBase = t.increasing[secondBase];
            }
            currentBase = t.increasing[currentBase];
        }
    }

    //outer loop here can be merged with double exclusion
    //there's probably a way to avoid the crossing overs between the loops where you have to check if there equal
    //by doing the combination method and then applying on all the permutations
    static void applyTripleInclusion(long[] target, ThreadVariables t, int m, int n, int p){
        int currentBase = t.increasing[0];
        while (currentBase != t.size) {
            int secondBase = t.increasing[0];
            while (secondBase != t.size) {
                if (secondBase == currentBase) {
                    secondBase = t.increasing[secondBase];
                    continue;
                }
                int thirdBase = t.increasing[0];
                while (thirdBase != t.size) {
                    if ((thirdBase == currentBase) || (thirdBase == secondBase)) {
                        thirdBase = t.increasing[thirdBase];
                        continue;
                    }
                    if (m * currentBase + n * secondBase + p * thirdBase <= NumToCheck) {
                        target[m * currentBase + n * secondBase + p * thirdBase]--;
                    }
                    thirdBase = t.increasing[thirdBase];
                }
                secondBase = t.increasing[secondBase];
            }
            currentBase = t.increasing[currentBase];
        }
    }

    //I'm not sure if the cache is necessary
    static long[] q3OneIncludeCalculate(int baseIncluded){
        long[] total = new long[NumToCheck + 1];
        //(a)(1)+(b)(2)+(c)(3) where a != b != c and one of a,b,c is baseIncluded
        //a,b,c can be zero without restriction on multiplicity
        //a = baseIncluded
        //all the ways with 2,3 minus those where b or c = baseIncluded
        addShifted(total, calculateOneExclude(23,baseIncluded), baseIncluded*1);
        //b = baseIncluded
        //all the ways with 1,3 minus those where a or c = baseIncluded
        addShifted(total, calculateOneExclude(13,baseIncluded), baseIncluded*2);
        //c = baseIncluded
        //all the ways with 1,2 minus those where a or b = baseIncluded
        addShifted(total, calculateOneExclude(12, baseIncluded), baseIncluded*3);
        return total;
    }*/

    static void addShifted(long[] destination, long[] source, int shift){
        for (int j = shift; j < destination.length; j++){
            destination[j] += source[j - shift];
        }
    }

    static void cummulativeShift(long[] target, int shift){
        for (int j = shift; j < target.length; j++){
            target[j] += target[j - shift];
        }
    }
/*
    //the clone actually isn't necessary because they are going to be copied again in the shift (i.e. the subtract here would take place during the shift)
    //things will be so much easier if primitives, even extended as two side by side longs, are used instead of mpz_t for all the array stuff
    static long[] calculateOneExclude(int descriptor, int base){
        //(base)(2)+(c)(3) - start at base and count by 3's, subtract one from these ones
        //I am allowing c to be 0
        long[] clone = new long[NumToCheck + 1];
        System.arraycopy(allWaysCache(descriptor), 0, clone, 0, clone.length);
        if (descriptor == 12){
            for (int j = base; j <= NumToCheck; j+= 2){
                clone[j]--;
            }
        } else if (descriptor == 13) {
            for (int j = base; j <= NumToCheck; j += 3){
                clone[j]--;
            }
        } else if (descriptor == 23){
            for (int j = 2*base; j <= NumToCheck; j+= 3){
                clone[j]--;
            }
        }
        return clone;
    }*/

    static long[][] allWaysCache;
    static int fromMaxMult = 3;

    static void makeAllWaysCacheDistToMethod(){
        //hardcoded with 3 for testing
        allWaysCache = new long[8][NumToCheck + 1];
        int currentDescriptor = 0;
        for (int jump = 1; jump <= 3; jump++){
            for (int j = jump; j <= NumToCheck; j += jump){
                allWaysCache[currentDescriptor][j] = 1;
            }
            currentDescriptor++;
        }
        //1,2
        addShifted(allWaysCache[3], allWaysCache[0], 3);
        addShifted(allWaysCache[3], allWaysCache[1], 3);
        cummulativeShift(allWaysCache[3], 3);
        //1,3
        addShifted(allWaysCache[4], allWaysCache[0], 4);
        addShifted(allWaysCache[4], allWaysCache[2], 4);
        cummulativeShift(allWaysCache[4], 4);
        //2,3
        addShifted(allWaysCache[5], allWaysCache[1], 5);
        addShifted(allWaysCache[5], allWaysCache[2], 5);
        cummulativeShift(allWaysCache[5], 5);
        //1,2,3
        addShifted(allWaysCache[6], allWaysCache[3], 6);
        addShifted(allWaysCache[6], allWaysCache[4], 6);
        addShifted(allWaysCache[6], allWaysCache[5], 6);
        cummulativeShift(allWaysCache[6], 6);

        //do I want to add them together?
        //I think I do,
        for (int i = 0; i < 6; i++){
            for (int j = 0; j <= NumToCheck; j++){
                allWaysCache[6][j] += allWaysCache[i][j];
            }
        }
        addShifted(allWaysCache[5], allWaysCache[1], 0);
        addShifted(allWaysCache[5], allWaysCache[2], 0);
        addShifted(allWaysCache[4], allWaysCache[0], 0);
        addShifted(allWaysCache[4], allWaysCache[2], 0);
        addShifted(allWaysCache[3], allWaysCache[0], 0);
        addShifted(allWaysCache[3], allWaysCache[1], 0);
        for (int i = 0; i <= 6; i++){
            allWaysCache[i][0] = 1;
        }

    }

    static long[] allWaysCache(int descriptor){
        if (allWaysCache == null){
            makeAllWaysCacheDistToMethod();
        }
        switch(descriptor){
            case 12: return allWaysCache[3];
            case 13: return allWaysCache[4];
            case 23: return allWaysCache[5];
            case 123: return allWaysCache[6];
        }
        return allWaysCache[descriptor];
    }

    /*
    static void makeAllWaysCacheDistToMethod(){
        //there is a generative iterative method for things at the same numterms by index already that could be capped with cutoff
        int totalWays = (int) Math.pow(2,cutoff);
        allWaysCache = new long[totalWays][];
        int currentIndex = 0;
        for (int
    }*/

    //the time is proportional to (maxExcludedBase)^2 2^cutoff
    //as cutoff increases, the time double here, but the possibilities decrease faster (and 2 is much better than n).  In addition, the maxExcludedBase decreases
    /*static long[] makeDistFromDistToMethod(ThreadVariables exclusionPattern, int maxDistLeft){

        //technically I only need each numTerms layer at a time
        //need to calculate at each shift of the pattern

    }*/

    /*static long[] allWaysCache(int descriptor){
        if (allWaysCache == null){
            allWaysCache = new long[4][];
        }
        if (descriptor == 12){
            if (allWaysCache[0] == null){
                allWaysCache[0] = makeAllWays(1,2);
            }
            return allWaysCache[0];
        } else if (descriptor == 13){
            if (allWaysCache[1] == null){
                allWaysCache[1] = makeAllWays(1,3);
            }
            return allWaysCache[1];
        } else if (descriptor == 23){
            //all the ways with 2 and 3 - shift 2, shift 3, subtract where a=b aka multiples of 5
            if (allWaysCache[2] == null){
                long[] cacheValues = new long[NumToCheck];
                cacheValues[0] = 1;
                //shift 2
                for (int j = 2; j <= NumToCheck; j+=2){ //technically go by 1's, but it doesn't matter
                    cacheValues[j] += cacheValues[j-2];
                }
                //shift 3
                for (int j = 3; j <= NumToCheck; j++) {
                    cacheValues[j] += cacheValues[j-3];
                }
                //subtract multiples of 5
                for (int j = 5; j <= NumToCheck; j+= 5){
                    cacheValues[j]--;
                }

            }
            return allWaysCache[2];
        } else if (descriptor == 123){
            if (allWaysCache[3] == null){
                long[] cacheValues = new long[NumToCheck];
                cacheValues[0] = 1;
                //shift 1
                for (int j = 1; j <= NumToCheck; j++){
                    cacheValues[j] += cacheValues[j - 1];
                }
                //shift 2
                for (int j = 2; j <= NumToCheck; j++){
                    cacheValues[j] += cacheValues[j - 2];
                }
                //shift 3
                for (int j = 3; j <= NumToCheck; j++){
                    cacheValues[j] += cacheValues[j - 3];
                }
                //remove duplicates

                //maybe I do 1,2 with duplicates removed (i.e. cacheValues[0]) and then shift by 3 and remove duplicates between 1 and 3 and 2 and 3?

                //distto recursion for bases 1,2,3 and cutoff 1 should be exactly these numbers
                //and would also create the rest of the cache values reliably and scalably (in time proportion to the number cache values needed)

                //could even have 2^n threads that all read the same job (semaphore+=2^n when posting) and all compute a part with exclusions

                //can I modify distTo recursion to exclude certain multiplicities? - then always 2^cutoff time (which you need to touch anyway, and is less than (numterms+1)^cutoff)
                //can I make other cutoff regions such as by using distTo bases 4,5? for multiplicities 4,5?



            }
            return allWaysCache[3];
        }
    }*/

    static long[] makeAllWays(int m, int n){
        long[] values = new long[NumToCheck];
        values[0] = 1;
        //or I could also say add 1 if a multiple of n, add the number from m below me, and subtract one if a multiple of m+n
        //this is what q2all does where n is 2 and m is 1
        for (int j = m; j <= NumToCheck; j+=m){
            values[j] = 1;
        }
        for (int j = n; j <= NumToCheck; j++){
            values[j] += values[j - n];
        }
        for (int j = m + n; j <= NumToCheck; j += (m + n)){
            values[j]--;
        }
        return values;
    }

    static long[] shift(long[] a, int shift){
        long[] output = new long[NumToCheck + 1];
        for (int i = shift; i <= NumToCheck; i++){
            output[i] = a[i - shift];
        }
        return output;
    }

    //takes advantage of similarity between number of solutions for consecutive/close by numbers
    //You could hardcode one for each and it would be faster?  But instruction caching
/*    static long[] q2mnAll(int maxDistanceLeft, ThreadVariables t, int m, int n){

        //count all (a)(m)+(b)(n) with a != b and a,b not elements of t

        assert(m < n);
        long[] c = new long[];
        int cummulativeCount = 1;
        int[] mCounts = new int[m];
        int[] nCounts = new int[n];
        for (int i = 0; i <= maxDistanceLeft; i++) {

        }


        int currentBase = t.increasing[0];
        while (currentBase != t.size){
            int nextBase = t.increasing[0];
            while (nextBase != t.size){
                if (m*currentBase + n*nextBase <= maxDistanceLeft) {
                    c[m * currentBase + n*nextBase]++;
                } else {
                    break;
                }
                nextBase = t.increasing[nextBase];
            }
            if ((m+n) * currentBase <= maxDistanceLeft){
                c[(m+n) * currentBase]++;
            } else if (m * currentBase > maxDistanceLeft){
                break;
            }
            currentBase = t.increasing[currentBase];
        }
        for (int i = (m+n); i <= maxDistanceLeft; i = i + (m+n)){
            c[i]--;
        }
        c[0] = 1;

    }*/


//pie faster than distFrom
//might still be something faster, but test this first!


    static long[] q3New(int maxDistanceLeft, ThreadVariables t){
        //System.out.println(t);
        //q3HardcodedFlipped(maxDistanceLeft, t);
        return q3PolyMul(maxDistanceLeft,t);
        //return q3HardcodedFlipped(maxDistanceLeft, t);
        //return q3Hardcoded(maxDistanceLeft, t);
        //return q3PolyMul(maxDistanceLeft,t);
    }


    //is there an efficiency advantage from adding the even terms and subtracting the sum of the odd?
    //Want to minimize magnitudes for operations I guess so it would be less efficient

    //could reference each with an int referring to the multiplicities included with each bit - 32 bits will cover cutoff 30
    //mult to add is bit removed for each
    //this would be slow because it would have to iterate through them each time
    //so they should just be stored ahead of time (can use above method)

    //should write test in java before coding in c
    //general version for all ways cache will probably be read from a file output from a modified dist to with cutoff 1, modified to print up to max always
    //then can be read into poly structs (I guess you would use minsums to save space and make jagged arrays)

    //If I can swap the poly addition loop to go in order of increasing base, then moving sequentially just means computing the last iteration of the loop for the new base
    //then a second base iteration simply requires two iterations of the loop (or starting over if you don't want to store that many extra polynomials
    //if moving sequentially can store first numterms-1 polynomials, then just compute the last and multiply without storing? - need some storage for shifting to work correctly
    //If I don't compute sequentially then each thread needs 2^cutoff * n storage for its intermediate polynomials
    //yup yup it's symmetric, so I can do such
    //it should only take 2^cutoff numterms -1 time to recompute every 2 cycle so idk what storage/time compromise is correct
    //each only has to add at most numterms arrays so I can store just that many? - not quite because each index has different dependent arrays
    //could just compress arrays if storing?  The only reason to use all n is because the sizes are variable, but some should generally be smaller
    //maybe all the poly arrays should have variable sizes - since I'm doing 2^cutoff work it's not unreasonable to malloc and let malloc deal with it - but I have to memset then and worry about fmpz pointers (not expecting any in the polys though, just in the products/pie, and maybe even only after the counts multiply)

    //The worse case value in the numterms arrays is all of them in the same position.  Can precheck will fit in fmpz
    //precomputed values can be read into mmap pages of fmpz to avoid memory overhead, and poly just contains a pointer to the array
    //smallest base and largest base uniquely determine the entire size needed for the polynomials so you could precompute it and also only use one allocation

    //most arrays seem pretty sparse so perhaps classical multiplication is still the best bet - at least for the arrays with possibilities using only a few multiplicities (1 or 2 for sure)
    //at 3 and above polynomial multiplication looks likely to be worthwhile

    //can compute maxDegree for each currentBase * mult and the max for the addShifted outside of the function to avoid excess additions when not all the numterms are added yet

    static long[] q3PolyMul(int maxDistanceLeft, ThreadVariables t){

        //precomputation is as simple as running distTo cutoff 1 with bases less than the multiplcity, and it is equivalent by wilf inversion

        long[] all = allWaysCache(123);
        long[] currentContains = new long[maxDistanceLeft + 1];
        System.arraycopy(all,0,currentContains,0,maxDistanceLeft + 1);
        Polynomial[] shifts = generateShiftsPolyMethod(maxDistanceLeft, t);
        //Polynomial[] correctShifts = generateShiftsHardcoded(maxDistanceLeft, t);
        //Polynomial[] shifts = correctShifts;
        for (int i = 1; i < 8; i++){
            //System.out.println(correctShifts[i]);
            System.out.println(shifts[i]);
        }
        //I think indexes could be designed so 7-index is index in other array due to symmetry of complements


        subtract(currentContains, shifts[1].productWith(allWaysCache(23), maxDistanceLeft));
        subtract(currentContains, shifts[2].productWith(allWaysCache(13), maxDistanceLeft));
        subtract(currentContains, shifts[3].productWith(allWaysCache(12), maxDistanceLeft));
        //System.out.print("Poly: Values after first removal: ");
        //printArray(currentContains);

        //it might be possible to modify these to avoid doing the last exclusion

        add(currentContains, shifts[4].trivialProductWith(3, maxDistanceLeft));
        add(currentContains, shifts[5].trivialProductWith(2, maxDistanceLeft));
        add(currentContains, shifts[6].trivialProductWith(1, maxDistanceLeft));
        //System.out.print("Poly: Values after second removal: ");
        //printArray(currentContains);
        subtract(currentContains, shifts[7].getFullArray(maxDistanceLeft));
        //System.out.println(shifts[7]);
        //System.out.print("Poly: final values: ");
        //printArray(currentContains);
        return currentContains;
    }

    static int[] multSums = new int[]{0,1,2,3,3,4,5,6};
    static int[][] constructs = new int[][]{{},{0},{0},{0},{1,2},{1,3},{2,3},{4,5,6}};
    static int[][] conjugations = new int[][]{{},{1},{2},{3},{2,1},{3,1},{3,2},{3,2,1}};

    static Polynomial[] generateShiftsPolyMethod(int maxDistanceLeft, ThreadVariables t){
        int smallestBase = t.increasing[0];
        int largestBase = t.decreasing[t.size];
        if (smallestBase > largestBase){
            smallestBase = largestBase;
        }
        Polynomial[] polynomials = new Polynomial[8];
        for (int i = 1; i < 8; i++) {
            polynomials[i] = new Polynomial(smallestBase, largestBase, multSums[i], maxDistanceLeft);
        }

        //int currentBase = t.decreasing[t.size];
        int currentBase = t.increasing[0];
        while (currentBase != t.size){
            for (int i = 1; i <= fromMaxMult; i++){
                polynomials[i].setCoeff(currentBase * multSums[i], 1);
            }

            polynomials[4].addShifted(polynomials[1], 2*currentBase, maxDistanceLeft);
            polynomials[4].addShifted(polynomials[2], currentBase, maxDistanceLeft);

            polynomials[5].addShifted(polynomials[1], 3*currentBase, maxDistanceLeft);
            polynomials[5].addShifted(polynomials[3], currentBase, maxDistanceLeft);

            polynomials[6].addShifted(polynomials[2], 3*currentBase, maxDistanceLeft);
            polynomials[6].addShifted(polynomials[3], 2*currentBase, maxDistanceLeft);

            polynomials[7].addShifted(polynomials[4], 3*currentBase, maxDistanceLeft);
            polynomials[7].addShifted(polynomials[5], 2*currentBase, maxDistanceLeft);
            polynomials[7].addShifted(polynomials[6], currentBase, maxDistanceLeft);

            //currentBase = t.decreasing[currentBase];
            currentBase = t.increasing[currentBase];
        }

        //add and shift - can make a table to statically determine shifts and rows to shift with
        //can just add the first have, and symmetrically construct the second half - thus you don't need to add the extra term
        //create space from smallestBase*multSum to largestBase * multSum
        //then symmetric add shifted the polynomials and finalize symmetry - symmetry point can be even (i.e. between integers)! - need to add through symmetry point if odd (and even oddness based on length, not degree value)

        //I feel like this add shifted should just be the same as poly mult?

        /*polynomials[4] = new Polynomial( multSums[4] * smallestBase, multSums[4] * largestBase);
        int size = (largestBase - smallestBase) * multSums[4] + 1;
        boolean symType = false;
        int symPoint;
        if (size % 2 == 0){
            symType = false;
            symPoint = size / 2;
        } else {
            symType = true;
            symPoint = size / 2 + 1;
        }
        symPoint += multSums[4] * smallestBase - 1;
        polynomials[4].addShifted(polynomials[1], 2 * smallestBase, symPoint);
        polynomials[4].addShifted(polynomials[2], 1 * smallestBase, symPoint);
        polynomials[4].finalizeSymmetry(symPoint, symType);

        polynomials[5] = new Polynomial( multSums[5] * smallestBase, multSums[5] * largestBase);
        size = (largestBase - smallestBase) * multSums[5] + 1;
        if (size % 2 == 0){
            symType = false;
            symPoint = size / 2;
        } else {
            symType = true;
            symPoint = size / 2 + 1;
        }
        symPoint += multSums[5] * smallestBase - 1;
        polynomials[5].addShifted(polynomials[1], 3 * smallestBase, symPoint);
        polynomials[5].addShifted(polynomials[3], 1 * smallestBase, symPoint);
        polynomials[5].finalizeSymmetry(symPoint, symType);

        polynomials[6] = new Polynomial( multSums[6] * smallestBase, multSums[6] * largestBase);
        size = (largestBase - smallestBase) * multSums[6] + 1;
        if (size % 2 == 0){
            symType = false;
            symPoint = size / 2;
        } else {
            symType = true;
            symPoint = size / 2 + 1;
        }
        symPoint += multSums[6] * smallestBase - 1;
        polynomials[6].addShifted(polynomials[2], 3 * smallestBase, symPoint);
        polynomials[6].addShifted(polynomials[3], 2 * smallestBase, symPoint);
        polynomials[6].finalizeSymmetry(symPoint, symType);

        polynomials[7] = new Polynomial( multSums[7] * smallestBase, multSums[7] * largestBase);
        size = (largestBase - smallestBase) * multSums[7] + 1;
        if (size % 2 == 0){
            symType = false;
            symPoint = size / 2;
        } else {
            symType = true;
            symPoint = size / 2 + 1;
        }
        symPoint += multSums[7] * smallestBase - 1;
        if (symPoint > maxDistanceLeft){
            symPoint = maxDistanceLeft;
        }
        polynomials[7].addShifted(polynomials[4], 3 * smallestBase, symPoint);
        polynomials[7].addShifted(polynomials[5], 2 * smallestBase, symPoint);
        polynomials[7].addShifted(polynomials[6], 1 * smallestBase, symPoint);
        polynomials[7].finalizeSymmetry(symPoint, symType);*/

        /*polynomials[4] = Polynomial.add(Polynomial.multiply(polynomials[1], polynomials[2], maxDistanceLeft), new Polynomial(t, maxDistanceLeft, 3));
        polynomials[5] = Polynomial.add(Polynomial.multiply(polynomials[1], polynomials[3], maxDistanceLeft), new Polynomial(t, maxDistanceLeft, 4));
        polynomials[6] = Polynomial.add(Polynomial.multiply(polynomials[2], polynomials[3], maxDistanceLeft), new Polynomial(t, maxDistanceLeft, 5));*/
        //choose 3 polynomials by multiplying choose 2 by choose 1 - preferably the shortest ones
        //could this become the same multiply for every layer?  Where I choose the max index to minimize the multiplications (and probably do classical?)
        //polynomials[7] = Polynomial.add(Polynomial.multiply(polynomials[3], polynomials[4], maxDistanceLeft), new Polynomial(t, maxDistanceLeft, 6, 4));
        return polynomials;
    }

    /*static Polynomial[] generateShiftsPolyMethod(int maxDistanceLeft, ThreadVariables t){
        Polynomial[] polynomials = new Polynomial[8];
        //the polynomial is [1,0,0,0...] for 0 terms, which is one, which corresponds to adding allWaysCache(123)

        //base polynomials
        for (int index = 1; index <= fromMaxMult; index++){
            polynomials[index] = new Polynomial(t, maxDistanceLeft, index);
        }
        //choose 2 polynomials
        polynomials[4] = Polynomial.add(Polynomial.multiply(polynomials[1], polynomials[2], maxDistanceLeft), Polynomial.multiply(polynomials[2], polynomials[1], maxDistanceLeft));
        polynomials[5] = Polynomial.add(Polynomial.multiply(polynomials[1], polynomials[3], maxDistanceLeft), Polynomial.multiply(polynomials[3], polynomials[1], maxDistanceLeft));
        polynomials[6] = Polynomial.add(Polynomial.multiply(polynomials[2], polynomials[3], maxDistanceLeft), Polynomial.multiply(polynomials[3], polynomials[2], maxDistanceLeft));
        //choose 3 polynomials by multiplying choose 2 by choose 1 - preferably the shortest ones
        //could this become the same multiply for every layer?  Where I choose the max index to minimize the multiplications (and probably do classical?)
        polynomials[7] = Polynomial.add(Polynomial.add(Polynomial.multiply(polynomials[1], polynomials[6], maxDistanceLeft), Polynomial.multiply(polynomials[2], polynomials[5], maxDistanceLeft)), Polynomial.multiply(polynomials[3], polynomials[4], maxDistanceLeft));
        return polynomials;
    }*/

    static void add(long[] dest, long[] src){
        for (int i = 0; i < Math.min(dest.length, src.length); i++){
            dest[i] += src[i];
        }
    }

    static void subtract(long[] dest, long[] src){
        for (int i = 0; i < Math.min(dest.length, src.length); i++){
            dest[i] -= src[i];
        }
    }

    //n * (numTerms)^cutoff
    static long[] q3Hardcoded(int maxDistanceLeft, ThreadVariables t){
        long[] all = allWaysCache(123);
        long[] currentContains = new long[maxDistanceLeft + 1];
        int currentBase = t.increasing[0];
        while (currentBase != t.size) {
            //subtract things with this base used
            //for caching this should be done as three separate loops (or perhaps multithreaded)
            //for n memory could precompute for each possible baseExcluded
            //1,3, all values - all bases with mults 1 and 3 - shifted by 2*baseExcluded
            long[] all13 = allWaysCache(13);
            for (int i = 2*currentBase; i <= maxDistanceLeft; i++){
                currentContains[i] += all13[i - 2*currentBase];
            }
            //1,2, all values - all bases with mults 1 and 2 - shifted by 3*baseExcluded
            long[] all12 = allWaysCache(12);
            for (int i = 3*currentBase; i <= maxDistanceLeft; i++){
                currentContains[i] += all12[i - 3*currentBase];
            }
            //2,3 all values - all bases with mults 2 and 3 - shifted by baseExcluded
            long[] all23 = allWaysCache(23);
            for (int i = currentBase; i <= maxDistanceLeft; i++){
                currentContains[i] += all23[i - currentBase];
            }
            currentBase = t.increasing[currentBase];

        }
        currentBase = t.increasing[0];
        while (currentBase != t.size){
            int secondBase = currentBase;
            while(secondBase != t.size){
                int shift3 = currentBase + 2 * secondBase;
                int shift2 = currentBase + 3 * secondBase;
                int shift1 = 2 * currentBase + 3 * secondBase;
                for (int i = shift1; i <= maxDistanceLeft; i++){
                    currentContains[i]--;
                }
                for (int i = shift2; i <= maxDistanceLeft; i += 2){
                    currentContains[i]--;
                }
                for (int i = shift3; i <= maxDistanceLeft; i += 3){
                    currentContains[i]--;
                }
                shift3 = secondBase + 2*currentBase;
                shift2 = secondBase + 3*currentBase;
                shift1 = 2*secondBase + 3*currentBase;
                for (int i = shift1; i <= maxDistanceLeft; i++){
                    currentContains[i]--;
                }
                for (int i = shift2; i <= maxDistanceLeft; i += 2){
                    currentContains[i]--;
                }
                for (int i = shift3; i <= maxDistanceLeft; i += 3){
                    currentContains[i]--;
                }
                secondBase = t.increasing[secondBase];
            }
            currentBase = t.increasing[currentBase];
        }
        currentBase = t.increasing[0];
        while(currentBase != t.size){
            int secondBase = currentBase;
            while (secondBase != t.size){
                int thirdBase = secondBase;
                while (thirdBase != t.size){
                    int shift = currentBase + 2*secondBase + 3*thirdBase;
                    if (shift <= maxDistanceLeft){
                        currentContains[shift]++;
                    }
                    shift = currentBase + 3*secondBase + 2*thirdBase;
                    if (shift <= maxDistanceLeft){
                        currentContains[shift]++;
                    }
                    shift = 2*currentBase + secondBase + 3*thirdBase;
                    if (shift <= maxDistanceLeft){
                        currentContains[shift]++;
                    }
                    shift = 2*currentBase + 3*secondBase + thirdBase;
                    if (shift <= maxDistanceLeft){
                        currentContains[shift]++;
                    }
                    shift = 3*currentBase + secondBase + 2*thirdBase;
                    if (shift <= maxDistanceLeft){
                        currentContains[shift]++;
                    }
                    shift = 3*currentBase + 2*secondBase + thirdBase;
                    if (shift <= maxDistanceLeft){
                        currentContains[shift]++;
                    }
                    thirdBase = t.increasing[thirdBase];
                }
                secondBase = t.increasing[secondBase];
            }
            currentBase = t.increasing[currentBase];
        }
        for (int i = 0; i <= maxDistanceLeft; i++){
            currentContains[i] = all[i] - currentContains[i];
        }
        return currentContains;
    }

    static Polynomial[] generateShiftsHardcoded(int maxDistanceLeft, ThreadVariables t){
        Polynomial[] polynomials = new Polynomial[8];
        for (int index = 1; index <= fromMaxMult; index++){
            polynomials[index] = new Polynomial(t, maxDistanceLeft, index);
        }
        int currentBase = t.increasing[0];
        int[] poly12 = new int[maxDistanceLeft + 1];
        int[] poly13 = new int[maxDistanceLeft + 1];
        int[] poly23 = new int[maxDistanceLeft + 1];
        while (currentBase != t.size){
            int secondBase = currentBase;
            while(secondBase != t.size){
                int shift3 = currentBase + 2 * secondBase;
                int shift2 = currentBase + 3 * secondBase;
                int shift1 = 2 * currentBase + 3 * secondBase;
                if (shift3 <= maxDistanceLeft){
                    poly12[shift3]++;
                }
                if (shift2 <= maxDistanceLeft){
                    poly13[shift2]++;
                }
                if (shift1 <= maxDistanceLeft){
                    poly23[shift1]++;
                }
                shift3 = secondBase + 2*currentBase;
                shift2 = secondBase + 3*currentBase;
                shift1 = 2*secondBase + 3*currentBase;
                if (shift3 <= maxDistanceLeft){
                    poly12[shift3]++;
                }
                if (shift2 <= maxDistanceLeft){
                    poly13[shift2]++;
                }
                if (shift1 <= maxDistanceLeft){
                    poly23[shift1]++;
                }
                secondBase = t.increasing[secondBase];
            }
            currentBase = t.increasing[currentBase];
        }
        polynomials[4] = new Polynomial(poly12);
        polynomials[5] = new Polynomial(poly13);
        polynomials[6] = new Polynomial(poly23);
        int[] poly123 = new int[maxDistanceLeft + 1];
        currentBase = t.increasing[0];
        while(currentBase != t.size){
            int secondBase = currentBase;
            while (secondBase != t.size){
                int thirdBase = secondBase;
                while (thirdBase != t.size){
                    int shift = currentBase + 2*secondBase + 3*thirdBase;
                    if (shift <= maxDistanceLeft){
                        poly123[shift]++;
                    }
                    shift = currentBase + 3*secondBase + 2*thirdBase;
                    if (shift <= maxDistanceLeft){
                        poly123[shift]++;
                    }
                    shift = 2*currentBase + secondBase + 3*thirdBase;
                    if (shift <= maxDistanceLeft){
                        poly123[shift]++;
                    }
                    shift = 2*currentBase + 3*secondBase + thirdBase;
                    if (shift <= maxDistanceLeft){
                        poly123[shift]++;
                    }
                    shift = 3*currentBase + secondBase + 2*thirdBase;
                    if (shift <= maxDistanceLeft){
                        poly123[shift]++;
                    }
                    shift = 3*currentBase + 2*secondBase + thirdBase;
                    if (shift <= maxDistanceLeft){
                        poly123[shift]++;
                    }
                    thirdBase = t.increasing[thirdBase];
                }
                secondBase = t.increasing[secondBase];
            }
            currentBase = t.increasing[currentBase];
        }
        polynomials[7] = new Polynomial(poly123);
        return polynomials;
    }

    static long[] q3HardcodedFlipped(int maxDistanceLeft, ThreadVariables t){
        long[] all = allWaysCache(123);
        long[] currentContains = new long[maxDistanceLeft + 1];
        System.arraycopy(all,0,currentContains,0,maxDistanceLeft + 1);
        int currentBase = t.increasing[0];
        while (currentBase != t.size) {
            //subtract things with this base used
            //for caching this should be done as three separate loops (or perhaps multithreaded)
            //for n memory could precompute for each possible baseExcluded
            //1,3, all values - all bases with mults 1 and 3 - shifted by 2*baseExcluded
            long[] all13 = allWaysCache(13);
            for (int i = 2*currentBase; i <= maxDistanceLeft; i++){
                currentContains[i] -= all13[i - 2*currentBase];
            }
            //1,2, all values - all bases with mults 1 and 2 - shifted by 3*baseExcluded
            long[] all12 = allWaysCache(12);
            for (int i = 3*currentBase; i <= maxDistanceLeft; i++){
                currentContains[i] -= all12[i - 3*currentBase];
            }
            //2,3 all values - all bases with mults 2 and 3 - shifted by baseExcluded
            long[] all23 = allWaysCache(23);
            for (int i = currentBase; i <= maxDistanceLeft; i++){
                currentContains[i] -= all23[i - currentBase];
            }
            currentBase = t.increasing[currentBase];

        }
        //System.out.print("Values after first removal: ");
        //printArray(currentContains);
        currentBase = t.increasing[0];
        while (currentBase != t.size){
            int secondBase = currentBase;
            while(secondBase != t.size){
                int shift3 = currentBase + 2 * secondBase;
                int shift2 = currentBase + 3 * secondBase;
                int shift1 = 2 * currentBase + 3 * secondBase;
                for (int i = shift1; i <= maxDistanceLeft; i++){
                    currentContains[i]++;
                }
                for (int i = shift2; i <= maxDistanceLeft; i += 2){
                    currentContains[i]++;
                }
                for (int i = shift3; i <= maxDistanceLeft; i += 3){
                    currentContains[i]++;
                }
                shift3 = secondBase + 2*currentBase;
                shift2 = secondBase + 3*currentBase;
                shift1 = 2*secondBase + 3*currentBase;
                for (int i = shift1; i <= maxDistanceLeft; i++){
                    currentContains[i]++;
                }
                for (int i = shift2; i <= maxDistanceLeft; i += 2){
                    currentContains[i]++;
                }
                for (int i = shift3; i <= maxDistanceLeft; i += 3){
                    currentContains[i]++;
                }
                secondBase = t.increasing[secondBase];
            }
            currentBase = t.increasing[currentBase];
        }
        //System.out.print("Values after second removal: ");
        //printArray(currentContains);
        currentBase = t.increasing[0];
        while(currentBase != t.size){
            int secondBase = currentBase;
            while (secondBase != t.size){
                int thirdBase = secondBase;
                while (thirdBase != t.size){
                    int shift = currentBase + 2*secondBase + 3*thirdBase;
                    if (shift <= maxDistanceLeft){
                        currentContains[shift]--;
                    }
                    shift = currentBase + 3*secondBase + 2*thirdBase;
                    if (shift <= maxDistanceLeft){
                        currentContains[shift]--;
                    }
                    shift = 2*currentBase + secondBase + 3*thirdBase;
                    if (shift <= maxDistanceLeft){
                        currentContains[shift]--;
                    }
                    shift = 2*currentBase + 3*secondBase + thirdBase;
                    if (shift <= maxDistanceLeft){
                        currentContains[shift]--;
                    }
                    shift = 3*currentBase + secondBase + 2*thirdBase;
                    if (shift <= maxDistanceLeft){
                        currentContains[shift]--;
                    }
                    shift = 3*currentBase + 2*secondBase + thirdBase;
                    if (shift <= maxDistanceLeft){
                        currentContains[shift]--;
                    }
                    thirdBase = t.increasing[thirdBase];
                }
                secondBase = t.increasing[secondBase];
            }
            currentBase = t.increasing[currentBase];
        }
        //System.out.print("final values: ");
        //printArray(currentContains);
        return currentContains;
    }


    static long[] qn(int maxDistanceLeft, ThreadVariables t){
        //1,2,3 all values
        long[] all = allWaysCache(123);
        long[] currentContains = new long[maxDistanceLeft + 1];
        int currentBase = t.increasing[0];
        while (currentBase != t.size) {
            //subtract things with this base used
            //for caching this should be done as three separate loops (or perhaps multithreaded)
            //for n memory could precompute for each possible baseExcluded
            //1,3, all values - all bases with mults 1 and 3 - shifted by 2*baseExcluded
            long[] all13 = allWaysCache(13);
            for (int i = 2*currentBase; i <= maxDistanceLeft; i++){
                currentContains[i] += all13[i - 2*currentBase];
            }
            //1,2, all values - all bases with mults 1 and 2 - shifted by 3*baseExcluded

            //2,3 all values - all bases with mults 2 and 3 - shifted by baseExcluded

        }
        //we have removed every conflicting partition
        //however overcounted those with two or three of the bases - and removed some non wilf partitions too
        //however we can add both back together
        //choose two bases with two multiplicities
        //It might make sense to iterate through all pairs of two->n-1 bases first with the multiplicities and see if they are the same shift value, and then do a multiply/add for those values instead

        //with send combinations becomes min(numterms choose cutoff),n) for each of the numterms steps :D
        //so total time no worse than (numterms * n^2) !! (plus numterms^cutoff to pick shifts I guess lol but that might be able to be improved if needed)
        //well I still need to go through 2^cutoff mult choosing/excluding patterns


        int[][] shifts = generateShifts(maxDistanceLeft, t);
        //add shifted with permutations
        for (int multDescriptor = 1; multDescriptor < (2^fromMaxMult - 1); multDescriptor++) {

            //can also switch on mult descriptor for trivial = 1,0,0,0,0 and 1,0,1,0,1, (for loop with other increment) partitions

            //could perhaps go to max combo if this is too inefficient (ex max combo = max bases with max mults in parallel order), can also make a min combo too

            for (int shift = 0; shift <= maxDistanceLeft; shift++) {
                switch (shifts[multDescriptor][shift]) {
                    case 0:
                        continue; //nothing to add
                    case 1:
                        //just add
                        for (int j = shift; j <= maxDistanceLeft; j++){
                            currentContains[j] -= allWaysCache(multDescriptor)[j - shift]; //might need to PIE the signs here
                        }
                        break;
                    default:
                        //add and multiply
                        for (int j = shift; j <= maxDistanceLeft; j++){
                            currentContains[j] -= shifts[multDescriptor][shift]* allWaysCache(multDescriptor)[j - shift]; //might need to PIE the signs here
                        }
                }
            }
        }

        //now for three overcounting - should be all unique now
        //this is just shifted of the 0 partition - distFrom at 0 is 1 and the rest 0 so I can just add where the shifts are
        for (int j = 0; j <= maxDistanceLeft; j++){
            currentContains[j] = all[j] - (currentContains[j] + shifts[2^fromMaxMult][j]);
        }
        return currentContains;
    }

    //it would be cool to do this in a loop instead of recursive function shenanigans
    //also it makes a lot of sense to use an array of bases instead of threadvariables now
    static int[][] generateShifts(int maxDistanceLeft, ThreadVariables t){
        return null;
    }

}
