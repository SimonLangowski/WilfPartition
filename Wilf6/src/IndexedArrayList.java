import java.util.ArrayList;

public class IndexedArrayList<T> {

    ArrayList<T> myElements;
    int myCurrentSize;

    public IndexedArrayList(){
        this(10);
    }

    public IndexedArrayList(int size){
        myCurrentSize = size;
        myElements = new ArrayList<T>(size);
        for (int i = 0; i < myCurrentSize; i++){
            myElements.add(null);
        }
    }

    public int getMyCurrentSize() {
        return myCurrentSize;
    }

    public T get(int index){
        while (index >= myCurrentSize){
            myElements.add(null);
            myCurrentSize++;
        }
        return myElements.get(index);
    }

    public void put(int index, T element){
        while (index >= myCurrentSize){
            myElements.add(null);
            myCurrentSize++;
        }
        myElements.set(index, element);
    }

    public ArrayList<T> getUnderlyingList(){
        return myElements;
    }

}
