package com.aisleshare;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter{
    ArrayList<Item> modelItems = null;
    Context context;
    /*public CustomAdapter(Context context, Item[] resource) {
        super(context,R.layout.row,resource);
        this.context = context;
        this.modelItems = resource;
    }*/
    public CustomAdapter(Context context, ArrayList resource) {
        super(context,R.layout.row,resource);
        this.context = context;
        this.modelItems = resource;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        convertView = inflater.inflate(R.layout.row, parent, false);
        TextView name = (TextView) convertView.findViewById(R.id.textView);
        CheckBox cb = (CheckBox) convertView.findViewById(R.id.checkBox);
        name.setText(modelItems.get(position).getName());
        if(modelItems.get(position).getValue() == 1) {
            cb.setChecked(true);
        }
        else {
            cb.setChecked(false);
        }
        return convertView;
    }
}

