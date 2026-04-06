package com.example.myhealthy;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FoodDiaryFragment extends Fragment {

    private EditText etFoodName, etCalories;
    private Spinner spinnerMealType;
    private TextView tvTotalCalories, tvDate;
    private RecyclerView rvDiary;

    private DiaryAdapter adapter;
    private final List<DiaryEntry> entries = new ArrayList<>();

    private FirebaseFirestore db;
    private String userId;
    private String todayStr;

    private final String[] MEAL_TYPES = {"Sarapan", "Makan Siang", "Makan Malam", "Snack"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_food_diary, container, false);

        etFoodName = view.findViewById(R.id.etFoodName);
        etCalories = view.findViewById(R.id.etCalories);
        spinnerMealType = view.findViewById(R.id.spinnerMealType);
        tvTotalCalories = view.findViewById(R.id.tvTotalCalories);
        tvDate = view.findViewById(R.id.tvDate);
        rvDiary = view.findViewById(R.id.rvDiary);

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user != null ? user.getUid() : "anonymous";

        todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        String displayDate = new SimpleDateFormat("EEEE, d MMMM yyyy", new Locale("id", "ID")).format(new Date());
        tvDate.setText(displayDate);

        // Spinner
        ArrayAdapter<String> mealAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, MEAL_TYPES);
        mealAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMealType.setAdapter(mealAdapter);

        // RecyclerView
        adapter = new DiaryAdapter(entries, position -> deleteEntry(position));
        rvDiary.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvDiary.setAdapter(adapter);

        // Add button
        view.findViewById(R.id.btnAddEntry).setOnClickListener(v -> addEntry());

        // Load today's entries
        loadEntries();

        return view;
    }

    private void addEntry() {
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

        Map<String, Object> data = new HashMap<>();
        data.put("foodName", name);
        data.put("calories", cal);
        data.put("mealType", mealType);
        data.put("date", todayStr);

        db.collection("users").document(userId)
                .collection("diary")
                .add(data)
                .addOnSuccessListener(ref -> {
                    etFoodName.setText("");
                    etCalories.setText("");
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
                    tvTotalCalories.setText(total + " kkal");
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Gagal memuat: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}
