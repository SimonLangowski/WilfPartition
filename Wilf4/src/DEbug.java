import java.io.*;

public class DEbug {

    File mFile;
    File vFile;
    File bFile;

    public DEbug(int num){
        mFile = new File("Msums" + num + ".txt");
        vFile = new File("ValueMsumBsumNumTerms" + num + ".txt");
        bFile = new File("bpa" + num + ".txt");
    }

    public DEbug(){
        mFile = new File("Msums.txt");
        vFile = new File("ValueMsumBsumNumTerms.txt");
        bFile = new File("bpa.txt");
    }

    public void printValMsumBsumNumTerms(long[][][][] printMe){
        try {
            PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter(vFile)));
            for (int i = 0; i < printMe.length; i++) {
                for (int j = 0; j < printMe[0].length; j++) {
                    for (int k = 0; k < printMe[0][0].length; k++) {
                        for (int l = 0; l < printMe[0][0][0].length; l++) {
                            if (printMe[i][j][k][l] > 0) {
                                printWriter.printf("Value = %d, msum = %d, bsum = %d, numTerms = %d\t\t%d\n", i, j, k, l, printMe[i][j][k][l]);
                            }
                        }
                    }
                }
            }
            printWriter.flush();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void printValueMsun(long[][] printMe){
        try {
            PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter(mFile)));
        for (int i = 0; i < printMe.length; i++){
            for (int j = 0; j < printMe[0].length; j++){
                if (printMe[i][j] > 0){
                    printWriter.printf("Value = %d, msum = %d\t\t%d\n", i,j,printMe[i][j]);
                }
            }
        }
            printWriter.flush();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void printBPA(BPA[][] printMe){
        try {
            PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter(bFile)));
        for (int i = 0; i < printMe.length; i++){
            for (int j = 0; j < printMe[0].length; j++){
                long[][] bpaTerms = printMe[i][j].getNumTermsMsum();
                for (int k = 0; k < bpaTerms.length; k++){
                    for (int l = 0; l < bpaTerms[0].length; l++){
                        if (bpaTerms[k][l] > 0){
                            printWriter.printf("BPA nsum %d, remainder %d, msum: %d, numTerms: %d\t\t%d\n", i,j,l,k, bpaTerms[k][l]);
                        }
                    }
                }
            }
        }
            printWriter.flush();
    } catch (IOException e){
        e.printStackTrace();
    }
    }

}
