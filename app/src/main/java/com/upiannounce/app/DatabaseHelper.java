package com.upiannounce.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "announceupi.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE = "transactions";
    private static volatile DatabaseHelper instance;

    private DatabaseHelper(Context ctx) { super(ctx, DB_NAME, null, DB_VERSION); }

    public static DatabaseHelper getInstance(Context ctx) {
        if (instance == null) {
            synchronized (DatabaseHelper.class) {
                if (instance == null) instance = new DatabaseHelper(ctx.getApplicationContext());
            }
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " (id INTEGER PRIMARY KEY AUTOINCREMENT, amount_text TEXT, raw_amount REAL, source TEXT, time_text TEXT, date_text TEXT, timestamp INTEGER)");
        db.execSQL("CREATE INDEX idx_timestamp ON " + TABLE + "(timestamp DESC)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    public long insert(TransactionEntity t) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("amount_text", t.amountText);
        cv.put("raw_amount", t.rawAmount);
        cv.put("source", t.source);
        cv.put("time_text", t.timeText);
        cv.put("date_text", t.dateText);
        cv.put("timestamp", t.timestamp);
        return db.insert(TABLE, null, cv);
    }

    public List<TransactionEntity> getAll() {
        List<TransactionEntity> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE, null, null, null, null, null, "timestamp DESC");
        if (c != null) {
            while (c.moveToNext()) {
                TransactionEntity t = new TransactionEntity();
                t.id = c.getLong(c.getColumnIndexOrThrow("id"));
                t.amountText = c.getString(c.getColumnIndexOrThrow("amount_text"));
                t.rawAmount = c.getDouble(c.getColumnIndexOrThrow("raw_amount"));
                t.source = c.getString(c.getColumnIndexOrThrow("source"));
                t.timeText = c.getString(c.getColumnIndexOrThrow("time_text"));
                t.dateText = c.getString(c.getColumnIndexOrThrow("date_text"));
                t.timestamp = c.getLong(c.getColumnIndexOrThrow("timestamp"));
                list.add(t);
            }
            c.close();
        }
        return list;
    }

    public void deleteAll() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE, null, null);
    }

    public void deleteOlderThan(long timestamp) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE, "timestamp < ?", new String[]{String.valueOf(timestamp)});
    }

    // ADD THIS METHOD
    public void deleteById(long id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE, "id = ?", new String[]{String.valueOf(id)});
    }
}