import java.util.Arrays;
import java.util.Random;

public class Q2AllTest {

    static int NumToCheck = 87;
    static int NumToPrint = 10;
    static boolean viewOnly = false;

    public static void main(String[] args){
        ThreadVariables t = new ThreadVariables(NumToCheck + 1);
        check(t);
        t.addSorted(1, 0);
        check(t);
        t.deleteAtIndex(1);
        t.addSorted(2,0);
        check(t);
        t.deleteAtIndex(2);
        t.addSorted(3, 0);
        check(t);
        t.deleteAtIndex(3);
        t.addSorted(10,0);
        check(t);
        t.deleteAtIndex(10);
        t.addSorted(1,0);
        t.addSorted(2, 1);
        check(t);
        t.addSorted(3, 2);
        check(t);
        t.deleteAtIndex(3);
        t.deleteAtIndex(2);

        t.addSorted(4, 1);
        check(t);

        ThreadVariables e = new ThreadVariables(NumToCheck + 1);
        e.addSorted(1,0);
        e.addSorted(3,1);
        check(e);

        checkRandom();
    }
    static void checkRandom(){
        Random r = new Random();
        for (int i = 0; i < NumToCheck; i++) {
            int numBases = r.nextInt((int) Math.sqrt(NumToCheck));
            int[] bases = new int[numBases];
            for (int j = 0; j < bases.length; j++){
                bases[j] = r.nextInt(NumToCheck - 1) + 1;
            }
            Arrays.sort(bases);
            ThreadVariables t = new ThreadVariables(NumToCheck + 1);
            int lastBase = 0;
            for (int j = 0; j < bases.length; j++){
                if (lastBase != bases[j]){
                    t.addSorted(bases[j], lastBase);
                    lastBase = bases[j];
                }
            }
            if (!check(t)){
                break;
            }
        }
    }

    static boolean check(ThreadVariables t){
        if (viewOnly){
            long startTime = System.nanoTime();
            long[] answers = q2AllOld(NumToCheck, t);
            long oldTime = System.nanoTime() - startTime;
            System.out.println(t + " " + oldTime + "ns");
            Calculate2.printArray(answers);
            return true;
        }
        long startTime = System.nanoTime();
        long[] results = q2AllNew(NumToCheck, t);
        long newTime = System.nanoTime() - startTime;
        startTime = System.nanoTime();
        long[] answers = q2AllOld(NumToCheck, t);
        long oldTime = System.nanoTime() - startTime;
        int failCount = 0;
        for (int i = 0; i <= NumToCheck; i++){
            if (results[i] != answers[i]){
                failCount++;
            }
        }
        if (failCount > 0){
            System.out.println(t + " failed " + failCount);
            int c = NumToPrint;
            for (int i = 0; i <= NumToCheck; i++){
                if (c <= 0){
                    break;
                }
                if (results[i] != answers[i]){
                    System.out.println("At " + i + " should be " + answers[i] + " but is " + results[i]);
                    c--;
                }
            }
            return false;
        } else {
            System.out.println(t + " passed in " + newTime + " ns (" + oldTime + "ns before)");
            return true;
        }
    }

    static long[] q2AllNew(int maxDistanceLeft, ThreadVariables t){
        long[] c = new long[2*(maxDistanceLeft/2) + 2];
        int cummulativeCount = 1;
        int evenBaseCount = 0;
        int oddBaseCount = 0;
        for (int d = 1; d <= (maxDistanceLeft / 2); d++) {
            //no need to add for odd
            //no need to divide by 2 to check for odd (since 2*base will always first trigger at even
            //odd bases
            if (t.increasing[2*d-1] != 0){
                oddBaseCount++;
            }
            c[2*d - 1] = cummulativeCount - oddBaseCount;

            //divide by 2, add 1
            //have counter that increments every other loop (or loop unroll twice)
            cummulativeCount++;
            //for each base subtract one from the numbers greater than equal to 2 * base
            if (t.increasing[d] != 0){
                cummulativeCount--;
            }
            //for bases of the same parity (loop unroll twice)
            //for each base subtract one from the numbers greater than equal to base
            //even bases
            if (t.increasing[2*d] != 0){
                evenBaseCount++;
            }
            c[2*d] = cummulativeCount - evenBaseCount;

        }
        if ((maxDistanceLeft % 2) == 1){
            if (t.increasing[maxDistanceLeft] != 0){
                oddBaseCount++;
            }
            c[maxDistanceLeft] = cummulativeCount - oddBaseCount;
        }
        //add one for each number that is 2 * a base plus another base - if you let check same bases, then need to add one back to each base * 3
        int currentBase = t.increasing[0];
        while (currentBase != t.size){
            int nextBase = t.increasing[0];
            while (nextBase != t.size){
                if (2*currentBase + nextBase <= maxDistanceLeft) {
                    c[2 * currentBase + nextBase]++;
                } else {
                    break;
                }
                nextBase = t.increasing[nextBase];
            }
            if (3 * currentBase <= maxDistanceLeft){
                c[3 * currentBase]++;
            } else if (2 * currentBase > maxDistanceLeft){
                break;
            }
            currentBase = t.increasing[currentBase];
        }
        //subtract one if multiple of three - even if no bases? - yes because counts a pair of (a)(1)+(a)(2)
        //temporary decrement if multiple of three (or loop unroll thrice)
        for (int i = 3; i <= maxDistanceLeft; i = i + 3){
            c[i]--;
        }
        c[0] = 1;
        return c;
    }

    static long[] q2AllOld(int maxDistanceLeft, ThreadVariables t) {
        long[] counts = new long[maxDistanceLeft + 1];
        for (int i = 0; i <= maxDistanceLeft; i++) {
            counts[i] = q2(i, t); //this can be done much more efficiently
        }
        return counts;
    }

    static int q2(int distanceLeft, ThreadVariables t) {
        //count of numbers of the form (a)(1) + (2)(b) = distanceLeft
        //if distanceLeft is even have form {(0, distanceLeft/2), (2, distanceLeft/2 - 1), ... (distanceLeft, 0)}
        //if distanceLeft is odd have form {(1, floor(distanceLeft/2)), (3, floor(distanceLeft/2) - 1) ... (distanceLeft, 0)}
        //in either case there are floor(distanceLeft/2)) + 1terms
        //subtract those with invalid a,b from previously used bases   (increasing[base] != 0 if the base has been used)
        int count = (distanceLeft / 2) + 1;
        int currentBase = t.increasing[0]; //get first base
        int parity = distanceLeft % 2;
        while (currentBase <= (distanceLeft / 2)) {
            //one (a,b) value pair must be removed for the b value being included
            count--;
            int checkValue = distanceLeft - 2 * currentBase;
            if ((checkValue > 0) && (t.increasing[checkValue] != 0)) { //check if both a and b values
                count++; //don't double count
            }
            //one (a,b) value pair must be removed if there is an a value with the same parity
            if (currentBase % 2 == parity) {
                count--;
            }
            currentBase = t.increasing[currentBase];
        }
        while (currentBase <= distanceLeft) {
            //one (a,b) value pair must be removed if there is an a value with the same parity
            if (currentBase % 2 == parity) {
                count--;
            }
            currentBase = t.increasing[currentBase];
        }
        //final check if there is an (a,b) pair with a = b that has not already been removed for being a base
        if ((distanceLeft % 3 == 0) && (t.increasing[distanceLeft / 3] == 0)) {
            count--;
        }
        //printf("q2 of %d returned %d\n", distanceLeft, count);
        return count;
    }

}
