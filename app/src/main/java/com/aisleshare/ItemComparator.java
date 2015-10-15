package com.aisleshare;

import java.util.Comparator;

public  class ItemComparator{
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
            return left.getQuantity() - right.getQuantity();
        }
    }
    public class Created implements Comparator<Item>{
        public int compare(Item left, Item right) {
            return left.getCreated() - right.getCreated();
        }
    }
    public class Checked implements Comparator<Item>{
        public int compare(Item left, Item right) {
            return left.getChecked() - right.getChecked();
        }
    }
}


