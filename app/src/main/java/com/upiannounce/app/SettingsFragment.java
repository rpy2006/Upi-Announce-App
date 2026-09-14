package com.upiannounce.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.switchmaterial.SwitchMaterial;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class SettingsFragment extends Fragment {

    private View root;
    private View rowAppPermissions, rowAppearance, rowLanguage, rowSpeechSpeed, rowRepeat, rowCustomPrefix, rowBluetoothOnly, rowTestVoice, rowExcludedApps, rowDailyTarget, rowMinAmount, rowDnd, rowDailySummary, rowSummaryTime, rowAppLock, rowStealthMode, rowIncognito, rowExportCsv, rowExportSettings, rowImportSettings, rowAutoDelete, rowClearHistory, rowEmail, rowInstagram, rowVersion;
    private View rowNotificationSettings, rowPrivacy, rowStorageUsage, rowSyncBackup, rowHelpSupport;
    private TextView tvLanguageValue, tvSpeechSpeedValue, tvPrefixValue, tvExcludedAppsValue, tvDailyTargetValue, tvMinAmountValue, tvDndTime, tvSummaryTimeValue, tvAppInfo, tvAutoDeleteValue;
    private TextView tvStorageUsageValue;

    private final String[] languages = {"English", "Hindi", "Tamil", "Telugu", "Kannada", "Bengali", "Marathi", "Malayalam", "Gujarati", "Punjabi"};
    private final String[] langCodes = {"en", "hi", "ta", "te", "kn", "bn", "mr", "ml", "gu", "pa"};

    private final String[] speeds = {"Slow", "Normal", "Fast"};
    private final String[] speedKeys = {"slow", "normal", "fast"};

    private final String[] minAmounts = {"₹0 (all)", "₹1+", "₹10+", "₹50+", "₹100+", "₹500+", "₹1000+"};
    private final int[] minAmountValues = {0, 1, 10, 50, 100, 500, 1000};

    private final String[] autoDeleteOptions = {"Never", "30 days", "60 days", "90 days"};
    private final int[] autoDeleteDays = {0, 30, 60, 90};

    private final String[] appNames = {"Google Pay", "PhonePe", "Paytm", "WhatsApp", "BHIM"};
    private final String[] appPackages = {"com.google.android.apps.nbu.paisa.user", "com.phonepe", "net.one97.paytm", "com.whatsapp", "in.org.npci.upiapp"};

    private final ActivityResultLauncher<String[]> importLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null) {
                    importSettings(uri);
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_settings, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews();
        setupClickListeners();
        setupSearch();
    }

    private void initializeViews() {
        rowAppPermissions = root.findViewById(R.id.row_app_permissions);
        rowAppearance = root.findViewById(R.id.row_appearance);
        rowLanguage = root.findViewById(R.id.row_language);
        rowSpeechSpeed = root.findViewById(R.id.row_speech_speed);
        rowRepeat = root.findViewById(R.id.row_repeat);
        rowCustomPrefix = root.findViewById(R.id.row_custom_prefix);
        rowBluetoothOnly = root.findViewById(R.id.row_bluetooth_only);
        rowTestVoice = root.findViewById(R.id.row_test_voice);
        rowExcludedApps = root.findViewById(R.id.row_excluded_apps);
        rowDailyTarget = root.findViewById(R.id.row_daily_target);
        rowMinAmount = root.findViewById(R.id.row_min_amount);
        rowDnd = root.findViewById(R.id.row_dnd);
        rowDailySummary = root.findViewById(R.id.row_daily_summary);
        rowSummaryTime = root.findViewById(R.id.row_summary_time);
        rowAppLock = root.findViewById(R.id.row_app_lock);
        rowStealthMode = root.findViewById(R.id.row_stealth_mode);
        rowIncognito = root.findViewById(R.id.row_incognito);
        rowExportCsv = root.findViewById(R.id.row_export_csv);
        rowExportSettings = root.findViewById(R.id.row_export_settings);
        rowImportSettings = root.findViewById(R.id.row_import_settings);
        rowAutoDelete = root.findViewById(R.id.row_auto_delete);
        rowClearHistory = root.findViewById(R.id.row_clear_history);
        rowEmail = root.findViewById(R.id.row_email);
        rowInstagram = root.findViewById(R.id.row_instagram);
        rowVersion = root.findViewById(R.id.row_version);

        rowNotificationSettings = root.findViewById(R.id.row_notification_settings);
        rowPrivacy = root.findViewById(R.id.row_privacy);
        rowStorageUsage = root.findViewById(R.id.row_storage_usage);
        rowSyncBackup = root.findViewById(R.id.row_sync_backup);
        rowHelpSupport = root.findViewById(R.id.row_help_support);

        tvLanguageValue = root.findViewById(R.id.tv_language_value);
        tvSpeechSpeedValue = root.findViewById(R.id.tv_speech_speed_value);
        tvPrefixValue = root.findViewById(R.id.tv_prefix_value);
        tvExcludedAppsValue = root.findViewById(R.id.tv_excluded_apps_value);
        tvDailyTargetValue = root.findViewById(R.id.tv_daily_target_value);
        tvMinAmountValue = root.findViewById(R.id.tv_min_amount_value);
        tvDndTime = root.findViewById(R.id.tv_dnd_time);
        tvSummaryTimeValue = root.findViewById(R.id.tv_summary_time_value);
        tvAppInfo = root.findViewById(R.id.tv_app_info);
        tvAutoDeleteValue = root.findViewById(R.id.tv_auto_delete_value);
        tvStorageUsageValue = root.findViewById(R.id.tv_storage_usage_value);
    }

    private void setupClickListeners() {
        rowAppPermissions.setOnClickListener(v -> startActivity(new Intent(requireContext(), PermissionsActivity.class)));
        rowAppearance.setOnClickListener(v -> startActivity(new Intent(requireContext(), AppearanceActivity.class)));
        rowLanguage.setOnClickListener(v -> startActivity(new Intent(requireContext(), LanguageActivity.class)));
        rowSpeechSpeed.setOnClickListener(v -> showSpeechSpeedDialog());
        rowCustomPrefix.setOnClickListener(v -> showCustomPrefixDialog());
        rowTestVoice.setOnClickListener(v -> TTSManager.getInstance().testAnnouncement(requireContext()));
        
        rowExcludedApps.setOnClickListener(v -> showExcludedAppsDialog());
        rowDailyTarget.setOnClickListener(v -> showDailyTargetDialog());
        
        rowMinAmount.setOnClickListener(v -> showMinAmountDialog());
        rowDnd.setOnClickListener(v -> showDndTimePicker());
        rowSummaryTime.setOnClickListener(v -> showSummaryTimePicker());

        rowExportCsv.setOnClickListener(v -> exportToCsv());
        rowExportSettings.setOnClickListener(v -> exportSettings());
        rowImportSettings.setOnClickListener(v -> importLauncher.launch(new String[]{"application/json"}));
        rowAutoDelete.setOnClickListener(v -> showAutoDeleteDialog());
        rowClearHistory.setOnClickListener(v -> confirmClearHistory());

        rowEmail.setOnClickListener(v -> sendSupportEmail());

        rowInstagram.setOnClickListener(v -> {
            Intent igIntent = new Intent(Intent.ACTION_VIEW);
            igIntent.setData(Uri.parse("https://instagram.com/rohiit.md"));
            startActivity(igIntent);
        });

        rowVersion.setOnClickListener(v -> showChangelogBottomSheet());

        rowNotificationSettings.setOnClickListener(v -> showNotificationSettingsDialog());
        rowPrivacy.setOnClickListener(v -> showPrivacyDialog());
        rowStorageUsage.setOnClickListener(v -> showStorageUsageDialog());
        rowHelpSupport.setOnClickListener(v -> showHelpSupportDialog());
    }

    @Override
    public void onResume() {
        super.onResume();
        updateAllLabels();
        setupAllSwitches();
    }

    private void updateAllLabels() {
        SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);

        String currentLang = prefs.getString("tts_lang", "en");
        for (int i = 0; i < langCodes.length; i++) {
            if (langCodes[i].equals(currentLang)) {
                tvLanguageValue.setText(languages[i]);
                break;
            }
        }

        String currentSpeed = prefs.getString("tts_speed", "normal");
        for (int i = 0; i < speedKeys.length; i++) {
            if (speedKeys[i].equals(currentSpeed)) {
                tvSpeechSpeedValue.setText(speeds[i]);
                break;
            }
        }

        String prefix = prefs.getString("tts_prefix", "");
        tvPrefixValue.setText(prefix.isEmpty() ? "Default" : prefix);

        Set<String> excluded = prefs.getStringSet("excluded_apps", new HashSet<>());
        tvExcludedAppsValue.setText(excluded.isEmpty() ? "None" : excluded.size() + " excluded");

        int dailyTarget = prefs.getInt("daily_target", 0);
        tvDailyTargetValue.setText(dailyTarget > 0 ? "₹" + dailyTarget : "Off");

        int currentMinAmt = prefs.getInt("min_amount", 0);
        for (int i = 0; i < minAmountValues.length; i++) {
            if (minAmountValues[i] == currentMinAmt) {
                tvMinAmountValue.setText(minAmounts[i]);
                break;
            }
        }

        int dndStart = prefs.getInt("dnd_start", 22);
        int dndEnd = prefs.getInt("dnd_end", 7);
        tvDndTime.setText(fmt12h(dndStart) + " - " + fmt12h(dndEnd));

        int summaryHour = prefs.getInt("summary_hour", 21);
        int summaryMinute = prefs.getInt("summary_minute", 0);
        tvSummaryTimeValue.setText(fmt12hMin(summaryHour, summaryMinute));

        int autoDeleteDaysVal = prefs.getInt("auto_delete_days", 0);
        for (int i = 0; i < autoDeleteDays.length; i++) {
            if (autoDeleteDays[i] == autoDeleteDaysVal) {
                tvAutoDeleteValue.setText(autoDeleteOptions[i]);
                break;
            }
        }

        try {
            String version = requireContext().getPackageManager().getPackageInfo(requireContext().getPackageName(), 0).versionName;
            tvAppInfo.setText(version);
        } catch (Exception e) {
            tvAppInfo.setText("2.0");
        }

        tvStorageUsageValue.setText(formatBytes(computeCacheAndDbSize()));
    }

    private void setupAllSwitches() {
        SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);

        SwitchMaterial switchRepeat = root.findViewById(R.id.switch_repeat);
        switchRepeat.setChecked(prefs.getBoolean("tts_repeat", false));
        switchRepeat.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.edit().putBoolean("tts_repeat", isChecked).apply());

        SwitchMaterial switchBluetooth = root.findViewById(R.id.switch_bluetooth_only);
        switchBluetooth.setChecked(prefs.getBoolean("tts_bluetooth_only", false));
        switchBluetooth.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("tts_bluetooth_only", isChecked).apply();
            if (isChecked) Toast.makeText(requireContext(), "Will only announce when Bluetooth headset is connected", Toast.LENGTH_SHORT).show();
        });

        SwitchMaterial switchDnd = root.findViewById(R.id.switch_dnd);
        switchDnd.setChecked(prefs.getBoolean("dnd_enabled", false));
        switchDnd.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.edit().putBoolean("dnd_enabled", isChecked).apply());

        SwitchMaterial switchDailySummary = root.findViewById(R.id.switch_daily_summary);
        switchDailySummary.setChecked(prefs.getBoolean("daily_summary_enabled", true));
        switchDailySummary.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.edit().putBoolean("daily_summary_enabled", isChecked).apply());

        SwitchMaterial switchAppLock = root.findViewById(R.id.switch_app_lock);
        switchAppLock.setChecked(prefs.getBoolean("app_lock_enabled", false));
        switchAppLock.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) Toast.makeText(requireContext(), "App Lock enabled", Toast.LENGTH_SHORT).show();
            prefs.edit().putBoolean("app_lock_enabled", isChecked).apply();
        });

        SwitchMaterial switchStealth = root.findViewById(R.id.switch_stealth_mode);
        switchStealth.setChecked(prefs.getBoolean("stealth_mode", false));
        switchStealth.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.edit().putBoolean("stealth_mode", isChecked).apply());

        SwitchMaterial switchIncognito = root.findViewById(R.id.switch_incognito);
        switchIncognito.setChecked(prefs.getBoolean("incognito_mode", false));
        switchIncognito.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.edit().putBoolean("incognito_mode", isChecked).apply());

        SwitchMaterial switchSyncBackup = root.findViewById(R.id.switch_sync_backup);
        switchSyncBackup.setChecked(prefs.getBoolean("sync_backup_enabled", false));
        switchSyncBackup.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("sync_backup_enabled", isChecked).apply();
            if (isChecked) Toast.makeText(requireContext(), "Cloud sync is coming in a future update. Your data stays on this device for now.", Toast.LENGTH_LONG).show();
        });
    }

    private void showChangelogBottomSheet() {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(requireContext());
        bottomSheet.setContentView(R.layout.dialog_changelog);
        bottomSheet.show();
    }

    private void showSpeechSpeedDialog() {
        SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        String currentSpeed = prefs.getString("tts_speed", "normal");
        int selected = 1;
        for (int i = 0; i < speedKeys.length; i++) {
            if (speedKeys[i].equals(currentSpeed)) { selected = i; break; }
        }
        final int[] checkedItem = {selected};
        new AlertDialog.Builder(requireContext())
                .setTitle("Speech Speed")
                .setSingleChoiceItems(speeds, selected, (dialog, which) -> checkedItem[0] = which)
                .setPositiveButton("Save", (dialog, which) -> {
                    prefs.edit().putString("tts_speed", speedKeys[checkedItem[0]]).apply();
                    updateAllLabels();
                })
                .setNegativeButton("Cancel", null).show();
    }

    private void showCustomPrefixDialog() {
        SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        String currentPrefix = prefs.getString("tts_prefix", "");
        final EditText input = new EditText(requireContext());
        input.setHint("e.g. Paisa aaya, Alert, You received");
        input.setText(currentPrefix);
        input.setSingleLine(true);
        int pad = dpToPx(20);
        input.setPadding(pad, pad, pad, pad);
        new AlertDialog.Builder(requireContext())
                .setTitle("Custom Announcement Prefix")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    prefs.edit().putString("tts_prefix", input.getText().toString().trim()).apply();
                    updateAllLabels();
                })
                .setNegativeButton("Cancel", null)
                .setNeutralButton("Reset to Default", (dialog, which) -> {
                    prefs.edit().remove("tts_prefix").apply();
                    updateAllLabels();
                }).show();
    }

    private void showExcludedAppsDialog() {
        SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        Set<String> excluded = new HashSet<>(prefs.getStringSet("excluded_apps", new HashSet<>()));
        
        boolean[] checked = new boolean[appNames.length];
        for (int i = 0; i < appNames.length; i++) {
            checked[i] = excluded.contains(appPackages[i]);
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Excluded Apps")
                .setMultiChoiceItems(appNames, checked, (dialog, which, isChecked) -> {
                    if (isChecked) excluded.add(appPackages[which]);
                    else excluded.remove(appPackages[which]);
                })
                .setPositiveButton("Save", (dialog, which) -> {
                    prefs.edit().putStringSet("excluded_apps", excluded).apply();
                    updateAllLabels();
                })
                .setNegativeButton("Cancel", null).show();
    }

    private void showDailyTargetDialog() {
        SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        int currentTarget = prefs.getInt("daily_target", 0);

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(currentTarget > 0 ? String.valueOf(currentTarget) : "");
        input.setHint("Enter amount (e.g. 5000)");
        int pad = dpToPx(20);
        input.setPadding(pad, pad, pad, pad);

        new AlertDialog.Builder(requireContext())
                .setTitle("Daily Target Alert")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String val = input.getText().toString().trim();
                    int target = val.isEmpty() ? 0 : Integer.parseInt(val);
                    prefs.edit().putInt("daily_target", target).apply();
                    updateAllLabels();
                })
                .setNegativeButton("Cancel", null)
                .setNeutralButton("Turn Off", (dialog, which) -> {
                    prefs.edit().putInt("daily_target", 0).apply();
                    updateAllLabels();
                }).show();
    }

    private void showMinAmountDialog() {
        SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        int currentAmt = prefs.getInt("min_amount", 0);
        int selected = 0;
        for (int i = 0; i < minAmountValues.length; i++) {
            if (minAmountValues[i] == currentAmt) { selected = i; break; }
        }
        final int[] checkedItem = {selected};
        new AlertDialog.Builder(requireContext())
                .setTitle("Minimum Amount")
                .setSingleChoiceItems(minAmounts, selected, (dialog, which) -> checkedItem[0] = which)
                .setPositiveButton("Save", (dialog, which) -> {
                    prefs.edit().putInt("min_amount", minAmountValues[checkedItem[0]]).apply();
                    updateAllLabels();
                })
                .setNegativeButton("Cancel", null).show();
    }

    private void showDndTimePicker() {
        SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        int startHour = prefs.getInt("dnd_start", 22);
        new android.app.TimePickerDialog(requireContext(), (view, hourOfDay, minute) -> {
            prefs.edit().putInt("dnd_start", hourOfDay).apply();
            int endHour = prefs.getInt("dnd_end", 7);
            new android.app.TimePickerDialog(requireContext(), (view2, hourOfDay2, minute2) -> {
                prefs.edit().putInt("dnd_end", hourOfDay2).apply();
                updateAllLabels();
            }, endHour, 0, false).show();
        }, startHour, 0, false).show();
    }

    private void showSummaryTimePicker() {
        SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        int hour = prefs.getInt("summary_hour", 21);
        int minute = prefs.getInt("summary_minute", 0);
        new android.app.TimePickerDialog(requireContext(), (view, hourOfDay, minute1) -> {
            prefs.edit().putInt("summary_hour", hourOfDay).apply();
            prefs.edit().putInt("summary_minute", minute1).apply();
            updateAllLabels();
        }, hour, minute, false).show();
    }

    private void showAutoDeleteDialog() {
        SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        int currentDays = prefs.getInt("auto_delete_days", 0);
        int selected = 0;
        for (int i = 0; i < autoDeleteDays.length; i++) {
            if (autoDeleteDays[i] == currentDays) { selected = i; break; }
        }
        final int[] checkedItem = {selected};
        new AlertDialog.Builder(requireContext())
                .setTitle("Auto-Delete Old Transactions")
                .setSingleChoiceItems(autoDeleteOptions, selected, (dialog, which) -> checkedItem[0] = which)
                .setPositiveButton("Save", (dialog, which) -> {
                    int days = autoDeleteDays[checkedItem[0]];
                    prefs.edit().putInt("auto_delete_days", days).apply();
                    updateAllLabels();
                    if (days > 0) {
                        TransactionRepository.getInstance(requireContext()).pruneOldTransactions(days);
                        Toast.makeText(requireContext(), "Old transactions cleaned up", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null).show();
    }

    private void exportToCsv() {
        AnnounceUPIApp.runInBackground(() -> {
            List<TransactionEntity> list = TransactionRepository.getInstance(requireContext()).getAllSync();
            if (list.isEmpty()) {
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "No transactions to export", Toast.LENGTH_SHORT).show());
                return;
            }

            try {
                File file = new File(requireContext().getCacheDir(), "announceupi_export.csv");
                FileWriter fw = new FileWriter(file);
                fw.append("Date,Time,Source,Amount\n");
                for (TransactionEntity t : list) {
                    fw.append(t.dateText).append(",");
                    fw.append(t.timeText).append(",");
                    fw.append(t.source).append(",");
                    fw.append(String.format(Locale.getDefault(), "%.2f", t.rawAmount));
                    fw.append("\n");
                }
                fw.flush();
                fw.close();

                Uri uri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".provider", file);
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/csv");
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(intent, "Export CSV"));
            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void exportSettings() {
        try {
            SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
            Map<String, ?> allEntries = prefs.getAll();
            JSONObject json = new JSONObject();
            
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                if (entry.getValue() instanceof Set) {
                    json.put(entry.getKey(), new JSONArray((Set<?>) entry.getValue()));
                } else {
                    json.put(entry.getKey(), entry.getValue());
                }
            }

            File file = new File(requireContext().getCacheDir(), "announceupi_settings.json");
            FileWriter fw = new FileWriter(file);
            fw.write(json.toString());
            fw.flush();
            fw.close();

            Uri uri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".provider", file);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/json");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Export Settings"));
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void importSettings(Uri uri) {
        try {
            InputStream is = requireContext().getContentResolver().openInputStream(uri);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            is.close();

            JSONObject json = new JSONObject(sb.toString());
            SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();

            Iterator<String> keys = json.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Object val = json.get(key);
                if (val instanceof Boolean) editor.putBoolean(key, (Boolean) val);
                else if (val instanceof Integer) editor.putInt(key, (Integer) val);
                else if (val instanceof String) editor.putString(key, (String) val);
                else if (val instanceof JSONArray) {
                    JSONArray arr = (JSONArray) val;
                    Set<String> set = new HashSet<>();
                    for (int i = 0; i < arr.length(); i++) set.add(arr.getString(i));
                    editor.putStringSet(key, set);
                }
            }
            editor.apply();
            
            Toast.makeText(requireContext(), "Settings imported! Restarting...", Toast.LENGTH_SHORT).show();
            requireActivity().recreate();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Import failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmClearHistory() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Clear All History")
                .setMessage("Are you sure you want to delete all transactions? This cannot be undone.")
                .setPositiveButton("Clear", (dialog, which) -> {
                    TransactionRepository.getInstance(requireContext()).clearAll();
                    Toast.makeText(requireContext(), "History cleared", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null).show();
    }

    // ---- New: aggregated notification-related settings shortcut ----
    private void showNotificationSettingsDialog() {
        SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);

        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        int pad = dpToPx(20);
        container.setPadding(pad, pad, pad, pad);

        SwitchMaterial dndSwitch = new SwitchMaterial(requireContext());
        dndSwitch.setText("Do Not Disturb");
        dndSwitch.setChecked(prefs.getBoolean("dnd_enabled", false));
        dndSwitch.setOnCheckedChangeListener((b, isChecked) -> prefs.edit().putBoolean("dnd_enabled", isChecked).apply());

        SwitchMaterial summarySwitch = new SwitchMaterial(requireContext());
        summarySwitch.setText("Daily Summary");
        summarySwitch.setChecked(prefs.getBoolean("daily_summary_enabled", true));
        summarySwitch.setOnCheckedChangeListener((b, isChecked) -> prefs.edit().putBoolean("daily_summary_enabled", isChecked).apply());

        container.addView(dndSwitch);
        container.addView(summarySwitch);

        new AlertDialog.Builder(requireContext())
                .setTitle("Notification Settings")
                .setView(container)
                .setPositiveButton("Set DND hours", (dialog, which) -> showDndTimePicker())
                .setNegativeButton("Set summary time", (dialog, which) -> showSummaryTimePicker())
                .setNeutralButton("Done", (dialog, which) -> {
                    updateAllLabels();
                    setupAllSwitches();
                })
                .show();
    }

    // ---- New: static, accurate privacy statement (app stores data locally only) ----
    private void showPrivacyDialog() {
        String message = "AnnounceUPI stores your transactions and settings only on this device, in a local database. "
                + "Nothing is uploaded to a server unless you explicitly export or share it yourself (for example, via Export CSV or Export Settings).\n\n"
                + "Notification and SMS access is used only to detect payment amounts for announcements and is never transmitted anywhere.";

        new AlertDialog.Builder(requireContext())
                .setTitle("Privacy")
                .setMessage(message)
                .setPositiveButton("Manage app permissions", (dialog, which) -> startActivity(new Intent(requireContext(), PermissionsActivity.class)))
                .setNegativeButton("Close", null)
                .show();
    }

    // ---- New: real cache/db size readout with a clear-cache action ----
    private void showStorageUsageDialog() {
        String size = formatBytes(computeCacheAndDbSize());
        new AlertDialog.Builder(requireContext())
                .setTitle("Storage Usage")
                .setMessage("AnnounceUPI is currently using " + size + " of storage for its local database and temporary export files.")
                .setPositiveButton("Clear cache", (dialog, which) -> {
                    clearCacheDir();
                    updateAllLabels();
                    Toast.makeText(requireContext(), "Cache cleared", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private long computeCacheAndDbSize() {
        long total = 0;
        total += dirSize(requireContext().getCacheDir());
        File dbFile = requireContext().getDatabasePath("announceupi.db");
        if (dbFile != null && dbFile.exists()) total += dbFile.length();
        return total;
    }

    private long dirSize(File dir) {
        if (dir == null || !dir.exists()) return 0;
        long size = 0;
        File[] files = dir.listFiles();
        if (files == null) return 0;
        for (File f : files) {
            size += f.isDirectory() ? dirSize(f) : f.length();
        }
        return size;
    }

    private void clearCacheDir() {
        File dir = requireContext().getCacheDir();
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            deleteRecursive(f);
        }
    }

    private void deleteRecursive(File f) {
        if (f.isDirectory()) {
            File[] children = f.listFiles();
            if (children != null) for (File c : children) deleteRecursive(c);
        }
        f.delete();
    }

    private String formatBytes(long bytes) {
        double mb = bytes / (1024.0 * 1024.0);
        if (mb < 0.1) {
            double kb = bytes / 1024.0;
            return String.format(Locale.getDefault(), "%.0f KB", kb);
        }
        return String.format(Locale.getDefault(), "%.1f MB", mb);
    }

    // ---- New: lightweight help dialog that funnels into the existing support email ----
    private void showHelpSupportDialog() {
        String message = "• Announcements not playing? Check App Permissions and make sure notification/SMS access is granted.\n"
                + "• Wrong amounts detected? Try adjusting Minimum Amount or Excluded Apps.\n"
                + "• Want quieter nights? Set up Do Not Disturb hours under Notification Settings.\n\n"
                + "Still stuck? Reach out directly and we'll help you sort it out.";

        new AlertDialog.Builder(requireContext())
                .setTitle("Help & Support")
                .setMessage(message)
                .setPositiveButton("Contact Support", (dialog, which) -> sendSupportEmail())
                .setNegativeButton("Close", null)
                .show();
    }

    private void sendSupportEmail() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:rohitprasadyadav06@gmail.com"));
        try {
            startActivity(Intent.createChooser(emailIntent, "Send Email"));
        } catch (Exception e) {
            Toast.makeText(requireContext(), "No email app found", Toast.LENGTH_SHORT).show();
        }
    }

    private String fmt12h(int hour) {
        String ampm = hour < 12 ? "AM" : "PM";
        int h12 = hour % 12;
        if (h12 == 0) h12 = 12;
        return h12 + " " + ampm;
    }

    private String fmt12hMin(int hour, int minute) {
        String ampm = hour < 12 ? "AM" : "PM";
        int h12 = hour % 12;
        if (h12 == 0) h12 = 12;
        return String.format(Locale.getDefault(), "%d:%02d %s", h12, minute, ampm);
    }

    private void setupSearch() {
        EditText etSearch = root.findViewById(R.id.et_search);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                filterSettings(s.toString().trim());
            }
        });
    }

    private void filterSettings(String query) {
        String q = query.toLowerCase();
        if (q.isEmpty()) {
            processRow(rowAppPermissions, "", "");
            processRow(rowAppearance, "", "");
            processRow(rowLanguage, "", "");
            processRow(rowSpeechSpeed, "", "");
            processRow(rowRepeat, "", "");
            processRow(rowCustomPrefix, "", "");
            processRow(rowBluetoothOnly, "", "");
            processRow(rowTestVoice, "", "");
            processRow(rowExcludedApps, "", "");
            processRow(rowDailyTarget, "", "");
            processRow(rowMinAmount, "", "");
            processRow(rowDnd, "", "");
            processRow(rowDailySummary, "", "");
            processRow(rowSummaryTime, "", "");
            processRow(rowAppLock, "", "");
            processRow(rowStealthMode, "", "");
            processRow(rowIncognito, "", "");
            processRow(rowExportCsv, "", "");
            processRow(rowExportSettings, "", "");
            processRow(rowImportSettings, "", "");
            processRow(rowAutoDelete, "", "");
            processRow(rowClearHistory, "", "");
            processRow(rowEmail, "", "");
            processRow(rowInstagram, "", "");
            processRow(rowVersion, "", "");
            processRow(rowNotificationSettings, "", "");
            processRow(rowPrivacy, "", "");
            processRow(rowStorageUsage, "", "");
            processRow(rowSyncBackup, "", "");
            processRow(rowHelpSupport, "", "");
        } else {
            processRow(rowAppPermissions, "app permissions", q);
            processRow(rowAppearance, "theme and colors", q);
            processRow(rowLanguage, "announcement language", q);
            processRow(rowSpeechSpeed, "speech speed", q);
            processRow(rowRepeat, "repeat announcement", q);
            processRow(rowCustomPrefix, "custom prefix", q);
            processRow(rowBluetoothOnly, "bluetooth", q);
            processRow(rowTestVoice, "test voice", q);
            processRow(rowExcludedApps, "excluded apps", q);
            processRow(rowDailyTarget, "daily target", q);
            processRow(rowMinAmount, "minimum amount", q);
            processRow(rowDnd, "do not disturb", q);
            processRow(rowDailySummary, "daily summary", q);
            processRow(rowSummaryTime, "summary time", q);
            processRow(rowAppLock, "app lock", q);
            processRow(rowStealthMode, "stealth mode", q);
            processRow(rowIncognito, "invisible incognito", q);
            processRow(rowExportCsv, "export transactions", q);
            processRow(rowExportSettings, "export app settings", q);
            processRow(rowImportSettings, "import app settings", q);
            processRow(rowAutoDelete, "auto delete", q);
            processRow(rowClearHistory, "clear history", q);
            processRow(rowEmail, "email developer", q);
            processRow(rowInstagram, "instagram", q);
            processRow(rowVersion, "version", q);
            processRow(rowNotificationSettings, "notification settings alerts", q);
            processRow(rowPrivacy, "privacy", q);
            processRow(rowStorageUsage, "storage usage cache", q);
            processRow(rowSyncBackup, "sync and backup", q);
            processRow(rowHelpSupport, "help and support faq", q);
        }
    }

    private void processRow(View row, String matchStr, String q) {
        if (q.isEmpty()) {
            row.setAlpha(1.0f);
            highlightTextInView(row, "");
        } else if (matchStr.contains(q)) {
            row.setAlpha(1.0f);
            highlightTextInView(row, q);
        } else {
            row.setAlpha(0.3f);
            highlightTextInView(row, "");
        }
    }

    private void highlightTextInView(View v, String query) {
        if (v instanceof TextView) {
            TextView tv = (TextView) v;
            String text = tv.getText().toString();
            SpannableStringBuilder ssb = new SpannableStringBuilder(text);
            
            if (!query.isEmpty()) {
                String lowerText = text.toLowerCase();
                String lowerQuery = query.toLowerCase();
                int idx = lowerText.indexOf(lowerQuery);
                while (idx >= 0) {
                    int color = getThemeColor(android.R.attr.colorPrimary);
                    ssb.setSpan(new ForegroundColorSpan(color), idx, idx + query.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    idx = lowerText.indexOf(lowerQuery, idx + query.length());
                }
            }
            tv.setText(ssb);
        } else if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                highlightTextInView(vg.getChildAt(i), query);
            }
        }
    }

    private int getThemeColor(int attr) {
        TypedValue typedValue = new TypedValue();
        requireContext().getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        root = null;
    }
}
