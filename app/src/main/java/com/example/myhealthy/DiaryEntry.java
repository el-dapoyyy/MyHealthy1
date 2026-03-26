package com.example.myhealthy;

public class DiaryEntry {
    public String documentId;
    public String foodName;
    public int calories;
    public String mealType;
    public String date;

    public DiaryEntry() {} // Firestore requires empty constructor

    public DiaryEntry(String foodName, int calories, String mealType, String date) {
        this.foodName = foodName;
        this.calories = calories;
        this.mealType = mealType;
        this.date = date;
    }
}
