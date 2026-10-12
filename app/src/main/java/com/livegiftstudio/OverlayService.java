
package com.livegiftstudio;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;

public class OverlayService extends Service {

    private WindowManager windowManager;
    private TextView overlay;
    private final Handler handler = new Handler();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        showMessage("🎁 LiveGiftStudio готов!");
    }

    private void showMessage(String text) {
        if (windowManager == null) {
            windowManager = (WindowManager)
                    getSystemService(WINDOW_SERVICE);
        }

        if (overlay != null) {
            try {
                windowManager.removeView(overlay);
            } catch (Exception ignored) {
            }
        }

        overlay = new TextView(this);
        overlay.setText(text);
        overlay.setTextColor(Color.WHITE);
        overlay.setTextSize(22);
        overlay.setGravity(Gravity.CENTER);
        overlay.setPadding(24, 18, 24, 18);
        overlay.setBackgroundColor(Color.rgb(120, 30, 200));

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
                                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                        PixelFormat.TRANSLUCENT
                );

        params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        params.y = 140;

        try {
            windowManager.addView(overlay, params);
            handler.postDelayed(() -> {
                if (overlay != null && windowManager != null) {
                    try {
                        windowManager.removeView(overlay);
                    } catch (Exception ignored) {
                    }
                    overlay = null;
                }
            }, 4000);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (overlay != null && windowManager != null) {
            try {
                windowManager.removeView(overlay);
            } catch (Exception ignored) {
            }
        }
        overlay = null;
        super.onDestroy();
    }
}
