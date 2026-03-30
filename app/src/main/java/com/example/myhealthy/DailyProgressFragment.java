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
    private CircularProgressView progressSteps, progressCalories, progressWater;
    private TextView tvStreak, tvLevel, tvXp, tvBmiInfo, tvCalorieTarget, tvSleepHours;
    private ProgressBar xpProgressBar;
    private LinearLayout moodContainer;

    // Views — Phase 2
    private WeeklyBarChartView weeklyChart;
    private WeightTrendView weightTrend;

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
    private List<String> earnedBadges = new ArrayList<>();

    // Mood options
    private final String[] MOODS = {"😊", "😐", "😔", "😡", "😴"};
    private final String[] MOOD_LABELS = {"Senang", "Biasa", "Sedih", "Marah", "Ngantuk"};
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
        tvBmiInfo = view.findViewById(R.id.tvBmiInfo);
        tvCalorieTarget = view.findViewById(R.id.tvCalorieTarget);
        tvSleepHours = view.findViewById(R.id.tvSleepHours);

        // Bind Phase 2 views
        weeklyChart = view.findViewById(R.id.weeklyChart);
        weightTrend = view.findViewById(R.id.weightTrend);

        // Bind Phase 3 views
        badgeContainer = view.findViewById(R.id.badgeContainer);

        // Setup circular progress labels
        progressSteps.setLabel("Langkah");
        progressSteps.setProgressColor(0xFF4CAF50);
        progressCalories.setLabel("Kalori");
        progressCalories.setProgressColor(0xFFFF9800);
        progressWater.setLabel("Air");
        progressWater.setProgressColor(0xFF2196F3);

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

        // Phase 2: Load weekly chart data
        loadWeeklyCalories();
        loadWeightTrend();

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
            tv.setTextSize(28);
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(dp(8), dp(6), dp(8), dp(6));
            tv.setClickable(true);
            tv.setFocusable(true);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
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
                moodButtons[i].setBackgroundColor(0x30008B02);
                moodButtons[i].setTextSize(34);
            } else {
                moodButtons[i].setBackgroundColor(Color.TRANSPARENT);
                moodButtons[i].setTextSize(28);
            }
        }
        Map<String, Object> update = new HashMap<>();
        update.put("mood", selectedMood);
        todayRef.set(update, SetOptions.merge());
        addXp(5);
        Toast.makeText(requireContext(), MOOD_LABELS[index] + " — +5 XP", Toast.LENGTH_SHORT).show();
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
        progressWater.setProgress(waterCount, WATER_TARGET);
        progressWater.setUnit(waterCount + "/" + WATER_TARGET);
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
                addXp(15);
                Toast.makeText(requireContext(), "Berat " + val + " kg disimpan! +15 XP", Toast.LENGTH_SHORT).show();
                // Refresh weight trend
                loadWeightTrend();
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
            tvSleepHours.setText((int) sleepHours + " jam");
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
            progressSteps.setLabel("N/A");
            progressSteps.setUnit("No sensor");
            progressSteps.setProgress(0, STEP_TARGET);
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
            progressSteps.setUnit(currentSteps + "/" + STEP_TARGET);

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
                if (bmi < 18.5) cat = "Kurus";
                else if (bmi < 24.9) cat = "Normal";
                else if (bmi < 29.9) cat = "Gemuk";
                else cat = "Obesitas";

                tvBmiInfo.setText(String.format(Locale.US, "BMI: %.1f (%s) — Berat: %.0f kg", bmi, cat, weight));

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

                tvCalorieTarget.setText("Target harian: " + calorieTarget + " kkal");
            } catch (Exception e) {
                tvBmiInfo.setText("Data kalkulator belum lengkap.");
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
                    caloriesEaten = 0;
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Long cal = doc.getLong("calories");
                        if (cal != null) caloriesEaten += cal.intValue();
                    }
                    progressCalories.setProgress(caloriesEaten, calorieTarget);
                    progressCalories.setUnit(caloriesEaten + "/" + calorieTarget);
                });
    }

    @SuppressWarnings("unchecked")
    private void loadProgress() {
        todayRef.get().addOnSuccessListener(doc -> {
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
                            moodButtons[i].setBackgroundColor(0x30008B02);
                            moodButtons[i].setTextSize(34);
                        }
                    }
                }

                // Sleep
                Double sl = doc.getDouble("sleepHours");
                if (sl != null) {
                    sleepHours = sl;
                    tvSleepHours.setText((int) sleepHours + " jam");
                }

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

        String[] dayLabels = {"Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab"};
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
    // PHASE 2: WEIGHT TREND
    // ═══════════════════════════════════════════════════════

    private void loadWeightTrend() {
        db.collection("users").document(userId)
                .collection("progress")
                .whereGreaterThan("weight", 0)
                .orderBy("weight", Query.Direction.ASCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Float> weights = new ArrayList<>();
                    List<String> labels = new ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Double w = doc.getDouble("weight");
                        if (w != null && w > 0) {
                            weights.add(w.floatValue());
                            String docId = doc.getId(); // yyyy-MM-dd
                            // Show as dd/MM
                            if (docId.length() >= 10) {
                                labels.add(docId.substring(8, 10) + "/" + docId.substring(5, 7));
                            } else {
                                labels.add(docId);
                            }
                        }
                    }

                    weightTrend.setData(weights, labels);

                    // Badge: weight_consistent (3+ weight records)
                    if (weights.size() >= 3) {
                        awardBadge(BADGE_WEIGHT_3, "⚖️ Konsisten");
                    }
                });
    }

    // ═══════════════════════════════════════════════════════
    // PHASE 3: XP & GAMIFICATION
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
        tvLevel.setText("⭐ Lv." + level + " " + levelName);

        int currentLevelXp = LEVEL_XP[Math.min(level - 1, LEVEL_XP.length - 1)];
        int nextLevelXp = level < LEVEL_XP.length ? LEVEL_XP[level] : LEVEL_XP[LEVEL_XP.length - 1] + 500;
        int xpInLevel = totalXp - currentLevelXp;
        int xpNeeded = nextLevelXp - currentLevelXp;

        xpProgressBar.setMax(xpNeeded);
        xpProgressBar.setProgress(Math.min(xpInLevel, xpNeeded));
        tvXp.setText(totalXp + " / " + nextLevelXp + " XP");
    }

    private void updateStreakUI() {
        tvStreak.setText("🔥 Streak: " + streak + " Hari");
    }

    // ═══════════════════════════════════════════════════════
    // PHASE 3: BADGES
    // ═══════════════════════════════════════════════════════

    private void awardBadge(String badgeId, String displayName) {
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
        if (badgeContainer == null) return;
        badgeContainer.removeAllViews();

        // All possible badges
        String[][] allBadges = {
                {BADGE_WATER, "🥇", "Pejuang Air Putih", "Minum 8 gelas dalam 1 hari"},
                {BADGE_WALKER, "🏃", "Pejalan Kaki", "6.000 langkah dalam 1 hari"},
                {BADGE_STREAK_3, "🔥", "3-Day Streak", "3 hari berturut-turut aktif"},
                {BADGE_STREAK_7, "🔥", "7-Day Streak", "7 hari berturut-turut aktif"},
                {BADGE_DIET_7, "🥗", "Diet Master", "Catat makanan 7 hari berturut"},
                {BADGE_WEIGHT_3, "⚖️", "Konsisten", "Update berat badan 3x"},
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
        row.setPadding(dp(8), dp(8), dp(8), dp(8));
        row.setAlpha(earned ? 1f : 0.4f);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rowParams.bottomMargin = dp(4);
        row.setLayoutParams(rowParams);

        // Emoji icon
        TextView tvEmoji = new TextView(requireContext());
        tvEmoji.setText(emoji);
        tvEmoji.setTextSize(24);
        tvEmoji.setPadding(0, 0, dp(12), 0);
        row.addView(tvEmoji);

        // Name + desc
        LinearLayout textCol = new LinearLayout(requireContext());
        textCol.setOrientation(LinearLayout.VERTICAL);
        textCol.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView tvName = new TextView(requireContext());
        tvName.setText(name);
        tvName.setTextColor(earned ? 0xFF222222 : 0xFF999999);
        tvName.setTextSize(13);
        tvName.setTypeface(null, Typeface.BOLD);
        textCol.addView(tvName);

        TextView tvDesc = new TextView(requireContext());
        tvDesc.setText(desc);
        tvDesc.setTextColor(0xFF999999);
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
        return (int) (value * requireContext().getResources().getDisplayMetrics().density);
    }
}
