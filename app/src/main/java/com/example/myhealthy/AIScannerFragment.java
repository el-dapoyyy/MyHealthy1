package com.example.myhealthy;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AIScannerFragment extends Fragment {

    private static final String TAG = "AIScannerFragment";
    private static final int REQUEST_CAMERA = 100;
    private static final int REQUEST_GALLERY = 101;
    private static final int REQUEST_CAMERA_PERMISSION = 102;

    private ImageView ivFoodPhoto;
    private LinearLayout placeholderContainer, resultCard;
    private ProgressBar progressAnalyzing;
    private TextView tvScannerStatus, tvSource;
    private EditText etName, etCalories, etProtein, etCarbs, etFat;
    private Spinner spinnerMealType;

    private Bitmap capturedBitmap;
    private final String[] MEAL_TYPES = {"Sarapan", "Makan Siang", "Makan Malam", "Snack"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ai_scanner, container, false);

        ivFoodPhoto = view.findViewById(R.id.ivFoodPhoto);
        placeholderContainer = view.findViewById(R.id.placeholderContainer);
        resultCard = view.findViewById(R.id.resultCard);
        progressAnalyzing = view.findViewById(R.id.progressAnalyzing);
        tvScannerStatus = view.findViewById(R.id.tvScannerStatus);
        tvSource = view.findViewById(R.id.tvSource);
        etName = view.findViewById(R.id.etResultName);
        etCalories = view.findViewById(R.id.etResultCalories);
        etProtein = view.findViewById(R.id.etResultProtein);
        etCarbs = view.findViewById(R.id.etResultCarbs);
        etFat = view.findViewById(R.id.etResultFat);
        spinnerMealType = view.findViewById(R.id.spinnerMealType);

        // Meal type spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_dropdown_item, MEAL_TYPES);
        spinnerMealType.setAdapter(adapter);

        // Auto-select meal type based on time
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        if (hour < 10) spinnerMealType.setSelection(0);
        else if (hour < 15) spinnerMealType.setSelection(1);
        else if (hour < 20) spinnerMealType.setSelection(2);
        else spinnerMealType.setSelection(3);

        // Status — check API key
        String apiKey = BuildConfig.GEMINI_API_KEY;
        if (apiKey != null && !apiKey.isEmpty()) {
            tvScannerStatus.setText("⚡ Gemini AI Ready");
            Log.d(TAG, "API Key loaded (length=" + apiKey.length() + ")");
        } else {
            tvScannerStatus.setText("⚠️ API Key belum diset");
            Log.e(TAG, "GEMINI_API_KEY is empty! Check local.properties");
        }

        // Buttons
        view.findViewById(R.id.btnCamera).setOnClickListener(v -> openCamera());
        view.findViewById(R.id.btnGallery).setOnClickListener(v -> openGallery());
        view.findViewById(R.id.btnSaveToDiary).setOnClickListener(v -> saveToDiary());

        return view;
    }

    // ═══════════════════════════════════════════════════════
    // CAMERA & GALLERY
    // ═══════════════════════════════════════════════════════

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            return;
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(requireContext(), "Izin kamera diperlukan", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK || data == null) return;

        try {
            if (requestCode == REQUEST_CAMERA) {
                capturedBitmap = (Bitmap) data.getExtras().get("data");
                Log.d(TAG, "Camera bitmap received");
            } else if (requestCode == REQUEST_GALLERY) {
                Uri imageUri = data.getData();
                if (imageUri != null) {
                    InputStream is = requireContext().getContentResolver().openInputStream(imageUri);
                    capturedBitmap = BitmapFactory.decodeStream(is);
                    if (is != null) is.close();
                    Log.d(TAG, "Gallery bitmap loaded");
                }
            }

            if (capturedBitmap != null) {
                // Scale bitmap to reduce payload size
                capturedBitmap = scaleBitmap(capturedBitmap, 768);
                Log.d(TAG, "Bitmap scaled: " + capturedBitmap.getWidth() + "x" + capturedBitmap.getHeight());

                ivFoodPhoto.setImageBitmap(capturedBitmap);
                ivFoodPhoto.setVisibility(View.VISIBLE);
                placeholderContainer.setVisibility(View.GONE);
                analyzeWithGemini(capturedBitmap);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading image", e);
            Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // ═══════════════════════════════════════════════════════
    // GEMINI REST API (v1beta)
    // ═══════════════════════════════════════════════════════

    private void analyzeWithGemini(Bitmap bitmap) {
        String apiKey = BuildConfig.GEMINI_API_KEY;
        if (apiKey == null || apiKey.isEmpty()) {
            Toast.makeText(requireContext(), "API Key belum diset di local.properties", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Cannot call Gemini — API key is missing");
            return;
        }

        // Show loading state
        progressAnalyzing.setVisibility(View.VISIBLE);
        resultCard.setVisibility(View.GONE);
        tvScannerStatus.setText("🔄 Menganalisis makanan...");

        // Convert bitmap to Base64 JPEG
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos);
        byte[] imageBytes = baos.toByteArray();
        String base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
        Log.d(TAG, "Image encoded: " + imageBytes.length + " bytes, base64 length: " + base64Image.length());

        // Background thread for network call
        new Thread(() -> {
            try {
                String urlStr = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-lite-latest:generateContent?key=" + apiKey;
                Log.d(TAG, "Calling Gemini API...");

                // ── Build request JSON ──────────────────────────────

                // Image part
                JSONObject inlineData = new JSONObject();
                inlineData.put("mime_type", "image/jpeg");
                inlineData.put("data", base64Image);

                JSONObject imagePart = new JSONObject();
                imagePart.put("inline_data", inlineData);

            // 1. Text prompt — intentionally strict
            JSONObject textPart = new JSONObject();
            textPart.put("text",
                    "Kamu adalah ahli gizi. Analisis foto makanan ini dengan cermat.\n\n" +
                    "Tugas:\n" +
                    "1. Identifikasi nama makanan dalam Bahasa Indonesia\n" +
                    "2. Estimasi porsi yang terlihat\n" +
                    "3. PERKIRAKAN kandungan nutrisi dari porsi tersebut (Kalori, Protein, Karbohidrat, Lemak).\n\n" +
                    "SYARAT WAJIB OUTPUT JSON:\n" +
                    "- Kalori wajib integer (angka saja, tanpa 'kkal').\n" +
                    "- Protein, Karbohidrat, Lemak wajib bernilai decimal/float (angka saja, TANPA tulisan 'g' atau 'gram'). WAJIB DIPERKIRAKAN, JANGAN DIKOSONGKAN!\n" +
                    "- Jika bukan makanan, kembalikan: {\"error\": \"Bukan makanan\"}\n\n" +
                    "Format yang benar:\n" +
                    "{\"nama\": \"Nasi Goreng\", \"kalori\": 450, \"protein\": 15.5, \"karbohidrat\": 55.0, \"lemak\": 12.0}");

            // Contents array
            JSONArray parts = new JSONArray();
            parts.put(imagePart);
            parts.put(textPart);

            JSONObject content = new JSONObject();
            content.put("parts", parts);

            JSONArray contents = new JSONArray();
            contents.put(content);

            // Generation config
            JSONObject generationConfig = new JSONObject();
            generationConfig.put("responseMimeType", "application/json");
            generationConfig.put("temperature", 0.4); // slightly higher temp for guessing macros

            // Final request body
            JSONObject requestBody = new JSONObject();
            requestBody.put("contents", contents);
            requestBody.put("generationConfig", generationConfig);

            Log.d(TAG, "Request body built successfully");

            // ── Send HTTP POST ──────────────────────────────────

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(60000); // Gemini can be slow with images

            OutputStream os = conn.getOutputStream();
            os.write(requestBody.toString().getBytes("UTF-8"));
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            Log.d(TAG, "Response code: " + responseCode);

            // Read response
            InputStream inputStream = (responseCode >= 200 && responseCode < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();
            conn.disconnect();

            String responseStr = sb.toString();
            Log.d(TAG, "Raw response: " + responseStr);

            // ── Handle response ─────────────────────────────────

            if (responseCode >= 200 && responseCode < 300) {
                // SUCCESS — extract text from Gemini response
                JSONObject response = new JSONObject(responseStr);
                JSONArray candidates = response.optJSONArray("candidates");

                if (candidates == null || candidates.length() == 0) {
                    postToUI(() -> showError("Gemini tidak mengembalikan hasil (mungkin safety filter)."));
                    return;
                }

                String text = candidates
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text");

                Log.d(TAG, "Gemini text: " + text);
                postToUI(() -> parseAndShowResult(text));

            } else {
                // ERROR response from API
                String errorMsg = "Error HTTP " + responseCode;
                try {
                    JSONObject errObj = new JSONObject(responseStr).optJSONObject("error");
                    if (errObj != null) errorMsg = errObj.optString("message", errorMsg);
                } catch (Exception ignored) {}
                String finalMsg = errorMsg;
                postToUI(() -> showError(finalMsg));
            }

        } catch (Exception e) {
            Log.e(TAG, "Error calling Gemini", e);
            postToUI(() -> showError("Error: " + e.getMessage()));
        }
    }).start();
}

// ═══════════════════════════════════════════════════════
// PARSE & DISPLAY RESULT
// ═══════════════════════════════════════════════════════

private double parseLenientMacro(JSONObject json, String key, String key2) {
    try {
        if (json.has(key)) {
            Object val = json.opt(key);
            if (val instanceof Number) return ((Number) val).doubleValue();
            if (val instanceof String) return Double.parseDouble(val.toString().replaceAll("[^0-9.]", ""));
        }
        if (json.has(key2)) {
            Object val = json.opt(key2);
            if (val instanceof Number) return ((Number) val).doubleValue();
            if (val instanceof String) return Double.parseDouble(val.toString().replaceAll("[^0-9.]", ""));
        }
    } catch (Exception e) {
        Log.w(TAG, "Gagal memparsing makro: " + e.getMessage());
    }
    return 0.0;
}

private void parseAndShowResult(String text) {
    progressAnalyzing.setVisibility(View.GONE);
    tvScannerStatus.setText("⚡ Gemini AI Ready");

    try {
        String cleaned = text.trim();
        cleaned = cleaned.replaceAll("^```(?:json)?\\s*", "");
        cleaned = cleaned.replaceAll("\\s*```$", "");

        JSONObject json;
        try {
            json = new JSONObject(cleaned);
        } catch (Exception e) {
            Matcher matcher = Pattern.compile("\\{[^{}]*\\}", Pattern.DOTALL).matcher(text);
            if (matcher.find()) json = new JSONObject(matcher.group());
            else throw new Exception("Tidak ditemukan JSON");
        }

        if (json.has("error")) {
            Toast.makeText(requireContext(), "⚠️ " + json.getString("error"), Toast.LENGTH_LONG).show();
            ivFoodPhoto.setVisibility(View.GONE);
            placeholderContainer.setVisibility(View.VISIBLE);
            return;
        }

        String nama = json.optString("nama", "Tidak dikenali").trim();
        
        resultCard.setVisibility(View.VISIBLE);
        tvSource.setText("⚡ via Gemini AI");
        etName.setText(nama);
        
        // Calories
        int calories = json.optInt("kalori", 0);
        if (calories == 0) calories = json.optInt("calories", 0);
        etCalories.setText(String.valueOf(calories));

        // Macros
        double pro = parseLenientMacro(json, "protein", "proteins");
        double car = parseLenientMacro(json, "karbohidrat", "carbs");
        double fat = parseLenientMacro(json, "lemak", "fat");

        etProtein.setText(String.format(Locale.US, "%.1f", pro));
        etCarbs.setText(String.format(Locale.US, "%.1f", car));
        etFat.setText(String.format(Locale.US, "%.1f", fat));

    } catch (Exception e) {
        Log.e(TAG, "JSON parse error", e);
        Toast.makeText(requireContext(), "⚠️ Gagal membaca hasil analisis.", Toast.LENGTH_LONG).show();
    }
}

    // ═══════════════════════════════════════════════════════
    // ERROR HANDLING HELPER
    // ═══════════════════════════════════════════════════════

    private void showError(String message) {
        progressAnalyzing.setVisibility(View.GONE);
        tvScannerStatus.setText("⚡ Gemini AI Ready");
        Toast.makeText(requireContext(), "❌ " + message, Toast.LENGTH_LONG).show();
    }

    /**
     * Safely post a Runnable to the UI thread, only if the fragment is still attached.
     */
    private void postToUI(Runnable action) {
        if (isAdded() && getActivity() != null) {
            requireActivity().runOnUiThread(() -> {
                // Double-check after posting to main thread
                if (isAdded()) {
                    action.run();
                }
            });
        }
    }

    // ═══════════════════════════════════════════════════════
    // UTILS
    // ═══════════════════════════════════════════════════════

    private Bitmap scaleBitmap(Bitmap original, int maxSize) {
        int w = original.getWidth(), h = original.getHeight();
        if (w <= maxSize && h <= maxSize) return original;
        float ratio = Math.min((float) maxSize / w, (float) maxSize / h);
        return Bitmap.createScaledBitmap(original,
                Math.round(w * ratio), Math.round(h * ratio), true);
    }

    // ═══════════════════════════════════════════════════════
    // SAVE TO DIARY
    // ═══════════════════════════════════════════════════════

    private void saveToDiary() {
        String name = etName.getText().toString().trim();
        String calStr = etCalories.getText().toString().trim();

        if (name.isEmpty() || calStr.isEmpty()) {
            Toast.makeText(requireContext(), "Nama dan kalori harus diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(), "Login diperlukan", Toast.LENGTH_SHORT).show();
            return;
        }

        String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());

        Map<String, Object> entry = new HashMap<>();
        entry.put("foodName", name);
        entry.put("calories", Integer.parseInt(calStr));
        entry.put("protein", parseDouble(etProtein.getText().toString()));
        entry.put("carbs", parseDouble(etCarbs.getText().toString()));
        entry.put("fat", parseDouble(etFat.getText().toString()));
        entry.put("mealType", MEAL_TYPES[spinnerMealType.getSelectedItemPosition()]);
        entry.put("date", todayStr);
        entry.put("source", "ai_scanner");
        entry.put("timestamp", FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance()
                .collection("users").document(user.getUid())
                .collection("diary")
                .add(entry)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(requireContext(),
                            "✅ " + name + " disimpan!", Toast.LENGTH_SHORT).show();
                    resultCard.setVisibility(View.GONE);
                    ivFoodPhoto.setVisibility(View.GONE);
                    placeholderContainer.setVisibility(View.VISIBLE);
                    capturedBitmap = null;
                    Log.d(TAG, "Diary entry saved: " + docRef.getId());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(),
                            "Gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Failed to save diary", e);
                });
    }

    private double parseDouble(String s) {
        try { return Double.parseDouble(s); }
        catch (Exception e) { return 0; }
    }
}
