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

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.view.ViewGroup;

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

    // MODIFIKASI: Menambahkan variabel untuk TextView BMI
    // MODIFIKASI: Menambahkan variabel untuk TextView BMI
    private TextView tvBmiResult;
    private TextView tvCalorieResult, tvResultDesc;

    private TextView tvMacroProtein, tvMacroCarb, tvMacroFat;
    private View barProtein, barCarb, barFat;
    private android.widget.SeekBar seekBarHeight;
    private TextView tvLastUpdated;
    private Button btnCalculate;
    private TextView btnBack;

    private enum Goal { MAINTAIN, CUT, BULK }

    private static final String K_LAST_UPDATED = "last_updated";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calorie_calculator);

        initViews();
        setupSpinners();
        setupHeightSlider();
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

        // MODIFIKASI: Menghubungkan variabel dengan ID di XML
        tvBmiResult = findViewById(R.id.tvBmiResult);

        tvCalorieResult = findViewById(R.id.tvCalorieResult);
        tvResultDesc = findViewById(R.id.tvResultDesc);
        tvMacroProtein = findViewById(R.id.tvMacroProtein);
        tvMacroCarb = findViewById(R.id.tvMacroCarb);
        tvMacroFat = findViewById(R.id.tvMacroFat);
        
        barProtein = findViewById(R.id.barProtein);
        barCarb = findViewById(R.id.barCarb);
        barFat = findViewById(R.id.barFat);
        
        seekBarHeight = findViewById(R.id.seekBarHeight);
        tvLastUpdated = findViewById(R.id.tvLastUpdated);
        
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

    private void setupHeightSlider() {
        if (seekBarHeight == null || etHeight == null) return;
        
        seekBarHeight.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    etHeight.setText(String.valueOf(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(android.widget.SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(android.widget.SeekBar seekBar) {}
        });

        etHeight.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                if (!TextUtils.isEmpty(s)) {
                    try {
                        int height = (int) Double.parseDouble(s.toString());
                        if (height >= 0 && height <= 250) {
                            seekBarHeight.setProgress(height);
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        });
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

        // === MODIFIKASI MULAI: Logika Hitung BMI ===
        // Rumus BMI: Berat (kg) / (Tinggi (m) * Tinggi (m))
        double heightInMeter = height / 100.0;
        double bmi = weight / (heightInMeter * heightInMeter);

        // Tentukan Kategori BMI
        String bmiCategory;
        if (bmi < 18.5) {
            bmiCategory = "Kurus";
        } else if (bmi < 24.9) {
            bmiCategory = "Normal";
        } else if (bmi < 29.9) {
            bmiCategory = "Gemuk";
        } else {
            bmiCategory = "Obesitas";
        }

        // Tampilkan hasil BMI ke TextView (Pastikan ID tvBmiResult sudah ada di XML)
        if (tvBmiResult != null) {
            tvBmiResult.setText(String.format(Locale.US, "BMI: %.1f (%s)", bmi, bmiCategory));
        }
        // === MODIFIKASI SELESAI ===


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
        
        updateMacroBars(mt.proteinG, mt.carbG, mt.fatG);

        long timestamp = System.currentTimeMillis();
        saveData(age, height, weight, timestamp, targetCalories);
        updateLastUpdatedText(timestamp);
    }
    
    private void updateMacroBars(double p, double c, double f) {
        double total = p + c + f;
        if (total <= 0) return;
        
        // Asumsi max bar width = 180dp (bisa disesuaikan proporsinya)
        int maxDp = 180;
        float density = getResources().getDisplayMetrics().density;
        
        int pWidth = (int) ((p / total) * maxDp * density);
        int cWidth = (int) ((c / total) * maxDp * density);
        int fWidth = (int) ((f / total) * maxDp * density);
        
        setBarWidth(barProtein, pWidth);
        setBarWidth(barCarb, cWidth);
        setBarWidth(barFat, fWidth);
    }
    
    private void setBarWidth(android.view.View bar, int widthPx) {
        if (bar != null) {
            android.view.ViewGroup.LayoutParams params = bar.getLayoutParams();
            params.width = Math.max(widthPx, (int)(10 * getResources().getDisplayMetrics().density)); // min 10dp
            bar.setLayoutParams(params);
        }
    }
    
    private void updateLastUpdatedText(long timestamp) {
        if (tvLastUpdated != null && timestamp > 0) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
            String dateStr = sdf.format(new java.util.Date(timestamp));
            tvLastUpdated.setText("LAST CALCULATED: " + dateStr.toUpperCase());
        }
    }

    private void saveData(int age, double height, double weight, long timestamp, double targetCalories) {
        SharedPreferences pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        pref.edit()
                .putString(K_GENDER_POS, String.valueOf(spinnerGender.getSelectedItemPosition()))
                .putString(K_AGE, String.valueOf(age))
                .putString(K_HEIGHT, String.valueOf(height))
                .putString(K_WEIGHT, String.valueOf(weight))
                .putString(K_ACTIVITY_POS, String.valueOf(spinnerActivity.getSelectedItemPosition()))
                .putString(K_GOAL_POS, String.valueOf(spinnerGoal.getSelectedItemPosition()))
                .putLong(K_LAST_UPDATED, timestamp)
                .putInt("target_calories", (int) targetCalories)
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
        long lastUpdated = pref.getLong(K_LAST_UPDATED, 0);

        spinnerGender.setSelection(genderPos);
        if (age > 0) etAge.setText(String.valueOf(age));
        if (height > 0) {
            etHeight.setText(String.valueOf(height));
            if (seekBarHeight != null) seekBarHeight.setProgress((int)height);
        }
        if (weight > 0) etWeight.setText(String.valueOf(weight));
        spinnerActivity.setSelection(activityPos);
        spinnerGoal.setSelection(goalPos);
        
        if (lastUpdated > 0) {
            updateLastUpdatedText(lastUpdated);
            
            // Recompute macros to set bar width if there's saved data
            boolean isMale = (genderPos == 0);
            double bmr;
            if (isMale) {
                bmr = (10.0 * weight) + (6.25 * height) - (5.0 * age) + 5.0;
            } else {
                bmr = (10.0 * weight) + (6.25 * height) - (5.0 * age) - 161.0;
            }
            String[] activityValues = getResources().getStringArray(R.array.activity_levels_values);
            double activityFactor = Double.parseDouble(activityValues[activityPos]);
            double tdee = bmr * activityFactor;
            
            String[] goalValues = getResources().getStringArray(R.array.goal_values);
            String selectedGoalKey = goalValues[goalPos];
            Goal goal = mapGoal(selectedGoalKey);
            double targetCalories = applyGoalAdjustment(tdee, goal, isMale);
            
            MacroTarget mt = computeMacroTargets(weight, targetCalories, goal);
            updateMacroBars(mt.proteinG, mt.carbG, mt.fatG);
        }
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