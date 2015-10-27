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
import java.util.Map;

public class Activities extends Fragment {
    // // TODO: update ACTIVITY_NAME with a com.Activities.MESSAGE variable
    public final static String ACTIVITY_NAME = "com.ShoppingList.MESSAGE";
    private ListView listView;
    private ArrayList<String> activities;
    private Map<String, MenuItem> menuActivities;
    private ArrayAdapter<String> itemAdapter;
    private Context dashboard;
    private TextView emptyNotice;
    private String deviceName;
    private JSONObject aisleShareData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_activities, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        dashboard = getActivity();
        listView = (ListView) getView().findViewById(R.id.activities);
        activities = new ArrayList<>();
        emptyNotice = (TextView) getView().findViewById(R.id.empty_notice);
        deviceName = Settings.Secure.getString(dashboard.getContentResolver(), Settings.Secure.ANDROID_ID);

        readSavedActivities();

        if(activities.size() == 0){
            emptyNotice.setVisibility(View.VISIBLE);
        }

        itemAdapter = new ArrayAdapter<>(dashboard,R.layout.row_dashboard, activities);
        listView.setAdapter(itemAdapter);

        setListeners();
    }

    // Popup for adding an Activity
    public void addActivityDialog(){
        // custom dialog
        final Dialog dialog = new Dialog(dashboard);
        dialog.setContentView(R.layout.dialog_add_name);
        dialog.setTitle("Add a New Activity");

        final EditText activityName = (EditText) dialog.findViewById(R.id.Name);
        final Button cancel = (Button) dialog.findViewById(R.id.Cancel);
        final Button done = (Button) dialog.findViewById(R.id.Done);

        // Open keyboard automatically
        activityName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
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
                if (!activityName.getText().toString().isEmpty()) {
                    String name = activityName.getText().toString();

                    if(name.equals("@sort") || name.equals("@order")){
                        activityName.setError("Sorry, that name is reserved...");
                        return;
                    }

                    for (int index = 0; index < activities.size(); index++) {
                        if (activities.get(index).equals(name)) {
                            activityName.setError("Activity already exists...");
                            return;
                        }
                    }

                    dialog.dismiss();

                    Intent intent = new Intent(dashboard, CurrentActivity.class);

                    activities.add(name);
                    itemAdapter.notifyDataSetChanged();
                    saveNewActivity(name);
                    emptyNotice.setVisibility(View.INVISIBLE);

                    intent.putExtra(ACTIVITY_NAME, name);
                    startActivity(intent);
                } else {
                    activityName.setError("Name is empty...");
                }
            }
        });

        dialog.show();
    }

    // Popup for editing a Activity
    public void editActivityDialog(final int position){
        // todo uncomment owner check once implemented
        /*if(!deviceName.equals(activities.get(position).getOwner())) {
            Toast toast = Toast.makeText(dashboard, "You are not the owner...", Toast.LENGTH_LONG);
            toast.show();
            return;
        }*/

        // custom dialog
        final Dialog dialog = new Dialog(dashboard);
        dialog.setContentView(R.layout.dialog_add_name);
        dialog.setTitle("Edit Activity Name");

        final EditText activityName = (EditText) dialog.findViewById(R.id.Name);
        final Button cancel = (Button) dialog.findViewById(R.id.Cancel);
        final Button done = (Button) dialog.findViewById(R.id.Done);
        final String orig_name = activities.get(position);

        activityName.setText(orig_name);

        // Open keyboard automatically
        activityName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
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
                if (!activityName.getText().toString().isEmpty()) {
                    String name = activityName.getText().toString();

                    if(name.equals(orig_name)){
                        dialog.dismiss();
                    }

                    if(name.equals("@sort") || name.equals("@order")){
                        activityName.setError("Sorry, that name is reserved...");
                        return;
                    }

                    for (int index = 0; index < activities.size(); index++) {
                        if (activities.get(index).equals(name) && index != position) {
                            activityName.setError("Activity already exists...");
                            return;
                        }
                    }

                    activities.set(position, name);
                    // TODO: uncomment once implemented
                    //sortActivity(false, currentOrder);
                    dialog.dismiss();
                    itemAdapter.notifyDataSetChanged();

                    try {
                        // Need to update other fragments before saving
                        File file = new File(dashboard.getFilesDir().getPath() + "/Aisle_Share_Data.json");
                        aisleShareData = new JSONObject(loadJSONFromAsset(file));

                        JSONObject activityData = aisleShareData.optJSONObject("Activities").optJSONObject(orig_name);
                        aisleShareData.optJSONObject("Activities").remove(orig_name);
                        aisleShareData.optJSONObject("Activities").put(name, activityData);

                        FileOutputStream fos = new FileOutputStream(dashboard.getFilesDir().getPath() + "/Aisle_Share_Data.json");
                        fos.write(aisleShareData.toString().getBytes());
                        fos.close();
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    activityName.setError("Name is empty...");
                }
            }
        });

        dialog.show();
    }

    public void readSavedActivities(){
        try {
            File file = new File(dashboard.getFilesDir().getPath() + "/Aisle_Share_Data.json");
            // Read or Initializes aisleShareData
            // Assumes the File itself has already been Initialized
            aisleShareData = new JSONObject(loadJSONFromAsset(file));
            JSONArray activityNames = aisleShareData.optJSONObject("Activities").names();
            if(activityNames != null) {
                for (int i = 0; i < activityNames.length(); i++) {
                    try {
                        // todo initialize with owner and timeCreated
                        if(!activityNames.get(i).toString().equals("@sort") && !activityNames.get(i).toString().equals("@order")) {
                            activities.add(activityNames.get(i).toString());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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

    public void saveNewActivity(String activityTitle){
        try {
            // Need to update other fragments before saving
            File file = new File(dashboard.getFilesDir().getPath() + "/Aisle_Share_Data.json");
            aisleShareData = new JSONObject(loadJSONFromAsset(file));

            aisleShareData.optJSONObject("Activities").accumulate(activityTitle, new JSONObject());
            aisleShareData.optJSONObject("Activities").optJSONObject(activityTitle).accumulate("items", new JSONArray());
            aisleShareData.optJSONObject("Activities").optJSONObject(activityTitle).put("sort", 2);
            aisleShareData.optJSONObject("Activities").optJSONObject(activityTitle).put("direction", true);
            // todo save out owner and timeCreated info

            FileOutputStream fos = new FileOutputStream(dashboard.getFilesDir().getPath() + "/Aisle_Share_Data.json");
            fos.write(aisleShareData.toString().getBytes());
            fos.close();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public void removeActivity(String activityTitle){
        try {
            // Need to update other fragments before saving
            File file = new File(dashboard.getFilesDir().getPath() + "/Aisle_Share_Data.json");
            aisleShareData = new JSONObject(loadJSONFromAsset(file));

            aisleShareData.optJSONObject("Activities").remove(activityTitle);

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
        menuActivities.put("sort", menu.findItem(R.id.sort_root));
        menuActivities.put("name", menu.findItem(R.id.sort_name));
        menuActivities.put("time", menu.findItem(R.id.sort_time));
        menuActivities.put("owner", menu.findItem(R.id.sort_owner));
        menuActivities.put("unsorted", menu.findItem(R.id.unsorted));
        menuActivities.put("delete", menu.findItem(R.id.delete_items));

        menuActivities.get("name").setCheckable(true);
        menuActivities.get("time").setCheckable(true);
        menuActivities.get("owner").setCheckable(true);
        menuActivities.get("unsorted").setVisible(false);
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
                //sortActivity(true, 0);
                //clearMenuCheckables();
                //option.setChecked(true);
                break;
            case R.id.sort_time:
                //sortActivity(true, 1);
                //clearMenuCheckables();
                //option.setChecked(true);
                break;
            case R.id.sort_owner:
                //sortActivity(true, 2);
                //clearMenuCheckables();
                //option.setChecked(true);
                break;
            case R.id.unsorted:
                //sortActivity(false, -1);
                //clearMenuCheckables();
                break;
            case R.id.delete_items:
                deleteItems();
                break;
            case R.id.sort:
                return super.onOptionsItemSelected(option);
        }
        itemAdapter.notifyDataSetChanged();
        return super.onOptionsItemSelected(option);
    }

    public void clearMenuCheckables(){
        menuActivities.get("name").setChecked(false);
        menuActivities.get("time").setChecked(false);
        menuActivities.get("owner").setChecked(false);
    }

    public AlertDialog confirmDeletion(final String activityName, final int position)
    {
        return new AlertDialog.Builder(dashboard)
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure? This cannot be undone.")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        emptyNotice = (TextView) getView().findViewById(R.id.empty_notice);

                        removeActivity(activityName);
                        activities.remove(position);
                        itemAdapter.notifyDataSetChanged();
                        dialog.dismiss();
                        Toast toast = Toast.makeText(dashboard, "Activity Deleted", Toast.LENGTH_LONG);
                        toast.show();
                        if (activities.size() == 0) {
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
        System.out.println("DELETE ITEMS");
        // custom dialog
        final Dialog dialog = new Dialog(dashboard); //has a problem, not caring atm
        dialog.setContentView(R.layout.dialog_select_list);

        final ListView lv = (ListView) dialog.findViewById(R.id.lists);
        final Button cancel = (Button) dialog.findViewById(R.id.cancel);

        final ArrayList<String> activityNames = new ArrayList<>();
        if(activities.size() != 0) {
            dialog.setTitle("What Should We Delete?");
            for (String i : activities) {
                activityNames.add(i);
            }
        }
        else{
            dialog.setTitle("No Activities to Delete.");
        }

        ArrayAdapter<String> itemAdapter = new ArrayAdapter<>(dashboard,android.R.layout.simple_list_item_1, activityNames);
        lv.setAdapter(itemAdapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                confirmDeletion(activityNames.get(position), position).show();
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

    public void setListeners() {
        // Floating Action Button
        FloatingActionButton addButton = (FloatingActionButton) getView().findViewById(R.id.float_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addActivityDialog();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long id) {
                Intent intent = new Intent(dashboard, CurrentActivity.class);
                String name = activities.get(pos);
                intent.putExtra(ACTIVITY_NAME, name);
                startActivity(intent);
            }
        });

        //Long Click for editing
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {
                editActivityDialog(pos);
                return true;
            }
        });
    }
}
