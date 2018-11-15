import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class Git3ScriptMaker {

    static int MAX;
    static int cutoff;
    static int fileSize = 500000;
    static int numberOfJobs = 10;
    static long [][][] table;
    static String executable = "./optimized";
    static String outputFile = "PartitionCounts.txt";
    static String sumProgrem = "./sumCounts";
    public static void main(String[] args){
        if (args.length < 2){
            System.out.println("Bad arguments");
            return;
        }
        MAX = Integer.parseInt(args[0]);
        cutoff = Integer.parseInt(args[1]);
        if (args.length >= 3){
            fileSize = Integer.parseInt(args[2]);
        }
        int maxNumTerms = computeInverse(MAX, cutoff);
        table = new long[maxNumTerms + 1][MAX / cutoff + 1][MAX + 1];
        String fileName = MAX + "-" + cutoff + "script.sh";
        File scriptFile = new File(fileName);
        try {
            PrintWriter scriptWriter = new PrintWriter(scriptFile);
            scriptWriter.println("set -e");
            for (int numTerms = 0; numTerms <= maxNumTerms; numTerms++) {
                scriptWriter.println(getMakeDirectoryCommand(numTerms));
                long maxIndex = computeValue(1, MAX, numTerms);
                long numFiles = maxIndex / fileSize;
                if (maxIndex != 0){
                    numFiles++;
                }
                String processScriptFile = "" + (numTerms) + "TermJobs.sh";
                File processFile = new File(processScriptFile);
                PrintWriter processWriter = new PrintWriter(processFile);
                for (int i = 0; i < numFiles; i++) {
                    processWriter.println(executable + " " + numTerms + " " + i);
                }
                processWriter.flush();
                processWriter.close();
                scriptWriter.println("echo \"Beginning " + numFiles + " blocks with " + (numTerms) + " terms\"");
                scriptWriter.println("cat " + processScriptFile + " | parallel -j" + numberOfJobs + " --delay 1 --halt soon,fail=1");
            }
            scriptWriter.println(sumProgrem + " " + MAX + " " + cutoff + " " + 1 + " > " + outputFile);
            scriptWriter.println("cat " + outputFile);
            scriptWriter.flush();
            scriptWriter.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    static String getMakeDirectoryCommand(int numTerms){
        return "mkdir -p ./" + numTerms;
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
