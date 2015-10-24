package com.aisleshare;

/**
 * Created by Jason on 10/23/2015.
 */
public class ListItem {
    private String owner;
    private String name;
    private int created;

    ListItem(String owner, String name){
        this.owner = owner;
        this.name = name;
        this.created = 0;
    }
    ListItem(String owner, String name, int created){
        this.owner = owner;
        this.name = name;
        this.created = created;
    }

    public String getOwner(){ return this.owner; }
    public String getName(){ return this.name; }
    public int getCreated(){ return this.created; }

    public void setName(String name){
        this.name = name;
    }
}
