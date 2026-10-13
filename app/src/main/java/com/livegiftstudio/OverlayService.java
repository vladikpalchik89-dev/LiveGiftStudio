
package com.livegiftstudio;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
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

    private OkHttpClient client;
    private WebSocket webSocket;
    private Runnable removeTask;
    private MediaPlayer currentPlayer;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
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
                android.util.Log.d(
                        "LiveGiftStudio", "Server connected");
            }

            @Override
            public void onMessage(WebSocket socket, String text) {
                try {
                    JSONObject event = new JSONObject(text);

                    if (!"gift".equals(event.optString("type"))) {
                        return;
                    }

                    String gift = event.optString(
                            "giftName", "Подарок");
                    String username = event.optString(
                            "username", "Зритель");
                    int count = event.optInt("repeatCount", 1);

                    String message = "🎁 " + gift + " × " + count
                            + "\n@" + username;

                    handler.post(() -> showGift(message, gift));

                } catch (Exception e) {
                    android.util.Log.e(
                            "LiveGiftStudio", "Event error", e);
                }
            }

            @Override
            public void onFailure(
                    WebSocket socket, Throwable error, Response response) {
                android.util.Log.e(
                        "LiveGiftStudio", "Connection failed", error);
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
                || name.contains("бриллиант")
                || name.contains("драгоц")) {
            return new int[]{
                    Color.rgb(0, 220, 255),
                    Color.rgb(20, 60, 210)
            };
        }

        if (name.contains("firework") || name.contains("фейерверк")) {
            return new int[]{
                    Color.rgb(255, 100, 0),
                    Color.rgb(220, 0, 100)
            };
        }

        if (name.contains("crown") || name.contains("корона")) {
            return new int[]{
                    Color.rgb(255, 215, 0),
                    Color.rgb(190, 100, 0)
            };
        }

        return new int[]{
                Color.rgb(150, 40, 255),
                Color.rgb(55, 15, 120)
        };
    }

    private int getSoundResource(String gift) {
        String name = gift.toLowerCase(Locale.ROOT);

        if (name.contains("rose") || name.contains("роза")) {
            return R.raw.gift_rose;
        }

        if (name.contains("heart") || name.contains("сердц")
                || name.contains("finger")) {
            return R.raw.gift_heart;
        }

        if (name.contains("galaxy") || name.contains("universe")
                || name.contains("вселен") || name.contains("галак")) {
            return R.raw.gift_galaxy;
        }

        if (name.contains("lion") || name.contains("лев")
                || name.contains("тигр") || name.contains("unicorn")) {
            return R.raw.gift_lion;
        }

        if (name.contains("diamond") || name.contains("алмаз")
                || name.contains("бриллиант")
                || name.contains("драгоц")) {
            return R.raw.gift_diamond;
        }

        if (name.contains("firework") || name.contains("фейерверк")) {
            return R.raw.gift_fireworks;
        }

        if (name.contains("crown") || name.contains("корона")) {
            return R.raw.gift_crown;
        }

        return R.raw.gift_default;
    }

    private void playGiftSound(String gift) {
        stopCurrentSound();

        try {
            int soundResource = getSoundResource(gift);
            currentPlayer = MediaPlayer.create(this, soundResource);

            if (currentPlayer == null) {
                android.util.Log.e(
                        "LiveGiftStudio", "Cannot load gift sound");
                return;
            }

            currentPlayer.setOnCompletionListener(player -> {
                if (currentPlayer == player) {
                    currentPlayer = null;
                }
                player.release();
            });

            currentPlayer.setOnErrorListener((player, what, extra) -> {
                if (currentPlayer == player) {
                    currentPlayer = null;
                }
                player.release();
                return true;
            });

            currentPlayer.start();

        } catch (Exception e) {
            android.util.Log.e(
                    "LiveGiftStudio", "Sound error", e);
            stopCurrentSound();
        }
    }

    private void stopCurrentSound() {
        if (currentPlayer != null) {
            MediaPlayer player = currentPlayer;
            currentPlayer = null;

            try {
                if (player.isPlaying()) {
                    player.stop();
                }
            } catch (Exception ignored) {
            }

            try {
                player.release();
            } catch (Exception ignored) {
            }
        }
    }

    private void showGift(String message, String gift) {
        if (windowManager == null) {
            windowManager =
                    (WindowManager) getSystemService(WINDOW_SERVICE);
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
            android.util.Log.e(
                    "LiveGiftStudio", "Overlay error", e);
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
        stopCurrentSound();

        if (webSocket != null) {
            webSocket.close(1000, "App closed");
            webSocket = null;
        }

        if (client != null) {
            client.dispatcher().executorService().shutdown();
            client = null;
        }

        super.onDestroy();
    }
}
