package com.aisleshare;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import com.getbase.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Recipes extends Fragment {
    // TODO: update RECIPE_NAME with a com.Recipes.MESSAGE variable
    public final static String RECIPE_NAME = "com.ShoppingList.MESSAGE";
    public final static String RECIPE_PREF = "RecipePreferences";
    private ListView listView;
    private ArrayList<String> recipes;
    private ArrayAdapter<String> itemAdapter;
    private Context dashboard;
    private TextView emptyNotice;
    private JSONObject aisleShareData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recipes, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        dashboard = getActivity();
        listView = (ListView) getView().findViewById(R.id.recipes);
        recipes = new ArrayList<>();
        emptyNotice = (TextView) getView().findViewById(R.id.empty_notice);

        readSavedRecipes();

        if(recipes.size() == 0){
            emptyNotice.setVisibility(View.VISIBLE);
        }

        itemAdapter = new ArrayAdapter<>(dashboard,R.layout.row_dashboard, recipes);
        listView.setAdapter(itemAdapter);

        FloatingActionButton addButton = (FloatingActionButton) getView().findViewById(R.id.float_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRecipeDialog();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long id) {
                Intent intent = new Intent(dashboard, CurrentRecipe.class);
                String name = recipes.get(pos);
                intent.putExtra(RECIPE_NAME, name);
                startActivity(intent);
            }
        });
    }

    // Popup for adding a Recipe
    public void addRecipeDialog(){
        // custom dialog
        final Dialog dialog = new Dialog(dashboard);
        dialog.setContentView(R.layout.dialog_add_name);
        dialog.setTitle("Add a New Recipe");

        final EditText recipeName = (EditText) dialog.findViewById(R.id.Name);
        final Button cancel = (Button) dialog.findViewById(R.id.Cancel);
        final Button done = (Button) dialog.findViewById(R.id.Done);

        // Open keyboard automatically
        recipeName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
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
                if (!recipeName.getText().toString().isEmpty()) {
                    String name = recipeName.getText().toString();

                    for (int index = 0; index < recipes.size(); index++) {
                        if (recipes.get(index).equals(name)) {
                            recipeName.setError("Recipe already exists...");
                            return;
                        }
                    }

                    dialog.dismiss();

                    Intent intent = new Intent(dashboard, CurrentRecipe.class);

                    recipes.add(name);
                    itemAdapter.notifyDataSetChanged();
                    saveNewRecipe(name);
                    emptyNotice.setVisibility(View.INVISIBLE);

                    intent.putExtra(RECIPE_NAME, name);
                    startActivity(intent);
                } else {
                    recipeName.setError("Name is empty...");
                }
            }
        });

        dialog.show();
    }

    public void readSavedRecipes(){
        try {
            File file = new File(dashboard.getFilesDir().getPath() + "/Aisle_Share_Data.json");
            // Read or Initializes aisleShareData
            // Assumes the File itself has already been Initialized
            aisleShareData = new JSONObject(loadJSONFromAsset(file));
            JSONArray recipeNames = aisleShareData.optJSONObject("Recipes").names();
            if(recipeNames != null) {
                for (int i = 0; i < recipeNames.length(); i++) {
                    try {
                        recipes.add(recipeNames.get(i).toString());
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

    public void saveNewRecipe(String recipeTitle){
        try {
            // Need to update other fragments before saving
            File file = new File(dashboard.getFilesDir().getPath() + "/Aisle_Share_Data.json");
            aisleShareData = new JSONObject(loadJSONFromAsset(file));

            aisleShareData.optJSONObject("Recipes").accumulate(recipeTitle, new JSONObject());
            aisleShareData.optJSONObject("Recipes").optJSONObject(recipeTitle).accumulate("items", new JSONArray());
            aisleShareData.optJSONObject("Recipes").optJSONObject(recipeTitle).put("sort", 2);
            aisleShareData.optJSONObject("Recipes").optJSONObject(recipeTitle).put("direction", true);

            FileOutputStream fos = new FileOutputStream(dashboard.getFilesDir().getPath() + "/Aisle_Share_Data.json");
            fos.write(aisleShareData.toString().getBytes());
            fos.close();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
}
