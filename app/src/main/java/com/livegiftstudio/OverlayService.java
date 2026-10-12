
package com.livegiftstudio;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

public class OverlayService extends Service {

    private WindowManager windowManager;
    private TextView overlay;
    private final Handler handler = new Handler();
    private ToneGenerator toneGenerator;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            toneGenerator = new ToneGenerator(
                    AudioManager.STREAM_MUSIC, 80);
        } catch (Exception ignored) {
            toneGenerator = null;
        }
    }

    @Override
    public int onStartCommand(
            Intent intent, int flags, int startId) {

        String text = "🎁 Новый подарок!";

        if (intent != null) {
            String received = intent.getStringExtra("overlay_text");
            if (received != null && !received.trim().isEmpty()) {
                text = received;
            }
        }

        showMessage(text);
        return START_NOT_STICKY;
    }

    private void showMessage(String text) {
        if (windowManager == null) {
            windowManager = (WindowManager)
                    getSystemService(WINDOW_SERVICE);
        }

        removeOverlay();

        overlay = new TextView(this);
        overlay.setText(text);
        overlay.setTextColor(Color.WHITE);
        overlay.setTextSize(25);
        overlay.setGravity(Gravity.CENTER);
        overlay.setPadding(30, 22, 30, 22);
        overlay.setBackgroundColor(Color.rgb(120, 30, 200));
        overlay.setElevation(20);

        int type;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            type = WindowManager.LayoutParams.TYPE_PHONE;
        }

        WindowManager.LayoutParams params =
                new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        type,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                        PixelFormat.TRANSLUCENT
                );

        params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        params.y = 160;

        try {
            windowManager.addView(overlay, params);

            ScaleAnimation scale = new ScaleAnimation(
                    0.4f, 1.0f,
                    0.4f, 1.0f,
                    ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                    ScaleAnimation.RELATIVE_TO_SELF, 0.5f
            );
            scale.setDuration(450);
            overlay.startAnimation(scale);

            if (toneGenerator != null) {
                toneGenerator.startTone(
                        ToneGenerator.TONE_PROP_ACK, 250);
            }

            handler.postDelayed(this::removeOverlay, 4000);

        } catch (Exception e) {
            removeOverlay();
        }
    }

    private void removeOverlay() {
        handler.removeCallbacksAndMessages(null);

        if (overlay != null && windowManager != null) {
            try {
                windowManager.removeView(overlay);
            } catch (Exception ignored) {
            }
        }

        overlay = null;
    }

    @Override
    public void onDestroy() {
        removeOverlay();

        if (toneGenerator != null) {
            toneGenerator.release();
            toneGenerator = null;
        }

        super.onDestroy();
    }
}
