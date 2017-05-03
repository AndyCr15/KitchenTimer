package com.androidandyuk.kitchentimer;

import static com.androidandyuk.kitchentimer.MainActivity.timeViewStyle;

/**
 * Created by AndyCr15 on 26/04/2017.
 */

public class timerItem implements Comparable<timerItem> {
    private String name;
    long milliSeconds;
    long finishBy;
    long finishByLeft;
    long totalTime;
    long milliSecondsLeft;
    private boolean pauseTimer = false;
    boolean pausingMainTimer = false;
    boolean hasStarted = false;
    timerItem nextItem = null;

    public timerItem(String name, int seconds, int finishBy) {
        this.name = name;
        this.milliSeconds = (long) seconds * 1000;
        this.milliSecondsLeft = milliSeconds;
        this.finishBy = (long) finishBy * 1000;
        this.finishByLeft = this.finishBy;
    }

    @Override
    public String toString() {
        String message;

        message = "" + name + " for " + timeInMinutes(milliSecondsLeft, timeViewStyle) + " minutes";

        if (finishBy > 0) {
            message += ", to be finished with " + timeInMinutes(finishByLeft, timeViewStyle) + " minutes to go";
        }

        // add the next item onto the display nemae
        if (nextItem != null) {
            message += " before the " + nextItem.getName();
        }

        if (this.isPauseTimer()) {
            message += " (PAUSED)";
        }
        ;
        return message;
    }

    public static String timeInMinutes(long milliSeconds, int style) {
        String mins = Long.toString(milliSeconds / 60000);
        int intSecs = (int) (milliSeconds % 60000) / 1000;
        String secs = Integer.toString(intSecs);
        if (secs.length() < 2) {
            secs = "0" + secs;
        }
        String value = "0m 0s";

        //varry the output based on the setting
        switch(style){
            case 0: value = mins + ":" + secs + " mins";
                break;
            case 1: value = mins + "m " + " " +secs + "s";
                break;
        }
        return value;
    }

    public timerItem getNextItem() {
        return nextItem;
    }

    public void setNextItem(timerItem nextItem) {
        this.nextItem = nextItem;
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
        return this.name;
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
        return this.milliSeconds;
    }

    public void setMilliSeconds(int milliSeconds) {
        this.milliSeconds = milliSeconds;
    }

    public long getFinishBy() {
        return this.finishBy;
    }

    public void setFinishBy(int finishBy) {
        this.finishBy = finishBy;
    }

    public long getTotalTime() {
        Long amount = this.milliSecondsLeft + this.finishByLeft;
        if (this.nextItem != null) {
            amount += this.nextItem.getTotalTime();
        }
        return amount;
    }

    public void setTotalTime(int totalTime) {
        this.totalTime = totalTime;
    }

    @Override
    public int compareTo(timerItem o) {
        // negative means the incoming is greater, positive means the this is greater
        return (int) this.getTotalTime() - (int) o.getTotalTime();
    }
}
