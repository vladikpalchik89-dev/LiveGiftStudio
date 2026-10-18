package com.livegiftstudio;

import android.app.Service;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Movie;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.VideoView;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.widget.MediaController;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class OverlayService extends Service {

    private static final String TAG = "LiveGiftStudio";  
    private static final String CHANNEL_ID = "livegiftstudio_service";
    private static final String ACTION_STOP_CHAT_SPEECH =
        "com.livegiftstudio.STOP_CHAT_SPEECH";
    private static final String ACTION_START_CHAT_SPEECH =
        "com.livegiftstudio.START_CHAT_SPEECH";
    private static final int NOTIFICATION_ID = 1001;
    private static final String SOCKET_URL =
            "wss://livegiftstudio.onrender.com/events";

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private WindowManager windowManager;
    private TextView overlay;
    private View overlayView;
    private SharedPreferences prefs;

    private OkHttpClient client;
    private WebSocket webSocket;
    private MediaPlayer currentPlayer;
    private TextToSpeech textToSpeech;

    private boolean ttsReady = false;
    private volatile boolean chatSpeechEnabled = true;
    private String lastGiftEventKey = "";
private long lastGiftEventTime = 0;
    private Runnable removeTask;
    private boolean destroyed = false;

    @Override
    public void onCreate() {
        super.onCreate();
    startForegroundNotification();
        prefs = getSharedPreferences(
                "LiveGiftStudio", MODE_PRIVATE);

        windowManager = (WindowManager)
                getSystemService(WINDOW_SERVICE);

        textToSpeech = new TextToSpeech(
                this,
                status -> {
                    if (status == TextToSpeech.SUCCESS
                            && textToSpeech != null) {

                        int result = textToSpeech.setLanguage(
                                new Locale("ru", "RU"));

                        ttsReady =
                                result != TextToSpeech.LANG_MISSING_DATA
                                && result != TextToSpeech.LANG_NOT_SUPPORTED;

                        android.util.Log.d(
                                TAG,
                                "TTS ready: " + ttsReady);
                    }
                }
        );

        connectToServer();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
private void startForegroundNotification() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "LiveGiftStudio — работа в фоне",
                NotificationManager.IMPORTANCE_LOW
        );

        channel.setDescription(
                "Подключение к TikTok LIVE и озвучка сообщений"
        );

        NotificationManager manager =
                (NotificationManager) getSystemService(
                        NOTIFICATION_SERVICE
                );

        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    Notification.Builder builder;

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        builder = new Notification.Builder(this, CHANNEL_ID);
    } else {
        builder = new Notification.Builder(this);
    }

    builder.setContentTitle("LiveGiftStudio PRO")
            .setContentText("Озвучка TikTok LIVE работает в фоне")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true);

    startForeground(NOTIFICATION_ID, builder.build());
}

 
@Override
public int onStartCommand(
        Intent intent, int flags, int startId) {

    if (intent != null
            && ACTION_STOP_CHAT_SPEECH.equals(intent.getAction())) {

        chatSpeechEnabled = false;

        if (textToSpeech != null) {
            textToSpeech.stop();
        }

        android.util.Log.d(TAG, "Озвучка чата выключена");
        return START_STICKY;
    }

    if (intent != null
            && ACTION_START_CHAT_SPEECH.equals(intent.getAction())) {

        chatSpeechEnabled = true;

        android.util.Log.d(TAG, "Озвучка чата включена");
        return START_STICKY;
    }

    if (intent != null) {
        
String previewUri =
        intent.getStringExtra("preview_animation_uri");

if (previewUri != null && !previewUri.isEmpty()) {
    handler.post(() -> {
        if (!destroyed) {
            showAnimationPreview(previewUri);
        }
    });

    return START_NOT_STICKY;
}


        String testDonation =
                intent.getStringExtra("test_donation");

        if ("yes".equals(testDonation)) {
            handler.post(() -> {
                if (!destroyed) {
                    showGift(
                            "💰 ДОНАТ 50 RUB\n@ТестовыйЗритель\n💬 Проверка озвучки!",
                            "donation"
                    );

                    speakText(
                            "Тестовый донат от Тестового зрителя. "
                                    + "Сумма 50 рублей. Проверка озвучки!"
                    );
                }
            });
        }

        String text = intent.getStringExtra("overlay_text");

        if (text != null && !text.trim().isEmpty()) {
            handler.post(() -> {
                if (!destroyed) {
                    showGift(text, "rose");
                    playGiftSound("rose");
                }
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

                android.util.Log.d(
                        TAG, "WebSocket connected");
            }

            @Override
            public void onMessage(
                    WebSocket socket, String text) {

                try {
                    JSONObject event = new JSONObject(text);

                    String type = event.optString(
                            "type", "").toLowerCase(Locale.ROOT);
                    if (type.equals("stream_end")) {
    handler.post(() -> {
        if (!destroyed && textToSpeech != null) {
            textToSpeech.stop();
            android.util.Log.d(
                TAG,
                "Эфир завершён: очередь озвучки очищена"
            );
        }
    });
    return;
}
                    // Новый подписчик
                    if (type.equals("follow")
                            || type.equals("followed")
                            || type.equals("new_follower")) {

                        String username = event.optString(
                                "username", "Зритель");

                        handler.post(() -> {
                          if (!destroyed) {
    playFollowSound();
    speakText(
            "Новый подписчик. " + username);
}  
                        });

                        return;
                    }

                    // Сообщение в чате
                    if (type.equals("chat")) {
                        String username = event.optString(
                                "username", "Зритель");

                        String comment = event.optString(
                                "comment", "");

                        if (chatSpeechEnabled && !comment.trim().isEmpty()) {
                            handler.post(() -> {
                                if (!destroyed) {
                                    speakTextForUser(
        null,
        comment
);
                                }
                            });
                        }

                        return;
                    }

                   // ТЕСТОВЫЙ ДОНАТ — без реальной оплаты
if (type.equals("test_donation")) {
    handler.post(() -> {
        if (!destroyed) {
            showGift(
                    "💰 ДОНАТ 50 RUB\n@ТестовыйЗритель\n💬 Проверка озвучки!",
                    "donation"
            );

            speakText(
                    "Тестовый донат от Тестовый зритель. "
                    + "Сумма 50 рублей. Проверка озвучки!"
            );
        }
    });

    return;
} 
                   
                    // Донат из DonationAlerts
                    if (type.equals("donation")) {
                        String donor = event.optString(
                                "username", "Зритель");

                        String amount = event.optString(
                                "amount", "0");

                        String currency = event.optString(
                                "currency", "RUB");

                        String donationText = event.optString(
                                "message", "").trim();

                        String displayMessage =
                                "💰 ДОНАТ " + amount + " "
                                        + currency
                                        + "\n@" + donor;

                        if (!donationText.isEmpty()) {
                            displayMessage +=
                                    "\n💬 " + donationText;
                        }

                        final String finalDisplayMessage =
                                displayMessage;

                        handler.post(() -> {
                            if (!destroyed) {
                                showGift(
                                        finalDisplayMessage,
                                        "donation"
                                );

                                String speech =
                                        "Донат от " + donor
                                        + ". Сумма " + amount
                                        + " " + currency;

                                if (!donationText.isEmpty()) {
                                    speech += ". Сообщение: "
                                            + donationText;
                                }

                                speakText(speech);
                            }
                        });

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
                    
String giftEventKey = username + "|" + gift;
long now = System.currentTimeMillis();

if (giftEventKey.equals(lastGiftEventKey)
        && now - lastGiftEventTime < 2000) {
    android.util.Log.d(
            TAG, "Повтор подарка пропущен: " + giftEventKey);
    return;
}

lastGiftEventKey = giftEventKey;
lastGiftEventTime = now;
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

                            speakText(
                                    "Подарок " + gift
                                            + ". Отправил "
                                            + username);
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
                        TAG,
                        "WebSocket connection failed",
                        error);

                if (!destroyed && socket == webSocket) {
                    handler.postDelayed(() -> {
                        if (!destroyed) {
                            connectToServer();
                        }
                    }, 5000);
                }
            }

            @Override
            public void onClosed(
                    WebSocket socket,
                    int code,
                    String reason) {

                android.util.Log.d(
                        TAG,
                        "WebSocket CLOSED code="
                                + code + " reason=" + reason);

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
private int getGiftImageResource(String gift) {
    String key = getGiftKey(gift);

    int resourceId = getResources().getIdentifier(
            "gift_" + key,
            "drawable",
            getPackageName()
    );

    if (resourceId == 0) {
        resourceId = getResources().getIdentifier(
                "gift_default",
                "drawable",
                getPackageName()
        );
    }

    return resourceId;
}

private int getGiftCoins(String gift) {
    // Цены задаются отдельно и только после проверки.
    // -1 означает, что цена пока не настроена.
    String key = getGiftKey(gift);

    return prefs.getInt("gift_coins_" + key, -1);
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
            return new int[]{
                    Color.rgb(255, 35, 115),
                    Color.rgb(130, 0, 65)
            };
        }
        if (name.contains("heart") || name.contains("сердц")) {
            return new int[]{
                    Color.rgb(255, 70, 190),
                    Color.rgb(100, 0, 180)
            };
        }
        if (name.contains("galaxy") || name.contains("universe")
                || name.contains("галак") || name.contains("вселен")) {
            return new int[]{
                    Color.rgb(40, 30, 160),
                    Color.rgb(160, 40, 255)
            };
        }
        if (name.contains("lion") || name.contains("diamond")
                || name.contains("алмаз") || name.contains("лев")) {
            return new int[]{
                    Color.rgb(255, 190, 30),
                    Color.rgb(190, 55, 0)
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
        String savedUri = prefs.getString(
                "gift_sound_" + key, null);

        if (savedUri != null && !savedUri.trim().isEmpty()) {
            playCustomSound(savedUri, gift);
        } else {
            playBuiltInSound(gift);
        }
    }

    private void playFollowSound() {
    stopCurrentSound();

    String savedUri = prefs.getString(
            "follow_sound_uri", null);

    if (savedUri != null && !savedUri.trim().isEmpty()) {
        playCustomSound(savedUri, "heart");
    } else {
        playBuiltInSound("heart");
    }
}

    private void playCustomSound(
            String savedUri, String gift) {

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

            android.util.Log.e(
                    TAG, "Custom audio error", e);

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
            android.util.Log.e(
                    TAG, "Built-in audio error", e);
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
    if (destroyed || windowManager == null) return;

    if (removeTask != null) {
        handler.removeCallbacks(removeTask);
        removeTask = null;
    }

    removeOverlay();

    String animationKey;

    if ("follower".equals(gift)) {
        animationKey = "animation_follower_uri";
    } else if ("donation".equals(gift)) {
        animationKey = "animation_donation_uri";
    } else {
        animationKey = "animation_gift_" + getGiftKey(gift);
    }

    String savedUri = prefs.getString(animationKey, "");

    FrameLayout root = new FrameLayout(this);
    root.setPadding(16, 16, 16, 16);

    GradientDrawable background = new GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            getColors(gift)
    );
    background.setCornerRadius(32);
    background.setStroke(3, Color.WHITE);
    root.setBackground(background);

    root.setElevation(24);

    if (!savedUri.isEmpty()) {
        try {
            Uri uri = Uri.parse(savedUri);
            String path = uri.toString().toLowerCase();

            if (path.contains(".gif")) {
                GifView gifView = new GifView(this, uri);

                FrameLayout.LayoutParams gifParams =
                        new FrameLayout.LayoutParams(
                                280,
                                280,
                                Gravity.CENTER
                        );

                root.addView(gifView, gifParams);

            } else {
                VideoView videoView = new VideoView(this);
                videoView.setVideoURI(uri);
                videoView.setOnPreparedListener(player -> {
                    player.setLooping(true);
                    player.start();
                });

                FrameLayout.LayoutParams videoParams =
                        new FrameLayout.LayoutParams(
                                280,
                                280,
                                Gravity.CENTER
                        );

                root.addView(videoView, videoParams);
            }
        } catch (Exception e) {
            android.util.Log.e(TAG, "Animation error", e);
        }
    }

    TextView text = new TextView(this);
    text.setTextColor(Color.WHITE);
    text.setTextSize(20);
    text.setGravity(Gravity.CENTER);
    text.setPadding(20, 16, 20, 16);

    int coins = getGiftCoins(gift);
    String displayMessage = message;

    if (coins >= 0) {
        displayMessage += "\n🪙 " + coins + " монет";
    }

    text.setText(displayMessage);

    FrameLayout.LayoutParams textParams =
            new FrameLayout.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL
            );

    root.addView(text, textParams);

    int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            : WindowManager.LayoutParams.TYPE_PHONE;

    WindowManager.LayoutParams params =
            new WindowManager.LayoutParams(
                    320,
                    380,
                    type,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT
            );

    params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
    params.y = 160;

    try {
        overlayView = root;
        overlay = text;

        windowManager.addView(root, params);

        AnimationSet animation = new AnimationSet(false);

        ScaleAnimation scale = new ScaleAnimation(
                0.2f, 1.0f, 0.2f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );

        scale.setDuration(650);
        scale.setInterpolator(new OvershootInterpolator());

        AlphaAnimation alpha = new AlphaAnimation(0.0f, 1.0f);
        alpha.setDuration(450);

        animation.addAnimation(scale);
        animation.addAnimation(alpha);

        root.startAnimation(animation);

        removeTask = () -> {
            removeOverlay();
            removeTask = null;
        };

        handler.postDelayed(removeTask, 6000);

    } catch (Exception e) {
        android.util.Log.e(TAG, "Cannot display overlay", e);
        removeOverlay();
    }
}

private void showAnimationPreview(String uriString) {
    if (destroyed || windowManager == null) return;

    removeOverlay();

    Uri uri = Uri.parse(uriString);

    FrameLayout root = new FrameLayout(this);
    root.setBackgroundColor(Color.argb(220, 10, 10, 30));

    TextView label = new TextView(this);
    label.setText("🎬 Предпросмотр анимации");
    label.setTextColor(Color.WHITE);
    label.setTextSize(18);
    label.setGravity(Gravity.CENTER);

    FrameLayout.LayoutParams labelParams =
            new FrameLayout.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    60,
                    Gravity.BOTTOM
            );

    root.addView(label, labelParams);

    try {
        String mime = getContentResolver().getType(uri);

        if (mime != null && mime.equals("image/gif")) {
            GifView gif = new GifView(this, uri);

            FrameLayout.LayoutParams mediaParams =
                    new FrameLayout.LayoutParams(
                            280, 280, Gravity.CENTER
                    );

            root.addView(gif, mediaParams);
        } else {
            VideoView video = new VideoView(this);
            video.setVideoURI(uri);
            video.setOnPreparedListener(player -> {
                player.setLooping(true);
                player.start();
            });

            FrameLayout.LayoutParams mediaParams =
                    new FrameLayout.LayoutParams(
                            280, 280, Gravity.CENTER
                    );

            root.addView(video, mediaParams);
        }
    } catch (Exception e) {
        android.util.Log.e(TAG, "Preview error", e);
        return;
    }

    int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            : WindowManager.LayoutParams.TYPE_PHONE;

    WindowManager.LayoutParams params =
            new WindowManager.LayoutParams(
                    320, 380, type,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT
            );

    params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
    params.y = 160;

    overlayView = root;
    overlay = label;

    try {
        windowManager.addView(root, params);

        removeTask = () -> {
            removeOverlay();
            removeTask = null;
        };

        handler.postDelayed(removeTask, 10000);
    } catch (Exception e) {
        android.util.Log.e(TAG, "Cannot show preview", e);
        removeOverlay();
    }
}

    private void removeOverlay() {
    if (overlayView != null) {
        try {
            overlayView.clearAnimation();

            if (overlayView instanceof VideoView) {
                ((VideoView) overlayView).stopPlayback();
            }

            if (windowManager != null) {
                windowManager.removeView(overlayView);
            }
        } catch (Exception ignored) {
        }

        overlayView = null;
        overlay = null;
    } else if (overlay != null) {
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

    // 30 вариантов звучания и настройка громкости.
private void speakText(String text) {
    speakTextForUser(null, text);
}

// Для каждого сообщения выбираем случайный стиль.
private void speakTextForUser(
        String username, String text) {

    if (textToSpeech == null || !ttsReady
            || text == null || text.trim().isEmpty()) {
        return;
    }

    float[] pitches = {
        0.65f, 0.70f, 0.75f, 0.80f, 0.85f,
        0.90f, 0.95f, 1.00f, 1.05f, 1.10f,
        1.15f, 1.20f, 1.25f, 1.30f, 1.35f,
        0.70f, 0.80f, 0.90f, 1.00f, 1.10f,
        1.20f, 1.30f, 0.75f, 0.95f, 1.15f,
        1.25f, 0.85f, 1.05f, 1.35f, 1.40f
    };

    float[] rates = {
        0.80f, 0.90f, 1.00f, 1.10f, 1.20f,
        0.85f, 0.95f, 1.05f, 1.15f, 1.25f,
        0.80f, 0.90f, 1.00f, 1.10f, 1.20f,
        1.25f, 1.15f, 1.05f, 0.95f, 0.85f,
        0.80f, 0.90f, 1.00f, 1.10f, 1.20f,
        1.25f, 1.15f, 1.05f, 0.90f, 1.30f
    };

    // Случайный стиль для текущего сообщения.
    int index = (int) (Math.random() * pitches.length);

    textToSpeech.setPitch(pitches[index]);
    textToSpeech.setSpeechRate(rates[index]);

    int volume = Math.max(0, Math.min(
            100, prefs.getInt("voice_volume", 100)));

    android.os.Bundle params = new android.os.Bundle();
    params.putFloat(
            TextToSpeech.Engine.KEY_PARAM_VOLUME,
            volume / 100.0f);

    textToSpeech.speak(
            text,
            TextToSpeech.QUEUE_ADD,
            params,
            "LiveGiftStudio_" + System.currentTimeMillis()
    );
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
   
private static class GifView extends View {
    private final Movie movie;
    private final Paint paint =
            new Paint(Paint.ANTI_ALIAS_FLAG);
    private final long startTime;

    GifView(android.content.Context context, Uri uri)
            throws Exception {
        super(context);

        java.io.InputStream input =
                context.getContentResolver()
                        .openInputStream(uri);

        if (input == null) {
            throw new java.io.IOException(
                    "Не удалось открыть GIF");
        }

        Movie loadedMovie;
        try {
            loadedMovie = Movie.decodeStream(input);
        } finally {
            input.close();
        }

        if (loadedMovie == null
                || loadedMovie.width() <= 0
                || loadedMovie.height() <= 0) {
            throw new java.io.IOException(
                    "Некорректный GIF");
        }

        movie = loadedMovie;
        startTime = SystemClock.uptimeMillis();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int duration = movie.duration();
        if (duration <= 0) duration = 1000;

        int elapsed = (int) (
                (SystemClock.uptimeMillis() - startTime)
                        % duration);

        movie.setTime(elapsed);

        float scale = Math.min(
                getWidth() / (float) movie.width(),
                getHeight() / (float) movie.height());

        float x = (getWidth()
                - movie.width() * scale) / 2f;
        float y = (getHeight()
                - movie.height() * scale) / 2f;

        canvas.save();
        canvas.translate(x, y);
        canvas.scale(scale, scale);
        movie.draw(canvas, 0, 0, paint);
        canvas.restore();

        postInvalidateDelayed(33);
    }
}
}
