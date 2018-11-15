import java.util.ArrayList;
public class WilfComplex {

    //add multiple cutoff values
    static ArrayList<IndexedArrayList<SuperSavvyTrieHelper>> cache;
    static int[] cutoffs;
    static int MAX;
    static boolean[] multiplicitiesUsed;
    static boolean[] basesUsed;
    static ArrayList<Integer> currentBases;


    //the statement that a trie should ignore numbers greater than its distance left is equivalent to making distanceleft the first node of the trie

    public static void main(String[] args){
        int MIN = Integer.parseInt(args[0]);
        MAX = Integer.parseInt(args[1]);
        cutoffs = new int[args.length - 2];
        cache = new ArrayList<IndexedArrayList<SuperSavvyTrieHelper>>();
        for (int i = 2; i < args.length; i++){
            cutoffs[i - 2] = Integer.parseInt(args[i]);
            cache.add(new IndexedArrayList<SuperSavvyTrieHelper>(   ));
        }
        multiplicitiesUsed = new boolean[MAX + 1];
        basesUsed = new boolean[MAX + 1];
        currentBases = new ArrayList<Integer>(MAX);
        /*
        long count1 = lowerRecursion(MAX, 0);
        long count2 = middleRecursion(MAX, 0);
        long count3 = upperRecursion(MAX, 0);
        long total = count1 + count2 + count3;
        System.out.printf("%d + %d + %d = %d\n", count1, count2, count3, total);
        */
        long startTime = System.currentTimeMillis();
        for (int i = MIN; i <= MAX; i++) {
            System.out.printf("%d: %d\n", i, upperRecursionWrapper(i));
        }
        long endTime = System.currentTimeMillis();
        System.out.printf("Total time: %d ms\n", (endTime - startTime));
    }

    public static long upperRecursionWrapper(int distanceLeft){
        return middleRecursionWrapper(distanceLeft, 1) + upperRecursion(distanceLeft, 0);
    }

    public static long upperRecursion(int distanceLeft, int maxBase){
        long count = 0;
        for (int b = maxBase + 1; b <= distanceLeft; b++){
            if (!basesUsed[b]){
                if (b * cutoffs[0] > distanceLeft){
                    break;
                }
                basesUsed[b] = true;
                currentBases.add(b);
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
                            count += upperRecursion(currentDistanceLeft, b);
                            Long cacheValue = getCacheValue(currentDistanceLeft, currentBases, 0);
                            if (cacheValue == null){
                                cacheValue = middleRecursionWrapper(currentDistanceLeft, 1);
                                putCacheValue(currentDistanceLeft, currentBases, cacheValue, 0);
                            }
                            count += cacheValue;
                            multiplicitiesUsed[m] = false;
                        }
                    }
                }
                currentBases.remove(currentBases.size() - 1);
                basesUsed[b] = false;
            }
        }
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
        long count = 0;
        for (int b = maxBase + 1; b <= distanceLeft; b++){
            if (!basesUsed[b]){
                if (b * cutoffs[cutoffNumber] > distanceLeft){
                    break;
                }
                basesUsed[b] = true;
                int insertedAt = insertSorted(currentBases, b);
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
                            count += middleRecursion(currentDistanceLeft, b, cutoffNumber);
                            Long cacheValue = getCacheValue(currentDistanceLeft, currentBases, cutoffNumber);
                            if (cacheValue == null){
                                cacheValue = middleRecursionWrapper(currentDistanceLeft, cutoffNumber + 1);
                                putCacheValue(currentDistanceLeft, currentBases, cacheValue, cutoffNumber);
                            }
                            count += cacheValue;
                            multiplicitiesUsed[m] = false;
                        }
                    }
                }
                currentBases.remove(insertedAt);
                basesUsed[b] = false;
            }
        }
        return count;
    }

    public static long lowerRecursionWrapper(int distanceLeft){
        return lowerRecursion(distanceLeft, 0);
    }

    public static long lowerRecursion(int distanceLeft, int maxBase){
        long count = 0;
        for (int b = maxBase + 1; b <= distanceLeft; b++){
            if (!basesUsed[b]){
                basesUsed[b] = true;
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
                            count += lowerRecursion(currentDistanceLeft, b);
                            multiplicitiesUsed[m] = false;
                        }
                    }
                }
                basesUsed[b] = false;
            }
        }
        return count;
    }

    public static Long getCacheValue(int distanceLeft, ArrayList<Integer> basesUsed, int cacheNumber){
        SuperSavvyTrieHelper internalCache = cache.get(cacheNumber).get(distanceLeft);
        if (internalCache != null){
            return internalCache.get(basesUsed);
        }
        return null;
    }

    public static void putCacheValue(int distanceLeft, ArrayList<Integer> basesUsed, long valueToCache, int cacheNumber){
        SuperSavvyTrieHelper internalCache = cache.get(cacheNumber).get(distanceLeft);
        if (internalCache != null){
            internalCache.put(basesUsed, valueToCache);
        } else {
            internalCache = new SuperSavvyTrieHelper(distanceLeft);
            internalCache.put(basesUsed, valueToCache);
            cache.get(cacheNumber).put(distanceLeft, internalCache);
        }
    }

    public static int insertSorted(ArrayList<Integer> array, int valueToInsert){
        for (int i = 0; i < array.size(); i++){
            if (valueToInsert < array.get(i)){
                array.add(i, valueToInsert);
                return i;
            }
        }
        array.add(valueToInsert);
        return array.size() - 1;
    }

}
