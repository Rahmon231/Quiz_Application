package com.example.myquizadmin;

public class CategoryModel {
    private String id;
    private String name;
    private int levels; //number of difficulties

    public CategoryModel(String id, String name, int levels) {
        this.id = id;
        this.name = name;
        this.levels = levels;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLevels() {
        return levels;
    }

    public void setLevels(int levels) {
        this.levels = levels;
    }
}
