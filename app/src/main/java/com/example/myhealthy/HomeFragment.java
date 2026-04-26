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
        tagAll.setTextColor(requireContext().getResources().getColor(R.color.text_secondary));
        tagHewani.setBackgroundResource(R.drawable.bg_tag_unselected_gray);
        tagHewani.setTextColor(requireContext().getResources().getColor(R.color.text_secondary));
        tagNabati.setBackgroundResource(R.drawable.bg_tag_unselected_gray);
        tagNabati.setTextColor(requireContext().getResources().getColor(R.color.text_secondary));

        if (tag == TagType.ALL) {
            tagAll.setBackgroundResource(R.drawable.bg_tag_selected_green);
            tagAll.setTextColor(requireContext().getResources().getColor(R.color.bg_main));
        } else if (tag == TagType.HEWANI) {
            tagHewani.setBackgroundResource(R.drawable.bg_tag_selected_green);
            tagHewani.setTextColor(requireContext().getResources().getColor(R.color.bg_main));
        } else {
            tagNabati.setBackgroundResource(R.drawable.bg_tag_selected_green);
            tagNabati.setTextColor(requireContext().getResources().getColor(R.color.bg_main));
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
        add("salmon panggang"); add("yoghurt yunani"); add("daging sapi tanpa lemak");
        add("ikan nila"); add("dada kalkun"); add("susu sapi rendah lemak"); add("keju cottage");
    }};

    private final Set<String> NABATI_NAMES = new HashSet<String>() {{
        add("tahu"); add("tempe"); add("brokoli"); add("oatmeal");
        add("quinoa"); add("bayam"); add("alpukat"); add("kacang almond");
        add("beras merah"); add("ubi jalar"); add("edamame");
        add("biji chia"); add("kacang kenari"); add("kedelai hitam"); 
        add("kacang hijau"); add("gandum utuh"); add("susu kedelai");
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
                4.9f
        ));

        allFoods.add(new FoodItem(
                "Tahu",
                "Tahu adalah sumber protein nabati yang rendah kalori dan serbaguna. Cocok untuk diet sehat, menjaga massa otot, dan menu harian. Nilai gizi terbaik jika dimasak dengan cara kukus, tumis ringan, atau panggang.",
                R.drawable.placeholder_food_image,
                8, 76, 5, 2,
                "• Berat bersih contoh saji: 100 g\n• Rekomendasi penyajian: Dikukus, ditumis sedikit minyak, dipanggang\n• Penyimpanan: Simpan di kulkas dalam wadah tertutup; habiskan 2–3 hari setelah dibuka",
                4.5f
        ));

        allFoods.add(new FoodItem(
                "Tempe",
                "Tempe adalah kedelai fermentasi kaya protein dan serat, dengan profil asam amino yang baik. Cocok untuk menu tinggi protein, ramah kenyang, dan mudah diolah.",
                R.drawable.tempe,
                20, 193, 11, 8,
                "• Berat bersih contoh saji: 100 g\n• Rekomendasi penyajian: Dikukus, ditumis ringan, dipanggang/air fryer\n• Penyimpanan: Simpan dingin; dapat dibekukan untuk umur simpan lebih lama",
                4.8f
        ));

        allFoods.add(new FoodItem(
                "Telur",
                "Telur ayam adalah sumber protein hewani lengkap dengan vitamin dan mineral. Praktis, mengenyangkan, dan cocok untuk sarapan atau camilan berprotein.",
                R.drawable.telur,
                13, 155, 11, 1,
                "• Berat bersih contoh saji: 100 g (~2 butir telur ukuran sedang, tanpa cangkang)\n• Rekomendasi penyajian: Direbus, orak-arik tanpa banyak minyak, telur dadar tipis\n• Penyimpanan: Simpan telur mentah di kulkas; telur rebus utuh tahan 3–4 hari dalam kulkas",
                4.7f
        ));

        allFoods.add(new FoodItem(
                "Paha Ayam",
                "Paha ayam tanpa kulit mengandung protein tinggi dengan rasa lebih gurih dibanding dada. Cocok untuk pembentukan otot dan menu harian. Disarankan metode masak rendah minyak agar nilai gizinya optimal.",
                R.drawable.paha_ayam,
                26, 209, 11, 0,
                "• Berat bersih contoh saji: 100 g daging matang, tanpa kulit/tulang\n• Rekomendasi penyajian: Direbus, dikukus, dipanggang/air fryer\n• Penyimpanan: Simpan daging mentah di kulkas (≤2 hari) atau beku; masakan matang tahan 3–4 hari di kulkas",
                4.2f
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
                5.0f
        ));

        allFoods.add(new FoodItem(
                "Oatmeal",
                "Oatmeal sumber karbohidrat kompleks dan serat. Cocok untuk sarapan kenyang lebih lama.",
                R.drawable.oatmeal,
                13, 379, 7, 67,
                "• Berat bersih contoh saji: 40 g (kering)\n• Rekomendasi: Seduh air panas/susu rendah lemak\n• Penyimpanan: Simpan kering & tertutup rapat",
                4.6f
        ));

        // 10 Menu Baru
        allFoods.add(new FoodItem(
                "Salmon Panggang",
                "Kaya akan Omega-3 dan protein berkualitas tinggi. Baik untuk kesehatan jantung dan otak.",
                R.drawable.salmon_panggang,
                22, 206, 13, 0,
                "• Berat bersih contoh saji: 100 g\n• Rekomendasi: Panggang/Bakar dengan sedikit minyak zaitun\n• Penyimpanan: Kulkas/Freezer",
                5.0f
        ));

        allFoods.add(new FoodItem(
                "Quinoa",
                "Sumber karbohidrat kompleks bebas gluten dan mengandung 9 asam amino esensial.",
                R.drawable.quinoa,
                4, 120, 2, 21,
                "• Berat bersih contoh saji: 100 g (matang)\n• Rekomendasi: Rebus sebagai pengganti nasi\n• Penyimpanan: Tempat kering tertutup rapat",
                5.0f
        ));

        allFoods.add(new FoodItem(
                "Bayam",
                "Sayuran hijau padat nutrisi, tinggi zat besi, kalsium, dan vitamin K.",
                R.drawable.bayam,
                3, 23, 0, 4,
                "• Berat bersih contoh saji: 100 g\n• Rekomendasi: Rebus sebentar atau jadikan salad segar\n• Penyimpanan: Kulkas 3-5 hari",
                5.0f
        ));

        allFoods.add(new FoodItem(
                "Alpukat",
                "Buah dengan lemak sehat (tak jenuh tunggal) yang baik untuk kolesterol.",
                R.drawable.alpukat,
                2, 160, 15, 9,
                "• Berat bersih contoh saji: 100 g\n• Rekomendasi: Dimakan langsung atau sebagai olesan\n• Penyimpanan: Suhu ruang hingga matang, lalu kulkas",
                4.8f
        ));

        allFoods.add(new FoodItem(
                "Yoghurt Yunani",
                "Yoghurt kental tinggi protein dan probiotik, sangat baik untuk pencernaan.",
                R.drawable.yoghurt_yunani,
                10, 100, 0, 4,
                "• Berat bersih contoh saji: 100 g\n• Rekomendasi: Dimakan langsung atau campur dengan buah\n• Penyimpanan: Simpan selalu di kulkas",
                4.9f
        ));

        allFoods.add(new FoodItem(
                "Kacang Almond",
                "Camilan sehat tinggi vitamin E, magnesium, dan lemak sehat tak jenuh.",
                R.drawable.kacang_almond,
                21, 579, 50, 22,
                "• Berat bersih contoh saji: 100 g\n• Rekomendasi: Camilan panggang tanpa garam\n• Penyimpanan: Toples kedap udara",
                4.7f
        ));

        allFoods.add(new FoodItem(
                "Daging Sapi Tanpa Lemak",
                "Sumber zat besi, zinc, dan vitamin B12 yang sangat baik untuk sel darah merah.",
                R.drawable.daging_sapi_tanpa_lemak,
                26, 250, 15, 0,
                "• Berat bersih contoh saji: 100 g\n• Rekomendasi: Rebus, sup, atau panggang matang\n• Penyimpanan: Freezer",
                4.6f
        ));

        allFoods.add(new FoodItem(
                "Beras Merah",
                "Alternatif nasi putih yang jauh lebih kaya serat dan indeks glikemik rendah.",
                R.drawable.beras_merah,
                3, 110, 1, 23,
                "• Berat bersih contoh saji: 100 g (matang)\n• Rekomendasi: Tanak seperti nasi biasa\n• Penyimpanan: Tempat kering dan sejuk",
                4.9f
        ));

        allFoods.add(new FoodItem(
                "Ubi Jalar",
                "Mengandung karbohidrat kompleks, serat tinggi, dan kaya akan vitamin A.",
                R.drawable.ubi_jalar,
                2, 86, 0, 20,
                "• Berat bersih contoh saji: 100 g\n• Rekomendasi: Rebus, kukus, atau panggang\n• Penyimpanan: Suhu ruang, sejuk",
                4.8f
        ));

        allFoods.add(new FoodItem(
                "Edamame",
                "Kedelai muda utuh yang direbus, merupakan camilan tinggi protein dan serat.",
                R.drawable.edamame,
                12, 121, 5, 11,
                "• Berat bersih contoh saji: 100 g\n• Rekomendasi: Rebus dengan sedikit garam laut\n• Penyimpanan: Kulkas/Freezer",
                4.8f
        ));

        // 10 Menu Baru Lagi (Gelombang 2)
        allFoods.add(new FoodItem(
                "Biji Chia",
                "Superfood mungil padat Omega-3, serat, dan kalsium. Sangat baik untuk pencernaan.",
                R.drawable.biji_chia,
                17, 486, 31, 42,
                "• Berat bersih contoh saji: 100 g\n• Rekomendasi: Rendam dalam air/susu hingga mengembang\n• Penyimpanan: Tempat kering dan kedap udara",
                4.9f
        ));

        allFoods.add(new FoodItem(
                "Kacang Kenari",
                "Sumber lemak sehat otak dan antioksidan yang sangat tinggi dibanding kacang lainnya.",
                R.drawable.kacang_kenari,
                15, 654, 65, 14,
                "• Berat bersih contoh saji: 100 g\n• Rekomendasi: Camilan mentah atau panggang\n• Penyimpanan: Kulkas agar minyak tidak tengik",
                4.8f
        ));

        allFoods.add(new FoodItem(
                "Ikan Nila",
                "Ikan air tawar rendah kalori dan lemak, namun sangat kaya akan protein.",
                R.drawable.ikan_nila,
                26, 128, 3, 0,
                "• Berat bersih contoh saji: 100 g\n• Rekomendasi: Kukus, tim, atau panggang\n• Penyimpanan: Freezer",
                4.6f
        ));

        allFoods.add(new FoodItem(
                "Dada Kalkun",
                "Alternatif dada ayam dengan kalori lebih rendah dan protein luar biasa tinggi.",
                R.drawable.dada_kalkun,
                17, 104, 2, 4,
                "• Berat bersih contoh saji: 100 g\n• Rekomendasi: Panggang atau rebus\n• Penyimpanan: Kulkas/Freezer",
                4.8f
        ));

        allFoods.add(new FoodItem(
                "Susu Sapi Rendah Lemak",
                "Sumber kalsium dan vitamin D harian dengan pangkasan kalori dari lemak jenuh.",
                R.drawable.susu_sapi_rendah_lemak,
                3, 42, 1, 5,
                "• Contoh saji: 100 ml\n• Rekomendasi: Diminum langsung atau campur sereal\n• Penyimpanan: Kulkas",
                4.5f
        ));

        allFoods.add(new FoodItem(
                "Kedelai Hitam",
                "Kaya antosianin (antioksidan) dan serat yang sangat baik untuk gula darah.",
                R.drawable.kedelai_hitam,
                11, 120, 5, 12,
                "• Berat bersih contoh saji: 100 g (rebus)\n• Rekomendasi: Rebus dan jadikan pelengkap salad\n• Penyimpanan: Tempat kering sebelum direbus",
                4.8f
        ));

        allFoods.add(new FoodItem(
                "Kacang Hijau",
                "Karbohidrat kompleks yang aman untuk lambung dan tinggi folat.",
                R.drawable.kacang_hijau,
                7, 105, 0, 19,
                "• Berat bersih contoh saji: 100 g (rebus)\n• Rekomendasi: Bubur tanpa santan manis alami\n• Penyimpanan: Tempat sejuk kering",
                4.7f
        ));

        allFoods.add(new FoodItem(
                "Gandum Utuh",
                "Biji-bijian utuh kaya serat tidak larut, menjaga pencernaan dan kenyang lebih lama.",
                R.drawable.gandum_utuh,
                13, 340, 2, 71,
                "• Berat bersih contoh saji: 100 g\n• Rekomendasi: Rebus atau jadikan roti gandum utuh\n• Penyimpanan: Toples tertutup rapat",
                4.6f
        ));

        allFoods.add(new FoodItem(
                "Keju Cottage",
                "Keju segar tinggi protein kasein, sangat disukai atlet untuk pemulihan otot malam hari.",
                R.drawable.keju_cottage,
                11, 98, 4, 3,
                "• Berat bersih contoh saji: 100 g\n• Rekomendasi: Dicampur buah atau olesan roti\n• Penyimpanan: Kulkas (cepat basi di suhu ruang)",
                4.7f
        ));

        allFoods.add(new FoodItem(
                "Susu Kedelai",
                "Alternatif susu sapi bebas laktosa, kaya isoflavon dan protein nabati.",
                R.drawable.susu_kedelai,
                3, 54, 2, 6,
                "• Contoh saji: 100 ml (tanpa gula)\n• Rekomendasi: Minuman hangat harian\n• Penyimpanan: Kulkas",
                4.6f
        ));
    }
}
