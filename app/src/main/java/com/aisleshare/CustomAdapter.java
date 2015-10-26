package com.aisleshare;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.provider.Settings;
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
    private ArrayList<Item> items = null;
    private Context context;
    private String deviceName;
    private int layout;

    public CustomAdapter(Context context, ArrayList<Item> items, int layout) {
        super(context,layout,items);
        this.context = context;
        this.items = items;
        this.deviceName = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        this.layout = layout;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        convertView = inflater.inflate(layout, parent, false);

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
        if(cb != null) {
            ItemComparator compare = new ItemComparator(context);
            ItemComparator.Checked sorter = compare.new Checked();
            Collections.sort(items, sorter);
        }

        // Frame
        row.setId(position);
        if(!items.get(position).getOwner().equals(deviceName)){
            //modifies item background color
            //row.setBackgroundColor(Color.parseColor("#d7d7d7"));
        }

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
        if(cb != null) {
            if (items.get(position).getChecked()) {
                cb.setChecked(true);
                name.setPaintFlags(name.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                cb.setChecked(false);
            }
            cb.setId(position);
        }

        return convertView;
    }
}

