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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Recipes extends Fragment {
    private SharedPreferences sp;
    // // TODO: update RECIPE_NAME with a com.Recipes.MESSAGE variable
    public final static String RECIPE_NAME = "com.ShoppingList.MESSAGE";
    public final static String RECIPE_PREF = "RecipePreferences";
    private ListView listView;
    private ArrayList<String> recipes;
    private Set<String> recipeSet;
    private ArrayAdapter<String> itemAdapter;
    private Context dashboard;
    private TextView emptyNotice;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recipes, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        dashboard = getActivity();
        sp = dashboard.getSharedPreferences(RECIPE_PREF, Context.MODE_PRIVATE);
        listView = (ListView) getView().findViewById(R.id.recipes);
        Set<String> defSet = new HashSet<>();
        recipeSet = sp.getStringSet("RecipeSets", defSet);
        recipes = new ArrayList<>(recipeSet);
        emptyNotice = (TextView) getView().findViewById(R.id.empty_notice);

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

                    for(int index = 0; index < recipes.size(); index++){
                        if(recipes.get(index).equals(name)){
                            recipeName.setError("Recipe already exists...");
                            return;
                        }
                    }

                    dialog.dismiss();

                    Intent intent = new Intent(dashboard, CurrentRecipe.class);

                    recipeSet.add(name);
                    recipes.add(name);
                    itemAdapter.notifyDataSetChanged();
                    updateStorage();
                    emptyNotice.setVisibility(View.INVISIBLE);

                    intent.putExtra(RECIPE_NAME, name);
                    startActivity(intent);
                }
                else{
                    recipeName.setError("Name is empty...");
                }
            }
        });

        dialog.show();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        updateStorage();
    }

    @Override
    public void onStop(){
        super.onStop();
        updateStorage();
    }

    /*@Override
    public void onBackPressed(){
        super.onBackPressed();
        updateStorage();
    }*/

    @Override
    public void onPause(){
        super.onPause();
        updateStorage();
    }

    public void updateStorage(){
        SharedPreferences.Editor editor = sp.edit();
        editor.putStringSet("RecipeSets", recipeSet);
        editor.commit();
        editor.apply();

        editor.remove("RecipeSets");
        editor.apply();
        editor.putStringSet("RecipeSets", recipeSet);
        editor.apply();
    }
}
