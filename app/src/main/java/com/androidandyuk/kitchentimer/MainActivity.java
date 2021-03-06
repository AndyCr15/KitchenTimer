package com.androidandyuk.kitchentimer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import static com.androidandyuk.kitchentimer.R.drawable.pause;
import static com.androidandyuk.kitchentimer.R.id.itemName;
import static com.androidandyuk.kitchentimer.settings.loadSettings;
import static com.androidandyuk.kitchentimer.timerItem.timeInMinutes;

public class MainActivity extends AppCompatActivity {

    Notification notification;

    NotificationManager notificationManager;

    private FirebaseAnalytics mFirebaseAnalytics;
    private static final String TAG = "MainActivity";
    private AdView mAdView;

    ListView itemListView;
    TextView itemTimeView;
    TextView itemFinishView;
    FloatingActionButton floatingActionButton;

    public static boolean isInForeground;

    static List<timerItem> itemList = new ArrayList<>();
    static ArrayList<timerSetup> savedSetups = new ArrayList<>();

    // to be able to backup the list, once, to be restored in reset
    static List<timerItem> backupList = new ArrayList<>();
    static boolean backupNeeded = true;

    boolean timerIsActive = false;
    boolean showingAddItem = false;
    boolean showingEditOrDelete = false;
    boolean mainTimerIsPaused = false;
    boolean mediaPlayerPlaying = false;
    public static boolean warningsWanted = true;
    public static boolean backgroundWanted = false;
    long warningTime = 30000;
    //to be used to know the next tap is choosing an item to be queued in the add item method
    boolean choosingQueueItem = false;
    timerItem itemToQueue;
    String queueTag = "0";
    static ToggleButton serialToggle;
    static boolean serial = false;

    long longestTimer;
    long nextLongestTimer;
    long mainTimerView;

    // used to store what item might be being edited or deleted
    int itemLongPressedPosition = 0;
    timerItem itemLongPressed;

    Button timerButton;
    Button buttonBefore;
    Button buttonAfter;
    View itemInfo;
    View editOrDelete;
    View nextAlarm;
    EditText itemInfoName;
    TextView nextAlarmText;
    SeekBar itemTimeSeekBar;
    SeekBar finishBySeekBar;

    CountDownTimer countDownTimer;
    MediaPlayer mplayer;
    // for the seekbars when adding a new item
    int itemTime = 120;
    int finishBy;

    //some settings
    public static int maxTime = 1500;
    public static int timeViewStyle = 1;

    public static SharedPreferences sharedPreferences;
    public static SharedPreferences.Editor ed;

    static MyItemAdapter myAdapter;
    
    // for persistent notification
    NotificationCompat.Builder mBuilder;
    NotificationManager mNotificationManager;
    boolean firstTime;
    int mNotificationId;

    PowerManager.WakeLock wl;

    public static FrameLayout mainLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Log.i("onCreate", "Starting");

        sharedPreferences = this.getSharedPreferences("com.androidandyuk.kitchentimer", Context.MODE_PRIVATE);
        ed = sharedPreferences.edit();

        loadSettings();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        MobileAds.initialize(getApplicationContext());

        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag");


        itemInfo = findViewById(R.id.ItemInfo);
        itemInfo.animate().translationY(-200f).setDuration(50);
        editOrDelete = findViewById(R.id.editOrDelete);

        itemListView = (ListView) findViewById(R.id.itemListView);
        timerButton = (Button) findViewById(R.id.timerButton);
        timerButton.setText("Start Timer");

        nextAlarm = findViewById(R.id.nextAlarm);
        nextAlarmText = (TextView) findViewById(R.id.nextAlarmText);

        itemTimeView = (TextView) findViewById(R.id.itemTime);
        itemTimeView.setText("TIMER : " + timeInMinutes((long) itemTime * 1000, 0));

