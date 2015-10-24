package com.aisleshare;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

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
    private ArrayAdapter<String> itemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        sp = getSharedPreferences(SHOP_PREF, Context.MODE_PRIVATE);
        listView = (ListView)findViewById(R.id.shoppingLists);
        Set<String> defSet = new HashSet<>();
        shoppingSet = sp.getStringSet("ShoppingSets", defSet);
        shoppingLists = new ArrayList<>(shoppingSet);

        if(shoppingLists.size() == 0){
            TextView emptyNotice = (TextView) findViewById(R.id.empty_notice);
            emptyNotice.setVisibility(View.VISIBLE);
        }

        itemAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, shoppingLists);
        listView.setAdapter(itemAdapter);

        FloatingActionButton addButton = (FloatingActionButton) findViewById(R.id.float_button);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addListDialog();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long id) {

                Intent intent = new Intent(ShoppingList.this, CurrentList.class);
                String name = shoppingLists.get(pos);
                intent.putExtra(LIST_NAME, name);
                startActivity(intent);
            }
        });
    }

    // Popup for adding a List
    public void addListDialog(){
        // custom dialog
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.add_list_dialog);
        dialog.setTitle("Add a New List");

        final EditText listName = (EditText) dialog.findViewById(R.id.Name);
        final Button cancel = (Button) dialog.findViewById(R.id.Cancel);
        final Button done = (Button) dialog.findViewById(R.id.Done);

        // Open keyboard automatically
        listName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!listName.getText().toString().isEmpty()) {
                    String name = listName.getText().toString();
                    TextView emptyNotice = (TextView) findViewById(R.id.empty_notice);

                    for(int index = 0; index < shoppingLists.size(); index++){
                        if(shoppingLists.get(index).equals(name)){
                            listName.setError("List already exists...");
                            return;
                        }
                    }

                    dialog.dismiss();

                    Intent intent = new Intent(ShoppingList.this, CurrentList.class);


                    shoppingSet.add(name);
                    shoppingLists.add(name);
                    itemAdapter.notifyDataSetChanged();
                    updateStorage();
                    emptyNotice.setVisibility(View.INVISIBLE);

                    intent.putExtra(LIST_NAME, name);
                    startActivity(intent);
                }
                else{
                    listName.setError("Name is empty...");
                }
            }
        });

        dialog.show();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        updateStorage();
    }

    @Override
    public void onStop(){
        super.onStop();
        updateStorage();
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        updateStorage();
    }

    @Override
    public void onPause(){
        super.onPause();
        updateStorage();
    }

    public void updateStorage(){
        SharedPreferences.Editor editor = sp.edit();
        editor.putStringSet("ShoppingSets", shoppingSet);
        editor.commit();
        editor.apply();

        editor.remove("ShoppingSets");
        editor.apply();
        editor.putStringSet("ShoppingSets", shoppingSet);
        editor.apply();
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
