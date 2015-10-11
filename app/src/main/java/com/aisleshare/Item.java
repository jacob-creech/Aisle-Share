package com.aisleshare;

public class Item {
    private String name;
    private String type;
    private int quantity;
    private int created;
    private int checked; /* 0 : checkbox disable, 1 : checkbox enable */

    Item(String name){
        this.name = name;
        this.type = "";
        this.quantity = 1;
        this.checked = 0;
    }
    Item(String name, String type){
        this.name = name;
        this.type = type;
        this.quantity = 1;
        this.checked = 0;
    }
    Item(String name, String type, int quantity){
        this.name = name;
        this.type = type;
        this.quantity = quantity;
        this.checked = 0;
    }
    Item(String name, String type, int quantity, int created){
        this.name = name;
        this.type = type;
        this.quantity = quantity;
        this.created = created;
        this.checked = 0;
    }
    Item(String name, String type, int quantity, int created, int value){
        this.name = name;
        this.type = type;
        this.quantity = quantity;
        this.created = created;
        this.checked = value;
    }

    // Accessors
    public String getName(){
        return this.name;
    }
    public String getType(){
        return this.type;
    }
    public int getQuantity(){
        return this.quantity;
    }
    public int getCreated(){
        return this.created;
    }
    public int getChecked(){
        return this.checked;
    }

    // Modifiers
    public void setQuantity(int q){
        this.quantity = q;
    }
    public void setChecked(int v){
        this.checked = v;
    }
}