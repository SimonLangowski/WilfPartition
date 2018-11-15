import java.util.ArrayList;

public class SuperSavvyTrieHelper {

    SuperSavvyTrieNode head;
    int maxBase;

    public SuperSavvyTrieHelper(int maxBase){
        this.maxBase = maxBase;
        head = new SuperSavvyTrieNode(maxBase + 1);
    }

    public void put(ArrayList<Integer> bases, Long value){
        int index;
        for (index = bases.size() - 1; index >= 0; index--){
            if (bases.get(index) <= maxBase){
                break;
            }
        }
        put(head, bases, index, value);
    }

    public Long get(ArrayList<Integer> bases){
        int index;
        for (index = bases.size() - 1; index >= 0; index--){
            if (bases.get(index) <= maxBase){
                break;
            }
        }
        SuperSavvyTrieNode x = get(head, bases, index);
        if (x == null){
            return null;
        }
        return x.value;
    }

    private class SuperSavvyTrieNode{
        Long value;
        SuperSavvyTrieNode[] children;

        private SuperSavvyTrieNode(int base){
            children = new SuperSavvyTrieNode[base];
        }

    }


    private SuperSavvyTrieNode put(SuperSavvyTrieNode x, ArrayList<Integer> bases, int index, Long value){
        if (x == null){
            x = new SuperSavvyTrieNode(bases.get(index + 1));
        }
        if (index == -1){
            x.value = value;
            return x;
        }
        x.children[bases.get(index)] =  put(x.children[bases.get(index)], bases, index - 1, value);
        return x;
    }

    private SuperSavvyTrieNode get(SuperSavvyTrieNode x, ArrayList<Integer> bases, int index){
        if (x == null){
            return null;
        }
        if (index == -1){
            return x;
        }
        return get(x.children[bases.get(index)], bases, index - 1);
    }
}
