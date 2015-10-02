package com.aisleshare;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by Jacob on 10/1/2015.
 */
public class AddListMenu extends Activity {

    Button addButton, cancelButton;
    EditText listName;
    public final static String LIST_NAME = "com.ShoppingList.MESSAGE";

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addlistmenu);

        addButton = (Button)findViewById(R.id.listAdd);
        cancelButton = (Button)findViewById(R.id.listCancel);
        listName = (EditText)findViewById(R.id.editText);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        getWindow().setLayout((int)(width*.9), (int)(height*.25));

        addButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addNewList(listName.getText().toString());
                    }
                }
        );

        cancelButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                }
        );
    }

    public void addNewList(String listName){
        Intent intent = new Intent(this, CurrentList.class);
        intent.putExtra(LIST_NAME, listName);
        startActivity(intent);
    }
}
