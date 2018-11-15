import java.io.*;
import java.math.BigInteger;

//The table could become a trie of depth three - one for each dimension

public class Calculate2 {
    static int minTerms = 4;
    static int MAX = 100;
    static int jobsNumber = 10;
    static int maxFlushSize = 100000;
    static BigInteger[][][] table;

    static String executable = "./optimized";
    static String outputFile = "PartitionCounts.txt";
    static String sumProgrem = "./sumCounts";
    static int maxSize = 500000;
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
        int maxNumTerms = computeInverse(MAX, minTerms);
        //System.out.println(residueOneMaxNumTerms);
        initTable(maxNumTerms);


        table = new BigInteger[maxNumTerms + 1][MAX / minTerms + 1][MAX + 1];
        for (int i = 0; i < offsets.length; i++){
            offsets[i] = 0;
        }
        if (!sizeOnly) {
            String fileName = MAX + "-" + minTerms + "script.sh";
            File scriptFile = new File(fileName);
            try {
                PrintWriter scriptWriter = new PrintWriter(scriptFile);
                scriptWriter.println("set -e");
                scriptWriter.println(getMakeDirectoryCommand(0));
                scriptWriter.println(getInitialFile());
                int totalFileCount = 0;
                for (int i = 0; i <= maxNumTerms; i++) {
                    System.out.println(i + ": " + computeValue(1, MAX, i));
                    BigInteger totalSize = computeValue(1, MAX, i);
                    BigInteger numSections = (totalSize.divide(new BigInteger(Integer.toString(maxSize)))).add(BigInteger.ONE);
                    BigInteger sectionSize = (totalSize.divide(numSections)).add(BigInteger.ONE);
                    BigInteger j = BigInteger.ZERO;
                    long blocksCreated = 0;
                    long blockNumber = 0;
                    BigInteger lastBoundary = BigInteger.ZERO;
                    scriptWriter.println(getMakeDirectoryCommand(i + 1));
                    String processScriptFile = "" + (i + 1) + "TermJobs.sh";
                    File processFile = new File(processScriptFile);
                    PrintWriter processWriter = new PrintWriter(processFile);
                    while (true) {
                        BigInteger targetSectionSize = sectionSize.multiply(j.add(BigInteger.ONE));
                        j = j.add(BigInteger.ONE);
                        if (j.compareTo(numSections) > 0) {
                            break;
                        } else {
                            totalFileCount++;
                            blocksCreated++;
                            processWriter.print(executable + " " + (i + 1) + " " + blockNumber + " ");
                            blockNumber++;
                            processWriter.print(lastBoundary + " " + targetSectionSize);
                            processWriter.println();
                            lastBoundary = targetSectionSize;
                        }
                        if (blocksCreated % maxFlushSize == maxFlushSize - 1) {
                            processWriter.flush();
                        }
                    }
                    processWriter.flush();
                    processWriter.close();
                    scriptWriter.println("echo \"Beginning " + blocksCreated + " blocks from " + i + " to " + (i + 1) + " terms\"");
                    scriptWriter.println("cat " + processScriptFile + " | parallel -j" + jobsNumber + " --delay 1 --halt soon,fail=1");
                    //scriptWriter.println(makeCheckCommand(i + 1, blocksCreated));
                    scriptWriter.println(makeSortCommand(i + 1));
                    scriptWriter.println("stat -c%s ./" + (i + 1) + "/sorted.txt > ./tmp.txt");
                }
                System.out.println(totalFileCount);
                scriptWriter.println(sumProgrem + " " + MAX + " " + minTerms + " " + 1 + " > " + outputFile);
                scriptWriter.println("cat " + outputFile);
                scriptWriter.flush();
                scriptWriter.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        BigInteger totalSum = BigInteger.ZERO;
        for (int i = 0; i <= maxNumTerms; i++) {
            System.out.println(i + ": " + computeValue(1, MAX, i));
            totalSum = totalSum.add(computeValue(1, MAX, i));
        }
        System.out.println(totalSum);
        long usedCells = 0;
        for (int i = 0; i < table.length; i++){
            if (table[i] == null){
                continue;
            }
            int[] kCounts = new int[MAX + 1 - offsets[i]];
            int maxBits = 0;
            int mink = Integer.MAX_VALUE;
            int minj = Integer.MAX_VALUE;
            int maxk = 0;
            int maxj = 0;
            for (int j = 0; j < table[i].length; j++){
                for (int k = 0; k < table[i][j].length; k++){
                    if (table[i][j][k] != null){
                        usedCells++;
                        kCounts[k]++;
                        if (table[i][j][k].bitLength() > maxBits){
                            maxBits = table[i][j][k].bitLength();
                        }
                        if (table[i][j][k].equals(BigInteger.ZERO)){

                        } else {
                            if (j > maxj) {
                                maxj = j;
                            }
                            if (j < minj){
                                minj = j;
                            }
                            if (k < mink){
                                mink = k;
                            }
                            if (k > maxk){
                                maxk = k;
                            }
                        }
                    }
                }
            }
        }

        //there's a max j for each i (maximum minBase @ numTerms)
        //there's a min k for each i (minDistLeft @ numTerms i.e. 1,2,3,4,with numTerms-1)
        //so for each i allocate an array.
        System.out.printf("Used %d cells\n", usedCells);
        //int[] tests = {500,1000,1500};
        //double[] observed = {0.01,29,2700};
        for (int t = 500; t <= MAX; t += 500) {
            //int t = tests[z];
            //double d = observed[z];
            BigInteger totalTotal = BigInteger.ZERO;
            double maxLayer = 0;
            int maxLayerNum = 0;
            for (int i = 1; i <= maxNumTerms; i++) {
                if (table[i] != null) {
                    //System.out.println(i + ": " + computeValue(1, t, i));
                    totalTotal = totalTotal.add(computeValue(1, t, i));
                    if (computeValue(1,t,i).doubleValue() > maxLayer){
                        maxLayerNum = i;
                        maxLayer = computeValue(1,t,i).doubleValue();
                    }
                }
            }
            System.out.println(t+ "-" + minTerms + ": " + totalTotal.doubleValue() + " predict: " + totalTotal.doubleValue()*50);
            System.out.println(": " + maxLayerNum + " with " + maxLayer*50);
        }
    }

    static String makeCheckCommand(int numTerms, long numFiles){
        StringBuilder command = new StringBuilder();
        command.append("for i in {0.." + (numFiles - 1) + "}; do\n");
        command.append("\tif [[ ! -s ./" + numTerms + "/block$icounts.txt ]]\n");
        command.append("\t\tthen echo \"Error block $i failed\"\n");
        command.append("\t\texit 1\n");
        command.append("\tfi\n");
        command.append("done");
        return command.toString();
    }

    static String makeSortCommand(int numTerms){
        StringBuilder command = new StringBuilder();
        command.append("sort -t, -k1,1n  --temporary-directory=./tempD --parallel=10");
        //can change to wildcard for number if counts files given different name or externsion
        /*for (int i = 0; i < numFiles; i++){
            command.append(" ./" + numTerms + "/block" + i + ".txt");
        }*/
        command.append(" --output=./" + numTerms + "/sorted.txt");
        command.append(" ./" + numTerms + "/block*.data");
        return command.toString();
    }

    static String getMakeDirectoryCommand(int numTerms){
        return "mkdir -p ./" + numTerms;
    }

    static String getInitialFile(){
        StringBuilder s = new StringBuilder();
        s.append("echo \"");
        s.append("0,"); //index
        //no bases to append
        s.append("0,"); //minSum
        s.append("1,"); //0 value
        for (int i = 1; i <= MAX; i++){
            s.append("0,");
        }
        s.append("\" > ./0/sorted.txt");
        return s.toString();
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
        int startingMultiplicity = numTerms + minTerms - 1;
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
            int val2 = (maxDistLeft / minTerms) - minBase + 1;
            if (val2 < 0){
                val = BigInteger.ZERO;
            } else {
                val = new BigInteger(Integer.toString(val2));
            }
        } else {
            val = BigInteger.ZERO;
            int boundary = maxDistLeft / (minTerms + numTerms - 1);
            for (int k = minBase; k <= boundary; k++){
                BigInteger possibilities = computeValue(k + 1, maxDistLeft - ((minTerms + numTerms - 1) * k), numTerms - 1);
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
        //table = new BigInteger[residueOneMaxNumTerms + 1][MAX / minTerms + 1][MAX + 1];
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
            System.out.printf("Made %d have max minBase %d and min distLeft %d\n", i, (int) maxBases[i], offsets[i]);
        }
    }

    static int getMinVal(int numTerms){
        int val = 0;
        int startingMultiplicity = minTerms + numTerms - 1;
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
                total += (i + minTerms + 1) * startingMultiplicity--;
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
