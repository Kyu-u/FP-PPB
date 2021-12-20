package com.example.tugascamera;

public class Meal {
    private String name, thumb, id;

    public Meal(String name, String thumb, String id){
        this.name = name;
        this.thumb = thumb;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getThumb() {
        return thumb;
    }

    public String getId() {
        return id;
    }
}
