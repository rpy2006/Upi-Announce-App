package com.upiannounce.app;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import java.util.HashSet;
import java.util.Set;

public class TransactionAdapter extends ListAdapter<TransactionEntity, TransactionAdapter.TxnViewHolder> {

    // Row ids currently expanded to show the detail panel.
    private final Set<Long> expandedIds = new HashSet<>();

    // Small fixed palette used to color-code avatars per source app/bank so
    // rows stay visually distinguishable without reproducing any real logos.
    private static final int[] AVATAR_COLORS = {
        0xFF3D8BFF, // blue
        0xFF7C4DFF, // violet
        0xFF00BFA5, // teal
        0xFFFF9800, // amber
        0xFFEF5350, // red
        0xFF34D399  // green
    };

    protected TransactionAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<TransactionEntity> DIFF_CALLBACK =
        new DiffUtil.ItemCallback<TransactionEntity>() {
            @Override
            public boolean areItemsTheSame(TransactionEntity o, TransactionEntity n) { return o.id == n.id; }
            @Override
            public boolean areContentsTheSame(TransactionEntity o, TransactionEntity n) {
                return o.id == n.id && o.amountText.equals(n.amountText) && o.source.equals(n.source);
            }
        };

    @NonNull
    @Override
    public TxnViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TxnViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TxnViewHolder holder, int position) {
        TransactionEntity txn = getItem(position);
        Context ctx = holder.itemView.getContext();

        holder.amount.setText("+" + txn.amountText);
        holder.source.setText(txn.source);
        holder.date.setText(txn.dateText + ", " + txn.timeText);
        holder.detailSource.setText(txn.source);
        holder.detailAmount.setText(txn.amountText);

        String initial = txn.source == null || txn.source.isEmpty() ? "?" : String.valueOf(Character.toUpperCase(txn.source.charAt(0)));
        holder.avatar.setText(initial);
        int color = AVATAR_COLORS[Math.floorMod(txn.source == null ? 0 : txn.source.hashCode(), AVATAR_COLORS.length)];
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(color);
        holder.avatar.setBackground(bg);

        boolean expanded = expandedIds.contains(txn.id);
        holder.detailPanel.setVisibility(expanded ? View.VISIBLE : View.GONE);
        holder.chevron.setRotation(expanded ? 180f : 0f);

        holder.rowSummary.setOnClickListener(v -> {
            if (expandedIds.contains(txn.id)) {
                expandedIds.remove(txn.id);
            } else {
                expandedIds.add(txn.id);
            }
            notifyItemChanged(holder.getBindingAdapterPosition());
        });

        holder.btnCopy.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) {
                cm.setPrimaryClip(ClipData.newPlainText("Transaction", txn.amountText + " from " + txn.source));
                Toast.makeText(ctx, "Copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });

        holder.btnShare.setOnClickListener(v -> {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_TEXT, "Received " + txn.amountText + " via " + txn.source
                + " on " + txn.dateText + ", " + txn.timeText);
            ctx.startActivity(Intent.createChooser(share, "Share transaction"));
        });
    }

    static class TxnViewHolder extends RecyclerView.ViewHolder {
        TextView amount, source, date, avatar, chevron, detailSource, detailAmount;
        LinearLayout rowSummary, detailPanel, btnCopy, btnShare;

        TxnViewHolder(@NonNull View itemView) {
            super(itemView);
            amount = itemView.findViewById(R.id.tv_amount);
            source = itemView.findViewById(R.id.tv_source);
            date = itemView.findViewById(R.id.tv_date);
            avatar = itemView.findViewById(R.id.tv_avatar);
            chevron = itemView.findViewById(R.id.tv_chevron);
            detailSource = itemView.findViewById(R.id.tv_detail_source);
            detailAmount = itemView.findViewById(R.id.tv_detail_amount);
            rowSummary = itemView.findViewById(R.id.row_summary);
            detailPanel = itemView.findViewById(R.id.detail_panel);
            btnCopy = itemView.findViewById(R.id.btn_copy);
            btnShare = itemView.findViewById(R.id.btn_share);
        }
    }
}
