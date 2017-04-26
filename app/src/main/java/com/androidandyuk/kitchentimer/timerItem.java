package com.androidandyuk.kitchentimer;

/**
 * Created by AndyCr15 on 26/04/2017.
 */

public class timerItem {
    String name;
    int seconds;
    int finishBy;

    public timerItem(String name, int seconds, int finishBy) {
        this.name = name;
        this.seconds = seconds;
        this.finishBy = finishBy;
    }

    @Override
    public String toString() {
        if (finishBy>0) {
            return "" + name + " for " + timeInMinutes(seconds) + " minutes, to be finished by " + timeInMinutes(finishBy);
        } else {
            return "" + name + " for " + timeInMinutes(seconds) + " minutes";
        }
    }

    public String timeInMinutes(int seconds) {
        String mins = Integer.toString(seconds / 60);
        String secs = Integer.toString(seconds % 60);
        if(secs.length()<2){
            secs = "0" + secs;
        }
        return mins + ":" + secs;
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
}
