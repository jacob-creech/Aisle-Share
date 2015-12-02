package com.aisleshare;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.ArrayList;

public class ItemAdapter extends ArrayAdapter{
    private ArrayList<Item> items = null;
    private Context context;
    private String deviceName;
    private int original_layout;
    private int layout;

    public ItemAdapter(Context context, ArrayList<Item> items, int layout) {
        super(context,layout,items);
        this.context = context;
        this.items = items;
        this.deviceName = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        this.original_layout = layout;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Is it an Item or Header
        if(items.get(position).isItem()) {
            layout = original_layout;
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            convertView = inflater.inflate(layout, parent, false);

            RelativeLayout row = (RelativeLayout) convertView.findViewById(R.id.row);
            ImageView imported = (ImageView) convertView.findViewById(R.id.imported);
            LinearLayout primary = (LinearLayout) convertView.findViewById(R.id.primary);
            CheckBox cb = (CheckBox) convertView.findViewById(R.id.checkBox);
            LinearLayout col1 = (LinearLayout) convertView.findViewById(R.id.column1);
            TextView name = (TextView) convertView.findViewById(R.id.name);
            TextView type = (TextView) convertView.findViewById(R.id.type);
            LinearLayout col2 = (LinearLayout) convertView.findViewById(R.id.column2);
            TextView quantity = (TextView) convertView.findViewById(R.id.quantity);
            TextView units = (TextView) convertView.findViewById(R.id.units);

            boolean hasType = items.get(position).getType().equals("");
            boolean hasUnits = items.get(position).getUnits().equals("");
            double quantityVal = items.get(position).getQuantity();

            // Item is not owned; show the imported symbol
            if (!items.get(position).getOwner().equals(deviceName)) {
                if (imported != null) {
                    imported.setVisibility(View.VISIBLE);
                }

                row.setTag("Disable Swipe");
                primary.setTag("Disable Swipe");
                cb.setTag("Disable Swipe");
                col1.setTag("Disable Swipe");
                name.setTag("Disable Swipe");
                type.setTag("Disable Swipe");
                col2.setTag("Disable Swipe");
                quantity.setTag("Disable Swipe");
                units.setTag("Disable Swipe");
            }

            // Frame
            row.setId(position);

            // Primary Content
            primary.setId(position);

            // Name
            name.setText(items.get(position).getName());
            name.setId(position);

            // Type
            if (!hasType) {
                type.setText(items.get(position).getType());
                type.setId(position);
            } else {
                type.setVisibility(View.GONE);
            }

            // Quantity
            Double value = items.get(position).getQuantity();
            if (quantityVal > 0 && (quantityVal != 1 || !hasUnits)) {
                if (value % 1 == 0) {
                    quantity.setText(Integer.toString((int) Math.round(value)));
                } else {
                    quantity.setText(Double.toString(value));
                }
            } else {
                quantity.setText("");
            }
            quantity.setId(position);

            // Units
            if (!hasUnits) {
                units.setText(items.get(position).getUnits());
                units.setId(position);
            } else {
                units.setVisibility(View.GONE);
            }

            // Checked
            if (cb != null) {
                if (items.get(position).getChecked()) {
                    cb.setChecked(true);
                    name.setPaintFlags(name.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                } else {
                    cb.setChecked(false);
                }
                cb.setId(position);
            }
        }
        else {
            layout = R.layout.row_header;
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            convertView = inflater.inflate(layout, parent, false);

            RelativeLayout row = (RelativeLayout) convertView.findViewById(R.id.row);
            row.setId(position);
            row.setTag("Disable Swipe");

            TextView name = (TextView) convertView.findViewById(R.id.separator);
            name.setText(items.get(position).getName());
            name.setId(position);
            name.setTag("Disable Swipe");

            if (items.get(position).showTrash()) {
                ImageView trash = (ImageView) convertView.findViewById(R.id.trash);
                trash.setVisibility(View.VISIBLE);
                trash.setId(position);
                trash.setTag("Disable Swipe");
            }
        }

        return convertView;
    }
}