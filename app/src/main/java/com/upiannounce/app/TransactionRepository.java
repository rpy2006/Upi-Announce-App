package com.upiannounce.app;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionRepository {
    private static volatile TransactionRepository instance;
    private final DatabaseHelper dbHelper;
    private final MutableLiveData<List<TransactionEntity>> liveData = new MutableLiveData<>();

    private TransactionRepository(Context ctx) {
        dbHelper = DatabaseHelper.getInstance(ctx);
        refreshData();
    }

    public static TransactionRepository getInstance(Context ctx) {
        if (instance == null) {
            synchronized (TransactionRepository.class) {
                if (instance == null) instance = new TransactionRepository(ctx);
            }
        }
        return instance;
    }

    public void saveTransaction(double amount, String source) {
        AnnounceUPIApp.runInBackground(() -> {
            TransactionEntity t = new TransactionEntity();
            t.amountText = "₹" + String.format(Locale.getDefault(), "%.0f", amount);
            t.rawAmount = amount;
            t.source = source;
            t.timeText = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
            t.dateText = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
            t.timestamp = System.currentTimeMillis();
            dbHelper.insert(t);
            refreshData();
        });
    }

    public LiveData<List<TransactionEntity>> getAllLive() { return liveData; }

    public List<TransactionEntity> getAllSync() {
        return dbHelper.getAll();
    }

    private void refreshData() {
        AnnounceUPIApp.runInBackground(() -> liveData.postValue(dbHelper.getAll()));
    }

    public void clearAll() {
        AnnounceUPIApp.runInBackground(() -> {
            dbHelper.deleteAll();
            refreshData();
        });
    }

    public void pruneOldTransactions(int days) {
        if (days <= 0) return;
        long cutoff = System.currentTimeMillis() - ((long) days * 24 * 60 * 60 * 1000);
        AnnounceUPIApp.runInBackground(() -> {
            dbHelper.deleteOlderThan(cutoff);
            refreshData();
        });
    }

    // ADD THIS METHOD
    public void deleteById(long id) {
        AnnounceUPIApp.runInBackground(() -> {
            dbHelper.deleteById(id);
            refreshData();
        });
    }
}