import com.sun.istack.internal.Pool;

import java.util.ArrayList;

public class trie {
/**********Trie Section*********/
//An R - way Trie stores the cached partitions
//The partitions are accessed with a sequence of bases in descending order
//Each node contains an array of long values indexed by the remaining distance left
    static trie trie;
    int myBaseMaxDistance;
    int myBSum;
    long[][] values;
    long[][] values2;
    trie[] children;
    int minBase;

    static int cutoffLength;
    static int[] cutoffs;

    public trie(int minBase, int maxDistanceNumber, int bSum) {
        this.minBase = minBase;
        this.myBaseMaxDistance = maxDistanceNumber;
        this.myBSum = bSum;
        values = new long[cutoffLength][];
        this.values2 = new long[cutoffLength][];
        int maxBase = (this.myBaseMaxDistance - (cutoffs[0] + 1) * this.myBSum) / cutoffs[0]; //max base given for smallest cutoff
        if (maxBase >= minBase) {
            this.children = new trie[maxBase - minBase + 1];
        }
    }

    public trie getChild(int base){
        return children[base - minBase];
    }

    public void setChild(int base, trie child){
        children[base - minBase] = child;
    }

    static void initTrie(int cutoffLength2, int[] cutoffs2, int MAX) {
        //head node of trie
        cutoffLength = cutoffLength2;
        cutoffs = cutoffs2;
        trie = new trie(1, MAX, 0);
    }

    public static trie getInternal(trie current, int base, ThreadVariables t) {
        if (base == t.size) {
            return current;
        }
        if (current.getChild(base) == null) {
                int newNodeDist = current.myBaseMaxDistance - (current.myBSum);
                if (current.getChild(base) == null) { //ensure no memory leaks :D
                    current.setChild(base, new trie(base + 1, newNodeDist, current.myBSum + base));
                }
        }
        return getInternal(current.getChild(base), t.increasing[base], t); //decreasing[base] contains the next base in decreasing order
    }

//returns the last number that has had the trie value added to it - 1 less than where to start computing

    static trie get(ThreadVariables t) {
        return getInternal(trie, t.increasing[0], t);
    }

    static void put(trie current, long[] putMe, int cutoff) {
        if (current.values[cutoff] == null) {
            current.values[cutoff] = putMe;
        }
    }

    static void put2(trie current, long[] putMe, int cutoff) {
        if (current.values2[cutoff] == null) {
            current.values2[cutoff] = putMe;
        }
    }

    static void clearNode(trie node) {
        node.values = null; //garbage collect will free rest
    }

    static int getMaxVal(trie node, int cutoffNum){
        return node.myBaseMaxDistance - (node.myBSum) * (cutoffs[cutoffNum]);
    }

    static int countAndPrintChildren(trie node, ThreadVariables path, int base, int numTerms, int target){
        if (numTerms == target){
            print(path, 1);
            return 1;
        }
        if(node.children == null){
            //print(path, 0);
            return 0;
        }
        int count = 0;
        for (int i = 0; i < node.children.length; i++){
            if (node.children[i] != null) {
                path.addSorted(i + node.minBase, base);
                count += countAndPrintChildren(node.children[i], path, i + node.minBase, numTerms + 1, target);
                path.deleteAtIndex(i + node.minBase);
            } else {
                System.out.print("Null child?\n");
            }
        }
        print(path, count);
        return count;
    }

    static void print(ThreadVariables path, int count){
        if (count == 0){
            return;
        }
        int currentBase = path.increasing[0];
        System.out.printf("%d: ", count);
        while (currentBase != path.size){
            System.out.printf("%d, ", currentBase);
            currentBase = path.increasing[currentBase];
        }
        System.out.println();
    }
}