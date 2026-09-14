package com.upiannounce.app;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private TransactionAdapter adapter;
    private ValueAnimator amountAnimator;
    private double currentDisplayedAmount = 0;
    private View root;

    private List<TransactionEntity> masterList = new ArrayList<>();
    private boolean showTodayChart = true;
    private String pendingSearchQuery = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_home, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new TransactionAdapter();
        RecyclerView rv = root.findViewById(R.id.rv_transactions);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setNestedScrollingEnabled(false);
        rv.setAdapter(adapter);

        setupSwipeToDelete(rv);
        setupTopBar();
        setupToggle();
        setupSearch();
        updateGreeting();

        TransactionRepository.getInstance(requireContext()).getAllLive()
            .observe(getViewLifecycleOwner(), this::updateUI);
    }

    private void setupTopBar() {
        ImageButton bell = root.findViewById(R.id.btn_bell);
        ImageButton settings = root.findViewById(R.id.btn_settings);
        TextView seeAll = root.findViewById(R.id.tv_see_all);

        bell.setOnClickListener(v -> Toast.makeText(requireContext(), "Notification settings coming soon", Toast.LENGTH_SHORT).show());

        settings.setOnClickListener(v -> {
            BottomNavigationView nav = requireActivity().findViewById(R.id.bottom_nav);
            if (nav != null) nav.setSelectedItemId(R.id.nav_settings);
        });

        seeAll.setOnClickListener(v -> {
            BottomNavigationView nav = requireActivity().findViewById(R.id.bottom_nav);
            if (nav != null) nav.setSelectedItemId(R.id.nav_dashboard);
        });
    }

    private void setupToggle() {
        TextView today = root.findViewById(R.id.toggle_today);
        TextView total = root.findViewById(R.id.toggle_total);

        today.setOnClickListener(v -> {
            if (showTodayChart) return;
            showTodayChart = true;
            applyToggleStyle(today, total);
            renderPeakHourChart(masterList);
        });

        total.setOnClickListener(v -> {
            if (!showTodayChart) return;
            showTodayChart = false;
            applyToggleStyle(total, today);
            renderPeakHourChart(masterList);
        });
    }

    private void applyToggleStyle(TextView active, TextView inactive) {
        active.setBackgroundResource(R.drawable.bg_toggle_active);
        active.setTextColor(resolveThemeColor(android.R.attr.textColorPrimary));
        active.setTypeface(active.getTypeface(), android.graphics.Typeface.BOLD);

        inactive.setBackground(null);
        inactive.setTypeface(android.graphics.Typeface.DEFAULT);
        inactive.setTextColor(resolveThemeColor(com.google.android.material.R.attr.colorOnSurfaceVariant));
    }

    private void setupSearch() {
        EditText search = root.findViewById(R.id.et_search);
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                pendingSearchQuery = s.toString().trim().toLowerCase(Locale.getDefault());
                applyFilter();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void applyFilter() {
        if (pendingSearchQuery.isEmpty()) {
            adapter.submitList(new ArrayList<>(masterList));
            root.findViewById(R.id.tv_empty).setVisibility(masterList.isEmpty() ? View.VISIBLE : View.GONE);
            return;
        }
        List<TransactionEntity> filtered = new ArrayList<>();
        for (TransactionEntity t : masterList) {
            boolean matches = (t.source != null && t.source.toLowerCase(Locale.getDefault()).contains(pendingSearchQuery))
                || (t.amountText != null && t.amountText.toLowerCase(Locale.getDefault()).contains(pendingSearchQuery));
            if (matches) filtered.add(t);
        }
        adapter.submitList(filtered);
        root.findViewById(R.id.tv_empty).setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void updateGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting;
        String emoji;
        if (hour < 12) { greeting = "Good morning"; emoji = "☕"; }
        else if (hour < 17) { greeting = "Good afternoon"; emoji = "\u2600\uFE0F"; }
        else { greeting = "Good evening"; emoji = "\uD83C\uDF19"; }

        TextView tvGreeting = root.findViewById(R.id.tv_greeting);
        tvGreeting.setText(greeting + " " + emoji);
    }

    private void setupSwipeToDelete(RecyclerView rv) {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            private final ColorDrawable background = new ColorDrawable(Color.parseColor("#FF3B30"));
            private final Drawable deleteIcon = androidx.core.content.ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_menu_delete);
            private final int intrinsicWidth = deleteIcon != null ? deleteIcon.getIntrinsicWidth() : 0;
            private final int intrinsicHeight = deleteIcon != null ? deleteIcon.getIntrinsicHeight() : 0;

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    TransactionEntity txn = adapter.getCurrentList().get(position);
                    TransactionRepository.getInstance(requireContext()).deleteById(txn.id);
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;
                int itemHeight = itemView.getHeight();

                background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                background.draw(c);

                int iconTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
                int iconMargin = (itemHeight - intrinsicHeight) / 2;
                int iconLeft = itemView.getRight() - iconMargin - intrinsicWidth;
                int iconRight = itemView.getRight() - iconMargin;
                int iconBottom = iconTop + intrinsicHeight;

                if (deleteIcon != null) {
                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                    deleteIcon.draw(c);
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        new ItemTouchHelper(simpleCallback).attachToRecyclerView(rv);
    }

    private void updateUI(List<TransactionEntity> list) {
        if (root == null) return;

        masterList = list;
        applyFilter();
        updateGreetingSubtitle(list);
        updateTotalAmount(list);
        renderPeakHourChart(list);
    }

    private void updateGreetingSubtitle(List<TransactionEntity> list) {
        String today = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
        int todayCount = 0;
        for (TransactionEntity t : list) if (today.equals(t.dateText)) todayCount++;

        TextView subtitle = root.findViewById(R.id.tv_greeting_subtitle);
        subtitle.setText(todayCount == 0
            ? "No payments received today yet."
            : "You have received " + todayCount + (todayCount == 1 ? " payment today." : " payments today."));
    }

    private void updateTotalAmount(List<TransactionEntity> list) {
        String todayStr = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        String yesterdayStr = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(cal.getTime());

        double allTimeTotal = 0, todayTotal = 0, yesterdayTotal = 0;
        int todayCount = 0;
        for (TransactionEntity t : list) {
            allTimeTotal += t.rawAmount;
            if (todayStr.equals(t.dateText)) { todayTotal += t.rawAmount; todayCount++; }
            else if (yesterdayStr.equals(t.dateText)) { yesterdayTotal += t.rawAmount; }
        }

        final double finalTotal = allTimeTotal;

        SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        boolean isStealth = prefs.getBoolean("stealth_mode", false);
        TextView tvAmount = root.findViewById(R.id.tv_total_amount);

        if (isStealth) {
            if (amountAnimator != null && amountAnimator.isRunning()) amountAnimator.cancel();
            tvAmount.setText("₹***");
            tvAmount.setOnClickListener(v -> {
                tvAmount.setText("₹" + String.format(Locale.getDefault(), "%,.0f", finalTotal));
                tvAmount.postDelayed(() -> { if (root != null) tvAmount.setText("₹***"); }, 3000);
            });
        } else {
            tvAmount.setOnClickListener(null);
            if (amountAnimator != null && amountAnimator.isRunning()) amountAnimator.cancel();
            amountAnimator = ValueAnimator.ofFloat((float) currentDisplayedAmount, (float) finalTotal);
            amountAnimator.setDuration(400);
            amountAnimator.addUpdateListener(animation -> {
                if (root == null) return;
                float value = (float) animation.getAnimatedValue();
                ((TextView) root.findViewById(R.id.tv_total_amount)).setText("₹" + String.format(Locale.getDefault(), "%,.0f", value));
            });
            amountAnimator.start();
        }
        currentDisplayedAmount = finalTotal;

        updateDeltaChip(todayTotal, yesterdayTotal, todayCount);
    }

    private void updateDeltaChip(double todayTotal, double yesterdayTotal, int todayCount) {
        LinearLayout container = root.findViewById(R.id.delta_container);
        TextView tvDelta = root.findViewById(R.id.tv_delta);

        if (todayCount == 0) {
            container.setVisibility(View.GONE);
            return;
        }
        container.setVisibility(View.VISIBLE);

        String paymentsLabel = todayCount + (todayCount == 1 ? " payment" : " payments");
        int positiveGreen = Color.parseColor("#3DDC84");
        int negativeRed = Color.parseColor("#FF3B30");

        if (yesterdayTotal <= 0) {
            tvDelta.setText("New today · " + paymentsLabel);
            tvDelta.setTextColor(positiveGreen);
            container.setBackgroundColor(Color.parseColor("#243DDC84"));
        } else {
            double pct = ((todayTotal - yesterdayTotal) / yesterdayTotal) * 100.0;
            boolean up = pct >= 0;
            String arrow = up ? "↑" : "↓";
            tvDelta.setText(arrow + " " + String.format(Locale.getDefault(), "%.0f", Math.abs(pct)) + "% vs yesterday · " + paymentsLabel);
            tvDelta.setTextColor(up ? positiveGreen : negativeRed);
            container.setBackgroundColor(up ? Color.parseColor("#243DDC84") : Color.parseColor("#24FF3B30"));
        }
    }

    private void renderPeakHourChart(List<TransactionEntity> source) {
        if (root == null) return;

        String todayStr = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
        int[] hourCounts = new int[24];
        int totalInScope = 0;
        Calendar cal = Calendar.getInstance();

        for (TransactionEntity t : source) {
            if (showTodayChart && !todayStr.equals(t.dateText)) continue;
            cal.setTimeInMillis(t.timestamp);
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            hourCounts[hour]++;
            totalInScope++;
        }

        TextView title = root.findViewById(R.id.tv_peak_hour_title);
        title.setText(showTodayChart ? "Peak Hour (Today)" : "Peak Hour (All time)");

        View liveDot = root.findViewById(R.id.live_dot);
        TextView liveLabel = root.findViewById(R.id.tv_live_label);
        int liveVisibility = (showTodayChart && totalInScope > 0) ? View.VISIBLE : View.GONE;
        liveDot.setVisibility(liveVisibility);
        liveLabel.setVisibility(liveVisibility);

        LinearLayout barContainer = root.findViewById(R.id.bar_chart_container);
        barContainer.removeAllViews();

        int maxCount = 1;
        int peakHour = -1;
        for (int i = 0; i < 24; i++) {
            if (hourCounts[i] > maxCount) { maxCount = hourCounts[i]; peakHour = i; }
            else if (hourCounts[i] == maxCount && hourCounts[i] > 0 && peakHour == -1) { peakHour = i; }
        }

        int accentColor = resolveThemeColor(android.R.attr.colorPrimary);
        int mutedColor = resolveThemeColor(com.google.android.material.R.attr.colorOnSurfaceVariant);
        float density = getResources().getDisplayMetrics().density;

        for (int i = 0; i < 24; i++) {
            float fraction = hourCounts[i] / (float) maxCount;
            int barHeight = Math.max((int) (fraction * 56 * density), (int) (3 * density));

            GradientDrawable shape = new GradientDrawable();
            shape.setCornerRadius(3 * density);
            boolean isPeak = (i == peakHour && hourCounts[i] > 0);
            shape.setColor(isPeak ? accentColor : withAlpha(mutedColor, 40));

            LinearLayout wrapper = new LinearLayout(requireContext());
            wrapper.setOrientation(LinearLayout.VERTICAL);
            wrapper.setGravity(android.view.Gravity.BOTTOM);
            LinearLayout.LayoutParams wrapLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
            wrapper.setLayoutParams(wrapLp);

            View sizedBar = new View(requireContext());
            LinearLayout.LayoutParams barLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, barHeight);
            barLp.setMarginEnd((int) (1.5f * density));
            sizedBar.setLayoutParams(barLp);
            sizedBar.setBackground(shape);

            wrapper.addView(sizedBar);
            barContainer.addView(wrapper);
        }

        TextView hint = root.findViewById(R.id.tv_peak_hint);
        if (totalInScope == 0) {
            hint.setText("No payment data yet");
        } else {
            Calendar peakCal = Calendar.getInstance();
            peakCal.set(Calendar.HOUR_OF_DAY, Math.max(peakHour, 0));
            peakCal.set(Calendar.MINUTE, 0);
            String peakLabel = new SimpleDateFormat("h a", Locale.getDefault()).format(peakCal.getTime());
            hint.setText(peakLabel + " busiest · " + maxCount + (maxCount == 1 ? " payment" : " payments"));
        }
    }

    private int withAlpha(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    private int resolveThemeColor(int attr) {
        TypedValue tv = new TypedValue();
        requireContext().getTheme().resolveAttribute(attr, tv, true);
        return tv.data;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (amountAnimator != null && amountAnimator.isRunning()) amountAnimator.cancel();
        root = null;
    }
}
