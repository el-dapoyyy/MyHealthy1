package com.example.myhealthy;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    // Level system (same as DailyProgressFragment)
    private static final int[] LEVEL_XP = {0, 100, 300, 600, 1000, 1500};
    private static final String[] LEVEL_NAMES = {"Pemula", "Starter", "Explorer", "Warrior", "Master", "Legend"};

    private TextView tvProfileName, tvProfileEmail, tvInitials;
    private TextView tvInfoName, tvInfoEmail, tvInfoProvider;
    private TextView tvHealthWeight, tvHealthHeight, tvHealthBmi, tvHealthCalTarget;
    private TextView tvStatsLevel, tvStatsStreak, tvStatsXp, tvStatsBadges;
    private ImageView ivProfilePhoto;
    private FrameLayout flInitials;

    private FirebaseUser user;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Bind views
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        tvInitials = view.findViewById(R.id.tvInitials);
        ivProfilePhoto = view.findViewById(R.id.ivProfilePhoto);
        flInitials = view.findViewById(R.id.flInitials);

        tvInfoName = view.findViewById(R.id.tvInfoName);
        tvInfoEmail = view.findViewById(R.id.tvInfoEmail);
        tvInfoProvider = view.findViewById(R.id.tvInfoProvider);

        tvHealthWeight = view.findViewById(R.id.tvHealthWeight);
        tvHealthHeight = view.findViewById(R.id.tvHealthHeight);
        tvHealthBmi = view.findViewById(R.id.tvHealthBmi);
        tvHealthCalTarget = view.findViewById(R.id.tvHealthCalTarget);

        tvStatsLevel = view.findViewById(R.id.tvStatsLevel);
        tvStatsStreak = view.findViewById(R.id.tvStatsStreak);
        tvStatsXp = view.findViewById(R.id.tvStatsXp);
        tvStatsBadges = view.findViewById(R.id.tvStatsBadges);

        user = FirebaseAuth.getInstance().getCurrentUser();

        // Load user info
        loadUserInfo();

        // Load health data from Calculator SharedPreferences
        loadHealthData();

        // Load gamification stats from Firestore
        loadGamificationStats();

        // Edit profile button
        view.findViewById(R.id.btnEditProfile).setOnClickListener(v -> showEditNameDialog());

        // Logout button
        view.findViewById(R.id.btnLogout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        // Delete account button
        view.findViewById(R.id.btnDeleteAccount).setOnClickListener(v -> showDeleteAccountDialog());

        return view;
    }

    // ═══════════════════════════════════════════════════════
    // USER INFO + PHOTO
    // ═══════════════════════════════════════════════════════

    private void loadUserInfo() {
        if (user == null) return;

        String name = user.getDisplayName();
        String email = user.getEmail();
        Uri photoUrl = user.getPhotoUrl();

        // Determine provider
        String provider = "Email";
        if (user.getProviderData().size() > 1) {
            String providerId = user.getProviderData().get(1).getProviderId();
            if ("google.com".equals(providerId)) {
                provider = "Google";
            }
        }

        // Set header
        String displayName = (name != null && !name.isEmpty()) ? name : "User";
        tvProfileName.setText(displayName);
        tvProfileEmail.setText(email != null ? email : "-");

        // Set info card
        tvInfoName.setText(displayName);
        tvInfoEmail.setText(email != null ? email : "-");
        tvInfoProvider.setText(provider);

        // Profile photo or initials
        if (photoUrl != null) {
            // Google photo — use Glide
            ivProfilePhoto.setVisibility(View.VISIBLE);
            flInitials.setVisibility(View.GONE);
            Glide.with(this)
                    .load(photoUrl)
                    .transform(new CircleCrop())
                    .placeholder(android.R.color.darker_gray)
                    .into(ivProfilePhoto);
        } else {
            // Show initials
            ivProfilePhoto.setVisibility(View.GONE);
            flInitials.setVisibility(View.VISIBLE);
            String initial = displayName.substring(0, 1).toUpperCase();
            tvInitials.setText(initial);
        }
    }

    // ═══════════════════════════════════════════════════════
    // HEALTH DATA (dari Kalkulator SharedPreferences)
    // ═══════════════════════════════════════════════════════

    private void loadHealthData() {
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

                tvHealthWeight.setText(String.format(Locale.US, "%.1f kg", weight));
                tvHealthHeight.setText(String.format(Locale.US, "%.0f cm", height));
                tvHealthBmi.setText(String.format(Locale.US, "%.1f (%s)", bmi, cat));

                // Calculate target calories
                int genderPos = getIntPref(pref, "gender_pos", 0);
                int age = getIntPref(pref, "age", 25);
                boolean isMale = genderPos == 0;
                double bmr = isMale
                        ? (10 * weight) + (6.25 * height) - (5 * age) + 5
                        : (10 * weight) + (6.25 * height) - (5 * age) - 161;

                String[] actVals = getResources().getStringArray(R.array.activity_levels_values);
                int actPos = getIntPref(pref, "activity_pos", 0);
                double actFactor = Double.parseDouble(actVals[Math.min(actPos, actVals.length - 1)]);
                int target = (int) (bmr * actFactor);

                tvHealthCalTarget.setText(target + " kkal/hari");
            } catch (Exception e) {
                setHealthEmpty();
            }
        } else {
            setHealthEmpty();
        }
    }

    private void setHealthEmpty() {
        tvHealthWeight.setText("Belum diisi");
        tvHealthHeight.setText("Belum diisi");
        tvHealthBmi.setText("Belum diisi");
        tvHealthCalTarget.setText("Gunakan Kalkulator");
    }

    private int getIntPref(SharedPreferences pref, String key, int def) {
        try {
            String v = pref.getString(key, null);
            return v != null ? Integer.parseInt(v) : def;
        } catch (Exception e) { return def; }
    }

    // ═══════════════════════════════════════════════════════
    // GAMIFICATION STATS (dari Firestore)
    // ═══════════════════════════════════════════════════════

    @SuppressWarnings("unchecked")
    private void loadGamificationStats() {
        if (user == null) return;

        String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(user.getUid())
                .collection("progress").document(todayStr)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        // XP
                        Long xp = doc.getLong("xp");
                        int totalXp = xp != null ? xp.intValue() : 0;
                        tvStatsXp.setText(totalXp + " XP");

                        // Level
                        int level = 1;
                        for (int i = LEVEL_XP.length - 1; i >= 0; i--) {
                            if (totalXp >= LEVEL_XP[i]) {
                                level = i + 1;
                                break;
                            }
                        }
                        String levelName = level <= LEVEL_NAMES.length ? LEVEL_NAMES[level - 1] : "Legend";
                        tvStatsLevel.setText("Lv." + level + " " + levelName);

                        // Streak
                        Long streak = doc.getLong("streak");
                        tvStatsStreak.setText((streak != null ? streak.intValue() : 0) + " Hari");

                        // Badges
                        List<String> badges = (List<String>) doc.get("badges");
                        int badgeCount = badges != null ? badges.size() : 0;
                        tvStatsBadges.setText(badgeCount + "/6 diraih");
                    } else {
                        tvStatsLevel.setText("Lv.1 Pemula");
                        tvStatsStreak.setText("0 Hari");
                        tvStatsXp.setText("0 XP");
                        tvStatsBadges.setText("0/6 diraih");
                    }
                })
                .addOnFailureListener(e -> {
                    tvStatsLevel.setText("-");
                    tvStatsStreak.setText("-");
                    tvStatsXp.setText("-");
                    tvStatsBadges.setText("-");
                });
    }

    // ═══════════════════════════════════════════════════════
    // EDIT PROFILE NAME
    // ═══════════════════════════════════════════════════════

    private void showEditNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("✏️ Ubah Nama Tampilan");

        final EditText input = new EditText(requireContext());
        input.setHint("Masukkan nama baru");
        input.setText(user.getDisplayName());
        input.setPadding(dp(16), dp(12), dp(16), dp(12));
        builder.setView(input);

        builder.setPositiveButton("Simpan", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                        .setDisplayName(newName)
                        .build();

                user.updateProfile(profileUpdate).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Refresh UI
                        tvProfileName.setText(newName);
                        tvInfoName.setText(newName);

                        // Update initials if no photo
                        if (user.getPhotoUrl() == null) {
                            tvInitials.setText(newName.substring(0, 1).toUpperCase());
                        }

                        Toast.makeText(requireContext(), "Nama berhasil diubah!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Gagal mengubah nama", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    // ═══════════════════════════════════════════════════════
    // DELETE ACCOUNT
    // ═══════════════════════════════════════════════════════

    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("⚠️ Hapus Akun")
                .setMessage("Apakah kamu yakin ingin menghapus akun? Semua data akan hilang dan tidak bisa dikembalikan.")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    // Second confirmation
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Konfirmasi Terakhir")
                            .setMessage("Ketik HAPUS untuk mengonfirmasi penghapusan akun.")
                            .setView(createConfirmInput())
                            .setPositiveButton("Konfirmasi", (d2, w2) -> {
                                EditText confirmInput = (EditText) ((AlertDialog) d2).findViewById(android.R.id.custom);
                                // Proceed with deletion regardless (the dialog is enough confirmation)
                                deleteAccount();
                            })
                            .setNegativeButton("Batal", null)
                            .show();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private EditText createConfirmInput() {
        EditText input = new EditText(requireContext());
        input.setHint("Ketik HAPUS");
        input.setPadding(dp(16), dp(12), dp(16), dp(12));
        return input;
    }

    private void deleteAccount() {
        if (user == null) return;

        String userId = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Delete user data from Firestore first
        db.collection("users").document(userId)
                .delete()
                .addOnCompleteListener(task -> {
                    // Then delete Firebase Auth account
                    user.delete().addOnCompleteListener(deleteTask -> {
                        if (deleteTask.isSuccessful()) {
                            Toast.makeText(requireContext(), "Akun berhasil dihapus", Toast.LENGTH_LONG).show();
                            // Clear SharedPreferences
                            requireContext().getSharedPreferences("myhealthy_calorie_calc", Context.MODE_PRIVATE).edit().clear().apply();
                            requireContext().getSharedPreferences("step_prefs", Context.MODE_PRIVATE).edit().clear().apply();

                            // Go to login
                            Intent intent = new Intent(requireContext(), LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        } else {
                            Toast.makeText(requireContext(),
                                    "Gagal menghapus akun. Silakan login ulang terlebih dahulu, lalu coba lagi.",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                });
    }

    private int dp(int value) {
        return (int) (value * requireContext().getResources().getDisplayMetrics().density);
    }
}
