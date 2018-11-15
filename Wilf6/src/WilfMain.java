import java.util.ArrayList;
import java.util.HashMap;

public class WilfMain {

    static HashMap<CacheObject, Integer> isCachedIndexes1;
    //static HashMap<CacheObject, Integer> isCachedIndexes2;
    //static boolean[][] smallCachedIndexes1; would the array with bitshifting be faster?
    static ArrayList<CacheObject> currentUse;
    static ArrayList<Integer> counts;
    static IndexedArrayList<IndexedArrayList<Long>> currentResidues;
    static int cacheIndex1 = 0;
    //static int cacheIndex2 = 0;

    public static void main(String[] args){
        isCachedIndexes1 = new HashMap<>();
        //isCachedIndexes2 = new HashMap<>();
        currentUse = new ArrayList<>();
        counts = new ArrayList<>();
        //currentResidues = createResidues(TableIO.readTable());
        if (currentResidues == null){ //starting from nothing
            currentResidues = new IndexedArrayList<IndexedArrayList<Long>>(0);
            //currentResidues.put(0, new IndexedArrayList<>(1));
            currentResidues.put(1, new IndexedArrayList<>(1));
            currentResidues.get(1).put(0,new Long(1));
        }
        int currentCounter = currentResidues.getMyCurrentSize();
        while(currentCounter < 50){
            long[] mSumMultiplicities = computeBaseMultiplicities(currentCounter);
            long myValue = 0;
            currentResidues.put(currentCounter, new IndexedArrayList<Long>(currentCounter + 1));
            for (int r = 1; r < currentCounter; r++) {
                if (currentResidues.get(r).get(currentCounter % r) == null){
                    currentResidues.get(r).put(currentCounter % r, new Long(0));
                }
                long residue = currentResidues.get(r).get(currentCounter % r) + mSumMultiplicities[r];        //add to myvalue the remainder module msum of num modulo msum (the total of all previous partitions of the same remainder with respect to msum)
                myValue += residue;
                currentResidues.get(r).put(currentCounter % r, residue);
            }
            System.out.printf("%d: %d\n", currentCounter, myValue);
            TableIO.writeTable(createTable(currentResidues));
            currentCounter++;
        }
    }

    public static IndexedArrayList<IndexedArrayList<Long>> createResidues(ArrayList<ArrayList<Long>> table) {
        IndexedArrayList<IndexedArrayList<Long>> res = new IndexedArrayList<IndexedArrayList<Long>>(table.size());
        for (int i = 0; i < table.size(); i++){
            IndexedArrayList<Long> row = new IndexedArrayList<>(table.get(i).size());
            for (int j = 0; j < table.get(i).size(); i++){
                row.put(j, table.get(i).get(j));
            }
            res.put(i, row);
        }
        if (res.getMyCurrentSize() > 0) {
            return res;
        } else {
            return null;
        }
    }

    public static ArrayList<ArrayList<Long>> createTable(IndexedArrayList<IndexedArrayList<Long>> residues){
        ArrayList<ArrayList<Long>> tab = new ArrayList<ArrayList<Long>>();
        ArrayList<IndexedArrayList<Long>> tabCols = residues.getUnderlyingList();
        for (int i = 0; i < tabCols.size(); i++){
            ArrayList<Long> row = new ArrayList<Long>();
            if (tabCols.get(i) != null) {
                row = tabCols.get(i).getUnderlyingList();
                for (int j = 0; j < row.size(); j++){
                    if (row.get(j) == null){
                        row.set(j, new Long(0));
                    }
                }
            } else {
                for (int j = 0; j <= i; j++){
                    row.add(new Long(0));
                }
            }
            tab.add(row);
        }
        return tab;
    }

    static long[] currentBSumCounts;
    static boolean[] multsUsed;
    static boolean[] currentBases;
    static int target;
    static int cutoff = 7;

    //only do recurse middle -> let it be a multiplicity of 1 so that it's always included -> table stores only counts using 1 as a multiplicity and hence valid

    public static long[] computeBaseMultiplicities(int targetValue){
        target = targetValue;
        //cutoff = (int) Math.pow(target, 1.0/3.0) + 1; //due to exclusive less than later
         //if I let the cutoff change, don't I have to invalidate the cache?

        currentBSumCounts = new long[target + 1];
        multsUsed = new boolean[target + 1];
        currentBases = new boolean[target + 1];
        /*for (int i = cutoff; i <= target; i++){
            multsUsed[i] = true;
            recurseUp(i, i, 1, false);  //add an initial pair of base 1 and multiplicity i
            multsUsed[i] = false;
        }*/
        recurseMiddle(targetValue,0,0);
        recurseUp(0,0,0); //the pair will consists of base 1 and multiplicity < cutoff added in the middle
        //currentBSumCounts[target]++;
        for (int i = 0; i < counts.size(); i++){
            if (counts.get(i) > 1){
                addToCounts(i);
                counts.set(i, 0);
            } else if (counts.get(i) == 1){
                long[] bsumCounts = currentUse.get(i).getmSum();
                for (int j = 1; j < currentBSumCounts.length; j++){
                    currentBSumCounts[j] += bsumCounts[j];
                }
                counts.set(i, 0);
            }
        }
        return currentBSumCounts;
    }

