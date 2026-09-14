package com.upiannounce.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AnnounceUPIApp extends Application {
    private static AnnounceUPIApp instance;
    private ExecutorService executor;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        
        runInBackground(() -> {
            DatabaseHelper.getInstance(this);
            TTSManager.getInstance().init(this);
            
            // Prune old transactions based on user preference
            SharedPreferences prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
            int days = prefs.getInt("auto_delete_days", 0);
            if (days > 0) {
                TransactionRepository.getInstance(this).pruneOldTransactions(days);
            }
        });
    }

    public static AnnounceUPIApp get() { return instance; }

    public static void runInBackground(Runnable runnable) {
        if (instance != null && instance.executor != null) {
            instance.executor.execute(runnable);
        }
    }
}