import java.util.ArrayList;
public class WilfSimple2 {

    //add multiple cutoff values
    static TrieHelper[][] cache;
    static int MAX;
    static int CUTOFF1;
    static int CUTOFF2;
    static boolean[] multiplicitiesUsed;
    static boolean[] basesUsed;
    static ArrayList<Integer> currentBases;


    public static void main(String[] args){
        MAX = Integer.parseInt(args[0]);
        CUTOFF2 = Integer.parseInt(args[1]);
        CUTOFF1 = Integer.parseInt(args[2]);
        cache = new TrieHelper[2][MAX + 1];
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
        System.out.println(upperRecursionWrapper(MAX));
    }

    public static long upperRecursionWrapper(int distanceLeft){
        return middleRecursionWrapper(distanceLeft) + upperRecursion(distanceLeft, 0);
    }

    public static long upperRecursion(int distanceLeft, int maxBase){
        long count = 0;
        for (int b = maxBase + 1; b <= distanceLeft; b++){
            if (!basesUsed[b]){
                if (b * CUTOFF2 > distanceLeft){
                    break;
                }
                basesUsed[b] = true;
                currentBases.add(b);
                for (int m = CUTOFF2; m <= MAX; m++){
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
                                cacheValue = middleRecursionWrapper(currentDistanceLeft);
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

    public static long middleRecursionWrapper(int distanceLeft){
        return lowerRecursionWrapper(distanceLeft) + middleRecursion(distanceLeft, 0);
    }

    //I think this breaks the idea that current bases is kept in sorted order - should just make inefficient, not wrong

    public static long middleRecursion(int distanceLeft, int maxBase){
        long count = 0;
        for (int b = maxBase + 1; b <= distanceLeft; b++){
            if (!basesUsed[b]){
                if (b * CUTOFF1 > distanceLeft){
                    break;
                }
                basesUsed[b] = true;
                int insertedAt = insertSorted(currentBases, b);
                for (int m = CUTOFF1; m < CUTOFF2; m++){
                    if(!multiplicitiesUsed[m]){
                        int currentDistanceLeft = distanceLeft - b * m;
                        if (currentDistanceLeft < 0){
                            break;
                        } else if (currentDistanceLeft == 0){
                            count++;
                            break;
                        } else {
                            multiplicitiesUsed[m] = true;
                            count += middleRecursion(currentDistanceLeft, b);
                            Long cacheValue = getCacheValue(currentDistanceLeft, currentBases, 1);
                            if (cacheValue == null){
                                cacheValue = lowerRecursionWrapper(currentDistanceLeft);
                                putCacheValue(currentDistanceLeft, currentBases, cacheValue, 1);
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
                for (int m = 1; m < CUTOFF1; m++){
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
        TrieHelper internalCache = cache[cacheNumber][distanceLeft];
        if (internalCache != null){
            return internalCache.get(basesUsed);
        }
        return null;
    }

    public static void putCacheValue(int distanceLeft, ArrayList<Integer> basesUsed, long valueToCache, int cacheNumber){
        TrieHelper internalCache = cache[cacheNumber][distanceLeft];
        if (internalCache != null){
            internalCache.put(basesUsed, valueToCache);
        } else {
            internalCache = new TrieHelper(MAX);
            internalCache.put(basesUsed, valueToCache);
            cache[cacheNumber][distanceLeft] = internalCache;
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
