package com.androidandyuk.kitchentimer;

import java.util.ArrayList;

/**
 * Created by AndyCr15 on 20/05/2017.
 */

public class timerSetup {
    String setupName;
    int maxTime;
    boolean warningsWanted;
    ArrayList<timerItem> itemsSetup = new ArrayList<>();

    public timerSetup() {
    }

    public ArrayList<timerItem> getItemsSetup() {
        return itemsSetup;
    }

    @Override
    public String toString() {
        return this.setupName;
    }
}
