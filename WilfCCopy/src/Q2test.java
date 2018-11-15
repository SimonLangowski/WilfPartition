public class Q2test {

    public static void main(String[] args) {
        ThreadVariables t = new ThreadVariables(31);
        ImplementingMultiplication2.addSorted(t, 2, 0);
        ImplementingMultiplication2.addSorted(t, 3, 2);
        ImplementingMultiplication2.addSorted(t, 4, 3);
        long[] total = q2All(30, t);
        long[] test = q2allTest(30, t);
        System.out.println("total");
        for (int i = 0; i < total.length; i++){
            System.out.printf("%d, ", total[i]);
        }
        System.out.println("\nTest");
        for (int i = 0; i < test.length; i++){
            System.out.printf("%d, ", test[i]);
        }

    }

    static long[] q2allTest(int maxDistanceLeft, ThreadVariables t){
        long[] counts = new long[maxDistanceLeft + 1];
        counts[0] = 1;
        int cummulativeCount = 1;
        int cummulativeOddCount = 0;
        int cummulativeEvenCount = 0;
        int cummulativeHalfCount = 0;
        int cummulativeHalfOddCount = 0;
        int cummulativeHalfEvenCount = 0;
        int nextBase = t.increasing[0];
        int nextHalfBase = t.increasing[0];
        for (int i = 1; i <= maxDistanceLeft; i++){
            //odd
            if (i/2 == nextHalfBase){
                cummulativeCount--;
                cummulativeHalfOddCount--;
                nextHalfBase = t.increasing[nextHalfBase];
            } else if (i == nextBase){
                cummulativeOddCount--;
                nextBase = t.increasing[nextBase];
            }
            counts[i] = cummulativeCount + cummulativeHalfOddCount + cummulativeOddCount;
            if ((i % 3 == 0) && (t.increasing[i / 3] == 0)) {
                counts[i]--;
            }
            i++;
            if (i > maxDistanceLeft){
                break;
            }
            //even
            cummulativeCount++;
            if (i/2 == nextHalfBase){
                cummulativeCount--;
                cummulativeHalfEvenCount--;
                nextHalfBase = t.increasing[nextHalfBase];
            } else if (i == nextBase){
                cummulativeEvenCount--;
                nextBase = t.increasing[nextBase];
            }
            counts[i] = cummulativeCount + cummulativeEvenCount + cummulativeHalfEvenCount;
            if ((i % 3 == 0) && (t.increasing[i / 3] == 0)) {
                counts[i]--;
            }
        }
        int currentBase = t.increasing[0];
        while (currentBase < maxDistanceLeft / 2){
            int currentBase2 = t.increasing[currentBase];
            while(currentBase2 < maxDistanceLeft){
                int c = 2 * currentBase + currentBase2;
                if (c > maxDistanceLeft){
                    break;
                } else {
                    counts[c]++;
                }
                currentBase2 = t.increasing[currentBase2];
            }
            currentBase = t.increasing[currentBase];
        }
        return counts;
    }

    static long[] q2All(int maxDistanceLeft, ThreadVariables t) {
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
