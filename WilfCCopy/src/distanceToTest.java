public class distanceToTest {


    //since we need to go through all the bases, we should to distance to,
    //there's less memory per node and you don't need to find the bottom first

    //can still use a lower cutoff of 3 (or 4 - test), and use q2 to compute totals for summing at each step
    //in single ram you can trim nodes that will have no children?

    //if not using disk, using numz and writing a numz mult int method (using gmp when necessary) makes more sense

    //writing/reading raw bits strings to disk makes more sense than calling printf
    //just have to write array sizes with data
    //mpz_out_raw and mpz_inp_raw for future
    //means you can't detect end of file with feof - have to write sizes whole way

    public static void main(String[] args){
        ThreadVariables t = new ThreadVariables(5);
        ImplementingMultiplication2.MAX = 100;
        ImplementingMultiplication2.masterCutoff = 5;
        ImplementingMultiplication2.addSorted(t, 1, 0);
        ImplementingMultiplication2.addSorted(t, 2, 1);
        ImplementingMultiplication2.addSorted(t, 3, 2);
        ImplementingMultiplication2.addSorted(t, 4, 3);
        long[] total = ImplementingMultiplication2.distanceTo(t);
        ImplementingMultiplication2.deleteAtIndex(t, 4);
        long[] part4 = ImplementingMultiplication2.distanceTo(t);
        ImplementingMultiplication2.addSorted(t,4,3);
        ImplementingMultiplication2.deleteAtIndex(t,3);
        long[] part3 = ImplementingMultiplication2.distanceTo(t);
        ImplementingMultiplication2.addSorted(t,3,2);
        ImplementingMultiplication2.deleteAtIndex(t,2);
        long[] part2 = ImplementingMultiplication2.distanceTo(t);
        ImplementingMultiplication2.addSorted(t,2,1);
        ImplementingMultiplication2.deleteAtIndex(t,1);
        long[] part1 = ImplementingMultiplication2.distanceTo(t);

        long[] test = new long[total.length];
        int[] cummulative = new int[1+2+3+4];
        for (int i = 0; i < total.length; i++){
            //each case denotes the lowest multiplicity in the partition
            if (i >= 14) {
                cummulative[i % 10] += part1[i - 10 - 4]; //bsum to increment all one multiplicity, (mastercutoff-1)*baseAdding //the minus one is since bsum includes adding once
            }
            if (i >= 18) {
                cummulative[i % 10] += part2[i - 10 - 8];
            }
            if (i >= 22) {
                cummulative[i % 10] += part3[i - 10 - 12];
            }
            if (i >= 26) {
                cummulative[i % 10] += part4[i - 10 - 16];
            }
            test[i] += cummulative[i % 10]; //modulo is for multiple multiplicity increases
        }
        System.out.println("total");
        for (int i = 0; i < total.length; i++){
            System.out.printf("%d, ", total[i]);
        }
        System.out.println("\nTest");
        for (int i = 0; i < test.length; i++){
            System.out.printf("%d, ", test[i]);
        }
    }
}
