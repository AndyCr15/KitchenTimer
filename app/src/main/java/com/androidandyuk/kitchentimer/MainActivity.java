package com.androidandyuk.kitchentimer;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ListView itemListView;
    List<timerItem> itemList = new ArrayList<>();
    int longestTimer;
    boolean timerIsActive = false;
    Button timerButton;
    CountDownTimer countDownTimer;

    public void timerButtonPressed(View view) {

        if (timerIsActive) {
            //code for when the timer is already running
            Log.i("Timer button pressed : ", "Already running");
            timerButton.setText("Start Timer");
            timerIsActive = false;
            resetTimer();
        } else {
            //code if timer is not running
            longestTimer = (itemList.get(itemList.size() - 1)).getTotalTime();
            Log.i("Starting Timer", "Longest time is " + (itemList.get(itemList.size() - 1)).getTotalTime());
            Collections.sort(itemList);
            timerIsActive = true;
            timerButton.setText("Stop");
            startTimer(longestTimer);
        }
    }

    public void startTimer(int seconds) {

        Log.i("Timer started : ", "" + Integer.valueOf(seconds));
        Toast.makeText(MainActivity.this, "Timer started : " + timeInMinutes(seconds), Toast.LENGTH_SHORT).show();
        countDownTimer = new CountDownTimer(seconds * 1000 + 100, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {

                timerButton.setText(timeInMinutes((int) millisUntilFinished / 1000));

            }

            @Override
            public void onFinish() {

                // actiojn to take on end of alarm
                Toast.makeText(MainActivity.this, "Times up!", Toast.LENGTH_LONG).show();
                resetTimer();

            }
        }.start();

    }

    public void resetTimer() {

        timerButton.setText("Start Timer");
        countDownTimer.cancel();
        timerIsActive = false;

    }

    public String timeInMinutes(int seconds) {
        String mins = Integer.toString(seconds / 60);
        String secs = Integer.toString(seconds % 60);
        if (secs.length() < 2) {
            secs = "0" + secs;
        }
        return mins + ":" + secs + " minutes";
    }

    public void addItem(View view) {

        EditText itemName = (EditText) findViewById(R.id.itemName);
        EditText itemTime = (EditText) findViewById(R.id.itemTime);
        EditText itemFinish = (EditText) findViewById(R.id.finishTime);

        if (itemName.length() == 0 || itemName.equals("") || itemTime.length() == 0 || itemTime.equals("")) {
            Toast.makeText(getApplicationContext(), "Enter item details",
                    Toast.LENGTH_SHORT).show();
        } else {

            Log.i("Item Added :", itemName.getText().toString());
            Log.i("Item Added :", itemTime.getText().toString());
            Log.i("Item Added :", itemFinish.getText().toString());

            String value = itemTime.getText().toString();
            int seconds = Integer.parseInt(value);

            int finish;

            String value2 = itemFinish.getText().toString();

            try {
                finish = Integer.parseInt(value2);
            } catch (Exception e) {
                finish = 0;
            }

            timerItem next = new timerItem(itemName.getText().toString(), seconds, finish);
            itemList.add(next);

            itemName.setText(null);
            itemTime.setText(null);
            itemFinish.setText(null);
            itemName.clearFocus();
            itemTime.clearFocus();
            itemFinish.clearFocus();

            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
        sortMyList();
    }

    public void sortMyList(){
        Collections.sort(itemList);
        ArrayAdapter<timerItem> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, itemList);

        itemListView.setAdapter(arrayAdapter);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        itemListView = (ListView) findViewById(R.id.itemListView);
        timerButton = (Button) findViewById(R.id.timerButton);
        timerButton.setText("Start Timer");

        timerItem egg = new timerItem("Egg", 180, 0);
        timerItem toast = new timerItem("Toast", 120, 0);
        timerItem bacon = new timerItem("Bacon", 300, 0);
        itemList.add(egg);
        itemList.add(toast);
        itemList.add(bacon);

        sortMyList();

        ArrayAdapter<timerItem> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, itemList);

        itemListView.setAdapter(arrayAdapter);

        itemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Toast.makeText(MainActivity.this, "Removing " + itemList.get(position), Toast.LENGTH_SHORT).show();
                itemList.remove(position);
                sortMyList();

            }
        });


    }

}

