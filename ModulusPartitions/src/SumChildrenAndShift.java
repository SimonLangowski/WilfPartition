import java.util.ArrayList;

public class SumChildrenAndShift {

    int numTerms;
    int minIndex;
    int maxIndex;
    int residue = 2;
    VirtualFile destinationFile;
    VirtualFileObject sourceFileChildren;
    VirtualFileObject sourceFileDestination;

    public SumChildrenAndShift( int numTerms, int minIndex, int maxIndex){
        this.numTerms = numTerms;
        this.minIndex = minIndex;
        this.maxIndex = maxIndex;
        sourceFileDestination = new VirtualFileObject(Script.distToValues.get(numTerms));
        destinationFile = Script.trieSums.get(numTerms);
    }

    public void runSubProcess(){
        goToDestIndex();
        int currentIndex = minIndex;
        while (currentIndex < maxIndex) {
            //output currentValues - maybe pass the index and values to a thread?
            VirtualFileEntry parentEntry = sourceFileDestination.getNext();
            int bsump = getBaseSum(parentEntry.bases);
            if (parentEntry.minSum + bsump * (residue - 1) <= Script.MAX) {
                int[] currentValues = new int[Script.MAX + 1];
                addShifted(currentValues, parentEntry.values, parentEntry.minSum, bsump * (residue - 1));
                createOutput(currentIndex, currentValues, parentEntry);
                currentIndex++;
            }
        }
    }

    public void runProcess(){
        sourceFileChildren = new VirtualFileObject(Script.trieSums.get(numTerms + 1));
        findFirstCorrespondingIndex();
        //some are leaves in the current layer
        //I can use the table to calculate how many children I have, and then take for granted that they are next in the source.
        int currentIndex = minIndex;
        while (currentIndex < maxIndex){

            //the skipping must take place when reading from the distToValues file = sourceFileDestination
            //not when reading from sourceFileChildren which is the trieSums

            VirtualFileEntry parentEntry = sourceFileDestination.getNext();
            int bsump = getBaseSum(parentEntry.bases);
            if (parentEntry.minSum + bsump * (residue - 1) <= Script.MAX) {
                int[] currentValues = new int[Script.MAX + 1];
                addShifted(currentValues, parentEntry.values, parentEntry.minSum, bsump * (residue - 1));
                int minBase = 0;
                if (numTerms > 0) {
                    minBase = parentEntry.bases.get(numTerms - 1);
                }
                //value and indexes from table for evens!
                //alternatively could just check if each child is my child
                int doubleShiftedBsum = Script.MAX - (parentEntry.minSum + bsump * (Script.modulo + residue - 1));
                if (doubleShiftedBsum >= 0) {
                    int numChildren = (int) Script.computeValue(minBase + 1, doubleShiftedBsum, 1, residue);
                    for (int i = 0; i < numChildren; i++) {
                        VirtualFileEntry currentChildEntry = sourceFileChildren.getNext();
                        for (int j = currentChildEntry.minSum; j <= Script.MAX; j++){
                            currentValues[j] += currentChildEntry.values[j];
                        }
                    }
                }
                createOutput(currentIndex, currentValues, parentEntry);
                currentIndex++;
            }
        }
    }

    public void createOutput(int index, int[] values, VirtualFileEntry destParent){
        ArrayList<Integer> destBases = destParent.bases;
        destinationFile.entries.add(new VirtualFileEntry(index, destBases, destParent.minSum, values));
    }

    public void addShifted(int[] destination, int[] source, int minSum, int shift){
        for (int i = minSum; i <= Script.MAX - shift; i++){
            destination[i + shift] += source[i];
        }
    }

    public static int getBaseSum(ArrayList<Integer> bases){
        int sum = 0;
        for (Integer i : bases){
            sum += i;
        }
        return sum;
    }

    public void findFirstCorrespondingIndex(){
        //find index of first child of minIndex in sourceFile
        goToDestIndex();
        ThreadVariables myFirstBases = Script.findIndex(minIndex, numTerms, 2);
        int maxBase = myFirstBases.decreasing[myFirstBases.size];
        myFirstBases.addSorted(maxBase + 1, maxBase);
        int firstSourceIndex = (int) Script.getIndex(myFirstBases, numTerms + 1, 2).index;
        sourceFileChildren.goToIndex(firstSourceIndex);
    }

    public void goToDestIndex(){
        sourceFileDestination.goToIndex(minIndex);
    }
}
