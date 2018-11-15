import java.util.ArrayList;

public class SavvyTrieHelper {

    SavvyTrieNode head;

    public SavvyTrieHelper(){
        head = new SavvyTrieNode(0);
    }

    public void put(ArrayList<Integer> bases, Long value){
        put(head, bases, 0, value);
    }

    public Long get(ArrayList<Integer> bases){
        SavvyTrieNode x = get(head, bases, 0);
        if (x == null){
            return null;
        }
        return x.value;
    }

    private class SavvyTrieNode{
        Long value;
        int offset;
        IndexedArrayList<SavvyTrieNode> children;

        private SavvyTrieNode(int base){
            this.offset = base + 1;
            children = new IndexedArrayList<SavvyTrieNode>();
        }

        private SavvyTrieNode getChild(int index){
            return children.get(index - offset);
        }

        private void setChild(int index, SavvyTrieNode child){
            children.put(index - offset, child);
        }

    }


    private SavvyTrieNode put(SavvyTrieNode x, ArrayList<Integer> bases, int index, Long value){
        if (x == null){
            x = new SavvyTrieNode(bases.get(index - 1));
        }
        if (index == bases.size()){
            x.value = value;
            return x;
        }
        x.setChild(bases.get(index), put(x.getChild(bases.get(index)), bases, index + 1, value));
        return x;
    }

    private SavvyTrieNode get(SavvyTrieNode x, ArrayList<Integer> bases, int index){
        if (x == null){
            return null;
        }
        if (index == bases.size()){
            return x;
        }
        return get(x.getChild(bases.get(index)), bases, index + 1);
    }
}
