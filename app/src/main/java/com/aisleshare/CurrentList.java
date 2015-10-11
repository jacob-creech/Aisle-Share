package com.aisleshare;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class CurrentList extends AppCompatActivity {

    private ListView listView;
    private ArrayList<Item> itemList;
    private CustomAdapter itemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_list);

        listView = (ListView)findViewById(R.id.currentItems);
        ArrayList<String> testList= new ArrayList<>();
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
