import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class TableIO {

    public static String fileName = "table.txt";
    public static PrintWriter fileWriter;
    public static BufferedReader fileReader;

    public static ArrayList<ArrayList<Long>> readTable(){
        ArrayList<ArrayList<Long>> tempTable = new ArrayList<ArrayList<Long>>();
        try {
            fileReader = new BufferedReader(new FileReader(fileName));
            String line = fileReader.readLine();
            while (line != null){
                ArrayList<Long> tempLine = new ArrayList<Long>();
                Scanner stringReader = new Scanner(line);
                while (stringReader.hasNext()){
                    tempLine.add(stringReader.nextLong());
                }
                tempTable.add(tempLine);
                line = fileReader.readLine();
            }
            return tempTable;
        } catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    public static void writeTable(ArrayList<ArrayList<Long>> tableToWrite){
        try{
            if (fileWriter == null){
                fileWriter = new PrintWriter(new BufferedWriter(new FileWriter(new File(fileName))));
            }
            for (int i = 0; i < tableToWrite.size(); i++){
                for (int j = 0; j < tableToWrite.get(i).size(); j++){
                    fileWriter.printf("%d ", tableToWrite.get(i).get(j));
                }
                fileWriter.print('\n');
            }
            fileWriter.flush();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

}
