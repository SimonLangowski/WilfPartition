import java.util.Scanner;

public class ManualLookup {

    static int MAX;
    static int cutoff;
    static long [][][] table;

    public static void main(String[] args){
        MAX = Integer.parseInt(args[0]);
        cutoff = Integer.parseInt(args[1]);
        int maxNumTerms = computeInverse(MAX, cutoff);
        table = new long[maxNumTerms + 1][MAX / cutoff + 1][MAX + 1];
        Scanner reader = new Scanner(System.in);
        while(true){
            String line = reader.nextLine();
            String[] numbers = line.split(" ");
            if (numbers.length == 0){
                continue;
            } else if (numbers[0].equals("f")){
                long indexToFind = Long.parseLong(numbers[1]);
                int numTerms = Integer.parseInt(numbers[2]);
                printArray(findIndex(indexToFind, numTerms));
            } else if (numbers[0].equals("g")) {
                int[] bases = new int[numbers.length - 1];
                for (int i = 1; i < numbers.length; i++){
                    bases[i - 1] = Integer.parseInt(numbers[i]);
                }
                System.out.println(getIndex(bases));
            } else if (numbers[0].equals("m")) {
                for (int i = 0; i <= maxNumTerms; i++) {
                    System.out.println("i: " + computeValue(1, MAX, i) + "\n");
                }
            } else if (numbers[0].equals("q")){
                break;
            } else {
                System.out.println("Options:\n\tf index numTerms\ng basesInIncreasingOrder\nm list maxes\n");
            }
        }
    }

    static int[] findIndex(long index, int numTerms){
        int[] guess = new int[numTerms];
        guess = finishGuess(guess, 1, 0);
        //the first time we get an index greater than the desired index means the number has gone one too far
        int minimumBaseGuess = 1;
        for (int currentTermIndex = 0; currentTermIndex < numTerms; currentTermIndex++) {
            int currentBaseGuess = minimumBaseGuess;
            while (true) {
                guess = finishGuess(guess, currentBaseGuess, currentTermIndex);
                long ind = getIndex(guess);
                if (ind == index){
                    return guess;
                } else if (ind > index){
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

    static long getIndex(int[] bases){
        int numTerms = bases.length;
        int startingMultiplicity = numTerms + cutoff - 1;
        int currentDistLeft = MAX;
        long spacesSkipped = 0;
        int minBase = 1;
        for (int i = 0; i < bases.length; i++){
            for (int j = minBase; j < bases[i]; j++){
                spacesSkipped += computeValue(j + 1, currentDistLeft - startingMultiplicity * j, numTerms - 1);
            }
            currentDistLeft -= bases[i] * startingMultiplicity;
            startingMultiplicity--;
            numTerms--;
            minBase = bases[i] + 1;
        }
        return spacesSkipped;
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

    static long computeValue(int minBase, int maxDistLeft, int numTerms){
        if (table[numTerms][minBase][maxDistLeft] != 0){
            //System.out.println("Returned " + table[numTerms][minBase][maxDistLeft] + " for " + numTerms + " terms, minBase " + minBase + ", distLeft " + maxDistLeft);
            return table[numTerms][minBase][maxDistLeft];
        }
        long val = 0;
        if (numTerms == 0){
            return 1;
        } else if (numTerms == 1){
            val = (maxDistLeft / cutoff) - minBase + 1;
            if (val < 0){
                val = 0;
            }
        } else {
            int boundary = maxDistLeft / (cutoff + numTerms - 1);
            for (int k = minBase; k <= boundary; k++){
                long possibilities = computeValue(k + 1, maxDistLeft - ((cutoff + numTerms - 1) * k), numTerms - 1);
                if (possibilities == 0){
                    break;
                }
                val += possibilities;
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
