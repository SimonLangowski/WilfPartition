import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

public class EvenOddPartitions {

    //number of jobs = number of partitions with all even = all unique base combos of MAX / 2;
    static int MAX = 10;
    static int residueOneMaxNumTerms;
    static int modulo = 2;
    //static int cutoff = 1;
    static ArrayList<LinkedList<JobObject>> jobs;
    static JobObject[][] residueOne;
    static long[][][] trieSums; //a trie sum is the sum of the children of this node in the lexicographic trie
    static long[] finalCounts = new long[MAX + 1];
    static long[][][] table;
    static long[][][] tree;

    //any bases bigger than halfMax must necessarily be paired with an odd number ex (51)(1)

    public static void main(String[] args){
        constructTable();
        residueOneMaxNumTerms = table.length - 1;
        tree = new long[residueOneMaxNumTerms + 1][][];
        for (int i = 1; i <= residueOneMaxNumTerms; i++){
            makeTreeFromTable(i);
        }
        //table = null;*/
        calculatePartitions3();
    }

    //I guess I could also just do exactly what I have now with distTo (cutoff 1) and then multiply by two?
    // this would help with the reusability of files
    public static void calculatePartitions3(){
        jobs = new ArrayList<>(residueOneMaxNumTerms + 1);
        for (int i = 0; i <= residueOneMaxNumTerms; i++){
            jobs.add(new LinkedList<JobObject>());
        }
        JobObject zero;
        ThreadVariables empty = new ThreadVariables(MAX + 1);
        long[] zeroValues = new long[MAX + 1];
        zeroValues[0] = 1;
        int zeroMinSum = 0;
        zero = new JobObject(empty, zeroValues, zeroMinSum, 0);
        //Testing.computeCounts(zeroMinSum, zeroValues, empty);
        jobs.get(0).add(zero);
        residueOne = new JobObject[residueOneMaxNumTerms + 1][];
        trieSums = new long[residueOneMaxNumTerms + 1][][];
        residueOne[0] = new JobObject[1];
        residueOne[0][0] = zero;
        trieSums[0] = new long[1][];
        for (int i = 1; i <= residueOneMaxNumTerms; i++){
            int numTerms = (int) computeValue(1, MAX, i);
            residueOne[i] = new JobObject[numTerms];
            trieSums[i] = new long[numTerms][];
        }
        for (int i = 1; i <= residueOneMaxNumTerms + 1; i++) {
            sortSection(i);
            //combineSection(i); //combine jobs into one array for the indexes
            //merged with next section
            computationSection(i); //same as Testing2 without calling computeCounts, things should not be removed from queues;
            //things are removed from queues, but the combined values are output as indexes in the residueOne array
        }
        for (int i = residueOneMaxNumTerms - 1; i >= 0; i--) {

        }
        //contains[0][0][0] = 1;
        for (int i = 0; i <= residueOneMaxNumTerms; i++){
            combineThings(i); //use PIE to compute the distFrom at each index (using contains), and multiplies by the distTo (the residueOne array) - need to know total contains (node 0) before PIE
            //an efficient iteration here can reduce the PIE usage
        }
        for (int i = 0; i < finalCounts.length; i++){
            System.out.println(i + ": " + finalCounts[i]);
        }

    }

    static void combineThings(int numTerms){
        long[] all = trieSums[0][0];
        ThreadVariables temp = new ThreadVariables(MAX + 1);
        for (int i = 0; i < residueOne[numTerms].length; i++){
            long[] distTo = residueOne[numTerms][i].values;
            long[] distFrom = new long[MAX + 1];
            for (int j = 0; j < distFrom.length; j++){
                distFrom[j] = all[j];
            }
            long[] contains = getContainsValue(numTerms, i); //need to add elements from trieSum together -> I'd like to do this efficiently base on the fact I'm incrementing my indexes as I go
            for (int j = 0; j < distFrom.length; j++){
                distFrom[j] -= contains[j];
            }
            //PIEiterate(0, temp, residueOne[numTerms][i].bases, true, distFrom, 0);
            for (int k = 0; k <= MAX; k++){
                for (int j = 0; j <= k; j++){
                    finalCounts[k] += distTo[j] * distFrom[k - j];
                }
            }
        }
    }