        itemFinishView = (TextView) findViewById(R.id.finishTime);
        itemFinishView.setText(timeInMinutes((long) finishBy, timeViewStyle));

        buttonBefore = (Button) findViewById(R.id.buttonBefore);
        buttonAfter = (Button) findViewById(R.id.buttonAfter);

        itemTimeSeekBar = (SeekBar) findViewById(R.id.itemTimeSeekBar);
        finishBySeekBar = (SeekBar) findViewById(R.id.finshBySeekBar);

        itemTimeSeekBar.setMax(MainActivity.maxTime);
        itemTimeSeekBar.setProgress(itemTime);
        finishBySeekBar.setMax(590);

        mainLayout = (FrameLayout) findViewById(R.id.mainLayout);



        settings.loadSetups();
        loadSettings();
        invalidateOptionsMenu();

        myAdapter = new MyItemAdapter(itemList);

        itemListView.setAdapter(myAdapter);

        itemListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                // store which item from the list has been long pressed
                itemLongPressedPosition = position;
                itemLongPressed = itemList.get(position);
                Log.i("Long Press on", " " + itemLongPressedPosition);

                // set editOrDelete to visible so user can choose which to do
                editOrDelete.setVisibility(View.VISIBLE);
                showingEditOrDelete = true;
                return true;
            }
        });

        itemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("Item :", " " + (itemList.get(position)));

                //check if choosing a queue it in add item
                if (choosingQueueItem) {
                    itemToQueue = (itemList.get(position));
                    showItemInfo();
                    choosingQueueItem = false;
                } else
                    // toggle the Pause timer state on a single tap
                    if ((itemList.get(position).isPausingMainTimer())) {
                        Log.i("Item pressed", "was pausing Main Timer");
                        // if clicking an item that is pausing the main timer, restart the timer on this time
                        itemList.get(position).setPausingMainTimer(false);

                        // pause anything equal or less than the paused item
                        unpauseAll((itemList.get(position)).getTotalTime());
                        // to pause this item, when it's time is the same as others, but
                        // to avoide pausing all that might be running at the same time
                        // the pauseAll method pauses those that are less than this time
                        // so we also need to pause the actual item pressed
                        (itemList.get(position)).setPauseTimer(false);


//                        countDownTimer.cancel();
//                        Log.i("Resuming Timer ", "from " + mainTimerView);
//                        startTimer(mainTimerView);
                        mainTimerIsPaused = false;
                    } else {
                        Log.i("Item pressed", "was not pausing Main Timer");

                        (itemList.get(position)).setPausingMainTimer(true);
                        pauseAll((itemList.get(position)).getTotalTime());
                        (itemList.get(position)).setPauseTimer(true);

                        // set flag to pause main timer?
                        mainTimerIsPaused = true;
                    }
            }
        });

        itemTimeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.i("Max Time is ", "" + MainActivity.maxTime);
                itemTimeSeekBar.setMax(MainActivity.maxTime);
                Log.i("Progress is ", "" + progress);
                itemTime = ((progress / 30) * 30);
                if (itemTime == 0) {
                    itemTime = 10;
                }
                itemTimeView.setText("TIMER : " + timeInMinutes((long) itemTime * 1000, 0));
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
//                Log.i("Finish SeekBar value", Integer.toString(progress));
                // make 10 the minimum
                finishBy = ((progress / 10) * 10);
                itemFinishView.setText("CUSHION : " + timeInMinutes((long) finishBy * 1000, 0));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private class MyItemAdapter extends BaseAdapter {
        public List<timerItem> itemDataAdapter;

        public MyItemAdapter(List<timerItem> itemDataAdapter) {
            this.itemDataAdapter = itemDataAdapter;
        }

        @Override
        public int getCount() {
            return itemDataAdapter.size();
        }

        @Override
        public String getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater mInflater = getLayoutInflater();
            View myView = mInflater.inflate(R.layout.timer_items_listview, null);

            final timerItem s = itemDataAdapter.get(position);

            TextView itemTime = (TextView) myView.findViewById(R.id.itemTime);
            itemTime.setText(timeInMinutes(s.milliSecondsLeft, 0));


            TextView item = (TextView) myView.findViewById(R.id.item);
            String itemName = s.name;
            if (s.isPauseTimer()) {
                itemName += " ||";
            }
            item.setText(itemName);

            TextView finish = (TextView) myView.findViewById(R.id.finish);
            finish.setText("");
            if (s.finishByLeft>0) {
                finish.setText("With " + timeInMinutes(s.finishByLeft, 0) + " left");
            }

            TextView before = (TextView) myView.findViewById(R.id.before);
            before.setText("");
            if(s.nextItem != null) {
                before.setText("Before " + s.nextItem.getName());
            }
            return myView;
        }

    }

    public void timerButtonPressed(View view) {

        // if a backup has not been made (first press) then make a backup
        // this needs to be set to true when something is added, edited or deleted.
        if (backupNeeded) {
            backupList.clear();
            for (timerItem item : itemList) {
                timerItem newItem = new timerItem(item);
                Log.i("Backup list", "adding " + newItem);
                backupList.add(newItem);
            }
            backupNeeded = false;
        }

        if (timerIsActive) {
            //code for when the timer is already running
            Log.i("Timer button pressed ", "Already running");
            timerButton.setText("Start Timer");
            nextAlarm.setVisibility(View.INVISIBLE);
            timerIsActive = false;
            resetTimer();
        } else if (itemList.size() > 0) {
            //code if timer is not runningCollections.sort(itemList);
            Collections.sort(itemList);
            longestTimer = (itemList.get(itemList.size() - 1)).getTotalTime();

//            }
            Log.i("Starting Timer", "Longest time is " + itemList.get(itemList.size() - 1).getTotalTime());
//            Log.i("Timer ", " " + itemList.get(itemList.size() - 1).milliSeconds + " finishBy " + itemList.get(itemList.size() - 1).getFinishBy());
            timerIsActive = true;
            timerButton.setText("Stop");
            // Start timer at four hours, mainTimerView will actually decide when what happens
            startTimer(14000000);
        } else {
            Toast.makeText(MainActivity.this, "You need to add some items", Toast.LENGTH_SHORT).show();
        }
    }

    public void updateNotification(String message) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 1, intent, 0);
        if (firstTime) {
            mBuilder.setSmallIcon(R.drawable.kitchen_timer_icon)
                    .setContentIntent(pendingIntent)
                    .setContentTitle("Multi Timer")
                    .setOnlyAlertOnce(true);
            firstTime = false;
        }
        mBuilder.setContentText(message);

        mNotificationManager.notify(mNotificationId, mBuilder.build());
    }

    public void startTimer(long milliSeconds) {

        //stop phone from sleeping
        wl.acquire();
        floatingActionButton = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        floatingActionButton.setImageResource(pause);

        // Timer is started to be an hour long, but only really used for it's tick
        // mainTimerView decides if and when things happen
        sortMyList();
        mainTimerView = longestTimer;

        if (!mainTimerIsPaused) {
            Log.i("Timer started : ", "" + Long.valueOf(longestTimer) / 1000);
            soundAlarm("Timer started : " + timeInMinutes(longestTimer, timeViewStyle), 0);
        }

        Log.i("Main Timer", "Setting Pause as False");
        mainTimerIsPaused = false;

        // persistent notification
        mBuilder = new NotificationCompat.Builder(this);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //Different Id's will show up as different notifications
        mNotificationId = 1;
        //Some things we only have to set the first time.
        firstTime = true;


        countDownTimer = new CountDownTimer(milliSeconds, 250) {

            @Override
            public void onTick(long millisUntilFinished) {

                sortMyList();
                if (longestTimer < mainTimerView) {
                    mainTimerView = longestTimer;
                }

                timerButton.setText("Total time " + timeInMinutes(mainTimerView, timeViewStyle));
                mainTimerView -= 250;

                // if the timer is running with more than one alarm left, show the window for the next alarm
                if (nextLongestTimer > 0) {
                    nextAlarm.setVisibility(View.VISIBLE);
                    nextAlarmText.setText("Time until\nnext alarm\n" + timeInMinutes(mainTimerView - nextLongestTimer, timeViewStyle));
                    updateNotification("Next Alarm in " + timeInMinutes(mainTimerView - nextLongestTimer, timeViewStyle) + " - All done in " + timeInMinutes(mainTimerView, timeViewStyle));
                } else {
                    nextAlarm.setVisibility(View.INVISIBLE);
                    updateNotification("All done in " + timeInMinutes(mainTimerView, timeViewStyle));
                }


                // every quarter of a second run this loop for each item in the list
                for (timerItem item : itemList) {

                    if (longestTimer > mainTimerView) {
                        mainTimerView = longestTimer;
                    }

                    // check if any new timers need to start
                    if ((mainTimerView == item.getTotalTime()) && (!item.hasStarted)) {
                        Log.i("Starting New Item", "" + item.getName());
                        //start a new timer up, send the message
                        soundAlarm("Put " + item.getName() + " in for " + timeInMinutes(item.milliSeconds, timeViewStyle) + item.note, 1);
                        item.hasStarted = true;
                    }

                    if (warningsWanted) {
                        // check for 30 second warning of new item
                        if (((mainTimerView - warningTime) == item.getTotalTime()) && (!item.hasStarted) && warningsWanted) {
                            //give 30 second warning but only with a toast, so not sent to alarmMessage
                            mplayer = MediaPlayer.create(getApplicationContext(), R.raw.blip);
                            mplayer.start();
                            mediaPlayerPlaying = true;
                            Toast.makeText(MainActivity.this, "Get the " + item.getName() + " ready!", Toast.LENGTH_LONG).show();
                        }
                    }

                    // check item is in range and not paused, the main timer is not paused then take a quarter of a second off
                    if ((item.hasStarted) && !item.isPauseTimer()) {

                        if (item.milliSecondsLeft > 0) {

                            item.milliSecondsLeft -= 250;
//                            Log.i("onTick", "ticking " + item.getName());

                        }

                        // check if an item is ready
                        if (item.milliSecondsLeft == 0) {
                            // what to do when an item is ready
                            Log.i("Item Ready", "" + item.getName());
                            soundAlarm("Remove " + item.getName() + item.note, 2);
                            item.milliSecondsLeft -= 1;
                        }

                        //check if timer is done, but item still has cushion to tick through
                        if (item.milliSecondsLeft < 0 && item.finishByLeft > 0) {
                            item.finishByLeft -= 250;
                        }

                        if (item.milliSecondsLeft < 0 && item.finishByLeft <= 0) {
                            Log.i("Item Removed from queue", "" + item.getName());
                            removeFromQueue(item);
                            itemList.remove(item);

                            break;
                        }
                    } else {

//                        Log.i("onTick", "not ticking " + item.getName());

                    }

                }
//                sortMyList();
                if (mainTimerView <= 0) {
                    // timer has come to an end
                    soundAlarm("Time to eat!", 2);
                    resetTimer();
                    wl.release();
                }
            }

            @Override
            public void onFinish() {
                // it should no longer ever get here as the timer started is an hout
                // and I just use the onTick from it to calculate everything.

                // action to take on end of alarm
                if (!mainTimerIsPaused) {
                    soundAlarm("Times up!", 2);
                    resetTimer();
                    wl.release();
                } else {
                    resetTimer();
                    startTimer(360000);
                    Log.i("onFinish", "Starting new timer");
                }
            }
        }.start();
    }

    public void resetTimer() {

        Log.i("Timer", "Reset!");
        floatingActionButton = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        floatingActionButton.setImageResource(R.drawable.plus);
        mNotificationManager.cancel(mNotificationId);
        restoreBackup();
        countDownTimer.cancel();
        timerIsActive = false;
        showingAddItem = false;
        //mediaPlayerPlaying = false;
        choosingQueueItem = false;
        queueTag = "0";
        itemLongPressedPosition = 0;

        timerButton.setText("Start Timer");

    }

    public void restoreBackup() {
        // clear the list out and put the backup list back in place
        Log.i("itemList", "Restoring backup");
        itemList.clear();
        for (timerItem item : backupList) {
            timerItem newItem = new timerItem(item);
            Log.i("Backup list", "restoring " + newItem);
            itemList.add(newItem);
        }
        sortMyList();
    }

    public void showItemInfo() {
        if (!showingAddItem) {
            showingAddItem = true;

            itemInfo.setVisibility(View.VISIBLE);

        }
    }

    public void hideItemInfo(View view) {
        Log.i("Add Item View ", "Hiding");
        itemInfo.setVisibility(View.INVISIBLE);
        showingAddItem = false;

    }

    public void FABpressed(View view) {
        floatingActionButton = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        // FAB is used to add an item if the timer hasn't started yet, but then pause and play
        if (!timerIsActive && !mainTimerIsPaused) {
            showItemInfo();
        } else {
            pauseEverything(view);
        }
    }

    public void soundAlarm(String message, int soundNumber) {
        Log.i("Sound Alarm", message);
        if (mediaPlayerPlaying) {
            Log.i("Media IS playing", message);
            mplayer.stop();
            mplayer.release();
            mediaPlayerPlaying = false;
        }
        TextView alarmMessage = (TextView) findViewById(R.id.alarmMessage);
        alarmMessage.setText(message);
        View messageLayout = findViewById(R.id.messageLayout);
        messageLayout.setVisibility(View.VISIBLE);

        // play alarm sound
        switch (soundNumber) {
            case 1:
                mplayer = MediaPlayer.create(getBaseContext(), R.raw.alarm);
                break;
            case 2:
                mplayer = MediaPlayer.create(getBaseContext(), R.raw.siren);
                break;
        }
        if (soundNumber > 0) {
            mplayer.start();
            mediaPlayerPlaying = true;
        }

        // check if the app is in the foreground, if not, use a notification to alert user
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 1, intent, 0);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (!isInForeground) {
            notification = new Notification.Builder(getApplicationContext())
                    .setContentTitle("Multi Timer")
                    .setContentText(message)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(android.R.drawable.sym_def_app_icon)
                    .setAutoCancel(true)
                    .build();

            notificationManager.notify(1, notification);
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

        // each time a new item is added, we would need a new backup of the list
        backupNeeded = true;

        itemInfoName = (EditText) findViewById(itemName);
        itemInfoName.requestFocus();
        itemInfoName.setSelection(itemInfoName.getText().length());

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);

        // check details are entered
        if (itemInfoName.length() == 0 || itemInfoName.equals("") || itemTime == 0) {
            Toast.makeText(getApplicationContext(), "Enter item details", Toast.LENGTH_SHORT).show();
        } else {
            // details are there, so lets add an item to the list
            // create the timerItem object
            timerItem newItem = new timerItem(itemInfoName.getText().toString(), itemTime, finishBy);
            Log.i("Item Added", newItem + " added");
            // check if the item needs to be before or after anything else
            Log.i("Adding Item", "queue tag : " + queueTag);
            serialToggle = (ToggleButton) findViewById(R.id.serialToggle);
            serialToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    // if toggle is pressed, add items in a serial manner
                    if (isChecked) {
                        serial = true;
                    } else {
                        serial = false;
                    }
                }
            });

            if (queueTag.equals("1")) {
                Log.i("itemQueue ", " adding to " + newItem);
                // use a method that will check if anything else already points to itemToQueue

                newItem.setNextItem(itemToQueue);
                //next.nextItem = itemToQueue;
            } else if (queueTag.equals("2")) {
                Log.i("itemQueue ", " adding to " + itemToQueue);
                itemToQueue.setNextItem(newItem);
            }

            // check if we're here because of an edit
            if (itemLongPressed != null) {
                //delete the edited item, before adding it back as a new item
                removeItem(itemLongPressed);
                itemLongPressed = null;
            }

            // add the completed item to the list
            Log.i("Adding item", newItem.toString());
            itemList.add(newItem);
            // reset the add item text boxes
            itemInfoName.setText(null);
            itemInfoName.clearFocus();

            // drops the keyboard out of view, now the item is added
            if (view != null) {
                imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
        // refreshes the list of items
        sortMyList();
        // hide the layout for adding an item
        hideItemInfo(view);
        //reset queueTag for next item to add
        queueTag = "0";
    }

    public void sortMyList() {
//        Log.i("Main Activity", "Sorting List");
        if (itemList.size() > 0) {
            Collections.sort(itemList);
            longestTimer = (itemList.get(itemList.size() - 1)).getTotalTime();
        }

        // check if there is another timer, if so, how long is that one from ending.
        nextLongestTimer = 0;
        Log.i("Finding ", "nextLongestTimer");
        for (int i = 0; i < itemList.size() - 1; i++) {
            if (itemList.get(i).getTotalTime() + 250 < longestTimer && !itemList.get(i).hasStarted) {
                nextLongestTimer = (itemList.get(i)).getTotalTime();
            }
        }

        myAdapter = new MyItemAdapter(itemList);
        itemListView.setAdapter(myAdapter);

    }

    public void queueItem(View view) {
        //when an item must be cooked before or after the one being added
        queueTag = (view.getTag().toString());
        Log.i("Queue Item", " Tag : " + queueTag);
        Toast.makeText(MainActivity.this, "Choose which item", Toast.LENGTH_SHORT).show();
        choosingQueueItem = true;
        hideItemInfo(view);
    }

    public void pauseAll(long milliseconds) {
        // only actually pauses items with less time that haven't started
        for (timerItem item : itemList) {
            if (item.getTotalTime() < milliseconds && !item.hasStarted) {
                item.setPauseTimer(true);
            }
        }
    }

    public void unpauseAll(long milliseconds) {
        for (timerItem item : itemList) {
            if (item.getTotalTime() <= milliseconds) {
                item.setPauseTimer(false);
            }
        }
    }

    public void pauseEverything(View view) {
        // pause literally everything
        floatingActionButton = (FloatingActionButton) findViewById(R.id.floatingActionButton);

        if (!mainTimerIsPaused) {
            for (timerItem item : itemList) {

                item.setPauseTimer(true);

            }
            mainTimerIsPaused = true;
            floatingActionButton.setImageResource(R.drawable.play);
        } else {
            for (timerItem item : itemList) {

                item.setPauseTimer(false);

            }
            mainTimerIsPaused = false;
            floatingActionButton.setImageResource(pause);
        }
    }

    public void deletePressed(View view) {
        editOrDelete.setVisibility(View.INVISIBLE);
        showingEditOrDelete = false;
        removeItem(itemLongPressed);
        itemLongPressed = null;
    }

    public void editPressed(View view) {
        Log.i("Editing item", " " + itemList.get(itemLongPressedPosition));
        editOrDelete.setVisibility(View.INVISIBLE);
        showingEditOrDelete = false;

        // setting all info in the editInfo box to be that of the item to be edited
        itemInfoName = (EditText) findViewById(itemName);
        itemInfoName.setText(itemList.get(itemLongPressedPosition).getName());

        itemTime = (int) itemList.get(itemLongPressedPosition).getMilliSeconds() / 1000;

        finishBy = (int) itemList.get(itemLongPressedPosition).getFinishBy() / 1000;
        editOrDelete.setVisibility(View.INVISIBLE);
        showingEditOrDelete = false;
        itemTimeSeekBar.setProgress(itemTime);
        finishBySeekBar.setProgress(finishBy);

        // keep the nextItem for the edited item
        queueTag = "1";
        itemToQueue = itemLongPressed.nextItem;

        showItemInfo();
    }

    public void removeItem(timerItem item) {

        // each time the list changes, a backup would be needed
        backupNeeded = true;

        removeFromQueue(item);
        itemList.remove(item);
        Log.i("Remove Item", "item removed");
        sortMyList();
    }

    public void removeFromQueue(timerItem item) {
        Log.i("Removing", item + " from all queues.");
        for (timerItem checkedItem : itemList) {
            //look through the queue for a match
            if (checkedItem.nextItem != null) {
                if (item.compareTo(checkedItem.nextItem) == 0) {
                    //match found, remove the item
                    Log.i("Removing", "Found a match");
                    if (checkedItem.nextItem == null) {
                        checkedItem.nextItem = null;
                    } else {
                        // item had a nextItem, so set it to it's own nextItem
                        // effectively skipping over the removed item
                        checkedItem.nextItem = checkedItem.nextItem.nextItem;
                    }
                }
            }
        }
    }

    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        super.onPrepareOptionsPanel(view, menu);

        invalidateOptionsMenu();

