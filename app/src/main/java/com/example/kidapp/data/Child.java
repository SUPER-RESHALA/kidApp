package com.example.kidapp.data;

public class Child {
    String code;
    String name;
    int age;
public boolean isValidAge(int age){
    if (age>0&&age<100){
        return true;
    }
    return false;
}
    public Child(String code, String name, int age) {
        this.code = code;
        this.name = name;
        this.age = age;
    }
}
