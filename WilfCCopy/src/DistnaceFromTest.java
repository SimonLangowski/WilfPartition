import java.util.ArrayList;

public class DistnaceFromTest {
/*
    static int MAX = 100;
    static int masterCutoff = MAX + 1; //won't be used right now

    public static void main(String[] args) {
        int[] cutoffs = new int[MAX - 3 + 1];
        for (int i = 0; i < cutoffs.length; i++){
            cutoffs[i] = i + 3;
        }
        trie.initTrie(cutoffs.length, cutoffs, MAX);
        //change what cache values are to be inclusive or exclusive of the highest multiplicity
        //then next multiplicity is computed by sum over below nodes (multiplicity below from node below)[excludes adding pair] + (multiplicity below from node below shifted base*multiplicity)[adds pair]
        ThreadVariables t = new ThreadVariables(MAX + 2);
        recurseTrie(trie.trie, t, 0);

    }

    static void recurseTrie(trie node, ThreadVariables location, int lastAdded){
        if (node.children.length == 0){
            updateNodesFrom(node, location);
        }
        for(int i = 0; i < node.children.length; i++){
            int base = i + node.minBase;
            location.addSorted(base, lastAdded);
            if (node.children[i] == null){
                node.children[i] = new trie(base + 1, node.myBaseMaxDistance - node.myBSum, base);
            }
            recurseTrie(node.children[i], location, base);
            location.deleteAtIndex(base);
        }
    }

    static void updateNodesFrom(trie node, ThreadVariables t){
        int maxMultiplicity = masterCutoff - 1;
        long[] tempVals = ImplementingMultiplication2.q2All(trie.getMaxVal(node, 0), t);
        int currentBase = t.increasing[0];
        int lastBase = 0;
        while (currentBase != t.size) {
            t.deleteAtIndex(currentBase);
            trie updateNode = trie.get(t);
            for (int multiplicity = maxMultiplicity; multiplicity >= 3; multiplicity--) {
                if (updateNode.values.get(multiplicity - 3) == null){
                    maxMultiplicity--;
                    continue;
                }
                if (multiplicity > 3) {
                    addArrays(updateNode.values.get(multiplicity - 3), shiftOffset(node.values.get(multiplicity - 1), currentBase * multiplicity));
                } else {
                    addArrays(updateNode.values.get(multiplicity - 3), shiftOffset(tempVals, currentBase * multiplicity));
                }
            }
            t.addSorted(currentBase, lastBase);
            lastBase = currentBase;
        }
    }

    static void addArrays(long[] destination, long[] input){
        if (destination.length != input.length){
            throw new IllegalArgumentException();
        }
        for (int i = 0; i < destination.length; i++){
            destination[i] += input[i];
        }
    }

    static long[] shiftOffset(long[] input, int offset){
        long[] output = new long[input.length + offset];
        for (int i = output.length - 1; i >= input.length; i--){
            output[i] = input[i - offset];
        }
        for (int i = input.length - 1; i >= offset; i--){
            output[i] = input[i] + input[i - offset];
        }
        return output;
    }*/
}
