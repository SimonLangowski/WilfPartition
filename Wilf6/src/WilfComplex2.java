import java.util.ArrayList;
public class WilfComplex2 {

    //add multiple cutoff values
    static ArrayList<IndexedArrayList<SuperSavvyTrieHelper2>> cache;
    static int[] cutoffs;
    static int MAX;
    static boolean[] multiplicitiesUsed;
    static DoublyLinkedArray basesUsed;
    static StatCollector statCollector;
    static long[] hitCounts;
    static long[] missCounts;

    //the statement that a trie should ignore numbers greater than its distance left is equivalent to making distanceleft the first node of the trie

    public static void main(String[] args){
        int MIN = Integer.parseInt(args[0]);
        MAX = Integer.parseInt(args[1]);
        cutoffs = new int[args.length - 2];
        cache = new ArrayList<IndexedArrayList<SuperSavvyTrieHelper2>>();
        for (int i = 2; i < args.length; i++){
            cutoffs[i - 2] = Integer.parseInt(args[i]);
            cache.add(new IndexedArrayList<SuperSavvyTrieHelper2>());
        }
        for (int i = 0; i < cutoffs.length / 2; i++){
            int temp = cutoffs[i];
            cutoffs[i] = cutoffs[cutoffs.length - 1 - i];
            cutoffs[cutoffs.length - 1 - i] = temp;
        }
        multiplicitiesUsed = new boolean[MAX + 1];
        basesUsed = new DoublyLinkedArray(MAX + 1);
        SuperSavvyTrieHelper2.setCorrespondance(basesUsed);
        /*
        long count1 = lowerRecursion(MAX, 0);
        long count2 = middleRecursion(MAX, 0);
        long count3 = upperRecursion(MAX, 0);
        long total = count1 + count2 + count3;
        System.out.printf("%d + %d + %d = %d\n", count1, count2, count3, total);
        */
        hitCounts = new long[cutoffs.length];
        missCounts = new long[cutoffs.length];
        statCollector = new StatCollector(cutoffs);

        long startTime = System.nanoTime();
        long[] values = new long[MAX - MIN + 1];
        for (int i = MIN; i <= MAX; i++) {
            values[i - MIN] = upperRecursionWrapper(i);
        }
        //long[] testValues = new long[] {1752443,1911046, 2067456,2249444,2429337, 2647532,2852449,3101167,3350292,3632299,3916575};
        for (int i = 0; i < values.length; i++){
            //if (testValues[i] != values[i]){
                System.out.printf("%d: %d\n", i + MIN, values[i]);
            //}
        }
        long endTime = System.nanoTime();
        System.out.printf("%f,", (double)(endTime - startTime) / 1000000000.0);
        long totalMissCounts = 0;
        long totalMissWeight1 = 0;
        long totalMissWeight2 = 0;
        long totalMissWeight3 = 0;
        /*
        for (int i = 0; i < cutoffs.length; i++){
            long totalCounts = hitCounts[i] + missCounts[i];
            totalMissCounts += missCounts[i];
            totalMissWeight1 += missCounts[i] * cutoffs[i];
            totalMissWeight2 += (hitCounts[i] / missCounts[i]) * cutoffs[i];
            totalMissWeight3 += (hitCounts[i] - missCounts[i]) * cutoffs[i] * cutoffs[i];
            System.out.printf("\tCache %d: %d/%d/%d (%.5f%%)", cutoffs[i], hitCounts[i], missCounts[i], totalCounts, (double) hitCounts[i] / totalCounts);
        }
        System.out.printf("\t\tMissCounts: %d, weighted: %d, ratio: %d, diff^2: %d\n", totalMissCounts, totalMissWeight1, totalMissWeight2, totalMissWeight3);
        */
        statCollector.printAll();
    }

    public static long upperRecursionWrapper(int distanceLeft){
        return middleRecursionWrapper(distanceLeft, 1) + upperRecursion(distanceLeft, 0);
    }

    public static long upperRecursion(int distanceLeft, int maxBase){
        //statCollector.startTime("0: upperRecursion");
        long count = 0;
        int lastSeen = maxBase;
        for (int b = maxBase + 1; b <= distanceLeft; b++){
            if (!basesUsed.isIncluded(b)){
                if (b * cutoffs[0] > distanceLeft){
                    break;
                }
                basesUsed.addSorted(b, lastSeen);
                for (int m = cutoffs[0]; m <= distanceLeft; m++){
                    if(!multiplicitiesUsed[m]){
                        int currentDistanceLeft = distanceLeft - b * m;
                        if (currentDistanceLeft < 0){
                            break;
                        } else if (currentDistanceLeft == 0){
                            count++;
                            break;
                        } else {
                            multiplicitiesUsed[m] = true;
                            //statCollector.stopTime("0: upperRecursion");
                            count += upperRecursion(currentDistanceLeft, b);
                            long startTime = System.nanoTime();
                            Long cacheValue = getCacheValue(currentDistanceLeft, 0);
                            if (cacheValue == null){
                                cacheValue = middleRecursionWrapper(currentDistanceLeft, 1);
                                putCacheValue(currentDistanceLeft, cacheValue, 0);
                                long endTime = System.nanoTime();
                                statCollector.addTime(0, false, endTime - startTime);
                            } else {
                                long endTime = System.nanoTime();
                                statCollector.addTime(0, true, endTime - startTime);
                            }
                            //statCollector.startTime("0: upperRecursion");
                            count += cacheValue;
                            multiplicitiesUsed[m] = false;
                        }
                    }
                }
                basesUsed.deleteAtIndex(b);
            } else {
                lastSeen = b;
            }
        }
        //statCollector.stopTime("0: upperRecursion");
        return count;
    }

