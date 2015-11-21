package com.aisleshare;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import com.getbase.floatingactionbutton.FloatingActionButton;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.timroes.swipetodismiss.SwipeDismissList;

public class CurrentList extends AppCompatActivity {

    // Class Variables
    private static final String LIST_NAME = "com.ShoppingList.MESSAGE";
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;
    private ListView listView;
    private ArrayList<Item> items;
    private ArrayList<Item> items_backup;
    private ItemAdapter itemAdapter;
    private boolean isIncreasingOrder;
    private int currentOrder;
    private Map<String, MenuItem> menuItems;
    private ArrayList<String> categories;
    private ArrayList<String> names;
    private ArrayList<String> units;
    private String deviceName;
    private String listTitle;
    private TextView emptyNotice;
    private PopupWindow undoPopup;
    private CountDownTimer undoTimer;
    private SwipeDismissList swipeAdapter;
    private JSONObject aisleShareData;
    private boolean transfering;
    private Bluetooth mBlueService = null;
    public BluetoothAdapter mBluetoothAdapter = null;
    private String mConnectedDeviceName = null;

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
        categories = new ArrayList<>();
        names = new ArrayList<>();
        units = new ArrayList<>();
        transfering = false;
        mBluetoothAdapter = Constants.mBluetoothAdapter;

        setListTitle(savedInstanceState);
        readSavedItems();
        setListeners();
        setSwipeToDelete();

        if(items.isEmpty()){
            emptyNotice.setVisibility(View.VISIBLE);
        }

        itemAdapter = new ItemAdapter(this, items, R.layout.row_list);
        listView.setAdapter(itemAdapter);

