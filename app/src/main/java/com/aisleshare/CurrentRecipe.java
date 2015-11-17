package com.aisleshare;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.timroes.swipetodismiss.SwipeDismissList;


public class CurrentRecipe extends AppCompatActivity {

    // Class Variables
    public final static String LIST_NAME = "com.ShoppingList.MESSAGE";
    private ListView listView;
    private ArrayList<Item> items;
    private ArrayList<Item> items_backup;
    private CustomAdapter customAdapter;
    private ArrayList<String> categories;
    private boolean isIncreasingOrder;
    private int currentOrder;
    private Map<String, MenuItem> menuItems;
    private String deviceName;
    private String recipeTitle;
    private TextView emptyNotice;
    private PopupWindow undoPopup;
    private CountDownTimer undoTimer;
    private SwipeDismissList swipeAdapter;
    private JSONObject aisleShareData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_recipe);

        listView = (ListView)findViewById(R.id.currentItems);
        items = new ArrayList<>();
        items_backup = new ArrayList<>();
        isIncreasingOrder = true;
        currentOrder = 2;
        emptyNotice = (TextView) findViewById(R.id.empty_notice);
        deviceName = Settings.Secure.getString(CurrentRecipe.this.getContentResolver(), Settings.Secure.ANDROID_ID);
        menuItems = new HashMap<>();
        categories = new ArrayList<>();

        setRecipeTitle(savedInstanceState);
        initializeStorage();
        setListeners();
        setSwipeToDelete();

        if(items.isEmpty()){
            emptyNotice.setVisibility(View.VISIBLE);
        }

        customAdapter = new CustomAdapter(this, items, R.layout.row_recipe);
        listView.setAdapter(customAdapter);

        try {
            aisleShareData.put("RecipeOpened", recipeTitle);
            saveData();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setSwipeToDelete() {
        SwipeDismissList.OnDismissCallback callback = new SwipeDismissList.OnDismissCallback() {
            @Override
            public SwipeDismissList.Undoable onDismiss(AbsListView listView, final int position) {
                final Item i = items.get(position);
                if(deviceName.equals(i.getOwner())) {
                    items.remove(position);
                    customAdapter.notifyDataSetChanged();
                    if(items.size() == 0) {
                        emptyNotice.setVisibility(View.VISIBLE);
                    }
                    saveData();
                    return new SwipeDismissList.Undoable() {
                        public void undo() {
                            items.add(position, i);
                            customAdapter.notifyDataSetChanged();
                            emptyNotice.setVisibility(View.INVISIBLE);
                            saveData();
                        }
                    };
                }
                return null;
            }
        };
        SwipeDismissList.UndoMode mode = SwipeDismissList.UndoMode.MULTI_UNDO;
        swipeAdapter = new SwipeDismissList(listView, callback, mode);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_current_recipe, menu);
        menuItems.put("share", menu.findItem(R.id.share));
        menuItems.put("add_to_list", menu.findItem(R.id.add_to_list));
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
                deleteItemsDialog();
                break;
            case R.id.sort:
                return true;
            case R.id.add_to_list:
                addToListDialog();
                break;
            case android.R.id.home:
                try {
                    File file = new File(getFilesDir().getPath() + "/Aisle_Share_Data.json");
                    aisleShareData = new JSONObject(loadJSONFromAsset(file));
                    aisleShareData.put("RecipeOpened", "");
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
        }

        saveData();
        customAdapter.notifyDataSetChanged();
        return true;
    }

    @Override
    public void onBackPressed() {
        try {
            File file = new File(getFilesDir().getPath() + "/Aisle_Share_Data.json");
            aisleShareData = new JSONObject(loadJSONFromAsset(file));
            aisleShareData.put("RecipeOpened", "");
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

    public void clearMenuCheckables(){
        menuItems.get("name").setChecked(false);
        menuItems.get("type").setChecked(false);
        menuItems.get("time").setChecked(false);
        menuItems.get("quantity").setChecked(false);
        menuItems.get("owner").setChecked(false);
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
    }

    public void setRecipeTitle(Bundle savedInstanceState){
        Bundle extras = getIntent().getExtras();
        if (savedInstanceState == null) {
            recipeTitle = getIntent().getStringExtra("com.ShoppingList.MESSAGE");
        }
        else {
            recipeTitle = extras.getString("com.ShoppingList.MESSAGE");
        }
        setTitle(recipeTitle);
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

        ItemComparator compare = new ItemComparator(CurrentRecipe.this);

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

    // Popup for adding an Item
    public void addItemDialog(){
        // custom dialog
        final Dialog dialog = new Dialog(CurrentRecipe.this);
        dialog.setContentView(R.layout.dialog_add_item);
        dialog.setTitle("Add a New Item");

        final EditText itemName = (EditText) dialog.findViewById(R.id.Name);
        final AutoCompleteTextView itemType = (AutoCompleteTextView) dialog.findViewById(R.id.Type);
        final Button minus = (Button) dialog.findViewById(R.id.Minus);
        final EditText itemQuantity = (EditText) dialog.findViewById(R.id.Quantity);
        final EditText itemUnits = (EditText) dialog.findViewById(R.id.units);
        final Button plus = (Button) dialog.findViewById(R.id.Plus);
        final Button cancel = (Button) dialog.findViewById(R.id.Cancel);
        final Button more = (Button) dialog.findViewById(R.id.More);
        final Button done = (Button) dialog.findViewById(R.id.Done);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, categories);

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
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String name = itemName.getText().toString();
                for (int index = 0; index < items.size(); index++) {
                    if (name.toLowerCase().equals(items.get(index).getName().toLowerCase())) {
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
                if(!itemQuantity.getText().toString().isEmpty()) {
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

        more.setOnClickListener(new View.OnClickListener() {
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

                    //check categories to prevent duplications
                    int cat_index;
                    for(cat_index = 0 ; cat_index < categories.size(); cat_index++) {
                        if(categories.get(cat_index).compareTo(type) == 0) {
                            break;
                        }
                    }
                    if(cat_index == categories.size()) {
                        categories.add(type);
                    }

                    Item m = new Item(deviceName, name, type, quantity, units);
                    items.add(m);

                    saveData();

                    sortList(false, currentOrder);
                    customAdapter.notifyDataSetChanged();
                    emptyNotice.setVisibility(View.INVISIBLE);
                    itemName.setText("");
                    itemType.setText("");
                    itemQuantity.setText("1");
                    itemUnits.setText("");
                    itemName.requestFocus();

                    Toast toast = Toast.makeText(CurrentRecipe.this, "Item Added", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP, 0, 0);
                    toast.show();
                } else {
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

                    //check categories to prevent duplications
                    int cat_index;
                    for(cat_index = 0 ; cat_index < categories.size(); cat_index++) {
                        if(categories.get(cat_index).compareTo(type) == 0) {
                            break;
                        }
                    }
                    if(cat_index == categories.size()) {
                        categories.add(type);
                    }

                    Item m = new Item(deviceName, name, type, quantity, units);
                    items.add(m);

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
        itemType.setAdapter(adapter);
        dialog.show();
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
        final Dialog dialog = new Dialog(CurrentRecipe.this);
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

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, categories);

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
                        if(value % 1 == 0){
                            itemQuantity.setText(String.format("%s", (int) (value - 1)));
                        }
                        else {
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

    // Popup for adding an Item
    public void addToListDialog() {
        if (items.size() == 0) {
            Toast toast = Toast.makeText(CurrentRecipe.this, "No items to add...", Toast.LENGTH_LONG);
            toast.show();
            return;
        }

        // custom dialog
        final Dialog dialog = new Dialog(CurrentRecipe.this);
        dialog.setContentView(R.layout.dialog_select_list);
        dialog.setTitle("Select a List");

        final ListView lv = (ListView) dialog.findViewById(R.id.lists);
        final Button cancel = (Button) dialog.findViewById(R.id.cancel);

        final ArrayList<String> lists = new ArrayList<>();
        JSONArray names = aisleShareData.optJSONObject("Lists").names();
        if(names != null) {
            for (int i = 0; i < names.length(); i++) {
                try {
                    lists.add(names.get(i).toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        else{
            return;
        }

        ArrayAdapter<String> itemAdapter = new ArrayAdapter<>(CurrentRecipe.this,android.R.layout.simple_list_item_1, lists);
        lv.setAdapter(itemAdapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String listTitle = lists.get(position);
                try {
                    aisleShareData.optJSONObject("Transfers").put("sort", currentOrder);
                    aisleShareData.optJSONObject("Transfers").put("order", isIncreasingOrder);
                    aisleShareData.optJSONObject("Transfers").put("name", listTitle);
                    aisleShareData.optJSONObject("Transfers").remove("items");
                    aisleShareData.optJSONObject("Transfers").accumulate("items", new JSONArray());
                    for (Item i : items) {
                        aisleShareData.optJSONObject("Transfers").optJSONArray("items").put(i.getJSONString());
                    }
                    FileOutputStream fos = new FileOutputStream(getFilesDir().getPath() + "/Aisle_Share_Data.json");
                    fos.write(aisleShareData.toString().getBytes());
                    fos.close();
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();

                Intent intent = new Intent(CurrentRecipe.this, Transfer.class);
                intent.putExtra(LIST_NAME, "Select which Items to Add");
                startActivity(intent);
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

    public void deleteItemsDialog(){
        // custom dialog
        final Dialog dialog = new Dialog(CurrentRecipe.this);
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
                boolean removals = false;

                items_backup.clear();
                for (Item item : items) {
                    items_backup.add(item);
                }

                int length = items.size();
                for (int index = length - 1; index > -1; index--) {
                    if (deviceName.equals(items.get(index).getOwner())) {
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
                    undoBox();
                }
                dialog.dismiss();
            }
        });

        delete_checked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                    undoBox();
                }
                dialog.dismiss();
            }
        });

        dialog.show();
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
                customAdapter.notifyDataSetChanged();
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

    public void initializeStorage(){
        try {
            File file = new File(getFilesDir().getPath() + "/Aisle_Share_Data.json");
            // Read or Initializes aisleShareData
            // Assumes the File itself has already been Initialized
            aisleShareData = new JSONObject(loadJSONFromAsset(file));

            currentOrder = aisleShareData.optJSONObject("Recipes").optJSONObject(recipeTitle).getInt("sort");
            isIncreasingOrder = aisleShareData.optJSONObject("Recipes").optJSONObject(recipeTitle).getBoolean("direction");
            JSONArray read_cat = aisleShareData.optJSONObject("Recipes").optJSONObject(recipeTitle).getJSONArray("category");
            if (read_cat != null) {
                int len = read_cat.length();
                for (int i=0;i<len;i++){
                    categories.add(read_cat.get(i).toString());
                }
            }
            JSONArray read_items = aisleShareData.optJSONObject("Recipes").optJSONObject(recipeTitle).getJSONArray("items");
            for(int index = 0; index < read_items.length(); index++){
                try {
                    JSONObject obj = new JSONObject(read_items.get(index).toString());
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
            aisleShareData.optJSONObject("Recipes").optJSONObject(recipeTitle).remove("items");
            aisleShareData.optJSONObject("Recipes").optJSONObject(recipeTitle).accumulate("items", new JSONArray());
            for(Item i : items){
                aisleShareData.optJSONObject("Recipes").optJSONObject(recipeTitle).optJSONArray("items").put(i.getJSONString());
            }
            aisleShareData.optJSONObject("Recipes").optJSONObject(recipeTitle).put("sort", currentOrder);
            aisleShareData.optJSONObject("Recipes").optJSONObject(recipeTitle).put("direction", isIncreasingOrder);
            aisleShareData.optJSONObject("Recipes").optJSONObject(recipeTitle).put("category", new JSONArray(categories));

            FileOutputStream fos = new FileOutputStream(getFilesDir().getPath() + "/Aisle_Share_Data.json");
            fos.write(aisleShareData.toString().getBytes());
            fos.close();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
}
