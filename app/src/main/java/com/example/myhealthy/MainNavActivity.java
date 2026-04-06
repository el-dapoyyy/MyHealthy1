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

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainNavActivity extends AppCompatActivity {

    private boolean isMenuOpen = false;
    private FrameLayout fragmentContainer;
    private BottomNavigationView bottomNav;
    private FrameLayout overlayDim;
    private LinearLayout scannerMenuContainer;
    private FloatingActionButton fabScanner;

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

        // Pre-measure the menu container height for the slide-up animation
        scannerMenuContainer.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                scannerMenuContainer.getViewTreeObserver().removeOnPreDrawListener(this);
                // Move it down completely immediately
                scannerMenuContainer.setTranslationY(scannerMenuContainer.getHeight());
                return true;
            }
        });

        // Default fragment
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            if (isMenuOpen) toggleMenu(); // Close overlay if user taps bottom nav

            Fragment fragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                fragment = new HomeFragment();
            } else if (id == R.id.nav_calculator) {
                fragment = new CalculatorFragment();
            } else if (id == R.id.nav_dummy) {
                // Do nothing, handled by FAB
                return false;
            } else if (id == R.id.nav_daily_progress) {
                fragment = new DailyProgressFragment();
            } else if (id == R.id.nav_profile) {
                fragment = new ProfileFragment();
            }

            if (fragment != null) {
                loadFragment(fragment);
            }
            return true;
        });

        // Prevent clicking the empty dummy space
        bottomNav.setItemActiveIndicatorEnabled(false); // Optional visual tweak

        fabScanner.setOnClickListener(v -> toggleMenu());
        overlayDim.setOnClickListener(v -> {
            if (isMenuOpen) toggleMenu();
        });

        // Popup Actions
        btnAiScan.setOnClickListener(v -> {
            toggleMenu();
            loadFragment(new AIScannerFragment());
            bottomNav.setSelectedItemId(R.id.nav_dummy); // Visually update nav
        });

        btnGallery.setOnClickListener(v -> {
            toggleMenu();
            // TODO: Route to Gallery intent or an alternative fragment
            // loadFragment(new GalleryScannerFragment());
        });

        btnSavedMeals.setOnClickListener(v -> {
            toggleMenu();
            loadFragment(new FoodDiaryFragment());
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
            scannerMenuContainer.animate().translationY(0).setDuration(300).setListener(null).start();

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

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
