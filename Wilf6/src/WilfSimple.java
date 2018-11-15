import java.util.ArrayList;
public class WilfSimple {

    //add multiple cutoff values
    static TrieHelper[] cache;
    static int MAX;
    static int CUTOFF;
    static boolean[] multiplicitiesUsed;
    static boolean[] basesUsed;
    static ArrayList<Integer> currentBases;


    public static void main(String[] args){
        MAX = Integer.parseInt(args[0]);
        CUTOFF = Integer.parseInt(args[1]);
        cache = new TrieHelper[MAX + 1];
        multiplicitiesUsed = new boolean[MAX + 1];
        basesUsed = new boolean[MAX + 1];
        currentBases = new ArrayList<Integer>(MAX);
        long startTime = System.currentTimeMillis();
        long count1 = lowerRecursion(MAX, 0);
        long count3 = upperRecursion(MAX, 0);
        long total = count1 + count3;
        System.out.printf("%d + %d = %d\n", count1, count3, total);
        long endTime = System.currentTimeMillis();
        System.out.printf("Total time: %d ms\n", (endTime - startTime));
    }

    public static long upperRecursion(int distanceLeft, int maxBase){
        long count = 0;
        for (int b = maxBase + 1; b <= distanceLeft; b++){
            if (!basesUsed[b]){
                if (b * CUTOFF > distanceLeft){
                    break;
                }
                basesUsed[b] = true;
                currentBases.add(b);
                for (int m = CUTOFF; m <= MAX; m++){
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
                            Long cacheValue = getCacheValue(currentDistanceLeft, currentBases);
                            if (cacheValue == null){
                                cacheValue = lowerRecursion(currentDistanceLeft, 0);
                                putCacheValue(currentDistanceLeft, currentBases, cacheValue);
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

    public static long lowerRecursion(int distanceLeft, int maxBase){
        long count = 0;
        for (int b = maxBase + 1; b <= distanceLeft; b++){
            if (!basesUsed[b]){
                basesUsed[b] = true;
                for (int m = 1; m < CUTOFF; m++){
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

    public static Long getCacheValue(int distanceLeft, ArrayList<Integer> basesUsed){
        TrieHelper internalCache = cache[distanceLeft];
        if (internalCache != null){
            return internalCache.get(basesUsed);
        }
        return null;
    }

    public static void putCacheValue(int distanceLeft, ArrayList<Integer> basesUsed, long valueToCache){
        TrieHelper internalCache = cache[distanceLeft];
        if (internalCache != null){
            internalCache.put(basesUsed, valueToCache);
        } else {
            internalCache = new TrieHelper(MAX);
            internalCache.put(basesUsed, valueToCache);
            cache[distanceLeft] = internalCache;
        }
    }

}
