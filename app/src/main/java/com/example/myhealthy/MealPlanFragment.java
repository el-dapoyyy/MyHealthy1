package com.example.myhealthy;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class MealPlanFragment extends Fragment {

    // Simple data class for meal items
    private static class MealItem {
        String name;
        String portion;
        int calories;

        MealItem(String name, String portion, int calories) {
            this.name = name;
            this.portion = portion;
            this.calories = calories;
        }
    }

    private final MealItem[] sarapanItems = {
            new MealItem("Oatmeal + Pisang", "40g oat + 1 pisang", 280),
            new MealItem("Roti Gandum + Telur Rebus", "2 lembar + 2 telur", 350),
            new MealItem("Smoothie Bayam & Buah", "1 gelas", 200),
    };

    private final MealItem[] siangItems = {
            new MealItem("Dada Ayam Panggang + Nasi Merah", "100g ayam + 150g nasi", 420),
            new MealItem("Ikan Tuna Kukus + Sayur", "100g tuna + sayur", 300),
            new MealItem("Tempe Bakar + Nasi + Brokoli", "100g tempe + nasi + brokoli", 380),
    };

    private final MealItem[] malamItems = {
            new MealItem("Sup Ayam + Tahu", "1 mangkok", 250),
            new MealItem("Salad Sayuran + Telur", "1 porsi besar", 220),
            new MealItem("Ikan Panggang + Sayur Kukus", "100g ikan + sayur", 280),
    };

    private final MealItem[] snackItems = {
            new MealItem("Buah Apel", "1 buah", 95),
            new MealItem("Yogurt Rendah Lemak", "150g", 100),
            new MealItem("Kacang Almond", "30g (sekitar 23 biji)", 170),
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_meal_plan, container, false);

        LinearLayout contSarapan = view.findViewById(R.id.containerSarapan);
        LinearLayout contSiang = view.findViewById(R.id.containerSiang);
        LinearLayout contMalam = view.findViewById(R.id.containerMalam);
        LinearLayout contSnack = view.findViewById(R.id.containerSnack);

        populateSection(contSarapan, sarapanItems);
        populateSection(contSiang, siangItems);
        populateSection(contMalam, malamItems);
        populateSection(contSnack, snackItems);

        return view;
    }

    private void populateSection(LinearLayout container, MealItem[] items) {
        for (MealItem item : items) {
            LinearLayout card = new LinearLayout(requireContext());
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setBackgroundResource(R.drawable.bg_card_white_rounded);
            card.setPadding(dp(14), dp(12), dp(14), dp(12));
            card.setGravity(Gravity.CENTER_VERTICAL);

            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            cardParams.bottomMargin = dp(8);
            card.setLayoutParams(cardParams);
            card.setElevation(dp(2));

            // Left: name + portion
            LinearLayout left = new LinearLayout(requireContext());
            left.setOrientation(LinearLayout.VERTICAL);
            left.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            TextView tvName = new TextView(requireContext());
            tvName.setText(item.name);
            tvName.setTextSize(14);
            tvName.setTextColor(0xFF222222);
            tvName.setTypeface(null, android.graphics.Typeface.BOLD);
            left.addView(tvName);

            TextView tvPortion = new TextView(requireContext());
            tvPortion.setText(item.portion);
            tvPortion.setTextSize(12);
            tvPortion.setTextColor(0xFF999999);
            left.addView(tvPortion);

            card.addView(left);

            // Right: calories
            TextView tvCal = new TextView(requireContext());
            tvCal.setText(item.calories + " kkal");
            tvCal.setTextSize(14);
            tvCal.setTextColor(0xFF008B02);
            tvCal.setTypeface(null, android.graphics.Typeface.BOLD);
            card.addView(tvCal);

            container.addView(card);
        }
    }

    private int dp(int value) {
        return (int) (value * requireContext().getResources().getDisplayMetrics().density);
    }
}
