public class DoublyLinkedArray {

    int[] increasing;
    int[] decreasing;

    DoublyLinkedArray(int size){
        increasing = new int[size + 1];
        decreasing = new int[size + 1];
        increasing[0] = size;
        decreasing[0] = 0;
        decreasing[size] = 0;
        increasing[size] = size;
    }

    public int[] getIncreasing(){
        return increasing;
    }

    public int[] getDecreasing(){
        return decreasing;
    }

    public boolean isIncluded(int index){
        return increasing[index] != 0;
    }

    public int addSorted(int index, int lastFull){
        increasing[index] = increasing[lastFull];
        decreasing[increasing[lastFull]] = index;
        decreasing[index] = lastFull;
        increasing[lastFull] = index;
        return index;
    }

    public void deleteAtIndex(int index){
        decreasing[increasing[index]] = decreasing[index];
        increasing[decreasing[index]] = increasing[index];
        increasing[index] = 0;
        decreasing[index] = 0;
    }
}
