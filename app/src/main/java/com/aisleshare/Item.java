package com.aisleshare;

public class Item {
    private String owner;
    private String name;
    private String type;
    private double quantity;
    private String units;
    private boolean checked;
    private long created;
    private boolean isItem;

    Item(String owner, String name){
        this.owner = owner;
        this.name = name;
        this.type = "";
        this.quantity = 1;
        this.units = "";
        this.checked = false;
        this.created = System.currentTimeMillis()/1000;
        this.isItem = true;
    }
    Item(String owner, String name, String type){
        this.owner = owner;
        this.name = name;
        this.type = type;
        this.quantity = 1;
        this.units = "";
        this.checked = false;
        this.created = System.currentTimeMillis()/1000;
        this.isItem = true;
    }
    Item(String owner, String name, boolean item){
        this.owner = owner;
        this.name = name;
        this.type = "";
        this.quantity = 1;
        this.units = "";
        this.checked = false;
        this.created = System.currentTimeMillis()/1000;
        this.isItem = item;
    }
    Item(String owner, String name, String type, double quantity){
        this.owner = owner;
        this.name = name;
        this.type = type;
        this.quantity = quantity;
        this.units = "";
        this.checked = false;
        this.created = System.currentTimeMillis()/1000;
        this.isItem = true;
    }
    Item(String owner, String name, String type, double quantity, String units){
        this.owner = owner;
        this.name = name;
        this.type = type;
        this.quantity = quantity;
        this.units = units;
        this.checked = false;
        this.created = System.currentTimeMillis()/1000;
        this.isItem = true;
    }
    Item(String owner, String name, String type, double quantity, String units, boolean value){
        this.owner = owner;
        this.name = name;
        this.type = type;
        this.quantity = quantity;
        this.units = units;
        this.checked = value;
        this.created = System.currentTimeMillis()/1000;
        this.isItem = true;
    }
    Item(String owner, String name, String type, double quantity, String units, boolean value, long created){
        this.owner = owner;
        this.name = name;
        this.type = type;
        this.quantity = quantity;
        this.units = units;
        this.checked = value;
        this.created = created;
        this.isItem = true;
    }



    // Accessors
    public boolean isItem() { return this.isItem; }
    public String getOwner(){ return this.owner; }
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
    public long getCreated(){
        return this.created;
    }
    public boolean getChecked(){
        return this.checked;
    }
    public String getJSONString() {
        String jsonString = "";

        jsonString += "{\"owner\":" + checkNull(owner);
        jsonString += ",\"name\":" + checkNull(name);
        jsonString += ",\"quantity\":" + this.getQuantity();
        jsonString += ",\"units\":" + checkNull(units);
        jsonString += ",\"type\":" + checkNull(type);
        jsonString += ",\"timeCreated\":" + this.getCreated();
        jsonString += ",\"checked\":" + this.getChecked();
        jsonString += ",\"isItem\":" + this.isItem() + "}";

        return jsonString;
    }
    public String checkNull(String s){
        if(!s.equals("")){
            return "\"" + s + "\"";
        }
        else{
            return "\"\"";
        }
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
    public void toggleChecked(){
        checked = !checked;
    }
    public void setChecked(boolean value){ checked = value; }
    public void setIsItem(boolean value) { isItem = value; }
}