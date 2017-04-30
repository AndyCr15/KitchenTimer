package com.androidandyuk.kitchentimer;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ListView itemListView;
    List<timerItem> itemList = new ArrayList<>();
    boolean timerIsActive = false;
    boolean showingAddItem = false;
    boolean mainTimerIsPaused = false;
    boolean mediaPlayerPlaying = false;

    long longestTimer;
    long mainTimerView;
    Button timerButton;
    CountDownTimer countDownTimer;
    MediaPlayer mplayer;
    //the add item layout is not showing to begin with


    public void timerButtonPressed(View view) {

        if (timerIsActive) {
            //code for when the timer is already running
            Log.i("Timer button pressed : ", "Already running");
            timerButton.setText("Start Timer");
            timerIsActive = false;
            resetTimer();
        } else if (itemList.size() > 0) {
            //code if timer is not running
            longestTimer = (itemList.get(itemList.size() - 1)).getTotalTime();
            Log.i("Starting Timer", "Longest time is " + itemList.get(itemList.size() - 1).getTotalTime());
            Log.i("Timer ", " " + itemList.get(itemList.size() - 1).milliSeconds + " finishBy " + itemList.get(itemList.size() - 1).getFinishBy());
            Collections.sort(itemList);
            timerIsActive = true;
            timerButton.setText("Stop");
            startTimer(longestTimer);
        } else {
            Toast.makeText(MainActivity.this, "You need to add some items", Toast.LENGTH_SHORT).show();
        }
    }

    public void startTimer(long milliSeconds) {

        mainTimerView = milliSeconds;

        if (!mainTimerIsPaused) {
            Log.i("Timer started : ", "" + Long.valueOf(milliSeconds) / 1000);
            soundAlarm("Timer started : " + timeInMinutes(milliSeconds), 0);
        }

        Log.i("Main Timer", "Setting Pause as False");
        mainTimerIsPaused = false;

        countDownTimer = new CountDownTimer(milliSeconds, 100) {

            @Override
            public void onTick(long millisUntilFinished) {
                //Log.i("Made it to : ", "on tick");
                timerButton.setText(timeInMinutes(mainTimerView) + " (Press to Reset)");
                mainTimerView -= 100;

                // every tenth of a second run this loop for each item in the list
                for (timerItem item : itemList) {

                    if (item.milliSecondsLeft + item.finishBy > mainTimerView) {
                        mainTimerView = item.milliSecondsLeft + item.finishBy;
                    }

                    // check if any new timers need to start
                    if ((mainTimerView < item.totalTime) && (mainTimerView + 95 > item.totalTime) && !mainTimerIsPaused) {
                        //start a new timer up
                        soundAlarm("Put " + item.getName() + " in for " + timeInMinutes(item.milliSeconds), 1);
                    }
                    // check item is in range and not paused, the main timer is not paused then take a tenth of a second off
                    if ((mainTimerView <= item.totalTime) && !item.isPauseTimer()) {
                        item.milliSecondsLeft -= 100;
                        // check if an item is ready
                        if (item.milliSecondsLeft <= 0) {
                            // what to do when an item is ready
                            soundAlarm("Remove " + item.getName(), 2);
                            itemList.remove(item);
                            break;
                        }
                    }
                    // check if the items time left is longer than the timer
                    if ((item.milliSecondsLeft - 100) > mainTimerView) {
                        // Pause the main timer as an item is now longer than it.
                        Log.i("Timer", "item longer than main timer. Item : " + item.milliSecondsLeft + " Main : " + mainTimerView);
                        mainTimerIsPaused = true;
                        item.setPausingMainTimer(true);
                        timerButton.setText(timeInMinutes(mainTimerView) + " (Press to Cancel)");
                    }
                }
                sortMyList();
            }

            @Override
            public void onFinish() {

                // action to take on end of alarm
                if (!mainTimerIsPaused) {
                    //Toast.makeText(MainActivity.this, "Times up!", Toast.LENGTH_LONG).show();
                    soundAlarm("Times up!", 2);
                    resetTimer();
                } else {
                    startTimer(mainTimerView);
                }
            }
        }.start();
    }

    public void itemTimerStart(String message) {
        //change the toast for a dismissable message
        //Toast.makeText(MainActivity.this, "" + message, Toast.LENGTH_LONG).show();
        soundAlarm(message, 1);
        // play alarm sound
//        mplayer = MediaPlayer.create(this, R.raw.alarm);
//        mplayer.start();
    }

    public void resetTimer() {

        timerButton.setText("Start Timer");
        countDownTimer.cancel();
        timerIsActive = false;
        for (timerItem item : itemList) {
            item.milliSecondsLeft = item.milliSeconds;
            item.setPausingMainTimer(false);
            item.setPauseTimer(false);
        }
        sortMyList();

    }

    public String timeInMinutes(long milliSeconds) {
        String mins = Long.toString(milliSeconds / 60000);
        int intSecs = (int) (milliSeconds % 60000) / 1000;
        String secs = Integer.toString(intSecs);
        if (secs.length() < 2) {
            secs = "0" + secs;
        }
        return mins + ":" + secs;
    }

    public void showAddItem(View view) {
        if (!showingAddItem) {
            showingAddItem = true;
            View addItem = findViewById(R.id.ItemInfo);

            addItem.setTranslationY(-1500f);
            addItem.setVisibility(View.VISIBLE);
            addItem.animate().translationYBy(1100f).setDuration(500);

        }
    }

    public void hideAddItem(View view) {
        View addItem = findViewById(R.id.ItemInfo);
        addItem.setVisibility(View.INVISIBLE);
        showingAddItem = false;

    }

    public void soundAlarm(String message, int soundNumber) {
        Log.i("Sound Alarm", "Method called");
        TextView alarmMessage = (TextView) findViewById(R.id.alarmMessage);
        alarmMessage.setText(message);
        View messageLayout = findViewById(R.id.messageLayout);
        messageLayout.setVisibility(View.VISIBLE);

        // play alarm sound
        switch (soundNumber) {
            case 1:
                Log.i("Switch", "Case 1");
                mplayer = MediaPlayer.create(getBaseContext(), R.raw.alarm);
                break;
            case 2:
                Log.i("Switch", "Case 2");
                mplayer = MediaPlayer.create(getBaseContext(), R.raw.siren);
                break;
        }
        if (soundNumber > 0) {
            mplayer.start();
            mediaPlayerPlaying = true;
        }
    }

    public void closeMessage(View view) {
        View messageLayout = findViewById(R.id.messageLayout);
        messageLayout.setVisibility(View.INVISIBLE);

        if (mediaPlayerPlaying) {
            mplayer.stop();
            mplayer.release();
            mediaPlayerPlaying = false;
        }
        Log.i("Close message", "End");
    }

    public void addItem(View view) {

        EditText itemName = (EditText) findViewById(R.id.itemName);
        EditText itemTime = (EditText) findViewById(R.id.itemTime);
        EditText itemFinish = (EditText) findViewById(R.id.finishTime);

        // check details are entered
        if (itemName.length() == 0 || itemName.equals("") || itemTime.length() == 0 || itemTime.equals("")) {
            Toast.makeText(getApplicationContext(), "Enter item details",
                    Toast.LENGTH_SHORT).show();
        } else {

            // details are there, so lets add an item to the list
            Log.i("Item Added :", itemName.getText().toString());
            Log.i("Item Added :", itemTime.getText().toString());
            Log.i("Item Added :", itemFinish.getText().toString());

            // read how many seconds the timer needs
            int seconds = Integer.parseInt(itemTime.getText().toString());
            int finish;

            String value2 = itemFinish.getText().toString();

            // finish time (gap befor end of cooking) can be left blank, so 'try' used
            try {
                finish = Integer.parseInt(value2);
            } catch (Exception e) {
                finish = 0;
            }

            // create the timerItem object and add it to my list of items
            timerItem next = new timerItem(itemName.getText().toString(), seconds, finish);
            itemList.add(next);

            // reset the add item text boxes
            itemName.setText(null);
            itemTime.setText(null);
            itemFinish.setText(null);
            itemName.clearFocus();
            itemTime.clearFocus();
            itemFinish.clearFocus();

            // drops the keyboard out of view, now the item is added
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
        // refreshes the list of items
        sortMyList();
        // hide the layout for adding an item
        hideAddItem(view);
    }

    public void sortMyList() {
        Collections.sort(itemList);
        ArrayAdapter<timerItem> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, itemList);

        itemListView.setAdapter(arrayAdapter);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        itemListView = (ListView) findViewById(R.id.itemListView);
        timerButton = (Button) findViewById(R.id.timerButton);
        timerButton.setText("Start Timer");

        timerItem egg = new timerItem("Egg", 180, 0);
        timerItem toast = new timerItem("Toast", 120, 0);
        timerItem sausage = new timerItem("Sausage", 290, 0);
        timerItem bacon = new timerItem("Bacon", 300, 0);
        itemList.add(egg);
        itemList.add(toast);
        itemList.add(sausage);
        itemList.add(bacon);

        sortMyList();

        ArrayAdapter<timerItem> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, itemList);

        itemListView.setAdapter(arrayAdapter);

        itemListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Log.i("Long Press on", " " + position);
                Toast.makeText(MainActivity.this, "Removing " + itemList.get(position), Toast.LENGTH_SHORT).show();
                itemList.remove(position);
                sortMyList();
                return true;
            }
        });

        itemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("Short Press on", " " + position);
                Log.i("Item :", " " + (itemList.get(position)));

                // if clicking an item that is pausing the main timer, restart the timer on this time
                if ((itemList.get(position).isPausingMainTimer())) {
                    Log.i("Item pressed", "was pausing Main Timer");
                    (itemList.get(position)).setPauseTimer(false);
                    itemList.get(position).setPausingMainTimer(false);
                    countDownTimer.cancel();
                    //startTimer((itemList.get(position).milliSecondsLeft) + (itemList.get(position).finishBy));
                    Log.i("Resuming Timer ", "from " + mainTimerView);
                    startTimer(mainTimerView);
                } else {
                    // toggle the Pause timer state on a single tap
                    Log.i("Item pressed", "was not pausing Main Timer");
                    (itemList.get(position)).setPauseTimer(true);
                    (itemList.get(position)).setPausingMainTimer(true);
                    // set flag to pause main timer?
                    mainTimerIsPaused = true;
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        //ListView itemListView;
        //savedInstanceState.putStringArrayList("itemList", itemList<timerItem>);

        savedInstanceState.putBoolean("timerIsActive", timerIsActive);
        savedInstanceState.putBoolean("showingAddItem", showingAddItem);
    }
}

