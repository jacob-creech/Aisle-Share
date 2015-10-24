package com.aisleshare;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class ShoppingList extends AppCompatActivity {
    private SharedPreferences sp;
    public final static String LIST_NAME = "com.ShoppingList.MESSAGE";
    public final static String SHOP_PREF = "ShoppingPreferences";
    private ListView listView;
    private ArrayList<ListItem> items;
    private ArrayList<ListItem> items_backup;
    //private ArrayList<String> shoppingLists;
    private Map<String, MenuItem> menuItems;
    //private Set<String> shoppingSet;
    private CustomListAdapter itemAdapter;
    private boolean isIncreasingOrder;
    private String deviceName;
    private int currentOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        sp = getSharedPreferences(SHOP_PREF, Context.MODE_PRIVATE);
        listView = (ListView)findViewById(R.id.shoppingLists);
        //Set<String> defSet = new HashSet<>();
        //shoppingSet = sp.getStringSet("ShoppingSets", defSet);
        //shoppingLists = new ArrayList<>(shoppingSet);
        menuItems = new HashMap<>();
        items = new ArrayList<>();
        items_backup = new ArrayList<>();
        isIncreasingOrder = true;
        currentOrder = -1;
        deviceName = Settings.Secure.getString(ShoppingList.this.getContentResolver(), Settings.Secure.ANDROID_ID);


        if(items.size() == 0){
            TextView emptyNotice = (TextView) findViewById(R.id.empty_notice);
            emptyNotice.setVisibility(View.VISIBLE);
        }

        itemAdapter = new CustomListAdapter(this, items);
        listView.setAdapter(itemAdapter);

        FloatingActionButton addButton = (FloatingActionButton) findViewById(R.id.float_button);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addListDialog();
            }
        });
    }

    public void sortList(boolean reverseOrder, int order) {
        if(reverseOrder) {
            isIncreasingOrder = !isIncreasingOrder;
        }
        if(order != currentOrder){
            currentOrder = order;
            isIncreasingOrder = true;
        }

        ListItemComparator compare = new ListItemComparator(ShoppingList.this);

        // Unsorted
        if(currentOrder == -1){
            menuItems.get("sort").setIcon(0);
            menuItems.get("unsorted").setVisible(false);
            return;
        }
        else{
            menuItems.get("unsorted").setVisible(true);
        }

        switch (currentOrder){
            // Name
            case 0:{
                ListItemComparator.Name sorter = compare.new Name();
                Collections.sort(items, sorter);
                break;}
            // Time Created
            case 1:{
                ListItemComparator.Created sorter = compare.new Created();
                Collections.sort(items, sorter);
                break;}
            // Owner
            case 2:{
                ListItemComparator.Owner sorter = compare.new Owner();
                Collections.sort(items, sorter);
                break;}
        }

        if(isIncreasingOrder) {
            menuItems.get("sort").setIcon(R.mipmap.inc_sort);
        }
        else{
            Collections.reverse(items);
            menuItems.get("sort").setIcon(R.mipmap.dec_sort);
        }
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

                    for(int index = 0; index < items.size(); index++){
                        if(items.get(index).equals(name)){
                            listName.setError("List already exists...");
                            return;
                        }
                    }
                    ListItem m = new ListItem(deviceName, name); //need to add time at some point
                    items.add(m);
                    sortList(false, currentOrder);
                    dialog.dismiss();

                    Intent intent = new Intent(ShoppingList.this, CurrentList.class);

                    //SharedPreferences.Editor editor = sp.edit();
                    //shoppingSet.add(name);
                    //shoppingLists.add(name);
                    itemAdapter.notifyDataSetChanged();
                    //editor.putStringSet("ShoppingSets", shoppingSet);
                    //editor.commit();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_shopping_list, menu);
        menuItems.put("sort", menu.findItem(R.id.sort_root));
        menuItems.put("name", menu.findItem(R.id.sort_name));
        menuItems.put("time", menu.findItem(R.id.sort_time));
        menuItems.put("owner", menu.findItem(R.id.sort_owner));
        menuItems.put("unsorted", menu.findItem(R.id.unsorted));
        menuItems.put("delete", menu.findItem(R.id.delete_items));

        menuItems.get("name").setCheckable(true);
        menuItems.get("time").setCheckable(true);
        menuItems.get("owner").setCheckable(true);
        menuItems.get("unsorted").setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem option) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = option.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id) {
            case R.id.sort_name:
                sortList(true, 0);
                clearMenuCheckables();
                option.setChecked(true);
                break;
            case R.id.sort_time:
                sortList(true, 1);
                clearMenuCheckables();
                option.setChecked(true);
                break;
            case R.id.sort_owner:
                sortList(true, 2);
                clearMenuCheckables();
                option.setChecked(true);
                break;
            case R.id.unsorted:
                sortList(false, -1);
                clearMenuCheckables();
                break;
            case R.id.delete_items:
                deleteItems();
                break;
            case R.id.sort:
                return super.onOptionsItemSelected(option);
        }
        itemAdapter.notifyDataSetChanged();
        return super.onOptionsItemSelected(option);
    }

    public void clearMenuCheckables(){
        menuItems.get("name").setChecked(false);
        menuItems.get("time").setChecked(false);
        menuItems.get("owner").setChecked(false);
    }

    public void deleteItems(){
        // custom dialog
        final Dialog dialog = new Dialog(ShoppingList.this);
        dialog.setContentView(R.layout.delete_items_dialog);
        dialog.setTitle("What Should We Delete?");

        final Button cancel = (Button) dialog.findViewById(R.id.cancel);
        final Button delete_all = (Button) dialog.findViewById(R.id.delete_all);
        final Button delete_checked = (Button) dialog.findViewById(R.id.delete_checked);


        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        delete_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout undoBox = (LinearLayout) findViewById(R.id.undo_box);
                boolean removals = false;

                items_backup.clear();
                for (ListItem item : items) {
                    items_backup.add(item);
                }

                int length = items.size();
                for (int index = length - 1; index > -1; index--) {
                    if (deviceName.equals(items.get(index).getOwner())) {
                        items.remove(index);
                        removals = true;
                    }
                }
                itemAdapter.notifyDataSetChanged();
                if (items.size() == 0) {
                    TextView emptyNotice = (TextView) findViewById(R.id.empty_notice);
                    emptyNotice.setVisibility(View.VISIBLE);
                }
                if (removals) {
                    undoBox.setVisibility(View.VISIBLE);
                }
                dialog.dismiss();
                hideUndoBoxTimer();
            }
        });

        /*delete_checked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout undoBox = (LinearLayout) findViewById(R.id.undo_box);
                boolean removals = false;

                items_backup.clear();
                for (Item item : items) {
                    items_backup.add(item);
                }

                int length = items.size();
                for (int index = length - 1; index > -1; index--) {
                    if (deviceName.equals(items.get(index).getOwner()) &&
                            items.get(index).getChecked()) {
                        items.remove(index);
                        removals = true;
                    }
                }
                itemAdapter.notifyDataSetChanged();
                if (items.size() == 0) {
                    TextView emptyNotice = (TextView) findViewById(R.id.empty_notice);
                    emptyNotice.setVisibility(View.VISIBLE);
                }
                if (removals) {
                    undoBox.setVisibility(View.VISIBLE);
                }
                dialog.dismiss();
                hideUndoBoxTimer();
            }
        });*/

        dialog.show();
    }
    public void hideUndoBoxTimer(){
        new CountDownTimer(10000, 10000) {
            public void onTick(long millisUntilFinished) {}
            public void onFinish() {
                LinearLayout undoBox = (LinearLayout) findViewById(R.id.undo_box);
                undoBox.setVisibility(View.INVISIBLE);
            }
        }.start();
    }
}
