package com.example.myhealthy;

public class FoodItem {

    private String name;
    private String description;
    private int imageRes;
    private int protein;
    private int calories;
    private int fat;
    private int carbs;
    private String additionalInfo;
    private float rating;

    public FoodItem(
            String name,
            String description,
            int imageRes,
            int protein,
            int calories,
            int fat,
            int carbs,
            String additionalInfo,
            float rating
    ) {
        this.name = name;
        this.description = description;
        this.imageRes = imageRes;
        this.protein = protein;
        this.calories = calories;
        this.fat = fat;
        this.carbs = carbs;
        this.additionalInfo = additionalInfo;
        this.rating = rating;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getImageRes() { return imageRes; }
    public int getProtein() { return protein; }
    public int getCalories() { return calories; }
    public int getFat() { return fat; }
    public int getCarbs() { return carbs; }
    public String getAdditionalInfo() { return additionalInfo; }
    public float getRating() { return rating; }
}
