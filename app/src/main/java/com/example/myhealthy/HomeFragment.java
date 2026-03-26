package com.example.myhealthy;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private FoodAdapter adapter;
    private EditText searchBar;
    private TextView tagAll, tagHewani, tagNabati;

    private final List<FoodItem> allFoods = new ArrayList<>();

    private enum TagType { ALL, HEWANI, NABATI }
    private TagType selectedTag = TagType.ALL;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        searchBar = view.findViewById(R.id.searchBar);
        tagAll = view.findViewById(R.id.tagAll);
        tagHewani = view.findViewById(R.id.tagHewani);
        tagNabati = view.findViewById(R.id.tagNabati);

        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        adapter = new FoodAdapter(requireContext(), new ArrayList<>());
        recyclerView.setAdapter(adapter);

        seedFoods();

        tagAll.setOnClickListener(v -> setTag(TagType.ALL));
        tagHewani.setOnClickListener(v -> setTag(TagType.HEWANI));
        tagNabati.setOnClickListener(v -> setTag(TagType.NABATI));

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }
        });

        setTag(TagType.ALL);
        return view;
    }

    private void setTag(TagType tag) {
        selectedTag = tag;

        tagAll.setBackgroundResource(R.drawable.bg_tag_unselected_gray);
        tagAll.setTextColor(requireContext().getResources().getColor(android.R.color.black));
        tagHewani.setBackgroundResource(R.drawable.bg_tag_unselected_gray);
        tagHewani.setTextColor(requireContext().getResources().getColor(android.R.color.black));
        tagNabati.setBackgroundResource(R.drawable.bg_tag_unselected_gray);
        tagNabati.setTextColor(requireContext().getResources().getColor(android.R.color.black));

        if (tag == TagType.ALL) {
            tagAll.setBackgroundResource(R.drawable.bg_tag_selected_green);
            tagAll.setTextColor(requireContext().getResources().getColor(android.R.color.white));
        } else if (tag == TagType.HEWANI) {
            tagHewani.setBackgroundResource(R.drawable.bg_tag_selected_green);
            tagHewani.setTextColor(requireContext().getResources().getColor(android.R.color.white));
        } else {
            tagNabati.setBackgroundResource(R.drawable.bg_tag_selected_green);
            tagNabati.setTextColor(requireContext().getResources().getColor(android.R.color.white));
        }

        applyFilters();
    }

    private void applyFilters() {
        String q = searchBar.getText() == null ? "" : searchBar.getText().toString().trim().toLowerCase(Locale.ROOT);

        List<FoodItem> filtered = new ArrayList<>();
        for (FoodItem f : allFoods) {
            if (selectedTag == TagType.HEWANI && !isHewani(f)) continue;
            if (selectedTag == TagType.NABATI && !isNabati(f)) continue;

            if (!q.isEmpty()) {
                String name = f.getName() == null ? "" : f.getName().toLowerCase(Locale.ROOT);
                if (!name.contains(q)) continue;
            }

            filtered.add(f);
        }

        adapter.setData(filtered);
    }

    private final Set<String> HEWANI_NAMES = new HashSet<String>() {{
        add("dada ayam"); add("paha ayam"); add("telur"); add("ikan tuna");
    }};

    private final Set<String> NABATI_NAMES = new HashSet<String>() {{
        add("tahu"); add("tempe"); add("brokoli"); add("oatmeal");
    }};

    private boolean isHewani(FoodItem f) {
        String n = f.getName() == null ? "" : f.getName().toLowerCase(Locale.ROOT);
        return HEWANI_NAMES.contains(n);
    }

    private boolean isNabati(FoodItem f) {
        String n = f.getName() == null ? "" : f.getName().toLowerCase(Locale.ROOT);
        return NABATI_NAMES.contains(n);
    }

    private void seedFoods() {
        allFoods.clear();

        allFoods.add(new FoodItem(
                "Dada Ayam",
                "Dada ayam adalah sumber protein hewani tinggi dengan lemak dan kalori rendah. Cocok untuk diet sehat, pembentukan otot, dan menjaga berat badan. Disarankan dimasak dengan cara rebus, kukus, atau panggang agar nilai gizinya tetap optimal.",
                R.drawable.dada_ayam,
                31, 165, 3, 0,
                "• Berat bersih: 100 g\n• Rekomendasi penyajian: Direbus, Dikukus, Dipanggang\n• Penyimpanan: Simpan di tempat sejuk & kering",
                5.0f
        ));

        allFoods.add(new FoodItem(
                "Tahu",
                "Tahu adalah sumber protein nabati yang rendah kalori dan serbaguna. Cocok untuk diet sehat, menjaga massa otot, dan menu harian. Nilai gizi terbaik jika dimasak dengan cara kukus, tumis ringan, atau panggang.",
                R.drawable.placeholder_food_image,
                8, 76, 5, 2,
                "• Berat bersih contoh saji: 100 g\n• Rekomendasi penyajian: Dikukus, ditumis sedikit minyak, dipanggang\n• Penyimpanan: Simpan di kulkas dalam wadah tertutup; habiskan 2–3 hari setelah dibuka",
                5.0f
        ));

        allFoods.add(new FoodItem(
                "Tempe",
                "Tempe adalah kedelai fermentasi kaya protein dan serat, dengan profil asam amino yang baik. Cocok untuk menu tinggi protein, ramah kenyang, dan mudah diolah.",
                R.drawable.tempe,
                20, 193, 11, 8,
                "• Berat bersih contoh saji: 100 g\n• Rekomendasi penyajian: Dikukus, ditumis ringan, dipanggang/air fryer\n• Penyimpanan: Simpan dingin; dapat dibekukan untuk umur simpan lebih lama",
                5.0f
        ));

        allFoods.add(new FoodItem(
                "Telur",
                "Telur ayam adalah sumber protein hewani lengkap dengan vitamin dan mineral. Praktis, mengenyangkan, dan cocok untuk sarapan atau camilan berprotein.",
                R.drawable.telur,
                13, 155, 11, 1,
                "• Berat bersih contoh saji: 100 g (~2 butir telur ukuran sedang, tanpa cangkang)\n• Rekomendasi penyajian: Direbus, orak-arik tanpa banyak minyak, telur dadar tipis\n• Penyimpanan: Simpan telur mentah di kulkas; telur rebus utuh tahan 3–4 hari dalam kulkas",
                5.0f
        ));

        allFoods.add(new FoodItem(
                "Paha Ayam",
                "Paha ayam tanpa kulit mengandung protein tinggi dengan rasa lebih gurih dibanding dada. Cocok untuk pembentukan otot dan menu harian. Disarankan metode masak rendah minyak agar nilai gizinya optimal.",
                R.drawable.paha_ayam,
                26, 209, 11, 0,
                "• Berat bersih contoh saji: 100 g daging matang, tanpa kulit/tulang\n• Rekomendasi penyajian: Direbus, dikukus, dipanggang/air fryer\n• Penyimpanan: Simpan daging mentah di kulkas (≤2 hari) atau beku; masakan matang tahan 3–4 hari di kulkas",
                5.0f
        ));

        allFoods.add(new FoodItem(
                "Ikan Tuna",
                "Tuna tinggi protein dan rendah lemak. Cocok untuk menu diet dan pembentukan otot.",
                R.drawable.tuna,
                29, 132, 1, 0,
                "• Berat bersih contoh saji: 100 g\n• Rekomendasi: Panggang/air fryer/kukus\n• Penyimpanan: Simpan dingin, konsumsi 1–2 hari",
                4.8f
        ));

        allFoods.add(new FoodItem(
                "Brokoli",
                "Brokoli kaya serat dan mikronutrien. Cocok untuk pendamping menu rendah kalori.",
                R.drawable.brokoli,
                3, 34, 0, 7,
                "• Berat bersih contoh saji: 100 g\n• Rekomendasi: Kukus/tumis ringan\n• Penyimpanan: Simpan di kulkas 3–5 hari",
                4.6f
        ));

        allFoods.add(new FoodItem(
                "Oatmeal",
                "Oatmeal sumber karbohidrat kompleks dan serat. Cocok untuk sarapan kenyang lebih lama.",
                R.drawable.oatmeal,
                13, 379, 7, 67,
                "• Berat bersih contoh saji: 40 g (kering)\n• Rekomendasi: Seduh air panas/susu rendah lemak\n• Penyimpanan: Simpan kering & tertutup rapat",
                4.7f
        ));
    }
}
