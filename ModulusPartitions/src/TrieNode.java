public class TrieNode {
    /**********Trie Section*********/
//An R - way Trie stores the cached partitions
//The partitions are accessed with a sequence of bases in descending order
//Each node contains an array of long values indexed by the remaining distance left
    int[] values;
    TrieNode[] children;
    int minBase;
    int myBaseMaxDistance;
    int myBsum;
    int maxBase;

    public TrieNode(int minBase, int maxD, int bsum) {
        this.minBase = minBase;
        this.myBaseMaxDistance = maxD;
        this.myBsum = bsum;
        this.maxBase = (this.myBaseMaxDistance - this.myBsum * Script.modulo) / 2;
        if (maxBase >= minBase){
            this.children = new TrieNode[maxBase - minBase + 1];
        }
    }

    public TrieNode getChild(int base){
        return children[base - minBase];
    }

    public void setChild(int base, TrieNode child){
        children[base - minBase] = child;
    }

    public static TrieNode getTrieHead(){
        return new TrieNode(1, Script.MAX, 0);
    }

    public static TrieNode getInternal(TrieNode current, int base, ThreadVariables t) {
        if (base == t.size) {
            return current;
        }
        if (base > current.maxBase){
            return null;
        }
        if (current.getChild(base) == null) {
            int newNodeDist = current.myBaseMaxDistance - current.myBsum * Script.modulo - base * ComputeDistFromAndCounts.fromResidue;
            current.setChild(base, new TrieNode(base + 1, newNodeDist, current.myBsum + base));
        }
        return getInternal(current.getChild(base), t.increasing[base], t); //decreasing[base] contains the next base in decreasing order
    }
}