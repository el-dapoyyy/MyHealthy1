package com.example.myhealthy;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FoodDiaryFragment extends Fragment {

    private TextView tvTotalCalories, tvRemaining, tvDate;
    private RecyclerView rvDiary;

    private DiaryAdapter adapter;
    private final List<DiaryEntry> entries = new ArrayList<>();

    private FirebaseFirestore db;
    private String userId;
    private String todayStr;
    
    private final String[] MEAL_TYPES = {"Sarapan", "Makan Siang", "Makan Malam", "Snack", "Minuman"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_food_diary, container, false);

        tvTotalCalories = view.findViewById(R.id.tvTotalCalories);
        tvRemaining = view.findViewById(R.id.tvRemaining);
        tvDate = view.findViewById(R.id.tvDate);
        rvDiary = view.findViewById(R.id.rvDiary);

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user != null ? user.getUid() : "anonymous";

        todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        String displayDate = new SimpleDateFormat("EEEE, MMM d", new Locale("id", "ID")).format(new Date());
        tvDate.setText(displayDate);

        // Sub-elements visibility
        // pbCalories max is set dynamically in loadEntries()

        // RecyclerView
        adapter = new DiaryAdapter(entries, position -> deleteEntry(position));
        rvDiary.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvDiary.setAdapter(adapter);

        // Back button
        TextView btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() instanceof MainNavActivity) {
                    ((MainNavActivity) getActivity()).goBackToPreviousTab();
                }
            });
        }

        // Add button (Open Dialog)
        view.findViewById(R.id.btnFloatingAdd).setOnClickListener(v -> showAddMealDialog());

        // Load today's entries
        loadEntries();

        return view;
    }

    private int getTargetCalories() {
        android.content.SharedPreferences pref = requireContext().getSharedPreferences("myhealthy_calorie_calc", android.content.Context.MODE_PRIVATE);
        return pref.getInt("target_calories", 0);
    }

    private void showAddMealDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_meal, null);
        builder.setView(dialogView);

        EditText etFoodName = dialogView.findViewById(R.id.etDialogFoodName);
        EditText etCalories = dialogView.findViewById(R.id.etDialogCalories);
        Spinner spinnerMealType = dialogView.findViewById(R.id.spinnerDialogMealType);

        ArrayAdapter<String> mealAdapter = new ArrayAdapter<>(requireContext(),
                R.layout.spinner_item_dark, MEAL_TYPES);
        mealAdapter.setDropDownViewResource(R.layout.spinner_dropdown_dark);
        spinnerMealType.setAdapter(mealAdapter);

        // Auto-select type based on time
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        if (hour < 10) spinnerMealType.setSelection(0);
        else if (hour < 15) spinnerMealType.setSelection(1);
        else if (hour < 20) spinnerMealType.setSelection(2);
        else spinnerMealType.setSelection(3);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialogView.findViewById(R.id.btnDialogCancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnDialogSave).setOnClickListener(v -> {
            String name = etFoodName.getText().toString().trim();
            String calStr = etCalories.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                etFoodName.setError("Masukkan nama makanan");
                return;
            }
            if (TextUtils.isEmpty(calStr)) {
                etCalories.setError("Masukkan kalori");
                return;
            }

            int cal = Integer.parseInt(calStr);
            String mealType = MEAL_TYPES[spinnerMealType.getSelectedItemPosition()];

            addEntryToFirestore(name, cal, mealType);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void addEntryToFirestore(String name, int calories, String mealType) {
        Map<String, Object> data = new HashMap<>();
        data.put("foodName", name);
        data.put("calories", calories);
        data.put("mealType", mealType);
        data.put("date", todayStr);

        db.collection("users").document(userId)
                .collection("diary")
                .add(data)
                .addOnSuccessListener(ref -> {
                    loadEntries();
                    Toast.makeText(requireContext(), "Catatan ditambahkan!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Gagal menyimpan: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void deleteEntry(int position) {
        if (position < 0 || position >= entries.size()) return;
        DiaryEntry entry = entries.get(position);
        if (entry.documentId == null) return;

        db.collection("users").document(userId)
                .collection("diary")
                .document(entry.documentId)
                .delete()
                .addOnSuccessListener(aVoid -> loadEntries())
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Gagal menghapus", Toast.LENGTH_SHORT).show()
                );
    }

    private void loadEntries() {
        db.collection("users").document(userId)
                .collection("diary")
                .whereEqualTo("date", todayStr)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    entries.clear();
                    int total = 0;
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        DiaryEntry entry = doc.toObject(DiaryEntry.class);
                        if (entry != null) {
                            entry.documentId = doc.getId();
                            entries.add(entry);
                            total += entry.calories;
                        }
                    }
                    
                    // Sort programmatically since Firestore requires build-time Index for mixed queries
                    entries.sort((a, b) -> {
                        String m1 = a.mealType != null ? a.mealType : "";
                        String m2 = b.mealType != null ? b.mealType : "";
                        return m1.compareTo(m2);
                    });
                    
                    adapter.notifyDataSetChanged();
                    
                    // Update Fuel Consumed Card
                    tvTotalCalories.setText(String.valueOf(total));
                    
                    int target = getTargetCalories();
                    if (target > 0) {
                        int remaining = Math.max(0, target - total);
                        tvRemaining.setText(String.valueOf(remaining));
                        TextView tvDailyTarget = getView().findViewById(R.id.tvDailyTarget);
                        if (tvDailyTarget != null) {
                            tvDailyTarget.setText(String.format("%,d", target));
                        }
                    } else {
                        tvRemaining.setText("--");
                        TextView tvDailyTarget = getView().findViewById(R.id.tvDailyTarget);
                        if (tvDailyTarget != null) {
                            tvDailyTarget.setText("--");
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Gagal memuat: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}
