package com.example.myhealthy;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArrayList<FoodItem> foodList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);

        if (recyclerView == null) {
            throw new IllegalStateException("RecyclerView dengan id @+id/recyclerView tidak ditemukan di activity_main.xml");
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        foodList = new ArrayList<>();

        foodList.add(new FoodItem(
                "Dada Ayam",
                "Dada ayam rendah lemak dan tinggi protein, cocok untuk diet.",
                R.drawable.dada_ayam,
                31, 165, 3, 0,
                "- Berat bersih 100g\n- Direbus/dikukus\n- Simpan di kulkas",
                5.0f
        ));

        foodList.add(new FoodItem(
                "Tahu",
                "Tahu merupakan sumber protein nabati yang mudah diolah.",
                R.drawable.placeholder_food_image,
                8, 76, 4, 2,
                "- Berat bersih 100g\n- Digoreng/dikukus\n- Simpan di kulkas",
                5.0f
        ));

        foodList.add(new FoodItem(
                "Tempe",
                "Tempe kaya protein dan probiotik, baik untuk pencernaan.",
                R.drawable.tempe,
                19, 193, 11, 9,
                "- Berat bersih 100g\n- Bisa dipanggang/rebus\n- Simpan di kulkas",
                5.0f
        ));

        foodList.add(new FoodItem(
                "Telur",
                "Telur sumber protein lengkap dan mudah disiapkan.",
                R.drawable.telur,
                13, 155, 11, 1,
                "- 1 butir besar\n- Rebus/ceplok\n- Simpan di suhu dingin",
                5.0f
        ));

        foodList.add(new FoodItem(
                "Paha Ayam",
                "Paha ayam lebih juicy, protein tetap tinggi.",
                R.drawable.paha_ayam,
                26, 209, 11, 0,
                "- Berat bersih 100g\n- Panggang/rebus\n- Simpan di kulkas",
                5.0f
        ));



        FoodAdapter adapter = new FoodAdapter(this, foodList);
        recyclerView.setAdapter(adapter);
    }
}
