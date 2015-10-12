package com.aisleshare;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;

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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import java.util.TreeMap;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;


public class CurrentList extends AppCompatActivity {

    private ListView listView;
    private ArrayList<Item> itemList;
    private CustomAdapter itemAdapter;
    private ArrayList<String> testList;
    private boolean[] reverseSort = {false, false, false, false};
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_list);

        listView = (ListView)findViewById(R.id.currentItems);
        testList = new ArrayList<>();
        itemList = new ArrayList<>();

        testList.add("{\"name\":itemName,\"quantity\":7,\"type\":defType, \"timeCreated\":12105543, \"checked\":0}");
        testList.add("{\"name\":burgers,\"quantity\":5,\"type\":Meats, \"timeCreated\":12105543, \"checked\":0}");
        testList.add("{\"name\":Eggs,\"quantity\":2,\"type\":Bread, \"timeCreated\":12104543, \"checked\":0}");
        testList.add("{\"name\":Bacon,\"quantity\":100,\"type\":Meats, \"timeCreated\":12105533, \"checked\":0}");
        testList.add("{\"name\":Cheese,\"quantity\":4,\"type\":Dairy, \"timeCreated\":13105543, \"checked\":0}");
        testList.add("{\"name\":Buns,\"quantity\":6,\"type\":Bread, \"timeCreated\":12105843, \"checked\":0}");
                    //"{\"phonetype\":\"N95\",\"cat\":\"WP\"}"

        JSONObject obj;
        for(int i = 0; i < testList.size(); i++){
            try {
                obj = new JSONObject(testList.get(i));
                itemList.add(new Item(obj.getString("name")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        itemAdapter = new CustomAdapter(this, itemList);
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
            itemList.add(new Item(key, 0));
        }
        if (reverseSort[reverser]) {
            reverseSort[reverser] = false;
        } else {
            reverseSort[reverser] = true;
            Collections.reverse(itemList);
        }


        listView = (ListView) findViewById(R.id.currentItems);

        final CustomAdapter itemAdapter = new CustomAdapter(this, itemList);
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
        //startActivity(new Intent(ShoppingList.this, AddListMenu.class));

        final AlertDialog modal = new AlertDialog.Builder(CurrentList.this).create();
        modal.setTitle("Add a New Item");

        // Set up the input
        final EditText input = new EditText(CurrentList.this);
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

        // Done Button
        modal.setButton(-1, "Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (!input.getText().toString().isEmpty()) {
                    String text = input.getText().toString();
                    Item m = new Item(text);
                    itemList.add(m);
                    itemAdapter.notifyDataSetChanged();
                }
            }
        });

        // More Button
        modal.setButton(-3, "More", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (!input.getText().toString().isEmpty()) {
                    String text = input.getText().toString();
                    Item m = new Item(text);
                    itemList.add(m);
                    itemAdapter.notifyDataSetChanged();
                }
                addItemDialog();
            }
        });

        // Cancel Button
        modal.setButton(-2, "Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        modal.show();
    }

    public void layoutClick(View v){
        final LinearLayout ll = (LinearLayout) v;
        final CheckBox cb = (CheckBox)ll.getChildAt(0);
        final TextView tv = (TextView)ll.getChildAt(1);

        cb.toggle();
        toggleChecked(cb);
        setStrikeThrough(cb, tv);
        itemAdapter.notifyDataSetChanged();
    }
    public void textClick(View v){
        final TextView tv = (TextView)v;
        final LinearLayout ll = (LinearLayout) tv.getParent();
        final CheckBox cb = (CheckBox)ll.getChildAt(0);

        cb.toggle();
        toggleChecked(cb);
        setStrikeThrough(cb, tv);
        itemAdapter.notifyDataSetChanged();
    }
    public void checkBoxClick(View v){
        final CheckBox cb = (CheckBox)v;
        final LinearLayout ll = (LinearLayout) v.getParent();
        final TextView tv = (TextView)ll.getChildAt(1);

        toggleChecked(cb);
        setStrikeThrough(cb, tv);
        itemAdapter.notifyDataSetChanged();
    }

    public void setStrikeThrough(CheckBox cb, TextView tv){
        if(cb.isChecked()){
            tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        else {
            tv.setPaintFlags(0);
        }
    }

    public void toggleChecked(CheckBox cb){
        if (itemList.get(cb.getId()).getValue() == 0){
            itemList.get(cb.getId()).setValue(1);
        }
        else{
            itemList.get(cb.getId()).setValue(0);
        }
    }
}
