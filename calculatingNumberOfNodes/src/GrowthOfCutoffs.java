import java.math.BigInteger;

public class GrowthOfCutoffs {

    static int minTerms;
    static int startTerms = 4;
    static int maxTerms = 50;
    static int MAX = 1000;
    static BigInteger[][][] table;

    public static void main(String[] args){
        if (args.length > 0){
            MAX = Integer.parseInt(args[0]);
        }
        BigInteger[] powerOfTwo = new BigInteger[maxTerms + 1];
        powerOfTwo[0] = BigInteger.ONE;
        for (int i = 1; i <= maxTerms; i++){
            powerOfTwo[i] = powerOfTwo[i-1].multiply(BigInteger.valueOf(2));
        }
        BigInteger minimum = null;
        int minVal = 0;
        for (int currentNumTerms = startTerms; currentNumTerms <= maxTerms; currentNumTerms++){
            minTerms = currentNumTerms;
            int maxNumTerms = computeInverse(MAX, currentNumTerms);
            initTable(maxNumTerms);
            table = new BigInteger[maxNumTerms + 1][MAX / minTerms + 1][MAX + 1];
            for (int i = 0; i < offsets.length; i++){
                offsets[i] = 0;
            }
            BigInteger totalSum = BigInteger.ZERO;
            for (int i = 0; i <= maxNumTerms; i++){
                totalSum = totalSum.add(computeValue(1,MAX,i));
            }
            System.out.println(currentNumTerms + ": " + totalSum + " : " + totalSum.multiply(powerOfTwo[currentNumTerms]));
            totalSum = totalSum.multiply(powerOfTwo[currentNumTerms]);
            if (minimum == null){
                minimum = totalSum;
                minVal = currentNumTerms;
            } else if (minimum.compareTo(totalSum) > 0){
                minimum = totalSum;
                minVal = currentNumTerms;
            }
        }
        System.out.println("Minimum: " + minimum + " num terms: " + minVal);
    }

    static BigInteger computeValue(int minBase, int maxDistLeft, int numTerms){
        if (offsets[numTerms] > maxDistLeft){
            return BigInteger.ZERO;
        } else if (table[numTerms][minBase][maxDistLeft - offsets[numTerms]] != null){
            //System.out.println("Returned " + table[numTerms][minBase][maxDistLeft] + " for " + numTerms + " terms, minBase " + minBase + ", distLeft " + maxDistLeft);
            return table[numTerms][minBase][maxDistLeft - offsets[numTerms]];
        }
        BigInteger val;
        if (numTerms == 0){
            return BigInteger.ONE;
        } else if (numTerms == 1){
            int val2 = (maxDistLeft / minTerms) - minBase + 1;
            if (val2 < 0){
                val = BigInteger.ZERO;
            } else {
                val = new BigInteger(Integer.toString(val2));
            }
        } else {
            val = BigInteger.ZERO;
            int boundary = maxDistLeft / (minTerms + numTerms - 1);
            for (int k = minBase; k <= boundary; k++){
                BigInteger possibilities = computeValue(k + 1, maxDistLeft - ((minTerms + numTerms - 1) * k), numTerms - 1);
                if (possibilities.equals(BigInteger.ZERO)){
                    break;
                }
                val = val.add(possibilities);
            }
        }
        table[numTerms][minBase][maxDistLeft - offsets[numTerms]] = val;
        //System.out.println("Returned " + table[numTerms][minBase][maxDistLeft] + " for " + numTerms + " terms, minBase " + minBase + ", distLeft " + maxDistLeft);
        return val;
    }

    static int[] offsets;
    static int[] maxBases;

    //there's a max j for each i (maximum minBase @ numTerms)
    //there's a min k for each i (minDistLeft @ numTerms i.e. 1,2,3,4,with numTerms-1)
    //so for each i allocate an array.

    static void initTable(int maxNumTerms){
        //table = new BigInteger[residueOneMaxNumTerms + 1][MAX / minTerms + 1][MAX + 1];
        table = new BigInteger[maxNumTerms + 1][][];
        offsets = new int[maxNumTerms + 1];
        maxBases = new int[maxNumTerms + 1];
        for (int i = 0; i <= maxNumTerms; i++){
            offsets[i] = getMinVal(i);
            if (offsets[i] > MAX) {
                break;
            }
            maxBases[i] = findMaxBase(i);
            table[i] = new BigInteger[maxBases[i] + 1][MAX + 1 - offsets[i]];
            //System.out.printf("Made %d have max minBase %d and min distLeft %d\n", i, (int) maxBases[i], offsets[i]);
        }
    }

    static int findMaxBase(int numTerms){
        if (numTerms == 0){
            return 0;
        }
        int maxBaseGuess = 1;
        while (true){
            int total = 0;
            int startingMultiplicity = maxBaseGuess + numTerms - 1;
            for (int i = 0; i < numTerms; i++){
                total += (i + minTerms) * startingMultiplicity--;
            }
            if (total > MAX){
                return maxBaseGuess - 1;
            }
            maxBaseGuess++;
        }
    }

    static int computeInverse(int max, int residue){
        int numTerms = 0;
        int result = getMinVal(numTerms, residue);
        while (result <= max){
            numTerms++;
            result = getMinVal(numTerms, residue);
        }
        return numTerms - 1;
    }

    static int getMinVal(int numTerms, int residue){
        int val = 0;
        int startingMultiplicity = (numTerms - 1) + residue;
        for (int i = 0; i < numTerms; i++){
            val += (i + 1) * (startingMultiplicity);
            startingMultiplicity --;
        }
        return val;
    }

    static int getMinVal(int numTerms){
        int val = 0;
        int startingMultiplicity = minTerms + numTerms - 1;
        for (int i = 0; i < numTerms; i++){
            val += (i + 1) * (startingMultiplicity--);
        }
        return val;
    }

}
