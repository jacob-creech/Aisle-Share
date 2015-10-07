package com.aisleshare;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.getbase.floatingactionbutton.FloatingActionButton;


public class ShoppingList extends ActionBarActivity {
    public final static String LIST_NAME = "com.ShoppingList.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        FloatingActionButton b = (FloatingActionButton) findViewById(R.id.float_button);

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(ShoppingList.this, AddListMenu.class));

                AlertDialog modal = new AlertDialog.Builder(ShoppingList.this).create();
                modal.setTitle("Add a New List");

                // Set up the input
                final EditText input = new EditText(ShoppingList.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setSingleLine(true);
                modal.setView(input);

                // Positive Button
                modal.setButton(-1, "Add", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(ShoppingList.this, CurrentList.class);
                        intent.putExtra(LIST_NAME, input.getText().toString());
                        startActivity(intent);
                    }
                });

                // Negative Button
                modal.setButton(-2, "Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which){
                    }
                });

                modal.show();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_shopping_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