    public static void addToCounts(int index){
        long[] bsumCounts = currentUse.get(index).getmSum();
        int myCount = counts.get(index);
        for (int i = 1; i < bsumCounts.length; i++){
            currentBSumCounts[i] += bsumCounts[i] * myCount;
        }
    }

    public static void recurseUp(int sumSoFar, int bSumSoFar, int maxBase){
        for (int b = maxBase + 1; sumSoFar + (b * cutoff) <= target; b++){
            for (int m = cutoff; m <= target; m++){
                if (multsUsed[m]){
                    continue;
                }
                int currentSum = sumSoFar + b * m;
                if (currentSum > target){
                    break;
                }
                if (currentSum <= target / 2) {
                    multsUsed[m] = true;
                    //check hashmap1
                    currentBases[b] = true;
                    recurseMiddle(target - sumSoFar, bSumSoFar + b, b);
                    //add if necessary
                    recurseUp(currentSum, bSumSoFar + b, b);
                    currentBases[b] = false;
                    multsUsed[m] = false;
                } else { //the only thing that can be added is target-sumSoFar 1 time
                    currentBSumCounts[bSumSoFar + target - sumSoFar]++;
                }
                /*} else {
                    if (currentSum <= target / 2) {
                        multsUsed[m] = true;
                        currentBases[currentIndex] = b;
                        recurseDown(target - sumSoFar, mSumSoFar, maxBase);
                        recurseUp(currentSum, mSumSoFar + m, b, false, currentBases, currentIndex);
                        currentBases[currentIndex] = 0;
                        multsUsed[m] = false;
                    } else { //the only thing that can be added is target-sumSoFar 1 time
                        currentBSumCounts[mSumSoFar + 1]++;
                    }
                }*/
            }
        }
    }

    static long[] localBSumCounts;

    public static void recurseMiddle(int distanceLeft, int bSumSoFar, int maxBase){
        CacheObject cacheObject = new CacheObject(distanceLeft, currentBases);
        Integer index = isCachedIndexes1.get(cacheObject);
        if (index != null){
            counts.set(index, counts.get(index) + 1);
            return;
        }
        localBSumCounts = new long[target + 1];
        //multsUsed[1] = true;
        for (int b = maxBase + 1; b < target; b++){
            if (distanceLeft - b > 0) {
                recurseDown(distanceLeft - b, bSumSoFar + b, maxBase); //add a (m, 1) pair
            } else if (distanceLeft - b == 0){
                localBSumCounts[bSumSoFar + b]++;
                break;
            } else {
                break;
            }
        }
        //multsUsed[1] = false;
        cacheObject.setmSum(localBSumCounts);
        currentUse.add(cacheObject);
        counts.add(1);
        isCachedIndexes1.put(cacheObject, cacheIndex1++);
    }

    public static void recurseDown(int distanceLeft, int bSumSoFar, int maxBase){
        /*CacheObject cacheObject = new CacheObject(distanceLeft, currentBases);
        Integer index = isCachedIndexes2.get(cacheObject);
        if (index != null){
            counts.set(index, counts.get(index) + 1);
            return currentUse.get(index).getmSum();
        }
        long[] localMsums = new long[target + 1];
        */
        for (int b = maxBase + 1; b <= target; b++){
            for (int m = 2; m < cutoff; m++){
                if (multsUsed[m]){
                    continue;
                }
                int currentDistanceLeft = distanceLeft - b * m;
                if (currentDistanceLeft < 0){
                    break;
                } else if (currentDistanceLeft == 0){
                    localBSumCounts[bSumSoFar + b]++;
                } else {
                    recurseDown(currentDistanceLeft, bSumSoFar + b, b);
                }
            }
        }
    }

    public static int getIndex(boolean[] basesUsed){
        int index = 0;
        for (int i = basesUsed.length - 1; i >= 2; i--){
            if (basesUsed[i]){
                index++;
            }
            index = index << 1;
        }
        if (basesUsed[1]){
            index++;
        }
        return index;
    }
    //could just add b - maxBase - 1 to nums in the first place
}
