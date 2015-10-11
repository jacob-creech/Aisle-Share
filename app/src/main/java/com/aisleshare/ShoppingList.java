package com.aisleshare;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.getbase.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class ShoppingList extends AppCompatActivity {
    private SharedPreferences sp;
    public final static String LIST_NAME = "com.ShoppingList.MESSAGE";
    public final static String SHOP_PREF = "ShoppingPreferences";
    private ListView listView;
    private ArrayList<String> shoppingLists;
    private Set<String> shoppingSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        sp = getSharedPreferences(SHOP_PREF, Context.MODE_PRIVATE);
        listView = (ListView)findViewById(R.id.shoppingLists);
        Set<String> defSet = new HashSet<>();
        shoppingSet = sp.getStringSet("ShoppingSets", defSet);
        shoppingLists = new ArrayList<>(shoppingSet);


        final ArrayAdapter<String> itemAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, shoppingLists);
        listView.setAdapter(itemAdapter);

        FloatingActionButton addButton = (FloatingActionButton) findViewById(R.id.float_button);

        // TODO: Remove from onCreate
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(ShoppingList.this, AddListMenu.class));

                final AlertDialog modal = new AlertDialog.Builder(ShoppingList.this).create();
                modal.setTitle("Add a New List");

                // Set up the input
                final EditText input = new EditText(ShoppingList.this);
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

                // Positive Button
                modal.setButton(-1, "Done", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (!input.getText().toString().isEmpty()) {
                            Intent intent = new Intent(ShoppingList.this, CurrentList.class);
                            String listInput = input.getText().toString();

                            SharedPreferences.Editor editor = sp.edit();
                            shoppingSet.add(listInput);
                            shoppingLists.add(listInput);
                            itemAdapter.notifyDataSetChanged();
                            editor.putStringSet("ShoppingSets", shoppingSet);
                            editor.commit();

                            intent.putExtra(LIST_NAME, listInput);
                            startActivity(intent);
                        }
                    }
                });

                // Negative Button
                modal.setButton(-2, "Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
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
