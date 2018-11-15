import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

public class Testing2 {

    static int MAX = 300;
    static int cutoff = 4;
    static int numbersComputed;
    static ArrayList<LinkedList<JobObject>> jobs;
    public static void main(String[] args){
        Testing.MAX = MAX;
        Testing.cutoff = cutoff;
        String[] passArgs = new String[] {Integer.toString(MAX), Integer.toString(cutoff)};
        Calculate2.main(passArgs);
        Testing.table = Calculate2.table;
        Testing.maxNumTerms = Calculate2.computeInverse(MAX, cutoff);
        Testing.tree = new long[Testing.maxNumTerms + 1][][];
        for (int i = 1; i <= Testing.maxNumTerms; i++){
            Testing.makeTreeFromTable(i);
        }
        //table = null;
        long startTime = System.nanoTime();
        calculatePartitions2();
        long endTime = System.nanoTime();
        for (int i = 0; i < Testing.finalCounts.length; i++){
            System.out.printf("%d: %d\n", i, Testing.finalCounts[i]);
        }
        System.out.println("Dist to time: " + (endTime - startTime) + "ns, " + ((endTime - startTime)/1000000000) + "s");

    }

    static void calculatePartitions2(){
        jobs = new ArrayList<>(Testing.maxNumTerms + 1);
        for (int i = 0; i <= Testing.maxNumTerms; i++){
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
        for (int i = 1; i <= Testing.maxNumTerms + 1; i++) {
            int numJobs = Calculate2.computeValue(1, MAX, i - 1).intValue();
            numbersComputed = 0;
            sortSection(i);
            computationSection(i);
            System.out.println("At " + (i - 1) + " computed " + numbersComputed + " of " + numJobs + " jobs");
        }
    }

    static void sortSection(int numTerms){
        Collections.sort(jobs.get(numTerms - 1));
        //printFile(jobs.get(numTerms - 1));
    }

    static void printFile(LinkedList<JobObject> list){
        for (JobObject j: list) {
            System.out.println("" + j.index + j.bases + j.minSum + stringArray(j.values, j.minSum));
        }
    }

    static String stringArray(long[] array, int min){
        StringBuilder s = new StringBuilder();
        s.append("[");
        for (int i = min; i < array.length; i++){
            s.append(array[i]);
            if (i != array.length - 1){
                s.append(" ");
            } else {
                s.append("]");
            }
        }
        return s.toString();
    }

/*
    static void memorySection(int numTerms, int blockNumber, long leftBoundary, long rightBoundary){
        long[][] storedValues = new long[(int) (leftBoundary - rightBoundary)][];
        ArrayDeque<JobObject> myFile = mjobs.get(numTerms).get(blockNumber);
        while (!myFile.isEmpty()){
            JobObject j = myFile.getFirst();
            if (storedValues[(int) (j.index - leftBoundary)] == null){
                storedValues[(int) (j.index - leftBoundary)] = j.values;
            } else {
                for (int i = j.minSum; i <= MAX; i++) {
                    storedValues[(int) (j.index - leftBoundary)][i] += j.values[i];
                }
            }
        }

    }
*/

    static void computationSection(int numTerms){
        LinkedList<JobObject> myJobs = jobs.get(numTerms - 1);
        if (myJobs.isEmpty()){
            return;
        }
        JobObject j = myJobs.removeFirst();
        long[] currentSum = j.values;
        while (!myJobs.isEmpty()){
            JobObject nextJob = myJobs.removeFirst();
            if (j.index == nextJob.index){
                for (int i = nextJob.minSum; i <= MAX; i++){
                    currentSum[i] += nextJob.values[i];
                }
            } else {
                //this part could be parallel threaded if it limits before disk
                //actually all the disk readers could pass jobs to the same pool of computing threads
                computateJob(j, currentSum, numTerms);
                //
                currentSum = nextJob.values;
            }
            j = nextJob;
        }
        //this job also needs to go somewhere!!
        computateJob(j, currentSum, numTerms);
    }

    static void computateJob(JobObject j, long[] currentSum, int numTerms){
        numbersComputed++;

        Testing.computeCounts(j.minSum, currentSum, j.bases);

        int bsum = j.bases.computeBsum();
        int minBase = j.bases.decreasing[j.bases.size];
        int lastSeen = 0;
        int total = j.minSum + bsum;
        for (int b = 1; b <= MAX; b++) {
            total += cutoff;
            if (total > MAX){
                break;
            }
            //System.out.println(r.bases + " + " + b + " total: " + total);
            if (j.bases.increasing[b] == 0) {
                j.bases.addSorted(b, lastSeen);
                IndexInfo indexInfo1 = Testing.getIndex(j.bases, numTerms);
                if (indexInfo1.minSum > MAX) {
                    j.bases.deleteAtIndex(b);
                    break;
                } else {
                    long[] output = new long[MAX + 1];
                    Testing.add(output, currentSum, bsum + b, b, minBase);
                    jobs.get(numTerms).add(new JobObject(j.bases.copyThreadVariables(), output, indexInfo1.minSum, indexInfo1.index));
                }
                j.bases.deleteAtIndex(b);
            } else {
                lastSeen = b;
            }
        }
    }
}
