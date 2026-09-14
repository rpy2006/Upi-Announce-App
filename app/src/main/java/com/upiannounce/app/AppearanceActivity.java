package com.upiannounce.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;

public class AppearanceActivity extends AppCompatActivity {

    private View cardSnow, cardMidnight, cardAmoled, cardSepia;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        applyAppStyle(); // Apply theme before creating views

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appearance);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        cardSnow = findViewById(R.id.card_snow);
        cardMidnight = findViewById(R.id.card_midnight);
        cardAmoled = findViewById(R.id.card_amoled);
        cardSepia = findViewById(R.id.card_sepia);

        cardSnow.setOnClickListener(v -> setAppStyle("snow"));
        cardMidnight.setOnClickListener(v -> setAppStyle("midnight"));
        cardAmoled.setOnClickListener(v -> setAppStyle("amoled"));
        cardSepia.setOnClickListener(v -> setAppStyle("sepia"));

        updateUI();
    }

    private void setAppStyle(String style) {
        prefs.edit().putString("style_pref", style).apply();
        // Just update the selected checkmarks instantly, no need to recreate the whole activity
        updateUI();
    }

    private void applyAppStyle() {
        String style = prefs.getString("style_pref", "snow");
        switch (style) {
            case "midnight": setTheme(R.style.Theme_AnnounceUPI_Midnight); break;
            case "amoled": setTheme(R.style.Theme_AnnounceUPI_Amoled); break;
            case "sepia": setTheme(R.style.Theme_AnnounceUPI_Sepia); break;
            default: setTheme(R.style.Theme_AnnounceUPI_Snow); break;
        }
    }

    private void updateUI() {
        String currentStyle = prefs.getString("style_pref", "snow");
        int selectedBg = R.drawable.bg_theme_card_selected;
        int unselectedBg = R.drawable.bg_theme_card;

        cardSnow.setBackgroundResource(currentStyle.equals("snow") ? selectedBg : unselectedBg);
        cardMidnight.setBackgroundResource(currentStyle.equals("midnight") ? selectedBg : unselectedBg);
        cardAmoled.setBackgroundResource(currentStyle.equals("amoled") ? selectedBg : unselectedBg);
        cardSepia.setBackgroundResource(currentStyle.equals("sepia") ? selectedBg : unselectedBg);
    }
}