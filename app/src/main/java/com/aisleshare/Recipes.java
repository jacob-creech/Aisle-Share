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

public class Recipes extends Fragment {
    // TODO: update RECIPE_NAME with a com.Recipes.MESSAGE variable
    public final static String RECIPE_NAME = "com.ShoppingList.MESSAGE";
    private ListView listView;
    private ArrayList<String> recipes;
    private Map<String, MenuItem> menuRecipes;
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
        deviceName = Settings.Secure.getString(dashboard.getContentResolver(), Settings.Secure.ANDROID_ID);

        readSavedRecipes();

        if(recipes.size() == 0){
            emptyNotice.setVisibility(View.VISIBLE);
        }

        itemAdapter = new ArrayAdapter<>(dashboard,R.layout.row_dashboard, recipes);
        listView.setAdapter(itemAdapter);

        setListeners();
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

                    if(name.equals("@sort")){
                        recipeName.setError("Sorry, that name is reserved...");
                        return;
                    }

                    for (int index = 0; index < recipes.size(); index++) {
                        if (recipes.get(index).equals(name) || name.equals("@order")) {
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

    // Popup for editing a Recipe
    public void editRecipeDialog(final int position){
        // todo uncomment owner check once implemented
        /*if(!deviceName.equals(recipes.get(position).getOwner())) {
            Toast toast = Toast.makeText(dashboard, "You are not the owner...", Toast.LENGTH_LONG);
            toast.show();
            return;
        }*/

        // custom dialog
        final Dialog dialog = new Dialog(dashboard);
        dialog.setContentView(R.layout.dialog_add_name);
        dialog.setTitle("Edit Recipe Name");

        final EditText recipeName = (EditText) dialog.findViewById(R.id.Name);
        final Button cancel = (Button) dialog.findViewById(R.id.Cancel);
        final Button done = (Button) dialog.findViewById(R.id.Done);
        final String orig_name = recipes.get(position);

        recipeName.setText(orig_name);

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

                    if(name.equals(orig_name)){
                        dialog.dismiss();
                    }

                    if(name.equals("@sort") || name.equals("@order")){
                        recipeName.setError("Sorry, that name is reserved...");
                        return;
                    }

                    for (int index = 0; index < recipes.size(); index++) {
                        if (recipes.get(index).equals(name) && index != position) {
                            recipeName.setError("Recipe already exists...");
                            return;
                        }
                    }

                    recipes.set(position, name);
                    // TODO: uncomment once implemented
                    //sortRecipe(false, currentOrder);
                    dialog.dismiss();
                    itemAdapter.notifyDataSetChanged();

                    try {
                        // Need to update other fragments before saving
                        File file = new File(dashboard.getFilesDir().getPath() + "/Aisle_Share_Data.json");
                        aisleShareData = new JSONObject(loadJSONFromAsset(file));

                        JSONObject recipeData = aisleShareData.optJSONObject("Recipes").optJSONObject(orig_name);
                        aisleShareData.optJSONObject("Recipes").remove(orig_name);
                        aisleShareData.optJSONObject("Recipes").put(name, recipeData);

                        FileOutputStream fos = new FileOutputStream(dashboard.getFilesDir().getPath() + "/Aisle_Share_Data.json");
                        fos.write(aisleShareData.toString().getBytes());
                        fos.close();
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
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
                        // todo initialize with owner and timeCreated
                        if(!recipeNames.get(i).toString().equals("@sort") && !recipeNames.get(i).toString().equals("@order")) {
                            recipes.add(recipeNames.get(i).toString());
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

    public void saveNewRecipe(String recipeTitle){
        try {
            // Need to update other fragments before saving
            File file = new File(dashboard.getFilesDir().getPath() + "/Aisle_Share_Data.json");
            aisleShareData = new JSONObject(loadJSONFromAsset(file));

            aisleShareData.optJSONObject("Recipes").accumulate(recipeTitle, new JSONObject());
            aisleShareData.optJSONObject("Recipes").optJSONObject(recipeTitle).accumulate("items", new JSONArray());
            aisleShareData.optJSONObject("Recipes").optJSONObject(recipeTitle).put("sort", 2);
            aisleShareData.optJSONObject("Recipes").optJSONObject(recipeTitle).put("direction", true);
            // todo save out owner and timeCreated info

            FileOutputStream fos = new FileOutputStream(dashboard.getFilesDir().getPath() + "/Aisle_Share_Data.json");
            fos.write(aisleShareData.toString().getBytes());
            fos.close();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public void removeRecipe(String recipeTitle){
        try {
            // Need to update other fragments before saving
            File file = new File(dashboard.getFilesDir().getPath() + "/Aisle_Share_Data.json");
            aisleShareData = new JSONObject(loadJSONFromAsset(file));

            aisleShareData.optJSONObject("Recipes").remove(recipeTitle);

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
        menuRecipes.put("sort", menu.findItem(R.id.sort_root));
        menuRecipes.put("name", menu.findItem(R.id.sort_name));
        menuRecipes.put("time", menu.findItem(R.id.sort_time));
        menuRecipes.put("owner", menu.findItem(R.id.sort_owner));
        menuRecipes.put("unsorted", menu.findItem(R.id.unsorted));
        menuRecipes.put("delete", menu.findItem(R.id.delete_items));

        menuRecipes.get("name").setCheckable(true);
        menuRecipes.get("time").setCheckable(true);
        menuRecipes.get("owner").setCheckable(true);
        menuRecipes.get("unsorted").setVisible(false);
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
                //sortRecipe(true, 0);
                //clearMenuCheckables();
                //option.setChecked(true);
                break;
            case R.id.sort_time:
                //sortRecipe(true, 1);
                //clearMenuCheckables();
                //option.setChecked(true);
                break;
            case R.id.sort_owner:
                //sortRecipe(true, 2);
                //clearMenuCheckables();
                //option.setChecked(true);
                break;
            case R.id.unsorted:
                //sortRecipe(false, -1);
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
        menuRecipes.get("name").setChecked(false);
        menuRecipes.get("time").setChecked(false);
        menuRecipes.get("owner").setChecked(false);
    }

    public AlertDialog confirmDeletion(final String recipeName, final int position)
    {
        return new AlertDialog.Builder(dashboard)
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure? This cannot be undone.")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        emptyNotice = (TextView) getView().findViewById(R.id.empty_notice);

                        removeRecipe(recipeName);
                        recipes.remove(position);
                        itemAdapter.notifyDataSetChanged();
                        dialog.dismiss();
                        Toast toast = Toast.makeText(dashboard, "Recipe Deleted", Toast.LENGTH_LONG);
                        toast.show();
                        if (recipes.size() == 0) {
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

        final ArrayList<String> recipeNames = new ArrayList<>();
        if(recipes.size() != 0) {
            dialog.setTitle("What Should We Delete?");
            for (String i : recipes) {
                recipeNames.add(i);
            }
        }
        else{
            dialog.setTitle("No Recipes to Delete.");
        }

        ArrayAdapter<String> itemAdapter = new ArrayAdapter<>(dashboard,android.R.layout.simple_list_item_1, recipeNames);
        lv.setAdapter(itemAdapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                confirmDeletion(recipeNames.get(position), position).show();
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

        //Long Click for editing
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {
                editRecipeDialog(pos);
                return true;
            }
        });
    }
}
