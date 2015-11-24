package com.aisleshare;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Dashboard  extends AppCompatActivity {
    public final static String LIST_NAME = "com.ShoppingList.MESSAGE";
    private TabsAdapter adapter;
    private ViewPager pager;
    private boolean disableAutocomplete;
    private Map<String, MenuItem> menuItems;
    private JSONObject aisleShareData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        initializeStorage();
        adapter = new TabsAdapter(getSupportFragmentManager());
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);
        disableAutocomplete = aisleShareData.optBoolean("DisableAutocomplete");
        menuItems = new HashMap<>();

        String listOpened = aisleShareData.optString("ListOpened");
        if(!listOpened.equals("")){
            pager.setCurrentItem(0);
            Intent intent = new Intent(Dashboard.this, CurrentList.class);
            intent.putExtra(LIST_NAME, listOpened);
            startActivity(intent);
        }

        String recipeOpened = aisleShareData.optString("RecipeOpened");
        if(!recipeOpened.equals("")) {
            pager.setCurrentItem(1);
            Intent intent = new Intent(Dashboard.this, CurrentRecipe.class);
            intent.putExtra(LIST_NAME, recipeOpened);
            startActivity(intent);
        }

        String activityOpened = aisleShareData.optString("ActivityOpened");
        if(!activityOpened.equals("")){
            pager.setCurrentItem(2);
            Intent intent = new Intent(Dashboard.this, CurrentActivity.class);
            intent.putExtra(LIST_NAME, activityOpened);
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        menuItems.put("disableAutocomplete", menu.findItem(R.id.autocomplete));

        menuItems.get("disableAutocomplete").setChecked(disableAutocomplete);
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
            case R.id.autocomplete:
                disableAutocomplete = !disableAutocomplete;
                menuItems.get("disableAutocomplete").setChecked(disableAutocomplete);
                saveData();
                return true;
            case R.id.clearAutocomplete:
                confirmAutocompleteDeletion().show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void initializeStorage(){
        try {
            File file = new File(getFilesDir().getPath() + "/Aisle_Share_Data.json");
            if (!file.exists()) {
                aisleShareData = new JSONObject();
                aisleShareData.accumulate("Lists", new JSONObject());
                aisleShareData.accumulate("ListsSort", -1);
                aisleShareData.accumulate("ListsDirection", true);
                aisleShareData.accumulate("ListOpened", "");

                aisleShareData.accumulate("Recipes", new JSONObject());
                aisleShareData.accumulate("RecipesSort", -1);
                aisleShareData.accumulate("RecipesDirection", true);
                aisleShareData.accumulate("RecipeOpened", "");

                aisleShareData.accumulate("Activities", new JSONObject());
                aisleShareData.accumulate("ActivitiesSort", -1);
                aisleShareData.accumulate("ActivitiesDirection", true);
                aisleShareData.accumulate("ActivityOpened", "");

                aisleShareData.accumulate("Transfers", new JSONObject());
                aisleShareData.optJSONObject("Transfers").accumulate("sort", -1);
                aisleShareData.optJSONObject("Transfers").accumulate("direction", true);
                aisleShareData.optJSONObject("Transfers").accumulate("name", "");
                aisleShareData.optJSONObject("Transfers").accumulate("items", new JSONArray());

                aisleShareData.accumulate("DisableAutocomplete", false);

                FileOutputStream fos = new FileOutputStream(getFilesDir().getPath() + "/Aisle_Share_Data.json");
                fos.write(aisleShareData.toString().getBytes());
                fos.close();
            }
            else{
                aisleShareData = new JSONObject(loadJSONFromAsset(file));
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    public void saveData(){
        try {
            File file = new File(getFilesDir().getPath() + "/Aisle_Share_Data.json");
            aisleShareData = new JSONObject(loadJSONFromAsset(file));

            aisleShareData.put("DisableAutocomplete", disableAutocomplete);

            FileOutputStream fos = new FileOutputStream(getFilesDir().getPath() + "/Aisle_Share_Data.json");
            fos.write(aisleShareData.toString().getBytes());
            fos.close();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }


    public AlertDialog confirmAutocompleteDeletion()
    {
        return new AlertDialog.Builder(Dashboard.this)
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure? Erasing the autocomplete dictionary cannot be undone.")
                .setPositiveButton("Clear", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        try {
                            File file = new File(getFilesDir().getPath() + "/Aisle_Share_Data.json");
                            aisleShareData = new JSONObject(loadJSONFromAsset(file));

                            aisleShareData.put("category", new JSONArray());
                            aisleShareData.put("name", new JSONArray());
                            aisleShareData.put("unit", new JSONArray());

                            FileOutputStream fos = new FileOutputStream(getFilesDir().getPath() + "/Aisle_Share_Data.json");
                            fos.write(aisleShareData.toString().getBytes());
                            fos.close();

                            Toast toast = Toast.makeText(Dashboard.this, "Cleared", Toast.LENGTH_LONG);
                            toast.show();
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
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
}
