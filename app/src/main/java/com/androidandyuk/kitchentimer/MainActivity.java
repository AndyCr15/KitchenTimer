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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ListView itemListView;
    TextView itemTimeView;
    TextView itemFinishView;

    List<timerItem> itemList = new ArrayList<>();
    boolean timerIsActive = false;
    boolean showingAddItem = false;
    boolean mainTimerIsPaused = false;
    boolean mediaPlayerPlaying = false;
    boolean warningsWanted = true;
    long warningTime = 30000;
    //to be used to know the next tap is choosing an item to be queued in the add item method
    boolean choosingQueueItem = false;
    timerItem itemToQueue;
    String queueTag = "0";

    long longestTimer;
    long mainTimerView;
    Button timerButton;
    Button buttonBefore;
    Button buttonAfter;
    CountDownTimer countDownTimer;
    MediaPlayer mplayer;
    // for the seekbars when adding a new item
    int itemTime = 120;
    int finishBy;
    int maxTime = 1500;
    //the add item layout is not showing to begin with


    public void timerButtonPressed(View view) {

        if (timerIsActive) {
            //code for when the timer is already running
            Log.i("Timer button pressed : ", "Already running");
            timerButton.setText("Start Timer");
            timerIsActive = false;
            resetTimer();
        } else if (itemList.size() > 0) {
            //code if timer is not runningCollections.sort(itemList);
            Collections.sort(itemList);
            longestTimer = (itemList.get(itemList.size() - 1)).getTotalTime();
            Log.i("Starting Timer", "Longest time is " + itemList.get(itemList.size() - 1).getTotalTime());
            Log.i("Timer ", " " + itemList.get(itemList.size() - 1).milliSeconds + " finishBy " + itemList.get(itemList.size() - 1).getFinishBy());
            timerIsActive = true;
            timerButton.setText("Stop");
            // Start timer at an hour, mainTimerView will actually decide when what happens
            startTimer(3600000);
        } else {
            Toast.makeText(MainActivity.this, "You need to add some items", Toast.LENGTH_SHORT).show();
        }
    }

    public void startTimer(long milliSeconds) {

        // Timer is started to be an hour long, but only really used for it's tick
        // mainTimerView decides if and when things happen
        mainTimerView = longestTimer;

        if (!mainTimerIsPaused) {
            Log.i("Timer started : ", "" + Long.valueOf(milliSeconds) / 1000);
            soundAlarm("Timer started : " + timeInMinutes(milliSeconds), 0);
        }

        Log.i("Main Timer", "Setting Pause as False");
        mainTimerIsPaused = false;

        countDownTimer = new CountDownTimer(milliSeconds, 250) {

            @Override
            public void onTick(long millisUntilFinished) {

                timerButton.setText(timeInMinutes(mainTimerView) + " (Press to Reset)");
                mainTimerView -= 250;

                // every quarter of a second run this loop for each item in the list
                for (timerItem item : itemList) {

                    if (longestTimer > mainTimerView) {
                        mainTimerView = longestTimer;
                    }

                    // check if any new timers need to start
                    if ((mainTimerView == item.totalTime) && (!item.hasStarted) && !mainTimerIsPaused) {
                        //start a new timer up
                        soundAlarm("Put " + item.getName() + " in for " + timeInMinutes(item.milliSeconds), 1);
                        item.hasStarted = true;
                    }

                    // check for 30 second warning of new item
                    if (((mainTimerView - warningTime) == item.totalTime) && (!item.hasStarted) && warningsWanted) {
                        //give 30 second warning
                        mplayer = MediaPlayer.create(getApplicationContext(), R.raw.blip);
                        mplayer.start();
                        Toast.makeText(MainActivity.this, "Get the " + item.getName() + " ready!", Toast.LENGTH_LONG).show();
                    }

                    // check item is in range and not paused, the main timer is not paused then take a tenth of a second off
                    if ((item.hasStarted) && !item.isPauseTimer()) {
                        item.milliSecondsLeft -= 250;
                        // check if an item is ready
                        if (item.milliSecondsLeft <= 0) {
                            // what to do when an item is ready
                            soundAlarm("Remove " + item.getName(), 2);
                            removeFromQueue(item);
                            itemList.remove(item);
                            break;
                        }
                    }
                    // check if the items time left is longer than the timer
//                    if ((item.milliSecondsLeft - 250) > mainTimerView) {
//                        // Pause the main timer as an item is now longer than it.
//                        Log.i("Timer", "item longer than main timer. Item : " + item.milliSecondsLeft + " Main : " + mainTimerView);
//                        mainTimerIsPaused = true;
//                        item.setPausingMainTimer(true);
//                        timerButton.setText(timeInMinutes(mainTimerView) + " (Press to Cancel)");
//                    }
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

//    public void itemTimerStart(String message) {
//
//        //Toast.makeText(MainActivity.this, "" + message, Toast.LENGTH_LONG).show();
//        soundAlarm(message, 1);
//        // play alarm sound
//
//    }

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
        Log.i("Add Item View ", "Hiding");
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
//        EditText displayItemTime = (EditText) findViewById(R.id.itemTime);
//        EditText displayItemFinish = (EditText) findViewById(R.id.finishTime);
//
//        displayItemTime.setText(timeInMinutes((long)itemTime*1000));

        // check details are entered
        if (itemName.length() == 0 || itemName.equals("") || itemTime == 0) {
            Toast.makeText(getApplicationContext(), "Enter item details",
                    Toast.LENGTH_SHORT).show();
        } else {
            // details are there, so lets add an item to the list
            Log.i("Item Added ", itemName.getText().toString());
            Log.i("Item Added ", "" + itemTime);
            Log.i("Item Added ", "" + finishBy);

            // read how many seconds the timer needs

//            int finish;
//
//            String value2 = itemFinishSeekBar.getText().toString();
//
//            // finish time (gap befor end of cooking) can be left blank, so 'try' used
//            try {
//                finish = Integer.parseInt(value2);
//            } catch (Exception e) {
//                finish = 0;
//            }

            // create the timerItem object
            timerItem next = new timerItem(itemName.getText().toString(), itemTime, finishBy);
            // check if the item needs to be before or after anything else
            Log.i("Adding Item", "queue tag : " + queueTag);
            if (queueTag.equals("1")) {
                Log.i("itemQueue ", " adding to " + next);
                next.itemQueue.add(itemToQueue);
            } else if (queueTag.equals("2")) {
                Log.i("itemQueue ", " adding to " + itemToQueue);
                itemToQueue.itemQueue.add(next);
            }

            // add the completed item to the list
            Log.i("Adding item", next.toString());
            itemList.add(next);
            // reset the add item text boxes
            itemName.setText(null);
//            itemTimeSeekBar.setText(null);
//            itemFinishSeekBar.setText(null);
            itemName.clearFocus();
//            itemTimeSeekBar.clearFocus();
//            itemFinishSeekBar.clearFocus();

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
        //reset queueTag for next item to add
        queueTag = "0";
    }

    public void sortMyList() {
        if (itemList.size() > 0) {
            Collections.sort(itemList);
            longestTimer = (itemList.get(itemList.size() - 1)).getTotalTime();
        }
        ArrayAdapter<timerItem> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, itemList);
        itemListView.setAdapter(arrayAdapter);
    }

    public void queueItem(View view) {
        //when an item must be cooked before or after the one being added
        queueTag = (view.getTag().toString());
        Log.i("Queue Item", " Tag : " + queueTag);
        Toast.makeText(MainActivity.this, "Choose which item", Toast.LENGTH_SHORT).show();
        choosingQueueItem = true;
        hideAddItem(view);
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

        itemTimeView = (TextView)findViewById(R.id.itemTime);
        itemTimeView.setText(timeInMinutes((long)itemTime));

        itemFinishView = (TextView)findViewById(R.id.finishTime);
        itemFinishView.setText(timeInMinutes((long)finishBy));

        buttonBefore = (Button) findViewById(R.id.buttonBefore);
        buttonAfter = (Button) findViewById(R.id.buttonAfter);

        SeekBar itemTimeSeekBar = (SeekBar) findViewById(R.id.itemTimeSeekBar);
        SeekBar finishBySeekBar = (SeekBar) findViewById(R.id.finshBySeekBar);

        itemTimeSeekBar.setMax(maxTime);
        itemTimeSeekBar.setProgress(itemTime);
        finishBySeekBar.setMax(600);

        itemTimeView.setText(timeInMinutes((long)itemTime*1000));

        // add default items
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
                removeFromQueue(itemList.get(position));
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

                //check if choosing a queue it in add item
                if (choosingQueueItem) {
                    itemToQueue = (itemList.get(position));
                    showAddItem(view);
                    choosingQueueItem = false;
                } else

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

        itemTimeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.i("Item Time SeekBar value", Integer.toString(progress));
                itemTime =  ((progress/10)*10);
                itemTimeView.setText(timeInMinutes((long)itemTime*1000));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        finishBySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.i("Finish SeekBar value", Integer.toString(progress));
                finishBy =  ((progress/10)*10);
                itemFinishView.setText(timeInMinutes((long)finishBy*1000));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    public void removeFromQueue(timerItem item) {
        Log.i("Removing", item + " from all queues.");
        for (timerItem checkedItem : itemList) {
            //look through the queue for a match
            int i = 0;
            while (i < checkedItem.itemQueue.size()) {
                if (item.compareTo(checkedItem.itemQueue.get(i)) == 0) {
                    //match found, remove the item
                    Log.i("Removing", "Found a match");
                    checkedItem.itemQueue.remove(i);
                    i++;
                }
            }
        }
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

