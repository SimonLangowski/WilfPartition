import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class PIEFullTest {

    static int cutoff = 3;
    static int MAX = 10;
    static Recipe[][] recipes;

    public static void main(String[] args){
        initTable();
        recipes = new Recipe[cutoff + 1][];
        for (int i = 0; i <= cutoff; i++){
            recipes[i] = new Recipe[pascalTable[cutoff][i]];
        }
        recipeGenerator();
        generatePrecompute();
        int[] testBases = new int[]{};
        long startTime = System.nanoTime();
        long[] result = distFrom(testBases, MAX);
        long endTime = System.nanoTime();
        long newTime = endTime - startTime;
        System.out.println("Generated PIE in " + (endTime - startTime) + "ns");
        System.out.println(Arrays.toString(result));
        startTime = System.nanoTime();
        long[] c = Q3Test.middleRecursionWrapper(MAX, new ThreadVariables(testBases, MAX), cutoff+1);
        endTime = System.nanoTime();
        System.out.println("Generated in " + (endTime - startTime) + "ns");
        long oldTime = endTime - startTime;
        System.out.println(Arrays.toString(c));
        if (newTime <= oldTime){
            System.out.println("FASTER");
        } else {
            System.out.println("SLOWER");
        }

    }

    static boolean incrementBases(int[] bases){
        int incrementPosition = bases.length - 1;
        while(incrementPosition >= 0){
            bases[incrementPosition]++;
            for (int i = incrementPosition + 1; i < bases.length; i++){
                bases[i] = bases[incrementPosition] + (i - incrementPosition);
            }
            if (bases[incrementPosition] > cutoff - (bases.length - 1 - incrementPosition)){
                incrementPosition--;
            } else {
                return true;
            }
        }
        return false;
    }

    static void recipeGenerator(){
        for (int numT = 0; numT <= cutoff; numT++){
            int[] bases = new int[numT];
            for (int i = 0; i < bases.length; i++){
                bases[i] = i + 1;
            }
            int counter = 0;
            while(true) {
                Recipe r = new Recipe(numT);
                for (int i = 0; i < numT; i++){
                    int index = getDeletedIndex(bases, i);
                    int correspondingMult = bases[i];
                    r.offsets[i] = index;
                    r.shifts[i] = correspondingMult;
                }
                r.complement = findComplement(bases);
                recipes[numT][counter++] = r;
                boolean done = incrementBases(bases);
                if (!done) {
                    System.out.println("Made " + counter + " recipes at " + numT + " terms");
                    break;
                }
            }
        }
    }


    //might just be cutoff - numTerms, length-index
    static int findComplement(int[] bases){
        int[] complement = new int[cutoff - bases.length];
        if ((complement.length == 0) || (bases.length == 0)){
            return 0;
        }
        int currentLoc = 0;
        int compInd = 0;
        for (int i = 1; i <= cutoff; i++){
            if ((currentLoc >= bases.length) || (bases[currentLoc] != i)){
                complement[compInd++] = i;
            } else {
                currentLoc++;
            }
        }
        return getIndex(complement);
    }

    static int[][] pascalTable;

    static void initTable() {
        pascalTable = new int[cutoff+1][cutoff+1];
        pascalTable[0][0] = 1;
        for (int i = 1; i <= cutoff; i++){
            pascalTable[i][0] = 1;
            pascalTable[i][i] = 1;
            for(int j = 1; j <= i-1; j++){
                pascalTable[i][j] = pascalTable[i - 1][j] + pascalTable[i - 1][j - 1];
            }
        }
    }


    static int getDeletedIndex(int[] bases, int toDelete){
        int[] b2 = new int[bases.length - 1];
        for (int i = 0; i < toDelete; i++){
            b2[i] = bases[i];
        }
        for (int i = toDelete + 1; i < bases.length; i++){
            b2[i - 1] = bases[i];
        }
        return getIndex(b2);
    }

    static int getIndex(int[] bases, int length){
        int index = 0;
        for (int i = 0; i < length; i++){
            if (i > 0) {
                for (int j = bases[i - 1] + 1; j < bases[i]; j++) {
                    index += pascalTable[cutoff - j][length - i - 1]; //should be hockey stick?
                }
            } else {
                for (int j = 1; j < bases[i]; j++) {
                    index += pascalTable[cutoff - j][length - i - 1]; //should be hockey stick?
                }
            }
        }
        return index;
    }


    static int getIndex(int[] bases){
        int index = 0;
        for (int i = 0; i < bases.length; i++){
            if (i > 0) {
                for (int j = bases[i - 1] + 1; j < bases[i]; j++) {
                    index += pascalTable[cutoff - j][bases.length - i - 1]; //should be hockey stick?
                }
            } else {
                for (int j = 1; j < bases[i]; j++) {
                    index += pascalTable[cutoff - j][bases.length - i - 1]; //should be hockey stick?
                }
            }
        }
        return index;
    }

    static int[][][] precomputeDataInternal;  //I guess each would also have a minsum stored with it - maybe when reading data in? - shifts data tells you the bases

    static long[][][] precomputeData;

    static void generatePrecompute(){
        precomputeDataInternal = new int[cutoff+1][][];
        precomputeData = new long[cutoff + 1][][];
        for (int i = 0; i <= cutoff; i++){
            precomputeDataInternal[i] = new int[pascalTable[cutoff][i]][MAX + 1];
            precomputeData[i] = new long[pascalTable[cutoff][i]][MAX + 1];
        }
        precomputeDataInternal[0][0][0] = 1;
        for (int i = 1; i <= cutoff; i++){
            for (int j = 0; j < recipes[i].length; j++){
                precomputeFollowRecipe(recipes[i][j], precomputeDataInternal[i][j], i);
                System.out.println("Internal:" + i + "-" + j + Arrays.toString(precomputeDataInternal[i][j]));
            }
        }
        for (int i = cutoff; i >= 1; i--){
            for (int j = 0; j < recipes[i].length; j++){
                //sum children into each one (Exactly once per child)
                System.out.printf("Construction %d at %d terms: ", j, i);
                precomputeAddChildren(recipes[i][j], precomputeData[i][j], i);
                System.out.println();
                System.out.println("Total:" + i + "-" + j +Arrays.toString(precomputeData[i][j]));
            }
        }
        precomputeData[0][0][0] = 1;
    }

    static void precomputeAddChildren(Recipe r, long[] dest, int numTerms){
        int[] bases = r.shifts;
        recursiveAdd(dest, 0, bases, new int[numTerms], 0);
    }

    static void recursiveAdd(long[] dest, int myIndex, int[] bases, int[] current, int len){
        if (myIndex == bases.length){
            int index = getIndex(current, len);
            System.out.printf("%d ", index);
            addToArray(dest, precomputeDataInternal[len][index]);
            return;
        }
        recursiveAdd(dest, myIndex + 1, bases, current, len); //continue without this index
        current[len] = bases[myIndex];
        recursiveAdd(dest, myIndex + 1, bases, current, len + 1); //add with this index
    }

    static void precomputeFollowRecipe(Recipe r, int[] dest, int numTerms){
        int bsum = 0;
        for (int i = 0; i < r.shifts.length; i++){
            bsum+=r.shifts[i];
        }
        for(int i = 0; i < numTerms; i++){
            addShifted(dest, precomputeDataInternal[numTerms - 1][r.offsets[i]], bsum);
        }
        for (int i = bsum; i < dest.length; i++){
            dest[i] += dest[i - bsum];
        }
    }

    static void addShifted(int[] dest, int[] source, int shift){
        for (int i = shift; i < dest.length; i++){
            dest[i] += source[i - shift];
        }
    }

    static long[] distFrom(int[] bases, int maxDist){
        return generateInclusionExclusionPolynomials(bases, maxDist);
    }

    static long[] generateInclusionExclusionPolynomials(int[] bases, int maxDist){
        Polynomial[][] polynomials = new Polynomial[cutoff+1][];
        polynomials[1] = new Polynomial[cutoff];
        for (int i = 1; i <= cutoff; i++) {
            polynomials[1][i-1] = new Polynomial(bases[0]*i,bases[bases.length - 1]*i);
            polynomials[1][i-1].coefficients[0] = 1;
        }
        for (int numTerms = 2; numTerms <= cutoff; numTerms++) {
            polynomials[numTerms] = new Polynomial[pascalTable[cutoff][numTerms]];
            for (int index = 0; index < pascalTable[cutoff][numTerms]; index++) {
                Recipe r = recipes[numTerms][index];
                int msum = 0;
                for (int i = 0; i < r.shifts.length; i++) {
                    msum += r.shifts[i];
                }
                polynomials[numTerms][index] = new Polynomial(msum * bases[0], msum * bases[bases.length - 1]);
                for (int i = 0; i < numTerms; i++) {
                    polynomials[numTerms][index].addShifted(polynomials[numTerms - 1][r.offsets[i]], r.shifts[i]*bases[0], maxDist);
                }
            }
        }
        for (int base = 1; base < bases.length - 1; base++) {
            for (int i = 1; i <= cutoff; i++) {
                polynomials[1][i-1].setCoeff(bases[base]*i, 1);
            }
            for (int numTerms = 2; numTerms <= cutoff; numTerms++) {
                for (int index = 0; index < pascalTable[cutoff][numTerms]; index++) {
                    Recipe r = recipes[numTerms][index];
                    for (int i = 0; i < numTerms; i++) {
                        polynomials[numTerms][index].addShifted(polynomials[numTerms - 1][r.offsets[i]], r.shifts[i]*bases[base], maxDist);
                    }
                }
            }
        }
        return generateInclusionExclusionPolynomialAndMultiply(polynomials, bases, maxDist);
    }

    static long[] generateInclusionExclusionPolynomialAndMultiply(Polynomial[][] polynomials, int[] bases, int maxDist){

        long[] output = new long[MAX + 1];
        addToArray(output, precomputeData[cutoff][0]);
        for (int i = 0; i < cutoff; i++){
            polynomials[1][i].setCoeff((i+1)*bases[bases.length - 1], 1);
            long[] complement = precomputeData[cutoff - 1][recipes[1][i].complement];
            //System.out.println(Arrays.toString(complement));
            subToArray(output, polynomials[1][i].productWith(complement, maxDist));
            //System.out.println(polynomials[1][i]);
        }
        for (int numTerms = 2; numTerms <= cutoff; numTerms++){
            for (int index = 0; index < pascalTable[cutoff][numTerms]; index++ ) {
                Recipe r = recipes[numTerms][index];
                for (int i = 0; i < numTerms; i++) {
                    polynomials[numTerms][index].addShifted(polynomials[numTerms - 1][r.offsets[i]], r.shifts[i]*bases[bases.length - 1], maxDist);
                }
                //System.out.println(polynomials[numTerms][index]);
                long[] complement = precomputeData[cutoff - numTerms][r.complement];
                if (r.complement != pascalTable[cutoff][numTerms] -index - 1){
                    System.out.printf("%d : %d  %d\n", index, r.complement, pascalTable[cutoff][numTerms]);
                }
                //System.out.println(Arrays.toString(complement));
                if (numTerms % 2 == 0){
                    addToArray(output, polynomials[numTerms][index].productWith(complement, maxDist));
                } else {
                    subToArray(output, polynomials[numTerms][index].productWith(complement, maxDist));
                }
            }
        }
        return output;
    }

    static void addToArray(long[] dest, long[] src){
        for (int i = 0; i <= MAX; i++){
            dest[i] += src[i];
        }
    }
    static void addToArray(long[] dest, int[] src){
        for (int i = 0; i <= MAX; i++){
            dest[i] += src[i];
        }
    }

    static void subToArray(long[] dest, long[] src){
        for (int i = 0; i <= MAX; i++){
            dest[i] -= src[i];
        }
    }
    static void addToArray(long[] dest, Polynomial src){
        for (int i = src.minDegree; i <= src.maxDegree; i++){
            dest[i] += src.coefficients[i - src.minDegree];
        }
    }

    static void subToArray(long[] dest, Polynomial src){
        for (int i = src.minDegree; i <= src.maxDegree; i++){
            dest[i] -= src.coefficients[i - src.minDegree];
        }
    }

}
