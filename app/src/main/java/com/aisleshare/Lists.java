package com.aisleshare;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
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

public class Lists extends Fragment {
    private SharedPreferences sp;
    public final static String LIST_NAME = "com.ShoppingList.MESSAGE";
    public final static String SHOP_PREF = "ShoppingPreferences";
    private ListView listView;
    private ArrayList<ListItem> listedItems;
    private ArrayList<ListItem> listedItems_backup;
    private ArrayList<String> lists;
    private Map<String, MenuItem> menuLists;
    private Set<String> listSet;
    private CustomListAdapter itemAdapter;
    private Context dashboard;
    private TextView emptyNotice;
    private boolean isIncreasingOrder;
    private int currentOrder;
    private String deviceName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lists, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        dashboard = getActivity();
        sp = dashboard.getSharedPreferences(SHOP_PREF, Context.MODE_PRIVATE);
        listView = (ListView) getView().findViewById(R.id.lists);
        Set<String> defSet = new HashSet<>();
        listSet = sp.getStringSet("ShoppingSets", defSet);
        lists = new ArrayList<>(listSet);
        listedItems = new ArrayList<>();
        isIncreasingOrder = true;
        currentOrder = -1;
        menuLists = new HashMap<>();
        //listedItems_backup = new ArrayList<>();
        emptyNotice = (TextView) getView().findViewById(R.id.empty_notice);
        deviceName = Settings.Secure.getString(dashboard.getContentResolver(), Settings.Secure.ANDROID_ID);

        if(lists.size() == 0){
            emptyNotice.setVisibility(View.VISIBLE);
        }
        itemAdapter = new CustomListAdapter(dashboard, listedItems, R.layout.row_dashboard);
        listView.setAdapter(itemAdapter);

        FloatingActionButton addButton = (FloatingActionButton) getView().findViewById(R.id.float_button);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addListDialog();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long id) {

                Intent intent = new Intent(dashboard, CurrentList.class);
                String name = lists.get(pos);
                intent.putExtra(LIST_NAME, name);
                startActivity(intent);
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

        ListItemComparator compare = new ListItemComparator(dashboard);

        // Unsorted
        if(currentOrder == -1){
            menuLists.get("sort").setIcon(0);
            menuLists.get("unsorted").setVisible(false);
            return;
        }
        else{
            menuLists.get("unsorted").setVisible(true);
        }

        switch (currentOrder){
            // Name
            case 0:{
                ListItemComparator.Name sorter = compare.new Name();
                Collections.sort(listedItems, sorter);
                break;}
            // Time Created
            case 1:{
                ListItemComparator.Created sorter = compare.new Created();
                Collections.sort(listedItems, sorter);
                break;}
            // Owner
            case 2:{
                ListItemComparator.Owner sorter = compare.new Owner();
                Collections.sort(listedItems, sorter);
                break;}
        }

        if(isIncreasingOrder) {
            menuLists.get("sort").setIcon(R.mipmap.inc_sort);
        }
        else{
            Collections.reverse(listedItems);
            menuLists.get("sort").setIcon(R.mipmap.dec_sort);
        }
    }

    // Popup for adding a List
    public void addListDialog(){
        // custom dialog
        final Dialog dialog = new Dialog(dashboard);
        dialog.setContentView(R.layout.dialog_add_name);
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

                    for(int index = 0; index < lists.size(); index++){
                        if(lists.get(index).equals(name)){
                            listName.setError("List already exists...");
                            return;
                        }
                    }

                    ListItem m = new ListItem(deviceName, name); //need to add time at some point
                    listedItems.add(m);
                    sortList(false, currentOrder);

                    dialog.dismiss();

                    Intent intent = new Intent(dashboard, CurrentList.class);

                    listSet.add(name);
                    lists.add(name);
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

    /*@Override
    public void onBackPressed(){
        super.onBackPressed();
        updateStorage();
    }*/

    @Override
    public void onPause(){
        super.onPause();
        updateStorage();
    }

    public void updateStorage(){
        SharedPreferences.Editor editor = sp.edit();
        editor.putStringSet("ShoppingSets", listSet);
        editor.commit();
        editor.apply();

        editor.remove("ShoppingSets");
        editor.apply();
        editor.putStringSet("ShoppingSets", listSet);
        editor.apply();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuLists.put("sort", menu.findItem(R.id.sort_root));
        menuLists.put("name", menu.findItem(R.id.sort_name));
        menuLists.put("time", menu.findItem(R.id.sort_time));
        menuLists.put("owner", menu.findItem(R.id.sort_owner));
        menuLists.put("unsorted", menu.findItem(R.id.unsorted));
        menuLists.put("delete", menu.findItem(R.id.delete_items));

        menuLists.get("name").setCheckable(true);
        menuLists.get("time").setCheckable(true);
        menuLists.get("owner").setCheckable(true);
        menuLists.get("unsorted").setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);

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
                //deleteItems();
                break;
            case R.id.sort:
                return super.onOptionsItemSelected(option);
        }
        itemAdapter.notifyDataSetChanged();
        return super.onOptionsItemSelected(option);
    }

    public void clearMenuCheckables(){
        menuLists.get("name").setChecked(false);
        menuLists.get("time").setChecked(false);
        menuLists.get("owner").setChecked(false);
    }

    /*public void deleteItems(){
        // custom dialog
        final Dialog dialog = new Dialog(editme.this); //has a problem, not caring atm
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
        });*/

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
    });

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
    }*/
}
