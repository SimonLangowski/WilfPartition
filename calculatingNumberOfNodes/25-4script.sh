export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/scratch/mentors/mdw/simon/wilf6/flint-2.5.2
mkdir -p /scratch/mentors/mdw/simon/wilf6/25-4/0/0
mkdir -p /scratch/mentors/mdw/simon/wilf6/25-4/1/0
mkdir -p /scratch/mentors/mdw/simon/wilf6/25-4/2/0
gcc -DtargetNumTerms=0 -DNUMBLOCKS=1 distFrom.c computeBorders.c distToAndData.c indexThread.c main.c memoryManager.c outputThread.c -lpthread -lgmp -L/scratch/mentors/mdw/simon/wilf6/flint-2.5.2 -I/scratch/mentors/mdw/simon/wilf6/flint-2.5.2 -lflint -g -o /scratch/mentors/mdw/simon/wilf6/25-4/0/debug
gcc -DtargetNumTerms=1 -DNUMBLOCKS=1 distFrom.c computeBorders.c distToAndData.c indexThread.c main.c memoryManager.c outputThread.c -lpthread -lgmp -L/scratch/mentors/mdw/simon/wilf6/flint-2.5.2 -I/scratch/mentors/mdw/simon/wilf6/flint-2.5.2 -lflint -g -o /scratch/mentors/mdw/simon/wilf6/25-4/1/debug
gcc -DtargetNumTerms=2 -DNUMBLOCKS=1 distFrom.c computeBorders.c distToAndData.c indexThread.c main.c memoryManager.c outputThread.c -lpthread -lgmp -L/scratch/mentors/mdw/simon/wilf6/flint-2.5.2 -I/scratch/mentors/mdw/simon/wilf6/flint-2.5.2 -lflint -g -o /scratch/mentors/mdw/simon/wilf6/25-4/2/debug
gcc sumCounts.c -lpthread -lgmp -L/scratch/mentors/mdw/simon/wilf6/flint-2.5.2 -I/scratch/mentors/mdw/simon/wilf6/flint-2.5.2 -lflint -g -o /scratch/mentors/mdw/simon/wilf6/25-4/sumCounts
/scratch/mentors/mdw/simon/wilf6/25-4/0/debug 0 1 0 0 0
stat -c"%s" /scratch/mentors/mdw/simon/wilf6/25-4/0/0/block.data > /scratch/mentors/mdw/simon/wilf6/25-4/0/0/length.txt
/scratch/mentors/mdw/simon/wilf6/25-4/1/debug 0 6 1 0 0
stat -c"%s" /scratch/mentors/mdw/simon/wilf6/25-4/1/0/block.data > /scratch/mentors/mdw/simon/wilf6/25-4/1/0/length.txt
/scratch/mentors/mdw/simon/wilf6/25-4/2/debug 0 5 2 0 0
stat -c"%s" /scratch/mentors/mdw/simon/wilf6/25-4/2/0/block.data > /scratch/mentors/mdw/simon/wilf6/25-4/2/0/length.txt
/scratch/mentors/mdw/simon/wilf6/25-4/sumCounts
