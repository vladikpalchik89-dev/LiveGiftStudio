
package com.livegiftstudio;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class OverlayService extends Service {

    private WindowManager windowManager;
    private TextView overlay;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private ToneGenerator toneGenerator;
    private OkHttpClient client;
    private WebSocket webSocket;
    private Runnable removeTask;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 85);
        } catch (Exception e) {
            toneGenerator = null;
        }

        connectToServer();
    }

    private void connectToServer() {
        client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("wss://livegiftstudio.onrender.com/events")
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {

            @Override
            public void onOpen(WebSocket socket, Response response) {
                android.util.Log.d("LiveGiftStudio", "Server connected");
            }

            @Override
            public void onMessage(WebSocket socket, String text) {
                try {
                    JSONObject event = new JSONObject(text);

                    if (!"gift".equals(event.optString("type"))) {
                        return;
                    }

                    String gift = event.optString("giftName", "Подарок");
                    String username = event.optString("username", "Зритель");
                    int count = event.optInt("repeatCount", 1);

                    String message = "🎁 " + gift + " × " + count
                            + "\n@" + username;

                    handler.post(() -> showGift(message, gift));

                } catch (Exception e) {
                    android.util.Log.e("LiveGiftStudio", "Event error", e);
                }
            }

            @Override
            public void onFailure(
                    WebSocket socket, Throwable error, Response response) {
                android.util.Log.e("LiveGiftStudio", "Connection failed", error);
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String text = intent.getStringExtra("overlay_text");

            if (text != null && !text.trim().isEmpty()) {
                showGift(text, "Тест");
            }
        }

        return START_STICKY;
    }

    private int[] getColors(String gift) {
        String name = gift.toLowerCase(Locale.ROOT);

        if (name.contains("rose") || name.contains("роза")) {
            return new int[]{
                    Color.rgb(255, 35, 115),
                    Color.rgb(130, 0, 65)
            };
        }

        if (name.contains("heart") || name.contains("сердц")
                || name.contains("finger")) {
            return new int[]{
                    Color.rgb(255, 70, 190),
                    Color.rgb(100, 0, 180)
            };
        }

        if (name.contains("galaxy") || name.contains("universe")
                || name.contains("вселен") || name.contains("галак")) {
            return new int[]{
                    Color.rgb(40, 30, 160),
                    Color.rgb(160, 40, 255)
            };
        }

        if (name.contains("lion") || name.contains("unicorn")
                || name.contains("лев") || name.contains("тигр")) {
            return new int[]{
                    Color.rgb(255, 190, 30),
                    Color.rgb(190, 55, 0)
            };
        }

        if (name.contains("diamond") || name.contains("алмаз")
                || name.contains("драгоц")) {
            return new int[]{
                    Color.rgb(0, 220, 255),
                    Color.rgb(20, 60, 210)
            };
        }

        return new int[]{
                Color.rgb(150, 40, 255),
                Color.rgb(55, 15, 120)
        };
    }

    private void playGiftSound(String gift) {
        if (toneGenerator == null) {
            return;
        }

        String name = gift.toLowerCase(Locale.ROOT);

        try {
            if (name.contains("rose") || name.contains("роза")) {
                toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 220);
            } else if (name.contains("heart") || name.contains("сердц")
                    || name.contains("finger")) {
                toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 180);
            } else if (name.contains("galaxy") || name.contains("universe")
                    || name.contains("галак") || name.contains("вселен")) {
                toneGenerator.startTone(
                        ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 450);
            } else if (name.contains("lion") || name.contains("unicorn")
                    || name.contains("лев") || name.contains("тигр")) {
                toneGenerator.startTone(
                        ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, 350);
            } else if (name.contains("diamond") || name.contains("алмаз")) {
                toneGenerator.startTone(ToneGenerator.TONE_PROP_PROMPT, 300);
            } else {
                toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP2, 200);
            }
        } catch (Exception e) {
            android.util.Log.e("LiveGiftStudio", "Sound error", e);
        }
    }

    private void showGift(String message, String gift) {
        if (windowManager == null) {
            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        }

        if (removeTask != null) {
            handler.removeCallbacks(removeTask);
            removeTask = null;
        }

        removeOverlay();

        overlay = new TextView(this);
        overlay.setText(message);
        overlay.setTextColor(Color.WHITE);
        overlay.setTextSize(25);
        overlay.setGravity(Gravity.CENTER);
        overlay.setPadding(35, 26, 35, 26);
        overlay.setMaxWidth(950);
        overlay.setElevation(24);

        GradientDrawable background = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                getColors(gift)
        );
        background.setCornerRadius(32);
        background.setStroke(3, Color.WHITE);
        overlay.setBackground(background);

        int type;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            type = WindowManager.LayoutParams.TYPE_PHONE;
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
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

            AnimationSet animation = new AnimationSet(false);

            ScaleAnimation scale = new ScaleAnimation(
                    0.25f, 1.08f, 0.25f, 1.08f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
            );
            scale.setDuration(350);
            scale.setInterpolator(new OvershootInterpolator());

            ScaleAnimation settle = new ScaleAnimation(
                    1.08f, 1.0f, 1.08f, 1.0f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
            );
            settle.setStartOffset(350);
            settle.setDuration(180);

            AlphaAnimation fade = new AlphaAnimation(0.0f, 1.0f);
            fade.setDuration(250);

            TranslateAnimation slide = new TranslateAnimation(
                    0, 0, -100, 0
            );
            slide.setDuration(400);

            animation.addAnimation(scale);
            animation.addAnimation(settle);
            animation.addAnimation(fade);
            animation.addAnimation(slide);

            overlay.startAnimation(animation);
            playGiftSound(gift);

            removeTask = () -> {
                removeOverlay();
                removeTask = null;
            };
            handler.postDelayed(removeTask, 4000);

        } catch (Exception e) {
            android.util.Log.e("LiveGiftStudio", "Overlay error", e);
            removeOverlay();
        }
    }

    private void removeOverlay() {
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
        if (removeTask != null) {
            handler.removeCallbacks(removeTask);
            removeTask = null;
        }

        removeOverlay();

        if (webSocket != null) {
            webSocket.close(1000, "App closed");
            webSocket = null;
        }

        if (client != null) {
            client.dispatcher().executorService().shutdown();
            client = null;
        }

        if (toneGenerator != null) {
            toneGenerator.release();
            toneGenerator = null;
        }

        super.onDestroy();
    }
}
            
