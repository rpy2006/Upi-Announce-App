package com.upiannounce.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardFragment extends Fragment {

    private enum Period { DAILY, WEEKLY, MONTHLY }

    private static final int[] SOURCE_COLORS = {
        0xFF3D8BFF, 0xFF7C4DFF, 0xFF00BFA5, 0xFFFF9800, 0xFFEF5350, 0xFF34D399
    };

    private View root;
    private List<TransactionEntity> allTransactions = new ArrayList<>();
    private Period period = Period.DAILY;
    private final SimpleDateFormat dayFmt = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupTopBar();
        setupToggle();
        setupGoalEditor();
        setupPeakInfo();

        TransactionRepository.getInstance(requireContext()).getAllLive()
            .observe(getViewLifecycleOwner(), this::onDataChanged);
    }

    private void setupTopBar() {
        ImageButton bell = root.findViewById(R.id.btn_bell);
        ImageButton settings = root.findViewById(R.id.btn_settings);

        bell.setOnClickListener(v -> Toast.makeText(requireContext(), "Notification settings coming soon", Toast.LENGTH_SHORT).show());

        settings.setOnClickListener(v -> {
            com.google.android.material.bottomnavigation.BottomNavigationView nav = requireActivity().findViewById(R.id.bottom_nav);
            if (nav != null) nav.setSelectedItemId(R.id.nav_settings);
        });
    }

    private void setupToggle() {
        TextView daily = root.findViewById(R.id.toggle_daily);
        TextView weekly = root.findViewById(R.id.toggle_weekly);
        TextView monthly = root.findViewById(R.id.toggle_monthly);

        daily.setOnClickListener(v -> selectPeriod(Period.DAILY, daily, weekly, monthly));
        weekly.setOnClickListener(v -> selectPeriod(Period.WEEKLY, weekly, daily, monthly));
        monthly.setOnClickListener(v -> selectPeriod(Period.MONTHLY, monthly, daily, weekly));
    }

    private void selectPeriod(Period newPeriod, TextView active, TextView... inactive) {
        if (period == newPeriod) return;
        period = newPeriod;

        active.setBackgroundResource(R.drawable.bg_toggle_active);
        active.setTextColor(resolveThemeColor(android.R.attr.textColorPrimary));
        active.setTypeface(active.getTypeface(), android.graphics.Typeface.BOLD);

        for (TextView tv : inactive) {
            tv.setBackground(null);
            tv.setTypeface(android.graphics.Typeface.DEFAULT);
            tv.setTextColor(resolveThemeColor(com.google.android.material.R.attr.colorOnSurfaceVariant));
        }

        render();
    }

    private void setupGoalEditor() {
        View.OnClickListener openGoalDialog = v -> {
            SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
            int currentTarget = prefs.getInt("daily_target", 0);

            EditText input = new EditText(requireContext());
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            input.setHint("Daily target amount");
            if (currentTarget > 0) input.setText(String.valueOf(currentTarget));

            new AlertDialog.Builder(requireContext())
                .setTitle("Set daily target")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String text = input.getText().toString().trim();
                    int newTarget = text.isEmpty() ? 0 : Integer.parseInt(text);
                    prefs.edit().putInt("daily_target", newTarget).apply();
                    render();
                })
                .setNegativeButton("Cancel", null)
                .show();
        };

        root.findViewById(R.id.tv_ring_goal).setOnClickListener(openGoalDialog);
    }

    private void setupPeakInfo() {
        ImageButton info = root.findViewById(R.id.btn_peak_info);
        info.setOnClickListener(v -> Toast.makeText(requireContext(),
            "Shows which hour of the day usually brings in the most payments.", Toast.LENGTH_LONG).show());
    }

    private void onDataChanged(List<TransactionEntity> list) {
        allTransactions = list;
        render();
    }

    private void render() {
        if (root == null) return;
        List<TransactionEntity> scoped = scopedTransactions();
        renderRing(scoped);
        renderInsight(scoped);
        renderTrend();
        renderTopSources(scoped);
        renderPeakHour(scoped);
        renderQuickStats(scoped);
    }

    /** Returns transactions falling within the currently selected D/W/M window. */
    private List<TransactionEntity> scopedTransactions() {
        Calendar cutoff = Calendar.getInstance();
        cutoff.set(Calendar.HOUR_OF_DAY, 0);
        cutoff.set(Calendar.MINUTE, 0);
        cutoff.set(Calendar.SECOND, 0);
        cutoff.set(Calendar.MILLISECOND, 0);

        if (period == Period.WEEKLY) cutoff.add(Calendar.DAY_OF_YEAR, -6);
        else if (period == Period.MONTHLY) cutoff.add(Calendar.DAY_OF_YEAR, -29);

        long cutoffMillis = cutoff.getTimeInMillis();
        List<TransactionEntity> result = new ArrayList<>();
        for (TransactionEntity t : allTransactions) {
            if (t.timestamp >= cutoffMillis) result.add(t);
        }
        return result;
    }

    private double sum(List<TransactionEntity> list) {
        double total = 0;
        for (TransactionEntity t : list) total += t.rawAmount;
        return total;
    }

    private void renderRing(List<TransactionEntity> scoped) {
        double total = sum(scoped);
        SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        boolean isStealth = prefs.getBoolean("stealth_mode", false);
        int dailyTarget = prefs.getInt("daily_target", 0);

        int goal = period == Period.DAILY ? dailyTarget
            : period == Period.WEEKLY ? dailyTarget * 7
            : dailyTarget * 30;

        RingProgressView ring = root.findViewById(R.id.ring_progress);
        TextView tvPercent = root.findViewById(R.id.tv_ring_percent);
        TextView tvAmount = root.findViewById(R.id.tv_ring_amount);
        TextView tvGoal = root.findViewById(R.id.tv_ring_goal);
        TextView chipPeriod = root.findViewById(R.id.chip_period);

        chipPeriod.setText(period == Period.DAILY ? "Today" : period == Period.WEEKLY ? "This week" : "This month");

        if (isStealth) {
            tvAmount.setText("₹***");
            tvAmount.setOnClickListener(v -> {
                tvAmount.setText("₹" + String.format(Locale.getDefault(), "%,.0f", total));
                tvAmount.postDelayed(() -> { if (root != null) tvAmount.setText("₹***"); }, 3000);
            });
        } else {
            tvAmount.setOnClickListener(null);
            tvAmount.setText("₹" + String.format(Locale.getDefault(), "%,.0f", total));
        }

        if (goal > 0) {
            tvGoal.setText("of ₹" + String.format(Locale.getDefault(), "%,d", goal));
            int pct = (int) Math.round((total / goal) * 100);
            tvPercent.setText(pct + "%");
            tvPercent.setVisibility(View.VISIBLE);
            ring.animateProgressTo((float) (total / goal));
        } else {
            tvGoal.setText("Tap to set a target");
            tvPercent.setVisibility(View.GONE);
            ring.animateProgressTo(0f);
        }

        String today = dayFmt.format(new Date());
        if (period == Period.DAILY && goal > 0 && total >= goal) {
            ring.setRingColor(Color.parseColor("#3DDC84"));
            tvAmount.setTextColor(Color.parseColor("#3DDC84"));
            String achievedDate = prefs.getString("target_achieved_date", "");
            if (!achievedDate.equals(today)) {
                prefs.edit().putString("target_achieved_date", today).apply();
                TTSManager.getInstance().announceTargetReached();
            }
        } else {
            ring.setRingColor(Color.parseColor("#F5F6F7"));
            tvAmount.setTextColor(resolveThemeColor(android.R.attr.textColorPrimary));
        }
    }

    private void renderInsight(List<TransactionEntity> scoped) {
        TextView insight = root.findViewById(R.id.tv_insight);
        double currentTotal = sum(scoped);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0);

        String comparisonLabel;
        Calendar prevStart, prevEnd;
        if (period == Period.DAILY) {
            prevEnd = (Calendar) cal.clone();
            prevStart = (Calendar) cal.clone(); prevStart.add(Calendar.DAY_OF_YEAR, -1);
            comparisonLabel = "yesterday";
        } else if (period == Period.WEEKLY) {
            prevEnd = (Calendar) cal.clone(); prevEnd.add(Calendar.DAY_OF_YEAR, -6);
            prevStart = (Calendar) prevEnd.clone(); prevStart.add(Calendar.DAY_OF_YEAR, -6);
            comparisonLabel = "last week";
        } else {
            prevEnd = (Calendar) cal.clone(); prevEnd.add(Calendar.DAY_OF_YEAR, -29);
            prevStart = (Calendar) prevEnd.clone(); prevStart.add(Calendar.DAY_OF_YEAR, -29);
            comparisonLabel = "last month";
        }

        double prevTotal = 0;
        for (TransactionEntity t : allTransactions) {
            if (t.timestamp >= prevStart.getTimeInMillis() && t.timestamp < prevEnd.getTimeInMillis()) {
                prevTotal += t.rawAmount;
            }
        }

        if (prevTotal <= 0) {
            insight.setText(currentTotal > 0 ? "This is your first period with payment activity" : "No payments recorded yet");
            return;
        }

        double pct = ((currentTotal - prevTotal) / prevTotal) * 100.0;
        String verb = pct >= 0 ? "more" : "less";
        insight.setText("You've received " + String.format(Locale.getDefault(), "%.0f", Math.abs(pct))
            + "% " + verb + " than " + comparisonLabel);
    }

    private void renderTrend() {
        SimpleDateFormat dayKeyFmt = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        SimpleDateFormat labelFmt = new SimpleDateFormat("EEE", Locale.getDefault());

        Map<String, Double> totalsByDay = new LinkedHashMap<>();
        List<String> labels = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -6);

        for (int i = 0; i < 7; i++) {
            String key = dayKeyFmt.format(cal.getTime());
            totalsByDay.put(key, 0.0);
            labels.add(labelFmt.format(cal.getTime()).substring(0, Math.min(3, labelFmt.format(cal.getTime()).length())));
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        for (TransactionEntity t : allTransactions) {
            if (totalsByDay.containsKey(t.dateText)) {
                totalsByDay.put(t.dateText, totalsByDay.get(t.dateText) + t.rawAmount);
            }
        }

        float[] values = new float[7];
        double weekTotal = 0;
        int i = 0;
        for (double v : totalsByDay.values()) {
            values[i++] = (float) v;
            weekTotal += v;
        }

        SparklineView sparkline = root.findViewById(R.id.sparkline_view);
        sparkline.setValues(values);

        TextView trendTotal = root.findViewById(R.id.tv_trend_total);
        trendTotal.setText("Total: ₹" + String.format(Locale.getDefault(), "%,.0f", weekTotal));

        LinearLayout labelsRow = root.findViewById(R.id.trend_labels_row);
        labelsRow.removeAllViews();
        for (String label : labels) {
            TextView tv = new TextView(requireContext());
            tv.setText(label);
            tv.setTextSize(9.5f);
            tv.setTextColor(resolveThemeColor(com.google.android.material.R.attr.colorOnSurfaceVariant));
            tv.setGravity(android.view.Gravity.CENTER);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            tv.setLayoutParams(lp);
            labelsRow.addView(tv);
        }
    }

    private void renderTopSources(List<TransactionEntity> scoped) {
        LinearLayout container = root.findViewById(R.id.top_sources_container);
        container.removeAllViews();

        Map<String, Double> totalsBySource = new LinkedHashMap<>();
        double grandTotal = 0;
        for (TransactionEntity t : scoped) {
            String key = t.source == null || t.source.isEmpty() ? "Unknown" : t.source;
            totalsBySource.put(key, totalsBySource.getOrDefault(key, 0.0) + t.rawAmount);
            grandTotal += t.rawAmount;
        }

        if (grandTotal <= 0) {
            TextView empty = new TextView(requireContext());
            empty.setText("No payments in this period");
            empty.setTextSize(11.5f);
            empty.setTextColor(resolveThemeColor(com.google.android.material.R.attr.colorOnSurfaceVariant));
            container.addView(empty);
            return;
        }

        List<Map.Entry<String, Double>> sorted = new ArrayList<>(totalsBySource.entrySet());
        sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        double shownTotal = 0;
        int shown = Math.min(3, sorted.size());
        float density = getResources().getDisplayMetrics().density;

        for (int i = 0; i < shown; i++) {
            Map.Entry<String, Double> entry = sorted.get(i);
            int pct = (int) Math.round((entry.getValue() / grandTotal) * 100);
            shownTotal += entry.getValue();
            container.addView(buildSourceRow(entry.getKey(), pct, SOURCE_COLORS[i % SOURCE_COLORS.length], density));
        }

        int othersPct = (int) Math.round(((grandTotal - shownTotal) / grandTotal) * 100);
        if (sorted.size() > shown || othersPct > 0) {
            LinearLayout othersRow = new LinearLayout(requireContext());
            othersRow.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            rowLp.topMargin = (int) (8 * density);
            othersRow.setLayoutParams(rowLp);
            othersRow.setPadding(0, (int) (8 * density), 0, 0);

            TextView label = new TextView(requireContext());
            label.setText("Others");
            label.setTextSize(10.5f);
            label.setTextColor(resolveThemeColor(com.google.android.material.R.attr.colorOnSurfaceVariant));
            LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            label.setLayoutParams(labelLp);

            TextView pctView = new TextView(requireContext());
            pctView.setText(Math.max(othersPct, 0) + "%");
            pctView.setTextSize(10.5f);
            pctView.setTextColor(resolveThemeColor(com.google.android.material.R.attr.colorOnSurfaceVariant));

            othersRow.addView(label);
            othersRow.addView(pctView);
            container.addView(othersRow);
        }
    }

    private View buildSourceRow(String name, int pct, int color, float density) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rowLp.bottomMargin = (int) (9 * density);
        row.setLayoutParams(rowLp);

        View dot = new View(requireContext());
        int dotSize = (int) (20 * density);
        LinearLayout.LayoutParams dotLp = new LinearLayout.LayoutParams(dotSize, dotSize);
        dotLp.setMarginEnd((int) (8 * density));
        dot.setLayoutParams(dotLp);
        GradientDrawable dotBg = new GradientDrawable();
        dotBg.setShape(GradientDrawable.OVAL);
        dotBg.setColor(color);
        dot.setBackground(dotBg);

        LinearLayout textCol = new LinearLayout(requireContext());
        textCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textColLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        textCol.setLayoutParams(textColLp);

        TextView nameView = new TextView(requireContext());
        nameView.setText(name);
        nameView.setTextSize(11f);
        nameView.setTextColor(resolveThemeColor(android.R.attr.textColorPrimary));

        View track = new View(requireContext());
        LinearLayout.LayoutParams trackLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) (3 * density));
        trackLp.topMargin = (int) (3 * density);
        track.setLayoutParams(trackLp);
        GradientDrawable trackBg = new GradientDrawable();
        trackBg.setCornerRadius(2 * density);
        trackBg.setColor(withAlpha(resolveThemeColor(com.google.android.material.R.attr.colorOnSurfaceVariant), 40));
        track.setBackground(trackBg);

        textCol.addView(nameView);
        textCol.addView(track);

        TextView pctView = new TextView(requireContext());
        pctView.setText(pct + "%");
        pctView.setTextSize(10.5f);
        pctView.setTextColor(resolveThemeColor(com.google.android.material.R.attr.colorOnSurfaceVariant));
        LinearLayout.LayoutParams pctLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        pctLp.setMarginStart((int) (8 * density));
        pctView.setLayoutParams(pctLp);

        row.addView(dot);
        row.addView(textCol);
        row.addView(pctView);
        return row;
    }

    private void renderPeakHour(List<TransactionEntity> scoped) {
        int[] hourCounts = new int[24];
        double[] hourAmounts = new double[24];
        Calendar cal = Calendar.getInstance();

        for (TransactionEntity t : scoped) {
            cal.setTimeInMillis(t.timestamp);
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            hourCounts[hour]++;
            hourAmounts[hour] += t.rawAmount;
        }

        int maxCount = 1, peakHour = -1;
        for (int i = 0; i < 24; i++) {
            if (hourCounts[i] > maxCount) { maxCount = hourCounts[i]; peakHour = i; }
        }

        LinearLayout barContainer = root.findViewById(R.id.peak_bar_container);
        barContainer.removeAllViews();

        int accentColor = resolveThemeColor(android.R.attr.colorPrimary);
        int mutedColor = resolveThemeColor(com.google.android.material.R.attr.colorOnSurfaceVariant);
        float density = getResources().getDisplayMetrics().density;

        for (int i = 0; i < 24; i++) {
            float fraction = hourCounts[i] / (float) maxCount;
            int barHeight = Math.max((int) (fraction * 46 * density), (int) (2 * density));

            GradientDrawable shape = new GradientDrawable();
            shape.setCornerRadius(2 * density);
            boolean isPeak = (i == peakHour && hourCounts[i] > 0);
            shape.setColor(isPeak ? accentColor : withAlpha(mutedColor, 40));

            LinearLayout wrapper = new LinearLayout(requireContext());
            wrapper.setOrientation(LinearLayout.VERTICAL);
            wrapper.setGravity(android.view.Gravity.BOTTOM);
            LinearLayout.LayoutParams wrapLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
            wrapper.setLayoutParams(wrapLp);

            View bar = new View(requireContext());
            LinearLayout.LayoutParams barLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, barHeight);
            barLp.setMarginEnd((int) (1 * density));
            bar.setLayoutParams(barLp);
            bar.setBackground(shape);

            wrapper.addView(bar);
            barContainer.addView(wrapper);
        }

        TextView bestHourStat = root.findViewById(R.id.tv_stat_best_hour);
        TextView peakTimeStat = root.findViewById(R.id.tv_stat_peak_time);
        if (peakHour >= 0) {
            Calendar peakCal = Calendar.getInstance();
            peakCal.set(Calendar.HOUR_OF_DAY, peakHour);
            peakCal.set(Calendar.MINUTE, 0);
            String peakLabel = new SimpleDateFormat("h a", Locale.getDefault()).format(peakCal.getTime());
            peakTimeStat.setText(peakLabel);
            bestHourStat.setText("₹" + String.format(Locale.getDefault(), "%,.0f", hourAmounts[peakHour]));
        } else {
            peakTimeStat.setText("--");
            bestHourStat.setText("₹0");
        }
    }

    private void renderQuickStats(List<TransactionEntity> scoped) {
        TextView paymentsStat = root.findViewById(R.id.tv_stat_payments);
        TextView averageStat = root.findViewById(R.id.tv_stat_average);

        int count = scoped.size();
        double total = sum(scoped);
        double average = count > 0 ? total / count : 0;

        paymentsStat.setText(String.valueOf(count));
        averageStat.setText("₹" + String.format(Locale.getDefault(), "%,.0f", average));
    }

    private int withAlpha(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    private int resolveThemeColor(int attr) {
        android.util.TypedValue tv = new android.util.TypedValue();
        requireContext().getTheme().resolveAttribute(attr, tv, true);
        return tv.data;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        root = null;
    }
}
