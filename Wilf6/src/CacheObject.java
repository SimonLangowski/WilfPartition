import java.util.ArrayList;

public class CacheObject {

    private int distanceLeft;
    private boolean[] currentBases;
    private long[] mSum;


    public CacheObject(int distanceLeft, boolean[] currentBases){
        this.distanceLeft = distanceLeft;
        this.currentBases = new boolean[currentBases.length];
        for (int i = 0; i < currentBases.length; i++){
            this.currentBases[i] = currentBases[i];
        }
    }

    public int getDistanceLeft() {
        return distanceLeft;
    }

    public boolean[] getCurrentBases() {
        return currentBases;
    }

    public void setmSum(long[] msum){
        this.mSum = msum;
    }

    public long[] getmSum() {
        return mSum;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CacheObject){
            if (currentBases.length == ((CacheObject) obj).getCurrentBases().length){
                if (distanceLeft == ((CacheObject) obj).getDistanceLeft()){
                    for (int i = 0; i < currentBases.length; i++){
                        if (!(currentBases[i] == (((CacheObject) obj).getCurrentBases()[i]))){
                            return false;
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }
}
