package com.aisleshare;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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

public class Lists extends Fragment {
    public final static String LIST_NAME = "com.ShoppingList.MESSAGE";
    private ListView listView;
    private ArrayList<ListItem> lists;
    private Map<String, MenuItem> menuLists;
    private CustomListAdapter itemAdapter;
    private Context dashboard;
    private TextView emptyNotice;
    private boolean isIncreasingOrder;
    private int currentOrder;
    private String deviceName;
    private JSONObject aisleShareData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lists, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //setHasOptionsMenu(true);
        dashboard = getActivity();
        listView = (ListView) getView().findViewById(R.id.lists);
        lists = new ArrayList<>();
        menuLists = new HashMap<>();
        emptyNotice = (TextView) getView().findViewById(R.id.empty_notice);
        deviceName = Settings.Secure.getString(dashboard.getContentResolver(), Settings.Secure.ANDROID_ID);

        readSavedLists();

        if(lists.size() == 0){
            emptyNotice.setVisibility(View.VISIBLE);
        }

        itemAdapter = new CustomListAdapter(dashboard, lists, R.layout.row_dashboard);
        listView.setAdapter(itemAdapter);

        setListeners();
    }

    public void sortList(boolean reverseOrder, int order) {
        if(reverseOrder) {
            isIncreasingOrder = !isIncreasingOrder;
        }
        if(order != currentOrder){
            currentOrder = order;
            isIncreasingOrder = true;
        }

        ListItemComparator compare = new ListItemComparator(dashboard);

        // Unsorted
        if(currentOrder == -1){
            saveSortInfo();
            return;
        }

        switch (currentOrder){
            // Name
            case 0:{
                ListItemComparator.Name sorter = compare.new Name();
                Collections.sort(lists, sorter);
                break;}
            // Time Created
            case 1:{
                ListItemComparator.Created sorter = compare.new Created();
                Collections.sort(lists, sorter);
                break;}
            // Owner
            case 2:{
                ListItemComparator.Owner sorter = compare.new Owner();
                Collections.sort(lists, sorter);
                break;}
        }

        if(!isIncreasingOrder) {
            Collections.reverse(lists);
        }
        saveSortInfo();
    }

    public void setSortIcons(){
        if(currentOrder == -1){
            menuLists.get("sort").setIcon(0);
            menuLists.get("unsorted").setVisible(false);
        }
        else {
            menuLists.get("unsorted").setVisible(true);
            if(isIncreasingOrder) {
                menuLists.get("sort").setIcon(R.mipmap.inc_sort);
            }
            else{
                menuLists.get("sort").setIcon(R.mipmap.dec_sort);
            }
        }
    }

    // Popup for adding a List
    public void addListDialog(){
        // custom dialog
        final Dialog dialog = new Dialog(dashboard);
        dialog.setContentView(R.layout.dialog_add_name);
        dialog.setTitle("Add a New List");

        final EditText listName = (EditText) dialog.findViewById(R.id.Name);
        final Button cancel = (Button) dialog.findViewById(R.id.Cancel);
        final Button done = (Button) dialog.findViewById(R.id.Done);

        // Open keyboard automatically
        listName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
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
                if (!listName.getText().toString().isEmpty()) {
                    String name = listName.getText().toString();

                    for (int index = 0; index < lists.size(); index++) {
                        if (lists.get(index).getName().equals(name)) {
                            listName.setError("List already exists...");
                            return;
                        }
                    }

                    ListItem list = new ListItem(deviceName, name);
                    lists.add(list);
                    sortList(false, currentOrder);
                    dialog.dismiss();
                    itemAdapter.notifyDataSetChanged();
                    saveNewList(name, list.getCreated());
                    emptyNotice.setVisibility(View.INVISIBLE);

                    Intent intent = new Intent(dashboard, CurrentList.class);
                    intent.putExtra(LIST_NAME, name);
                    startActivity(intent);
                } else {
                    listName.setError("Name is empty...");
                }
            }
        });

        dialog.show();
    }

    // Popup for editing a List
    public void editListDialog(final int position){
        if(!deviceName.equals(lists.get(position).getOwner())) {
            Toast toast = Toast.makeText(dashboard, "You are not the owner...", Toast.LENGTH_LONG);
            toast.show();
            return;
        }

        // custom dialog
        final Dialog dialog = new Dialog(dashboard);
        dialog.setContentView(R.layout.dialog_add_name);
        dialog.setTitle("Edit List Name");

        final EditText listName = (EditText) dialog.findViewById(R.id.Name);
        final Button cancel = (Button) dialog.findViewById(R.id.Cancel);
        final Button done = (Button) dialog.findViewById(R.id.Done);
        final String orig_name = lists.get(position).getName();

        listName.setText(orig_name);

        // Open keyboard automatically
        listName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
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
                if (!listName.getText().toString().isEmpty()) {
                    String name = listName.getText().toString();

                    if(name.equals(orig_name)){
                        dialog.dismiss();
                    }

                    for (int index = 0; index < lists.size(); index++) {
                        if (lists.get(index).getName().equals(name) && index != position) {
                            listName.setError("List already exists...");
                            return;
                        }
                    }

                    lists.get(position).setName(name);
                    sortList(false, currentOrder);
                    dialog.dismiss();
                    itemAdapter.notifyDataSetChanged();

                    try {
                        // Need to update other fragments before saving
                        File file = new File(dashboard.getFilesDir().getPath() + "/Aisle_Share_Data.json");
                        aisleShareData = new JSONObject(loadJSONFromAsset(file));

                        JSONObject listData = aisleShareData.optJSONObject("Lists").optJSONObject(orig_name);
                        aisleShareData.optJSONObject("Lists").remove(orig_name);
                        aisleShareData.optJSONObject("Lists").put(name, listData);

                        FileOutputStream fos = new FileOutputStream(dashboard.getFilesDir().getPath() + "/Aisle_Share_Data.json");
                        fos.write(aisleShareData.toString().getBytes());
                        fos.close();
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    listName.setError("Name is empty...");
                }
            }
        });

        dialog.show();
    }

    public void readSavedLists(){
        try {
            File file = new File(dashboard.getFilesDir().getPath() + "/Aisle_Share_Data.json");
            // Read or Initializes aisleShareData
            // Assumes the File itself has already been Initialized
            aisleShareData = new JSONObject(loadJSONFromAsset(file));
            JSONArray listNames = aisleShareData.optJSONObject("Lists").names();
            currentOrder = aisleShareData.optInt("ListsSort");
            isIncreasingOrder = aisleShareData.optBoolean("ListsDirection");
            if(listNames != null) {
                for (int i = 0; i < listNames.length(); i++) {
                    try {
                        JSONObject entry = aisleShareData.optJSONObject("Lists").optJSONObject(listNames.get(i).toString());
                        if(entry != null) {
                            String owner = entry.optString("owner");
                            long created = entry.optLong("time");
                            lists.add(new ListItem(owner, listNames.get(i).toString(), created));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            sortList(false, currentOrder);
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

    public void saveSortInfo(){
        try {
            // Need to update other fragments before saving
            File file = new File(dashboard.getFilesDir().getPath() + "/Aisle_Share_Data.json");
            aisleShareData = new JSONObject(loadJSONFromAsset(file));

            aisleShareData.put("ListsSort", currentOrder);
            aisleShareData.put("ListsDirection", isIncreasingOrder);

            FileOutputStream fos = new FileOutputStream(dashboard.getFilesDir().getPath() + "/Aisle_Share_Data.json");
            fos.write(aisleShareData.toString().getBytes());
            fos.close();
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    public void saveNewList(String listTitle, long timeCreated){
        try {
            // Need to update other fragments before saving
            File file = new File(dashboard.getFilesDir().getPath() + "/Aisle_Share_Data.json");
            aisleShareData = new JSONObject(loadJSONFromAsset(file));

            aisleShareData.optJSONObject("Lists").accumulate(listTitle, new JSONObject());
            aisleShareData.optJSONObject("Lists").optJSONObject(listTitle).accumulate("items", new JSONArray());
            aisleShareData.optJSONObject("Lists").optJSONObject(listTitle).accumulate("sort", -1);
            aisleShareData.optJSONObject("Lists").optJSONObject(listTitle).accumulate("direction", true);
            aisleShareData.optJSONObject("Lists").optJSONObject(listTitle).accumulate("time", timeCreated);
            aisleShareData.optJSONObject("Lists").optJSONObject(listTitle).accumulate("owner", deviceName);

            FileOutputStream fos = new FileOutputStream(dashboard.getFilesDir().getPath() + "/Aisle_Share_Data.json");
            fos.write(aisleShareData.toString().getBytes());
            fos.close();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public void removeList(String listTitle){
        try {
            // Need to update other fragments before saving
            File file = new File(dashboard.getFilesDir().getPath() + "/Aisle_Share_Data.json");
            aisleShareData = new JSONObject(loadJSONFromAsset(file));

            aisleShareData.optJSONObject("Lists").remove(listTitle);

            FileOutputStream fos = new FileOutputStream(dashboard.getFilesDir().getPath() + "/Aisle_Share_Data.json");
            fos.write(aisleShareData.toString().getBytes());
            fos.close();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuLists.put("sort", menu.findItem(R.id.sort_root));
        menuLists.put("name", menu.findItem(R.id.sort_name));
        menuLists.put("time", menu.findItem(R.id.sort_time));
        menuLists.put("owner", menu.findItem(R.id.sort_owner));
        menuLists.put("unsorted", menu.findItem(R.id.unsorted));
        menuLists.put("delete", menu.findItem(R.id.delete));

        menuLists.get("name").setCheckable(true);
        menuLists.get("time").setCheckable(true);
        menuLists.get("owner").setCheckable(true);
        menuLists.get("unsorted").setVisible(false);

        setSortIcons();
        switch (currentOrder){
            case 0:
                menuLists.get("name").setChecked(true);
                break;
            case 1:
                menuLists.get("time").setChecked(true);
                break;
            case 2:
                menuLists.get("owner").setChecked(true);
                break;
        }
        super.onCreateOptionsMenu(menu, inflater);
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
                setSortIcons();
                clearMenuCheckables();
                option.setChecked(true);
                break;
            case R.id.sort_time:
                sortList(true, 1);
                setSortIcons();
                clearMenuCheckables();
                option.setChecked(true);
                break;
            case R.id.sort_owner:
                sortList(true, 2);
                setSortIcons();
                clearMenuCheckables();
                option.setChecked(true);
                break;
            case R.id.unsorted:
                sortList(false, -1);
                setSortIcons();
                clearMenuCheckables();
                break;
            case R.id.delete:
                deleteItems();
                break;
            case R.id.sort:
                return super.onOptionsItemSelected(option);
        }
        itemAdapter.notifyDataSetChanged();
        return super.onOptionsItemSelected(option);
    }

    public void clearMenuCheckables(){
        menuLists.get("name").setChecked(false);
        menuLists.get("time").setChecked(false);
        menuLists.get("owner").setChecked(false);
    }

    public AlertDialog confirmDeletion(final String listName, final int position)
    {
        return new AlertDialog.Builder(dashboard)
            .setTitle("Confirm Deletion")
            .setMessage("Are you sure? This cannot be undone.")
            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    emptyNotice = (TextView) getView().findViewById(R.id.empty_notice);

                    removeList(listName);
                    lists.remove(position);
                    itemAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                    Toast toast = Toast.makeText(dashboard, "List Deleted", Toast.LENGTH_LONG);
                    toast.show();
                    if (lists.size() == 0) {
                        emptyNotice.setVisibility(View.VISIBLE);
                    }
                }
            })
            .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            })
            .create();
    }

    public void deleteItems(){
        // custom dialog
        final Dialog dialog = new Dialog(dashboard); //has a problem, not caring atm
        dialog.setContentView(R.layout.dialog_select_list);

        final ListView lv = (ListView) dialog.findViewById(R.id.lists);
        final Button cancel = (Button) dialog.findViewById(R.id.cancel);

        final ArrayList<String> listNames = new ArrayList<>();
        if(lists.size() != 0) {
            dialog.setTitle("What Should We Delete?");
            for (ListItem i : lists) {
                listNames.add(i.getName());
            }
        }
        else{
            dialog.setTitle("No Lists to Delete.");
        }

        ArrayAdapter<String> itemAdapter = new ArrayAdapter<>(dashboard,android.R.layout.simple_list_item_1, listNames);
        lv.setAdapter(itemAdapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                confirmDeletion(listNames.get(position), position).show();
                dialog.dismiss();
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

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            // Floating Action Button
            FloatingActionButton addButton = (FloatingActionButton) getActivity().findViewById(R.id.float_button);
            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addListDialog();
                }
            });
        }
    }

    public void setListeners() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long id) {
                Intent intent = new Intent(dashboard, CurrentList.class);
                String name = lists.get(pos).getName();
                intent.putExtra(LIST_NAME, name);
                startActivity(intent);
            }
        });

        //Long Click for editing
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {
                editListDialog(pos);
                return true;
            }
        });
    }
}
