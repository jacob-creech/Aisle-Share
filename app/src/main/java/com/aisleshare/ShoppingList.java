package com.aisleshare;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class ShoppingList extends AppCompatActivity {

    // duration that the device is discoverable
    private static final int DISCOVER_DURATION = 300;

    // our request code (must be greater than zero)
    private static final int REQUEST_BLU = 1;

    BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

    Context context;

    public ShoppingList(Context context) {
        this.context = context;
    }

    public void sendViaBlutooth(View v){
        if (btAdapter == null){
            // Device does not support blutooth
            // Inform user with an error message
            Toast.makeText(context, "Blutooth is not supported on this device", Toast.LENGTH_LONG).show();
        } else {
            enableBlutooth();
        }

    }

    public void enableBlutooth(){
        if (!btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_BLU);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == DISCOVER_DURATION && requestCode == REQUEST_BLU){
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("text/plain");
        }
    }
    
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_shopping_list, container, false);
    }
}
