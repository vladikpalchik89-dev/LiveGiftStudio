
package com.livegiftstudio;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.speech.tts.TextToSpeech;
import android.net.Uri;
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
import android.widget.TextView;

import org.json.JSONObject;

import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class OverlayService extends Service {

    private static final String TAG = "LiveGiftStudio";
    private static final String SOCKET_URL =
            "wss://livegiftstudio.onrender.com/events";

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private WindowManager windowManager;
    private TextView overlay;
    private SharedPreferences prefs;

    private OkHttpClient client;
    private WebSocket webSocket;
    private MediaPlayer currentPlayer;
    private TextToSpeech textToSpeech;
    private boolean ttsReady = false;
    private Runnable removeTask;
    private boolean destroyed = false;

    @Override
public void onCreate() {
    super.onCreate();

    textToSpeech = new TextToSpeech(
            this,
            status -> {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(
                            new Locale("ru", "RU"));
                    ttsReady = result != TextToSpeech.LANG_MISSING_DATA
                            && result != TextToSpeech.LANG_NOT_SUPPORTED;
                }
            }
    );

    prefs = getSharedPreferences(
            "LiveGiftStudio", MODE_PRIVATE);

    windowManager = (WindowManager)
            getSystemService(WINDOW_SERVICE);

    connectToServer();
}

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(
            Intent intent, int flags, int startId) {

        if (intent != null) {
            String text = intent.getStringExtra("overlay_text");

            if (text != null && !text.trim().isEmpty()) {
                handler.post(() -> {
                    showGift(text, "rose");
                    playGiftSound("rose");
                });
            }
        }

        return START_STICKY;
    }

    private void connectToServer() {
        if (destroyed) {
            return;
        }

        client = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .build();

        Request request = new Request.Builder()
                .url(SOCKET_URL)
                .build();

        webSocket = client.newWebSocket(
                request, new WebSocketListener() {

            @Override
            public void onOpen(
                    WebSocket socket, Response response) {
                android.util.Log.d(TAG, "WebSocket connected");
            }

            @Override
            public void onMessage(
                    WebSocket socket, String text) {

                try {
                    JSONObject event = new JSONObject(text);
                    String type = event.optString("type", "")
                            .toLowerCase(Locale.ROOT);

                    // Новый подписчик
                    if (type.equals("follow")
                            || type.equals("followed")
                            || type.equals("new_follower")) {

                        String username = event.optString(
                                "username", "Зритель");

                        final String message =
                                "➕ Новый подписчик\n@" + username;

                        handler.post(() -> {
    if (!destroyed) {
        showGift(message, "heart");
        playFollowSound();
        speakText("Новый подписчик. " + username);
    }
});

                        return;
                    }
                    
// Сообщение в чате
if (type.equals("chat")) {
    String username = event.optString("username", "Зритель");
    String comment = event.optString("comment", "");

    if (!comment.trim().isEmpty()) {
        handler.post(() -> {
            if (!destroyed) {
                showGift("💬 @" + username + "\n" + comment, "heart");
                speakText(username + " пишет: " + comment);
            }
        });
    }

    return;
}
                    // Подарок
                    if (!type.equals("gift")) {
                        return;
                    }

                    String gift = event.optString(
                            "giftName", "Подарок");

                    String username = event.optString(
                            "username", "Зритель");

                    int count = event.optInt("repeatCount", 1);
                    if (count < 1) {
                        count = 1;
                    }

                    final String message =
                            "🎁 " + gift + " × " + count
                            + "\n@" + username;

                    handler.post(() -> {
    if (!destroyed) {
        showGift(message, gift);
        playGiftSound(gift);
        speakText("Подарок " + gift + ". Отправил " + username);
    }
});

                } catch (Exception e) {
                    android.util.Log.e(
                            TAG, "Invalid event", e);
                }
            }

            @Override
            public void onFailure(
                    WebSocket socket,
                    Throwable error,
                    Response response) {

                android.util.Log.e(
                        TAG, "WebSocket connection failed", error);

                // Повторное подключение с задержкой
                if (!destroyed) {
                    handler.postDelayed(
                            () -> connectToServer(), 5000);
                }
            }

           
@Override
public void onClosed(
        WebSocket socket,
        int code,
        String reason) {

    android.util.Log.d(
            TAG,
            "WebSocket closed: " + code + " " + reason
    );

    
     if (!destroyed && socket == webSocket) {
        handler.postDelayed(() -> {
            if (!destroyed) {
                connectToServer();
            }
        }, 5000);
    }
}
        
    });
}
    private String getGiftKey(String gift) {
        String name = gift.toLowerCase(Locale.ROOT);

        if (name.contains("finger heart")
                || name.contains("finger_heart")
                || name.contains("сердечко пальцами")) {
            return "finger_heart";
        }
        if (name.contains("rose") || name.contains("роза")) {
            return "rose";
        }
        if (name.contains("heart") || name.contains("сердц")) {
            return "heart";
        }
        if (name.contains("tiktok") || name.contains("тик ток")) {
            return "tiktok";
        }
        if (name.contains("coffee") || name.contains("кофе")) {
            return "coffee";
        }
        if (name.contains("ice cream")
                || name.contains("ice_cream")
                || name.contains("морожен")) {
            return "ice_cream";
        }
        if (name.contains("perfume") || name.contains("парфюм")) {
            return "perfume";
        }
        if (name.contains("love you")
                || name.contains("love_you")
                || name.contains("люблю тебя")) {
            return "love_you";
        }
        if (name.contains("firework")
                || name.contains("фейерверк")) {
            return "fireworks";
        }
        if (name.contains("galaxy") || name.contains("галак")) {
            return "galaxy";
        }
        if (name.contains("universe") || name.contains("вселен")) {
            return "universe";
        }
        if (name.contains("lion") || name.equals("лев")) {
            return "lion";
        }
        if (name.contains("diamond")
                || name.contains("алмаз")
                || name.contains("бриллиант")) {
            return "diamond";
        }
        if (name.contains("crown") || name.contains("корона")) {
            return "crown";
        }
        if (name.contains("unicorn") || name.contains("единорог")) {
            return "unicorn";
        }
        if (name.contains("tiger") || name.contains("тигр")) {
            return "tiger";
        }
        if (name.contains("castle") || name.contains("замок")) {
            return "castle";
        }
        if (name.contains("sports car")
                || name.contains("sports_car")) {
            return "sports_car";
        }
        if (name.contains("yacht") || name.contains("яхта")) {
            return "yacht";
        }
        if (name.contains("dragon flame")
                || name.contains("dragon_flame")) {
            return "dragon_flame";
        }
        if (name.contains("phoenix") || name.contains("феникс")) {
            return "phoenix";
        }
        if (name.contains("rocket") || name.contains("ракета")) {
            return "rocket";
        }
        if (name.contains("planet") || name.contains("планета")) {
            return "planet";
        }
        if (name.contains("star") || name.equals("звезда")) {
            return "star";
        }
        if (name.contains("lightning") || name.contains("молния")) {
            return "lightning";
        }
        if (name.contains("football")) return "football";
        if (name.contains("basketball")) return "basketball";
        if (name.contains("cake") || name.equals("торт")) return "cake";
        if (name.contains("birthday")) return "birthday";
        if (name.contains("balloon")) return "balloon";
        if (name.contains("bouquet") || name.contains("букет")) return "bouquet";
        if (name.contains("teddy bear") || name.contains("teddy_bear")) return "teddy_bear";
        if (name.contains("microphone") || name.contains("микрофон")) return "microphone";
        if (name.contains("guitar") || name.contains("гитара")) return "guitar";
        if (name.contains("drum") || name.contains("барабан")) return "drum";
        if (name.contains("motorcycle")) return "motorcycle";
        if (name.contains("train") || name.contains("поезд")) return "train";
        if (name.contains("airplane") || name.contains("самолёт")
                || name.contains("самолет")) return "airplane";
        if (name.contains("house") || name.equals("дом")) return "house";
        if (name.contains("ring") || name.contains("кольцо")) return "ring";
        if (name.contains("watch") || name.contains("часы")) return "watch";
        if (name.contains("money") || name.contains("деньги")) return "money";
        if (name.contains("gold")) return "gold";
        if (name.equals("fire") || name.equals("огонь")) return "fire";
        if (name.contains("rainbow")) return "rainbow";
        if (name.contains("butterfly")) return "butterfly";
        if (name.contains("flower") || name.equals("цветок")) return "flower";
        if (name.equals("cat") || name.equals("кот")) return "cat";
        if (name.equals("dog") || name.equals("собака")) return "dog";
        if (name.contains("rabbit") || name.contains("кролик")) return "rabbit";
        if (name.contains("panda") || name.contains("панда")) return "panda";
        if (name.contains("champagne")) return "champagne";
        if (name.contains("trophy") || name.contains("кубок")) return "trophy";
        if (name.contains("medal") || name.contains("медаль")) return "medal";
        if (name.contains("love") || name.contains("любов")) return "love";
        if (name.contains("music") || name.contains("музык")) return "music";
        if (name.contains("cap") || name.contains("кепка")) return "cap";
        if (name.contains("sunglasses") || name.contains("очки")) return "sunglasses";
        if (name.contains("donut") || name.contains("пончик")) return "donut";
        if (name.contains("swan") || name.contains("лебедь")) return "swan";
        if (name.contains("concert") || name.contains("концерт")) return "concert";
        if (name.contains("paper crane") || name.contains("paper_crane")) return "paper_crane";

        return "default";
    }

    private int[] getColors(String gift) {
        String name = gift.toLowerCase(Locale.ROOT);

        if (name.contains("rose") || name.contains("роза")) {
            return new int[]{Color.rgb(255, 35, 115), Color.rgb(130, 0, 65)};
        }
        if (name.contains("heart") || name.contains("сердц")) {
            return new int[]{Color.rgb(255, 70, 190), Color.rgb(100, 0, 180)};
        }
        if (name.contains("galaxy") || name.contains("universe")
                || name.contains("галак") || name.contains("вселен")) {
            return new int[]{Color.rgb(40, 30, 160), Color.rgb(160, 40, 255)};
        }
        if (name.contains("lion") || name.contains("diamond")
                || name.contains("алмаз") || name.contains("лев")) {
            return new int[]{Color.rgb(255, 190, 30), Color.rgb(190, 55, 0)};
        }
        if (name.contains("firework") || name.contains("фейерверк")) {
            return new int[]{Color.rgb(255, 100, 0), Color.rgb(220, 0, 100)};
        }
        if (name.contains("crown") || name.contains("корона")) {
            return new int[]{Color.rgb(255, 215, 0), Color.rgb(190, 100, 0)};
        }

        return new int[]{Color.rgb(150, 40, 255), Color.rgb(55, 15, 120)};
    }

    private int getSoundResource(String gift) {
        String name = gift.toLowerCase(Locale.ROOT);

        if (name.contains("rose") || name.contains("роза")) {
            return R.raw.gift_rose;
        }
        if (name.contains("heart") || name.contains("сердц")) {
            return R.raw.gift_heart;
        }
        if (name.contains("galaxy") || name.contains("universe")
                || name.contains("галак") || name.contains("вселен")) {
            return R.raw.gift_galaxy;
        }
        if (name.contains("lion") || name.contains("лев")
                || name.contains("тигр") || name.contains("unicorn")) {
            return R.raw.gift_lion;
        }
        if (name.contains("diamond") || name.contains("алмаз")) {
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

        String key = getGiftKey(gift);
        String savedUri = prefs.getString("gift_sound_" + key, null);

        if (savedUri != null && !savedUri.trim().isEmpty()) {
            playCustomSound(savedUri, gift);
        } else {
            playBuiltInSound(gift);
        }
    }

    // Звук нового подписчика из настроек MainActivity
    private void playFollowSound() {
        stopCurrentSound();

        String savedUri = prefs.getString("follow_sound_uri", null);

        if (savedUri != null && !savedUri.trim().isEmpty()) {
            playCustomSound(savedUri, "heart");
        }
    }

    private void playCustomSound(String savedUri, String gift) {
        MediaPlayer player = new MediaPlayer();
        currentPlayer = player;

        player.setOnPreparedListener(mp -> {
            if (currentPlayer == mp && !destroyed) {
                mp.start();
            }
        });

        player.setOnCompletionListener(mp -> {
            if (currentPlayer == mp) {
                currentPlayer = null;
            }
            mp.release();
        });

        player.setOnErrorListener((mp, what, extra) -> {
            if (currentPlayer == mp) {
                currentPlayer = null;
            }
            mp.release();

            if (!destroyed) {
                playBuiltInSound(gift);
            }
            return true;
        });

        try {
            player.setDataSource(this, Uri.parse(savedUri));
            player.prepareAsync();
        } catch (Exception e) {
            if (currentPlayer == player) {
                currentPlayer = null;
            }
            try {
                player.release();
            } catch (Exception ignored) {
            }

            android.util.Log.e(TAG, "Custom audio error", e);

            if (!destroyed) {
                playBuiltInSound(gift);
            }
        }
    }

    private void playBuiltInSound(String gift) {
        try {
            currentPlayer = MediaPlayer.create(
                    this, getSoundResource(gift));

            if (currentPlayer == null) {
                return;
            }

            MediaPlayer player = currentPlayer;

            player.setOnCompletionListener(mp -> {
                if (currentPlayer == mp) {
                    currentPlayer = null;
                }
                mp.release();
            });

            player.setOnErrorListener((mp, what, extra) -> {
                if (currentPlayer == mp) {
                    currentPlayer = null;
                }
                mp.release();
                return true;
            });

            player.start();

        } catch (Exception e) {
            android.util.Log.e(TAG, "Built-in audio error", e);
            stopCurrentSound();
        }
    }

    private void stopCurrentSound() {
        if (currentPlayer == null) {
            return;
        }

        MediaPlayer player = currentPlayer;
        currentPlayer = null;

        try {
            player.setOnPreparedListener(null);
            player.setOnCompletionListener(null);
            player.setOnErrorListener(null);
            player.release();
        } catch (Exception ignored) {
        }
    }

    private void showGift(String message, String gift) {
        if (destroyed || windowManager == null) {
            return;
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
                getColors(gift));

        background.setCornerRadius(32);
        background.setStroke(3, Color.WHITE);
        overlay.setBackground(background);

        int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        WindowManager.LayoutParams params =
                new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        type,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                        PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        params.y = 160;

        try {
            windowManager.addView(overlay, params);

            AnimationSet animation = new AnimationSet(false);

            ScaleAnimation scale = new ScaleAnimation(
                    0.2f, 1.0f, 0.2f, 1.0f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            scale.setDuration(650);
            scale.setInterpolator(new OvershootInterpolator());

            AlphaAnimation alpha = new AlphaAnimation(0.0f, 1.0f);
            alpha.setDuration(450);

            animation.addAnimation(scale);
            animation.addAnimation(alpha);
            overlay.startAnimation(animation);

            removeTask = () -> {
                removeOverlay();
                removeTask = null;
            };

            handler.postDelayed(removeTask, 4000);

        } catch (Exception e) {
            android.util.Log.e(TAG, "Cannot display overlay", e);
            removeOverlay();
        }
    }

    private void removeOverlay() {
        
        if (overlay != null) {
            try {
                overlay.clearAnimation();
                if (windowManager != null) {
                    windowManager.removeView(overlay);
                }
            } catch (Exception ignored) {
            }
            overlay = null;
        }
    }

    private void speakText(String text) {
        if (textToSpeech != null && ttsReady
                && text != null && !text.trim().isEmpty()) {
            textToSpeech.speak(
                    text,
                    TextToSpeech.QUEUE_ADD,
                    null,
                    "LiveGiftStudio_" + System.currentTimeMillis()
            );
        }
    }


    @Override
    public void onDestroy() {
        destroyed = true;   
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
            ttsReady = false;
        }
        if (removeTask != null) {
            handler.removeCallbacks(removeTask);
            removeTask = null;
        }

        removeOverlay();
        stopCurrentSound();

        if (webSocket != null) {
            webSocket.close(1000, "Service stopped");
            webSocket = null;
        }

        if (client != null) {
            client.dispatcher().cancelAll();
            client.dispatcher().executorService().shutdown();
            client.connectionPool().evictAll();
            client = null;
        }

        super.onDestroy();
    }
}
