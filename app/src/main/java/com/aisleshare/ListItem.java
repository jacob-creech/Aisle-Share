package com.aisleshare;

public class ListItem {
    private String owner;
    private String name;
    private long created;

    ListItem(String owner, String name){
        this.owner = owner;
        this.name = name;
        this.created = System.currentTimeMillis()/1000;
    }
    ListItem(String owner, String name, long created){
        this.owner = owner;
        this.name = name;
        this.created = created;
    }

    // Accessors
    public String getOwner(){ return this.owner; }
    public String getName(){ return this.name; }
    public long getCreated(){ return this.created; }

    // Modifiers
    public void setName(String name){
        this.name = name;
    }
}
