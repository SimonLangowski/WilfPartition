import java.math.BigInteger;
//https://stackoverflow.com/questions/38534460/how-to-convert-biginteger-number-into-scientific-notation-string-and-again-recon
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

//The table could become a trie of depth three - one for each dimension

public class Calculate3 {
    static int minCutoff = 3;
    static int maxCutoff = 39;
    static int MAX = 1500;
    static BigInteger[][][] table;
    static int cutoff;
    static int baseWeight = MAX; //for the number of terms to compute by wrote

    //make write bash script with jobs in it
    public static void main(String[] args){
        if (args.length >= 1){
            MAX = Integer.parseInt(args[0]);
        }
        BigInteger maxBig = BigInteger.valueOf(MAX);
        NumberFormat formatter = new DecimalFormat("0.######E0", DecimalFormatSymbols.getInstance(Locale.ROOT));
        for (int c = minCutoff; c <= maxCutoff; c++) {
            cutoff = c;
            int maxNumTerms = computeInverse(MAX, cutoff);
            initTable(maxNumTerms);
            table = new BigInteger[maxNumTerms + 1][MAX / cutoff + 1][MAX + 1];
            for (int i = 0; i < offsets.length; i++){
                offsets[i] = 0;
            }
            BigInteger totalTotal = BigInteger.ZERO;
            BigInteger weightTotal = BigInteger.ZERO;
            BigInteger weightTotalWrote = BigInteger.ZERO;
            for (int i = 1; i <= maxNumTerms; i++) {
                if (table[i] != null) {
                    //System.out.println(i + ": " + computeValue(1, MAX, i));
                    BigInteger possibilities = computeValue(1, MAX, i);
                    totalTotal = totalTotal.add(possibilities);
                    weightTotal = weightTotal.add(possibilities.multiply(getWeight(i)));
                    BigInteger polyMultiplier = maxBig.pow(cutoff);

                    //does this need a factorial for the ordering?
                    //When I say by wrote it's saying choose out of the n remaining numbers not conflicting for each multiplicity, 1,2,3,4, then compute the total.
                    //although there is some stack efficiency in the actual program

                    weightTotalWrote = weightTotalWrote.add(possibilities.multiply(polyMultiplier));
                }
            }
            System.out.println(cutoff + ": " + totalTotal + " total, " + formatter.format(weightTotal) + " weightedPIE, " + formatter.format(weightTotalWrote) + " weightedIteration");
        }
    }

    /*static BigInteger getSpaceWeight(int numTerms){
        //if PIE arrays - need MAX^numTerms for each
        //What about if distFrom calculation is bundled with the distTo calculation?  So each base you add in distTo, you also do for distFrom?
        //It would come in from all permutation directions, can I take advantage of that?
        //Is distFrom or some partial contains coming in?
        //If I took one distFrom in, then I could knock down the power 1 without having to figure out the consecutive thing
        //If I only need the one, could I be more efficient in say always adding the largest base, and then doing something more DP - y?  Is this going to be any better than even odd then?  I guess I have control over maxBase and that complexity
        //couldn't I modify even/odd to do the same thing?
        //How would a DP algorithm have multiple outputs?
        //
    }*/

    static BigInteger getWeight(int numTerms){
        return BigInteger.valueOf(numTerms + 1).pow(cutoff - 1); //Subtract one if using consecutive indexes;
    }

    static void printArray(int[] array){
        System.out.print("[");
        for (int i = 0; i < array.length; i++){
            System.out.print(array[i]);
            if (i != array.length - 1){
                System.out.print(" ");
            } else {
                System.out.println("]");
            }
        }
    }

    static void printArray(long[] array){
        System.out.print("[");
        for (int i = 0; i < array.length; i++){
            System.out.print(array[i]);
            if (i != array.length - 1){
                System.out.print(" ");
            } else {
                System.out.println("]");
            }
        }
    }

