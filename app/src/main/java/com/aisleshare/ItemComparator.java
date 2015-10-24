package com.aisleshare;

import android.content.Context;
import android.provider.Settings;

import java.util.Comparator;

public  class ItemComparator{
    private String deviceName;

    public ItemComparator(Context context) {
        deviceName = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public class Owner implements Comparator<Item>{
        public int compare(Item left, Item right) {
            boolean leftOwner = left.getOwner().equals(deviceName);
            if(left.getOwner().equals(right.getOwner())){
                return 0;
            }
            else if(leftOwner){
                return -1;
            }
            else{
                return 1;
            }
        }
    }
    public class Name implements Comparator<Item>{
        public int compare(Item left, Item right) {
            return left.getName().toLowerCase().compareTo(right.getName().toLowerCase());
        }
    }
    public class Type implements Comparator<Item>{
        public int compare(Item left, Item right) {
            return left.getType().toLowerCase().compareTo(right.getType().toLowerCase());
        }
    }
    public class Quantity implements Comparator<Item>{
        public int compare(Item left, Item right) {
            double difference = left.getQuantity() - right.getQuantity();
            if(difference > 0){
                return 1;
            }
            else if(difference == 0){
                return 0;
            }
            else{
                return -1;
            }
        }
    }
    public class Created implements Comparator<Item>{
        public int compare(Item left, Item right) {
            double difference = left.getCreated() - right.getCreated();
            if(difference > 0){
                return 1;
            }
            else if(difference == 0){
                return 0;
            }
            else{
                return -1;
            }
        }
    }
    public class Checked implements Comparator<Item>{
        public int compare(Item left, Item right) {
            int leftInt, rightInt;
            if(left.getChecked()) leftInt = 1;
            else leftInt = 0;
            if(right.getChecked()) rightInt = 1;
            else rightInt = 0;
            return leftInt - rightInt;
        }
    }
}


