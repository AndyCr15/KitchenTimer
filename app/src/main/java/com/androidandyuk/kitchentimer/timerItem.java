package com.androidandyuk.kitchentimer;

import android.util.Log;

import static com.androidandyuk.kitchentimer.MainActivity.itemList;
import static com.androidandyuk.kitchentimer.MainActivity.serial;
import static com.androidandyuk.kitchentimer.MainActivity.timeViewStyle;

/**
 * Created by AndyCr15 on 26/04/2017.
 */

public class timerItem implements Comparable<timerItem> {
    private String name;
    long milliSeconds;
    long finishBy;
    long finishByLeft;
    long milliSecondsLeft;
    private boolean pauseTimer = false;
    boolean pausingMainTimer = false;
    boolean hasStarted = false;
    timerItem nextItem = null;
    String note = "";

    public timerItem(String name, int seconds, int finishBy) {
        this.name = name;
        this.milliSeconds = (long) seconds * 1000;
        this.milliSecondsLeft = milliSeconds;
        this.finishBy = (long) finishBy * 1000;
        this.finishByLeft = this.finishBy;
    }

    public timerItem(timerItem copy) {
        this.name = copy.name;
        this.milliSeconds = copy.milliSeconds;
        this.milliSecondsLeft = copy.milliSeconds;
        this.finishBy = copy.finishBy;
        this.finishByLeft = copy.finishBy;
        this.nextItem = copy.nextItem;
    }

    @Override
    public String toString() {
        String message;

        message = "" + name + " for " + timeInMinutes(milliSecondsLeft, timeViewStyle);

        if (finishBy > 0) {
            message += ", to be finished with " + timeInMinutes(finishByLeft, timeViewStyle) + " to go";
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
        String value="";

        //varry the output based on the setting
        switch (style) {
            case 0:
                value = mins + ":" + secs + " min";
                if (milliSeconds / 60000 > 1) {
                    value += "s";
                }
                break;
            case 1:
                value = "";
                if (!mins.equals("0")) {
                    value = mins + " min";
                }
                if (milliSeconds / 60000 > 1) {
                    value += "s";
                }
                if (intSecs > 0) {
                    value += " " + secs + " secs";
                }
                break;
        }
        return value;
    }

    public long getTotalTime() {
        Long amount = this.milliSecondsLeft + this.finishByLeft;
        if (this.nextItem != null) {
            amount += this.nextItem.getTotalTime();
        }
        return amount;
    }

    @Override
    public int compareTo(timerItem o) {
        // negative means the incoming is greater, positive means the this is greater
        return (int) this.getTotalTime() - (int) o.getTotalTime();
    }

    public timerItem getNextItem() {
        return nextItem;
    }

    public void setNextItem(timerItem nextItem) {
        Log.i("setNextItem", "" + nextItem);
        // Get state of the toggle button;
        Log.i("Toggle State", "" + serial);
        // code to check if it's already being pointed to
        for (timerItem item : itemList) {
            // check both have nextItems, they can't be a match if one is empty!
            Log.i("itemList size", "" + itemList.size());
            if (item.nextItem != null && nextItem != null) {
                if (item.nextItem.compareTo(nextItem) == 0) {
                    Log.i("setNextItem", "Match Found");
                    if (serial) {
                        // what to do if serial is slected
                        item.nextItem = this;
                        this.nextItem = nextItem;
                        return;
                    } else {
                        // what to do if parallel is selected
                        this.nextItem = nextItem;
                        return;
                    }

                }
            }
        }
        Log.i("setNextItem", "No Matches Found");
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
}
