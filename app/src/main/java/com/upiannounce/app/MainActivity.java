package com.upiannounce.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private Fragment homeFragment, dashboardFragment, settingsFragment, activeFragment;
    private String currentStyle = "snow";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyAppStyle(); // Apply theme before creating views
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_nav);

        if (savedInstanceState == null) {
            homeFragment = new HomeFragment();
            dashboardFragment = new DashboardFragment();
            settingsFragment = new SettingsFragment();

            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction()
                .add(R.id.fragment_container, dashboardFragment, "dashboard").hide(dashboardFragment)
                .add(R.id.fragment_container, settingsFragment, "settings").hide(settingsFragment)
                .add(R.id.fragment_container, homeFragment, "home").commit();
            activeFragment = homeFragment;
        } else {
            homeFragment = getSupportFragmentManager().findFragmentByTag("home");
            dashboardFragment = getSupportFragmentManager().findFragmentByTag("dashboard");
            settingsFragment = getSupportFragmentManager().findFragmentByTag("settings");
            activeFragment = homeFragment;
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment target = null;
            int id = item.getItemId();
            
            if (id == R.id.nav_home) target = homeFragment;
            else if (id == R.id.nav_dashboard) target = dashboardFragment;
            else if (id == R.id.nav_settings) target = settingsFragment;

            if (target != null && target != activeFragment) {
                getSupportFragmentManager().beginTransaction()
                    .hide(activeFragment)
                    .show(target)
                    .commitAllowingStateLoss();
                activeFragment = target;
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        SharedPreferences prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        
        // 1. Check if the user changed the theme in AppearanceActivity
        String newStyle = prefs.getString("style_pref", "snow");
        if (!newStyle.equals(currentStyle)) {
            recreate(); // Instantly rebuild the activity with the new colors
            return; // Stop here so it doesn't trigger biometric twice during recreate
        }

        // 2. Check if App Lock is enabled
        if (prefs.getBoolean("app_lock_enabled", false)) {
            promptBiometric();
        }
    }

    private void promptBiometric() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                finishAffinity();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock AnnounceUPI")
                .setSubtitle("Use biometric or device PIN to unlock")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK | BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void applyAppStyle() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        currentStyle = prefs.getString("style_pref", "snow");
        
        switch (currentStyle) {
            case "midnight": setTheme(R.style.Theme_AnnounceUPI_Midnight); break;
            case "amoled": setTheme(R.style.Theme_AnnounceUPI_Amoled); break;
            case "sepia": setTheme(R.style.Theme_AnnounceUPI_Sepia); break;
            default: setTheme(R.style.Theme_AnnounceUPI_Snow); break;
        }
    }
}