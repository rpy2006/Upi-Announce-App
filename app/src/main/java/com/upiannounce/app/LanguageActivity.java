package com.upiannounce.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;

public class LanguageActivity extends AppCompatActivity {

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply saved theme style
        prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        String style = prefs.getString("style_pref", "snow");
        switch (style) {
            case "midnight": setTheme(R.style.Theme_AnnounceUPI_Midnight); break;
            case "amoled": setTheme(R.style.Theme_AnnounceUPI_Amoled); break;
            case "sepia": setTheme(R.style.Theme_AnnounceUPI_Sepia); break;
            default: setTheme(R.style.Theme_AnnounceUPI_Snow); break;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        RadioGroup rgLanguages = findViewById(R.id.rg_languages);
        
        // Get currently saved language
        String currentLang = prefs.getString("tts_lang", "en");

        // Loop through RadioGroup to check the saved language
        for (int i = 0; i < rgLanguages.getChildCount(); i++) {
            View child = rgLanguages.getChildAt(i);
            if (child instanceof RadioButton) {
                RadioButton rb = (RadioButton) child;
                if (currentLang.equals(rb.getTag())) {
                    rb.setChecked(true);
                    break;
                }
            }
        }

        findViewById(R.id.btn_save).setOnClickListener(v -> {
            int selectedId = rgLanguages.getCheckedRadioButtonId();
            if (selectedId != -1) {
                RadioButton selectedRb = findViewById(selectedId);
                prefs.edit().putString("tts_lang", (String) selectedRb.getTag()).apply();
            }
            finish(); // Close activity and return to settings
        });
    }
}