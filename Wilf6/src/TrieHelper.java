import java.util.ArrayList;

public class TrieHelper {

    int maxValue;
    TrieNode head;

    public TrieHelper(int maxValue){
        this.maxValue = maxValue;
        head = new TrieNode();
    }

    public void put(ArrayList<Integer> bases, Long value){
        put(head, bases, 0, value);
    }

    public Long get(ArrayList<Integer> bases){
        TrieNode x = get(head, bases, 0);
        if (x == null){
            return null;
        }
        return x.value;
    }

    private class TrieNode{
        Long value;
        TrieNode[] children = new TrieNode[maxValue + 1];
    }

    private TrieNode put(TrieNode x, ArrayList<Integer> bases, int index, Long value){
        if (x == null){
            x = new TrieNode();
        }
        if (index == bases.size()){
            x.value = value;
            return x;
        }
        x.children[bases.get(index)] = put(x.children[bases.get(index)], bases, index + 1, value);
        return x;
    }

    private TrieNode get(TrieNode x, ArrayList<Integer> bases, int index){
        if (x == null){
            return null;
        }
        if (index == bases.size()){
            return x;
        }
        return get(x.children[bases.get(index)], bases, index + 1);
    }
}
