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
    /*public CustomAdapter(Context context, Item[] resource) {
        super(context,R.layout.row,resource);
        this.context = context;
        this.items = resource;
    }*/
    public CustomAdapter(Context context, ArrayList resource) {
        super(context,R.layout.row,resource);
        this.context = context;
        this.items = resource;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        convertView = inflater.inflate(R.layout.row, parent, false);
        TextView name = (TextView) convertView.findViewById(R.id.textView);
        CheckBox cb = (CheckBox) convertView.findViewById(R.id.checkBox);
        name.setText(items.get(position).getName());
        name.setId(position);
        cb.setId(position);
        if(items.get(position).getValue() == 1) {
            cb.setChecked(true);
            name.setPaintFlags(name.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        else {
            cb.setChecked(false);
        }
        return convertView;
    }
    @Override
    public void notifyDataSetChanged() {
        ArrayList<Item> uncheckedItems = new ArrayList<>();
        ArrayList<Item> checkedItems = new ArrayList<>();
        System.out.println("Start");
        for(int x = 0; x < items.size(); x++){
            if(items.get(x).getValue() == 0){
                uncheckedItems.add(items.get(x));
                System.out.println("Unchecked");
            }
            else{
                checkedItems.add(items.get(x));
                System.out.println("Checked");
            }
        }
        uncheckedItems.addAll(checkedItems);
        items.clear();
        items.addAll(uncheckedItems);

        super.notifyDataSetChanged();
    }
}

