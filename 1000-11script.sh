export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/scratch/mentors/mdw/simon/polyWilf7/flint-2.5.2
mkdir -p /scratch/mentors/mdw/simon/polyWilf7/1000-11/0/0
mkdir -p /scratch/mentors/mdw/simon/polyWilf7/1000-11/1/0
mkdir -p /scratch/mentors/mdw/simon/polyWilf7/1000-11/2/0
mkdir -p /scratch/mentors/mdw/simon/polyWilf7/1000-11/3/0
mkdir -p /scratch/mentors/mdw/simon/polyWilf7/1000-11/4/0
mkdir -p /scratch/mentors/mdw/simon/polyWilf7/1000-11/5/0
mkdir -p /scratch/mentors/mdw/simon/polyWilf7/1000-11/6/0
mkdir -p /scratch/mentors/mdw/simon/polyWilf7/1000-11/6/1
mkdir -p /scratch/mentors/mdw/simon/polyWilf7/1000-11/7/0
mkdir -p /scratch/mentors/mdw/simon/polyWilf7/1000-11/8/0
mkdir -p /scratch/mentors/mdw/simon/polyWilf7/1000-11/9/0
mkdir -p /scratch/mentors/mdw/simon/polyWilf7/1000-11/10/0
mkdir -p /scratch/mentors/mdw/simon/polyWilf7/1000-11/11/0
gcc -DtargetNumTerms=0 -DNUMBLOCKS=1 distFrom.c computeBorders.c distToAndData.c indexThread.c main.c memoryManager.c outputThread.c distFromPoly.c precompute.c -std=c11 -lpthread -lgmp -L/scratch/mentors/mdw/simon/polyWilf7/flint-2.5.2 -I/scratch/mentors/mdw/simon/polyWilf7/flint-2.5.2 -lflint -g -pg -o /scratch/mentors/mdw/simon/polyWilf7/1000-11/0/profile
gcc -DtargetNumTerms=1 -DNUMBLOCKS=1 distFrom.c computeBorders.c distToAndData.c indexThread.c main.c memoryManager.c outputThread.c distFromPoly.c precompute.c -std=c11 -lpthread -lgmp -L/scratch/mentors/mdw/simon/polyWilf7/flint-2.5.2 -I/scratch/mentors/mdw/simon/polyWilf7/flint-2.5.2 -lflint -g -pg -o /scratch/mentors/mdw/simon/polyWilf7/1000-11/1/profile
gcc -DtargetNumTerms=2 -DNUMBLOCKS=1 distFrom.c computeBorders.c distToAndData.c indexThread.c main.c memoryManager.c outputThread.c distFromPoly.c precompute.c -std=c11 -lpthread -lgmp -L/scratch/mentors/mdw/simon/polyWilf7/flint-2.5.2 -I/scratch/mentors/mdw/simon/polyWilf7/flint-2.5.2 -lflint -g -pg -o /scratch/mentors/mdw/simon/polyWilf7/1000-11/2/profile
gcc -DtargetNumTerms=3 -DNUMBLOCKS=1 distFrom.c computeBorders.c distToAndData.c indexThread.c main.c memoryManager.c outputThread.c distFromPoly.c precompute.c -std=c11 -lpthread -lgmp -L/scratch/mentors/mdw/simon/polyWilf7/flint-2.5.2 -I/scratch/mentors/mdw/simon/polyWilf7/flint-2.5.2 -lflint -g -pg -o /scratch/mentors/mdw/simon/polyWilf7/1000-11/3/profile
gcc -DtargetNumTerms=4 -DNUMBLOCKS=1 distFrom.c computeBorders.c distToAndData.c indexThread.c main.c memoryManager.c outputThread.c distFromPoly.c precompute.c -std=c11 -lpthread -lgmp -L/scratch/mentors/mdw/simon/polyWilf7/flint-2.5.2 -I/scratch/mentors/mdw/simon/polyWilf7/flint-2.5.2 -lflint -g -pg -o /scratch/mentors/mdw/simon/polyWilf7/1000-11/4/profile
gcc -DtargetNumTerms=5 -DNUMBLOCKS=1 distFrom.c computeBorders.c distToAndData.c indexThread.c main.c memoryManager.c outputThread.c distFromPoly.c precompute.c -std=c11 -lpthread -lgmp -L/scratch/mentors/mdw/simon/polyWilf7/flint-2.5.2 -I/scratch/mentors/mdw/simon/polyWilf7/flint-2.5.2 -lflint -g -pg -o /scratch/mentors/mdw/simon/polyWilf7/1000-11/5/profile
gcc -DtargetNumTerms=6 -DNUMBLOCKS=1 distFrom.c computeBorders.c distToAndData.c indexThread.c main.c memoryManager.c outputThread.c distFromPoly.c precompute.c -std=c11 -lpthread -lgmp -L/scratch/mentors/mdw/simon/polyWilf7/flint-2.5.2 -I/scratch/mentors/mdw/simon/polyWilf7/flint-2.5.2 -lflint -g -pg -o /scratch/mentors/mdw/simon/polyWilf7/1000-11/6/profile
gcc -DtargetNumTerms=7 -DNUMBLOCKS=2 distFrom.c computeBorders.c distToAndData.c indexThread.c main.c memoryManager.c outputThread.c distFromPoly.c precompute.c -std=c11 -lpthread -lgmp -L/scratch/mentors/mdw/simon/polyWilf7/flint-2.5.2 -I/scratch/mentors/mdw/simon/polyWilf7/flint-2.5.2 -lflint -g -pg -o /scratch/mentors/mdw/simon/polyWilf7/1000-11/7/profile
gcc -DtargetNumTerms=8 -DNUMBLOCKS=1 distFrom.c computeBorders.c distToAndData.c indexThread.c main.c memoryManager.c outputThread.c distFromPoly.c precompute.c -std=c11 -lpthread -lgmp -L/scratch/mentors/mdw/simon/polyWilf7/flint-2.5.2 -I/scratch/mentors/mdw/simon/polyWilf7/flint-2.5.2 -lflint -g -pg -o /scratch/mentors/mdw/simon/polyWilf7/1000-11/8/profile
gcc -DtargetNumTerms=9 -DNUMBLOCKS=1 distFrom.c computeBorders.c distToAndData.c indexThread.c main.c memoryManager.c outputThread.c distFromPoly.c precompute.c -std=c11 -lpthread -lgmp -L/scratch/mentors/mdw/simon/polyWilf7/flint-2.5.2 -I/scratch/mentors/mdw/simon/polyWilf7/flint-2.5.2 -lflint -g -pg -o /scratch/mentors/mdw/simon/polyWilf7/1000-11/9/profile
gcc -DtargetNumTerms=10 -DNUMBLOCKS=1 distFrom.c computeBorders.c distToAndData.c indexThread.c main.c memoryManager.c outputThread.c distFromPoly.c precompute.c -std=c11 -lpthread -lgmp -L/scratch/mentors/mdw/simon/polyWilf7/flint-2.5.2 -I/scratch/mentors/mdw/simon/polyWilf7/flint-2.5.2 -lflint -g -pg -o /scratch/mentors/mdw/simon/polyWilf7/1000-11/10/profile
gcc -DtargetNumTerms=11 -DNUMBLOCKS=1 distFrom.c computeBorders.c distToAndData.c indexThread.c main.c memoryManager.c outputThread.c distFromPoly.c precompute.c -std=c11 -lpthread -lgmp -L/scratch/mentors/mdw/simon/polyWilf7/flint-2.5.2 -I/scratch/mentors/mdw/simon/polyWilf7/flint-2.5.2 -lflint -g -pg -o /scratch/mentors/mdw/simon/polyWilf7/1000-11/11/profile
gcc sumCounts.c -std=c11 -lpthread -lgmp -L/scratch/mentors/mdw/simon/polyWilf7/flint-2.5.2 -I/scratch/mentors/mdw/simon/polyWilf7/flint-2.5.2 -lflint -g -pg -o /scratch/mentors/mdw/simon/polyWilf7/1000-11/sumCounts
/scratch/mentors/mdw/simon/polyWilf7/1000-11/0/profile 0 1 0 0 0
stat -c"%s" /scratch/mentors/mdw/simon/polyWilf7/1000-11/0/0/block.data > /scratch/mentors/mdw/simon/polyWilf7/1000-11/0/0/length.txt
/scratch/mentors/mdw/simon/polyWilf7/1000-11/1/profile 0 90 1 0 0
stat -c"%s" /scratch/mentors/mdw/simon/polyWilf7/1000-11/1/0/block.data > /scratch/mentors/mdw/simon/polyWilf7/1000-11/1/0/length.txt
/scratch/mentors/mdw/simon/polyWilf7/1000-11/2/profile 0 1912 2 0 0
stat -c"%s" /scratch/mentors/mdw/simon/polyWilf7/1000-11/2/0/block.data > /scratch/mentors/mdw/simon/polyWilf7/1000-11/2/0/length.txt
/scratch/mentors/mdw/simon/polyWilf7/1000-11/3/profile 0 16461 3 0 0
stat -c"%s" /scratch/mentors/mdw/simon/polyWilf7/1000-11/3/0/block.data > /scratch/mentors/mdw/simon/polyWilf7/1000-11/3/0/length.txt
/scratch/mentors/mdw/simon/polyWilf7/1000-11/4/profile 0 71406 4 0 0
stat -c"%s" /scratch/mentors/mdw/simon/polyWilf7/1000-11/4/0/block.data > /scratch/mentors/mdw/simon/polyWilf7/1000-11/4/0/length.txt
/scratch/mentors/mdw/simon/polyWilf7/1000-11/5/profile 0 172236 5 0 0
stat -c"%s" /scratch/mentors/mdw/simon/polyWilf7/1000-11/5/0/block.data > /scratch/mentors/mdw/simon/polyWilf7/1000-11/5/0/length.txt
/scratch/mentors/mdw/simon/polyWilf7/1000-11/6/profile 0 120336 6 0 0
/scratch/mentors/mdw/simon/polyWilf7/1000-11/6/profile 120336 240671 6 1 0
stat -c"%s" /scratch/mentors/mdw/simon/polyWilf7/1000-11/6/0/block.data > /scratch/mentors/mdw/simon/polyWilf7/1000-11/6/0/length.txt
/scratch/mentors/mdw/simon/polyWilf7/1000-11/7/profile 0 194565 7 0 0
stat -c"%s" /scratch/mentors/mdw/simon/polyWilf7/1000-11/7/0/block.data > /scratch/mentors/mdw/simon/polyWilf7/1000-11/7/0/length.txt
/scratch/mentors/mdw/simon/polyWilf7/1000-11/8/profile 0 86785 8 0 0
stat -c"%s" /scratch/mentors/mdw/simon/polyWilf7/1000-11/8/0/block.data > /scratch/mentors/mdw/simon/polyWilf7/1000-11/8/0/length.txt
/scratch/mentors/mdw/simon/polyWilf7/1000-11/9/profile 0 18909 9 0 0
stat -c"%s" /scratch/mentors/mdw/simon/polyWilf7/1000-11/9/0/block.data > /scratch/mentors/mdw/simon/polyWilf7/1000-11/9/0/length.txt
/scratch/mentors/mdw/simon/polyWilf7/1000-11/10/profile 0 1487 10 0 0
stat -c"%s" /scratch/mentors/mdw/simon/polyWilf7/1000-11/10/0/block.data > /scratch/mentors/mdw/simon/polyWilf7/1000-11/10/0/length.txt
/scratch/mentors/mdw/simon/polyWilf7/1000-11/11/profile 0 12 11 0 0
stat -c"%s" /scratch/mentors/mdw/simon/polyWilf7/1000-11/11/0/block.data > /scratch/mentors/mdw/simon/polyWilf7/1000-11/11/0/length.txt
/scratch/mentors/mdw/simon/polyWilf7/1000-11/sumCounts