    static long[] getAtResidue(int numTerms, long index, int desiredResidue){
        JobObject data = residueOne[numTerms][(int) index];
        return getShiftedToResidue(data.values, data.bases.computeBsum(), desiredResidue);
    }

    static long[] getShiftedToResidue(long[] shiftMe, int bsum, int desiredResidue){
        if (desiredResidue == 0){
            desiredResidue += modulo;
        }
        int shiftAmount = bsum * (desiredResidue - 1);
        long[] result = new long[MAX + 1];
        for (int i = shiftAmount; i <= MAX; i++){
            result[i] = shiftMe[i - shiftAmount];
        }
        return result;
    }


    //basically you should add minBase (one more than the largest base) and then sum the sequential indexes
    //the index you stop at should be the first index for the next index in my numTerms -> so just sequentially reading the file below me (one more numTerms)
    //and adding (also shifting to make values for my modulo (so I need to know the bsums as I go, but they just increment too))
    //what's the easiest way to know when to start the next number?  To use the bases as I read them in or to use the construction of the index tree?



    //everything begins independent (no base overlap)
    //then some removed for conflicts as you go down the tree?
    //?
    //If I instead use a contains method (which should still be valid)
    //Don't I just want the sum of all my children XD

    static void computeBaseContainsValues(int numTerms){
        for (int i = 0; i < residueOne[numTerms].length; i++){
            int bsum = residueOne[numTerms][i].bases.computeBsum();
            trieSums[numTerms][i] = getShiftedToResidue(residueOne[numTerms][i].values, bsum, 0);
        }
    }

    static long[] getContainsValue(ThreadVariables t, int numTerms){
        return getContainsValue(numTerms, (int)getIndex(t, numTerms).index);
    }

    static long[] getContainsValue(int numTerms, long index){
        long[] myBaseArray = getAtResidue(numTerms, index, 0);
        return null;
    }
/*
    static void constructContainsHelper(int numTerms){
        for (int i = 0; i < residueOne[numTerms].length; i++){
            computateContainsHelper(residueOne[numTerms][i], numTerms);
        }
    }

    static void computeContainsHelper(JobObject j, int numTerms){
        JobObject me = residueOne[numTerms][(int)index];
        //be careful if you multithread if you change the bases directly
        int minBase = me.bases.decreasing[me.bases.size] + 1;
        int distLeft = MAX - me.minSum;
        int multiplicity = modulo * (numTerms - 1) + 1;
        int boundary = distLeft / multiplicity;
        for (int k = minBase; k <= boundary; k++){

            val += possibilities;
        }
    }
*/
    /*static void computateContains(JobObject j, int numTerms){
        int bsum = j.bases.computeBsum();
        int lastSeen = 0;
        int currenDist = j.minSum + bsum;

        //might end up being oldIndex newIndex, and the minSum and addition checks should be based on the newIndex
        contains[numTerms][(int) j.index] = getShiftedToResidue(j.values, bsum, 0);
        long[] destination = contains[numTerms][(int) j.index];
        for (int b = 1; b <= MAX - currenDist; b++) {
            //System.out.println(r.bases + " + " + b + " total: " + total);
            if (j.bases.increasing[b] == 0) {
                j.bases.addSorted(b, lastSeen);
                IndexInfo indexInfo1 = getIndex(j.bases, numTerms + 1);
                if (indexInfo1.minSum > MAX) {
                    j.bases.deleteAtIndex(b);
                    break;
                } else {
                    long[] source = getContainsValue(numTerms + 1, indexInfo1.index);
                    for (int i = 0; i <= MAX; i++){
                        destination[i] += source[i];
                    }
                }
                j.bases.deleteAtIndex(b);
            } else {
                lastSeen = b;
            }
        }
    }

    static void PIEiterate(int lastBase, ThreadVariables currentBases, ThreadVariables totalBases, boolean subtract, long[] totals, int currentNumTerms){
        int myCurrentBase = totalBases.increasing[lastBase];
        while (myCurrentBase != totalBases.size){
            currentBases.addSorted(myCurrentBase, lastBase);
            long[] vals = getContainsValue(currentBases, currentNumTerms + 1);
            if (subtract){
                for (int i = 0; i <= MAX; i++){
                    totals[i] -= vals[i];
                }
            } else {
                for (int i = 0; i <= MAX; i++){
                    totals[i] += vals[i];
                }
            }
            PIEiterate(myCurrentBase, currentBases, totalBases, !subtract, totals, currentNumTerms + 1);
            currentBases.deleteAtIndex(myCurrentBase);
            lastBase = myCurrentBase;
            myCurrentBase = totalBases.increasing[myCurrentBase];
        }
    }*/

