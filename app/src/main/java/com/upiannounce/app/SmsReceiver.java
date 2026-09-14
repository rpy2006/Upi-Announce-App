package com.upiannounce.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;
import android.util.Log;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsReceiver extends BroadcastReceiver {
    private static final Pattern AMOUNT_PATTERN = Pattern.compile(
        "(?:₹|rs\\.?\\s?|inr\\s?|rs\\s)([\\d,]+(?:\\.\\d{1,2})?)", Pattern.CASE_INSENSITIVE);

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!"android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) return;

        PendingResult pendingResult = goAsync();
        
        AnnounceUPIApp.runInBackground(() -> {
            try {
                Object[] pdus = (Object[]) intent.getExtras().get("pdus");
                String format = intent.getStringExtra("format");
                if (pdus == null) return;

                for (Object pdu : pdus) {
                    SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu, format);
                    if (sms == null) continue;

                    String body = sms.getMessageBody();
                    if (body == null) continue;

                    Matcher m = AMOUNT_PATTERN.matcher(body.toLowerCase());
                    if (m.find()) {
                        try {
                            double amount = Double.parseDouble(m.group(1).replace(",", ""));
                            if (amount > 0) {
                                String sender = sms.getOriginatingAddress();
                                String source = sender != null ? sender : "Bank SMS";
                                
                                TransactionRepository.getInstance(context).saveTransaction(amount, source);
                                TTSManager.getInstance().announcePayment(amount, source);
                                
                                Log.d("SmsReceiver", "Detected: ₹" + amount);
                            }
                        } catch (Exception ignored) {}
                    }
                }
            } finally {
                pendingResult.finish();
            }
        });
    }
}