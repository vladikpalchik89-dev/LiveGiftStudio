
package com.livegiftstudio;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
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

    private SharedPreferences prefs;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        prefs = getSharedPreferences("LiveGiftStudio", MODE_PRIVATE);
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

    // Определяем ключ подарка для поиска назначенного звука.
    private String getGiftKey(String gift) {
        String name = gift.toLowerCase(Locale.ROOT);

        if (name.contains("rose") || name.contains("роза")) {
            return "rose";
        }

        if (name.contains("finger heart")
                || name.contains("finger_heart")
                || name.contains("сердечко пальцами")) {
            return "finger_heart";
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

        if (name.contains("ice cream") || name.contains("ice_cream")
                || name.contains("морожен")) {
            return "ice_cream";
        }

        if (name.contains("perfume") || name.contains("парфюм")) {
            return "perfume";
        }

        if (name.contains("love you") || name.contains("love_you")
                || name.contains("люблю тебя")) {
            return "love_you";
        }

        if (name.equals("gg") || name.contains(" gg")) {
            return "gg";
        }

        if (name.contains("donut") || name.contains("пончик")) {
            return "donut";
        }

        if (name.contains("sunglasses")
                || name.contains("солнцезащитные очки")) {
            return "sunglasses";
        }

        if (name.contains("paper crane") || name.contains("paper_crane")
                || name.contains("журавлик")) {
            return "paper_crane";
        }

        if (name.contains("swan") || name.contains("лебедь")) {
            return "swan";
        }

        if (name.contains("concert") || name.contains("концерт")) {
            return "concert";
        }

        if (name.contains("firework") || name.contains("фейерверк")) {
            return "fireworks";
        }

        if (name.contains("galaxy") || name.contains("галак")) {
            return "galaxy";
        }

        if (name.contains("universe") || name.contains("вселен")) {
            return "universe";
        }

        if (name.contains("lion") || name.contains("лев")) {
            return "lion";
        }

        if (name.contains("diamond") || name.contains("алмаз")
                || name.contains("бриллиант")
                || name.contains("драгоц")) {
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

        if (name.contains("sports car") || name.contains("sports_car")
                || name.contains("спортивная машина")) {
            return "sports_car";
        }

        if (name.contains("yacht") || name.contains("яхта")) {
            return "yacht";
        }

        if (name.contains("dragon flame") || name.contains("dragon_flame")
                || name.contains("пламя дракона")) {
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

        if (name.contains("star") || name.contains("звезда")) {
            return "star";
        }

        if (name.contains("lightning") || name.contains("молния")) {
            return "lightning";
        }

        if (name.contains("football") || name.contains("футбольный мяч")) {
            return "football";
        }

        if (name.contains("basketball") || name.contains("баскетбольный мяч")) {
            return "basketball";
        }

        if (name.contains("cake") || name.contains("торт")) {
            return "cake";
        }

        if (name.contains("birthday") || name.contains("праздник")) {
            return "birthday";
        }

        if (name.contains("balloon") || name.contains("воздушный шар")) {
            return "balloon";
        }

        if (name.contains("bouquet") || name.contains("букет")) {
            return "bouquet";
        }

        if (name.contains("teddy bear") || name.contains("teddy_bear")
                || name.contains("плюшевый мишка")) {
            return "teddy_bear";
        }

        if (name.contains("microphone") || name.contains("микрофон")) {
            return "microphone";
        }

        if (name.contains("guitar") || name.contains("гитара")) {
            return "guitar";
        }

        if (name.contains("drum") || name.contains("барабан")) {
            return "drum";
        }

        if (name.contains("motorcycle") || name.contains("мотоцикл")) {
            return "motorcycle";
        }

        if (name.contains("train") || name.contains("поезд")) {
            return "train";
        }

        if (name.contains("airplane") || name.contains("самолёт")
                || name.contains("самолет")) {
            return "airplane";
        }

        if (name.contains("house") || name.contains("дом")) {
            return "house";
        }

        if (name.contains("ring") || name.contains("кольцо")) {
            return "ring";
        }

        if (name.contains("watch") || name.contains("часы")) {
            return "watch";
        }

        if (name.contains("money") || name.contains("деньги")) {
            return "money";
        }

        if (name.contains("gold") || name.contains("золотая монета")) {
            return "gold";
        }

        if (name.equals("fire") || name.contains("огонь")) {
            return "fire";
        }

        if (name.contains("rainbow") || name.contains("радуга")) {
            return "rainbow";
        }

        if (name.contains("butterfly") || name.contains("бабочка")) {
            return "butterfly";
        }

        if (name.contains("flower") || name.contains("цветок")) {
            return "flower";
        }

        if (name.contains("cat") || name.equals("кот")) {
            return "cat";
        }

        if (name.contains("dog") || name.equals("собака")) {
            return "dog";
        }

        if (name.contains("rabbit") || name.contains("кролик")) {
            return "rabbit";
        }

        if (name.contains("panda") || name.contains("панда")) {
            return "panda";
        }

        if (name.contains("champagne") || name.contains("бокал")) {
            return "champagne";
        }

        if (name.contains("trophy") || name.contains("кубок")) {
            return "trophy";
        }

        if (name.contains("medal") || name.contains("медаль")) {
            return "medal";
        }

        if (name.contains("love") || name.contains("любов")) {
            return "love";
        }

        if (name.contains("music") || name.contains("музык")) {
            return "music";
        }

        if (name.contains("cap") || name.contains("кепка")) {
            return "cap";
        }

        return "default";
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
            String key = getGiftKey(gift);
            String savedUri = prefs.getString("gift_sound_" + key, null);

            if (savedUri != null && !savedUri.trim().isEmpty()) {
                playCustomSound(savedUri);
            } else {
                playBuiltInSound(gift);
            }

        } catch (Exception e) {
            android.util.Log.e(
                    "LiveGiftStudio", "Sound error", e);
            stopCurrentSound();
        }
    }

    private void playCustomSound(String savedUri) {
        MediaPlayer player = new MediaPlayer();
        currentPlayer = player;

        player.setOnPreparedListener(mediaPlayer -> {
            if (currentPlayer == mediaPlayer) {
                mediaPlayer.start();
            }
        });

        player.setOnCompletionListener(mediaPlayer -> {
            if (currentPlayer == mediaPlayer) {
                currentPlayer = null;
            }
            mediaPlayer.release();
        });

        player.setOnErrorListener((mediaPlayer, what, extra) -> {
            android.util.Log.e(
                    "LiveGiftStudio", "Custom audio playback error");

            if (currentPlayer == mediaPlayer) {
                currentPlayer = null;
            }

            mediaPlayer.release();
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

            android.util.Log.e(
                    "LiveGiftStudio", "Cannot open custom audio", e);

            // Если пользовательский файл недоступен,
            // проигрываем стандартный звук.
            playBuiltInSound("unknown");
        }
    }

    private void playBuiltInSound(String gift) {
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
                    "LiveGiftStudio", "Built-in sound error", e);
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
                    Animation.RELATIVE_TO_SELF
