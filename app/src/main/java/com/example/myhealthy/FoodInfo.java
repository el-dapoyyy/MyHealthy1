package com.example.myhealthy;

public class FoodInfo {
    public String name;
    public int calories;
    public double protein;
    public double carbs;
    public double fat;
    public String portion;

    public FoodInfo(String name, int calories, double protein, double carbs, double fat, String portion) {
        this.name = name;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
        this.portion = portion;
    }

    public FoodInfo(String name, int calories, double protein, double carbs, double fat) {
        this(name, calories, protein, carbs, fat, "1 porsi");
    }
}
