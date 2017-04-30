package com.androidandyuk.kitchentimer;

/**
 * Created by AndyCr15 on 26/04/2017.
 */

public class timerItem implements Comparable<timerItem> {
    String name;
    long milliSeconds;
    long finishBy;
    long totalTime;
    long milliSecondsLeft;
    boolean pauseTimer = false;
    boolean pausingMainTimer = false;

    public timerItem(String name, int seconds, int finishBy) {
        this.name = name;
        this.milliSeconds = (long)seconds * 1000;
        this.finishBy = (long)finishBy * 1000;
        this.totalTime = milliSeconds + finishBy;
        this.milliSecondsLeft = milliSeconds;
    }

    public int seconds(int milliSeconds){
        return milliSeconds / 1000;
    }

    @Override
    public String toString() {
        String message;

        if (finishBy > 0) {
            message = "" + name + " for " + timeInMinutes(milliSecondsLeft) + " minutes, to be finished with " + timeInMinutes(finishBy) + " minutes to go";
        } else {
            message = "" + name + " for " + timeInMinutes(milliSecondsLeft) + " minutes";
        }
        if (this.isPauseTimer()) {
            message += " (PAUSED)";
        } ;
        return message;
    }

    public String timeInMinutes(long milliSeconds) {
        String mins = Long.toString(milliSeconds / 60000);
        int intSecs = (int)(milliSeconds % 60000)/1000;
        String secs = Integer.toString(intSecs);
        if (secs.length() < 2) {
            secs = "0" + secs;
        }
        return mins + ":" + secs;
    }

    public long getMilliSecondsLeft() {
        return milliSecondsLeft;
    }

    public boolean isPausingMainTimer() {
        return pausingMainTimer;
    }

    public void setPausingMainTimer(boolean pausingMainTimer) {
        this.pausingMainTimer = pausingMainTimer;
    }

    public void setMilliSecondsLeft(int secondsLeft) {
        this.milliSecondsLeft = secondsLeft;
    }

    public String getName() {
        return name;
    }

    public boolean isPauseTimer() {
        return pauseTimer;
    }

    public void setPauseTimer(boolean pauseTimer) {
        this.pauseTimer = pauseTimer;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getMilliSeconds() {
        return milliSeconds;
    }

    public void setMilliSeconds(int milliSeconds) {
        this.milliSeconds = milliSeconds;
    }

    public long getFinishBy() {
        return finishBy;
    }

    public void setFinishBy(int finishBy) {
        this.finishBy = finishBy;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(int totalTime) {
        this.totalTime = totalTime;
    }

    @Override
    public int compareTo(timerItem o) {
        // negative means the incoming is greater, positive means the this is greater
        long dif = this.totalTime - o.totalTime;
        return (int)dif;
    }
}
