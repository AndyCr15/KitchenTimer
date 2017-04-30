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
    boolean pauseTimer = false;
    boolean pausingMainTimer = false;

    public timerItem(String name, int seconds, int finishBy) {
        this.name = name;
        this.seconds = seconds;
        this.finishBy = finishBy;
        this.totalTime = seconds + finishBy;
        this.secondsLeft = seconds;
    }

    @Override
    public String toString() {
        String message;

        if (finishBy > 0) {
            message = "" + name + " for " + timeInMinutes(secondsLeft) + " minutes, to be finished with " + timeInMinutes(finishBy) + " minutes to go";
        } else {
            message = "" + name + " for " + timeInMinutes(secondsLeft) + " minutes";
        }
        if (this.isPauseTimer()) {
            message += " (PAUSED)";
        } ;
        return message;
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

    public boolean isPausingMainTimer() {
        return pausingMainTimer;
    }

    public void setPausingMainTimer(boolean pausingMainTimer) {
        this.pausingMainTimer = pausingMainTimer;
    }

    public void setSecondsLeft(int secondsLeft) {
        this.secondsLeft = secondsLeft;
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
    }
}
