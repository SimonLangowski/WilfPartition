public class ThreadVariables {
    int[] increasing;
    int[] decreasing;
    int size;

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

}
