import java.math.BigInteger;

public class GrowthOfIndexes {

    static int minTerms = 1;
    static int MAX = 100;
    static BigInteger[][][] table;

    public static void main(String[] args){
        int maxNumTerms = computeInverse(MAX, minTerms);
        initTable(maxNumTerms);
        BigInteger[] values = new BigInteger[MAX + 1];
        BigInteger totalSum = BigInteger.ZERO;
        for (int i = 1; i <= MAX; i++){
            int currentNumTerms = computeInverse(i, minTerms);
            values[i] = BigInteger.ZERO;
            for (int j = 1; j <= currentNumTerms; j++){
                values[i] = values[i].add(computeValue(1, i, j));
            }
            totalSum = totalSum.add(values[i]);
            System.out.println(i + "," + values[i] + "," + Math.log(values[i].doubleValue()) + "," + totalSum + "," + Math.log(totalSum.doubleValue()));
        }
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
            System.out.printf("Made %d have max minBase %d and min distLeft %d\n", i, (int) maxBases[i], offsets[i]);
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
