package com.example.myhealthy;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.InputStream;

public class MainNavActivity extends AppCompatActivity {

    private boolean isMenuOpen = false;
    private String currentFragmentTag = "";
    private FrameLayout fragmentContainer;
    private BottomNavigationView bottomNav;
    private FrameLayout overlayDim;
    private LinearLayout scannerMenuContainer;
    private FloatingActionButton fabScanner;
    
    private int previousTabId = R.id.nav_home;
    private int currentTabId = R.id.nav_home;

    private static final int REQUEST_CAMERA = 100;
    private static final int REQUEST_GALLERY = 101;
    private static final int REQUEST_CAMERA_PERMISSION = 102;

    // Temporary storage to pass to AIScannerFragment without crashing Bundle size limits
    public static Bitmap pendingScannerBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_nav);

        fragmentContainer = findViewById(R.id.fragment_container);
        bottomNav = findViewById(R.id.bottom_nav);
        overlayDim = findViewById(R.id.overlayDim);
        scannerMenuContainer = findViewById(R.id.scannerMenuContainer);
        fabScanner = findViewById(R.id.fab_scanner);

        LinearLayout btnAiScan = findViewById(R.id.btnOverlayAiScan);
        LinearLayout btnGallery = findViewById(R.id.btnOverlayGallery);
        LinearLayout btnSavedMeals = findViewById(R.id.btnOverlaySavedMeals);

        // The slide-up layout measurement has been moved inside toggleMenu() to ensure correct height.

        // Default fragment
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment(), "home");
            currentFragmentTag = "home";
        }

        bottomNav.setOnItemSelectedListener(item -> {
            if (isMenuOpen) toggleMenu(); // Close overlay if user taps bottom nav

            Fragment fragment = null;
            String tag = "";
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                tag = "home";
                fragment = new HomeFragment();
            } else if (id == R.id.nav_calculator) {
                tag = "calculator";
                fragment = new CalculatorFragment();
            } else if (id == R.id.nav_dummy) {
                // Do nothing, handled by FAB
                return false;
            } else if (id == R.id.nav_daily_progress) {
                tag = "daily_progress";
                fragment = new DailyProgressFragment();
            } else if (id == R.id.nav_profile) {
                tag = "profile";
                fragment = new ProfileFragment();
            }

            // Track history so back button works correctly
            if (id != R.id.nav_dummy) {
                if (currentTabId != id) {
                    previousTabId = currentTabId;
                    currentTabId = id;
                }
            } else {
                // If it's dummy (Scanner/Diary), don't update previousTabId so we can go back to it.
                currentTabId = id;
            }

            // Skip if same tab or activity is finishing
            if (fragment != null && !tag.equals(currentFragmentTag)
                    && !isFinishing() && !isDestroyed()) {
                currentFragmentTag = tag;
                loadFragment(fragment, tag);
            }
            return true;
        });

        // Prevent clicking the empty dummy space
        bottomNav.setItemActiveIndicatorEnabled(false);

        fabScanner.setOnClickListener(v -> toggleMenu());
        overlayDim.setOnClickListener(v -> {
            if (isMenuOpen) toggleMenu();
        });

        // Popup Actions
        btnAiScan.setOnClickListener(v -> {
            toggleMenu();
            openCamera();
        });

        btnGallery.setOnClickListener(v -> {
            toggleMenu();
            openGallery();
        });

        btnSavedMeals.setOnClickListener(v -> {
            toggleMenu();
            currentFragmentTag = "food_diary";
            loadFragment(new FoodDiaryFragment(), "food_diary");
            bottomNav.setSelectedItemId(R.id.nav_dummy);
        });
    }

    private void toggleMenu() {
        isMenuOpen = !isMenuOpen;

        if (isMenuOpen) {
            // Open animation
            overlayDim.setVisibility(View.VISIBLE);
            overlayDim.setAlpha(0f);
            overlayDim.animate().alpha(1f).setDuration(300).setListener(null).start();

            scannerMenuContainer.setVisibility(View.VISIBLE);
            if (scannerMenuContainer.getHeight() == 0) {
                scannerMenuContainer.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        scannerMenuContainer.getViewTreeObserver().removeOnPreDrawListener(this);
                        scannerMenuContainer.setTranslationY(scannerMenuContainer.getHeight());
                        scannerMenuContainer.animate().translationY(0).setDuration(300).setListener(null).start();
                        return true;
                    }
                });
            } else {
                scannerMenuContainer.animate().translationY(0).setDuration(300).setListener(null).start();
            }

            fabScanner.animate().rotation(45f).setDuration(300).start();

            // Hardware Accelerated Glassmorphism Blur (Android 12+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                fragmentContainer.setRenderEffect(RenderEffect.createBlurEffect(20f, 20f, Shader.TileMode.CLAMP));
            }
        } else {
            // Close animation
            overlayDim.animate().alpha(0f).setDuration(300).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    overlayDim.setVisibility(View.GONE);
                }
            }).start();

            scannerMenuContainer.animate().translationY(scannerMenuContainer.getHeight()).setDuration(300).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    scannerMenuContainer.setVisibility(View.GONE);
                }
            }).start();

            fabScanner.animate().rotation(0f).setDuration(300).start();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                fragmentContainer.setRenderEffect(null);
            }
        }
    }

    private void loadFragment(Fragment fragment, String tag) {
        if (isFinishing() || isDestroyed()) return;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment, tag)
                .commitAllowingStateLoss();
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Izin kamera diperlukan untuk scan", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK || data == null) {
            // User cancelled. Re-open the popup menu!
            if (!isMenuOpen) {
                toggleMenu();
            }
            return;
        }

        try {
            pendingScannerBitmap = null;

            if (requestCode == REQUEST_CAMERA) {
                pendingScannerBitmap = (Bitmap) data.getExtras().get("data");
            } else if (requestCode == REQUEST_GALLERY) {
                Uri imageUri = data.getData();
                if (imageUri != null) {
                    InputStream is = getContentResolver().openInputStream(imageUri);
                    pendingScannerBitmap = BitmapFactory.decodeStream(is);
                    if (is != null) is.close();
                }
            }

            if (pendingScannerBitmap != null) {
                // Success! Now we finally load the UI and pass the image.
                Fragment fragment = new AIScannerFragment();
                Bundle args = new Bundle();
                args.putBoolean("HAS_PENDING_BITMAP", true);
                fragment.setArguments(args);
                currentFragmentTag = "ai_scanner";
                loadFragment(fragment, "ai_scanner");
                bottomNav.setSelectedItemId(R.id.nav_dummy);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Gagal memproses gambar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void goBackToPreviousTab() {
        if (previousTabId != 0 && previousTabId != R.id.nav_dummy) {
            bottomNav.setSelectedItemId(previousTabId);
        } else {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }
}
