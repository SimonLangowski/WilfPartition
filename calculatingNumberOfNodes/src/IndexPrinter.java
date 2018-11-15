public class IndexPrinter {

    static int MAX = 300;
    static int cutoff = 4;
    static long[][][] table;
    static int targetNumTerms;

    public static void main(String[] args){
        String[] passArgs = new String[0];
        Calculate.main(passArgs);
        table = Calculate.table;
        int maxNumTerms = Calculate2.computeInverse(MAX, cutoff);
        ThreadVariables trace = new ThreadVariables(MAX + 1);
        for (int i = 1; i <= maxNumTerms; i++){
            targetNumTerms = i;
            //int minVal = Calculate2.getMinVal(targetNumTerms, cutoff);
            //minBsum = 1,3,6,10,15...
            //recurseTree(trace, 0, 0, MAX - minVal, 0);
            recurseTree(trace, 0, 0, MAX, 0);
        }
    }


    public static void recurseTree(ThreadVariables current, int lastSeen, int totalTerms, int maxDistLeft, int bsum){
        if (totalTerms < targetNumTerms) {
            maxDistLeft -= (bsum); //shift previous up
            bsum += (lastSeen + 1); //add new base to bsum
            //should already be at min multiplicity? - but then things before should already be shifted up - need to use a minBsum?
            //long index1 = getIndex(finishGuess); //this may be invalid
            //long cycleLength = computeValue();
            //System.out.println("Predicted cycle from "  + index1 + " to " + index2);
            maxDistLeft -= (lastSeen + 1) * cutoff;
            for (int b = lastSeen + 1; maxDistLeft >= 0; b++) {
                current.addSorted(b, lastSeen);
                recurseTree(current, b, totalTerms + 1, maxDistLeft, bsum);
                current.deleteAtIndex(b);
                bsum++; //increment the base
                maxDistLeft -= cutoff; //decrease maxDistLeft
            }
        } else {
            System.out.print(current.toString() + " " +  getIndex(current, targetNumTerms) + ": ");
            int currentBase = current.increasing[0];
            int lastBase = 0;
            while (currentBase != current.size){
                current.deleteAtIndex(currentBase);
                System.out.print(getIndex(current, targetNumTerms - 1) + ",");
                current.addSorted(currentBase, lastBase);
                lastBase = currentBase;
                currentBase = current.increasing[currentBase];
            }
            System.out.println();
        }
    }

    static long getIndex(ThreadVariables t, int numTerms){
        int startingMultiplicity = numTerms + cutoff - 1;
        int currentDistLeft = MAX;
        long spacesSkipped = 0;
        int minBase = 1;
        int currentBase = t.increasing[0];
        while (currentBase < t.size){
            for (int j = minBase; j < currentBase; j++){
                //spacesSkipped += computeValue(j + 1, currentDistLeft - startingMultiplicity * j, numTerms - 1);
                spacesSkipped += computeValue(numTerms - 1, j + 1, currentDistLeft - startingMultiplicity * j);
            }
            currentDistLeft -= currentBase * startingMultiplicity;
            startingMultiplicity--;
            numTerms--;
            minBase = currentBase + 1;
            currentBase = t.increasing[currentBase];
        }
        return spacesSkipped;
    }

    static long computeValue(int numTerms, int minBase, int distLeft){
        return Calculate.computeValue(minBase, distLeft, numTerms);
    }

}