//        for (int i = 0; i < savedSetups.size(); i++) {
//            String bikeMakeMenu = savedSetups.get(i).setupName;
//            menu.add(0, i + 1, 0, bikeMakeMenu).setShortcut('3', 'c');
//        }

        return true;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        super.onCreateOptionsMenu(menu);

        menu.add(0, 0, 0, "Settings").setShortcut('3', 'c');

        for (int i = 0; i < savedSetups.size(); i++) {
            String bikeMakeMenu = savedSetups.get(i).setupName;
            menu.add(0, i + 1, 0, bikeMakeMenu).setShortcut('3', 'c');
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menu_choice = item.getItemId();
        switch (menu_choice) {
            case 0:
                Log.i("Option", "0");
                Intent intent = new Intent(getApplicationContext(), settings.class);
                startActivity(intent);
                return true;
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
                Log.i("Option", "" + menu_choice);
                loadSetup(menu_choice - 1);
                sortMyList();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void loadSetup(int position) {
        itemList.clear();
        timerSetup thisSetup = savedSetups.get(position);
        for (timerItem thisItem : thisSetup.itemsSetup) {
            itemList.add(thisItem);
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

    @Override
    public void onBackPressed() {
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            // check if the back button was pressed with the add item view showing
            // if it was, hide this view.  If not, carry on as normal.
            Log.i("Back Pressed", "Adding item = " + showingAddItem);
            if (showingAddItem) {

                showingAddItem = false;
                itemInfo.setVisibility(View.INVISIBLE);
                // reset if it was an item being edited
                itemLongPressed = null;
                // reset the add item text boxes
                if (itemInfoName != null) {
                    itemInfoName.setText(null);
                    itemInfoName.clearFocus();
                }

            } else if (showingEditOrDelete) {
                editOrDelete.setVisibility(View.INVISIBLE);
                showingEditOrDelete = false;
            } else {
                moveTaskToBack(true);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isInForeground = true;

        if(backgroundWanted){
            int resID = getResources().getIdentifier("cooking", "drawable",  this.getPackageName());
            Drawable drawablePic = getResources().getDrawable(resID);
            MainActivity.mainLayout.setBackground(drawablePic);
        } else {
            MainActivity.mainLayout.setBackgroundColor(Color.parseColor("#ffffff"));
        }

        Log.i("onResume", "Setting Foreground to " + isInForeground);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        settings.saveSetups();
        isInForeground = false;
        Log.i("onResume", "Setting Foreground to " + isInForeground);
    }
}