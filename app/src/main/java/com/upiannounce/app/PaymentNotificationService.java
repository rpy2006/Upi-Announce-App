package com.upiannounce.app;

import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PaymentNotificationService extends NotificationListenerService {

    private static final Pattern AMOUNT_PATTERN = Pattern.compile(
            "(?:₹|rs\\.?\\s?|inr\\s?|rs\\s)([\\d,]+(?:\\.\\d{1,2})?)", Pattern.CASE_INSENSITIVE);

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();
        if (!isUpiApp(packageName)) return;

        // Check Excluded Apps
        SharedPreferences prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        Set<String> excluded = prefs.getStringSet("excluded_apps", new HashSet<>());
        if (excluded.contains(packageName)) {
            Log.d("NotifService", "Ignored excluded app: " + packageName);
            return;
        }

        // Check Incognito Mode (block visual toasts if enabled, but still save and announce)
        boolean isIncognito = prefs.getBoolean("incognito_mode", false);

        Notification notification = sbn.getNotification();
        if (notification == null) return;

        Bundle extras = notification.extras;
        String title = extras.getString("android.title", "");
        String text = extras.getString("android.text", "");

        String combined = (title + " " + text).toLowerCase();
        if (!combined.contains("received") && !combined.contains("credited") && !combined.contains("paid you")) {
            return;
        }

        Matcher m = AMOUNT_PATTERN.matcher(combined);
        if (m.find()) {
            try {
                double amount = Double.parseDouble(m.group(1).replace(",", ""));
                if (amount > 0) {
                    String appName = getAppName(packageName);
                    
                    // Save to database
                    TransactionRepository.getInstance(this).saveTransaction(amount, appName);
                    
                    // Announce via TTS
                    TTSManager.getInstance().announcePayment(amount, appName);
                    
                    // Show Toast if not in Incognito mode
                    if (!isIncognito) {
                        android.widget.Toast.makeText(this, "Payment Detected: ₹" + amount, android.widget.Toast.LENGTH_SHORT).show();
                    }
                    
                    Log.d("NotifService", "Detected UPI: ₹" + amount + " from " + packageName);
                }
            } catch (Exception ignored) {}
        }
    }

    private boolean isUpiApp(String pkg) {
        return pkg != null && (pkg.contains("google.android.apps.nbu.paisa.user") // GPay
                || pkg.contains("com.phonepe") // PhonePe
                || pkg.contains("net.one97.paytm") // Paytm
                || pkg.contains("com.whatsapp") // WhatsApp
                || pkg.contains("in.org.npci.upiapp")); // BHIM
    }

    private String getAppName(String pkg) {
        if (pkg.contains("paisa.user")) return "Google Pay";
        if (pkg.contains("phonepe")) return "PhonePe";
        if (pkg.contains("paytm")) return "Paytm";
        if (pkg.contains("whatsapp")) return "WhatsApp";
        return "UPI App";
    }
}