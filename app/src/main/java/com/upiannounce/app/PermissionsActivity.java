package com.upiannounce.app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.appbar.MaterialToolbar;

public class PermissionsActivity extends AppCompatActivity {

    private final ActivityResultLauncher<String> smsPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> updatePermissionStatuses());

    private final ActivityResultLauncher<String> notifPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> updatePermissionStatuses());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        String style = prefs.getString("style_pref", "snow");
        switch (style) {
            case "midnight": setTheme(R.style.Theme_AnnounceUPI_Midnight); break;
            case "amoled": setTheme(R.style.Theme_AnnounceUPI_Amoled); break;
            case "sepia": setTheme(R.style.Theme_AnnounceUPI_Sepia); break;
            default: setTheme(R.style.Theme_AnnounceUPI_Snow); break;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        findViewById(R.id.row_sms).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
                smsPermissionLauncher.launch(Manifest.permission.RECEIVE_SMS);
            } else {
                openAppSettings();
            }
        });

        findViewById(R.id.row_notif_access).setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            startActivity(intent);
        });

        findViewById(R.id.row_notifications).setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                } else {
                    openAppSettings();
                }
            } else {
                openAppSettings();
            }
        });

        findViewById(R.id.row_vibrate).setOnClickListener(v -> openAppSettings());
        findViewById(R.id.row_boot).setOnClickListener(v -> openAppSettings());
    }

    @Override
    protected void onResume() {
        super.onResume();
        updatePermissionStatuses();
    }

    private void updatePermissionStatuses() {
        boolean hasSms = ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED;
        setStatus((TextView) findViewById(R.id.tv_sms_status), hasSms);

        boolean hasNotifAccess = isNotificationListenerEnabled();
        setStatus((TextView) findViewById(R.id.tv_notif_access_status), hasNotifAccess);

        boolean hasNotif = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasNotif = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        setStatus((TextView) findViewById(R.id.tv_notif_status), hasNotif);

        boolean hasVibrate = ContextCompat.checkSelfPermission(this, Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED;
        setStatus((TextView) findViewById(R.id.tv_vibrate_status), hasVibrate);

        boolean hasBoot = ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_BOOT_COMPLETED) == PackageManager.PERMISSION_GRANTED;
        setStatus((TextView) findViewById(R.id.tv_boot_status), hasBoot);
    }

    private void setStatus(TextView tv, boolean granted) {
        if (granted) {
            tv.setText("GRANTED");
            tv.setTextColor(getColor(R.color.accent_black));
        } else {
            tv.setText("GRANT");
            tv.setTextColor(getColor(R.color.text_secondary));
        }
    }

    private boolean isNotificationListenerEnabled() {
        String flat = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        return flat != null && flat.contains(getPackageName());
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", getPackageName(), null));
        startActivity(intent);
    }
}