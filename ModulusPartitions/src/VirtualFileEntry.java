import java.util.ArrayList;

public class VirtualFileEntry implements Comparable<VirtualFileEntry> {

    int index;
    ArrayList<Integer> bases;
    int minSum;
    int[] values;

    public VirtualFileEntry(int index, ThreadVariables t, int minSum, int[] values){
        this(index, t.getBasesAsArrayList(), minSum, values);
    }

    public VirtualFileEntry(int index, ArrayList<Integer> bases, int minSum, int[] values){
        this.index = index;
        this.bases = bases;
        this.minSum = minSum;
        this.values = values;
    }

    @Override
    public int compareTo(VirtualFileEntry o) {
        return index - o.index;
    }
}
