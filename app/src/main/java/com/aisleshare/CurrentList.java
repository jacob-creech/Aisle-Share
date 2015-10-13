package com.aisleshare;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
<<<<<<< Updated upstream
=======

>>>>>>> Stashed changes
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
<<<<<<< Updated upstream
import android.widget.NumberPicker;
import android.widget.TextView;
=======
>>>>>>> Stashed changes
import java.util.TreeMap;
import android.widget.Toast;
import com.getbase.floatingactionbutton.FloatingActionButton;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;


public class CurrentList extends AppCompatActivity {

    private ListView listView;
<<<<<<< Updated upstream
    private ArrayList<Item> items;
    private CustomAdapter itemAdapter;
    private ArrayList<String> jsonList;
=======
    private ArrayList<Model> itemList;
    private ArrayList<String> testList;
>>>>>>> Stashed changes
    private boolean[] reverseSort = {false, false, false, false};
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_list);
<<<<<<< Updated upstream

        listView = (ListView)findViewById(R.id.currentItems);
        ArrayList<String> jsonList = new ArrayList<>();
        items = new ArrayList<>();
=======
        testList = new ArrayList<>();
        itemList = new ArrayList<>();
>>>>>>> Stashed changes

        jsonList.add("{\"name\":itemName,\"quantity\":7,\"type\":defType, \"timeCreated\":12105543, \"checked\":0}");
        jsonList.add("{\"name\":burgers,\"quantity\":5,\"type\":Meats, \"timeCreated\":12105543, \"checked\":0}");
        jsonList.add("{\"name\":Eggs,\"quantity\":2,\"type\":Bread, \"timeCreated\":12104543, \"checked\":0}");
        jsonList.add("{\"name\":Bacon,\"quantity\":100,\"type\":Meats, \"timeCreated\":12105533, \"checked\":0}");
        jsonList.add("{\"name\":Cheese,\"quantity\":4,\"type\":Dairy, \"timeCreated\":13105543, \"checked\":0}");
        jsonList.add("{\"name\":Buns,\"quantity\":6,\"type\":Bread, \"timeCreated\":12105843, \"checked\":0}");
                    //"{\"phonetype\":\"N95\",\"cat\":\"WP\"}"

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

    //Function obtained from http://stackoverflow.com/questions/1448369/how-to-sort-a-treemap-based-on-its-values
    public static <K, V extends Comparable<V>> Map<K, V> sortByValues(final Map<K, V> map) {
        Comparator<K> valueComparator =  new Comparator<K>() {
            public int compare(K k1, K k2) {
                int compare = map.get(k2).compareTo(map.get(k1));
                if (compare == 0) return 1;
                else return compare;
            }
        };
        Map<K, V> sortedByValues = new TreeMap<K, V>(valueComparator);
        sortedByValues.putAll(map);
        return sortedByValues;
    }

    public void sort_list(String variable, int reverser) {
<<<<<<< Updated upstream
        String str1 = "", str2 = "";
        Integer int1 = 0, int2 = 0;
        for(int i = 0; i < items.size() - 1; i++) {
            for(int j = 0; j < items.size() - i - 1; j++) {
                switch(variable) {
                    case "name":
                        str1 = items.get(j).getName();
                        str2 = items.get(j+1).getName();
                        break;
                    case "type":
                        str1 = items.get(j).getType();
                        str2 = items.get(j+1).getType();
                        break;
                    case "timeCreated":
                        int1 = items.get(j).getCreated();
                        int2 = items.get(j+1).getCreated();
                        break;
                    case "quantity":
                        int1 = items.get(j).getQuantity();
                        int2 = items.get(j+1).getQuantity();
                        break;
                }
                if(reverser == 1 || reverser == 2) {
                    if(int1 > int2) {
                        Item temp = items.get(j);
                        items.set(j, items.get(j+1));
                        items.set(j+1, temp);
                    }
                }
                else {
                    if(str1.compareTo(str2) < 0) {
                        Item temp = items.get(j);
                        items.set(j, items.get(j+1));
                        items.set(j+1, temp);
                    }
                }
            }
=======
        itemList.clear();
        Map pairMap;
        if(reverser == 1 || reverser == 2) {
            pairMap = new HashMap<String, Integer>();
        }
        else {
            pairMap = new HashMap<String, String>();
        }
        JSONObject obj = null;
        try {
            for (int i = 0; i < testList.size(); i++) {
                obj = new JSONObject(testList.get(i));
                pairMap.put(obj.getString("name"), obj.get(variable));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Map<String, String> treeMap = sortByValues(pairMap);
        for (String key : treeMap.keySet()) {
            itemList.add(new Model(key, 0));
>>>>>>> Stashed changes
        }
        if (reverseSort[reverser]) {
            reverseSort[reverser] = false;
        } else {
            reverseSort[reverser] = true;
<<<<<<< Updated upstream
            Collections.reverse(items);
        }
        for(int i = 0; i < 4; i++) {
            if(i != reverser) {
                reverseSort[i] = false;
            }
        }

        listView = (ListView) findViewById(R.id.currentItems);

        final CustomAdapter itemAdapter = new CustomAdapter(this, items);
=======
            Collections.reverse(itemList);
        }


        listView = (ListView) findViewById(R.id.currentItems);

        final CustomAdapter itemAdapter = new CustomAdapter(this, itemList);
>>>>>>> Stashed changes
        listView.setAdapter(itemAdapter);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_current_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id) {
            case R.id.sort_name:
                sort_list("name", 0);
                break;
            case R.id.sort_quantity:
                sort_list("quantity", 1);
                break;
            case R.id.sort_time:
                sort_list("timeCreated", 2);
                break;
            case R.id.sort_type:
                sort_list("type", 3);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

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
                    itemAdapter.notifyDataSetChanged();
                }
                dialog.dismiss();
            }
        });

        dialog.show();
    }

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
