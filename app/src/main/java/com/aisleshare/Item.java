package com.aisleshare;

public class Item {
    private String name;
    private int value; /* 0 : checkbox disable, 1 : checkbox enable */

    Item(String name){
        this.name = name;
        this.value = 0;
    }
    Item(String name, int value){
        this.name = name;
        this.value = value;
    }
    public String getName(){
        return this.name;
    }
    public int getValue(){
        return this.value;
    }
    public void setValue(int v){
        this.value = v;
    }
}