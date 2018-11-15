import java.util.ArrayList;
import java.util.Arrays;

public class ComputeDistFromAndCounts {

    static int toResidue = 1;
    static int fromResidue = 2;
    int numTerms;
    int minIndex;
    int maxIndex;
    VirtualFile destinationCountsFile;
    VirtualFileObject[] trieSources;
    VirtualFileObject distToSource;
    VirtualFileObject containsSource;
    VirtualFile containsDestination;
    TrieNode myTrieRoot;
    int[] masterTotals;
    int[] mycounts;
    int[] maxPath;

    //use max bases from trie to guide recursion
    //create existance check (out of bounds in get internal returns null) for add recursive - then return (no need to add more numbers or increase base further
    //The numbers needed to be read from the file will be next to each other - if could provide max by max index, then could read ahead (I guess since it's being read from file we have time to compute)


    //should the recursion do the computeCounts on the way down or the way up?
    //Am I going to do multiple numTerms as I pass through them or just only in the one layer? - Many many processes are going to remove 1, 2, etc.
    //I could make a new table that sums the current tables across all numTerms to make a ordering for all partitions of all numTerms
    //I guess the current idea is that it will allow you to read through the distTo files in order, although that means repeating some distFrom computation (which I think is much more expensive)

    //construct contains 3,4,5 from contains 3,4 and triesum 5 1,5 2,5 1,2,5
    //so there will be another file for contains
    //removes need for recursion - just has to read previous contains value from file
    //will also cache 1,6 1,7 1,8... 2,6 2,7 2,8... 1,2,6 1,2,7... so it will be efficient - since in order next will be 3,4,6 (adds 5,6) 3,4,7...

    //can cache from minBase to maxBase of children? Yes
    //but this is still less efficient caching because after  3,4,5 3,4,6 3,4,7 3,4,8... there will be 3,4,5,6 3,4,5,7 that will add 1,7 2,7 1,2,7 7 again but since they're in a different process will have to read (and compute) again
    //3,4,5,7 is related to both 3,4,5 and 3,4,7

    //maybe I just store byte offset when I run out of ram?

    //I should read a set of contains values in on defined indexes, and then compute the contains for the trie children of the read contains values
    //can compute counts for children or the parents at this step
    //contains values will have to match the distTo indexing - it might be convenient to have the parents and computeCounts be the same index/ranges

    public ComputeDistFromAndCounts(int numTerms, int maxNumTerms, int minIndex, int maxIndex){
        this.numTerms = numTerms;
        this.minIndex = minIndex;
        this.maxIndex = maxIndex;
        myTrieRoot = TrieNode.getTrieHead();
        trieSources = new VirtualFileObject[maxNumTerms + 1];
        for (int i = 0; i <= maxNumTerms; i++){
            trieSources[i] = new VirtualFileObject(Script.trieSums.get(i));
        }
        destinationCountsFile = Script.counts.get(numTerms);
        distToSource = new VirtualFileObject(Script.distToValues.get(numTerms));
        containsSource =new VirtualFileObject(Script.contains.get(numTerms));
        mycounts = new int[Script.MAX + 1];
    }

    public void runSubProcess(){
        masterTotals = trieSources[0].getNext().values;
        distToSource.goToIndex(minIndex);
        containsSource.goToIndex(minIndex);
        int currentIndex = minIndex;
        while (currentIndex < maxIndex){
            VirtualFileEntry containsEntry = containsSource.getNext();
            computeCounts(containsEntry.values); //can be done in parallel
            currentIndex++;
        }
        destinationCountsFile.entries.add(new VirtualFileEntry(minIndex, new ArrayList<>(), 0, mycounts));
    }

    public void runProcess(){
        containsDestination = Script.contains.get(numTerms + 1);
        masterTotals = trieSources[0].getNext().values;
        distToSource.goToIndex(minIndex);
        containsSource.goToIndex(minIndex);
        int currentIndex = minIndex;
        ThreadVariables t = Script.findIndex(minIndex, numTerms, 1);
        int nextBase = t.decreasing[t.size];
        t.addSorted(nextBase + 1, nextBase);
        int currentOutputIndex = (int) Script.getIndex(t, numTerms + 1, 1).index;
        ThreadVariables t2 = new ThreadVariables();
        while (currentIndex < maxIndex){
            VirtualFileEntry containsEntry = containsSource.getNext();
            computeCounts(containsEntry.values); //can be done in parallel
            t = new ThreadVariables(containsEntry.bases);
            int bsum = getBaseSum(containsEntry.bases);
            int minBase = 1;
            if (numTerms > 0) {
                minBase = containsEntry.bases.get(numTerms - 1) + 1;
            }
            int currentBaseMinSum = containsEntry.minSum + bsum * Script.modulo + minBase * toResidue;
            //I recommend multithreading for this part?, and then making sure the output is in the correct order with some sort of bounded array
            int b = minBase;
            while(currentBaseMinSum <= Script.MAX) {
                int[] containsCopy = new int[Script.MAX + 1]; //can be thread cached - or maybe not since will be stored for outputing
                for (int i = 0; i <= Script.MAX; i++){
                    containsCopy[i] = containsEntry.values[i];
                }
                t2.addSorted(b, 0);
                TrieNode node = getValue(t2, 1);
                if (node != null) {
                    for (int i = Script.MAX - node.myBaseMaxDistance; i <= Script.MAX; i++) {
                        containsCopy[i] += node.values[i];
                    }
                    addRecursiveExclusions(t2, t, b, containsCopy, 1, 0);
                }
                t2.deleteAtIndex(b);
                t.addSorted(b, minBase - 1);
                containsDestination.entries.add(new VirtualFileEntry(currentOutputIndex, t, currentBaseMinSum, containsCopy)); //min sum needed to find next maxBase
                currentOutputIndex++;
                t.deleteAtIndex(b);
                b++;
                currentBaseMinSum += toResidue;
            }
            currentIndex++;
        }
        destinationCountsFile.entries.add(new VirtualFileEntry(minIndex, new ArrayList<>(), 0, mycounts));
    }

