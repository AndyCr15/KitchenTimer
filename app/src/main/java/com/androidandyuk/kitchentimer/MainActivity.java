package com.androidandyuk.kitchentimer;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView itemListView;
    ArrayList<timerItem> itemList = new ArrayList<>();

    public void addItem(View view) {

        EditText itemName = (EditText) findViewById(R.id.itemName);
        EditText itemTime = (EditText) findViewById(R.id.itemTime);
        EditText itemFinish = (EditText) findViewById(R.id.finishTime);

        if(itemName.length() == 0 || itemName.equals("") || itemTime.length() == 0 || itemTime.equals(""))
        {
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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        itemListView = (ListView) findViewById(R.id.itemListView);


        timerItem egg = new timerItem("Egg", 180, 0);
        timerItem toast = new timerItem("Toast", 120, 0);
        itemList.add(egg);
        itemList.add(toast);

        ArrayAdapter<timerItem> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, itemList);

        itemListView.setAdapter(arrayAdapter);

    }
}
