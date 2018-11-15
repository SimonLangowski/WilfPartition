public class ImplementingMultiplication {
    static int MAX = 10;
    static int masterCutoff = 4;
    static int[] cutoffs = {3,4};
    static int cutoffLength = 2;
    static long[] finalCounts;
    static boolean[] multiplicitiesUsed;

    public static void main(String[] args){
        trie.initTrie(cutoffLength, cutoffs, MAX);
        finalCounts = new long[MAX + 1];
        multiplicitiesUsed = new boolean[MAX + 1];
        long startTime = System.currentTimeMillis();
        System.out.println("Distance from test\n");
        long[] dFrom = upperRecursionWrapper(MAX, new ThreadVariables(MAX + 1), new ThreadVariables(MAX + 1));
        for (int i = 0; i < dFrom.length; i++){
            System.out.printf("%d: %d\n ", i, dFrom[i]);
        }
        long endTime = System.currentTimeMillis();
        System.out.printf("\n %d ms\n", endTime - startTime);

        System.out.println("Distance To test\n");
        //this goes really fast because everything is already cached lol
        trie.initTrie(cutoffLength, cutoffs, MAX); //clear cache
        long startTime2 = System.currentTimeMillis();
        cycleList();
        for (int i = 0; i < finalCounts.length; i++){
            System.out.printf("%d: %d\n ", i, finalCounts[i]);
        }
        long endTime2 = System.currentTimeMillis();
        System.out.printf("\n %d ms\n", endTime2 - startTime2);
    }

    static void combineFunction(long[] destinationCounts, long[] inputCounts, int maxDistanceLeft, int currentDistanceLeft){
        int distanceTraveled = maxDistanceLeft - currentDistanceLeft;
        for (int j = 0; j <= Math.min(currentDistanceLeft, inputCounts.length - 1); j++){
            destinationCounts[j + distanceTraveled] += inputCounts[j];
        }
    }

    static void cycleList(){
        ThreadVariables t = new ThreadVariables(MAX + 1);
        ThreadVariables z = new ThreadVariables(MAX  + 1);
        for (int i = 0; i <= MAX; i++){
            cycleListDown(t, z, i, 0, 0);
        }
    }
    //0, 1,  2,  1,2   3,  1,3   2,3  1,2,3  4,  1,4  2,4  1,2,4...
    static int cycleListDown(ThreadVariables t, ThreadVariables z, int i, int currentMinSum, int numTerms){
        if (i == 0){
            createJob(t,z, currentMinSum);
            return 1;
        }
        //check validity
        int newSum = currentMinSum + (masterCutoff + numTerms) * i;
        if (newSum > MAX){
            return 0;
        }
        //add i
        int leftSide;
        int rightSide;
        for (leftSide = i - 1; leftSide > 0; leftSide--){
            if(t.increasing[leftSide] != 0){
                break;
            }
        }
        rightSide = t.increasing[leftSide];
        t.increasing[i] = t.increasing[leftSide];
        t.decreasing[i] = t.decreasing[rightSide];
        t.increasing[leftSide] = i;
        t.decreasing[rightSide] = i;


        for (int j = 0; j < i; j++){
            if(cycleListDown(t, z, j, newSum, numTerms + 1) == 0){
                break;
            }
        }

        //remove i
        t.increasing[leftSide] = rightSide;
        t.decreasing[rightSide] = leftSide;
        t.increasing[i] = 0;
        t.decreasing[i] = 0;

        return 1;
    }

    static void createJob(ThreadVariables t, ThreadVariables z, int minSum){
        long[] dFrom = middleRecursionWrapper(MAX - minSum, cutoffLength - 2, t, z);
        long[] dTo = distanceTo(t);
        long[] counts = combine(dTo, dFrom, minSum);
        for (int i = 0; i < counts.length; i++){
            finalCounts[i] += counts[i];
        }
    }

    //since we will have already checked every base as part of the recursion, we use the last element seen in the list to determine where to do the insert
    static void addSorted(ThreadVariables t, int index, int lastFull) {
        t.increasing[index] = t.increasing[lastFull];
        t.decreasing[t.increasing[lastFull]] = index;
        t.decreasing[index] = lastFull;
        t.increasing[lastFull] = index;
    }

    static void deleteAtIndex(ThreadVariables t, int index) {
        t.decreasing[t.increasing[index]] = t.decreasing[index];
        t.increasing[t.decreasing[index]] = t.increasing[index];
        t.increasing[index] = 0;
        t.decreasing[index] = 0;
    }

//return all ways to make a distance with only (And exactly) specified bases.  Default is 0.
//I think we have to specify that the bases given have to be used - or else overcounting
//ways to make each distance with specific bases and multiplicities above master cutoff

    static long[] distanceTo(ThreadVariables t) {
        long[] array = new long[MAX + 1];
        boolean[] multiplicitiesUsed = new boolean[MAX + 1];
        ; //only actually needs to be(MAX/masterCutoff - masterCutoff + 1 + 1)
        distanceToInternal(array, t, t.increasing[0], 0, multiplicitiesUsed);
        if (t.increasing[0] == t.size) {//check for no bases above cutoff case - need to return 1 way at distance zero -> will just count distanceFrom then
            array[0] = 1;
        }
        return array;
    }

    //if this actually has to count by 1 beyond the size of unsigned long, it will take forever
//assuming I will make this more efficient in the future
//can't reuse multiplicities lol
    static void distanceToInternal(long[] counts, ThreadVariables t, int base, int currentSum, boolean[] multiplicities) {
        if (base == t.size) {
            counts[currentSum] =  counts[currentSum] + 1;
            return;
        }
        currentSum += base * masterCutoff; //add minimum number of times
        for (int m = masterCutoff; currentSum <= MAX; currentSum += base, m++) {
            if (multiplicities[m] == false) {
                multiplicities[m] = true;
                distanceToInternal(counts, t, t.increasing[base], currentSum, multiplicities);
                multiplicities[m] = false;
            }
        }
    }

//the ways to make a number are the product of the ways to make a distanceTo using only specific bases and multiplicities above/including the masterCutoff time the ways to make the remaining distance with multiplicities below the cutoff and not using the bases already used (for all possible divisions of distance);

    static long[] combine(long[] dTo, long[] dFrom, int minSum) {
        long[] counts = new long[MAX + 1];
        for (int i = 0; i <= MAX; i++) {
            for (int j = minSum; j <= i; j++) {
                counts[i] = counts[i] + dTo[j] * dFrom[i - j];
            }
        }
        return counts;
    }


/**********Recursion section*********/
//upperRecursion considers only those multiplicities above and including the highest cutoff
//middleRecurstionWrapper is added to count the possibilities where no multiplicities counted in upper recursion are included

    static long[] upperRecursionWrapper(int maxDistanceLeft, ThreadVariables t, ThreadVariables z) {
        long[] a = middleRecursionWrapper(maxDistanceLeft, cutoffLength - 2, t,z);
        long[] b = upperRecursion(maxDistanceLeft, cutoffs[cutoffLength - 1], t, z,0,0,0);
        for (int j = 0; j <= maxDistanceLeft; j++) {
            b[j] = a[j] + b[j];
        }
        return b;
    }

//first computes minimum base left * my multiplicty.  This possibility will apply to all max - base*min, and we need to add the ways to make at base*min less than each number
//then we do base * mult+1, which applies to all max-base*mult+1, and we need to add all the ways at base*(min+1) less than each number
//thus the value to be offset is the difference between max distance and current distance left
//we can offset the array pointer, or perform a bunch of additions (alternatively subtractions) - for (i = offset; i < max) add(i, i, i-offset) <- this is the most clear, but not necessarily the fastests

//upperRecursion considers only those multiplicities above and including the highest cutoff, up to distanceLeft, which will obtain MAX at MAX, but is unnescessary before then
//I'm assuming you're going to read this one first if you read any at all.


    static long[] upperRecursion(int maxDistanceLeft, int myMultiplicity, ThreadVariables t, ThreadVariables z, int numTerms, int bsum, int lastAdded) {
        long[] counts = new long[maxDistanceLeft + 1];
        int lastSeen = 0; //stores the last invalid base, used by addSorted to determine where to add
        int baseDistanceLeft = maxDistanceLeft - myMultiplicity; //using subtraction instead of multiplication, because the bases increase consecutively, and is much much faster
        //we're counting down the distance to 0, which means we have found a partition
        for (int b = 1; b <= maxDistanceLeft; b++, baseDistanceLeft -= myMultiplicity) {
            if (baseDistanceLeft < 0) { //if we're below zero, no more bases will work, since this is tried with the minimum multiplicity of this cutoff range
                break;
            }
            if (t.increasing[b] == 0) { //this checks if b has not already been used as a base
                addSorted(t, b, lastSeen); //add b to bases
                addSorted(z, b, lastAdded);
                int newDistance = maxDistanceLeft - bsum - b * cutoffs[cutoffLength - 1];
                if (newDistance > 0) {
                    add(counts, upperRecursion(newDistance, myMultiplicity + 1, t, z, numTerms + 1, bsum + b, b));
                }
                trie node = trie.get(t);
                long[] cacheValues = null;
                if (node != null) {
                    cacheValues = node.values[cutoffLength - 1];
                }
                //by doing the base first, we avoid excess cache lookups

                    if (cacheValues == null) {
                        if (node != null) {
                            int maxSize = node.myBaseMaxDistance - (node.myBSum) * (cutoffs[cutoffLength - 1]);
                            long[] values = middleRecursionWrapper(maxSize, cutoffLength - 2, t, z);//ask for maxSize to be created
                            upperRecursionAdd(maxDistanceLeft,0,counts,values,z,t.increasing[0]);
                        } else {
                            //no more room in trie right now

                                long[] values = middleRecursionWrapper(maxDistanceLeft, cutoffLength - 2, t, z); //only ask for what we need
                                upperRecursionAdd(maxDistanceLeft,0,counts,values,z,t.increasing[0]);

                        }
                    } else {
                        //values already computed
                        upperRecursionAdd(maxDistanceLeft, 0, counts, cacheValues, z, t.increasing[0]);
                    }

                deleteAtIndex(t, b); //remove the base
            } else {
                lastSeen = b; //update the last seen base
            }
        }
        return counts;
    }

    static boolean upperRecursionAdd(int maxDistanceLeft, int currentOffset, long[] destination, long[] input, ThreadVariables t, int index){
        if (index == t.size){
            combineFunction(destination, input, maxDistanceLeft, maxDistanceLeft - currentOffset);
            return true;
        }
        currentOffset = currentOffset + cutoffs[cutoffLength - 1] * index;
        if (currentOffset > maxDistanceLeft){
            return false;
        }
        for (int m = cutoffs[cutoffLength - 1]; (m <= maxDistanceLeft) && (currentOffset <= maxDistanceLeft); m++, currentOffset += index){
            if (!multiplicitiesUsed[m]) {
                multiplicitiesUsed[m] = true;
                if (!upperRecursionAdd(maxDistanceLeft, currentOffset, destination, input, t, t.increasing[index])){
                    multiplicitiesUsed[m] = false;
                    return true;
                }
                multiplicitiesUsed[m] = false;
            }
        }
        return true;
    }

//middle recursion wrapper decides if there is another cutoff, and does another middle recursion
//else it calls q2 or lowerRecursion to finish the multiplicities below the lowest cutoff.  (q2 considers only 2 and 1 and a cutoff[0] of 3 would include 3 in the middle recursion)
//once again we add skipping the region between inclusive[ cutoffs[cutoff] , cutoffs[cutoff + 1] )exclusive
//by adding the wrapper of the cutoff value one less
    static long[] middleRecursionWrapper(int maxDistanceLeft, int cutoff, ThreadVariables t, ThreadVariables z) {
        if (cutoff >= 0) {
            long[] a = middleRecursionWrapper(maxDistanceLeft, cutoff - 1, t, z);
            long[] b = middleRecursion(maxDistanceLeft, cutoffs[cutoff], cutoff, t, z,  0,0, 0);
            for (int j = 0; j <= maxDistanceLeft; j++) {
                b[j] = a[j] + b[j];
            }
            return b;
        } else if (cutoffs[0] > 3) {
            long[] a = lowerRecursion(maxDistanceLeft, 3, t, z, 0, 0, 0);
            long[] b = q2All(maxDistanceLeft, t);
            for (int j = 0; j <= maxDistanceLeft; j++) {
                b[j] = a[j] + b[j];
            }
            return b;
        } else {
            return q2All(maxDistanceLeft, t);
        }
    }

//have to shift counts over every time
//first time adds to all of those with base 1 time
//then those with base 2 times - higher distance left need to be added more, same as shifting starting location so size is max
//shift by base multiplicity times in current setup
//then reset for new base

//this is the meat of the algorithm, but is virtually the same as upper recursion in explanation

    //select (multiple) bases in increasing order - iteration probably like cycle down?

    //check my cache, or generate ways to make without bases with multiplicities below my cutoff

    //select corresponding multiplicities and permute as appropriate - can have boolean array of multiplicities used - within this cutoff, alloca or static size?

    //add as appropriate - add the ways to make a number with the specified bases/multiplicities in this cutoff range, to the remaining distance found from cache


    static long[] middleRecursion(int maxDistanceLeft, int myMultiplicity, int cutoffNumber, ThreadVariables t, ThreadVariables z, int currentNumTerms, int bsum, int lastAdded) {
        int lastSeen = 0;
        int baseDistanceLeft = maxDistanceLeft - myMultiplicity;
        long[] counts = new long[maxDistanceLeft + 1];
        for (int b = 1; b <= maxDistanceLeft; b++, baseDistanceLeft -= myMultiplicity) {
            if (baseDistanceLeft < 0) {
                break;
            }
            if (t.increasing[b] == 0) { //maybe we can replace cacheValues when currentDistanceLeft drops below the highest current base -> and then modify the lookup function to accept a start value (or index)
                addSorted(t, b, lastSeen);
                addSorted(z,b,lastAdded);
                int newDistance = maxDistanceLeft - bsum - cutoffs[cutoffNumber] * b;
                if ((myMultiplicity  + 1 < cutoffs[cutoffNumber + 1]) && (newDistance > 0)){
                    add(counts, middleRecursion(newDistance, myMultiplicity + 1, cutoffNumber, t, z, currentNumTerms + 1, bsum + b, b));
                }
                trie node = trie.get(t);
                long[] cacheValues = null;
                if (node != null) {
                    cacheValues = node.values[cutoffNumber]; //values from wrapper (Sum of all multiplicities less than)
                }
                //we should just get all the possible more values here? //value from this cutoff range only
                if (cacheValues == null) {
                    if (node != null) {
                        int maxSize = node.myBaseMaxDistance - (node.myBSum) * cutoffs[cutoffNumber];
                        if (maxSize < 0){
                            System.out.println("error");
                        }
                        //have room to store in trie
                        long[] values = middleRecursionWrapper(maxSize, cutoffNumber - 1, t, z); //ask for maxSize to be created
                        trie.put(node, values, cutoffNumber);
                        middleRecursionAdd(maxDistanceLeft,0,counts, values,z,t.increasing[0],cutoffNumber);
                    } else {
                        long[] values = middleRecursionWrapper(maxDistanceLeft, cutoffNumber - 1, t, z);
                        middleRecursionAdd(maxDistanceLeft, 0, counts, values, z,t.increasing[0],cutoffNumber);
                    }
                } else {
                    //values already computed
                    middleRecursionAdd(maxDistanceLeft, 0, counts, cacheValues, z,t.increasing[0],cutoffNumber);
                }
                deleteAtIndex(t, b);
                deleteAtIndex(z,b);
            } else {
                lastSeen = b;
            }
        }
        return counts;
    }

    static int middleRecursionAdd(int maxDistanceLeft, int currentOffset, long[] destination, long[] input, ThreadVariables t, int index, int cutoffNumber){
        if (index == t.size){
            combineFunction(destination, input, maxDistanceLeft, maxDistanceLeft - currentOffset);
            return 1;
        }
        currentOffset += cutoffs[cutoffNumber] * index;
        if (currentOffset > maxDistanceLeft){
            return 0;
        }
        for (int m = cutoffs[cutoffNumber]; (m < cutoffs[cutoffNumber + 1]) && (currentOffset <= maxDistanceLeft); m++, currentOffset += index){
            if (!multiplicitiesUsed[m]) {
                multiplicitiesUsed[m] = true;
                if (middleRecursionAdd(maxDistanceLeft, currentOffset, destination, input, t, t.increasing[index], cutoffNumber) == 0){
                    multiplicitiesUsed[m] = false;
                    break;
                }
                multiplicitiesUsed[m] = false;
            }
        }
        return 1;
    }

//lower recursion does no caching, and calls q2 where the others call the recursion for the cutoff below them
    static long[] lowerRecursion(int maxDistanceLeft, int myMultiplicity, ThreadVariables t, ThreadVariables z, int numTerms, int bsum, int lastAdded) {
        long[] counts = new long[maxDistanceLeft + 1];
        int lastSeen = 0;
        int baseDistanceLeft = maxDistanceLeft - myMultiplicity;
        for (int b = 1; b <= maxDistanceLeft; b++, baseDistanceLeft -= myMultiplicity) {
            if (baseDistanceLeft < 0) {
                break;
            }
            if (t.increasing[b] == 0) {
                addSorted(t, b, lastSeen);
                addSorted(z,b,lastAdded);
                int newDistance = maxDistanceLeft - bsum - 3 * b;
                if ((myMultiplicity + 1 < cutoffs[0]) && (newDistance > 0)){
                    add( counts, lowerRecursion(newDistance, myMultiplicity + 1, t, z, numTerms + 1, bsum + b, b));
                }
                long[] values = q2All(baseDistanceLeft, t);
                lowerRecursionAdd(maxDistanceLeft, 0, counts, values, z, t.increasing[0]);
                deleteAtIndex(t, b);
                deleteAtIndex(z, b);
            } else {
                lastSeen = b;
            }
        }
        return counts;
    }

    static int lowerRecursionAdd(int maxDistanceLeft, int currentOffset, long[] destination, long[] input, ThreadVariables t, int index){
        if (index == t.size){
            combineFunction(destination, input, maxDistanceLeft, maxDistanceLeft - currentOffset);
            return 1;
        }
        currentOffset += 3 * index;
        if (currentOffset > maxDistanceLeft){
            return 0;
        }
        for (int m = 3; (m < cutoffs[0]) && (currentOffset <= maxDistanceLeft); m++, currentOffset += index){
            if (!multiplicitiesUsed[m]) {
                multiplicitiesUsed[m] = true;
                if (lowerRecursionAdd(maxDistanceLeft, currentOffset, destination, input, t, t.increasing[index]) == 0){
                    multiplicitiesUsed[m] = false;
                    break;
                }
                multiplicitiesUsed[m] = false;
            }
        }
        return 1;
    }

    static long[] q2All(int maxDistanceLeft, ThreadVariables t) {
        long[] counts = new long[maxDistanceLeft + 1];
        for (int i = 0; i <= maxDistanceLeft; i++) {
           counts[i] = q2(i, t); //this can be done much more efficiently
        }
        return counts;
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

    static void add(long[] a, long[] b){
        for (int i = 0; i < Math.min(a.length, b.length); i++){
            a[i] += b[i];
        }
    }

}