    public static int getBaseSum(ArrayList<Integer> bases){
        int sum = 0;
        for (Integer i : bases){
            sum += i;
        }
        return sum;
    }


    //this needs some way to not try to add things that don't exist
    public void addRecursiveExclusions(ThreadVariables t, ThreadVariables exclusions, int target, int[] totals, int numBases, int lastAdded){
        int lastSeen = lastAdded;
        for (int b = lastAdded + 1; b < target; b++){
            if (exclusions.increasing[b] == 0) {
                if (t.increasing[b] == 0) {
                    t.addSorted(b, lastSeen);
                    TrieNode node = getValue(t, numBases + 1);
                    if (node != null) {
                        for (int i = Script.MAX - node.myBaseMaxDistance; i <= Script.MAX; i++) {
                            totals[i] += node.values[i];
                        }
                        addRecursiveExclusions(t, exclusions, target, totals, numBases + 1, b);
                        t.deleteAtIndex(b);
                    } else {
                        t.deleteAtIndex(b);
                        break; //no need to continue following this path
                    }
                } else {
                    lastSeen = b;
                }
            }
        }
    }

    public void computeCounts(int[] exclusionTotals){
        int[] distFroms = new int[Script.MAX + 1];
        boolean flag = false;
        for (int i = 1; i <= Script.MAX; i++){
            distFroms[i] = masterTotals[i] - exclusionTotals[i];
            if (distFroms[i] < 0){
                flag = true;
            }
        }
        VirtualFileEntry dTo = distToSource.getNext();
        int[] distTos = dTo.values;
        if (flag){
            System.out.println(dTo.bases);
            System.out.println(Arrays.toString(exclusionTotals));
            System.out.println(Arrays.toString(distFroms));
        }
        distFroms[0] = 1;
        for (int k = 0; k <= Script.MAX; k++){
            for (int j = 0; j <= k; j++){
                mycounts[k] += distTos[j] * distFroms[k - j];
            }
        }
    }

    public TrieNode getValue(ThreadVariables t, int numBases){
        int lastBase = t.decreasing[t.size];
        int lastLastBase = t.decreasing[lastBase];
        t.deleteAtIndex(lastBase);
        TrieNode locationParent;
        if (t.increasing[0] != t.size) {
            locationParent = TrieNode.getInternal(myTrieRoot, t.increasing[0], t);
        } else {
            locationParent = myTrieRoot;
        }
        if ((locationParent == null) || (lastBase > locationParent.maxBase)) {
            t.addSorted(lastBase, lastLastBase);
            return null;
        } else {
            TrieNode location = locationParent.getChild(lastBase);
            if (location == null) {
                t.addSorted(lastLastBase + 1, lastLastBase);
                getFromFile(locationParent, numBases, t);
                t.deleteAtIndex(lastLastBase + 1);
                location = locationParent.getChild(lastBase);
            }
            t.addSorted(lastBase, lastLastBase);
            return location;
        }
    }

    //threads should compute the index needed (and allocate the read object and trie node) ahead of time
    //alternatively each thread could have there own copy of each file (or create it on demand)
    //mallocing and reading could be separated
    public void getFromFile(TrieNode destinationParent, int numTerms, ThreadVariables atMinIndex){
        int beginIndex = (int) Script.getIndex(atMinIndex, numTerms, fromResidue).index;
        trieSources[numTerms].goToIndex(beginIndex);
        for (int i = destinationParent.minBase; i <= destinationParent.maxBase; i++) {
            //in C you will need to double check that the thing is null before assigning values
            int newNodeDist = destinationParent.myBaseMaxDistance - destinationParent.myBsum * Script.modulo - i * fromResidue; //could just use minSum as read from file
            destinationParent.setChild(i, new TrieNode(i + 1, newNodeDist, destinationParent.myBsum + i));
            destinationParent.getChild(i).values = trieSources[numTerms].getNext().values;
        }
    }

}
