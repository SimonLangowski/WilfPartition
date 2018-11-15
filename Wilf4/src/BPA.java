public class BPA {

    long[][] numTermsMsum;
    //can store min number of terms and max number of terms and update whenever a new term is inserted

    public BPA(){
        numTermsMsum = new long[Wilf2.maxNumTerms + 1][Wilf2.max + 1];
    }

    public long[][] getNumTermsMsum(){
        return numTermsMsum;
    }

    public void addNewTerm(int numTerms, int msum, long numTimes){
        if (msum + numTerms <= Wilf2.max) {
            numTermsMsum[numTerms][msum + numTerms] += numTimes;
        }
    }
/*
    public void increment(int valToUpdate){
        for (int n = 1; n <= Wilf2.maxNumTerms; n++){
            for (int msum = 1; (msum <= valToUpdate) && (msum + n <= Wilf2.max); msum++){
                numTermsMsum[n][msum + n] = numTermsMsum[n][msum];
                numTermsMsum[n][msum] = 0;
            }
        }
    }

    public long getMsum(int msum){
        int sum = 0;
        for (int n = 1; n <= Wilf2.maxNumTerms; n++){
            sum += numTermsMsum[n][msum];
        }
        return sum;
    }
    */
    public void addMeMsum(long[] addTo){
        for (int numTerms = Wilf2.maxNumTerms; numTerms >= 1; numTerms--){
            for (int msum = Wilf2.max; msum >= 1; msum--){
                addTo[msum] += numTermsMsum[numTerms][msum];
                if (msum + numTerms <= Wilf2.max) {
                    numTermsMsum[numTerms][msum + numTerms] = numTermsMsum[numTerms][msum];
                }
                numTermsMsum[numTerms][msum] = 0;
            }
        }
    }
}
