package com.aisleshare;

import android.content.Context;
import android.provider.Settings;
import java.util.Comparator;

/**
 * Created by Jason on 10/23/2015.
 */
public class ListItemComparator {
    private String deviceName;

    public ListItemComparator(Context context) {
        deviceName = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public class Owner implements Comparator<ListItem>{
        public int compare(ListItem left, ListItem right) {
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
    public class Name implements Comparator<ListItem> {
        public int compare(ListItem left, ListItem right) {
            return left.getName().toLowerCase().compareTo(right.getName().toLowerCase());
        }
    }
    public class Created implements Comparator<ListItem>{
        public int compare(ListItem left, ListItem right) {
            long difference = left.getCreated() - right.getCreated();
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
}
