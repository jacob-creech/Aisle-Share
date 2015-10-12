package com.aisleshare;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import com.getbase.floatingactionbutton.FloatingActionButton;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;


public class CurrentList extends AppCompatActivity {

    private ListView listView;
    private ArrayList<Item> items;
    private CustomAdapter itemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_list);

        listView = (ListView)findViewById(R.id.currentItems);
        ArrayList<String> jsonList = new ArrayList<>();
        items = new ArrayList<>();

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
