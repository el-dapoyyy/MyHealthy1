package com.example.myhealthy;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DailyProgressFragment extends Fragment implements SensorEventListener {

    private static final int STEP_TARGET = 6000;
    private static final int WATER_TARGET = 8;
    private static final int REQUEST_ACTIVITY_RECOGNITION = 200;

    // Views — Phase 1
    private CircularProgressView progressSteps;
    private ProgressBar progressCalories, progressWater;
    private TextView tvStreak, tvLevel, tvXp, tvSleepHoursDesc;
    private TextView tvSteps, tvStepsTarget, tvCalories, tvWater;
    private TextView tvBmiValue, tvBmiStatus;
    private View bmiBarUnder, bmiBarHealthy, bmiBarOver, bmiBarObese;
    private LinearLayout bmiBarContainer;
    private TextView bmiNeedle;
    private ProgressBar xpProgressBar;
    private LinearLayout moodContainer;
    private TextView tvUnlockedCount;
    private TextView tvViewAllMeals;
    private LinearLayout savedNutritionContainer;

    // Views — Phase 2
    private WeeklyBarChartView weeklyChart;

    // Views — Phase 3
    private LinearLayout badgeContainer;

    // Data
    private FirebaseFirestore db;
    private String userId;
    private String todayStr;
    private DocumentReference todayRef;

    private SensorManager sensorManager;
    private Sensor stepSensor;
    private int initialSteps = -1;
    private int currentSteps = 0;

    private int waterCount = 0;
    private int calorieTarget = 2000;
    private int caloriesEaten = 0;
    private String selectedMood = "";
    private double sleepHours = 0;
    private int totalXp = 0;
    private int streak = 0;
    private boolean moodXpAwarded = false;
    private List<String> earnedBadges = new ArrayList<>();

    // Mood options
    private final String[] MOODS = {"😫", "😔", "😐", "😊", "🤩"};
    private final String[] MOOD_LABELS = {"Lelah", "Sedih", "Biasa", "Senang", "Bersemangat"};
    private final TextView[] moodButtons = new TextView[5];

    // Level system
    private static final int[] LEVEL_XP = {0, 100, 300, 600, 1000, 1500};
    private static final String[] LEVEL_NAMES = {"Pemula", "Starter", "Explorer", "Warrior", "Master", "Legend"};

    // Badge definitions
    private static final String BADGE_WATER = "water_champion";
    private static final String BADGE_WALKER = "daily_walker";
    private static final String BADGE_STREAK_3 = "streak_3";
    private static final String BADGE_STREAK_7 = "streak_7";
    private static final String BADGE_DIET_7 = "diet_master";
    private static final String BADGE_WEIGHT_3 = "weight_consistent";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_daily_progress, container, false);

        // Init Firebase
        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user != null ? user.getUid() : "anonymous";
        todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        todayRef = db.collection("users").document(userId)
                .collection("progress").document(todayStr);

        // Bind Phase 1 views
        progressSteps = view.findViewById(R.id.progressSteps);
        progressCalories = view.findViewById(R.id.progressCalories);
        progressWater = view.findViewById(R.id.progressWater);
        tvStreak = view.findViewById(R.id.tvStreak);
        tvLevel = view.findViewById(R.id.tvLevel);
        tvXp = view.findViewById(R.id.tvXp);
        xpProgressBar = view.findViewById(R.id.xpProgressBar);
        moodContainer = view.findViewById(R.id.moodContainer);
        tvBmiValue = view.findViewById(R.id.tvBmiValue);
        tvBmiStatus = view.findViewById(R.id.tvBmiStatus);
        bmiBarUnder = view.findViewById(R.id.bmiBarUnder);
        bmiBarHealthy = view.findViewById(R.id.bmiBarHealthy);
        bmiBarOver = view.findViewById(R.id.bmiBarOver);
        bmiBarObese = view.findViewById(R.id.bmiBarObese);
        bmiBarContainer = view.findViewById(R.id.bmiBarContainer);
        bmiNeedle = view.findViewById(R.id.bmiNeedle);
        tvSleepHoursDesc = view.findViewById(R.id.tvSleepHoursDesc);
        tvSteps = view.findViewById(R.id.tvSteps);
        tvStepsTarget = view.findViewById(R.id.tvStepsTarget);
        tvCalories = view.findViewById(R.id.tvCalories);
        tvWater = view.findViewById(R.id.tvWater);
        tvUnlockedCount = view.findViewById(R.id.tvUnlockedCount);
        tvViewAllMeals = view.findViewById(R.id.tvViewAllMeals);
        savedNutritionContainer = view.findViewById(R.id.savedNutritionContainer);

        // View All click listener
        tvViewAllMeals.setOnClickListener(v -> openSavedMeals());

        // Bind Phase 2 views
        weeklyChart = view.findViewById(R.id.weeklyChart);

        // Bind Phase 3 views
        badgeContainer = view.findViewById(R.id.badgeContainer);

        // Setup circular progress labels
        progressSteps.setLabel("");
        progressSteps.setUnit("");
        progressSteps.setProgressColor(0xFF00FF85);
        progressSteps.setHideText(true);
        
        // Initial setup for Body Composition Bars
        bmiBarUnder.setAlpha(0.3f);
        bmiBarHealthy.setAlpha(0.3f);
        bmiBarOver.setAlpha(0.3f);
        bmiBarObese.setAlpha(0.3f);

        // Setup mood buttons
        setupMoodButtons();

        // Quick action buttons
        view.findViewById(R.id.btnAddWater).setOnClickListener(v -> addWater());
        view.findViewById(R.id.btnUpdateWeight).setOnClickListener(v -> showWeightDialog());
        view.findViewById(R.id.btnLogSleep).setOnClickListener(v -> showSleepDialog());

        // Load calorie target from Calculator SharedPreferences
        loadCalorieTarget();

        // Setup step counter
        setupStepCounter();

        // Load today's progress from Firestore
        loadProgress();

        // Load calories eaten from diary
        loadCaloriesEaten();

        // Load recent saved meals
        loadRecentSavedMeals();

        // Phase 2: Load weekly chart data
        loadWeeklyCalories();
        loadWeightTrendInBackground(); // Just checks for weight badge

        return view;
    }

    // ═══════════════════════════════════════════════════════
    // PHASE 1: MOOD
    // ═══════════════════════════════════════════════════════

    private void setupMoodButtons() {
        moodContainer.removeAllViews();
        for (int i = 0; i < MOODS.length; i++) {
            TextView tv = new TextView(requireContext());
            tv.setText(MOODS[i]);
            tv.setTextSize(26);
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(dp(2), dp(2), dp(2), dp(2));
            tv.setClickable(true);
            tv.setFocusable(true);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dp(40), dp(40));
            params.setMarginStart(dp(6));
            params.setMarginEnd(dp(6));
            tv.setLayoutParams(params);

            final int idx = i;
            tv.setOnClickListener(v -> selectMood(idx));
            moodButtons[i] = tv;
            moodContainer.addView(tv);
        }
    }

    private void selectMood(int index) {
        selectedMood = MOODS[index];
        for (int i = 0; i < moodButtons.length; i++) {
            if (i == index) {
                moodButtons[i].setBackgroundResource(R.drawable.bg_mood_selected);
                moodButtons[i].setTextSize(28);
            } else {
                moodButtons[i].setBackgroundColor(Color.TRANSPARENT);
                moodButtons[i].setTextSize(26);
            }
        }
        Map<String, Object> update = new HashMap<>();
        update.put("mood", selectedMood);

        // Award XP only on first mood selection per day
        if (!moodXpAwarded) {
            moodXpAwarded = true;
            update.put("moodXpAwarded", true);
            todayRef.set(update, SetOptions.merge());
            addXp(5);
            Toast.makeText(requireContext(), MOOD_LABELS[index] + " — +5 XP", Toast.LENGTH_SHORT).show();
        } else {
            todayRef.set(update, SetOptions.merge());
            Toast.makeText(requireContext(), MOOD_LABELS[index], Toast.LENGTH_SHORT).show();
        }
    }

    // ═══════════════════════════════════════════════════════
    // PHASE 1: WATER INTAKE
    // ═══════════════════════════════════════════════════════

    private void addWater() {
        waterCount++;
        updateWaterUI();
        Map<String, Object> update = new HashMap<>();
        update.put("waterGlasses", waterCount);
        todayRef.set(update, SetOptions.merge());

        if (waterCount >= WATER_TARGET) {
            awardBadge(BADGE_WATER, "🥇 Pejuang Air Putih");
            addXp(20);
            Toast.makeText(requireContext(), "🎉 Target air tercapai! +20 XP", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "💧 " + waterCount + "/" + WATER_TARGET + " gelas", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateWaterUI() {
        progressWater.setProgress(waterCount);
        tvWater.setText(waterCount + "/8 gelas");
    }

    // ═══════════════════════════════════════════════════════
    // PHASE 1: WEIGHT UPDATE
    // ═══════════════════════════════════════════════════════

    private void showWeightDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("⚖️ Update Berat Badan (kg)");

        final EditText input = new EditText(requireContext());
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Contoh: 65.5");
        input.setPadding(dp(16), dp(12), dp(16), dp(12));
        builder.setView(input);

        builder.setPositiveButton("Simpan", (dialog, which) -> {
            String val = input.getText().toString().trim();
            if (!val.isEmpty()) {
                double weight = Double.parseDouble(val);
                Map<String, Object> update = new HashMap<>();
                update.put("weight", weight);
                todayRef.set(update, SetOptions.merge());
                
                // Update SharedPreferences specifically for calculator
                SharedPreferences pref = requireContext().getSharedPreferences("myhealthy_calorie_calc", Context.MODE_PRIVATE);
                pref.edit().putString("weight", String.valueOf(weight)).apply();
                
                addXp(15);
                Toast.makeText(requireContext(), "Berat " + val + " kg disimpan! +15 XP", Toast.LENGTH_SHORT).show();
                loadCalorieTarget(); // Re-calculate BMI
                loadWeightTrendInBackground();
            }
        });
        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    // ═══════════════════════════════════════════════════════
    // PHASE 1: SLEEP LOG
    // ═══════════════════════════════════════════════════════

    private void showSleepDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("😴 Berapa jam tidur tadi malam?");

        final NumberPicker picker = new NumberPicker(requireContext());
        picker.setMinValue(1);
        picker.setMaxValue(14);
        picker.setValue(7);
        String[] displayValues = new String[14];
        for (int i = 0; i < 14; i++) {
            displayValues[i] = (i + 1) + " jam";
        }
        picker.setDisplayedValues(displayValues);
        builder.setView(picker);

        builder.setPositiveButton("Simpan", (dialog, which) -> {
            sleepHours = picker.getValue();
            tvSleepHoursDesc.setText((int) sleepHours + "j 0m tadi malam");
            Map<String, Object> update = new HashMap<>();
            update.put("sleepHours", sleepHours);
            todayRef.set(update, SetOptions.merge());
            addXp(10);
            Toast.makeText(requireContext(), "Tidur " + (int) sleepHours + " jam dicatat! +10 XP", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    // ═══════════════════════════════════════════════════════
    // PHASE 1: STEP COUNTER
    // ═══════════════════════════════════════════════════════

    private void setupStepCounter() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                        REQUEST_ACTIVITY_RECOGNITION);
                return;
            }
        }
        startStepCounting();
    }

    private void startStepCounting() {
        sensorManager = (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
        } else {
            progressSteps.setProgress(0, STEP_TARGET);
            tvSteps.setText("0");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_ACTIVITY_RECOGNITION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startStepCounting();
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!isAdded() || getContext() == null) return;
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            int totalSteps = (int) event.values[0];
            if (initialSteps < 0) {
                SharedPreferences sp = requireContext().getSharedPreferences("step_prefs", Context.MODE_PRIVATE);
                String savedDate = sp.getString("step_date", "");
                if (savedDate.equals(todayStr)) {
                    initialSteps = sp.getInt("initial_steps", totalSteps);
                } else {
                    initialSteps = totalSteps;
                    sp.edit().putString("step_date", todayStr).putInt("initial_steps", initialSteps).apply();
                }
            }
            currentSteps = totalSteps - initialSteps;
            progressSteps.setProgress(currentSteps, STEP_TARGET);
            tvSteps.setText(String.format(Locale.US, "%,d", currentSteps));

            // Check badge
            if (currentSteps >= STEP_TARGET) {
                awardBadge(BADGE_WALKER, "🏃 Pejalan Kaki");
            }

            // Auto-save every 100 steps
            if (currentSteps % 100 == 0 && currentSteps > 0) {
                Map<String, Object> update = new HashMap<>();
                update.put("steps", currentSteps);
                todayRef.set(update, SetOptions.merge());
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    // ═══════════════════════════════════════════════════════
    // PHASE 1: LOAD DATA
    // ═══════════════════════════════════════════════════════

    private void loadCalorieTarget() {
        SharedPreferences pref = requireContext().getSharedPreferences("myhealthy_calorie_calc", Context.MODE_PRIVATE);
        String weightStr = pref.getString("weight", null);
        String heightStr = pref.getString("height", null);

        if (weightStr != null && heightStr != null) {
            try {
                double weight = Double.parseDouble(weightStr);
                double height = Double.parseDouble(heightStr);
                double bmi = weight / Math.pow(height / 100.0, 2);
                String cat;
                
                bmiBarUnder.setAlpha(0.2f);
                bmiBarHealthy.setAlpha(0.2f);
                bmiBarOver.setAlpha(0.2f);
                bmiBarObese.setAlpha(0.2f);
                
                if (bmi < 18.5) {
                    cat = "UNDERWEIGHT";
                    bmiBarUnder.setAlpha(1.0f);
                } else if (bmi < 24.9) {
                    cat = "NORMAL";
                    bmiBarHealthy.setAlpha(1.0f);
                } else if (bmi < 29.9) {
                    cat = "OVERWEIGHT";
                    bmiBarOver.setAlpha(1.0f);
                } else {
                    cat = "OBESE";
                    bmiBarObese.setAlpha(1.0f);
                }

                // Needle positioning logic
                bmiBarContainer.post(() -> {
                    int containerWidth = bmiBarContainer.getWidth();
                    double progress = 0;
                    if (bmi <= 18.5) {
                        progress = (bmi / 18.5) * (1.5 / 7.0);
                    } else if (bmi <= 25.0) {
                        progress = (1.5 / 7.0) + ((bmi - 18.5) / 6.5) * (1.5 / 7.0);
                    } else if (bmi <= 30.0) {
                        progress = (3.0 / 7.0) + ((bmi - 25.0) / 5.0) * (1.5 / 7.0);
                    } else {
                        progress = (4.5 / 7.0) + (Math.min(bmi - 30.0, 15) / 15.0) * (2.5 / 7.0);
                    }
                    float needleX = (float) (progress * containerWidth);
                    // Center needle on the point
                    bmiNeedle.setTranslationX(needleX - (bmiNeedle.getWidth() / 2f));
                });

                tvBmiValue.setText(String.format(Locale.US, "%.1f", bmi));
                tvBmiStatus.setText(cat);

                int genderPos = getIntPref(pref, "gender_pos", 0);
                int age = getIntPref(pref, "age", 25);
                boolean isMale = genderPos == 0;
                double bmr = isMale
                        ? (10 * weight) + (6.25 * height) - (5 * age) + 5
                        : (10 * weight) + (6.25 * height) - (5 * age) - 161;

                String[] actVals = getResources().getStringArray(R.array.activity_levels_values);
                int actPos = getIntPref(pref, "activity_pos", 0);
                double actFactor = Double.parseDouble(actVals[Math.min(actPos, actVals.length - 1)]);
                calorieTarget = (int) (bmr * actFactor);

                progressCalories.setMax(calorieTarget);
            } catch (Exception e) {
                tvBmiValue.setText("-");
                tvBmiStatus.setText("NO DATA");
            }
        }
    }

    private int getIntPref(SharedPreferences pref, String key, int def) {
        try {
            String v = pref.getString(key, null);
            return v != null ? Integer.parseInt(v) : def;
        } catch (Exception e) { return def; }
    }

    private void loadCaloriesEaten() {
        db.collection("users").document(userId)
                .collection("diary")
                .whereEqualTo("date", todayStr)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!isAdded() || getContext() == null) return;
                    caloriesEaten = 0;
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Long cal = doc.getLong("calories");
                        if (cal != null) caloriesEaten += cal.intValue();
                    }
                    progressCalories.setProgress(caloriesEaten);
                    tvCalories.setText(String.format(Locale.US, "%,d", caloriesEaten));
                });
    }

    private void loadRecentSavedMeals() {
        db.collection("users").document(userId)
                .collection("diary")
                .whereEqualTo("date", todayStr)
                .limit(3)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!isAdded() || getContext() == null) return;
                    savedNutritionContainer.removeAllViews();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String name = doc.getString("foodName");
                        Long cal = doc.getLong("calories");
                        
                        if (name != null && cal != null) {
                            addSavedMealCard(name, cal.intValue() + " KCAL", "🍽️");
                        }
                    }
                    if (querySnapshot.isEmpty()) {
                        TextView empty = new TextView(requireContext());
                        empty.setText("Belum ada makanan tersimpan hari ini.");
                        empty.setTextColor(0xFFA9B5AC);
                        empty.setPadding(0, dp(16), 0, 0);
                        savedNutritionContainer.addView(empty);
                    }
                });
    }

    private void addSavedMealCard(String title, String subtitle, String emoji) {
        // Construct card programmatically
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setBackgroundResource(R.drawable.bg_progress_card_dark);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(12);
        card.setLayoutParams(params);

        TextView tvEmoji = new TextView(requireContext());
        tvEmoji.setLayoutParams(new LinearLayout.LayoutParams(dp(60), dp(60)));
        tvEmoji.setBackgroundResource(R.drawable.bg_badge_icon);
        tvEmoji.setGravity(Gravity.CENTER);
        tvEmoji.setText(emoji);
        tvEmoji.setTextSize(32);
        
        LinearLayout textCol = new LinearLayout(requireContext());
        textCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        textParams.setMarginStart(dp(16));
        textCol.setLayoutParams(textParams);

        TextView tvTitle = new TextView(requireContext());
        tvTitle.setText(title);
        tvTitle.setTextColor(Color.WHITE);
        tvTitle.setTextSize(14);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        
        TextView tvSub = new TextView(requireContext());
        tvSub.setText(subtitle);
        tvSub.setTextColor(0xFFA9B5AC);
        tvSub.setTextSize(11);
        tvSub.setPadding(0, dp(2), 0, 0);
        
        textCol.addView(tvTitle);
        textCol.addView(tvSub);
        
        card.addView(tvEmoji);
        card.addView(textCol);
        
        savedNutritionContainer.addView(card);
    }
    
    private void openSavedMeals() {
        if (getActivity() instanceof MainNavActivity) {
            MainNavActivity act = (MainNavActivity) getActivity();
            act.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new FoodDiaryFragment(), "food_diary")
                    .commitAllowingStateLoss();
            com.google.android.material.bottomnavigation.BottomNavigationView bnv = act.findViewById(R.id.bottom_nav);
            if (bnv != null) bnv.setSelectedItemId(R.id.nav_dummy);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadProgress() {
        todayRef.get().addOnSuccessListener(doc -> {
            if (!isAdded() || getContext() == null) return;
            if (doc.exists()) {
                // Water
                Long w = doc.getLong("waterGlasses");
                waterCount = w != null ? w.intValue() : 0;
                updateWaterUI();

                // Mood
                String mood = doc.getString("mood");
                if (mood != null) {
                    selectedMood = mood;
                    for (int i = 0; i < MOODS.length; i++) {
                        if (MOODS[i].equals(mood)) {
                            moodButtons[i].setBackgroundResource(R.drawable.bg_mood_selected);
                            moodButtons[i].setTextSize(28);
                        }
                    }
                }

                // Sleep
                Double sl = doc.getDouble("sleepHours");
                if (sl != null) {
                    sleepHours = sl;
                    tvSleepHoursDesc.setText((int) sleepHours + "j 0m tadi malam");
                }

                // Mood XP flag
                Boolean mxp = doc.getBoolean("moodXpAwarded");
                moodXpAwarded = mxp != null && mxp;

                // XP
                Long xp = doc.getLong("xp");
                totalXp = xp != null ? xp.intValue() : 0;

                // Streak
                Long st = doc.getLong("streak");
                streak = st != null ? st.intValue() : 0;

                // Badges
                List<String> badges = (List<String>) doc.get("badges");
                if (badges != null) {
                    earnedBadges = new ArrayList<>(badges);
                }
            }

            updateXpUI();
            updateStreakUI();
            renderBadges();

            // Calculate streak if not yet done today
            if (streak == 0) {
                calculateStreak();
            }
        });
    }

    // ═══════════════════════════════════════════════════════
    // STREAK CALCULATOR
    // ═══════════════════════════════════════════════════════

    private void calculateStreak() {
        // Check yesterday's progress document
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        String yesterdayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.getTime());

        db.collection("users").document(userId)
                .collection("progress").document(yesterdayStr)
                .get()
                .addOnSuccessListener(yesterdayDoc -> {
                    if (!isAdded() || getContext() == null) return;
                    if (yesterdayDoc.exists()) {
                        // Yesterday had activity → continue streak
                        Long yesterdayStreak = yesterdayDoc.getLong("streak");
                        streak = (yesterdayStreak != null ? yesterdayStreak.intValue() : 0) + 1;
                    } else {
                        // No activity yesterday → start fresh
                        streak = 1;
                    }

                    // Save streak to today's document
                    Map<String, Object> update = new HashMap<>();
                    update.put("streak", streak);
                    todayRef.set(update, SetOptions.merge());

                    updateStreakUI();
                    renderBadges();
                });
    }

    // ═══════════════════════════════════════════════════════
    // PHASE 2: WEEKLY CALORIE CHART
    // ═══════════════════════════════════════════════════════

    private void loadWeeklyCalories() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        String[] dayLabels = {"S", "M", "T", "W", "T", "F", "S"};
        String[] dateKeys = new String[7];

        // Go to Sunday of current week
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        for (int i = 0; i < 7; i++) {
            dateKeys[i] = sdf.format(cal.getTime());
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        weeklyChart.setDayLabels(dayLabels);

        // Query diary for each day
        db.collection("users").document(userId)
                .collection("diary")
                .whereGreaterThanOrEqualTo("date", dateKeys[0])
                .whereLessThanOrEqualTo("date", dateKeys[6])
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!isAdded() || getContext() == null) return;
                    // Sum calories per date
                    Map<String, Integer> dailyTotals = new HashMap<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String date = doc.getString("date");
                        Long calories = doc.getLong("calories");
                        if (date != null && calories != null) {
                            dailyTotals.put(date, dailyTotals.getOrDefault(date, 0) + calories.intValue());
                        }
                    }

                    int[] data = new int[7];
                    for (int i = 0; i < 7; i++) {
                        Integer t = dailyTotals.get(dateKeys[i]);
                        data[i] = t != null ? t : 0;
                    }

                    weeklyChart.setData(data, calorieTarget);

                    // Check diet badge: 7 consecutive days with diary entries
                    int consecutiveDays = 0;
                    for (int i = 6; i >= 0; i--) {
                        if (data[i] > 0) consecutiveDays++;
                        else break;
                    }
                    if (consecutiveDays >= 7) {
                        awardBadge(BADGE_DIET_7, "🥗 Diet Master");
                    }
                });
    }

    // ═══════════════════════════════════════════════════════
    // Background Check for Badges (Hidden from UI)
    // ═══════════════════════════════════════════════════════

    private void loadWeightTrendInBackground() {
        db.collection("users").document(userId)
                .collection("progress")
                .whereGreaterThan("weight", 0)
                .limit(10)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!isAdded() || getContext() == null) return;
                    // Badge: weight_consistent (3+ weight records)
                    if (querySnapshot.getDocuments().size() >= 3) {
                        awardBadge(BADGE_WEIGHT_3, "⚖️ Konsisten");
                    }
                });
    }

    // ═══════════════════════════════════════════════════════
    // GAMIFICATION logic
    // ═══════════════════════════════════════════════════════

    private void addXp(int amount) {
        totalXp += amount;
        Map<String, Object> update = new HashMap<>();
        update.put("xp", totalXp);
        todayRef.set(update, SetOptions.merge());
        updateXpUI();
    }

    private void updateXpUI() {
        int level = 1;
        for (int i = LEVEL_XP.length - 1; i >= 0; i--) {
            if (totalXp >= LEVEL_XP[i]) {
                level = i + 1;
                break;
            }
        }

        String levelName = level <= LEVEL_NAMES.length ? LEVEL_NAMES[level - 1] : "Legend";
        tvLevel.setText("Level Up!"); // As requested from screen

        int currentLevelXp = LEVEL_XP[Math.min(level - 1, LEVEL_XP.length - 1)];
        int nextLevelXp = level < LEVEL_XP.length ? LEVEL_XP[level] : LEVEL_XP[LEVEL_XP.length - 1] + 500;
        int xpInLevel = totalXp - currentLevelXp;
        int xpNeeded = nextLevelXp - currentLevelXp;

        xpProgressBar.setMax(xpNeeded);
        xpProgressBar.setProgress(Math.min(xpInLevel, xpNeeded));
        tvXp.setText(totalXp + "/" + nextLevelXp + " XP");
    }

    private void updateStreakUI() {
        tvStreak.setText("🔥 Streak: " + streak + " Hari");
    }

    // ═══════════════════════════════════════════════════════
    // PHASE 3: BADGES
    // ═══════════════════════════════════════════════════════

    private void awardBadge(String badgeId, String displayName) {
        if (!isAdded() || getContext() == null) return;
        if (earnedBadges.contains(badgeId)) return; // Already earned

        earnedBadges.add(badgeId);

        Map<String, Object> update = new HashMap<>();
        update.put("badges", earnedBadges);
        todayRef.set(update, SetOptions.merge());

        // Also add XP for badge
        addXp(25);

        Toast.makeText(requireContext(), "🏆 Badge baru: " + displayName + " (+25 XP)", Toast.LENGTH_LONG).show();
        renderBadges();
    }

    private void renderBadges() {
        if (!isAdded() || getContext() == null || badgeContainer == null) return;
        badgeContainer.removeAllViews();

        tvUnlockedCount.setText(earnedBadges.size() + "/6 UNLOCKED");

        // All possible badges
        String[][] allBadges = {
                {BADGE_WATER, "💧", "Hydration Hero", "Minum 8 gelas dalam 1 hari"},
                {BADGE_WALKER, "🏃", "Daily Walker", "6.000 langkah dalam 1 hari"},
                {BADGE_STREAK_3, "☀", "Early Bird", "Login 3 hari berturut-turut"},
                {BADGE_STREAK_7, "🔥", "Streak 7", "Login 7 hari berturut-turut"},
                {BADGE_DIET_7, "🥗", "Diet Master", "Catat makanan 7 hari"},
                {BADGE_WEIGHT_3, "⭐", "Consistency King", "Log semua matrik untuk 14 hari"},
        };

        for (String[] badge : allBadges) {
            boolean earned = earnedBadges.contains(badge[0]);
            addBadgeRow(badge[1], badge[2], badge[3], earned);
        }

        // Check streak badges
        if (streak >= 3 && !earnedBadges.contains(BADGE_STREAK_3)) {
            awardBadge(BADGE_STREAK_3, "🔥 3-Day Streak");
        }
        if (streak >= 7 && !earnedBadges.contains(BADGE_STREAK_7)) {
            awardBadge(BADGE_STREAK_7, "🔥 7-Day Streak");
        }
    }

    private void addBadgeRow(String emoji, String name, String desc, boolean earned) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(8), dp(4), dp(8), dp(4));
        row.setAlpha(earned ? 1f : 0.4f);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rowParams.bottomMargin = dp(8);
        row.setLayoutParams(rowParams);

        // Emoji icon
        TextView tvEmoji = new TextView(requireContext());
        tvEmoji.setText(emoji);
        tvEmoji.setTextSize(24);
        tvEmoji.setBackgroundResource(R.drawable.bg_badge_icon);
        tvEmoji.setGravity(Gravity.CENTER);
        
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(48), dp(48));
        iconParams.setMarginEnd(dp(16));
        tvEmoji.setLayoutParams(iconParams);
        
        row.addView(tvEmoji);

        // Name + desc
        LinearLayout textCol = new LinearLayout(requireContext());
        textCol.setOrientation(LinearLayout.VERTICAL);
        textCol.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView tvName = new TextView(requireContext());
        tvName.setText(name);
        tvName.setTextColor(earned ? 0xFFFFFFFF : 0xFFA9B5AC);
        tvName.setTextSize(14);
        tvName.setTypeface(null, Typeface.BOLD);
        textCol.addView(tvName);

        TextView tvDesc = new TextView(requireContext());
        tvDesc.setText(desc);
        tvDesc.setTextColor(0xFFA9B5AC);
        tvDesc.setTextSize(11);
        textCol.addView(tvDesc);

        row.addView(textCol);

        // Status indicator
        TextView tvStatus = new TextView(requireContext());
        tvStatus.setText(earned ? "✅" : "🔒");
        tvStatus.setTextSize(18);
        row.addView(tvStatus);

        badgeContainer.addView(row);
    }

    // ═══════════════════════════════════════════════════════
    // LIFECYCLE
    // ═══════════════════════════════════════════════════════

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    private int dp(int value) {
        if (!isAdded() || getContext() == null) return 0;
        return (int) (value * requireContext().getResources().getDisplayMetrics().density);
    }
}
