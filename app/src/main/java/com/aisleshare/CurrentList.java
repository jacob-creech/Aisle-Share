package com.aisleshare;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


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
    private String listTitle;
    private TextView emptyNotice;
    private JSONObject aisleShareData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_list);

        listView = (ListView)findViewById(R.id.currentItems);
        items = new ArrayList<>();
        items_backup = new ArrayList<>();
        isIncreasingOrder = true;
        currentOrder = 2;
        emptyNotice = (TextView) findViewById(R.id.empty_notice);
        deviceName = Settings.Secure.getString(CurrentList.this.getContentResolver(), Settings.Secure.ANDROID_ID);
        menuItems = new HashMap<>();

        setListTitle(savedInstanceState);
        readSavedItems();
        setListeners();
        setSwipeAdapter();

        if(items.isEmpty()){
            emptyNotice.setVisibility(View.VISIBLE);
        }

        customAdapter = new CustomAdapter(this, items, R.layout.row_list);
        listView.setAdapter(customAdapter);
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

        sortList(false, currentOrder);
        switch (currentOrder){
            case 0:
                menuItems.get("name").setChecked(true);
                break;
            case 1:
                menuItems.get("quantity").setChecked(true);
                break;
            case 2:
                menuItems.get("time").setChecked(true);
                break;
            case 3:
                menuItems.get("type").setChecked(true);
                break;
            case 4:
                menuItems.get("owner").setChecked(true);
                break;
        }
        customAdapter.notifyDataSetChanged();
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

        saveData();
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
        dialog.setContentView(R.layout.dialog_add_item);
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
                    if(!itemQuantity.getText().toString().isEmpty()) {
                        quantity = Double.parseDouble(itemQuantity.getText().toString());
                    }
                    else{
                        quantity = 1;
                    }

                    Item i = new Item(deviceName, name, type, quantity, units);
                    items.add(i);
                    saveData();
                    sortList(false, currentOrder);
                    customAdapter.notifyDataSetChanged();
                    emptyNotice.setVisibility(View.INVISIBLE);
                    itemName.setText("");
                    itemType.setText("");
                    itemQuantity.setText("1");
                    itemUnits.setText("");
                    itemName.requestFocus();
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
                    if(!itemQuantity.getText().toString().isEmpty()) {
                        quantity = Double.parseDouble(itemQuantity.getText().toString());
                    }
                    else{
                        quantity = 1;
                    }

                    Item i = new Item(deviceName, name, type, quantity, units);
                    items.add(i);
                    saveData();
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
    public void itemClick(View v){
        Item item = items.get(v.getId());
        item.toggleChecked();
        saveData();
        sortList(false, currentOrder);
        customAdapter.notifyDataSetChanged();
    }

    public void rowClick(int position){
        Item item = items.get(position);
        item.toggleChecked();
        saveData();
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
                                items.remove(position);
                                saveData();
                                customAdapter.notifyDataSetChanged();
                                if(items.size() == 0){
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
        dialog.setContentView(R.layout.dialog_delete_items);
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
                        items.remove(index);
                        removals = true;
                    }
                }
                saveData();
                customAdapter.notifyDataSetChanged();
                if(items.size() == 0){
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
                        items.remove(index);
                        removals = true;
                    }
                }
                saveData();
                customAdapter.notifyDataSetChanged();
                if (items.size() == 0) {
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
        new CountDownTimer(5000, 5000) {
            public void onTick(long millisUntilFinished) {}
            public void onFinish() {
                LinearLayout undoBox = (LinearLayout) findViewById(R.id.undo_box);
                undoBox.setVisibility(View.INVISIBLE);
                items_backup.clear();
            }
        }.start();
    }

    public void readSavedItems(){
        try {
            File file = new File(getFilesDir().getPath() + "/Aisle_Share_Data.json");
            // Read or Initializes aisleShareData
            // Assumes the File itself has already been Initialized
            aisleShareData = new JSONObject(loadJSONFromAsset(file));
            currentOrder = aisleShareData.optJSONObject("Lists").optJSONObject(listTitle).getInt("sort");
            isIncreasingOrder = aisleShareData.optJSONObject("Lists").optJSONObject(listTitle).getBoolean("direction");
            JSONArray read_items = aisleShareData.optJSONObject("Lists").optJSONObject(listTitle).getJSONArray("items");
            for(int index = 0; index < read_items.length(); index++){
                try {
                    JSONObject obj = new JSONObject(read_items.get(index).toString());
                    items.add(new Item(
                            obj.getString("owner"),
                            obj.getString("name"),
                            obj.getString("type"),
                            obj.getInt("quantity"),
                            obj.getString("units"),
                            obj.getBoolean("checked"),
                            obj.getLong("timeCreated")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String loadJSONFromAsset(File f) {
        String json;
        try {
            FileInputStream fis = new FileInputStream(f);
            int bytes = fis.available();
            byte[] buffer = new byte[bytes];
            fis.read(buffer, 0, bytes);
            fis.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public void saveData(){
        try {
            aisleShareData.optJSONObject("Lists").optJSONObject(listTitle).remove("items");
            aisleShareData.optJSONObject("Lists").optJSONObject(listTitle).accumulate("items", new JSONArray());
            for(Item i : items){
                aisleShareData.optJSONObject("Lists").optJSONObject(listTitle).optJSONArray("items").put(i.getJSONString());
            }
            aisleShareData.optJSONObject("Lists").optJSONObject(listTitle).put("sort", currentOrder);
            aisleShareData.optJSONObject("Lists").optJSONObject(listTitle).put("direction", isIncreasingOrder);

            FileOutputStream fos = new FileOutputStream(getFilesDir().getPath() + "/Aisle_Share_Data.json");
            fos.write(aisleShareData.toString().getBytes());
            fos.close();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
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
                items.clear();
                for (Item item : items_backup) {
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
        dialog.setContentView(R.layout.dialog_edit_item);
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

                    item.setName(name);
                    item.setType(type);
                    item.setQuantity(quantity);
                    item.setUnits(units);
                    items.set(position, item);

                    saveData();
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
