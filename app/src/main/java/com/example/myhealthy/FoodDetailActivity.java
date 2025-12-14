package com.example.myhealthy;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class FoodDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        TextView btnBack = findViewById(R.id.btnBack);
        ImageView foodImage = findViewById(R.id.foodImage);
        TextView foodName = findViewById(R.id.foodName);
        TextView foodDesc = findViewById(R.id.foodDesc);

        TextView prot = findViewById(R.id.prot);
        TextView cal = findViewById(R.id.cal);
        TextView fat = findViewById(R.id.fat);
        TextView carb = findViewById(R.id.carb);

        TextView additionalInfo = findViewById(R.id.additionalInfo);

        btnBack.setOnClickListener(v -> finish());

        foodName.setText(getIntent().getStringExtra("name"));
        foodDesc.setText(getIntent().getStringExtra("desc"));
        foodImage.setImageResource(getIntent().getIntExtra("img", 0));

        prot.setText("PROT\n" + getIntent().getIntExtra("prot", 0) + "g");
        cal.setText("CAL\n" + getIntent().getIntExtra("cal", 0));
        fat.setText("FAT\n" + getIntent().getIntExtra("fat", 0) + "g");
        carb.setText("CARBS\n" + getIntent().getIntExtra("carb", 0) + "g");

        additionalInfo.setText(getIntent().getStringExtra("info"));
    }
}
