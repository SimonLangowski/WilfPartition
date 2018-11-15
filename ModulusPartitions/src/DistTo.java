public class DistTo {

    int targetNumTerms;
    int sourceNumTerms = targetNumTerms - 1;

    public static void main(String[] args){

    }

    //it would be helpful for checking the indexes if the minIndex was on a leftBoundary and the maxIndex was on a rightBoundary

    //I will request 500000 sequential indexes.  It will cycle unless the cycle has length greater than 500000 or the gap index increments immediately (inefficient!)
    //What is the most number of things I can store in memory under 20GB?  (20GB * 10 processes = 200GB, also good for queues)
    //for cycles of length greater than 250000, there's no need to store the whole thing as it won't be reaccessed
    //I could also shrink the number of indexes computed per process, at the expense of the number of files to open
    //need to allocate space for code, global variables, etc (probs small), but also that index table size! (technically only need up to targetNumTerms)
    //how big is a read object.  Am I compressing?  I probably should if I am going to store so many, do I need to store the bases?  Or just the indexes - just the data should be good, I'm thinking a large array of cmp*
    //my longest cycle is when I drop the first digit.  All of this data is contained in the cycle 2,3,4,5,6... to max which then starts at 3,4,5,6,7 the next time (a subset).  There is a significant amount not needed to be saved
    //the 2nd to last cycle will start 1,3,4,5... the cycle subsets are 1,4,5,6...  It's a different cycle beginning with 2 and 3
    //can I run through complete small cycles but skip around so that it is the same part of the larger cycle over and over? (I could just do multiple write blocks to account for the skips) (And if a write block size is proportional to the ram size, I probably won't read more than one in each time anyway)
    //i.e Something like prefix gap cycle -> prefix in larger cycle gap cycle -> so when going from one cycle to the next with the largest small cycle in ram, I always compute subtrees of that size, but the sub trees skip along to where the large cycle would repeat
    // Dropping the last digit doesn't cycle and just goes through the tree of one less term
    // All of the cycle sizes should be contained somewhere in the index table


    //I should write a program that prints out every index followed by its prerequisite indexes (even inefficiently)
    void traverseTree() {
        int baseCycleIndex; //index of first element in the cycle formed where the gap is immediately following this index, an index into (targetNumTerms - 1)
        int minBase;
        int currentSelectedBase;
        int maxBase;
        int termIndex;
        for (currentSelectedBase = minBase; currentSelectedBase <= maxBase; currentSelectedBase++){
            //allocate and request cycle cache be made (check max buffer size and max index)
            baseCycleIndex = ;
            readingThread[termIndex];
            //recurse
            //change to next cycle
        }

    }

    void computeLastTerm(){
        //get each component with each term removed
        //shift each and sum together
        //do the cummulative shift
        //output the result
        //repeat with all possible lastBases
    }
}
