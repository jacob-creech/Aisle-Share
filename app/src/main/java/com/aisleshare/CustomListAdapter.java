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
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Jason on 10/24/2015.
 */
public class CustomListAdapter extends ArrayAdapter {
    private ArrayList<ListItem> items = null;
    private Context context;
    private String deviceName;
    private int layout;

    public CustomListAdapter(Context context, ArrayList<ListItem> items, int layout) {
        super(context, layout, items);
        this.context = context;
        this.items = items;
        this.deviceName = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        this.layout = layout;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        convertView = inflater.inflate(layout, parent, false);

        TextView name = (TextView) convertView.findViewById(R.id.list_name);

        // Frame
        if(!items.get(position).getOwner().equals(deviceName)){
            //modifies item background color
            //row.setBackgroundColor(Color.parseColor("#d7d7d7"));
        }

        // Name
        name.setText(items.get(position).getName());
        name.setId(position);


        return convertView;
    }
}