import java.util.ArrayList;

public class Script {

    static int MAX = 200;
    static int modulo = 2;
    static long[][][][] table;
    static ArrayList<VirtualFile> distToValues;
    static ArrayList<VirtualFile> distToPartials;
    static ArrayList<VirtualFile> trieSums;
    static ArrayList<VirtualFile> counts;
    static ArrayList<VirtualFile> contains;

    public static void main(String[] args){
        constructTable();
        for (int r = 0; r < table.length; r++){
            for (int i = 0; i < table[r].length; i++){
                for (int j = 0; j < table[r][i].length; j++){
                    for (int k = 0; k < table[r][i][j].length; k++){
                        if (table[r][i][j][k] != 0){
                            System.out.println("Residue " + r + " terms left " + i + " min base " + j + " distLeft " + k + " value " + table[r][i][j][k]);
                        }
                    }
                }
            }
        }
        int maxNumTerms1 = computeInverse(MAX, 1);
        int maxNumTerms2 = computeInverse(MAX, 2);
        distToValues = new ArrayList<>(maxNumTerms1 + 1);
        distToPartials = new ArrayList<>(maxNumTerms1 + 1);
        trieSums = new ArrayList<>(maxNumTerms2 + 1);
        counts = new ArrayList<>(maxNumTerms1 + 1);
        contains = new ArrayList<>(maxNumTerms1 + 1);
        for (int i = 0; i <= maxNumTerms1; i++){
            distToValues.add(new VirtualFile());
            distToPartials.add(new VirtualFile());
            trieSums.add(new VirtualFile());
            counts.add(new VirtualFile());
            contains.add(new VirtualFile());
        }
        long startTime = System.nanoTime();
        int[] zeroValues = new int[MAX + 1];
        zeroValues[0] = 1;
        int zeroMinSum = 0;
        distToPartials.get(0).entries.add(new VirtualFileEntry(0, new ArrayList<>(), zeroMinSum, zeroValues));
        for (int i = 1; i <= maxNumTerms1; i++){
            int m = (int) computeValue(1, MAX, i - 1, 1);
            DistToProcess d = new DistToProcess(i,0,m);
            d.runProcess();
            distToPartials.get(i).sortFile();
        }
        int m = (int) computeValue(1, MAX, maxNumTerms1, 1);
        DistToProcess d = new DistToProcess(maxNumTerms1 + 1, 0, m);
        d.runSubProcess();
        long endTime = System.nanoTime();
        System.out.println("Dist to time: " + (endTime - startTime) + "ns, " + ((endTime - startTime)/1000000000) + "s");
        startTime = System.nanoTime();
        m = (int) computeValue(1, MAX, maxNumTerms2, 2);
        SumChildrenAndShift baseValues = new SumChildrenAndShift(maxNumTerms2, 0, m);
        baseValues.runSubProcess();
        for (int i = maxNumTerms2 - 1; i >= 0; i--){
            m = (int) computeValue(1, MAX, i, 2);
            SumChildrenAndShift s = new SumChildrenAndShift(i, 0, m);
            s.runProcess();
        }
        endTime = System.nanoTime();
        System.out.println("Trie sum time: " + (endTime - startTime) + "ns, " + ((endTime - startTime)/1000000000) + "s");
        startTime = System.nanoTime();
        //0 may have to be put in a separate subprocess - can also allow you to combine everything that is purely exclusive and multiply once
        contains.get(0).entries.add(new VirtualFileEntry(0, new ArrayList<>(), zeroMinSum, zeroValues));
        for (int i = 0; i <= maxNumTerms1 - 1; i++){
            m = (int) computeValue(1, MAX, i, 1);
            ComputeDistFromAndCounts c = new ComputeDistFromAndCounts(i,maxNumTerms2,0,m);
            c.runProcess();
        }
        m = (int) computeValue(1, MAX, maxNumTerms1, 1);
        ComputeDistFromAndCounts c = new ComputeDistFromAndCounts(maxNumTerms1, maxNumTerms2, 0, m);
        c.runSubProcess();
        endTime = System.nanoTime();
        System.out.println("Contains and compute: " + (endTime - startTime) + "ns, " + ((endTime - startTime)/1000000000) + "s");
        SumCounts s = new SumCounts(maxNumTerms1);
        s.runProcess();
        System.out.println();
    }

