package com.example.mediapresentation;

import com.google.gson.annotations.SerializedName; // <--- FUCKING IMPORT THIS

public class ArticleItem {

    // Use SerializedName to map the JSON key to your Java variable
    @SerializedName("id") // Change "id" to whatever the backend actually sends (e.g. "articleId")
    public int id;

    @SerializedName("name") // Change "name" to whatever backend sends (e.g. "naziv")
    public String name;

    @SerializedName("price") // Change "price" to whatever backend sends (e.g. "cijena")
    public Float price;

    public ArticleItem() { }

    public ArticleItem(int id, String name, Float price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    // "F-ing" helper to print it out for debugging
    @Override
    public String toString() {
        return "ID: " + id + ", Name: " + name + ", Price: " + price;
    }
}
