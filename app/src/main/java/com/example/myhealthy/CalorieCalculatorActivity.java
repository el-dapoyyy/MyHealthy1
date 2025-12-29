package com.example.myhealthy;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class CalorieCalculatorActivity extends AppCompatActivity {

    private static final String PREF_NAME = "myhealthy_calorie_calc";
    private static final String K_GENDER_POS = "gender_pos";
    private static final String K_AGE = "age";
    private static final String K_HEIGHT = "height";
    private static final String K_WEIGHT = "weight";
    private static final String K_ACTIVITY_POS = "activity_pos";
    private static final String K_GOAL_POS = "goal_pos";

    private EditText etAge, etHeight, etWeight;
    private Spinner spinnerGender, spinnerActivity, spinnerGoal;
    private TextView tvCalorieResult, tvResultDesc;
    private TextView tvMacroProtein, tvMacroCarb, tvMacroFat;
    private Button btnCalculate;
    private TextView btnBack;

    private enum Goal { MAINTAIN, CUT, BULK }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calorie_calculator);

        initViews();
        setupSpinners();
        loadSavedData();

        btnBack.setOnClickListener(v -> finish());
        btnCalculate.setOnClickListener(v -> handleCalculate());
    }

    private void initViews() {
        etAge = findViewById(R.id.etAge);
        etHeight = findViewById(R.id.etHeight);
        etWeight = findViewById(R.id.etWeight);
        spinnerGender = findViewById(R.id.spinnerGender);
        spinnerActivity = findViewById(R.id.spinnerActivity);
        spinnerGoal = findViewById(R.id.spinnerGoal);
        tvCalorieResult = findViewById(R.id.tvCalorieResult);
        tvResultDesc = findViewById(R.id.tvResultDesc);
        tvMacroProtein = findViewById(R.id.tvMacroProtein);
        tvMacroCarb = findViewById(R.id.tvMacroCarb);
        tvMacroFat = findViewById(R.id.tvMacroFat);
        btnCalculate = findViewById(R.id.btnCalculate);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupSpinners() {
        // Setup Spinner Gender
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(this,
                R.array.gender_labels, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        // Setup Spinner Activity
        ArrayAdapter<CharSequence> activityAdapter = ArrayAdapter.createFromResource(this,
                R.array.activity_levels_labels, android.R.layout.simple_spinner_item);
        activityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerActivity.setAdapter(activityAdapter);

        // Setup Spinner Goal
        ArrayAdapter<CharSequence> goalAdapter = ArrayAdapter.createFromResource(this,
                R.array.goal_labels, android.R.layout.simple_spinner_item);
        goalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGoal.setAdapter(goalAdapter);
    }

    private void handleCalculate() {
        String sAge = etAge.getText().toString();
        String sHeight = etHeight.getText().toString();
        String sWeight = etWeight.getText().toString();

        if (TextUtils.isEmpty(sAge) || TextUtils.isEmpty(sHeight) || TextUtils.isEmpty(sWeight)) {
            Toast.makeText(this, "Harap isi semua kolom!", Toast.LENGTH_SHORT).show();
            return;
        }

        int age = Integer.parseInt(sAge);
        double height = Double.parseDouble(sHeight);
        double weight = Double.parseDouble(sWeight);

        // Cek Gender dari Spinner (0 = Laki-laki, 1 = Perempuan sesuai urutan di arrays.xml)
        boolean isMale = (spinnerGender.getSelectedItemPosition() == 0);

        // Hitung BMR (Mifflin-St Jeor)
        double bmr;
        if (isMale) {
            bmr = (10.0 * weight) + (6.25 * height) - (5.0 * age) + 5.0;
        } else {
            bmr = (10.0 * weight) + (6.25 * height) - (5.0 * age) - 161.0;
        }

        // Ambil Nilai Aktivitas
        String[] activityValues = getResources().getStringArray(R.array.activity_levels_values);
        double activityFactor = Double.parseDouble(activityValues[spinnerActivity.getSelectedItemPosition()]);
        double tdee = bmr * activityFactor;

        // Ambil Goal
        String[] goalValues = getResources().getStringArray(R.array.goal_values);
        String selectedGoalKey = goalValues[spinnerGoal.getSelectedItemPosition()];
        Goal goal = mapGoal(selectedGoalKey);

        // Hitung Target Kalori Berdasarkan Goal
        double targetCalories = applyGoalAdjustment(tdee, goal, isMale);

        // Update UI
        tvCalorieResult.setText(String.format(Locale.US, "%.0f kkal", targetCalories));
        tvResultDesc.setText("Target harian Anda untuk " + spinnerGoal.getSelectedItem().toString().toLowerCase());

        // Hitung & Update Makro
        MacroTarget mt = computeMacroTargets(weight, targetCalories, goal);
        tvMacroProtein.setText(String.format(Locale.US, "Protein: %.1f g", mt.proteinG));
        tvMacroCarb.setText(String.format(Locale.US, "Karbo: %.1f g", mt.carbG));
        tvMacroFat.setText(String.format(Locale.US, "Lemak: %.1f g", mt.fatG));

        saveData(age, height, weight);
    }

    private void saveData(int age, double height, double weight) {
        SharedPreferences pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        pref.edit()
                .putString(K_GENDER_POS, String.valueOf(spinnerGender.getSelectedItemPosition()))
                .putString(K_AGE, String.valueOf(age))
                .putString(K_HEIGHT, String.valueOf(height))
                .putString(K_WEIGHT, String.valueOf(weight))
                .putString(K_ACTIVITY_POS, String.valueOf(spinnerActivity.getSelectedItemPosition()))
                .putString(K_GOAL_POS, String.valueOf(spinnerGoal.getSelectedItemPosition()))
                .apply();
    }

    private void loadSavedData() {
        SharedPreferences pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Ambil data dengan fallback ke default jika tidak ditemukan
        int genderPos = getIntSafely(pref, K_GENDER_POS, 0);
        int age = getIntSafely(pref, K_AGE, 0);
        double height = getDoubleSafely(pref, K_HEIGHT, 0.0);
        double weight = getDoubleSafely(pref, K_WEIGHT, 0.0);
        int activityPos = getIntSafely(pref, K_ACTIVITY_POS, 0);
        int goalPos = getIntSafely(pref, K_GOAL_POS, 0);

        spinnerGender.setSelection(genderPos);
        if (age > 0) etAge.setText(String.valueOf(age));
        if (height > 0) etHeight.setText(String.valueOf(height));
        if (weight > 0) etWeight.setText(String.valueOf(weight));
        spinnerActivity.setSelection(activityPos);
        spinnerGoal.setSelection(goalPos);
    }

    // Metode bantu untuk mengambil integer dari SharedPreferences dengan penanganan kesalahan
    private int getIntSafely(SharedPreferences pref, String key, int defaultValue) {
        try {
            String value = pref.getString(key, null);
            if (value != null) {
                return Integer.parseInt(value);
            } else {
                return pref.getInt(key, defaultValue);
            }
        } catch (ClassCastException e) {
            // Jika data disimpan sebagai integer sebelumnya, ambil sebagai integer
            return pref.getInt(key, defaultValue);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // Metode bantu untuk mengambil double dari SharedPreferences dengan penanganan kesalahan
    private double getDoubleSafely(SharedPreferences pref, String key, double defaultValue) {
        try {
            String value = pref.getString(key, null);
            if (value != null) {
                return Double.parseDouble(value);
            } else {
                return pref.getFloat(key, (float) defaultValue);
            }
        } catch (ClassCastException e) {
            // Jika data disimpan sebagai float sebelumnya, ambil sebagai float
            return pref.getFloat(key, (float) defaultValue);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private Goal mapGoal(String v) {
        if ("cut".equalsIgnoreCase(v)) return Goal.CUT;
        if ("bulk".equalsIgnoreCase(v)) return Goal.BULK;
        return Goal.MAINTAIN;
    }

    private double applyGoalAdjustment(double tdee, Goal goal, boolean isMale) {
        double target = tdee;
        if (goal == Goal.CUT) target = tdee - 500.0;
        else if (goal == Goal.BULK) target = tdee + 300.0;
        double min = isMale ? 1500.0 : 1200.0;
        return Math.max(min, target);
    }

    private MacroTarget computeMacroTargets(double weightKg, double targetCalories, Goal goal) {
        double pPerKg = (goal == Goal.CUT) ? 1.8 : 1.6;
        double fPerKg = (goal == Goal.CUT) ? 0.8 : (goal == Goal.BULK ? 1.0 : 0.9);

        MacroTarget mt = new MacroTarget();
        mt.proteinG = weightKg * pPerKg;
        mt.fatG = weightKg * fPerKg;
        double pCal = mt.proteinG * 4;
        double fCal = mt.fatG * 9;
        double remainingCal = targetCalories - pCal - fCal;
        mt.carbG = Math.max(0, remainingCal / 4);
        return mt;
    }

    private static class MacroTarget {
        double proteinG, fatG, carbG;
    }
}