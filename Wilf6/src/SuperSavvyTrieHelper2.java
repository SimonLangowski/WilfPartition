
public class SuperSavvyTrieHelper2 {

    SuperSavvyTrieNode head;
    int maxBase;
    static DoublyLinkedArray correspondance;
    static int[] decreasing;
    static int[] increasing;

    public SuperSavvyTrieHelper2(int maxBase){
        this.maxBase = maxBase;
        head = new SuperSavvyTrieNode(maxBase + 1);
    }

    public static void setCorrespondance(DoublyLinkedArray d){
        correspondance = d;
        decreasing = d.getDecreasing();
        increasing = d.getIncreasing();
    }

    public void put(Long value){
        /*
        int base = increasing[0];
        while (base <= maxBase){
            base = increasing[base];
        }
        base = decreasing[base];
        */

        int base = decreasing[decreasing.length - 1];
        while (base > maxBase){
            base = decreasing[base];
        }

        put(head, base, value);
    }

    public Long get(){
        /*
        int base = increasing[0];
        while (base <= maxBase){
            base = increasing[base];
        }
        base = decreasing[base];
        */

        int base = decreasing[decreasing.length - 1];
        while (base > maxBase){
            base = decreasing[base];
        }

        SuperSavvyTrieNode x = get(head, base);
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


    private SuperSavvyTrieNode put(SuperSavvyTrieNode x, int base, Long value){
        if (x == null){
            x = new SuperSavvyTrieNode(increasing[base]);
        }
        if (base == 0){
            x.value = value;
            return x;
        }
        x.children[base] =  put(x.children[base], decreasing[base], value);
        return x;
    }

    private SuperSavvyTrieNode get(SuperSavvyTrieNode x, int base){
        if (x == null){
            return null;
        }
        if (base == 0){
            return x;
        }
        return get(x.children[base], decreasing[base]);
    }
}
