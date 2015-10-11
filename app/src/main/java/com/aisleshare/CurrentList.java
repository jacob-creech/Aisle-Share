package com.aisleshare;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
                items.add(new Item(obj.getString("name")));
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
        final AlertDialog modal = new AlertDialog.Builder(CurrentList.this).create();
        modal.setTitle("Add a New Item");

        // Set up the text input
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

        // Done Button (Positive)
        modal.setButton(-1, "Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (!input.getText().toString().isEmpty()) {
                    String text = input.getText().toString();
                    Item m = new Item(text);
                    items.add(m);
                    itemAdapter.notifyDataSetChanged();
                }
            }
        });

        // More Button (Neutral)
        modal.setButton(-3, "More", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (!input.getText().toString().isEmpty()) {
                    String text = input.getText().toString();
                    Item m = new Item(text);
                    items.add(m);
                    itemAdapter.notifyDataSetChanged();
                }
                addItemDialog();
            }
        });

        // Cancel Button (Negative)
        modal.setButton(-2, "Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        modal.show();
    }

    public void itemClick(View v){
        if(v.getTag().equals("linearLayout")){
            final LinearLayout ll = (LinearLayout) v;
            final CheckBox cb = (CheckBox)ll.getChildAt(0);

            toggleChecked(cb);
        }
        else if(v.getTag().equals("textView")){
            final TextView tv = (TextView)v;
            final LinearLayout ll = (LinearLayout) tv.getParent();
            final CheckBox cb = (CheckBox)ll.getChildAt(0);

            toggleChecked(cb);
        }
        else if(v.getTag().equals("checkBox")){
            final CheckBox cb = (CheckBox)v;

            toggleChecked(cb);
        }
        itemAdapter.notifyDataSetChanged();
    }

    public void toggleChecked(CheckBox cb){
        if (items.get(cb.getId()).getValue() == 0){
            items.get(cb.getId()).setValue(1);
        }
        else{
            items.get(cb.getId()).setValue(0);
        }
    }
}
