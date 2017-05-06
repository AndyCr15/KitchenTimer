package com.androidandyuk.kitchentimer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

public class settings extends AppCompatActivity {

    int maxTime;
    boolean warningsWanted;
    SeekBar maxTimeSeekBar;
    ToggleButton warningsWantedButton;
    TextView maxTimeTextBox;

    public void saveSettings(View view){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);

        intent.putExtra("maxTime", maxTime);
        intent.putExtra("warningsWanted", warningsWanted);

        startActivity(intent);
    }

    public void seeMoreApps(View view){
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=AAUK")));
        Log.i("See More Apps", "Logged");
    }

    public void playVideoInstructions(View view){
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=G0MmtLZp9u8")));
        Log.i("Video", "Video Playing....");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        maxTimeSeekBar = (SeekBar)findViewById(R.id.maxTimeSeekBar);
        warningsWantedButton = (ToggleButton)findViewById(R.id.warningsToggle);
        maxTimeTextBox =(TextView) findViewById(R.id.maxTimeTextBox);

        Intent intent = getIntent();
        maxTime = intent.getIntExtra("maxTime", maxTime);
        warningsWanted = intent.getBooleanExtra("warningsWanted", warningsWanted);

        maxTimeSeekBar.setMax(19);
        maxTimeSeekBar.setProgress(maxTime/600);

        warningsWantedButton.setChecked(warningsWanted);

        maxTimeTextBox.setText(timerItem.timeInMinutes(maxTime*1000, 1));

        maxTimeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                maxTime = (progress+1)*600;
                maxTimeTextBox.setText(timerItem.timeInMinutes(maxTime*1000, 1));
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
                if(isChecked){
                    // The toggle is enabled
                    warningsWanted = true;
                    Log.i("Warnings Wanted", "" + warningsWanted);
                } else {
                    // The toggle is disabled
                    warningsWanted = false;
                    Log.i("Warnings Wanted", "" + warningsWanted);
                }
            }
        });
    }
}
