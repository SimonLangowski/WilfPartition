import java.lang.reflect.Array;
import java.util.ArrayList;

public class ThreadVariables {
    int[] increasing;
    int[] decreasing;
    int size;

    public ThreadVariables(ArrayList<Integer> bases){
        this(Script.MAX + 1, bases);
    }

    public ThreadVariables(){
        this(Script.MAX + 1);
    }

    public ThreadVariables(int size, ArrayList<Integer> bases){
        this(size);
        int lastBase = 0;
        for (int i = 0; i < bases.size(); i++){
            this.addSorted(bases.get(i), lastBase);
            lastBase = bases.get(i);
        }
    }

    public ThreadVariables(int size) {
        increasing = new int[size + 1];
        decreasing = new int[size + 1];
        this.size = size;
        increasing[0] = size;
        decreasing[0] = 0;
        decreasing[size] = 0;
        increasing[size] = size;
    }

    void addSorted(int index, int lastFull) {
        ThreadVariables t= this;
        t.increasing[index] = t.increasing[lastFull];
        t.decreasing[t.increasing[lastFull]] = index;
        t.decreasing[index] = lastFull;
        t.increasing[lastFull] = index;
    }

    void deleteAtIndex(int index) {
        ThreadVariables t= this;
        t.decreasing[t.increasing[index]] = t.decreasing[index];
        t.increasing[t.decreasing[index]] = t.increasing[index];
        t.increasing[index] = 0;
        t.decreasing[index] = 0;
    }

    int computeBsum(){
        int bsum = 0;
        int currentBase = increasing[0];
        while (currentBase != size){
            bsum += currentBase;
            currentBase = increasing[currentBase];
        }
        return bsum;
    }

    ThreadVariables copyThreadVariables(){
        ThreadVariables copy = new ThreadVariables(size);
        int currentBase = increasing[0];
        int lastBase = 0;
        while (currentBase != size){
            copy.addSorted(currentBase, lastBase);
            lastBase = currentBase;
            currentBase = increasing[currentBase];
        }
        return copy;
    }

    ArrayList<Integer> getBasesAsArrayList(){
        ArrayList<Integer> bases = new ArrayList<Integer>();
        int currentBase = increasing[0];
        while (currentBase != size){
            bases.add(currentBase);
            currentBase = increasing[currentBase];
        }
        return bases;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("[");
        int currentBase = increasing[0];
        while(currentBase != size){
            s.append((currentBase));
            if (increasing[currentBase] != size){
                s.append(" ");
            } else {
                s.append("]");
            }
            currentBase = increasing[currentBase];
        }
        return s.toString();
    }
}
