package com.aisleshare;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;


public class CurrentList extends AppCompatActivity {


    private ListView listView;
    private ArrayList<String> itemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_list);

        listView = (ListView)findViewById(R.id.currentItems);
        itemList = new ArrayList<>();

        final ArrayAdapter<String> itemAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, itemList);
        listView.setAdapter(itemAdapter);

        String listTitle = "";
        if (savedInstanceState == null) {
            listTitle = getIntent().getStringExtra("com.ShoppingList.MESSAGE");
        }
        else {
            listTitle = (String) savedInstanceState.getSerializable("com.ShoppingList.MESSAGE");
        }
        getSupportActionBar().setTitle(listTitle);

        FloatingActionButton addButton = (FloatingActionButton) findViewById(R.id.float_button);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItem(itemAdapter);
            }
        });
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

    public void addItem(final ArrayAdapter<String> itemAdapter){
        //startActivity(new Intent(ShoppingList.this, AddListMenu.class));

        final AlertDialog modal = new AlertDialog.Builder(CurrentList.this).create();
        modal.setTitle("Add a New Item");

        // Set up the input
        final EditText input = new EditText(CurrentList.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setSingleLine(true);
        modal.setView(input);

        // Open keyboard automatically
        input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    modal.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });

        // Done Button
        modal.setButton(-1, "Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (!input.getText().toString().isEmpty()) {
                    String text = input.getText().toString();
                    itemList.add(text);
                    itemAdapter.notifyDataSetChanged();
                }
            }
        });

        // More Button
        modal.setButton(-2, "More", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (!input.getText().toString().isEmpty()) {
                    String text = input.getText().toString();
                    itemList.add(text);
                    itemAdapter.notifyDataSetChanged();
                }
                addItem(itemAdapter);
            }
        });

        // Cancel Button
        modal.setButton(-3, "Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        modal.show();
    }
}
