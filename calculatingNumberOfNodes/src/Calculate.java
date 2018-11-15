import java.io.*;

//The table could become a trie of depth three - one for each dimension

public class Calculate {
    static int minTerms = 4;
    static int MAX = 700;
    static long [][][] table;

    static String executable = "./optimized";
    static int maxSize = 100000000;
    //make write bash script with jobs in it
    public static void main(String[] args){
        boolean sizeOnly = false;
        if (args.length >= 2){
            MAX = Integer.parseInt(args[0]);
            minTerms = Integer.parseInt(args[1]);
            try {
                maxSize = Integer.parseInt(args[2]);
            } catch (ArrayIndexOutOfBoundsException e){
                System.out.println("No max size specified, using default");
            } catch (NumberFormatException e){
                sizeOnly = true;
            }
        } else {
            sizeOnly = true;
        }
        int maxNumTerms = Calculate2.computeInverse(MAX, minTerms);
        //System.out.println(residueOneMaxNumTerms);
        table = new long[maxNumTerms + 1][MAX / minTerms + 1][MAX + 1];
        if (!sizeOnly) {
            String fileName = MAX + "-" + minTerms + "script.sh";
            File scriptFile = new File(fileName);
            try {
                PrintWriter scriptWriter = new PrintWriter(scriptFile);
                int fileCount = 0;
                boolean firstFile = true;
                for (int i = 1; i <= maxNumTerms; i++) {
                    System.out.println(i + ": " + computeValue(1, MAX, i));
                    int[][] boundaries = computeBoundaries(i, maxSize);
                    if ((firstFile) && (boundaries.length > 1)) {
                        scriptWriter.print(executable + " " + (i - 1) + "\n");
                        firstFile = false;
                    }
                    int blockNumber = 0;
                    for (int j = 0; j < boundaries.length; j++) {
                        printArray(boundaries[j]);
                        fileCount++;
                        if (!firstFile) {
                            scriptWriter.print(executable + " " + i + " " + blockNumber + " ");
                            blockNumber++;
                            if (j == 0) {
                                for (int k = 0; k < boundaries[j].length; k++) {
                                    scriptWriter.print("0 ");
                                }
                            } else {
                                for (int k = 0; k < boundaries[j].length; k++) {
                                    scriptWriter.print(boundaries[j - 1][k] + " ");
                                }
                            }
                            for (int k = 0; k < boundaries[j].length; k++) {
                                scriptWriter.print(boundaries[j][k] + " ");
                            }
                            scriptWriter.println();
                        }
                    }
                }
                System.out.println(fileCount);
                scriptWriter.flush();
                scriptWriter.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            long total = 0;
            for (int i = 1; i <= maxNumTerms; i++){
                System.out.println(i + ": " + computeValue(1, MAX, i));
                total += computeValue(1, MAX, i);
            }
            System.out.println(total);
            /*for (int i = 0; i < table.length; i++){
                System.out.println(i);
                for (int j = 0; j < table[i].length; j++){
                    for (int k = 0; k < table[i][j].length; k++) {
                        System.out.print(table[i][j][k] + ", ");
                    }
                    System.out.println();
                }
            }*/
            /*
            for (int o = MAX; o >= 1; o--) {
                long totalCells = 0;
                long usedCells = 0;
                for (int i = 0; i < table.length; i++) {
                    for (int j = 0; j < table[i].length; j++) {
                        for (int k = 0; k < table[i][j].length; k++) {
                            totalCells++;
                            if (table[i][j][k] != 0) {
                                usedCells++;
                            }
                        }
                    }
                }
                System.out.printf("Used %d of %d cells (%f)\n", usedCells, totalCells, (double) usedCells / totalCells);
                for (int i = 1; i <= maxNumTerms; i++){
                    System.out.println(i + ": " + (computeValue(1, o, i) - computeValue(1, o - 1, i)));
                }
            }
            */
        }
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

    static int[][] computeBoundaries(int numTerms, int maxSize){
        long totalSize = computeValue(1, MAX, numTerms);
        int numSections = (int) (totalSize / maxSize) + 1;
        long sectionSize = (totalSize / numSections) + 1;
        int[][] boundaries = new int[numSections][numTerms];
        for (int i = 0; i < numSections; i++){
            long targetSectionSize = sectionSize * (i + 1);
            int[] boundary;
            if (targetSectionSize < totalSize) {
                boundary = findIndex(targetSectionSize - 1, numTerms);
            } else {
                boundary = findIndex(totalSize - 1, numTerms); //this makes it inclusive
            }
            for (int j = 0; j < boundary.length; j++){
                boundaries[i][j] = boundary[j];
            }
        }
        return boundaries;
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
        int startingMultiplicity = numTerms + minTerms - 1;
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

    static long computeValue(int minBase, int maxDistLeft, int numTerms){
        if (table[numTerms][minBase][maxDistLeft] != 0){
            //System.out.println("Returned " + table[numTerms][minBase][maxDistLeft] + " for " + numTerms + " terms, minBase " + minBase + ", distLeft " + maxDistLeft);
            return table[numTerms][minBase][maxDistLeft];
        }
        long val = 0;
        if (numTerms == 0){
            return 1;
        } else if (numTerms == 1){
            val = (maxDistLeft / minTerms) - minBase + 1;
            if (val < 0){
                val = 0;
            }
        } else {
            int boundary = maxDistLeft / (minTerms + numTerms - 1);
            for (int k = minBase; k <= boundary; k++){
                long possibilities = computeValue(k + 1, maxDistLeft - ((minTerms + numTerms - 1) * k), numTerms - 1);
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

    //x = max, d = one less than minimum multiplicity for distance to
    static int getInverse(int x, int d){
        //System.out.println("x: " + x + ", d: " + d);
        double a = Math.sqrt(-243*Math.pow(d,4) - 1944*Math.pow(d,3)*x - 486*Math.pow(d,3) - 2916*Math.pow(d,2)*x - 351*Math.pow(d,2) - 972 * d * x + 2916*Math.pow(x,2) - 108*d - 12);
        double b = Math.pow(-324*Math.pow(d,2) - 108*d + 648*x - 216*Math.pow(d,3) + 12*a, (1.0/3.0));
        double result =(b/6) - ((6 * (-d - (1.0/3.0) - Math.pow(d,2))) /b) - d;
        //System.out.println("a: " + a + ", b: " + b + ", r: " + result);
        return (int) Math.floor(result);
    }

}
