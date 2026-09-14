package com.upiannounce.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;
import java.util.Locale;

public class TTSManager {
    private static volatile TTSManager instance;
    private TextToSpeech tts;
    private volatile boolean isReady = false;
    private Context appContext;

    private TTSManager() {}

    public static TTSManager getInstance() {
        if (instance == null) {
            synchronized (TTSManager.class) {
                if (instance == null) {
                    instance = new TTSManager();
                }
            }
        }
        return instance;
    }

    public void init(Context context) {
        if (tts != null) return;
        appContext = context.getApplicationContext();
        tts = new TextToSpeech(appContext, status -> {
            if (status == TextToSpeech.SUCCESS) {
                isReady = true;
                Log.d("TTSManager", "TTS Engine Ready");
            } else {
                Log.e("TTSManager", "TTS Init Failed");
            }
        });
    }

    public void announcePayment(double amount, String source) {
        if (!isReady || tts == null) return;

        SharedPreferences prefs = appContext.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        
        // Check Bluetooth Only mode
        if (prefs.getBoolean("tts_bluetooth_only", false)) {
            AudioManager am = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);
            if (am == null || !am.isBluetoothA2dpOn()) {
                Log.d("TTSManager", "Bluetooth only mode: A2DP not connected, skipping.");
                return;
            }
        }

        String langCode = prefs.getString("tts_lang", "en");
        Locale locale = new Locale(langCode);

        int avail = tts.isLanguageAvailable(locale);
        if (avail >= TextToSpeech.LANG_AVAILABLE) {
            tts.setLanguage(locale);
        } else {
            tts.setLanguage(Locale.US);
        }

        // Apply Speech Speed
        String speed = prefs.getString("tts_speed", "normal");
        float speechRate = 1.0f;
        if (speed.equals("slow")) speechRate = 0.75f;
        else if (speed.equals("fast")) speechRate = 1.25f;
        tts.setSpeechRate(speechRate);

        String prefix = getPrefix(prefs, langCode);
        String text = buildSpeechText(prefix, amount, source, langCode);
        
        boolean repeat = prefs.getBoolean("tts_repeat", false);

        if (repeat) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "announcement_1");
            tts.speak(text, TextToSpeech.QUEUE_ADD, null, "announcement_2");
        } else {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "announcement_1");
        }
    }

    public void testAnnouncement(Context context) {
        if (!isReady || tts == null) {
            Toast.makeText(context, "TTS Engine is not ready yet.", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        String langCode = prefs.getString("tts_lang", "en");
        
        Locale locale = new Locale(langCode);
        int avail = tts.isLanguageAvailable(locale);
        if (avail >= TextToSpeech.LANG_AVAILABLE) {
            tts.setLanguage(locale);
        } else {
            tts.setLanguage(Locale.US);
        }

        String speed = prefs.getString("tts_speed", "normal");
        float speechRate = 1.0f;
        if (speed.equals("slow")) speechRate = 0.75f;
        else if (speed.equals("fast")) speechRate = 1.25f;
        tts.setSpeechRate(speechRate);

        String prefix = getPrefix(prefs, langCode);
        String text = buildSpeechText(prefix, 500.0, "Test", langCode);
        
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "test_announcement");
    }

    public void announceTargetReached() {
        if (!isReady || tts == null) return;

        SharedPreferences prefs = appContext.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        String langCode = prefs.getString("tts_lang", "en");
        Locale locale = new Locale(langCode);
        
        int avail = tts.isLanguageAvailable(locale);
        if (avail >= TextToSpeech.LANG_AVAILABLE) {
            tts.setLanguage(locale);
        } else {
            tts.setLanguage(Locale.US);
        }

        String text;
        switch (langCode) {
            case "hi": text = "Badhai ho! Aapka aaj ka lakshya poora ho gaya hai."; break;
            case "ta": text = "Virumbugal nerainthathu! Innaiya ilakkai ithanithu."; break;
            case "te": text = "Swagatam! Mee roju lakshyam puraindi."; break;
            case "kn": text = "Harushavagali! Ninna dinada guri mugitu."; break;
            case "bn": text = "Shubheccha! Apnar ajker lakshya puron hoyeche."; break;
            case "mr": text = "Shubhechha! Aaj cha lakshya purn zale ahe."; break;
            default: text = "Congratulations! Your daily target has been achieved."; break;
        }

        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "target_announcement");
    }

    private String getPrefix(SharedPreferences prefs, String lang) {
        String customPrefix = prefs.getString("tts_prefix", "");
        if (!customPrefix.isEmpty()) {
            return customPrefix;
        }

        // Default prefixes if user hasn't set one
        switch (lang) {
            case "hi": return "Paisa aaya.";
            case "ta": return "Panam vandhadu.";
            case "te": return "Dabbu vachindi.";
            case "kn": return "Hana bandide.";
            case "bn": return "Taka esheche.";
            case "mr": return "Paise aale.";
            default: return "Payment received.";
        }
    }

    private String buildSpeechText(String prefix, double amount, String source, String lang) {
        long rupees = (long) amount;
        int paise = (int) Math.round((amount - rupees) * 100);

        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append(" ");

        switch (lang) {
            case "hi":
                if (paise > 0) sb.append(rupees).append(" rupaye aur ").append(paise).append(" paise");
                else sb.append(rupees).append(" rupaye");
                break;
            case "ta":
                if (paise > 0) sb.append(rupees).append(" rubaiyil ").append(paise).append(" paise");
                else sb.append(rupees).append(" rubai");
                break;
            case "te":
                if (paise > 0) sb.append(rupees).append(" ruvalu ").append(paise).append(" paise");
                else sb.append(rupees).append(" ruvalu");
                break;
            case "kn":
                if (paise > 0) sb.append(rupees).append(" rupayi ").append(paise).append(" paise");
                else sb.append(rupees).append(" rupayi");
                break;
            case "bn":
                if (paise > 0) sb.append(rupees).append(" taka ").append(paise).append(" poisha");
                else sb.append(rupees).append(" taka");
                break;
            case "mr":
                if (paise > 0) sb.append(rupees).append(" rupaye ").append(paise).append(" paise");
                else sb.append(rupees).append(" rupaye");
                break;
            default:
                if (paise > 0) sb.append(rupees).append(" rupees and ").append(paise).append(" paise");
                else sb.append(rupees).append(" rupees");
                break;
        }

        if (source != null && !source.isEmpty()) {
            sb.append(" from ").append(source);
        }

        return sb.toString();
    }

    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
            isReady = false;
        }
    }
}