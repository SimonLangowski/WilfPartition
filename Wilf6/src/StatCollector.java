

import java.util.HashMap;

public class StatCollector {
/*
    HashMap<String, StatCollectorHelper> stats;
    public StatCollector(){
        stats = new HashMap<>();
    }
  */

    StatCollectorHelper[] helpers;
    int[] cutoffCopy;
    public StatCollector(int[] cutoffs){
        helpers = new StatCollectorHelper[cutoffs.length * 2];
        cutoffCopy = cutoffs;
        for (int i = 0; i < helpers.length; i++){
            helpers[i] = new StatCollectorHelper();
        }
    }

    public void addTime(int cutoffNumber, boolean isHit, long timeToAdd){
        int index = cutoffNumber * 2;
        if (!isHit){
            index++;
        }
        helpers[index].addTime(timeToAdd);
    }

    public void printAll(){
        double collectedTime = 0;
        for (int i = 0; i < helpers.length; i = i + 2){
            StatCollectorHelper hit = helpers[i];
            StatCollectorHelper miss = helpers[i + 1];
            System.out.printf("%d,", cutoffCopy[i/2]);
            System.out.printf("%d,%f,%.3f,",hit.getCount(), hit.getTime(), hit.getAverage());
            collectedTime += hit.getTime();
            System.out.printf("%d,%f,%.3f,",miss.getCount(), miss.getTime(), miss.getAverage());
            collectedTime += miss.getTime();
        }
        System.out.printf("%f\n", collectedTime);
    }
/*
    public void printAll(){
        ArrayList<String> keys = new ArrayList<String>(stats.keySet());
        Collections.sort(keys);
        double collectedTime = 0;
        for (String key : keys){
            StatCollectorHelper sch = stats.get(key);
            System.out.printf("%s,%d,%f,%.3f", key, sch.getCount(), sch.getTime(), sch.getAverage());
            collectedTime += sch.getTime();
        }
        System.out.printf(",%f\n", collectedTime);
    }

    public void startTime(String key){
        StatCollectorHelper sch = stats.get(key);
        if (sch == null){
            sch = new StatCollectorHelper();
            stats.put(key, sch);
        }
        sch.setTime();
    }

    public void stopTime(String key){
        stats.get(key).addDiff();
    }
    public void reset(){
        stats = new HashMap<>();
    }


    public void addTime(String key, long time){
        StatCollectorHelper sch = stats.get(key);
        if (sch == null){
            sch = new StatCollectorHelper();
            stats.put(key, sch);
        }
        sch.addTime(time);
    }


*/
    private class StatCollectorHelper{

        private long totalTime = 0;
        private int count = 0;
        private long lastTime;

        void addTime(long time){
            count++;
            totalTime += time;
        }

        double getTime(){
            return totalTime / 1000000000.0;
        }

        int getCount(){
            return count;
        }

        double getAverage(){
            return (double) totalTime / count;
        }

        void setTime(){
            lastTime = System.nanoTime();
        }

        void addDiff(){
            addTime(System.nanoTime() - lastTime);
        }

    }

}