    static void sortSection(int numTerms){
        Collections.sort(jobs.get(numTerms - 1));
        //printFile(jobs.get(numTerms - 1));
    }

    static void computationSection(int numTerms){
        LinkedList<JobObject> myJobs = jobs.get(numTerms - 1);
        if (myJobs.isEmpty()){
            return;
        }
        JobObject j = myJobs.removeFirst();
        long[] currentSum = j.values;
        int bsum = j.bases.computeBsum();
        while (!myJobs.isEmpty()){
            JobObject nextJob = myJobs.removeFirst();
            if (j.index == nextJob.index){
                for (int i = nextJob.minSum; i <= (MAX + bsum)/modulo; i++){
                    currentSum[i] += nextJob.values[i];
                }
            } else {
                //this part could be parallel threaded if it limits before disk
                //actually all the disk readers could pass jobs to the same pool of computing threads
                residueOne[numTerms - 1][(int) j.index] = new JobObject(j.bases, currentSum, j.minSum, j.index); //this would be printing
                computateJob(j, currentSum, numTerms);
                //
                currentSum = nextJob.values;
            }
            j = nextJob;
            bsum = j.bases.computeBsum();
        }
        //this job also needs to go somewhere!!
        residueOne[numTerms - 1][(int) j.index] = new JobObject(j.bases, currentSum, j.minSum, j.index); //this would be printing
        computateJob(j, currentSum, numTerms);
    }

    static void computateJob(JobObject j, long[] currentSum, int numTerms){
        int bsum = j.bases.computeBsum();
        int lastSeen = 0;
        int currenDist = j.minSum + bsum;
        for (int b = 1; b <= MAX - currenDist; b++) {
            //System.out.println(r.bases + " + " + b + " total: " + total);
            if (j.bases.increasing[b] == 0) {
                j.bases.addSorted(b, lastSeen);
                IndexInfo indexInfo1 = getIndex(j.bases, numTerms);
                if (indexInfo1.minSum > MAX) {
                    j.bases.deleteAtIndex(b);
                    break;
                } else {
                    long[] output = new long[MAX + 1];
                    add(output, currentSum, bsum, indexInfo1.minSum, b);
                    jobs.get(numTerms).add(new JobObject(j.bases.copyThreadVariables(), output, indexInfo1.minSum, indexInfo1.index));
                }
                j.bases.deleteAtIndex(b);
            } else {
                lastSeen = b;
            }
        }
    }

    static IndexInfo getIndex(ThreadVariables t, int numTerms){
        System.out.println(t);
        int startingMultiplicity = modulo * (numTerms - 1) + 1;
        int currentDistLeft = MAX;
        long spacesSkipped = 0;
        int minBase = 1;
        int currentBase = t.increasing[0];
        while(currentBase != t.size){
            for (int j = minBase; j < currentBase; j++){
                spacesSkipped += computeValue(j + 1, currentDistLeft - startingMultiplicity * j, numTerms - 1);
            }
            currentDistLeft -= currentBase * startingMultiplicity;
            startingMultiplicity -= modulo;
            numTerms--;
            minBase = currentBase + 1;
            currentBase = t.increasing[currentBase];
        }
        return new IndexInfo(spacesSkipped, MAX - currentDistLeft);
    }

