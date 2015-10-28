package com.aisleshare;

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
        if (id == R.id.action_settings) {
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
                aisleShareData.optJSONObject("Lists").accumulate("@sort", 1);
                aisleShareData.optJSONObject("Lists").accumulate("@direction", true);

                aisleShareData.accumulate("Recipes", new JSONObject());
                aisleShareData.optJSONObject("Recipes").accumulate("@sort", 1);
                aisleShareData.optJSONObject("Recipes").accumulate("@direction", true);

                aisleShareData.accumulate("Activities", new JSONObject());
                aisleShareData.optJSONObject("Activities").accumulate("@sort", 1);
                aisleShareData.optJSONObject("Activities").accumulate("@direction", true);

                aisleShareData.accumulate("Transfers", new JSONObject());
                aisleShareData.optJSONObject("Transfers").accumulate("sort", 1);
                aisleShareData.optJSONObject("Transfers").accumulate("direction", true);
                aisleShareData.optJSONObject("Transfers").accumulate("name", "");
                aisleShareData.optJSONObject("Transfers").accumulate("items", new JSONArray());

                FileOutputStream fos = new FileOutputStream(getFilesDir().getPath() + "/Aisle_Share_Data.json");
                fos.write(aisleShareData.toString().getBytes());
                fos.close();
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }
}