    public static long middleRecursionWrapper(int distanceLeft, int cutoff){
        if (cutoff <= cutoffs.length - 1){
            return middleRecursionWrapper(distanceLeft, cutoff + 1) + middleRecursion(distanceLeft, 0, cutoff);
        } else {
            return lowerRecursionWrapper(distanceLeft);
        }
    }

    //I think this breaks the idea that current bases is kept in sorted order - should just make inefficient, not wrong

    public static long middleRecursion(int distanceLeft, int maxBase, int cutoffNumber){
        //statCollector.startTime(cutoffNumber + ": middleRecursion");
        long count = 0;
        int lastSeen = maxBase;
        for (int b = maxBase + 1; b <= distanceLeft; b++){
            if (!basesUsed.isIncluded(b)){
                if (b * cutoffs[cutoffNumber] > distanceLeft){
                    break;
                }
                basesUsed.addSorted(b, lastSeen);
                for (int m = cutoffs[cutoffNumber]; m < cutoffs[cutoffNumber - 1]; m++){
                    if(!multiplicitiesUsed[m]){
                        int currentDistanceLeft = distanceLeft - b * m;
                        if (currentDistanceLeft < 0){
                            break;
                        } else if (currentDistanceLeft == 0){
                            count++;
                            break;
                        } else {
                            multiplicitiesUsed[m] = true;
                            //statCollector.stopTime(cutoffNumber + ": middleRecursion");
                            count += middleRecursion(currentDistanceLeft, b, cutoffNumber);
                            long startTime = System.nanoTime();
                            Long cacheValue = getCacheValue(currentDistanceLeft, cutoffNumber);
                            if (cacheValue == null){
                                cacheValue = middleRecursionWrapper(currentDistanceLeft, cutoffNumber + 1);
                                putCacheValue(currentDistanceLeft, cacheValue, cutoffNumber);
                                long endTime = System.nanoTime();
                                statCollector.addTime(cutoffNumber, false, endTime - startTime);
                            } else {
                                long endTime = System.nanoTime();
                                statCollector.addTime(cutoffNumber, true, endTime - startTime);
                            }
                            //statCollector.startTime(cutoffNumber + ": middleRecursion");
                            count += cacheValue;
                            multiplicitiesUsed[m] = false;
                        }
                    }
                }
                basesUsed.deleteAtIndex(b);
            } else {
                lastSeen = b;
            }
        }
        //statCollector.stopTime(cutoffNumber + ": middleRecursion");
        return count;
    }

    public static long lowerRecursionWrapper(int distanceLeft){
        return lowerRecursion(distanceLeft, 0);
    }

    public static long lowerRecursion(int distanceLeft, int maxBase){
        //statCollector.startTime("lowerRecursion");
        long count = 0;
        for (int b = maxBase + 1; b <= distanceLeft; b++){
            if (!basesUsed.isIncluded(b)){
                basesUsed.getIncreasing()[b] = -1;
                for (int m = 1; m < cutoffs[cutoffs.length - 1]; m++){
                    if(!multiplicitiesUsed[m]){
                        int currentDistanceLeft = distanceLeft - b * m;
                        if (currentDistanceLeft < 0){
                            break;
                        } else if (currentDistanceLeft == 0){
                            count++;
                            break;
                        } else {
                            multiplicitiesUsed[m] = true;
                            //statCollector.stopTime("lowerRecursion");
                            count += lowerRecursion(currentDistanceLeft, b);
                            //statCollector.startTime("lowerRecursion");
                            multiplicitiesUsed[m] = false;
                        }
                    }
                }
                basesUsed.getIncreasing()[b] = 0;
            }
        }
        //statCollector.stopTime("lowerRecursion");
        return count;
    }

    public static Long getCacheValue(int distanceLeft, int cacheNumber){
        //long sTime = System.nanoTime();
        SuperSavvyTrieHelper2 internalCache = cache.get(cacheNumber).get(distanceLeft);
        if (internalCache != null){
            //long eTime = System.nanoTime();
            //hitCounts[cacheNumber]++;
            //statCollector.addTime(cacheNumber + ": getCacheValueHit", eTime - sTime);
            return internalCache.get();
        }
        //long eTime = System.nanoTime();
        //missCounts[cacheNumber]++;
        //statCollector.addTime(cacheNumber + ": getCacheValueMiss", eTime - sTime); //when there's a miss adding nulls to the arraylist takes a long time since they are added on get rather than put
        return null;
    }

    public static void putCacheValue(int distanceLeft, long valueToCache, int cacheNumber){
        //statCollector.startTime(cacheNumber + ": putCacheValue");
        SuperSavvyTrieHelper2 internalCache = cache.get(cacheNumber).get(distanceLeft);
        if (internalCache != null){
            internalCache.put(valueToCache);
        } else {
            internalCache = new SuperSavvyTrieHelper2(distanceLeft);
            internalCache.put(valueToCache);
            cache.get(cacheNumber).put(distanceLeft, internalCache);
        }
        //statCollector.stopTime(cacheNumber + ": putCacheValue");
    }

}
