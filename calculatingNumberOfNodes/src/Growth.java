import java.math.BigInteger;

public class Growth {

    static int MAX = 100;
    static int cutoff = 1;
    static BigInteger[][][] table;


    //is the growth for each number of terms similar?
    //what does the difference between two represent - pairs with 1,2,3 at each number? the number of unique base combinations added? - I can get a "value" for each one where it begins existing
    //If I square it does it mean combine every unique base combo with every unique multiplicity combo :p

    public static void main(String[] args) {
        int maxNumTerms = computeInverse(MAX, cutoff);
        table = new BigInteger[maxNumTerms + 1][MAX / cutoff + 1][MAX + 1];
        System.out.println("Total growth of unique base combinations");
        for (int i = 0; i <= MAX; i++){
            int myMaxTerms = computeInverse(i, cutoff);
            BigInteger count = BigInteger.ZERO;
            for (int j = 0;  j<= myMaxTerms; j++){
                count = count.add(computeValue(1,i,j));
            }
            System.out.println(i + ": " + count);
        }
        System.out.println("Combinations added at each number");
        for (int i = 1; i <= MAX; i++){
            int myMaxTerms = computeInverse(i, cutoff);
            BigInteger count = BigInteger.ZERO;
            for (int j = 0;  j<= myMaxTerms; j++){
                count = count.add(computeValue(1,i,j).subtract(computeValue(1,i-1,j)));
            }
            System.out.println(i + ": " + count);
        }
        /*
        for (int i = 1; i <= MAX; i++){
            int myMaxTerms = computeInverse(i, cutoff);
            BigInteger count = BigInteger.ZERO;
            for (int j = 0;  j<= myMaxTerms; j++){
                BigInteger dif = (computeValue(1,i,j).subtract(computeValue(1,i-1,j)));
                count = count.add(dif.multiply(dif));
            }
            System.out.println(i + ": " + count);
        }*/
        for (int k = 1; k <= maxNumTerms; k++){
            System.out.println("Growth of " + k + " term combinations");
            System.out.println("Total");
            for (int i = 0; i <= MAX; i++){
                BigInteger result = computeValue(1,i,k);
                if (result.compareTo(BigInteger.ZERO) > 0){
                    System.out.println(i + ": " + result);
                }
            }
            System.out.println("Contributions");
            for (int i = 1; i <= MAX; i++){
                BigInteger result = computeValue(1,i,k).subtract(computeValue(1,i-1,k));
                if (result.compareTo(BigInteger.ZERO) > 0){
                    System.out.println(i + ": " + result);
                }
            }
        }
    }

    static BigInteger computeValue(int minBase, int maxDistLeft, int numTerms){
        if (table[numTerms][minBase][maxDistLeft] != null){
            //System.out.println("Returned " + table[numTerms][minBase][maxDistLeft] + " for " + numTerms + " terms, minBase " + minBase + ", distLeft " + maxDistLeft);
            return table[numTerms][minBase][maxDistLeft];
        }
        BigInteger val;
        if (numTerms == 0){
            return BigInteger.ONE;
        } else if (numTerms == 1){
            int val2 = (maxDistLeft / cutoff) - minBase + 1;
            if (val2 < 0){
                val = BigInteger.ZERO;
            } else {
                val = new BigInteger(Integer.toString(val2));
            }
        } else {
            val = BigInteger.ZERO;
            int boundary = maxDistLeft / (cutoff + numTerms - 1);
            for (int k = minBase; k <= boundary; k++){
                BigInteger possibilities = computeValue(k + 1, maxDistLeft - ((cutoff + numTerms - 1) * k), numTerms - 1);
                if (possibilities.equals(BigInteger.ZERO)){
                    break;
                }
                val = val.add(possibilities);
            }
        }
        table[numTerms][minBase][maxDistLeft] = val;
        //System.out.println("Returned " + table[numTerms][minBase][maxDistLeft] + " for " + numTerms + " terms, minBase " + minBase + ", distLeft " + maxDistLeft);
        return val;
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
}
