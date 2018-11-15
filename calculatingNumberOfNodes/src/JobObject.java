public class JobObject implements Comparable<JobObject> {

    ThreadVariables bases;
    int minSum;
    long index;
    long[] values;

    JobObject(ThreadVariables bases, long[] values, int minSum, long index){
        this.bases = bases;
        this.values = values;
        this.minSum = minSum;
        this.index = index;
    }

    JobObject(ThreadVariables bases, long[] values, int minSum){
        this(bases, values, minSum, 0);
    }

    @Override
    public int compareTo(JobObject o) {
        if (index < o.index){
            return -1;
        } else if (index > o.index){
            return 1;
        }
        return 0;
    }
}
