package com.androidandyuk.kitchentimer;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;

import static android.util.Log.i;
import static com.androidandyuk.kitchentimer.MainActivity.ed;
import static com.androidandyuk.kitchentimer.MainActivity.itemList;
import static com.androidandyuk.kitchentimer.MainActivity.savedSetups;
import static com.androidandyuk.kitchentimer.MainActivity.sharedPreferences;
import static com.androidandyuk.kitchentimer.timerItem.findItem;
import static java.lang.String.valueOf;

public class settings extends AppCompatActivity {

    int maxTime;
    boolean warningsWanted;
    SeekBar maxTimeSeekBar;
    ToggleButton warningsWantedButton;
    TextView maxTimeTextBox;
    EditText timerName;
    Spinner deleteItem;
    Button deleteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        maxTimeSeekBar = (SeekBar) findViewById(R.id.maxTimeSeekBar);
        warningsWantedButton = (ToggleButton) findViewById(R.id.warningsToggle);
        maxTimeTextBox = (TextView) findViewById(R.id.maxTimeTextBox);

//        Intent intent = getIntent();
//        maxTime = intent.getIntExtra("maxTime", maxTime);
//        warningsWanted = intent.getBooleanExtra("warningsWanted", warningsWanted);

        maxTimeSeekBar.setMax(19);
        maxTimeSeekBar.setProgress(MainActivity.maxTime / 600);

        warningsWantedButton.setChecked(MainActivity.warningsWanted);

        maxTimeTextBox.setText(timerItem.timeInMinutes(maxTime * 1000, 1));

        maxTimeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                MainActivity.maxTime = (progress + 1) * 600;
                maxTimeTextBox.setText(timerItem.timeInMinutes(MainActivity.maxTime * 1000, 1));
                i("Max Time", "" + MainActivity.maxTime);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        warningsWantedButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    MainActivity.warningsWanted = true;
                    i("Warnings Wanted", "" + warningsWanted);
                } else {
                    // The toggle is disabled
                    MainActivity.warningsWanted = false;
                    i("Warnings Wanted", "" + warningsWanted);
                }
            }
        });

        // setup the delete setup spinner
        final Spinner deleteItem = (Spinner)findViewById(R.id.deleteSpinner);
        ArrayList<timerSetup> items = savedSetups;
        ArrayAdapter<timerSetup> adapter = new ArrayAdapter<timerSetup>(this, android.R.layout.simple_spinner_dropdown_item, items);
        deleteItem.setAdapter(adapter);
        deleteButton = (Button)findViewById(R.id.deleteButton);

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Delete ", "" + valueOf(deleteItem.getSelectedItem()));
                Toast.makeText(settings.this, "Deleting" + valueOf(deleteItem.getSelectedItem()), Toast.LENGTH_LONG).show();
                savedSetups.remove(deleteItem.getSelectedItem());
                finish();
            }
        });

    }

    public void saveSetup(View view) {
        EditText setupName = (EditText) findViewById(R.id.setupName);

        // first, save the setup into the memory held savedSetups
        timerSetup thisSetup = new timerSetup();
        thisSetup.setupName = setupName.getText().toString();
        for (timerItem thisItem : itemList) {
            thisSetup.itemsSetup.add(thisItem);
        }
        savedSetups.add(thisSetup);

        // hide keyboard
        View thisViewHere = this.getCurrentFocus();
        if (thisViewHere != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(thisViewHere.getWindowToken(), 0);
        }
        onBackPressed();
    }

    public static void saveSetups() {
        i("Settings", "saveSetups");
        // save the size of the list, so it can be checked when loading
        ed.putInt("size", savedSetups.size()).apply();
        i("Saving", "Amount of Setups : " + savedSetups.size());
        //save into the next available place
        for (int x = 0; x < savedSetups.size(); x++) {
            timerSetup thisSetup = savedSetups.get(x);

            i("Setup : ", "" + thisSetup);


            // name of this setup
            ed.putString("setupName" + x, thisSetup.setupName).apply();

            // save the settings for this setup too
            ed.putInt("maxTime" + x, thisSetup.maxTime).apply();
            ed.putBoolean("warningsWanted" + x, thisSetup.warningsWanted).apply();
            ed.putInt("itemListSize" + x, thisSetup.itemsSetup.size()).apply();
            // save the items for this setup
            int y = 0;
            for (timerItem thisItem : itemList) {
                ed.putString("name" + x + y, thisItem.getName()).apply();
                ed.putString("note" + x + y, thisItem.note).apply();
                ed.putLong("milliSeconds" + x + y, thisItem.milliSeconds).apply();
                ed.putLong("finishBy" + x + y, thisItem.finishBy).apply();
                int nextItemPos = -1;
                if (thisItem.nextItem != null) {
                    nextItemPos = findItem(thisItem.nextItem);
                }
                i("Next Item Pos", "" + nextItemPos);
                ed.putInt("nextItemPos" + x + y, nextItemPos).apply();
                y++;
            }
        }
    }

    public static void loadSetups() {
        i("Settings", "loadSetups");
        //c;ear previous setups
        savedSetups.clear();
        itemList.clear();

        int listSize = sharedPreferences.getInt("size", 0);
        i("Loading", "Setups :" + listSize);
        for (int x = 0; x < listSize; x++) {
            // initialize the new setup item
            timerSetup thisSetup = new timerSetup();
            thisSetup.setupName = sharedPreferences.getString("setupName" + x, "Unknown");
            thisSetup.maxTime = sharedPreferences.getInt("maxTime" + x, 1500);
            thisSetup.warningsWanted = sharedPreferences.getBoolean("warningsWanted" + x, true);

            int itemListSize = sharedPreferences.getInt("itemListSize" + x, 0);
            for (int y = 0; y < itemListSize; y++) {
                String thisName = sharedPreferences.getString("name" + x + y, "Unknown");
                String thisNote = sharedPreferences.getString("note" + x + y, "");
                long thisMilliSeconds = sharedPreferences.getLong("milliSeconds" + x + y, 0);
                long thisfinishBy = sharedPreferences.getLong("finishBy" + x + y, 0);

                timerItem thisItem = new timerItem(thisName, thisMilliSeconds, thisfinishBy, thisNote);

                int nextItemPos = sharedPreferences.getInt("nextItemPos" + x + y, -1);
                i("Loading Item :" + thisItem,"Next Item Pos" + nextItemPos);
                if (nextItemPos >= 0) {
                    thisItem.nextItem = thisSetup.itemsSetup.get(nextItemPos);
                }
                thisSetup.itemsSetup.add(thisItem);
            }
            savedSetups.add(thisSetup);
        }

    }

    public void saveSettings() {
        i("Settings", "Saving Settings");

        ed.putBoolean("warningsWanted", MainActivity.warningsWanted).apply();
        ed.putInt("maxTime", MainActivity.maxTime).apply();


    }

    public static void loadSettings() {
        MainActivity.maxTime = sharedPreferences.getInt("maxTime", 1500);
        MainActivity.warningsWanted = sharedPreferences.getBoolean("warningsWanted", true);
    }

    public void playVideoInstructions(View view) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=G0MmtLZp9u8")));
        i("Video", "Video Playing....");
    }

    @Override
    protected void onPause() {
        super.onPause();
        i("Settings", "On Pause");
        saveSettings();
        saveSetups();
    }
}
