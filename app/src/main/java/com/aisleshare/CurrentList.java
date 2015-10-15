package com.aisleshare;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.getbase.floatingactionbutton.FloatingActionButton;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;


public class CurrentList extends AppCompatActivity {

    // Class Variables
    private ListView listView;
    private ArrayList<Item> items;
    private CustomAdapter itemAdapter;
    private boolean isIncreasingOrder;
    private int currentOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_list);

        listView = (ListView)findViewById(R.id.currentItems);
        items = new ArrayList<>();
        isIncreasingOrder = true;
        currentOrder = -1;

        ArrayList<String> jsonList = new ArrayList<>();
        jsonList.add("{\"name\":itemName,\"quantity\":1,\"type\":defType, \"timeCreated\":12105543, \"checked\":0}");
        jsonList.add("{\"name\":burgers,\"quantity\":5,\"type\":Meats, \"timeCreated\":12105543, \"checked\":0}");
        jsonList.add("{\"name\":Eggs,\"quantity\":2,\"type\":\"\", \"timeCreated\":12104543, \"checked\":0}");
        jsonList.add("{\"name\":Bacon,\"quantity\":100,\"type\":Meats, \"timeCreated\":12105533, \"checked\":0}");
        jsonList.add("{\"name\":Cheese,\"quantity\":4,\"type\":Dairy, \"timeCreated\":13105543, \"checked\":0}");
        jsonList.add("{\"name\":Buns,\"quantity\":1,\"type\":\"\", \"timeCreated\":12105843, \"checked\":0}");

        JSONObject obj;
        for(int i = 0; i < jsonList.size(); i++){
            try {
                obj = new JSONObject(jsonList.get(i));
                items.add(new Item(obj.getString("name"), obj.getString("type"), obj.getInt("quantity"),
                        obj.getInt("timeCreated"), obj.getInt("checked")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        itemAdapter = new CustomAdapter(this, items);
        listView.setAdapter(itemAdapter);

        String listTitle;
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
                addItemDialog();
            }
        });
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

        ItemComparator compare = new ItemComparator();

        // Unsorted
        if(currentOrder == -1){
            return;
        }
        // Name
        else if(currentOrder == 0){
            ItemComparator.Name sorter = compare.new Name();
            Collections.sort(items, sorter);
        }
        // Quantity
        else if(currentOrder == 1){
            ItemComparator.Quantity sorter = compare.new Quantity();
            Collections.sort(items, sorter);
        }
        // Time Created
        else if(currentOrder == 2){
            ItemComparator.Created sorter = compare.new Created();
            Collections.sort(items, sorter);
        }
        // Type
        else if(currentOrder == 3){
            ItemComparator.Type sorter = compare.new Type();
            Collections.sort(items, sorter);
        }

        if(!isIncreasingOrder){
            Collections.reverse(items);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_current_list, menu);
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
                break;
            case R.id.sort_quantity:
                sortList(true, 1);
                break;
            case R.id.sort_time:
                sortList(true, 2);
                break;
            case R.id.sort_type:
                sortList(true, 3);
                break;
        }

        itemAdapter.notifyDataSetChanged();
        return super.onOptionsItemSelected(option);
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

        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!itemQuantity.getText().toString().isEmpty()){
                    int value = Integer.parseInt(itemQuantity.getText().toString());
                    if (value > 1) {
                        itemQuantity.setText("" + (value - 1));
                    }
                }
            }
        });

        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!itemQuantity.getText().toString().isEmpty()) {
                    int value = Integer.parseInt(itemQuantity.getText().toString());
                    itemQuantity.setText("" + (value + 1));
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
                    int quantity;
                    if(!itemQuantity.getText().toString().isEmpty()) {
                        quantity = Integer.parseInt(itemQuantity.getText().toString());
                    }
                    else{
                        quantity = 1;
                    }
                    Item m = new Item(name, type, quantity);
                    items.add(m);
                    sortList(false, currentOrder);
                    itemAdapter.notifyDataSetChanged();
                }
                dialog.dismiss();
                addItemDialog();
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!itemName.getText().toString().isEmpty()) {
                    String name = itemName.getText().toString();
                    String type = itemType.getText().toString();
                    int quantity;
                    if(!itemQuantity.getText().toString().isEmpty()) {
                        quantity = Integer.parseInt(itemQuantity.getText().toString());
                    }
                    else{
                        quantity = 1;
                    }
                    Item m = new Item(name, type, quantity);
                    items.add(m);
                    sortList(false, currentOrder);
                    itemAdapter.notifyDataSetChanged();
                }
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    // Checks/UnChecks an item by clicking on any element in its row
    public void itemClick(View v){
        if(v.getTag().equals("row")){
            final LinearLayout row = (LinearLayout) v;
            final CheckBox cb = (CheckBox)row.getChildAt(0);

            toggleChecked(cb);
        }
        else if(v.getTag().equals("name")){
            final TextView name = (TextView)v;
            final LinearLayout column = (LinearLayout) name.getParent();
            final LinearLayout row = (LinearLayout) column.getParent();
            final CheckBox cb = (CheckBox)row.getChildAt(0);

            toggleChecked(cb);
        }
        else if(v.getTag().equals("checkBox")){
            final CheckBox cb = (CheckBox)v;

            toggleChecked(cb);
        }
        else if(v.getTag().equals("column")){
            final LinearLayout column = (LinearLayout) v;
            final LinearLayout row = (LinearLayout) column.getParent();
            final CheckBox cb = (CheckBox)row.getChildAt(0);

            toggleChecked(cb);
        }
        else if(v.getTag().equals("type")){
            final TextView type = (TextView)v;
            final LinearLayout column = (LinearLayout) type.getParent();
            final LinearLayout row = (LinearLayout) column.getParent();
            final CheckBox cb = (CheckBox)row.getChildAt(0);

            toggleChecked(cb);
        }

        sortList(false, currentOrder);
        itemAdapter.notifyDataSetChanged();
    }

    public void toggleChecked(CheckBox cb){
        if (items.get(cb.getId()).getChecked() == 0){
            items.get(cb.getId()).setChecked(1);
        }
        else{
            items.get(cb.getId()).setChecked(0);
        }
    }
}
