package com.example.myhealthy;

import java.util.HashMap;
import java.util.Map;

public class FoodDatabase {

    private static final Map<String, FoodInfo> DATABASE = new HashMap<>();
    // ML Kit label → local food name mapping
    private static final Map<String, String> LABEL_MAP = new HashMap<>();

    static {
        // ═══════════════════════════════════════
        // NASI
        // ═══════════════════════════════════════
        put("Nasi Putih", 204, 4.3, 44.5, 0.4, "1 piring");
        put("Nasi Goreng", 267, 5.4, 38.0, 10.2, "1 piring");
        put("Nasi Uduk", 241, 4.0, 40.0, 7.5, "1 piring");
        put("Nasi Kuning", 248, 4.5, 42.0, 6.8, "1 piring");
        put("Lontong", 168, 3.0, 36.0, 0.4, "2 potong");
        put("Nasi Padang", 350, 12.0, 45.0, 14.0, "1 piring");
        put("Bubur Ayam", 180, 8.0, 28.0, 4.0, "1 mangkuk");

        // ═══════════════════════════════════════
        // AYAM
        // ═══════════════════════════════════════
        put("Ayam Goreng", 260, 27.0, 8.0, 14.0, "1 potong");
        put("Ayam Bakar", 220, 28.0, 3.0, 10.5, "1 potong");
        put("Sate Ayam", 225, 18.0, 8.0, 13.0, "10 tusuk");
        put("Opor Ayam", 280, 22.0, 5.0, 19.0, "1 potong");
        put("Ayam Geprek", 350, 25.0, 20.0, 18.0, "1 porsi");
        put("Ayam Penyet", 310, 24.0, 15.0, 16.0, "1 porsi");
        put("Chicken Nugget", 280, 14.0, 18.0, 17.0, "6 potong");

        // ═══════════════════════════════════════
        // DAGING
        // ═══════════════════════════════════════
        put("Rendang", 486, 22.6, 7.8, 40.6, "1 potong");
        put("Sate Kambing", 250, 20.0, 2.0, 18.0, "10 tusuk");
        put("Empal", 310, 25.0, 5.0, 21.0, "1 potong");
        put("Rawon", 235, 18.0, 8.0, 15.0, "1 mangkuk");
        put("Sop Daging", 180, 15.0, 10.0, 8.0, "1 mangkuk");
        put("Bakso", 190, 12.0, 18.0, 7.0, "1 mangkuk");

        // ═══════════════════════════════════════
        // IKAN & SEAFOOD
        // ═══════════════════════════════════════
        put("Ikan Goreng", 200, 20.0, 6.0, 10.0, "1 ekor");
        put("Ikan Bakar", 170, 22.0, 2.0, 8.0, "1 ekor");
        put("Pecel Lele", 280, 18.0, 15.0, 16.0, "1 porsi");
        put("Pempek", 195, 8.0, 28.0, 5.5, "3 buah");
        put("Udang Goreng", 220, 22.0, 10.0, 10.0, "5 ekor");
        put("Cumi Goreng", 175, 16.0, 8.0, 8.5, "1 porsi");

        // ═══════════════════════════════════════
        // TELUR
        // ═══════════════════════════════════════
        put("Telur Goreng", 185, 12.0, 1.0, 14.5, "2 butir");
        put("Telur Dadar", 190, 12.5, 2.0, 15.0, "1 lembar");
        put("Telur Rebus", 155, 13.0, 1.0, 11.0, "2 butir");
        put("Telur Balado", 210, 12.0, 5.0, 16.0, "2 butir");

        // ═══════════════════════════════════════
        // TAHU & TEMPE
        // ═══════════════════════════════════════
        put("Tempe Goreng", 150, 11.0, 7.6, 9.0, "3 potong");
        put("Tahu Goreng", 115, 8.0, 5.5, 7.0, "3 potong");
        put("Perkedel", 180, 5.0, 18.0, 10.0, "2 buah");
        put("Mendoan", 170, 9.0, 12.0, 10.0, "2 potong");
        put("Tahu Isi", 160, 7.0, 14.0, 8.5, "3 buah");

        // ═══════════════════════════════════════
        // SAYUR & SALAD
        // ═══════════════════════════════════════
        put("Gado-gado", 250, 10.0, 20.0, 15.0, "1 porsi");
        put("Sayur Asem", 60, 2.0, 12.0, 0.5, "1 mangkuk");
        put("Capcay", 120, 5.0, 10.0, 7.0, "1 porsi");
        put("Urap", 130, 4.0, 8.0, 9.0, "1 porsi");
        put("Sayur Lodeh", 110, 3.0, 10.0, 7.0, "1 mangkuk");
        put("Tumis Kangkung", 80, 3.0, 6.0, 5.0, "1 porsi");
        put("Lalapan", 25, 1.5, 4.0, 0.3, "1 porsi");
        put("Salad", 90, 2.0, 8.0, 5.5, "1 porsi");

        // ═══════════════════════════════════════
        // MIE & BAKSO
        // ═══════════════════════════════════════
        put("Mie Goreng", 380, 8.0, 52.0, 15.0, "1 piring");
        put("Mie Ayam", 320, 12.0, 45.0, 10.0, "1 mangkuk");
        put("Indomie Goreng", 380, 8.0, 52.0, 15.0, "1 bungkus");
        put("Indomie Kuah", 310, 7.0, 48.0, 10.0, "1 bungkus");
        put("Kwetiau Goreng", 350, 10.0, 48.0, 13.0, "1 piring");

        // ═══════════════════════════════════════
        // SOTO & SUP
        // ═══════════════════════════════════════
        put("Soto Ayam", 120, 9.0, 8.0, 5.5, "1 mangkuk");
        put("Soto Betawi", 280, 15.0, 10.0, 20.0, "1 mangkuk");
        put("Sup Ayam", 100, 8.0, 8.0, 4.0, "1 mangkuk");
        put("Sop Buntut", 320, 20.0, 8.0, 24.0, "1 mangkuk");

        // ═══════════════════════════════════════
        // BUAH
        // ═══════════════════════════════════════
        put("Pisang", 89, 1.1, 22.8, 0.3, "1 buah");
        put("Mangga", 60, 0.8, 15.0, 0.4, "1 buah");
        put("Jeruk", 47, 0.9, 12.0, 0.1, "1 buah");
        put("Apel", 52, 0.3, 14.0, 0.2, "1 buah");
        put("Pepaya", 43, 0.5, 11.0, 0.3, "1 potong");
        put("Semangka", 30, 0.6, 7.6, 0.2, "1 potong");
        put("Alpukat", 160, 2.0, 8.5, 14.7, "1 buah");
        put("Anggur", 69, 0.7, 18.0, 0.2, "10 buah");

        // ═══════════════════════════════════════
        // ROTI & SNACK
        // ═══════════════════════════════════════
        put("Roti Tawar", 75, 2.5, 14.0, 1.0, "1 lembar");
        put("Roti Bakar", 190, 4.0, 24.0, 8.0, "1 porsi");
        put("Gorengan", 200, 3.0, 20.0, 12.0, "3 buah");
        put("Martabak Manis", 350, 6.0, 45.0, 16.0, "1 potong");
        put("Martabak Telur", 280, 12.0, 22.0, 16.0, "1 potong");
        put("Pisang Goreng", 180, 2.0, 25.0, 8.0, "2 buah");
        put("Kue Lapis", 150, 2.0, 22.0, 6.0, "1 potong");
        put("Donat", 250, 4.0, 30.0, 12.0, "1 buah");
        put("Keripik", 150, 2.0, 15.0, 9.0, "1 bungkus kecil");

        // ═══════════════════════════════════════
        // MINUMAN
        // ═══════════════════════════════════════
        put("Teh Manis", 80, 0, 20.0, 0, "1 gelas");
        put("Kopi Susu", 120, 3.0, 15.0, 5.0, "1 gelas");
        put("Es Jeruk", 90, 0.5, 22.0, 0.1, "1 gelas");
        put("Es Campur", 200, 2.0, 40.0, 4.0, "1 mangkuk");
        put("Es Teh", 70, 0, 18.0, 0, "1 gelas");
        put("Jus Alpukat", 250, 3.0, 20.0, 18.0, "1 gelas");
        put("Air Putih", 0, 0, 0, 0, "1 gelas");
        put("Susu", 130, 7.0, 12.0, 6.0, "1 gelas");

        // ═══════════════════════════════════════
        // ML Kit Label → Indonesian Food Mapping
        // ═══════════════════════════════════════
        LABEL_MAP.put("rice", "Nasi Putih");
        LABEL_MAP.put("fried rice", "Nasi Goreng");
        LABEL_MAP.put("chicken", "Ayam Goreng");
        LABEL_MAP.put("fried chicken", "Ayam Goreng");
        LABEL_MAP.put("grilled chicken", "Ayam Bakar");
        LABEL_MAP.put("meat", "Rendang");
        LABEL_MAP.put("beef", "Rendang");
        LABEL_MAP.put("fish", "Ikan Goreng");
        LABEL_MAP.put("seafood", "Udang Goreng");
        LABEL_MAP.put("egg", "Telur Goreng");
        LABEL_MAP.put("noodle", "Mie Goreng");
        LABEL_MAP.put("noodles", "Mie Goreng");
        LABEL_MAP.put("soup", "Soto Ayam");
        LABEL_MAP.put("salad", "Salad");
        LABEL_MAP.put("vegetable", "Capcay");
        LABEL_MAP.put("fruit", "Pisang");
        LABEL_MAP.put("banana", "Pisang");
        LABEL_MAP.put("apple", "Apel");
        LABEL_MAP.put("orange", "Jeruk");
        LABEL_MAP.put("mango", "Mangga");
        LABEL_MAP.put("bread", "Roti Tawar");
        LABEL_MAP.put("sandwich", "Roti Bakar");
        LABEL_MAP.put("cake", "Kue Lapis");
        LABEL_MAP.put("donut", "Donat");
        LABEL_MAP.put("pizza", "Martabak Telur");
        LABEL_MAP.put("coffee", "Kopi Susu");
        LABEL_MAP.put("tea", "Teh Manis");
        LABEL_MAP.put("juice", "Es Jeruk");
        LABEL_MAP.put("milk", "Susu");
        LABEL_MAP.put("ice cream", "Es Campur");
        LABEL_MAP.put("snack", "Gorengan");
        LABEL_MAP.put("food", "Nasi Goreng"); // generic fallback
    }

    private static void put(String name, int cal, double p, double c, double f, String portion) {
        DATABASE.put(name.toLowerCase(), new FoodInfo(name, cal, p, c, f, portion));
    }

    /** Search by exact name (case-insensitive) */
    public static FoodInfo findByName(String name) {
        return DATABASE.get(name.toLowerCase());
    }

    /** Map ML Kit label to local food */
    public static FoodInfo findByLabel(String label) {
        String mapped = LABEL_MAP.get(label.toLowerCase());
        if (mapped != null) {
            return DATABASE.get(mapped.toLowerCase());
        }
        return null;
    }

    public static FoodInfo searchByKeyword(String keyword) {
        String lowerKey = keyword.toLowerCase();
        for (Map.Entry<String, FoodInfo> entry : DATABASE.entrySet()) {
            if (entry.getKey().contains(lowerKey) || lowerKey.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    /** Get all food names for dropdown/autocomplete */
    public static String[] getAllNames() {
        FoodInfo[] foods = DATABASE.values().toArray(new FoodInfo[0]);
        String[] names = new String[foods.length];
        for (int i = 0; i < foods.length; i++) {
            names[i] = foods[i].name;
        }
        java.util.Arrays.sort(names);
        return names;
    }
}
