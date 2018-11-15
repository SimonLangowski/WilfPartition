export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/scratch/mentors/mdw/simon/wilf6/flint-2.5.2
mkdir -p /scratch/mentors/mdw/simon/wilf6/500-4/0/0
mkdir -p /scratch/mentors/mdw/simon/wilf6/500-4/1/0
mkdir -p /scratch/mentors/mdw/simon/wilf6/500-4/2/0
mkdir -p /scratch/mentors/mdw/simon/wilf6/500-4/3/0
mkdir -p /scratch/mentors/mdw/simon/wilf6/500-4/4/0
mkdir -p /scratch/mentors/mdw/simon/wilf6/500-4/5/0
mkdir -p /scratch/mentors/mdw/simon/wilf6/500-4/5/1
mkdir -p /scratch/mentors/mdw/simon/wilf6/500-4/6/0
mkdir -p /scratch/mentors/mdw/simon/wilf6/500-4/6/1
mkdir -p /scratch/mentors/mdw/simon/wilf6/500-4/7/0
mkdir -p /scratch/mentors/mdw/simon/wilf6/500-4/7/1
mkdir -p /scratch/mentors/mdw/simon/wilf6/500-4/8/0
mkdir -p /scratch/mentors/mdw/simon/wilf6/500-4/8/1
mkdir -p /scratch/mentors/mdw/simon/wilf6/500-4/9/0
mkdir -p /scratch/mentors/mdw/simon/wilf6/500-4/10/0
mkdir -p /scratch/mentors/mdw/simon/wilf6/500-4/11/0
gcc -DtargetNumTerms=0 -DNUMBLOCKS=1 distFrom.c computeBorders.c distToAndData.c indexThread.c main.c memoryManager.c outputThread.c -lpthread -lgmp -L/scratch/mentors/mdw/simon/wilf6/flint-2.5.2 -I/scratch/mentors/mdw/simon/wilf6/flint-2.5.2 -lflint -g -o /scratch/mentors/mdw/simon/wilf6/500-4/0/debug
gcc -DtargetNumTerms=1 -DNUMBLOCKS=1 distFrom.c computeBorders.c distToAndData.c indexThread.c main.c memoryManager.c outputThread.c -lpthread -lgmp -L/scratch/mentors/mdw/simon/wilf6/flint-2.5.2 -I/scratch/mentors/mdw/simon/wilf6/flint-2.5.2 -lflint -g -o /scratch/mentors/mdw/simon/wilf6/500-4/1/debug
gcc -DtargetNumTerms=2 -DNUMBLOCKS=1 distFrom.c computeBorders.c distToAndData.c indexThread.c main.c memoryManager.c outputThread.c -lpthread -lgmp -L/scratch/mentors/mdw/simon/wilf6/flint-2.5.2 -I/scratch/mentors/mdw/simon/wilf6/flint-2.5.2 -lflint -g -o /scratch/mentors/mdw/simon/wilf6/500-4/2/debug
gcc -DtargetNumTerms=3 -DNUMBLOCKS=1 distFrom.c computeBorders.c distToAndData.c indexThread.c main.c memoryManager.c outputThread.c -lpthread -lgmp -L/scratch/mentors/mdw/simon/wilf6/flint-2.5.2 -I/scratch/mentors/mdw/simon/wilf6/flint-2.5.2 -lflint -g -o /scratch/mentors/mdw/simon/wilf6/500-4/3/debug
gcc -DtargetNumTerms=4 -DNUMBLOCKS=1 distFrom.c computeBorders.c distToAndData.c indexThread.c main.c memoryManager.c outputThread.c -lpthread -lgmp -L/scratch/mentors/mdw/simon/wilf6/flint-2.5.2 -I/scratch/mentors/mdw/simon/wilf6/flint-2.5.2 -lflint -g -o /scratch/mentors/mdw/simon/wilf6/500-4/4/debug
gcc -DtargetNumTerms=5 -DNUMBLOCKS=1 distFrom.c computeBorders.c distToAndData.c indexThread.c main.c memoryManager.c outputThread.c -lpthread -lgmp -L/scratch/mentors/mdw/simon/wilf6/flint-2.5.2 -I/scratch/mentors/mdw/simon/wilf6/flint-2.5.2 -lflint -g -o /scratch/mentors/mdw/simon/wilf6/500-4/5/debug
gcc -DtargetNumTerms=6 -DNUMBLOCKS=2 distFrom.c computeBorders.c distToAndData.c indexThread.c main.c memoryManager.c outputThread.c -lpthread -lgmp -L/scratch/mentors/mdw/simon/wilf6/flint-2.5.2 -I/scratch/mentors/mdw/simon/wilf6/flint-2.5.2 -lflint -g -o /scratch/mentors/mdw/simon/wilf6/500-4/6/debug
gcc -DtargetNumTerms=7 -DNUMBLOCKS=2 distFrom.c computeBorders.c distToAndData.c indexThread.c main.c memoryManager.c outputThread.c -lpthread -lgmp -L/scratch/mentors/mdw/simon/wilf6/flint-2.5.2 -I/scratch/mentors/mdw/simon/wilf6/flint-2.5.2 -lflint -g -o /scratch/mentors/mdw/simon/wilf6/500-4/7/debug
gcc -DtargetNumTerms=8 -DNUMBLOCKS=2 distFrom.c computeBorders.c distToAndData.c indexThread.c main.c memoryManager.c outputThread.c -lpthread -lgmp -L/scratch/mentors/mdw/simon/wilf6/flint-2.5.2 -I/scratch/mentors/mdw/simon/wilf6/flint-2.5.2 -lflint -g -o /scratch/mentors/mdw/simon/wilf6/500-4/8/debug
gcc -DtargetNumTerms=9 -DNUMBLOCKS=2 distFrom.c computeBorders.c distToAndData.c indexThread.c main.c memoryManager.c outputThread.c -lpthread -lgmp -L/scratch/mentors/mdw/simon/wilf6/flint-2.5.2 -I/scratch/mentors/mdw/simon/wilf6/flint-2.5.2 -lflint -g -o /scratch/mentors/mdw/simon/wilf6/500-4/9/debug
gcc -DtargetNumTerms=10 -DNUMBLOCKS=1 distFrom.c computeBorders.c distToAndData.c indexThread.c main.c memoryManager.c outputThread.c -lpthread -lgmp -L/scratch/mentors/mdw/simon/wilf6/flint-2.5.2 -I/scratch/mentors/mdw/simon/wilf6/flint-2.5.2 -lflint -g -o /scratch/mentors/mdw/simon/wilf6/500-4/10/debug
gcc -DtargetNumTerms=11 -DNUMBLOCKS=1 distFrom.c computeBorders.c distToAndData.c indexThread.c main.c memoryManager.c outputThread.c -lpthread -lgmp -L/scratch/mentors/mdw/simon/wilf6/flint-2.5.2 -I/scratch/mentors/mdw/simon/wilf6/flint-2.5.2 -lflint -g -o /scratch/mentors/mdw/simon/wilf6/500-4/11/debug
gcc sumCounts.c -lpthread -lgmp -L/scratch/mentors/mdw/simon/wilf6/flint-2.5.2 -I/scratch/mentors/mdw/simon/wilf6/flint-2.5.2 -lflint -g -o /scratch/mentors/mdw/simon/wilf6/500-4/sumCounts
/scratch/mentors/mdw/simon/wilf6/500-4/0/debug 0 1 0 0 0
stat -c"%s" /scratch/mentors/mdw/simon/wilf6/500-4/0/0/block.data > /scratch/mentors/mdw/simon/wilf6/500-4/0/0/length.txt
/scratch/mentors/mdw/simon/wilf6/500-4/1/debug 0 125 1 0 0
stat -c"%s" /scratch/mentors/mdw/simon/wilf6/500-4/1/0/block.data > /scratch/mentors/mdw/simon/wilf6/500-4/1/0/length.txt
/scratch/mentors/mdw/simon/wilf6/500-4/2/debug 0 3389 2 0 0
stat -c"%s" /scratch/mentors/mdw/simon/wilf6/500-4/2/0/block.data > /scratch/mentors/mdw/simon/wilf6/500-4/2/0/length.txt
/scratch/mentors/mdw/simon/wilf6/500-4/3/debug 0 35528 3 0 0
stat -c"%s" /scratch/mentors/mdw/simon/wilf6/500-4/3/0/block.data > /scratch/mentors/mdw/simon/wilf6/500-4/3/0/length.txt
/scratch/mentors/mdw/simon/wilf6/500-4/4/debug 0 178979 4 0 0
stat -c"%s" /scratch/mentors/mdw/simon/wilf6/500-4/4/0/block.data > /scratch/mentors/mdw/simon/wilf6/500-4/4/0/length.txt
/scratch/mentors/mdw/simon/wilf6/500-4/5/debug 0 240486 5 0 0
/scratch/mentors/mdw/simon/wilf6/500-4/5/debug 240486 480969 5 1 0
stat -c"%s" /scratch/mentors/mdw/simon/wilf6/500-4/5/0/block.data > /scratch/mentors/mdw/simon/wilf6/500-4/5/0/length.txt
/scratch/mentors/mdw/simon/wilf6/500-4/6/debug 0 359922 6 0 0
/scratch/mentors/mdw/simon/wilf6/500-4/6/debug 359922 719834 6 1 0
stat -c"%s" /scratch/mentors/mdw/simon/wilf6/500-4/6/0/block.data > /scratch/mentors/mdw/simon/wilf6/500-4/6/0/length.txt
/scratch/mentors/mdw/simon/wilf6/500-4/7/debug 0 298986 7 0 0
/scratch/mentors/mdw/simon/wilf6/500-4/7/debug 298986 597963 7 1 0
stat -c"%s" /scratch/mentors/mdw/simon/wilf6/500-4/7/0/block.data > /scratch/mentors/mdw/simon/wilf6/500-4/7/0/length.txt
/scratch/mentors/mdw/simon/wilf6/500-4/8/debug 0 130242 8 0 0
/scratch/mentors/mdw/simon/wilf6/500-4/8/debug 130242 260483 8 1 0
stat -c"%s" /scratch/mentors/mdw/simon/wilf6/500-4/8/0/block.data > /scratch/mentors/mdw/simon/wilf6/500-4/8/0/length.txt
/scratch/mentors/mdw/simon/wilf6/500-4/9/debug 0 51294 9 0 0
stat -c"%s" /scratch/mentors/mdw/simon/wilf6/500-4/9/0/block.data > /scratch/mentors/mdw/simon/wilf6/500-4/9/0/length.txt
/scratch/mentors/mdw/simon/wilf6/500-4/10/debug 0 3092 10 0 0
stat -c"%s" /scratch/mentors/mdw/simon/wilf6/500-4/10/0/block.data > /scratch/mentors/mdw/simon/wilf6/500-4/10/0/length.txt
/scratch/mentors/mdw/simon/wilf6/500-4/11/debug 0 8 11 0 0
stat -c"%s" /scratch/mentors/mdw/simon/wilf6/500-4/11/0/block.data > /scratch/mentors/mdw/simon/wilf6/500-4/11/0/length.txt
/scratch/mentors/mdw/simon/wilf6/500-4/sumCounts
