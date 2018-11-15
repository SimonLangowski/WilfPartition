# WilfPartition
##Purpose
Wilf Partitions are a combinatorial/mathematical object of potential interest to the field.  To better understand Wilf Partitions, it helps to have a way to compute them.  In particular, this program computes the number of possible Wilf Partitions of each integer in order to allow analysis of the growth rate.  It also helps to generate data on certain “families” of Wilf Partitions, and find similarities and patterns between them that might lead to faster computation and/or mathematical formulas.
##What exactly is a Wilf Partition? 
A partition of a positive integer is a representation as a sum of positive integers.  For example `3 = 1+1+1` or `4 = 2+1+1`.
The number of times a number appears in a partition is called its multiplicity, and the numbers themselves are called the bases.  A Wilf Partition is a partition with the property that each number has a distinct multiplicity.  So `6 = 2+2+1+1` is not a Wilf Partition while `6 = 3+1+1+1` is.
##The Basic Algorithm
1. Compute Wilf Partitions where all multiplicities are greater than or equal to a number, defined as CUTOFF in main.h
2. For each specific set of bases used in part 1, compute Wilf Partitions with bases strictly less than CUTOFF, and that do not overlap with the set of bases from part 1
3. Multiply the possibilities together.  The FLINT library is used to perform fast polynomial multiplication.
##The details
###Part 1 implementation:
Note that increasing the multiplicity of each number in a Wilf Partition by 1 generates another valid Wilf Partition, and the value of this Wilf Partition is exactly the sum of the bases in the partion larger than before.  So given a Wilf Partition with a set of bases, one can create a family of Wilf Partitions from it by repeatedly increasing the multiplicities.  By going in reverse, one of the multiplicites will become zero (since they are distinct, this will occur with exactly one base), and this generates a Wilf Partition with one less base.  The recursion is to start with the Wilf Partition of one less base, and add the base that was deleted.  By considering all possible deletions, all possible Wilf Partitions for a set of bases are counted.
A demonstration of the Algorithm can be found in WilfCCopy/distanceToTest.java which is a Java program that simply brute forces the components, and shows that the recurrence yields the same value.  In the C code, this is performed by calling the decompressAdd2 function from distToAndData.c on the correct data location from line 142 of main.c  This is simply an aliased for loop that also decompresses the data.  Each entry represents the counts for a set of bases as the coefficients of a polynomial as a generating function.
###Part 2 implementation:
Precompute.c computes all Wilf Partitions with multiplicities strictly less than CUTOFF.  This data is then used to perform Principle of Inclusion Exclusion to remove the overlaps.  The operations are done with polynomial multiplication and addition, and take advantage of the Wilf Involution.  The number of Wilf Partitions with multiplicities strictly less than CUTOFF exactly equals the number of Wilf Partitions with bases strictly less than CUTOFF.  Hence, the algorithm from part 1 applies, and can be used to generate the precompute data as well.  A demonstration of the algorithm can be found in DistFromPIE in Java and DistFromPoly.c in C.
### C implementation details
The program is multithreaded, and uses bounded buffers and semaphores for coordination.  
- IndexThread.c computes which bases are going to be used for Part 1, and excluded for Part 2.  Every base sequence in assigned an index.  For example, 1,2,3,4 could be assigned 0, and 1,2,3,5 assigned 1, and so on.  This is implemented through a table lookup in computeBorders.c  The program is designed to have multiple processes running in parallel, that each start and end on different indexes, which are referred to as block numbers.
- MemoryManager.c prefetches pages from disk, and was important at a time when the recursion for part 1 used terrabytes of data.  However, with improvements in the Algorithm for part 2, the CUTOFF number has been increased, and such complexity is no longer necessary.  It works by simply computing the next indexes, similar to indexThread.c, but in larger jumps and finding the necessary file locations.  Then it maintains a linked list and uses a merging algorithm to merge consecutive and close entries, which are then mmaped.  Half of the memory is “in use”, while the next half is being loaded.  When the boundary is reached, the regions are flipped, with the loaded part becoming “in use”, and the other section being loaded.
- OutputThread.c ensures the output data is in sequential order, so that lookup is efficient for the next process.
###The Other Folders
Algorithms are first implemented and tested in Java to ensure they are correct.  Then when they are implemented in C, it remains only to worry about the details of memory, process, and disk management.  It also makes it easy to simultaneously debug in Java and C and compare exactly where values differ in order to find bugs.  I’ve included these additional Java files for reference.  Descriptions of the other folders:  CalculatingNumberOfNodes implements the algorithms in computeBorders.c, Wilf 4 is an additional slower algorithm, ModulusPartitions is the above algorithm, but part 1 and 2 are split by odd and even multiplicities – meaning that adding 1 to each multiplicity allows one to easily compute the other part, but still needs PIE and has generally more nodes than the current version.
##How to Run this
- Use the file calculatingNumberOfNodes/GrowthOfCutoffs.java to compute the optimal value of CUTOFF for a MAX
- Change the root file path and CUTOFF and MAX parameters in calculatingNumberOfNodes/Wilf6ScriptMaker.java.  The default is compile commands for unoptimized code.
- Change the root file path and CUTOFF and MAX and set POWEROFTWO to `2^(CUTOFF - 1)` in main.h  In addition set the NUMTHREADS to your desired number of threads, and ensure that your ram available is larger than NUMTHREADS*CACHESIZE
- Run the first compile command in the script, also create directories and ensure libflint is on the loader path
- Run the precompute by using arguments 0 1 0 0 0 on the executable in MAX-CUTOFF/0, and make sure to create the directory MAX-CUTOFF/0/1 first.
- Set PRECOMPUTEFILELENGTH in main.h to the length of block.data in MAX-CUTOFF/0/1
- Run the shell script created from the Java file
- The file finalCounts.txt will be available in MAX-CUTOFF/0/0 upon completion and will range from 0 to MAX
A sample script is 1000-11script.sh, and a sample run is contained in the folder 1000-11
##Acknowledgements
- Special thanks to Dr. Mark Daniel Ward
- This research was supported by NSF grant 1246818


