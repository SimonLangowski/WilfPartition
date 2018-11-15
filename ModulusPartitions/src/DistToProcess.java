public class DistToProcess {

    int numTerms;
    int minIndex;
    int maxIndex;
    VirtualFileObject sourceFile;
    VirtualFile destinationPartial;
    VirtualFile destinationComplete;

    public DistToProcess(int numTerms, int minIndex, int maxIndex){
        this.numTerms = numTerms;
        this.minIndex = minIndex;
        this.maxIndex = maxIndex;
        sourceFile = new VirtualFileObject(Script.distToPartials.get(numTerms - 1));
        destinationComplete = Script.distToValues.get(numTerms - 1);
    }

    //just sum from source to complete
    public void runSubProcess(){
        sourceFile.goToIndex(minIndex);
        VirtualFileEntry j = sourceFile.getNext();
        int[] currentSum = j.values;
        int currentIndex = minIndex + 1;
        while (currentIndex < maxIndex){
            VirtualFileEntry nextJob = sourceFile.getNext();
            if (j.index == nextJob.index){
                for (int i = nextJob.minSum; i <= Script.MAX; i++){
                    currentSum[i] += nextJob.values[i];
                }
            } else {
                destinationComplete.entries.add(new VirtualFileEntry(j.index,j.bases,j.minSum,currentSum));
                currentSum = nextJob.values;
                currentIndex++;
            }
            j = nextJob;
        }
        //this job also needs to go somewhere!!
        destinationComplete.entries.add(new VirtualFileEntry(j.index,j.bases,j.minSum,currentSum));
    }

    public void runProcess(){
        destinationPartial = Script.distToPartials.get(numTerms);
        sourceFile.goToIndex(minIndex);
        VirtualFileEntry j = sourceFile.getNext();
        int[] currentSum = j.values;
        int currentIndex = minIndex + 1;
        while (currentIndex < maxIndex){
            VirtualFileEntry nextJob = sourceFile.getNext();
            if (j.index == nextJob.index){
                for (int i = nextJob.minSum; i <= Script.MAX; i++){
                    currentSum[i] += nextJob.values[i];
                }
            } else {
                destinationComplete.entries.add(new VirtualFileEntry(j.index,j.bases,j.minSum,currentSum));
                computateJob(j, currentSum, numTerms);
                currentSum = nextJob.values;
                currentIndex++;
            }
            j = nextJob;
        }
        //this job also needs to go somewhere!!
        destinationComplete.entries.add(new VirtualFileEntry(j.index,j.bases,j.minSum,currentSum));
        computateJob(j, currentSum, numTerms);
    }

    void computateJob(VirtualFileEntry j, int[] currentSum, int numTerms){
        ThreadVariables t = new ThreadVariables(j.bases);
        int bsum = t.computeBsum();
        int lastSeen = 0;
        int currenDist = j.minSum + bsum * Script.modulo;
        int stop = Script.MAX - currenDist;
        for (int b = 1; b <= stop; b++) {
            //System.out.println(r.bases + " + " + b + " total: " + total);
            currenDist++; //since residue 1
            if (t.increasing[b] == 0) {
                t.addSorted(b, lastSeen);
                IndexInfo indexInfo1 = Script.getIndex(t, numTerms, 1);
                if (indexInfo1.minSum > Script.MAX) {
                    t.deleteAtIndex(b);
                    break;
                } else {
                    int[] output = new int[Script.MAX + 1];
                    add(output, currentSum, bsum, currenDist, b); //this should be the minsum for this particular permutation
                    destinationPartial.entries.add(new VirtualFileEntry((int) indexInfo1.index, t.getBasesAsArrayList(), indexInfo1.minSum, output)); //this isn't the minsum for this permutation, rather the minsum for the bases in general
                }
                t.deleteAtIndex(b);
            } else {
                lastSeen = b;
            }
        }
    }

    static void add(int[] destination, int[] input, int oldbsum, int startLocation, int baseAdded){
        //shift baseAdded once
        //cummulative shift newbsum many times
        int shift = Script.modulo * oldbsum + baseAdded;
        int newBsum = oldbsum + baseAdded;
        long[] cummulative = new long[newBsum];
        int initial = startLocation % newBsum;
        for (int i = startLocation; i <= Script.MAX; i+= Script.modulo){
            cummulative[initial] += input[i - shift]; //shift baseAdded once
            destination[i] += cummulative[initial]; //cummulative shift newBsum many times
            if(++initial == newBsum){
                initial = 0;
            }
        }
    }
}
