package com.aisleshare;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Dashboard  extends AppCompatActivity {
    public final static String LIST_NAME = "com.ShoppingList.MESSAGE";
    private TabsAdapter adapter;
    private ViewPager pager;
    private JSONObject aisleShareData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        initializeStorage();
        adapter = new TabsAdapter(getSupportFragmentManager());
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);

        String listOpened = aisleShareData.optString("ListOpened");
        if(!listOpened.equals("")){
            Intent intent = new Intent(Dashboard.this, CurrentList.class);
            String name = listOpened;
            intent.putExtra(LIST_NAME, name);
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    public void initializeStorage(){
        try {
            File file = new File(getFilesDir().getPath() + "/Aisle_Share_Data.json");
            if (!file.exists()) {
                aisleShareData = new JSONObject();
                aisleShareData.accumulate("Lists", new JSONObject());
                aisleShareData.accumulate("ListsSort", 1);
                aisleShareData.accumulate("ListsDirection", true);
                aisleShareData.accumulate("ListOpened", "");

                aisleShareData.accumulate("Recipes", new JSONObject());
                aisleShareData.accumulate("RecipesSort", 1);
                aisleShareData.accumulate("RecipesDirection", true);

                aisleShareData.accumulate("Activities", new JSONObject());
                aisleShareData.accumulate("ActivitiesSort", 1);
                aisleShareData.accumulate("ActivitiesDirection", true);

                aisleShareData.accumulate("Transfers", new JSONObject());
                aisleShareData.optJSONObject("Transfers").accumulate("sort", 1);
                aisleShareData.optJSONObject("Transfers").accumulate("direction", true);
                aisleShareData.optJSONObject("Transfers").accumulate("name", "");
                aisleShareData.optJSONObject("Transfers").accumulate("items", new JSONArray());

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
