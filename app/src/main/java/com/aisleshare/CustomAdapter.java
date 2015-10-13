package com.aisleshare;
import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter{
    ArrayList<Item> items = null;
    Context context;
    public CustomAdapter(Context context, ArrayList resource) {
        super(context,R.layout.row,resource);
        this.context = context;
        this.items = resource;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        convertView = inflater.inflate(R.layout.row, parent, false);

        TextView name = (TextView) convertView.findViewById(R.id.name);
        TextView type = (TextView) convertView.findViewById(R.id.type);
        CheckBox cb = (CheckBox) convertView.findViewById(R.id.checkBox);

        // Name
        if(items.get(position).getQuantity() > 1){
            name.setText(items.get(position).getName() +
                    " (" + items.get(position).getQuantity() + ")");
        }
        else{
            name.setText(items.get(position).getName());
        }
        name.setId(position);


        // Type
        if (!items.get(position).getType().equals("")) {
            type.setText(items.get(position).getType());
            type.setId(position);
        }
        else{
            type.setVisibility(View.GONE);
        }

        // Checked
        if(items.get(position).getChecked() == 1) {
            cb.setChecked(true);
            name.setPaintFlags(name.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        else {
            cb.setChecked(false);
        }
        cb.setId(position);


        return convertView;
    }
    @Override
    public void notifyDataSetChanged() {
        // Move checked items to the bottom of the list
        ArrayList<Item> uncheckedItems = new ArrayList<>();
        ArrayList<Item> checkedItems = new ArrayList<>();
        for(int x = 0; x < items.size(); x++){
            if(items.get(x).getChecked() == 0){
                uncheckedItems.add(items.get(x));
            }
            else{
                checkedItems.add(items.get(x));
            }
        }
        items.clear();
        items.addAll(uncheckedItems);
        items.addAll(checkedItems);

        super.notifyDataSetChanged();
    }
}