        try {
            aisleShareData.put("ListOpened", listTitle);
            saveData();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            //Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else if (mBlueService == null) {
            setupBluetooth();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBlueService != null) {
            mBlueService.stop();
        }
    }

    @Override
    public void onBackPressed() {
        try {
            File file = new File(getFilesDir().getPath() + "/Aisle_Share_Data.json");
            aisleShareData = new JSONObject(loadJSONFromAsset(file));
            aisleShareData.put("ListOpened", "");
            saveData();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(undoTimer != null) {
            undoTimer.cancel();
        }
        swipeAdapter.finish();
        super.onBackPressed();
    }

    @Override
    public void onResume(){
        super.onResume();
        if(transfering) {
            readSavedItems();
            sortList(false, currentOrder);
            itemAdapter.notifyDataSetChanged();
            transfering = false;
        }

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mBlueService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mBlueService.getState() == Bluetooth.STATE_NONE) {
                // Start the Bluetooth chat services
                mBlueService.start();
            }
        }
    }


    private void setupBluetooth() {
        mBlueService = new Bluetooth(CurrentList.this, mHandler);
    }

    public void setSwipeToDelete() {
        SwipeDismissList.OnDismissCallback callback = new SwipeDismissList.OnDismissCallback() {
            @Override
            public SwipeDismissList.Undoable onDismiss(AbsListView listView, final int position) {
                final Item i = items.get(position);
                if(deviceName.equals(i.getOwner())) {
                    items.remove(position);
                    itemAdapter.notifyDataSetChanged();
                    if(items.size() == 0) {
                        emptyNotice.setVisibility(View.VISIBLE);
                    }
                    saveData();
                    return new SwipeDismissList.Undoable() {
                        public void undo() {
                            items.add(position, i);
                            itemAdapter.notifyDataSetChanged();
                            emptyNotice.setVisibility(View.INVISIBLE);
                            saveData();
                        }
                    };
                }
                else{
                    Toast toast = Toast.makeText(CurrentList.this, "Item not owned...", Toast.LENGTH_LONG);
                    toast.show();
                }
                return null;
            }
        };
        SwipeDismissList.UndoMode mode = SwipeDismissList.UndoMode.MULTI_UNDO;
        swipeAdapter = new SwipeDismissList(listView, callback, mode);
    }

    public void addHeaders() {
        Item header;
        switch(currentOrder) {
            case 0: {
                //add new headers
                if(items.size() < 1) break;
                String title = items.get(0).getName();
                if(title.length() < 1) break;
                header = new Item(deviceName, title.substring(0, 1), false);
                items.add(0, header);
                for(int i = 1; i < items.size()-1; i++) {
                    if(items.get(i).getName().substring(0, 1).compareTo(items.get(i+1).getName().substring(0, 1)) != 0) {
                        title = items.get(i+1).getName();
                        header = new Item(deviceName, title.substring(0, 1), false);
                        items.add(i+1, header);
                        i++;
                    }
                }
                break;
            }
            case 1: {
                //add new headers
                if(items.size() < 1) break;
                double num = items.get(0).getQuantity();
                header = new Item(deviceName, String.valueOf(num), false);
                items.add(0, header);
                for(int i = 1; i < items.size()-1; i++) {
                    if(items.get(i).getQuantity() != items.get(i+1).getQuantity()) {
                        num = items.get(i+1).getQuantity();
                        header = new Item(deviceName, String.valueOf(num), false);
                        items.add(i+1, header);
                        i++;
                    }
                }
                break;
            }
            case 3: {
                //add new headers
                if(items.size() < 1) break;
                String title = items.get(0).getType();
                //if(title.length() < 1) break;
                header = new Item(deviceName, title, false);
                items.add(0, header);
                for(int i = 1; i < items.size()-1; i++) {
                    if(items.get(i).getType().compareTo(items.get(i + 1).getType()) != 0) {
                        title = items.get(i+1).getType();
                        header = new Item(deviceName, title, false);
                        items.add(i+1, header);
                        i++;
                    }
                }
                break;
            }
        }
    }

    // Sorted based on the order index parameter
    public void sortList(boolean reverseOrder, int order) {
        //remove all previous headers
        for(int i = 0; i < items.size(); i++) {
            if(!items.get(i).getIsItem()) {
                items.remove(i);
                i--;
            }
        }
        if(reverseOrder) {
            isIncreasingOrder = !isIncreasingOrder;
        }
        if(order != currentOrder){
            currentOrder = order;
            isIncreasingOrder = true;
        }

        ItemComparator compare = new ItemComparator(CurrentList.this);

        // Unsorted
        if(menuItems.get("sort") != null && menuItems.get("unsorted") != null) {
            if (currentOrder == -1) {
                menuItems.get("sort").setIcon(0);
                menuItems.get("unsorted").setVisible(false);
                return;
            } else {
                menuItems.get("unsorted").setVisible(true);
            }
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
            if(menuItems.get("sort") != null) {
                menuItems.get("sort").setIcon(R.mipmap.inc_sort);
            }
        }
        else{
            Collections.reverse(items);
            if(menuItems.get("sort") != null) {
                menuItems.get("sort").setIcon(R.mipmap.dec_sort);
            }
        }
        if(currentOrder >= 0) {
            addHeaders();
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
        itemAdapter.notifyDataSetChanged();
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
                confirmDeletion();
                break;
            case R.id.addRecipe:
                addToListDialog("Recipes");
                break;
            case R.id.addActivity:
                addToListDialog("Activities");
                break;
            case R.id.sort:
                return true;
            case android.R.id.home:
                try {
                    File file = new File(getFilesDir().getPath() + "/Aisle_Share_Data.json");
                    aisleShareData = new JSONObject(loadJSONFromAsset(file));
                    aisleShareData.put("ListOpened", "");
                    saveData();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(undoTimer != null) {
                    undoTimer.cancel();
                }
                swipeAdapter.finish();
                finish();
                return true;
            case R.id.share:
                setupBluetooth();
                setBluetooth();
                break;
        }

        saveData();
        itemAdapter.notifyDataSetChanged();
        return true;
    }

    // Popup for adding an Item
    public void addToListDialog(final String type) {
        // custom dialog
        final Dialog dialog = new Dialog(CurrentList.this);
        dialog.setContentView(R.layout.dialog_select_list);
        if(type.equals("Recipes")) {
            dialog.setTitle("Select a Recipe");
        }
        else if(type.equals("Activities")) {
            dialog.setTitle("Select an Activity");
        }
        else{
            return;
        }

        final ListView lv = (ListView) dialog.findViewById(R.id.lists);
        final Button cancel = (Button) dialog.findViewById(R.id.cancel);

        final ArrayList<String> entries = new ArrayList<>();
        JSONArray names = aisleShareData.optJSONObject(type).names();
        if(names != null) {
            for (int i = 0; i < names.length(); i++) {
                try {
                    entries.add(names.get(i).toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        else{
            return;
        }

        ArrayAdapter<String> itemAdapter = new ArrayAdapter<>(CurrentList.this,android.R.layout.simple_list_item_1, entries);
        lv.setAdapter(itemAdapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = entries.get(position);
                JSONArray sel_items = new JSONArray();
                try {
                    int sel_currentOrder = aisleShareData.optJSONObject(type).optJSONObject(name).optInt("sort");
                    boolean sel_isIncreasingOrder = aisleShareData.optJSONObject(type).optJSONObject(name).optBoolean("order");
                    sel_items = aisleShareData.optJSONObject(type).optJSONObject(name).optJSONArray("items");

                    aisleShareData.optJSONObject("Transfers").put("sort", sel_currentOrder);
                    aisleShareData.optJSONObject("Transfers").put("order", sel_isIncreasingOrder);
                    aisleShareData.optJSONObject("Transfers").put("name", listTitle);
                    aisleShareData.optJSONObject("Transfers").remove("items");
                    aisleShareData.optJSONObject("Transfers").accumulate("items", new JSONArray());
                    for (int index = 0; index < sel_items.length(); index++) {
                        aisleShareData.optJSONObject("Transfers").optJSONArray("items").put(sel_items.get(index));
                    }
                    FileOutputStream fos = new FileOutputStream(getFilesDir().getPath() + "/Aisle_Share_Data.json");
                    fos.write(aisleShareData.toString().getBytes());
                    fos.close();
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();

                if (sel_items.length() > 0) {
                    transfering = true;
                    Intent intent = new Intent(CurrentList.this, Transfer.class);
                    intent.putExtra(LIST_NAME, "Select which Items to Add");
                    startActivity(intent);
                } else {
                    Toast toast = Toast.makeText(CurrentList.this, name + " is empty...", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    //TODO: find a way
    private void setBluetooth() {
        Intent serverIntent = new Intent(CurrentList.this, DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
    }



    public void clearMenuCheckables(){
        menuItems.get("name").setChecked(false);
        menuItems.get("type").setChecked(false);
        menuItems.get("time").setChecked(false);
        menuItems.get("quantity").setChecked(false);
        menuItems.get("owner").setChecked(false);
    }

    public String autoComplete_DupCheck(ArrayList<String> inputArray, String inputString) {
        //prevents and kills the dups
        int index;
        for(index = 0 ; index < inputArray.size(); index++) {
            if(inputArray.get(index).compareTo(inputString) == 0) {
                break;
            }
        }
        if(index == inputArray.size()) {
            return inputString;
        }
        return "";
    }

    // Popup for adding an Item
    public void addItemDialog(){
        // custom dialog
        final Dialog dialog = new Dialog(CurrentList.this);
        dialog.setContentView(R.layout.dialog_add_item);
        dialog.setTitle("Add a New Item");

        final AutoCompleteTextView itemName = (AutoCompleteTextView) dialog.findViewById(R.id.Name);
        final AutoCompleteTextView itemType = (AutoCompleteTextView) dialog.findViewById(R.id.Type);
        final Button minus = (Button) dialog.findViewById(R.id.Minus);
        final EditText itemQuantity = (EditText) dialog.findViewById(R.id.Quantity);
        final AutoCompleteTextView itemUnits = (AutoCompleteTextView) dialog.findViewById(R.id.units);
        final Button plus = (Button) dialog.findViewById(R.id.Plus);
        final Button cancel = (Button) dialog.findViewById(R.id.Cancel);
        final Button more = (Button) dialog.findViewById(R.id.More);
        final Button done = (Button) dialog.findViewById(R.id.Done);

        //minus.setVisibility(View.INVISIBLE);

        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, categories);
        ArrayAdapter<String> nameAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, names);
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, units);



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
                        toast.setGravity(Gravity.TOP,0,0);
                        toast.show();
                    }
                }

            }
        });

        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!itemQuantity.getText().toString().isEmpty()) {
                    double value = Double.parseDouble(itemQuantity.getText().toString());
                    if (value > 1) {
                        if (value % 1 == 0) {
                            itemQuantity.setText(String.format("%s", (int) (value - 1)));
                        } else {
                            itemQuantity.setText(String.format("%s", (int) Math.ceil(value - 1)));
                        }
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
                        if (value % 1 == 0) {
                            itemQuantity.setText(String.format("%s", (int) (value + 1)));
                        } else {
                            itemQuantity.setText(String.format("%s", (int) Math.floor(value + 1)));
                        }
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
                    String unit = itemUnits.getText().toString();
                    String duplicator; //used to check for duplicate auto complete words
                    if(!itemQuantity.getText().toString().isEmpty()) {
                        quantity = Double.parseDouble(itemQuantity.getText().toString());
                    }
                    else{
                        quantity = 1;
                    }

                    Item i = new Item(deviceName, name, type, quantity, unit);

                    duplicator = autoComplete_DupCheck(categories, type);
                    if(duplicator.compareTo(type) == 0) {
                        categories.add(type);
                    }

                    duplicator = autoComplete_DupCheck(names, name);
                    if(duplicator.compareTo(name) == 0) names.add(name);

                    duplicator = autoComplete_DupCheck(units, unit);
                    if(duplicator.compareTo(unit) == 0) units.add(unit);


                    items.add(i);
                    saveData();
                    sortList(false, currentOrder);
                    itemAdapter.notifyDataSetChanged();
                    emptyNotice.setVisibility(View.INVISIBLE);
                    itemName.setText("");
                    itemType.setText("");
                    itemQuantity.setText("1");
                    itemUnits.setText("");
                    itemName.requestFocus();

                    Toast toast = Toast.makeText(CurrentList.this, "Item Added", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP,0,0);
                    toast.show();
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
                    String unit = itemUnits.getText().toString();
                    String duplicator; //used to check for duplicate auto complete words
                    if(!itemQuantity.getText().toString().isEmpty()) {
                        quantity = Double.parseDouble(itemQuantity.getText().toString());
                    }
                    else{
                        quantity = 1;
                    }

                    Item i = new Item(deviceName, name, type, quantity, unit);

                    duplicator = autoComplete_DupCheck(categories, type);
                    if(duplicator.compareTo(type) == 0) categories.add(type);

                    duplicator = autoComplete_DupCheck(names, name);
                    if(duplicator.compareTo(name) == 0) names.add(name);

                    duplicator = autoComplete_DupCheck(units, unit);
                    if(duplicator.compareTo(unit) == 0) units.add(unit);


                    items.add(i);
                    saveData();
                    sortList(false, currentOrder);
                    itemAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                    emptyNotice.setVisibility(View.INVISIBLE);
                }
                else{
                    itemName.setError("Name is empty...");
                }
            }
        });
        itemType.setAdapter(catAdapter);
        itemUnits.setAdapter(unitAdapter);
        itemName.setAdapter(nameAdapter);
        dialog.show();
    }

    // Checks/UnChecks an item by clicking on any element in its row
    public void itemClick(View v){
        Item item = items.get(v.getId());
        item.toggleChecked();
        saveData();
        sortList(false, currentOrder);
        itemAdapter.notifyDataSetChanged();
        sendMessage(item.getJSONString());
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

    public void confirmDeletion()
    {
        boolean itemsChecked = false;
        for(Item i : items){
            if(i.getChecked()){
                itemsChecked = true;
                break;
            }
        }
        if(itemsChecked) {
            new AlertDialog.Builder(CurrentList.this)
                .setTitle("Delete Checked Items?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        boolean removals = false;

                        items_backup.clear();
                        for (Item item : items) {
                            items_backup.add(item);
                        }

                        int length = items.size();
                        for (int index = length - 1; index > -1; index--) {
                            if (deviceName.equals(items.get(index).getOwner()) && items.get(index).getChecked()) {
                                items.remove(index);
                                removals = true;
                            }
                        }
                        saveData();
                        itemAdapter.notifyDataSetChanged();
                        if (items.size() == 0) {
                            emptyNotice.setVisibility(View.VISIBLE);
                        }
                        if (removals) {
                            undoBox();
                        }
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
        }
        else{
            Toast toast = Toast.makeText(CurrentList.this, "No Checked Items...", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    public void undoBox(){
        // -- Load undo popup --
        LayoutInflater inflater = (LayoutInflater) listView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(de.timroes.swipetodismiss.R.layout.undo_popup, null);
        Button undoButton = (Button)view.findViewById(de.timroes.swipetodismiss.R.id.undo);
        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                items.clear();
                for (Item item : items_backup) {
                    items.add(item);
                }
                saveData();
                itemAdapter.notifyDataSetChanged();
                emptyNotice.setVisibility(View.INVISIBLE);
                undoPopup.dismiss();
            }
        });
        undoButton.setText("UNDO");
        TextView undoText = (TextView)view.findViewById(de.timroes.swipetodismiss.R.id.text);
        undoText.setText("Items deleted");
        float density = listView.getResources().getDisplayMetrics().density;

        if(undoPopup != null && undoPopup.isShowing()) {
            undoPopup.dismiss();
        }
        undoPopup = new PopupWindow(view);
        undoPopup.setAnimationStyle(de.timroes.swipetodismiss.R.style.fade_animation);
        // Get scren width in dp and set width respectively
        int xdensity = (int)(listView.getContext().getResources().getDisplayMetrics().widthPixels / density);
        if(xdensity < 300) {
            undoPopup.setWidth((int)(density * 220));
        } else if(xdensity < 350) {
            undoPopup.setWidth((int)(density * 240));
        } else if(xdensity < 500) {
            undoPopup.setWidth((int)(density * 270));
        } else {
            undoPopup.setWidth((int)(density * 390));
        }
        undoPopup.setHeight((int) (density * 56));

        undoPopup.showAtLocation(listView, Gravity.LEFT | Gravity.BOTTOM, 30, (int) (density * 15));
        hideUndoBoxTimer();
    }

    public void hideUndoBoxTimer(){
        undoTimer = new CountDownTimer(5000, 5000) {
            public void onTick(long millisUntilFinished) {}
            public void onFinish() {
                if(undoPopup.isShowing()) {
                    undoPopup.dismiss();
                }
                items_backup.clear();
                undoTimer = null;
            }
        }.start();
    }

    public void readSavedArray(JSONArray input, int id) {
        try {
            if (input != null) {
                int len = input.length();
                for (int i = 0; i < len; i++) {
                    if(id == 0) categories.add(input.get(i).toString());
                    if(id == 1) names.add(input.get(i).toString());
                    if(id == 2) units.add(input.get(i).toString());
                }
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void readSavedItems(){
        try {
            File file = new File(getFilesDir().getPath() + "/Aisle_Share_Data.json");
            // Read or Initializes aisleShareData
            // Assumes the File itself has already been Initialized
            aisleShareData = new JSONObject(loadJSONFromAsset(file));
            currentOrder = aisleShareData.optJSONObject("Lists").optJSONObject(listTitle).getInt("sort");
            isIncreasingOrder = aisleShareData.optJSONObject("Lists").optJSONObject(listTitle).getBoolean("direction");
            JSONArray read_cat = aisleShareData.getJSONArray("category");
            readSavedArray(read_cat, 0);
            JSONArray read_name = aisleShareData.getJSONArray("name");
            readSavedArray(read_name, 1);
            JSONArray read_unit = aisleShareData.getJSONArray("unit");
            readSavedArray(read_unit, 2);
            JSONArray read_items = aisleShareData.optJSONObject("Lists").optJSONObject(listTitle).getJSONArray("items");
            items.clear();
            for(int index = 0; index < read_items.length(); index++){
                try {
                    JSONObject obj = new JSONObject(read_items.get(index).toString());
                    Item i = new Item(
                            obj.getString("owner"),
                            obj.getString("name"),
                            obj.getString("type"),
                            obj.getDouble("quantity"),
                            obj.getString("units"),
                            obj.getBoolean("checked"),
                            obj.getLong("timeCreated"));
                    i.setIsItem(obj.getBoolean("isItem"));
                    items.add(i);
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
            for(Item i : items) {
                aisleShareData.optJSONObject("Lists").optJSONObject(listTitle).optJSONArray("items").put(i.getJSONString());
            }
            aisleShareData.optJSONObject("Lists").optJSONObject(listTitle).put("sort", currentOrder);
            aisleShareData.optJSONObject("Lists").optJSONObject(listTitle).put("direction", isIncreasingOrder);
            aisleShareData.put("category", new JSONArray(categories));
            aisleShareData.put("name", new JSONArray(names));
            aisleShareData.put("unit", new JSONArray(units));

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

        // checking/unchecking
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                itemClick(view);
            }
        });

        // Long Click opens contextual menu
        registerForContextMenu(listView);

        Button bMessageButton = (Button) findViewById(R.id.button);
        bMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("1");
            }
        });
    }

    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mBlueService.getState() != Bluetooth.STATE_CONNECTED) {
            Toast.makeText(CurrentList.this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mBlueService.write(send);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu_curr, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem menuItem) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuItem.getMenuInfo();
        int index = info.position;
        switch (menuItem.getItemId()) {
            case R.id.edit:
                editItemDialog(index);
                return true;
            case R.id.delete:
                items_backup.clear();
                for (Item item : items) {
                    items_backup.add(item);
                }

                items.remove(index);
                itemAdapter.notifyDataSetChanged();
                if(items.size() == 0) {
                    emptyNotice.setVisibility(View.VISIBLE);
                }
                saveData();
                undoBox();
                return true;
            case R.id.cancel:
                return super.onContextItemSelected(menuItem);
            default:
                return super.onContextItemSelected(menuItem);
        }
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

        final AutoCompleteTextView itemName = (AutoCompleteTextView) dialog.findViewById(R.id.Name);
        final AutoCompleteTextView itemType = (AutoCompleteTextView) dialog.findViewById(R.id.Type);
        final Button minus = (Button) dialog.findViewById(R.id.Minus);
        final EditText itemQuantity = (EditText) dialog.findViewById(R.id.Quantity);
        final AutoCompleteTextView itemUnits = (AutoCompleteTextView) dialog.findViewById(R.id.units);
        final Button plus = (Button) dialog.findViewById(R.id.Plus);
        final Button cancel = (Button) dialog.findViewById(R.id.Cancel);
        final Button done = (Button) dialog.findViewById(R.id.Done);

        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, categories);
        ArrayAdapter<String> nameAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, names);
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, units);

        itemName.setText(item.getName());
        itemType.setText(item.getType());
        if(item.getQuantity() % 1 == 0){
            itemQuantity.setText(Integer.toString((int) (item.getQuantity())));
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
                        if (value % 1 == 0) {
                            itemQuantity.setText(String.format("%s", (int) (value - 1)));
                        } else {
                            itemQuantity.setText(String.format("%s", (int) Math.ceil(value - 1)));
                        }
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
                        if(value % 1 == 0){
                            itemQuantity.setText(String.format("%s", (int) (value + 1)));
                        }
                        else {
                            itemQuantity.setText(String.format("%s", (int) Math.floor(value + 1)));
                        }
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
                    String unit = itemUnits.getText().toString();
                    String duplicator; //used to check for duplicate auto complete words
                    if (!itemQuantity.getText().toString().isEmpty()) {
                        quantity = Double.parseDouble(itemQuantity.getText().toString());
                    } else {
                        quantity = 1;
                    }

                    duplicator = autoComplete_DupCheck(categories, type);
                    if(duplicator.compareTo(type) == 0) categories.add(type);

                    duplicator = autoComplete_DupCheck(names, name);
                    if(duplicator.compareTo(name) == 0) names.add(name);

                    duplicator = autoComplete_DupCheck(units, unit);
                    if(duplicator.compareTo(unit) == 0) units.add(unit);

                    item.setName(name);
                    item.setType(type);
                    item.setQuantity(quantity);
                    item.setUnits(unit);
                    items.set(position, item);

                    saveData();
                    sortList(false, currentOrder);
                    itemAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                } else {
                    itemName.setError("Name is empty...");
                }
            }
        });
        itemType.setAdapter(catAdapter);
        itemName.setAdapter(nameAdapter);
        itemUnits.setAdapter(unitAdapter);
        dialog.show();
    }

    private void setStatus(int resId) {
        FragmentActivity activity = CurrentList.this;
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(resId);
    }

    private void setStatus(CharSequence subTitle) {
        FragmentActivity activity = CurrentList.this;
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(subTitle);
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            FragmentActivity activity = CurrentList.this;
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case Bluetooth.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            Toast.makeText(CurrentList.this, "CONNECTED LOL", Toast.LENGTH_SHORT).show();
                            break;
                        case Bluetooth.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case Bluetooth.STATE_LISTEN:
                        case Bluetooth.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    Toast.makeText(CurrentList.this, "SENT MESSAGE", Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    if(readMessage.equals("1")) {
                        Toast.makeText(CurrentList.this, "YES!!", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        try {
                            JSONObject obj = new JSONObject(readMessage);
                            items.add(new Item(
                                    obj.getString("owner"),
                                    obj.getString("name"),
                                    obj.getString("type"),
                                    obj.getDouble("quantity"),
                                    obj.getString("units"),
                                    obj.getBoolean("checked"),
                                    obj.getLong("timeCreated")));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        itemAdapter.notifyDataSetChanged();
                    }
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    //setupBluetooth();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    //Toast.makeText(CurrentList.this, R.string.bt_not_enabled_leaving,
                    //        Toast.LENGTH_SHORT).show();
                }
        }
    }

    /**
     * Establish connection with other device
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mBlueService.connect(device, secure);
    }
}
