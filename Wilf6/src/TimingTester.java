import java.util.ArrayList;
import java.util.Collections;

public class TimingTester {

    static ArrayList<Integer> testValues;
    static int valuesToAdd;
    static String[] arguments;

    //what about a queue and add +- 1 to every value if new minimum
    //10% of minimum?
    //remove if minimum changed?
    //start with low high middle combinations?

    public static void main(String [] args){
        valuesToAdd = Integer.parseInt(args[0]);
        arguments = new String[valuesToAdd + 2];
        arguments[0] = args[1];
        arguments[1] = args[2];
        int num = Integer.parseInt(args[1]);
        int[] basicValues = new int[] {2,3,4,5,6,7,8,9,10};
        int[] divisionValues = new int[]{3,4,5,6,7,8,9,10};
        testValues = new ArrayList<Integer>() ;
        for (int p : basicValues){
            testValues.add(p);
        }
        for (int j : divisionValues){
            testValues.add(num / j);
        }
        if (valuesToAdd == 1){
            testValues.add(1);
            testValues.add(num/2);
        }
        Collections.sort(testValues);
        for (int j = 1; j < testValues.size(); j++){
            if (testValues.get(j).equals(testValues.get(j - 1))){
                testValues.remove(j);
                j--;
            }
        }
        System.out.print("Total time (s), ");
        for (int i = 0; i < valuesToAdd; i++) {
            System.out.print("Cache number, Total hit count, Total hit time (s), Average hit time (ns), Total miss count, Total miss time (s), Average miss time (ns),");
        }
        System.out.println("Total collected time (s)");
        recurse(0,0);
    }

    public static void recurse(int currentSize, int currentIndex){
        if (currentSize == valuesToAdd){
            run(arguments);
            return;
        }
        for (int i = currentIndex; i < testValues.size(); i++){
            arguments[currentSize + 2] = "" + testValues.get(i);
            recurse(currentSize + 1, i + 1);
        }
    }



    public static void run(String[] arguments){
        WilfComplex2.main(arguments);
        /*for (int i = 2; i < arguments.length; i++){
            System.out.printf("%s ", arguments[i]);
        }*/
        //System.out.println();
        //WilfComplex2.statCollector.reset();
    }
}
