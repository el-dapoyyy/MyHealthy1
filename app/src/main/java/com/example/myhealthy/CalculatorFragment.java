package com.example.myhealthy;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Locale;

public class CalculatorFragment extends Fragment {

    private static final String PREF_NAME = "myhealthy_calorie_calc";
    private static final String K_GENDER_POS = "gender_pos";
    private static final String K_AGE = "age";
    private static final String K_HEIGHT = "height";
    private static final String K_WEIGHT = "weight";
    private static final String K_ACTIVITY_POS = "activity_pos";
    private static final String K_GOAL_POS = "goal_pos";

    private EditText etAge, etHeight, etWeight;
    private Spinner spinnerGender, spinnerActivity, spinnerGoal;
    private TextView tvBmiResult, tvCalorieResult, tvResultDesc;
    private TextView tvMacroProtein, tvMacroCarb, tvMacroFat;
    private View barProtein, barCarb, barFat;
    private android.widget.SeekBar seekBarHeight;
    private TextView tvLastUpdated;

    private enum Goal { MAINTAIN, CUT, BULK }

    private static final String K_LAST_UPDATED = "last_updated";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calculator, container, false);

        etAge = view.findViewById(R.id.etAge);
        etHeight = view.findViewById(R.id.etHeight);
        etWeight = view.findViewById(R.id.etWeight);
        spinnerGender = view.findViewById(R.id.spinnerGender);
        spinnerActivity = view.findViewById(R.id.spinnerActivity);
        spinnerGoal = view.findViewById(R.id.spinnerGoal);
        tvBmiResult = view.findViewById(R.id.tvBmiResult);
        tvCalorieResult = view.findViewById(R.id.tvCalorieResult);
        tvResultDesc = view.findViewById(R.id.tvResultDesc);
        tvMacroProtein = view.findViewById(R.id.tvMacroProtein);
        tvMacroCarb = view.findViewById(R.id.tvMacroCarb);
        tvMacroFat = view.findViewById(R.id.tvMacroFat);
        
        barProtein = view.findViewById(R.id.barProtein);
        barCarb = view.findViewById(R.id.barCarb);
        barFat = view.findViewById(R.id.barFat);
        
        seekBarHeight = view.findViewById(R.id.seekBarHeight);
        tvLastUpdated = view.findViewById(R.id.tvLastUpdated);
        
        Button btnCalculate = view.findViewById(R.id.btnCalculate);

        setupSpinners();
        setupHeightSlider();
        loadSavedData();

        btnCalculate.setOnClickListener(v -> handleCalculate());
        return view;
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.gender_labels, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        ArrayAdapter<CharSequence> activityAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.activity_levels_labels, android.R.layout.simple_spinner_item);
        activityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerActivity.setAdapter(activityAdapter);

        ArrayAdapter<CharSequence> goalAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.goal_labels, android.R.layout.simple_spinner_item);
        goalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGoal.setAdapter(goalAdapter);
    }

    private void setupHeightSlider() {
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
            Toast.makeText(requireContext(), "Harap isi semua kolom!", Toast.LENGTH_SHORT).show();
            return;
        }

        int age = Integer.parseInt(sAge);
        double height = Double.parseDouble(sHeight);
        double weight = Double.parseDouble(sWeight);

        double heightInMeter = height / 100.0;
        double bmi = weight / (heightInMeter * heightInMeter);

        String bmiCategory;
        if (bmi < 18.5) bmiCategory = "Kurus";
        else if (bmi < 24.9) bmiCategory = "Normal";
        else if (bmi < 29.9) bmiCategory = "Gemuk";
        else bmiCategory = "Obesitas";

        if (tvBmiResult != null) {
            tvBmiResult.setText(String.format(Locale.US, "BMI: %.1f (%s)", bmi, bmiCategory));
        }

        boolean isMale = (spinnerGender.getSelectedItemPosition() == 0);

        double bmr;
        if (isMale) {
            bmr = (10.0 * weight) + (6.25 * height) - (5.0 * age) + 5.0;
        } else {
            bmr = (10.0 * weight) + (6.25 * height) - (5.0 * age) - 161.0;
        }

        String[] activityValues = getResources().getStringArray(R.array.activity_levels_values);
        double activityFactor = Double.parseDouble(activityValues[spinnerActivity.getSelectedItemPosition()]);
        double tdee = bmr * activityFactor;

        String[] goalValues = getResources().getStringArray(R.array.goal_values);
        String selectedGoalKey = goalValues[spinnerGoal.getSelectedItemPosition()];
        Goal goal = mapGoal(selectedGoalKey);

        double targetCalories = applyGoalAdjustment(tdee, goal, isMale);

        tvCalorieResult.setText(String.format(Locale.US, "%.0f kkal", targetCalories));
        tvResultDesc.setText("Target harian Anda untuk " + spinnerGoal.getSelectedItem().toString().toLowerCase());

        double pPerKg = (goal == Goal.CUT) ? 1.8 : 1.6;
        double fPerKg = (goal == Goal.CUT) ? 0.8 : (goal == Goal.BULK ? 1.0 : 0.9);
        double proteinG = weight * pPerKg;
        double fatG = weight * fPerKg;
        double carbG = Math.max(0, (targetCalories - proteinG * 4 - fatG * 9) / 4);

        tvMacroProtein.setText(String.format(Locale.US, "Protein: %.1f g", proteinG));
        tvMacroCarb.setText(String.format(Locale.US, "Karbo: %.1f g", carbG));
        tvMacroFat.setText(String.format(Locale.US, "Lemak: %.1f g", fatG));
        
        updateMacroBars(proteinG, carbG, fatG);

        long timestamp = System.currentTimeMillis();
        saveData(age, height, weight, timestamp);
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
    
    private void setBarWidth(View bar, int widthPx) {
        if (bar != null) {
            ViewGroup.LayoutParams params = bar.getLayoutParams();
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

    private void saveData(int age, double height, double weight, long timestamp) {
        SharedPreferences pref = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit()
                .putString(K_GENDER_POS, String.valueOf(spinnerGender.getSelectedItemPosition()))
                .putString(K_AGE, String.valueOf(age))
                .putString(K_HEIGHT, String.valueOf(height))
                .putString(K_WEIGHT, String.valueOf(weight))
                .putString(K_ACTIVITY_POS, String.valueOf(spinnerActivity.getSelectedItemPosition()))
                .putString(K_GOAL_POS, String.valueOf(spinnerGoal.getSelectedItemPosition()))
                .putLong(K_LAST_UPDATED, timestamp)
                .apply();
    }

    private void loadSavedData() {
        SharedPreferences pref = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
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
            seekBarHeight.setProgress((int)height);
        }
        if (weight > 0) etWeight.setText(String.valueOf(weight));
        spinnerActivity.setSelection(activityPos);
        spinnerGoal.setSelection(goalPos);
        
        if (lastUpdated > 0) {
            updateLastUpdatedText(lastUpdated);
            // Optional: Recompute macros if data exists to refill bars
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
            
            double pPerKg = (goal == Goal.CUT) ? 1.8 : 1.6;
            double fPerKg = (goal == Goal.CUT) ? 0.8 : (goal == Goal.BULK ? 1.0 : 0.9);
            double proteinG = weight * pPerKg;
            double fatG = weight * fPerKg;
            double carbG = Math.max(0, (targetCalories - proteinG * 4 - fatG * 9) / 4);
            
            updateMacroBars(proteinG, carbG, fatG);
        }
    }

    private int getIntSafely(SharedPreferences pref, String key, int defaultValue) {
        try {
            String value = pref.getString(key, null);
            return value != null ? Integer.parseInt(value) : pref.getInt(key, defaultValue);
        } catch (Exception e) { return defaultValue; }
    }

    private double getDoubleSafely(SharedPreferences pref, String key, double defaultValue) {
        try {
            String value = pref.getString(key, null);
            return value != null ? Double.parseDouble(value) : pref.getFloat(key, (float) defaultValue);
        } catch (Exception e) { return defaultValue; }
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
}