    static void add(long[] destination, long[] input, int oldbsum, int newminsum, int baseAdded){
        //shift baseAdded once
        //cummulative shift newbsum many times
        int shift = modulo * oldbsum + baseAdded;
        if(shift > newminsum){
            return; //no need to add anything
        }
        int newBsum = oldbsum + baseAdded;
        long[] cummulative = new long[newBsum];
        //int startLocation = oldminsum + shift; //shift + first occurence of 1 in input array  (shift + minBase?? - minBase must have the same multiplicity mod bsum as old/new minSum)
        int startLocation = newminsum; //new minsum should be oldminsum + shift??
        int initial = startLocation % newBsum;
        for (int i = startLocation; i <= MAX; i+= modulo){
            cummulative[initial] += input[i - shift]; //shift baseAdded once
            destination[i] += cummulative[initial]; //cummulative shift newBsum many times
            if(++initial == newBsum){
                initial = 0;
            }
        }
    }

    static void constructTable(){
        int maxNumTerms = computeInverse(MAX);
        table = new long[maxNumTerms + 1][MAX + 1][MAX + 1];
        for (int i = 0; i <= maxNumTerms; i++){
            System.out.println(i + ": " + computeValue(1, MAX, i));
        }
    }

    static long computeValue(int minBase, int maxDistLeft, int numTerms){
        if (table[numTerms][minBase][maxDistLeft] != 0){
            //System.out.println("Returned " + table[numTerms][minBase][maxDistLeft] + " for " + numTerms + " terms, minBase " + minBase + ", distLeft " + maxDistLeft);
            return table[numTerms][minBase][maxDistLeft];
        }
        long val = 0;
        if (numTerms == 0){
            return 1;
        } else if (numTerms == 1){
            val = maxDistLeft - minBase + 1;
            if (val < 0){
                val = 0;
            }
        } else {
            int multiplicity = modulo * (numTerms - 1) + 1;
            int boundary = maxDistLeft / multiplicity;
            for (int k = minBase; k <= boundary; k++){
                long possibilities = computeValue(k + 1, maxDistLeft - (multiplicity * k), numTerms - 1);
                if (possibilities == 0){
                    break;
                }
                val += possibilities;
            }
        }
        table[numTerms][minBase][maxDistLeft] = val;
        //System.out.println("Returned " + table[numTerms][minBase][maxDistLeft] + " for " + numTerms + " terms, minBase " + minBase + ", distLeft " + maxDistLeft);
        return val;
    }

    static int computeInverse(int max){
        int numTerms = 0;
        int result = getMinVal(numTerms);
        while (result < max){
            numTerms++;
            result = getMinVal(numTerms);
        }
        return numTerms - 1;
    }

    static int getMinVal(int numTerms){
        int val = 0;
        int startingMultiplicity = modulo * (numTerms - 1) + 1;
        for (int i = 0; i < numTerms; i++){
            val += (i + 1) * (startingMultiplicity);
            startingMultiplicity -= modulo;
        }
        return val;
    }

    static void makeTreeFromTable(int numTerms){
        long[][] tempMinBaseArray = new long[MAX + 1][];
        boolean allZeroRows = false;
        for (int j = 1; j <= MAX; j++){
            boolean allZeroColumns = true;
            for (int k = 1; k <= MAX; k++){
                if ((allZeroColumns) && (table[numTerms][j][k] != 0)){
                    allZeroColumns = false;
                    allZeroRows = true;
                    long offset = k - 1;
                    tempMinBaseArray[j - 1] = new long[(MAX - k + 1 + 1)];
                    tempMinBaseArray[j - 1][0] = offset;
                    while (k <= MAX){
                        if (table[numTerms][j][k] != 0) {
                            tempMinBaseArray[j - 1][(int) (k - offset)] = table[numTerms][j][k];
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

}
