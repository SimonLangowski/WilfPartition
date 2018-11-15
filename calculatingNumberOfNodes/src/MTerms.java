public class MTerms {


    public static void main(String[] args){
        System.out.println(computeInverse(1000,1));
    }

    static int computeInverse(int max, int minTerms){
        int numTerms = 0;
        int result = getMinVal(numTerms, minTerms);
        while (result < max){
            numTerms++;
            result = getMinVal(numTerms, minTerms);
        }
        return numTerms - 1;
    }

    static int getMinVal(int numTerms, int minTerms){
        int val = 0;
        int startingMultiplicity = minTerms + numTerms - 1;
        for (int i = 0; i < numTerms; i++){
            val += (i + 1) * (startingMultiplicity--);
        }
        return val;
    }
}
