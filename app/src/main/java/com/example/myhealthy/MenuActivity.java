package com.example.myhealthy;

import android.content.Intent;
import androidx.appcompat.widget.AppCompatImageButton;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MenuActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FoodAdapter adapter;
    private EditText searchBar;

    // [BARU] Variabel untuk Switch

    private TextView tagAll, tagHewani, tagNabati;

    private final List<FoodItem> allFoods = new ArrayList<>();

    private enum TagType { ALL, HEWANI, NABATI }
    private TagType selectedTag = TagType.ALL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // Menggunakan AppCompatImageButton agar cocok dengan XML yang baru (Rounded Button)
        AppCompatImageButton fabCalculator = findViewById(R.id.fabCalculator);

        if (fabCalculator != null) {
            fabCalculator.setOnClickListener(v -> {
                Intent i = new Intent(MenuActivity.this, CalorieCalculatorActivity.class);
                startActivity(i);
            });
        }
        // --------------------------------

        recyclerView = findViewById(R.id.recyclerView);
        searchBar = findViewById(R.id.searchBar);

        tagAll = findViewById(R.id.tagAll);
        tagHewani = findViewById(R.id.tagHewani);
        tagNabati = findViewById(R.id.tagNabati);

        // Grid 3 kolom (sesuai tampilan kotak-kotak kamu)
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        adapter = new FoodAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // 1) Isi data (PASTIKAN drawable-nya memang ada)
        seedFoods();

        // 2) Tag klik
        tagAll.setOnClickListener(v -> setTag(TagType.ALL));
        tagHewani.setOnClickListener(v -> setTag(TagType.HEWANI));
        tagNabati.setOnClickListener(v -> setTag(TagType.NABATI));

        // 3) Search tetap jalan
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }
        });

        // default tampil semua
        setTag(TagType.ALL);
    }

    private void setTag(TagType tag) {
        selectedTag = tag;

        // reset semua jadi unselected
        tagAll.setBackgroundResource(R.drawable.bg_tag_unselected_gray);
        tagAll.setTextColor(getResources().getColor(android.R.color.white));

        tagHewani.setBackgroundResource(R.drawable.bg_tag_unselected_gray);
        tagHewani.setTextColor(getResources().getColor(android.R.color.white));

        tagNabati.setBackgroundResource(R.drawable.bg_tag_unselected_gray);
        tagNabati.setTextColor(getResources().getColor(android.R.color.white));

        // selected jadi hijau
        if (tag == TagType.ALL) {
            tagAll.setBackgroundResource(R.drawable.bg_tag_selected_green);
            tagAll.setTextColor(getResources().getColor(android.R.color.white));
        } else if (tag == TagType.HEWANI) {
            tagHewani.setBackgroundResource(R.drawable.bg_tag_selected_green);
            tagHewani.setTextColor(getResources().getColor(android.R.color.white));
        } else {
            tagNabati.setBackgroundResource(R.drawable.bg_tag_selected_green);
            tagNabati.setTextColor(getResources().getColor(android.R.color.white));
        }

        applyFilters();
    }

    private void applyFilters() {
        String q = searchBar.getText() == null ? "" : searchBar.getText().toString().trim().toLowerCase(Locale.ROOT);

        List<FoodItem> filtered = new ArrayList<>();
        for (FoodItem f : allFoods) {

            // filter tag
            if (selectedTag == TagType.HEWANI && !isHewani(f)) continue;
            if (selectedTag == TagType.NABATI && !isNabati(f)) continue;

            // filter search
            if (!q.isEmpty()) {
                String name = f.getName() == null ? "" : f.getName().toLowerCase(Locale.ROOT);
                if (!name.contains(q)) continue;
            }

            filtered.add(f);
        }

        adapter.setData(filtered);
    }

    // ==== KATEGORI (tanpa ubah FoodItem) ====
    private final Set<String> HEWANI_NAMES = new HashSet<String>() {{
        add("dada ayam");
        add("paha ayam");
        add("telur");
        add("ikan tuna");
        add("ikan salmon");
        add("dada kalkun");
        add("greek yogurt");
    }};

    private final Set<String> NABATI_NAMES = new HashSet<String>() {{
        add("tahu");
        add("tempe");
        add("brokoli");
        add("oatmeal");
        add("bayam");
        add("kacang almond");
        add("alpukat");
        add("quinoa");
        add("ubi jalar");
        add("edamame");
        add("tomat");
    }};

    private boolean isHewani(FoodItem f) {
        String n = f.getName() == null ? "" : f.getName().toLowerCase(Locale.ROOT);
        return HEWANI_NAMES.contains(n);
    }

    private boolean isNabati(FoodItem f) {
        String n = f.getName() == null ? "" : f.getName().toLowerCase(Locale.ROOT);
        return NABATI_NAMES.contains(n);
    }

    // ==== DATA ====
    private void seedFoods() {
        allFoods.clear();

        allFoods.add(new FoodItem(
                "Dada Ayam",
                "Dada ayam adalah sumber protein hewani tinggi dengan lemak dan kalori rendah. Cocok untuk diet sehat, pembentukan otot, dan menjaga berat badan. Disarankan dimasak dengan cara rebus, kukus, atau panggang agar nilai gizinya tetap optimal.",
                R.drawable.dada_ayam,
                31, 165, 3, 0,
                "• Berat bersih: 100 g\n" +
                        "• Rekomendasi penyajian: Direbus, Dikukus, Dipanggang\n" +
                        "• Penyimpanan: Simpan di tempat sejuk & kering",
                4.9f
        ));

        allFoods.add(new FoodItem(
                "Tahu",
                "Tahu adalah sumber protein nabati yang rendah kalori dan serbaguna. Cocok untuk diet sehat, menjaga massa otot, dan menu harian. Nilai gizi terbaik jika dimasak dengan cara kukus, tumis ringan, atau panggang.",
                R.drawable.placeholder_food_image,
                8, 76, 5, 2,
                "• Berat bersih contoh saji: 100 g\n" +
                        "• Rekomendasi penyajian: Dikukus, ditumis sedikit minyak, dipanggang\n" +
                        "• Penyimpanan: Simpan di kulkas dalam wadah tertutup; habiskan 2–3 hari setelah dibuka",
                4.5f
        ));

        allFoods.add(new FoodItem(
                "Tempe",
                "Tempe adalah kedelai fermentasi kaya protein dan serat, dengan profil asam amino yang baik. Cocok untuk menu tinggi protein, ramah kenyang, dan mudah diolah.",
                R.drawable.tempe,
                20, 193, 11, 8,
                "• Berat bersih contoh saji: 100 g\n" +
                        "• Rekomendasi penyajian: Dikukus, ditumis ringan, dipanggang/air fryer\n" +
                        "• Penyimpanan: Simpan dingin; dapat dibekukan untuk umur simpan lebih lama",
                4.8f
        ));

        allFoods.add(new FoodItem(
                "Telur",
                "Telur ayam adalah sumber protein hewani lengkap dengan vitamin dan mineral. Praktis, mengenyangkan, dan cocok untuk sarapan atau camilan berprotein.",
                R.drawable.telur,
                13, 155, 11, 1,
                "• Berat bersih contoh saji: 100 g (~2 butir telur ukuran sedang, tanpa cangkang)\n" +
                        "• Rekomendasi penyajian: Direbus, orak-arik tanpa banyak minyak, telur dadar tipis\n" +
                        "• Penyimpanan: Simpan telur mentah di kulkas; telur rebus utuh tahan 3–4 hari dalam kulkas",
                4.7f
        ));

        allFoods.add(new FoodItem(
                "Paha Ayam",
                "Paha ayam tanpa kulit mengandung protein tinggi dengan rasa lebih gurih dibanding dada. Cocok untuk pembentukan otot dan menu harian. Disarankan metode masak rendah minyak agar nilai gizinya optimal.",
                R.drawable.paha_ayam,
                26, 209, 11, 0,
                "• Berat bersih contoh saji: 100 g daging matang, tanpa kulit/tulang\n" +
                        "• Rekomendasi penyajian: Direbus, dikukus, dipanggang/air fryer\n" +
                        "• Penyimpanan: Simpan daging mentah di kulkas (≤2 hari) atau beku; masakan matang tahan 3–4 hari di kulkas",
                4.2f
        ));

        allFoods.add(new FoodItem(
                "Ikan Tuna",
                "Tuna tinggi protein dan rendah lemak. Cocok untuk menu diet dan pembentukan otot.",
                R.drawable.tuna,
                29, 132, 1, 0,
                "• Berat bersih contoh saji: 100 g\n" +
                        "• Rekomendasi: Panggang/air fryer/kukus\n" +
                        "• Penyimpanan: Simpan dingin, konsumsi 1–2 hari",
                4.8f
        ));

        allFoods.add(new FoodItem(
                "Brokoli",
                "Brokoli kaya serat dan mikronutrien. Cocok untuk pendamping menu rendah kalori.",
                R.drawable.brokoli,
                3, 34, 0, 7,
                "• Berat bersih contoh saji: 100 g\n" +
                        "• Rekomendasi: Kukus/tumis ringan\n" +
                        "• Penyimpanan: Simpan di kulkas 3–5 hari",
                5.0f
        ));

        allFoods.add(new FoodItem(
                "Oatmeal",
                "Oatmeal sumber karbohidrat kompleks dan serat. Cocok untuk sarapan kenyang lebih lama.",
                R.drawable.oatmeal,
                13, 379, 7, 67,
                "• Berat bersih contoh saji: 40 g (kering)\n" +
                        "• Rekomendasi: Seduh air panas/susu rendah lemak\n" +
                        "• Penyimpanan: Simpan kering & tertutup rapat",
                4.6f
        ));

        allFoods.add(new FoodItem(
                "Bayam",
                "Sayuran hijau padat nutrisi yang kaya akan zat besi, kalsium, dan vitamin K. Sangat baik untuk kesehatan tulang dan sirkulasi darah.",
                R.drawable.bayam,
                3, 23, 0, 4,
                "• Berat bersih contoh saji: 100 g\n" +
                        "• Rekomendasi penyajian: Dikukus, direbus sebentar, tumis ringan\n" +
                        "• Penyimpanan: Simpan di kulkas dalam wadah tertutup",
                5.0f
        ));

        allFoods.add(new FoodItem(
                "Ikan Salmon",
                "Ikan berlemak yang kaya akan asam lemak omega-3 kualitas tinggi, sangat baik untuk kesehatan otak dan jantung.",
                R.drawable.salmon,
                20, 208, 13, 0,
                "• Berat bersih contoh saji: 100 g\n" +
                        "• Rekomendasi penyajian: Dipanggang, dikukus, pan-sear\n" +
                        "• Penyimpanan: Simpan di kulkas/freezer",
                4.9f
        ));

        allFoods.add(new FoodItem(
                "Kacang Almond",
                "Kacang tinggi lemak sehat, vitamin E, dan serat. Sangat mengenyangkan dan cocok untuk camilan sehat dalam porsi terkontrol.",
                R.drawable.almond,
                21, 579, 50, 22,
                "• Berat bersih contoh saji: 30 g (1 genggam kecil)\n" +
                        "• Rekomendasi penyajian: Dimakan langsung, dipanggang\n" +
                        "• Penyimpanan: Simpan di suhu ruang tertutup rapat",
                4.7f
        ));

        allFoods.add(new FoodItem(
                "Alpukat",
                "Buah unik tinggi lemak tak jenuh ganda yang menyehatkan jantung. Membantu penyerapan vitamin dari sayuran lain.",
                R.drawable.alpukat,
                2, 160, 15, 9,
                "• Berat bersih contoh saji: 100 g\n" +
                        "• Rekomendasi penyajian: Dimakan langsung, campuran salad\n" +
                        "• Penyimpanan: Suhu ruang hingga matang, lalu kulkas",
                4.8f
        ));

        allFoods.add(new FoodItem(
                "Dada Kalkun",
                "Alternatif unggas selain ayam, dengan protein sangat tinggi dan nyaris tanpa lemak. Ideal untuk pembentukan massa otot.",
                R.drawable.kalkun,
                30, 135, 1, 0,
                "• Berat bersih contoh saji: 100 g\n" +
                        "• Rekomendasi penyajian: Dipanggang, direbus\n" +
                        "• Penyimpanan: Simpan di kulkas/freezer",
                4.9f
        ));

        allFoods.add(new FoodItem(
                "Quinoa",
                "Biji-bijian utuh bebas gluten yang unik karena mengandung semua asam amino esensial. Pengganti nasi putih yang luar biasa.",
                R.drawable.quinoa,
                4, 120, 2, 21,
                "• Berat bersih contoh saji: 100 g (matang)\n" +
                        "• Rekomendasi penyajian: Direbus seperti nasi\n" +
                        "• Penyimpanan: Simpan di kulkas (matang)",
                4.8f
        ));

        allFoods.add(new FoodItem(
                "Ubi Jalar",
                "Sumber karbohidrat kompleks dengan indeks glikemik rendah. Sangat tinggi vitamin A dan serat, lambat dicerna sehingga awet kenyang.",
                R.drawable.ubi_jalar,
                2, 86, 0, 20,
                "• Berat bersih contoh saji: 100 g\n" +
                        "• Rekomendasi penyajian: Dikukus, dipanggang utuh\n" +
                        "• Penyimpanan: Tempat sejuk dan kering",
                4.8f
        ));

        allFoods.add(new FoodItem(
                "Greek Yogurt",
                "Produk susu fermentasi yang disaring sehingga proteinnya tinggi. Mengandung probiotik alami untuk kesehatan pencernaan.",
                R.drawable.greek_yogurt,
                10, 59, 0, 3,
                "• Berat bersih contoh saji: 100 g\n" +
                        "• Rekomendasi penyajian: Dikonsumsi langsung, dicampur buah\n" +
                        "• Penyimpanan: Simpan di kulkas",
                4.8f
        ));

        allFoods.add(new FoodItem(
                "Edamame",
                "Kedelai muda utuh yang kaya protein nabati dan serat tinggi. Sangat populer sebagai camilan sehat penunda lapar.",
                R.drawable.edamame,
                11, 121, 5, 11,
                "• Berat bersih contoh saji: 100 g\n" +
                        "• Rekomendasi penyajian: Direbus sebentar dengan sedikit garam laut\n" +
                        "• Penyimpanan: Simpan di kulkas/freezer",
                4.8f
        ));

        allFoods.add(new FoodItem(
                "Tomat",
                "Buah/sayur berkalori sangat rendah, kaya akan likopen (antioksidan pelindung sel) dan vitamin C.",
                R.drawable.tomat,
                1, 18, 0, 4,
                "• Berat bersih contoh saji: 100 g\n" +
                        "• Rekomendasi penyajian: Dimakan langsung, salad, dimasak\n" +
                        "• Penyimpanan: Suhu ruang (sebelum dipotong)",
                4.9f
        ));
    }
}