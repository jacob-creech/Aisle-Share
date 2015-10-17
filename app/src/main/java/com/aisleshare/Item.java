package com.aisleshare;

public class Item {
    private String name;
    private String type;
    private double quantity;
    private String units;
    private int created;
    private int checked; /* 0 : checkbox disable, 1 : checkbox enable */

    Item(String name){
        this.name = name;
        this.type = "";
        this.quantity = 1;
        this.units = "";
        this.created = 0;
        this.checked = 0;
    }
    Item(String name, String type){
        this.name = name;
        this.type = type;
        this.quantity = 1;
        this.units = "";
        this.created = 0;
        this.checked = 0;
    }
    Item(String name, String type, double quantity){
        this.name = name;
        this.type = type;
        this.quantity = quantity;
        this.units = "";
        this.created = 0;
        this.checked = 0;
    }
    Item(String name, String type, double quantity, String units){
        this.name = name;
        this.type = type;
        this.quantity = quantity;
        this.units = units;
        this.created = 0;
        this.checked = 0;
    }
    Item(String name, String type, double quantity, String units, int created){
        this.name = name;
        this.type = type;
        this.quantity = quantity;
        this.units = units;
        this.created = created;
        this.checked = 0;
    }
    Item(String name, String type, double quantity, String units, int created, int value){
        this.name = name;
        this.type = type;
        this.quantity = quantity;
        this.units = units;
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
    public double getQuantity(){
        return this.quantity;
    }
    public String getUnits(){
        return this.units;
    }
    public int getCreated(){
        return this.created;
    }
    public int getChecked(){
        return this.checked;
    }

    // Modifiers
    public void setName(String name){
        this.name = name;
    }
    public void setType(String type){
        this.type = type;
    }
    public void setQuantity(double quantity){
        this.quantity = quantity;
    }
    public void setUnits(String units){
        this.units = units;
    }
    public void setChecked(int checked){
        this.checked = checked;
    }
}