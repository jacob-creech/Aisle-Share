package com.aisleshare;

public class Item {
    private String name;
    private String type;
    private int quantity;
    private int value; /* 0 : checkbox disable, 1 : checkbox enable */

    Item(String name){
        this.name = name;
        this.type = "";
        this.quantity = 1;
        this.value = 0;
    }
    Item(String name, String type){
        this.name = name;
        this.type = type;
        this.quantity = 1;
        this.value = 0;
    }
    Item(String name, String type, int quantity){
        this.name = name;
        this.type = type;
        this.quantity = quantity;
        this.value = 0;
    }
    Item(String name, String type, int quantity, int value){
        this.name = name;
        this.type = type;
        this.quantity = quantity;
        this.value = value;
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
    public int getValue(){
        return this.value;
    }

    // Modifiers
    public void setQuantity(int q){
        this.quantity = q;
    }
    public void setValue(int v){
        this.value = v;
    }
}