package com.aisleshare;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.hudomju.swipe.SwipeToDismissTouchListener;
import com.hudomju.swipe.adapter.ListViewAdapter;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class CurrentList extends AppCompatActivity {

    // Class Variables
    private ListView listView;
    private ArrayList<Item> items;
    private ArrayList<Item> items_backup;
    private CustomAdapter customAdapter;
    private boolean isIncreasingOrder;
    private int currentOrder;
    private Map<String, MenuItem> menuItems;
    private String deviceName;
    private Set<String> currentSet;
    private SharedPreferences settings;
    private String listTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_list);

        //TODO: put block in function
        settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //sp = getSharedPreferences("ShoppingPreferences", Context.MODE_PRIVATE);
        setListTitle(savedInstanceState);
        Set<String> defSet = new HashSet<>();
        currentSet = settings.getStringSet(listTitle, defSet);



        listView = (ListView)findViewById(R.id.currentItems);
        items = new ArrayList<>();
        items_backup = new ArrayList<>();
        isIncreasingOrder = true;
        currentOrder = -1;

        deviceName = Settings.Secure.getString(CurrentList.this.getContentResolver(), Settings.Secure.ANDROID_ID);
        menuItems = new HashMap<>();

        System.out.println("ONCREATE CURRENTLIST");

        ArrayList<String> jsonItems = new ArrayList<>(currentSet);
        JSONObject obj;
        for(int i = 0; i < jsonItems.size(); i++){
            try {
                obj = new JSONObject(jsonItems.get(i));
                items.add(new Item(
                        obj.getString("owner"),
                        obj.getString("name"),
                        obj.getString("type"),
                        obj.getInt("quantity"),
                        obj.getString("units"),
                        obj.getInt("timeCreated"),
                        obj.getBoolean("checked")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        customAdapter = new CustomAdapter(this, items);
        listView.setAdapter(customAdapter);

        //setupTestItems();
        setListeners();
        setSwipeAdapter();
    }

    // Sorted based on the order index parameter
    public void sortList(boolean reverseOrder, int order) {
        if(reverseOrder) {
            isIncreasingOrder = !isIncreasingOrder;
        }
        if(order != currentOrder){
            currentOrder = order;
            isIncreasingOrder = true;
        }

        ItemComparator compare = new ItemComparator(CurrentList.this);

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
                ItemComparator.Name sorter = compare.new Name();
                Collections.sort(items, sorter);
                break;}
            // Quantity
            case 1:{
                ItemComparator.Quantity sorter = compare.new Quantity();
                Collections.sort(items, sorter);
                break;}
            // Time Created
            case 2:{
                ItemComparator.Created sorter = compare.new Created();
                Collections.sort(items, sorter);
                break;}
            // Type
            case 3:{
                ItemComparator.Type sorter = compare.new Type();
                Collections.sort(items, sorter);
                break;}
            // Owner
            case 4:{
                ItemComparator.Owner sorter = compare.new Owner();
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

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_current_list, menu);
        menuItems.put("share", menu.findItem(R.id.share));
        menuItems.put("sort", menu.findItem(R.id.sort_root));
        menuItems.put("name", menu.findItem(R.id.sort_name));
        menuItems.put("type", menu.findItem(R.id.sort_type));
        menuItems.put("time", menu.findItem(R.id.sort_time));
        menuItems.put("quantity", menu.findItem(R.id.sort_quantity));
        menuItems.put("owner", menu.findItem(R.id.sort_owner));
        menuItems.put("unsorted", menu.findItem(R.id.unsorted));
        menuItems.put("delete", menu.findItem(R.id.delete_items));

        menuItems.get("name").setCheckable(true);
        menuItems.get("type").setCheckable(true);
        menuItems.get("time").setCheckable(true);
        menuItems.get("quantity").setCheckable(true);
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
            case R.id.sort_quantity:
                sortList(true, 1);
                clearMenuCheckables();
                option.setChecked(true);
                break;
            case R.id.sort_time:
                sortList(true, 2);
                clearMenuCheckables();
                option.setChecked(true);
                break;
            case R.id.sort_type:
                sortList(true, 3);
                clearMenuCheckables();
                option.setChecked(true);
                break;
            case R.id.sort_owner:
                sortList(true, 4);
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

        customAdapter.notifyDataSetChanged();
        return super.onOptionsItemSelected(option);
    }

    public void clearMenuCheckables(){
        menuItems.get("name").setChecked(false);
        menuItems.get("type").setChecked(false);
        menuItems.get("time").setChecked(false);
        menuItems.get("quantity").setChecked(false);
        menuItems.get("owner").setChecked(false);
    }

    // Popup for adding an Item
    public void addItemDialog(){
        // custom dialog
        final Dialog dialog = new Dialog(CurrentList.this);
        dialog.setContentView(R.layout.add_item_dialog);
        dialog.setTitle("Add a New Item");

        final EditText itemName = (EditText) dialog.findViewById(R.id.Name);
        final EditText itemType = (EditText) dialog.findViewById(R.id.Type);
        final Button minus = (Button) dialog.findViewById(R.id.Minus);
        final EditText itemQuantity = (EditText) dialog.findViewById(R.id.Quantity);
        final EditText itemUnits = (EditText) dialog.findViewById(R.id.units);
        final Button plus = (Button) dialog.findViewById(R.id.Plus);
        final Button cancel = (Button) dialog.findViewById(R.id.Cancel);
        final Button more = (Button) dialog.findViewById(R.id.More);
        final Button done = (Button) dialog.findViewById(R.id.Done);

        // Open keyboard automatically
        itemName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });

        // Notify user about duplicate item
        itemName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String name = itemName.getText().toString();
                for(int index = 0; index < items.size(); index++){
                    if(name.toLowerCase().equals(items.get(index).getName().toLowerCase())){
                        Context context = getApplicationContext();
                        CharSequence text = "Is this a Duplicate?";
                        int duration = Toast.LENGTH_LONG;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.setGravity(Gravity.TOP, 0, 30);
                        toast.show();
                    }
                }

            }
        });

        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!itemQuantity.getText().toString().isEmpty()){
                    double value = Double.parseDouble(itemQuantity.getText().toString());
                    if (value > 1) {
                        itemQuantity.setText(String.format("%s", (int) Math.ceil(value - 1)));
                    }
                }
            }
        });

        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!itemQuantity.getText().toString().isEmpty()) {
                    double value = Double.parseDouble(itemQuantity.getText().toString());
                    if (value < 99999) {
                        itemQuantity.setText(String.format("%s", (int) Math.floor(value + 1)));
                    }
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!itemName.getText().toString().isEmpty()) {
                    String name = itemName.getText().toString();
                    String type = itemType.getText().toString();
                    double quantity;
                    String units = itemUnits.getText().toString();
                    TextView emptyNotice = (TextView) findViewById(R.id.empty_notice);
                    if(!itemQuantity.getText().toString().isEmpty()) {
                        quantity = Double.parseDouble(itemQuantity.getText().toString());
                    }
                    else{
                        quantity = 1;
                    }
                    Item m = new Item(deviceName, name, type, quantity, units);
                    items.add(m);

                    currentSet.add(m.getJSONString());
                    updateStorage();

                    sortList(false, currentOrder);
                    customAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                    emptyNotice.setVisibility(View.INVISIBLE);
                    addItemDialog();
                }
                else{
                    itemName.setError("Name is empty...");
                }
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!itemName.getText().toString().isEmpty()) {
                    String name = itemName.getText().toString();
                    String type = itemType.getText().toString();
                    double quantity;
                    String units = itemUnits.getText().toString();
                    TextView emptyNotice = (TextView) findViewById(R.id.empty_notice);
                    if(!itemQuantity.getText().toString().isEmpty()) {
                        quantity = Double.parseDouble(itemQuantity.getText().toString());
                    }
                    else{
                        quantity = 1;
                    }
                    Item m = new Item(deviceName, name, type, quantity, units);
                    items.add(m);

                    currentSet.add(m.getJSONString());
                    updateStorage();

                    sortList(false, currentOrder);
                    customAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                    emptyNotice.setVisibility(View.INVISIBLE);
                }
                else{
                    itemName.setError("Name is empty...");
                }
            }
        });

        dialog.show();
    }

    // Checks/UnChecks an item by clicking on any element in its row
    //TODO: change setChecked to toggled
    public void itemClick(View v){
        Item item = items.get(v.getId());
        currentSet.remove(item.getJSONString());
        if (item.getChecked()){
            item.setChecked(false);
        }
        else{
            item.setChecked(true);
        }
        currentSet.add(item.getJSONString());
        updateStorage();

        sortList(false, currentOrder);
        customAdapter.notifyDataSetChanged();
    }

    public void rowClick(int position){
        Item item = items.get(position);
        currentSet.remove(item.getJSONString());
        if (item.getChecked()){
            item.setChecked(false);
        }
        else{
            item.setChecked(true);
        }
        currentSet.add(item.getJSONString());
        updateStorage();

        sortList(false, currentOrder);
        customAdapter.notifyDataSetChanged();
    }

    public void setSwipeAdapter(){
        // TODO: Fix issue with swiping multiple items concurrently
        final SwipeToDismissTouchListener<ListViewAdapter> touchListener =
                new SwipeToDismissTouchListener<>(
                        new ListViewAdapter(listView),
                        new SwipeToDismissTouchListener.DismissCallbacks<ListViewAdapter>() {
                            @Override
                            public boolean canDismiss(int position) {
                                return deviceName.equals(items.get(position).getOwner());
                            }

                            @Override
                            public void onDismiss(ListViewAdapter view, int position) {
                                currentSet.remove(items.get(position).getJSONString());
                                updateStorage();
                                items.remove(position);
                                customAdapter.notifyDataSetChanged();
                                if(items.size() == 0){
                                    TextView emptyNotice = (TextView) findViewById(R.id.empty_notice);
                                    emptyNotice.setVisibility(View.VISIBLE);
                                }
                            }
                        });
        listView.setOnTouchListener(touchListener);
        listView.setOnScrollListener((AbsListView.OnScrollListener) touchListener.makeScrollListener());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (touchListener.existPendingDismisses()) {
                    touchListener.undoPendingDismiss();
                } else {
                    rowClick(position);
                }
            }
        });
    }

    public void setupTestItems(){
        ArrayList<String> jsonList = new ArrayList<>();
        jsonList.add("{\"owner\":owner,\"name\":itemName,\"quantity\":1,\"units\":unit,\"type\":defType, \"timeCreated\":12105543, \"checked\":false}");
        jsonList.add("{\"owner\":" + deviceName + ",\"name\":burgers,\"quantity\":5,\"units\":\"\",\"type\":Meats, \"timeCreated\":12105543, \"checked\":false}");
        jsonList.add("{\"owner\":" + deviceName + ",\"name\":Eggs,\"quantity\":2,\"units\":dozen,\"type\":\"\", \"timeCreated\":12104543, \"checked\":false}");
        jsonList.add("{\"owner\":" + deviceName + ",\"name\":Bacon,\"quantity\":100,\"units\":strips,\"type\":Meats, \"timeCreated\":12105533, \"checked\":false}");
        jsonList.add("{\"owner\":" + deviceName + ",\"name\":Cheese,\"quantity\":4,\"units\":slices,\"type\":Dairy, \"timeCreated\":13105543, \"checked\":false}");
        jsonList.add("{\"owner\":" + deviceName + ",\"name\":Buns,\"quantity\":1,\"units\":\"\",\"type\":\"\", \"timeCreated\":12105843, \"checked\":false}");

        JSONObject obj;
        for(int i = 0; i < jsonList.size(); i++){
            try {
                obj = new JSONObject(jsonList.get(i));
                items.add(new Item(
                        obj.getString("owner"),
                        obj.getString("name"),
                        obj.getString("type"),
                        obj.getInt("quantity"),
                        obj.getString("units"),
                        obj.getInt("timeCreated"),
                        obj.getBoolean("checked")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void setListTitle(Bundle savedInstanceState){
        Bundle extras = getIntent().getExtras();
        if (savedInstanceState == null) {
            listTitle = getIntent().getStringExtra("com.ShoppingList.MESSAGE");
        }
        else {
            listTitle = extras.getString("com.ShoppingList.MESSAGE");
        }
        setTitle(listTitle);
    }

    public void deleteItems(){
        // custom dialog
        final Dialog dialog = new Dialog(CurrentList.this);
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
                for(Item item : items){
                    items_backup.add(item);
                }

                int length = items.size();
                for(int index = length - 1; index > -1; index--){
                    if(deviceName.equals(items.get(index).getOwner())){
                        currentSet.remove(items.get(index).getJSONString());
                        items.remove(index);
                        removals = true;
                    }
                }
                updateStorage();
                customAdapter.notifyDataSetChanged();
                if(items.size() == 0){
                    TextView emptyNotice = (TextView) findViewById(R.id.empty_notice);
                    emptyNotice.setVisibility(View.VISIBLE);
                }
                if(removals){
                    undoBox.setVisibility(View.VISIBLE);
                }
                dialog.dismiss();
                hideUndoBoxTimer();
            }
        });

        delete_checked.setOnClickListener(new View.OnClickListener() {
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
                        currentSet.remove(items.get(index).getJSONString());
                        items.remove(index);
                        removals = true;
                    }
                }
                updateStorage();
                customAdapter.notifyDataSetChanged();
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
        SharedPreferences.Editor editor = settings.edit();
        editor.putStringSet(listTitle, currentSet);
        editor.commit();
        editor.apply();

        editor.remove(listTitle);
        editor.apply();
        editor.putStringSet(listTitle, currentSet);
        editor.apply();
    }

    public void setListeners() {



        // Floating Action Button
        FloatingActionButton addButton = (FloatingActionButton) findViewById(R.id.float_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItemDialog();
            }
        });

        //Long Click for editing
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {
                editItemDialog(pos);
                return true;
            }
        });

        // Undo Listener
        Button undo = (Button) findViewById(R.id.undo);
        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout undoBox = (LinearLayout) findViewById(R.id.undo_box);
                TextView emptyNotice = (TextView) findViewById(R.id.empty_notice);
                items.clear();
                for(Item item : items_backup){
                    items.add(item);
                }
                customAdapter.notifyDataSetChanged();
                undoBox.setVisibility(View.INVISIBLE);
                emptyNotice.setVisibility(View.INVISIBLE);
            }
        });
    }

    public void editItemDialog(final int position){
        final Item item = items.get(position);
        if(!deviceName.equals(item.getOwner())){
            Context context = getApplicationContext();
            CharSequence text = "You are not the owner...";
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }

        // custom dialog
        final Dialog dialog = new Dialog(CurrentList.this);
        dialog.setContentView(R.layout.edit_item_dialog);
        dialog.setTitle("Edit Item");

        final EditText itemName = (EditText) dialog.findViewById(R.id.Name);
        final EditText itemType = (EditText) dialog.findViewById(R.id.Type);
        final Button minus = (Button) dialog.findViewById(R.id.Minus);
        final EditText itemQuantity = (EditText) dialog.findViewById(R.id.Quantity);
        final EditText itemUnits = (EditText) dialog.findViewById(R.id.units);
        final Button plus = (Button) dialog.findViewById(R.id.Plus);
        final Button cancel = (Button) dialog.findViewById(R.id.Cancel);
        final Button done = (Button) dialog.findViewById(R.id.Done);

        itemName.setText(item.getName());
        itemType.setText(item.getType());
        if(item.getQuantity() % 1 == 0){
            itemQuantity.setText(Integer.toString((int) Math.round(item.getQuantity())));
        }
        else {
            itemQuantity.setText(Double.toString(item.getQuantity()));
        }
        itemUnits.setText(item.getUnits());

        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!itemQuantity.getText().toString().isEmpty()) {
                    double value = Double.parseDouble(itemQuantity.getText().toString());
                    if (value > 1) {
                        itemQuantity.setText(String.format("%s", (int) Math.ceil(value - 1)));
                    }
                }
            }
        });

        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!itemQuantity.getText().toString().isEmpty()) {
                    double value = Double.parseDouble(itemQuantity.getText().toString());
                    if (value < 99999) {
                        itemQuantity.setText(String.format("%s", (int) Math.floor(value + 1)));
                    }
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
                if (!itemName.getText().toString().isEmpty()) {
                    String name = itemName.getText().toString();
                    String type = itemType.getText().toString();
                    double quantity;
                    String units = itemUnits.getText().toString();
                    if (!itemQuantity.getText().toString().isEmpty()) {
                        quantity = Double.parseDouble(itemQuantity.getText().toString());
                    } else {
                        quantity = 1;
                    }
                    currentSet.remove(item.getJSONString());
                    item.setName(name);
                    item.setType(type);
                    item.setQuantity(quantity);
                    item.setUnits(units);
                    items.set(position, item);

                    currentSet.add(item.getJSONString());
                    updateStorage();

                    sortList(false, currentOrder);
                    customAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                } else {
                    itemName.setError("Name is empty...");
                }
            }
        });

        dialog.show();
    }
}