    static IndexInfo getIndex(ThreadVariables t, int numTerms, int residue){
        //System.out.println(t);
        int startingMultiplicity = modulo * (numTerms - 1) + residue;
        int currentDistLeft = MAX;
        long spacesSkipped = 0;
        int minBase = 1;
        int currentBase = t.increasing[0];
        while(currentBase != t.size){
            for (int j = minBase; j < currentBase; j++){
                spacesSkipped += computeValue(j + 1, currentDistLeft - startingMultiplicity * j, numTerms - 1, residue);
            }
            currentDistLeft -= currentBase * startingMultiplicity;
            startingMultiplicity -= modulo;
            numTerms--;
            minBase = currentBase + 1;
            currentBase = t.increasing[currentBase];
        }
        return new IndexInfo(spacesSkipped, MAX - currentDistLeft);
    }

    static void constructTable(){
        int maxNumTerms = computeInverse(MAX, 1);
        table = new long[modulo + 1][maxNumTerms + 1][MAX + 1][MAX + 1];
        for (int i = 0; i <= maxNumTerms; i++){
            for (int j = 1; j <= modulo; j++) {
                System.out.println(i + ": " + computeValue(1, MAX, i, j));
            }
        }
    }

    static long computeValue(int minBase, int maxDistLeft, int numTerms, int residue){
        if (table[residue][numTerms][minBase][maxDistLeft] != 0){
            //System.out.println("Returned " + table[numTerms][minBase][maxDistLeft] + " for " + numTerms + " terms, minBase " + minBase + ", distLeft " + maxDistLeft);
            return table[residue][numTerms][minBase][maxDistLeft];
        }
        long val = 0;
        if (numTerms == 0){
            return 1;
        } else if (numTerms == 1){
            if (residue == 1) {
                val = (maxDistLeft - minBase + 1);
                if (val < 0) {
                    val = 0;
                }
            } else {
                int multiplicity = modulo * (numTerms - 1) + residue;
                int boundary = Math.min(maxDistLeft / multiplicity, MAX - 1);
                for (int k = minBase; k <= boundary; k++){
                    long possibilities = computeValue(k + 1, maxDistLeft - (multiplicity * k), numTerms - 1, residue);
                    if (possibilities == 0){
                        break;
                    }
                    val += possibilities;
                }
            }
        } else {
            int multiplicity = modulo * (numTerms - 1) + residue;
            int boundary = maxDistLeft / multiplicity;
            for (int k = minBase; k <= boundary; k++){
                long possibilities = computeValue(k + 1, maxDistLeft - (multiplicity * k), numTerms - 1, residue);
                if (possibilities == 0){
                    break;
                }
                val += possibilities;
            }
        }
        table[residue][numTerms][minBase][maxDistLeft] = val;
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
        int startingMultiplicity = modulo * (numTerms - 1) + residue;
        for (int i = 0; i < numTerms; i++){
            val += (i + 1) * (startingMultiplicity);
            startingMultiplicity -= modulo;
        }
        return val;
    }

    static ThreadVariables findIndex(int index, int numTerms, int residue){
        int[] bases = findIndexInternal(index, numTerms, residue);
        ThreadVariables t = new ThreadVariables();
        int lastSeen = 0;
        for (int i = 0; i < bases.length; i++){
            t.addSorted(bases[i], lastSeen);
            lastSeen = bases[i];
        }
        return t;
    }

    static int[] findIndexInternal(int index, int numTerms, int residue){
        int[] guess = new int[numTerms];
        guess = finishGuess(guess, 1, 0);
        //the first time we get an index greater than the desired index means the number has gone one too far
        int minimumBaseGuess = 1;
        for (int currentTermIndex = 0; currentTermIndex < numTerms; currentTermIndex++) {
            int currentBaseGuess = minimumBaseGuess;
            while (true) {
                guess = finishGuess(guess, currentBaseGuess, currentTermIndex);
                int ind = getIndex(guess, residue);
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

    static int getIndex(int[] bases, int residue){
        int numTerms = bases.length;
        int startingMultiplicity = modulo * (numTerms - 1) + residue;
        int currentDistLeft = MAX;
        int spacesSkipped = 0;
        int minBase = 1;
        for (int i = 0; i < bases.length; i++){
            int currentBase = bases[i];
            for (int j = minBase; j < bases[i]; j++){
                spacesSkipped += computeValue(j + 1, currentDistLeft - startingMultiplicity * j, numTerms - 1, residue);
            }
            currentDistLeft -= currentBase * startingMultiplicity;
            startingMultiplicity -= modulo;
            numTerms--;
            minBase = currentBase + 1;
        }
        return spacesSkipped;
    }

    static int[] finishGuess(int[] guess, int minBase, int location){
        for (int i = location; i < guess.length; i++){
            guess[i] = minBase + (i - location);
        }
        return guess;
    }

}
