public class SumCounts {

    VirtualFileObject[] countsFiles;
    int maxNumTerms;

    //I mean this could be split by numTerms or groups if you really wanted
    public SumCounts(int maxNumTerms){
        this.maxNumTerms = maxNumTerms;
        countsFiles = new VirtualFileObject[maxNumTerms + 1];
        for (int i = 0; i <= maxNumTerms; i++){
            countsFiles[i] = new VirtualFileObject(Script.counts.get(i));
        }
    }

    public void runProcess(){
        int[] countsTotals = new int[Script.MAX + 1];
        for (int n = 0; n <= maxNumTerms; n++){
            VirtualFileObject currentFile = countsFiles[n];
            while (!currentFile.eof()){
                VirtualFileEntry e = currentFile.getNext();
                for (int i = e.minSum; i <= Script.MAX; i++){
                    countsTotals[i] += e.values[i];
                }
            }
        }
        for (int i = 0; i <= Script.MAX; i++){
            System.out.println(i + ": " + countsTotals[i]);
        }
    }

}
