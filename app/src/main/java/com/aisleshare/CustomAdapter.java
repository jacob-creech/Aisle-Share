package com.aisleshare;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;

public class CustomAdapter extends ArrayAdapter{
    ArrayList<Item> items = null;
    Context context;
    public CustomAdapter(Context context, ArrayList<Item> resource) {
        super(context,R.layout.row,resource);
        this.context = context;
        this.items = resource;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        convertView = inflater.inflate(R.layout.row, parent, false);

        FrameLayout row = (FrameLayout) convertView.findViewById(R.id.row);
        TextView name = (TextView) convertView.findViewById(R.id.name);
        TextView type = (TextView) convertView.findViewById(R.id.type);
        TextView quantity = (TextView) convertView.findViewById(R.id.quantity);
        TextView units = (TextView) convertView.findViewById(R.id.units);
        CheckBox cb = (CheckBox) convertView.findViewById(R.id.checkBox);

        boolean hasType = items.get(position).getType().equals("");
        boolean hasUnits = items.get(position).getUnits().equals("");
        double quantityVal = items.get(position).getQuantity();

        // Move Checked Items to the Bottom
        ItemComparator compare = new ItemComparator();
        ItemComparator.Checked sorter = compare.new Checked();
        Collections.sort(items, sorter);

        // Frame
        row.setId(position);

        // Name
        name.setText(items.get(position).getName());
        name.setId(position);

        // Type
        if (!hasType) {
            type.setText(items.get(position).getType());
            type.setId(position);
        }
        else{
            type.setVisibility(View.GONE);
        }

        // Quantity
        Double value = items.get(position).getQuantity();
        if(quantityVal > 0 && (quantityVal != 1 || !hasUnits)){
            if(value % 1 == 0) {
                quantity.setText(Integer.toString((int) Math.round(value)));
            }
            else{
                quantity.setText(Double.toString(value));
            }
        }
        else{
            quantity.setText("");
        }
        quantity.setId(position);

        // Units
        if (!hasUnits) {
            units.setText(items.get(position).getUnits());
            units.setId(position);
        }
        else{
            units.setVisibility(View.GONE);
        }

        // Checked
        if(items.get(position).getChecked()) {
            cb.setChecked(true);
            name.setPaintFlags(name.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        else {
            cb.setChecked(false);
        }
        cb.setId(position);


        return convertView;
    }
}