    static int[] findIndex(BigInteger index, int numTerms){
        int[] guess = new int[numTerms];
        guess = finishGuess(guess, 1, 0);
        //the first time we get an index greater than the desired index means the number has gone one too far
        int minimumBaseGuess = 1;
        for (int currentTermIndex = 0; currentTermIndex < numTerms; currentTermIndex++) {
            int currentBaseGuess = minimumBaseGuess;
            while (true) {
                guess = finishGuess(guess, currentBaseGuess, currentTermIndex);
                BigInteger ind = getIndex(guess);
                if (ind.compareTo(index) == 0){
                    return guess;
                } else if (ind.compareTo(index) > 0){
                    currentBaseGuess--;
                    guess = finishGuess(guess, currentBaseGuess, currentTermIndex);
                    break;
                } else {
                    currentBaseGuess++;
                }
            }
            minimumBaseGuess = currentBaseGuess + 1;
        }
        return guess;
    }

    static int[] finishGuess(int[] guess, int minBase, int location){
        for (int i = location; i < guess.length; i++){
            guess[i] = minBase + (i - location);
        }
        return guess;
    }

    static BigInteger getIndex(int[] bases){
        int numTerms = bases.length;
        int startingMultiplicity = numTerms + cutoff - 1;
        int currentDistLeft = MAX;
        BigInteger spacesSkipped = BigInteger.ZERO;
        int minBase = 1;
        for (int i = 0; i < bases.length; i++){
            for (int j = minBase; j < bases[i]; j++){
                spacesSkipped = spacesSkipped.add(computeValue(j + 1, currentDistLeft - startingMultiplicity * j, numTerms - 1));
            }
            currentDistLeft -= bases[i] * startingMultiplicity;
            startingMultiplicity--;
            numTerms--;
            minBase = bases[i] + 1;
        }
        return spacesSkipped;
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
        table[numTerms][minBase][maxDistLeft - offsets[numTerms]] = val;
        //System.out.println("Returned " + table[numTerms][minBase][maxDistLeft] + " for " + numTerms + " terms, minBase " + minBase + ", distLeft " + maxDistLeft);
        return val;
    }

    //x = max, d = one less than minimum multiplicity for distance to
    static int getInverse(int x, int d){
        //System.out.println("x: " + x + ", d: " + d);
        double a = Math.sqrt(-243*Math.pow(d,4) - 1944*Math.pow(d,3)*x - 486*Math.pow(d,3) - 2916*Math.pow(d,2)*x - 351*Math.pow(d,2) - 972 * d * x + 2916*Math.pow(x,2) - 108*d - 12);
        double b = Math.pow(-324*Math.pow(d,2) - 108*d + 648*x - 216*Math.pow(d,3) + 12*a, (1.0/3.0));
        double result =(b/6) - ((6 * (-d - (1.0/3.0) - Math.pow(d,2))) /b) - d;
        //System.out.println("a: " + a + ", b: " + b + ", r: " + result);
        return (int) Math.floor(result);
    }



    static int[] offsets;
    static int[] maxBases;

    //there's a max j for each i (maximum minBase @ numTerms)
    //there's a min k for each i (minDistLeft @ numTerms i.e. 1,2,3,4,with numTerms-1)
    //so for each i allocate an array.

    static void initTable(int maxNumTerms){
        //table = new BigInteger[residueOneMaxNumTerms + 1][MAX / cutoff + 1][MAX + 1];
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

    static int getMinVal(int numTerms){
        int val = 0;
        int startingMultiplicity = cutoff + numTerms - 1;
        for (int i = 0; i < numTerms; i++){
            val += (i + 1) * (startingMultiplicity--);
        }
        return val;
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
                total += (i + cutoff + 1) * startingMultiplicity--;
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

    static BigInteger getTableValue(int numTerms, int minBase, int maxDistLeft){
        return table[numTerms][minBase][maxDistLeft - offsets[numTerms]];
    }

    static void setTableValue(int numTerms, int minBase, int maxDistLeft, BigInteger value){
        table[numTerms][minBase][maxDistLeft - offsets[numTerms]] = value;
    }

}
