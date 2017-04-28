package com.androidandyuk.kitchentimer;

/**
 * Created by AndyCr15 on 26/04/2017.
 */

public class timerItem implements Comparable<timerItem> {
    String name;
    int seconds;
    int finishBy;
    int totalTime;
    int secondsLeft;

    public timerItem(String name, int seconds, int finishBy) {
        this.name = name;
        this.seconds = seconds;
        this.finishBy = finishBy;
        this.totalTime = seconds + finishBy;
        this.secondsLeft = seconds;
    }

    @Override
    public String toString() {
        if (finishBy > 0) {
            return "" + name + " for " + timeInMinutes(secondsLeft) + " minutes, to be finished with " + timeInMinutes(finishBy) + " minutes to go";
        } else {
            return "" + name + " for " + timeInMinutes(secondsLeft) + " minutes";
        }
    }

    public String timeInMinutes(int seconds) {
        String mins = Integer.toString(seconds / 60);
        String secs = Integer.toString(seconds % 60);
        if (secs.length() < 2) {
            secs = "0" + secs;
        }
        return mins + ":" + secs;
    }

    public int getSecondsLeft() {
        return secondsLeft;
    }

    public void setSecondsLeft(int secondsLeft) {
        this.secondsLeft = secondsLeft;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public int getFinishBy() {
        return finishBy;
    }

    public void setFinishBy(int finishBy) {
        this.finishBy = finishBy;
    }

    public int getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(int totalTime) {
        this.totalTime = totalTime;
    }

    @Override
    public int compareTo(timerItem o) {

        // negative means the incoming is greater, positive means the this is greater

        return this.totalTime - o.totalTime;

//        if(this.totalTime > o.totalTime) {
//            return 1;
//        } else if (this.totalTime > o.totalTime) {
//            return -1;
//        }
//
//        return 0;
    }
}
