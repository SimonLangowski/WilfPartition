import java.io.IOException;
import java.io.PrintWriter;

public class Wilf6ScriptMaker {

    static int MAX;
    static int cutoff;
    static long [][][] table;
    //static String rootDirectory = "/scratch/mentors/mdw/simon/wilf6/";
    static String compiler = "gcc";
    //static String rootDirectory = "/scratch/rice/s/slangows/wilf/";
    static String rootDirectory = "/scratch/mentors/mdw/simon/polyWilf7/";
    //static String compiler = "icc";
    static String files = "distFrom.c computeBorders.c distToAndData.c indexThread.c main.c memoryManager.c outputThread.c distFromPoly.c precompute.c";
    static String flint = "flint-2.5.2";
    static String locationFlags = "-L" + rootDirectory + flint + " -I" + rootDirectory + flint;
    static String libraryFlags = "-std=c11 -lpthread -lgmp " + locationFlags + " -lflint";
    static String commandFlags = "-g";
    static String executableName = "debug";
    static String sumName = "sumCounts";
    static long minimumBlockSize = 100000;
    static int targetBlockCount = 2;
    static int indexOffsetSize = 6;

    public static void main(String[] args){
        MAX = Integer.parseInt(args[0]);
        cutoff = Integer.parseInt(args[1]);
        try {
            if (args[2].trim().equals("o")){
                executableName = "optimized";
                commandFlags = "-O3";
            } else if (args[2].trim().equals("p")){
                executableName = "profile";
                commandFlags = "-g -pg";
            } else if (args[2].trim().equals("g")){
                executableName = "debug";
                commandFlags = "-g";
            }
        } catch (ArrayIndexOutOfBoundsException e){

        }
        int maxNumTerms = computeInverse(MAX, cutoff);
        table = new long[maxNumTerms + 1][MAX / cutoff + 1][MAX + 1];
        int[] numBlocks = new int[maxNumTerms + 1];
        long[] blockSizes = new long[maxNumTerms + 1];
        numBlocks[0] = 1;
        blockSizes[0] = 1;
        minimumBlockSize = (minimumBlockSize/indexOffsetSize + 1)*indexOffsetSize;
        for (int i = 1; i <= maxNumTerms; i++){
            long numIndexes = computeValue(1, MAX, i);
            if (numIndexes <= minimumBlockSize) {
                blockSizes[i] = minimumBlockSize;
                numBlocks[i] = 1;
            } else if (numIndexes > minimumBlockSize * targetBlockCount){
                long blockSizeUnrounded = (numIndexes / targetBlockCount);
                blockSizes[i] = (blockSizeUnrounded/indexOffsetSize + 1)*indexOffsetSize;
                numBlocks[i] = targetBlockCount;
            } else {
                long desiredNumBlocks = numIndexes / minimumBlockSize;
                long blockSizeUnrounded = numIndexes / desiredNumBlocks;
                blockSizes[i] = (blockSizeUnrounded/indexOffsetSize + 1)*indexOffsetSize;
                numBlocks[i] = (int) desiredNumBlocks;
            }
        }

        try {
            String fileName = MAX + "-" + cutoff + "script.sh";
            PrintWriter printWriter = new PrintWriter(fileName);
            printWriter.print(makeExportStatement());
            for (int i = 0; i <= maxNumTerms; i++){
                for (int j = 0; j < numBlocks[i]; j++){
                    printWriter.print(getMakeDirectoryCommand(i,j));
                }
            }
            printWriter.print(makeCompileCommand(0,1));
            for (int i = 1; i <= maxNumTerms; i++){
                printWriter.print(makeCompileCommand(i, numBlocks[i-1]));
            }
            printWriter.print(makeSumCompileCommand());
            for (int i = 0; i <= maxNumTerms; i++){
                for (int j = 0; j < numBlocks[i]; j++){
                    long minIndex = blockSizes[i] * j;
                    long maxIndex = blockSizes[i] * (j+1);
                    if (maxIndex > computeValue(1, MAX, i)){
                        maxIndex = computeValue(1, MAX, i);
                    }
                    printWriter.print(makeRunCommand(i,j,minIndex,maxIndex));
                }
                printWriter.print(makeLengthCommand(i));
            }
            printWriter.print(makeSumCommand());
            printWriter.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    static String makeExportStatement(){
        return "export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:" + rootDirectory + flint + "\n";
    }

    static String getMakeDirectoryCommand(int numTerms, int block){
        return "mkdir -p " + getDirectory(numTerms, block)+ "\n";
    }

    static String getDirectory(int numTerms, int block){
        return  rootDirectory + MAX + "-" + cutoff + "/" + numTerms + "/" + block;
    }

    static String makeLengthCommand(int numTerms){
        return "stat -c\"%s\" " + getDirectory(numTerms, 0) + "/block.data > " + getDirectory(numTerms,0) + "/length.txt\n";
    }

    static String makeCompileCommand(int numTerms, int prevNumBlocks){
        return compiler + " -DtargetNumTerms="+numTerms + " -DNUMBLOCKS="+prevNumBlocks + " " + files + " " + libraryFlags + " " + commandFlags + " -o " + getExectuable(numTerms) + "\n";
    }

    static String getExectuable(int numTerms){
        return rootDirectory  + MAX + "-" + cutoff + "/" + numTerms + "/" + executableName;
    }

    static String makeRunCommand(int numTerms, int block, long minIndex, long maxIndex){
        return getExectuable(numTerms) + " " + minIndex + " " + maxIndex + " " + numTerms + " " + block + " 0\n";
    }

    static String makeSumCompileCommand(){
        return compiler + " " + sumName + ".c " + libraryFlags + " " + commandFlags + " -o " + rootDirectory  + MAX + "-" + cutoff + "/" + sumName + "\n";
    }

    static String makeSumCommand(){
        return rootDirectory  + MAX + "-" + cutoff + "/" + sumName + "\n";
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
