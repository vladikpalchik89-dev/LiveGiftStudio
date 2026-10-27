package com.livegiftstudio;

import android.app.Service;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
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
import android.speech.tts.UtteranceProgressListener;
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
import android.widget.Toast;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class OverlayService extends Service {

private void startForegroundNotification() {
    String channelId = "livegiftstudio_service";

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        NotificationChannel channel =
                new NotificationChannel(
                        channelId,
                        "LiveGiftStudio в фоне",
                        NotificationManager.IMPORTANCE_LOW
                );

        channel.setDescription(
                "Озвучка комментариев TikTok LIVE"
        );

        NotificationManager manager =
                getSystemService(NotificationManager.class);

        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    Intent openIntent = new Intent(this, MainActivity.class);
    openIntent.setFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP);

    PendingIntent contentIntent = PendingIntent.getActivity(
            this, 0, openIntent, PendingIntent.FLAG_IMMUTABLE);

    // Без этой кнопки сервис нельзя было остановить иначе как
    // убив приложение целиком.
    Intent stopIntent = new Intent(this, OverlayService.class)
            .setAction(ACTION_STOP);

    PendingIntent stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent, PendingIntent.FLAG_IMMUTABLE);

    Notification.Builder builder =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                    ? new Notification.Builder(this, channelId)
                    : new Notification.Builder(this);

    Notification notification = builder
            .setContentTitle("LiveGiftStudio PRO")
            .setContentText(
                    "Озвучка TikTok LIVE работает в фоне"
            )
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .setContentIntent(contentIntent)
            .addAction(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    "Остановить",
                    stopPendingIntent)
            .build();

    startForeground(NOTIFICATION_ID, notification);
}

    private static final String TAG = "LiveGiftStudio";  
    private static final String CHANNEL_ID = "livegiftstudio_service";
    public static final String ACTION_STOP_CHAT_SPEECH =
        "com.livegiftstudio.STOP_CHAT_SPEECH";
    public static final String ACTION_START_CHAT_SPEECH =
        "com.livegiftstudio.START_CHAT_SPEECH";
    public static final String ACTION_TEST_VOICE =
        "com.livegiftstudio.TEST_VOICE";
    private static final String ACTION_STOP =
        "com.livegiftstudio.STOP_SERVICE";
    public static final String ACTION_PREVIEW_TEXT =
        "com.livegiftstudio.PREVIEW_TEXT";
    private static final int NOTIFICATION_ID = 1001;
    private static final String SOCKET_URL =
            "wss://livegiftstudio.onrender.com/events";

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private WindowManager windowManager;

    private static volatile OverlayService instance = null;

    // Виджеты эфира: цель сбора, лайки, топ подарков.
    private LinearLayout widgetsRoot;
    private LinearLayout widgetGoalPanel;
    private TextView widgetGoalTitle;
    private TextView widgetGoalValue;
    private ProgressBar widgetGoalBar;
    private LinearLayout widgetLikesPanel;
    private TextView widgetLikesValue;
    private TextView widgetLikesTop;
    private LinearLayout widgetTopPanel;
    private TextView widgetTopTitle;
    private TextView widgetTopList;
    private TextView overlay;
    private View overlayView;
    private SharedPreferences prefs;

    private OkHttpClient client;
    private WebSocket webSocket;
    private MediaPlayer currentPlayer;
    private TextToSpeech textToSpeech;

    private boolean ttsReady = false;
    // Сколько фраз стоит в очереди синтезатора. Без ограничения
    // активный чат уводил озвучку на минуты вперёд.
    private final AtomicInteger pendingSpeech = new AtomicInteger();
    private volatile boolean chatSpeechEnabled = true;

    private final java.util.Set<String> seenDonationIds =
            new java.util.HashSet<>();
    private final java.util.concurrent.ExecutorService donationExecutor =
            java.util.concurrent.Executors.newSingleThreadExecutor();
    private Runnable donationPollTask;
    private boolean donationPollStarted = false;
    private String donationPollToken = "";
    private volatile String lastDonationPollError = "";
    private String lastGiftEventKey = "";
private long lastGiftEventTime = 0;
    private Runnable removeTask;
    private Runnable reconnectTask;
    private int reconnectAttempts = 0;
    private volatile boolean destroyed = false;

    // Состояние для экрана диагностики: видно, живой ли сокет
    // и когда пришло последнее событие.
    private static volatile WebSocket activeSocket = null;

    public static volatile long switchResultAt = 0;
    public static volatile boolean switchResultOk = false;
    public static volatile String switchResultName = "";
    public static volatile String switchResultText = "";

    public static volatile boolean wsConnected = false;
    public static volatile long lastEventAt = 0;
    public static volatile String lastEventType = "";
    public static volatile String lastSocketError = "";
    public static volatile long serviceStartedAt = 0;


@Override
public void onCreate() {
    super.onCreate();

    serviceStartedAt = System.currentTimeMillis();

    startForegroundNotification();

    android.util.Log.e(
            TAG,
            "OVERLAY SERVICE CREATED"
    );

    prefs = getSharedPreferences(
            "LiveGiftStudio", MODE_PRIVATE);

    instance = this;

    handler.post(this::updateWidgets);

    chatSpeechEnabled = prefs.getBoolean(
            "chat_speech_enabled", true);

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

                        applySavedVoice();
                        applyVoiceTuning();

                        android.util.Log.d(
                                TAG,
                                "TTS ready: " + ttsReady);
                    }
                }
        );

        textToSpeech.setOnUtteranceProgressListener(
                new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
            }

            @Override
            public void onDone(String utteranceId) {
                speechFinished();
            }

            @Override
            public void onError(String utteranceId) {
                speechFinished();
            }
        });

        connectToServer();
        startDonationPolling();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
     
@Override
public int onStartCommand(
        Intent intent, int flags, int startId) {

    if (intent != null
            && ACTION_PREVIEW_TEXT.equals(intent.getAction())) {

        String text = intent.getStringExtra("text");

        if (text == null || text.trim().isEmpty()) {
            text = "🎁 @зритель подарил Розу × 3";
        }

        final String previewText = text;

        handler.post(() -> {
            if (!destroyed) {
                showGift(previewText, "rose");
            }
        });

        return START_STICKY;
    }

    if (intent != null
            && ACTION_STOP.equals(intent.getAction())) {

        android.util.Log.d(
                TAG, "Остановка сервиса из уведомления");

        stopSelf();
        return START_NOT_STICKY;
    }

    if (intent != null
            && ACTION_TEST_VOICE.equals(intent.getAction())) {

        String phrase = intent.getStringExtra("text");

        if (phrase == null || phrase.trim().isEmpty()) {
            phrase = "Привет! Так будет звучать озвучка чата.";
        }

        final String testPhrase = phrase;

        handler.post(() -> {
            if (!destroyed) {
                speakText(testPhrase);
            }
        });

        return START_STICKY;
    }

    if (intent != null
            && ACTION_STOP_CHAT_SPEECH.equals(intent.getAction())) {

        chatSpeechEnabled = false;
        prefs.edit()
                .putBoolean("chat_speech_enabled", false)
                .apply();

        stopSpeech();

        android.util.Log.d(TAG, "Озвучка чата выключена");
        return START_STICKY;
    }

    if (intent != null
            && ACTION_START_CHAT_SPEECH.equals(intent.getAction())) {

        chatSpeechEnabled = true;
        prefs.edit()
                .putBoolean("chat_speech_enabled", true)
                .apply();

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

    return START_STICKY;
}
String testFollow =
        intent.getStringExtra("test_follow");

if ("yes".equals(testFollow)) {
    String testFollowSound =
        intent.getStringExtra("test_follow_sound");

if ("yes".equals(testFollowSound)) {
    handler.post(() -> {
        if (!destroyed) {
            playFollowSound();
        }
    });

    return START_STICKY;
}
    handler.post(() -> {
        if (!destroyed) {
            showGift(
                    "💖 ТЕСТ ПОДПИСКИ\n@ТестовыйЗритель",
                    "follower"
            );

            playFollowSound();

            speakText(
                    "Новый подписчик. Тестовый зритель"
            );
        }
    });

    return START_STICKY;
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
                    
                  playDonationSound();
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
   
    // Переключение аккаунта по уже открытому сокету.
    // Возвращает false, если сокет не готов.
    public static boolean sendAccountSwitch(String username, String key) {
        WebSocket socket = activeSocket;

        if (socket == null || !wsConnected) return false;

        try {
            JSONObject message = new JSONObject();
            message.put("type", "switch_account");
            message.put("username", username);
            message.put("key", key);

            return socket.send(message.toString());
        } catch (Exception error) {
            return false;
        }
    }

    // Просьба подключиться к TikTok заново по открытому сокету.
    public static boolean sendReconnectRequest(String key) {
        WebSocket socket = activeSocket;

        if (socket == null || !wsConnected) return false;

        try {
            JSONObject message = new JSONObject();
            message.put("type", "reconnect");
            message.put("key", key);

            return socket.send(message.toString());
        } catch (Exception error) {
            return false;
        }
    }

    private void connectToServer() {
        if (destroyed) {
            return;
        }

        // Один клиент на всё время жизни сервиса: раньше на каждый
        // реконнект создавался новый пул потоков, а старый не закрывался.
        if (client == null) {
            client = new OkHttpClient.Builder()
                    .retryOnConnectionFailure(true)
                    .pingInterval(20, TimeUnit.SECONDS)
                    .build();
        }

        if (webSocket != null) {
            webSocket.cancel();
            webSocket = null;
        }

        Request request = new Request.Builder()
                .url(SOCKET_URL)
                .build();

        webSocket = client.newWebSocket(
                request, new WebSocketListener() {

            
@Override
public void onOpen(
        WebSocket socket,
        Response response) {

    android.util.Log.d(
            TAG,
            "WebSocket connected"
    );

    reconnectAttempts = 0;
    wsConnected = true;
    lastSocketError = "";
    activeSocket = socket;

    android.util.Log.e(
            TAG,
            "WEBSOCKET OPEN: соединение установлено"
    );
}

            

            @Override
            public void onMessage(
                    WebSocket socket, String text) {
                
android.util.Log.d(
        TAG,
        "WEBSOCKET EVENT: " + text
);

                try {
                    JSONObject event = new JSONObject(text);

                    String type = event.optString(
                            "type", "").toLowerCase(Locale.ROOT);

                    lastEventAt = System.currentTimeMillis();
                    lastEventType = type;

                    // Ответ на переключение аккаунта по сокету.
                    if (type.equals("switch_result")) {
                        final boolean ok =
                                event.optBoolean("ok", false);
                        final String name =
                                event.optString("username", "");
                        final String resultText =
                                event.optString("message", "");

                        switchResultOk = ok;
                        switchResultName = name;
                        switchResultText = resultText;
                        switchResultAt = System.currentTimeMillis();

                        handler.post(() -> {
                            if (ok && !name.isEmpty()) {
                                prefs.edit()
                                        .putString(
                                                "active_tiktok_account",
                                                name)
                                        .putString(
                                                "tiktok_username",
                                                name)
                                        .apply();

                                Toast.makeText(
                                        OverlayService.this,
                                        "Сервер переключён на @"
                                                + name,
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(
                                        OverlayService.this,
                                        "Не удалось переключить: "
                                                + resultText,
                                        Toast.LENGTH_LONG).show();
                            }
                        });

                        return;
                    }

                    if (type.equals("stream_end")) {
    handler.post(() -> {
        if (!destroyed && textToSpeech != null) {
            stopSpeech();
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
                                try {
    android.util.Log.d(TAG, "FOLLOW: перед showGift");

    showGift(
            "💖 НОВЫЙ ПОДПИСЧИК\n@" + username,
            "follower"
    );

    android.util.Log.d(TAG, "FOLLOW: после showGift");

} catch (Exception e) {
    android.util.Log.e(TAG, "FOLLOW showGift error", e);
}

try {
    android.util.Log.d(TAG, "FOLLOW: перед playFollowSound");

    playFollowSound();

    android.util.Log.d(TAG, "FOLLOW: после playFollowSound");

} catch (Exception e) {
    android.util.Log.e(TAG, "FOLLOW sound error", e);
}

                                speakText(
                                        "Новый подписчик. " + username
                                );
                            }
                        });

                        return;
                    }

                  
                    // Сообщение в чате
                    if (type.equals("chat")
                            || type.equals("comment")) {

                        String username = event.optString(
                                "username",
                                event.optString("nickname", "Зритель"));

                        String comment = event.optString("comment", "");

                        if (comment.trim().isEmpty()) {
                            comment = event.optString("message", "");
                        }

                        if (comment.trim().isEmpty()) {
                            comment = event.optString("text", "");
                        }

                        android.util.Log.d(
                                TAG,
                                "CHAT: user=" + username
                                        + ", comment=" + comment
                                        + ", enabled=" + chatSpeechEnabled
                                        + ", ttsReady=" + ttsReady
                        );
                        

android.util.Log.e(
        TAG,
        "CHAT DEBUG: enabled=" + chatSpeechEnabled
                + ", ttsReady=" + ttsReady
                + ", destroyed=" + destroyed
                + ", comment=" + comment
);

                        final String finalComment = comment.trim();
                        final String finalUsername = username;

                        if (chatSpeechEnabled
                                && !finalComment.isEmpty()) {

                            handler.post(() -> {
                                if (!destroyed) {
                                    speakTextForUser(
                                            finalUsername,
                                            finalComment
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
                        handleDonation(
                                event.optString("username", "Зритель"),
                                event.optString("amount", "0"),
                                event.optString("currency", "RUB"),
                                event.optString("message", "").trim()
                        );

                        return;
                    } 
                    // Лайки: считаем общее число и по зрителям.
                    if (type.equals("like")) {
                        addLikeStats(
                                event.optString("username", "Зритель"),
                                Math.max(1, event.optInt("likeCount", 1)));

                        handler.post(
                                OverlayService.this::updateWidgets);

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

                    // Дубль от коннектора — тот же подарок с тем же
                    // количеством. Продолжение комбо (другой repeatCount)
                    // пропускать нельзя.
                    String giftEventKey =
                            username + "|" + gift + "|" + count;
                    long now = System.currentTimeMillis();

                    if (giftEventKey.equals(lastGiftEventKey)
                            && now - lastGiftEventTime < 2000) {
                        android.util.Log.d(
                                TAG,
                                "Повтор подарка пропущен: "
                                        + giftEventKey);
                        return;
                    }

                    lastGiftEventKey = giftEventKey;
                    lastGiftEventTime = now;

                    // Комбо приходит несколькими событиями: в топ
                    // пишем только его завершение, иначе сумма
                    // задваивается.
                    if (event.optBoolean("repeatEnd", true)) {
                        addGiftStats(username, gift, count);
                    }

                    final String message =
                            "🎁 " + gift + " × " + count
                                    + "\n@" + username;

                    handler.post(() -> {
                        if (!destroyed) {
                            showGift(message, gift);
                            playGiftSound(gift);
                            updateWidgets();

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

    int httpCode = -1;

    if (response != null) {
        httpCode = response.code();
    }

    android.util.Log.e(
            TAG,
            "WebSocket FAILED. HTTP="
                    + httpCode
                    + ", error="
                    + String.valueOf(error),
            error
    );

    wsConnected = false;
    activeSocket = null;
    lastSocketError = "HTTP=" + httpCode + " "
            + String.valueOf(error);

    if (socket == webSocket) {
        scheduleReconnect();
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

                wsConnected = false;
                activeSocket = null;
                lastSocketError = "closed " + code + " " + reason;

                if (socket == webSocket) {
                    scheduleReconnect();
                }
            }
        });
    }
    /**
     * Один общий реконнект с растущей паузой. Раньше onFailure и onClosed
     * могли запланировать два переподключения сразу и поднять два сокета.
     */
    private void scheduleReconnect() {
        if (destroyed || reconnectTask != null) {
            return;
        }

        int[] delays = {5, 10, 20, 30};
        int index = Math.min(reconnectAttempts, delays.length - 1);
        long delayMs = delays[index] * 1000L;

        reconnectAttempts++;

        android.util.Log.d(
                TAG,
                "WebSocket reconnect in " + (delayMs / 1000) + "s"
        );

        reconnectTask = () -> {
            reconnectTask = null;
            connectToServer();
        };

        handler.postDelayed(reconnectTask, delayMs);
    }

    private android.graphics.Bitmap decodeSampledBitmap(
            Uri uri, int reqWidth, int reqHeight) {
        try {
            android.graphics.BitmapFactory.Options options =
                    new android.graphics.BitmapFactory.Options();

            options.inJustDecodeBounds = true;

            try (java.io.InputStream input =
                         getContentResolver().openInputStream(uri)) {
                if (input == null) return null;
                android.graphics.BitmapFactory.decodeStream(
                        input, null, options);
            }

            options.inSampleSize = calculateInSampleSize(
                    options.outWidth, options.outHeight,
                    reqWidth, reqHeight);
            options.inJustDecodeBounds = false;

            try (java.io.InputStream input =
                         getContentResolver().openInputStream(uri)) {
                if (input == null) return null;
                return android.graphics.BitmapFactory.decodeStream(
                        input, null, options);
            }
        } catch (Exception e) {
            android.util.Log.e(TAG, "Не удалось прочитать картинку", e);
            return null;
        }
    }

    private int calculateInSampleSize(
            int width, int height, int reqWidth, int reqHeight) {
        int sampleSize = 1;

        if (width <= 0 || height <= 0) {
            return sampleSize;
        }

        while (width / (sampleSize * 2) >= reqWidth
                && height / (sampleSize * 2) >= reqHeight) {
            sampleSize *= 2;
        }

        return sampleSize;
    }

    private int dp(float value) {
        return Math.round(value
                * getResources().getDisplayMetrics().density);
    }

    private android.graphics.Typeface getOverlayTypeface() {
        String spec = prefs.getString(
                "overlay_font", "sans-serif|1");

        String family = spec;
        int style = android.graphics.Typeface.BOLD;

        int separator = spec.indexOf('|');

        if (separator > 0) {
            family = spec.substring(0, separator);

            try {
                style = Integer.parseInt(
                        spec.substring(separator + 1));
            } catch (Exception ignored) {
            }
        }

        try {
            return android.graphics.Typeface.create(family, style);
        } catch (Exception e) {
            return android.graphics.Typeface.DEFAULT_BOLD;
        }
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
        // «follower» и «donation» — служебные события, а не подарки.
        // Иначе они могут совпасть с подарком из папки иконок
        // (например, «Follower») и показать его цену.
        if ("follower".equals(gift) || "donation".equals(gift)) {
            return gift;
        }

        String name = gift.toLowerCase(Locale.ROOT);

        // Подарки, добавленные вручную из папки с иконками, ищем первыми:
        // у них русские названия, а общий ключ их не понимает.
        String customKey = findCustomGiftKey(gift);

        if (customKey != null) {
            return customKey;
        }

                if (name.contains("rose_1")) return "rose_1";
        if (name.contains("ice_cream_cone")) return "ice_cream_cone";
        if (name.contains("you_are_amazing")) return "you_are_amazing";
        if (name.contains("bravo")) return "bravo";
        if (name.contains("confetti")) return "confetti";
        if (name.contains("ticket")) return "ticket";
        if (name.contains("corgi")) return "corgi";
        if (name.contains("squirrel")) return "squirrel";
        if (name.contains("pomegranate")) return "pomegranate";
        if (name.contains("hat_moustache")) return "hat_moustache";
        if (name.contains("from_the_heart")) return "from_the_heart";
        if (name.contains("heart_from_hands")) return "heart_from_hands";
        if (name.contains("friendship_pendant")) return "friendship_pendant";
        if (name.contains("love_more")) return "love_more";
        if (name.contains("flying_airplanes")) return "flying_airplanes";
        if (name.contains("intense_reaction")) return "intense_reaction";
        if (name.contains("slowmo")) return "slowmo";

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

        // Любой подарок, которого нет в списке выше, получает свой ключ:
        // у него будет отдельный звук и отдельная анимация.
        String generic = name.trim()
                .replaceAll("[^a-z0-9_ ]", "")
                .replaceAll("\\s+", "_");

        if (!generic.isEmpty()) {
            return generic;
        }

        return "default";
    }

    private String findCustomGiftKey(String gift) {
        try {
            JSONArray custom = new JSONArray(
                    prefs.getString("custom_gifts", "[]"));

            String wanted = gift.toLowerCase(Locale.ROOT)
                    .replaceAll("[^\\p{L}\\p{N}]+", " ")
                    .replace('ё', 'е')
                    .trim();

            if (wanted.isEmpty()) return null;

            for (int i = 0; i < custom.length(); i++) {
                JSONObject item = custom.optJSONObject(i);

                if (item == null) continue;

                String key = item.optString("key", "");
                String label = item.optString("name", "")
                        .toLowerCase(Locale.ROOT)
                        .replaceAll("[^\\p{L}\\p{N}]+", " ")
                        .replace('ё', 'е')
                        .trim();

                if (key.isEmpty() || label.isEmpty()) continue;

                if (wanted.equals(label) || wanted.contains(label)) {
                    return key;
                }
            }
        } catch (Exception ignored) {
        }

        return null;
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
        playAssignedSound("gift_sound_" + key, gift);
    }

    /**
     * Значение настройки звука:
     * null             — стандартный звук подарка;
     * "none"           — без звука;
     * "raw:sfx_coin"   — встроенный звук из набора;
     * остальное        — URI файла с телефона.
     */
    private void playAssignedSound(
            String prefKey, String fallbackGift) {

        String spec = prefs.getString(prefKey, null);

        if (spec == null || spec.trim().isEmpty()) {
            playBuiltInSound(fallbackGift);
            return;
        }

        if ("none".equals(spec)) {
            return;
        }

        if (spec.startsWith("raw:")) {
            playRawSound(spec.substring(4), fallbackGift);
            return;
        }

        playCustomSound(spec, fallbackGift);
    }

    private void playRawSound(String name, String fallbackGift) {
        int resourceId = getResources().getIdentifier(
                name, "raw", getPackageName());

        if (resourceId == 0) {
            playBuiltInSound(fallbackGift);
            return;
        }

        try {
            MediaPlayer player = MediaPlayer.create(this, resourceId);

            if (player == null) {
                playBuiltInSound(fallbackGift);
                return;
            }

            currentPlayer = player;

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
            android.util.Log.e(TAG, "Raw sound error", e);
            playBuiltInSound(fallbackGift);
        }
    }

    private void playFollowSound() {
        stopCurrentSound();

        android.util.Log.d(
                TAG,
                "FOLLOW SOUND: "
                        + prefs.getString("follow_sound_uri", null));

        playAssignedSound("follow_sound_uri", "heart");
    }

private void playDonationSound() {
    stopCurrentSound();

    android.util.Log.d(
            TAG,
            "DONATION SOUND: "
                    + prefs.getString(
                            "donationalerts_sound_uri", null));

    playAssignedSound("donationalerts_sound_uri", "heart");
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
   
LinearLayout root = new LinearLayout(this);
root.setOrientation(LinearLayout.VERTICAL);
root.setGravity(Gravity.CENTER_HORIZONTAL);
root.setPadding(dp(6), dp(6), dp(6), dp(6));
root.setBackgroundColor(Color.TRANSPARENT);
    if (!savedUri.isEmpty()) {
        try {
         FrameLayout animationContainer = new FrameLayout(this);
LinearLayout.LayoutParams animationAreaParams =
        new LinearLayout.LayoutParams(
                dp(250),
                dp(250)
        );   
            
Uri uri = Uri.parse(savedUri);

String mimeType = getContentResolver().getType(uri);
String fileName = uri.getLastPathSegment();

boolean isGif =
        "image/gif".equalsIgnoreCase(mimeType)
        || (fileName != null
        && fileName.toLowerCase(Locale.ROOT).endsWith(".gif"));

boolean isImage =
        mimeType != null
        && mimeType.toLowerCase(Locale.ROOT).startsWith("image/");

if (fileName != null) {
    String lowerName = fileName.toLowerCase(Locale.ROOT);

    if (lowerName.endsWith(".jpg")
            || lowerName.endsWith(".jpeg")
            || lowerName.endsWith(".png")
            || lowerName.endsWith(".webp")
            || lowerName.endsWith(".bmp")) {
        isImage = true;
    }
}

if (isGif) {

    GifView gifView = new GifView(this, uri);

    FrameLayout.LayoutParams gifParams =
            new FrameLayout.LayoutParams(
                    dp(240),
                    dp(240),
                    Gravity.CENTER
            );

    animationContainer.addView(gifView, gifParams);

} else if (isImage) {
    android.widget.ImageView imageView =
            new android.widget.ImageView(this);

    // Полноразмерное фото 12 Мп — это ~48 МБ в памяти, на слабом
    // телефоне приложение падало с OutOfMemoryError.
    android.graphics.Bitmap bitmap =
            decodeSampledBitmap(uri, 560, 560);

    if (bitmap != null) {
        imageView.setImageBitmap(bitmap);
    }

    imageView.setScaleType(
            android.widget.ImageView.ScaleType.FIT_CENTER
    );

    FrameLayout.LayoutParams imageParams =
            new FrameLayout.LayoutParams(
                    dp(240),
                    dp(240),
                    Gravity.CENTER
            );

    animationContainer.addView(imageView, imageParams);

} else {
    VideoView videoView = new VideoView(this);
    videoView.setVideoURI(uri);

    videoView.setOnPreparedListener(player -> {
        player.setLooping(true);
        player.start();
    });

    // Без своего обработчика VideoView показывает AlertDialog из контекста
    // сервиса и падает с WindowManager.BadTokenException.
    videoView.setOnErrorListener((player, what, extra) -> {
        android.util.Log.e(
                TAG,
                "Видео-анимация не открылась: what=" + what
                        + ", extra=" + extra);
        return true;
    });

    FrameLayout.LayoutParams videoParams =
            new FrameLayout.LayoutParams(
                    dp(240),
                    dp(240),
                    Gravity.CENTER
            );

    animationContainer.addView(videoView, videoParams);
}

            root.addView(animationContainer, animationAreaParams);

        } catch (Exception e) {
            android.util.Log.e(TAG, "Animation error", e);
        }
    }
    TextView text = new TextView(this);
    text.setTextColor(Color.WHITE);
    text.setTextSize(prefs.getInt("overlay_text_size", 22));
    text.setTypeface(getOverlayTypeface());
    text.setGravity(Gravity.CENTER);
    text.setShadowLayer(8f, 0f, 3f, Color.BLACK);
    text.setPadding(dp(16), dp(12), dp(16), dp(12));

    // Полупрозрачная плашка: текст читается на любом фоне эфира.
    android.graphics.drawable.GradientDrawable panel =
            new android.graphics.drawable.GradientDrawable();
    panel.setColor(Color.argb(200, 8, 10, 26));
    panel.setCornerRadius(dp(20));
    panel.setStroke(dp(2), Color.argb(160, 255, 20, 147));
    text.setBackground(panel);

    boolean serviceEvent = "follower".equals(gift)
            || "donation".equals(gift);

    int coins = serviceEvent ? -1 : getGiftCoins(gift);
    String displayMessage = message;

    if (coins >= 0) {
        displayMessage += "\n🪙 " + coins + " монет";
    }

    text.setText(displayMessage);

    
LinearLayout.LayoutParams textParams =
        new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

textParams.topMargin = 8;

root.addView(text, textParams);
    int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            : WindowManager.LayoutParams.TYPE_PHONE;

    
WindowManager.LayoutParams params =
        new WindowManager.LayoutParams(
                dp(320),
                WindowManager.LayoutParams.WRAP_CONTENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
        );
              
    params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
    params.y = dp(120);

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
            video.setOnErrorListener((player, what, extra) -> {
                android.util.Log.e(
                        TAG,
                        "Предпросмотр не открылся: what=" + what
                                + ", extra=" + extra);
                return true;
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
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT
            );

    params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
    params.y = dp(120);

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

    
    private void stopVideoViews(View view) {
        if (view instanceof VideoView) {
            try {
                ((VideoView) view).stopPlayback();
            } catch (Exception ignored) {
            }
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;

            for (int i = 0; i < group.getChildCount(); i++) {
                stopVideoViews(group.getChildAt(i));
            }
        }
    }

private void removeOverlay() {
    if (removeTask != null) {
        handler.removeCallbacks(removeTask);
        removeTask = null;
    }

    if (overlayView != null) {
        try {
            // Видео может лежать не в корне, а внутри animationContainer,
            // поэтому обходим всё дерево, иначе оно продолжает играть.
            stopVideoViews(overlayView);

            overlayView.clearAnimation();

            if (windowManager != null) {
                windowManager.removeView(overlayView);
            }
        } catch (Exception e) {
            android.util.Log.e(
                    TAG, "Ошибка закрытия анимации", e);
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

    private void stopSpeech() {
        if (textToSpeech != null) {
            textToSpeech.stop();
        }

        pendingSpeech.set(0);
    }

    private void speechFinished() {
        if (pendingSpeech.get() > 0) {
            pendingSpeech.decrementAndGet();
        }
    }

    // 30 вариантов звучания и настройка громкости.
private void speakText(String text) {
    speakTextForUser(null, text);
}
private void handleDonation(
        String donor, String amount, String currency, String text) {

    String displayMessage = "💰 ДОНАТ " + amount + " "
            + currency + "\n@" + donor;

    if (text != null && !text.isEmpty()) {
        displayMessage += "\n💬 " + text;
    }

    try {
        double value = Double.parseDouble(
                String.valueOf(amount).replace(',', '.'));

        if (!"RUB".equalsIgnoreCase(currency)) {
            value = value * 90;
        }

        addDonationStats(value);
        handler.post(this::updateWidgets);
    } catch (Exception ignored) {
    }

    final String finalMessage = displayMessage;
    final String finalDonor = donor;
    final String finalAmount = amount;
    final String finalCurrency = currency;
    final String finalText = text == null ? "" : text;

    handler.post(() -> {
        if (destroyed) return;

        showGift(finalMessage, "donation");
        playDonationSound();

        String speech = "Донат от " + finalDonor
                + ". Сумма " + finalAmount + " " + finalCurrency;

        if (!finalText.isEmpty()) {
            speech += ". Сообщение: " + finalText;
        }

        speakText(speech);
    });
}

// Прямой опрос DonationAlerts из приложения: работает даже без сервера,
// нужен только токен в настройках.
private void startDonationPolling() {
    if (donationPollTask != null || destroyed) return;

    donationPollTask = new Runnable() {
        @Override
        public void run() {
            if (destroyed) return;

            donationExecutor.execute(() -> pollDonationsOnce());

            handler.postDelayed(this, 15000);
        }
    };

    handler.post(donationPollTask);
}

private void pollDonationsOnce() {
    String token = cleanDaToken(
            prefs.getString("da_token", ""));

    if (token.isEmpty()) return;

    if (!token.equals(donationPollToken)) {
        donationPollToken = token;
        donationPollStarted = false;
        seenDonationIds.clear();
    }

    try {
        java.net.HttpURLConnection connection =
                (java.net.HttpURLConnection) new java.net.URL(
                        "https://www.donationalerts.com/api/v1/alerts/donations?page=1")
                        .openConnection();

        connection.setRequestProperty(
                "Authorization", "Bearer " + token);
        connection.setRequestProperty("Accept", "application/json");
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(15000);

        int code = connection.getResponseCode();

        if (code != 200) {
            lastDonationPollError = "HTTP " + code;
            connection.disconnect();
            return;
        }

        StringBuilder body = new StringBuilder();

        try (java.io.BufferedReader reader =
                     new java.io.BufferedReader(
                             new java.io.InputStreamReader(
                                     connection.getInputStream(),
                                     java.nio.charset.StandardCharsets.UTF_8))) {

            String line;

            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
        }

        connection.disconnect();

        org.json.JSONArray list;

        String raw = body.toString().trim();

        if (raw.startsWith("{")) {
            list = new org.json.JSONObject(raw)
                    .optJSONArray("data");
        } else {
            list = new org.json.JSONArray(raw);
        }

        if (list == null) return;

        if (!donationPollStarted) {
            donationPollStarted = true;

            for (int i = 0; i < list.length(); i++) {
                org.json.JSONObject donation = list.optJSONObject(i);

                if (donation != null) {
                    seenDonationIds.add(
                            donation.optString("id", ""));
                }
            }

            lastDonationPollError = "";
            return;
        }

        // Список приходит от новых к старым — читаем с конца,
        // чтобы анимации шли в правильном порядке.
        for (int i = list.length() - 1; i >= 0; i--) {
            org.json.JSONObject donation = list.optJSONObject(i);

            if (donation == null) continue;

            String id = donation.optString("id", "");

            if (id.isEmpty() || seenDonationIds.contains(id)) {
                continue;
            }

            seenDonationIds.add(id);

            handleDonation(
                    donation.optString("username", "Зритель"),
                    formatAmount(donation.opt("amount")),
                    donation.optString("currency", "RUB"),
                    donation.optString("message", "").trim()
            );
        }

        lastDonationPollError = "";
    } catch (Exception error) {
        lastDonationPollError = String.valueOf(error.getMessage());
    }
}

private String cleanDaToken(String raw) {
    if (raw == null) return "";

    String token = raw.trim();

    int hash = token.indexOf('#');

    if (hash >= 0) {
        token = token.substring(hash + 1);
    }

    int marker = token.indexOf("access_token=");

    if (marker >= 0) {
        token = token.substring(marker + "access_token=".length());
    }

    int amp = token.indexOf('&');

    if (amp >= 0) {
        token = token.substring(0, amp);
    }

    return token.replace(" ", "").trim();
}

private String formatAmount(Object value) {
    if (value == null) return "0";

    if (value instanceof Number) {
        double number = ((Number) value).doubleValue();

        if (number == Math.floor(number)) {
            return String.valueOf((long) number);
        }

        return String.format(Locale.US, "%.2f", number);
    }

    return String.valueOf(value);
}

private void applySavedVoice() {
    if (textToSpeech == null) return;

    try {
        String saved = prefs.getString(
                "selected_voice_name", "");

        java.util.Set<Voice> voices = textToSpeech.getVoices();

        if (voices == null || voices.isEmpty()) return;

        if (!saved.isEmpty()) {
            for (Voice voice : voices) {
                if (voice.getName().equals(saved)) {
                    textToSpeech.setVoice(voice);
                    return;
                }
            }
        }

        // Автовыбор: лучший русский голос. Сетевые голоса Google
        // звучат заметно живее локальных.
        Voice best = null;

        for (Voice voice : voices) {
            Locale locale = voice.getLocale();

            if (locale == null
                    || !"ru".equalsIgnoreCase(locale.getLanguage())) {
                continue;
            }

            if (best == null) {
                best = voice;
                continue;
            }

            boolean better = voice.getQuality() > best.getQuality();

            if (!better && voice.getQuality() == best.getQuality()) {
                better = voice.isNetworkConnectionRequired()
                        && !best.isNetworkConnectionRequired();
            }

            if (better) {
                best = voice;
            }
        }

        if (best != null) {
            textToSpeech.setVoice(best);
        }
    } catch (Exception ignored) {
    }
}

private void applyVoiceTuning() {
    if (textToSpeech == null) return;

    float pitch = prefs.getFloat("voice_pitch", 1.0f);
    float rate = prefs.getFloat("voice_rate", 0.95f);

    textToSpeech.setPitch(Math.max(0.5f, Math.min(1.6f, pitch)));
    textToSpeech.setSpeechRate(Math.max(0.5f, Math.min(1.6f, rate)));
}

private void speakTextForUser(
        String username, String text) {

    android.util.Log.e(TAG,
            "SPEAK CALLED: user=" + username
                    + ", text=" + text
                    + ", ttsReady=" + ttsReady
                    + ", ttsNull=" + (textToSpeech == null)
                    + ", destroyed=" + destroyed);

    if (textToSpeech == null || !ttsReady
            || text == null || text.trim().isEmpty()) {

        android.util.Log.e(TAG,
                "SPEAK SKIPPED: TTS not ready or text empty");
        return;
    }

    if (pendingSpeech.get() >= 3) {
        android.util.Log.d(
                TAG,
                "SPEAK SKIPPED: очередь озвучки заполнена");
        return;
    }
    // Голос, тон и скорость берём из настроек. Раньше они выбирались
    // случайно для каждого сообщения — из-за этого голос «прыгал»
    // и звучал как робот.
    applySavedVoice();
    applyVoiceTuning();

    int volume = Math.max(0, Math.min(
            100, prefs.getInt("voice_volume", 100)));

    android.os.Bundle params = new android.os.Bundle();
    params.putFloat(
            TextToSpeech.Engine.KEY_PARAM_VOLUME,
            volume / 100.0f);

    String spokenText = text;

    if (username != null && !username.trim().isEmpty()
            && prefs.getBoolean("chat_speech_nick", true)) {

        String nick = username.trim();

        if (nick.startsWith("@")) {
            nick = nick.substring(1);
        }

        if (!nick.isEmpty()) {
            spokenText = nick + " пишет: " + text;
        }
    }

    // Длинные сообщения не читаем целиком — эфир не должен тонуть
    // в бесконечной озвучке.
    if (spokenText.length() > 220) {
        spokenText = spokenText.substring(0, 220) + "…";
    }

    pendingSpeech.incrementAndGet();

    int speakResult = textToSpeech.speak(
            spokenText,
            TextToSpeech.QUEUE_ADD,
            params,
            "LiveGiftStudio_" + System.currentTimeMillis()
    );

    if (speakResult != TextToSpeech.SUCCESS) {
        speechFinished();
    }
}

    // Android 15 останавливает foreground-сервисы, которые работают
    // слишком долго. Гасим себя аккуратно, иначе система убьёт процесс.
    @Override
    public void onTimeout(int startId, int fgsType) {
        android.util.Log.w(
                TAG,
                "Foreground service timeout, stopping service");

        stopSelf();
    }

    @Override
    public void onDestroy() {
        destroyed = true;

        if (donationPollTask != null) {
            handler.removeCallbacks(donationPollTask);
            donationPollTask = null;
        }

        donationExecutor.shutdownNow();
android.util.Log.e(
        TAG,
        "OVERLAY SERVICE DESTROYED"
);
        if (textToSpeech != null) {
            stopSpeech();
            textToSpeech.shutdown();
            textToSpeech = null;
            ttsReady = false;
        }

        if (removeTask != null) {
            handler.removeCallbacks(removeTask);
            removeTask = null;
        }

        if (reconnectTask != null) {
            handler.removeCallbacks(reconnectTask);
            reconnectTask = null;
        }

        // Снимаем и все остальные отложенные задачи: иначе они держат
        // ссылку на уже уничтоженный сервис.
        handler.removeCallbacksAndMessages(null);

        removeWidgets();
        removeOverlay();
        stopCurrentSound();

        instance = null;

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
   
    // ===== ВИДЖЕТЫ ЭФИРА =====
    // Цель сбора, лайки и топ подарков показываются отдельным окном
    // поверх эфира и обновляются по событиям с сервера.

    public static void refreshWidgetsFromPrefs() {
        OverlayService service = instance;

        if (service == null) return;

        service.handler.post(service::updateWidgets);
    }

    private String formatNumber(int value) {
        return String.format(Locale.US, "%,d", value)
                .replace(',', ' ');
    }

    private JSONObject statsUsers(String key) {
        try {
            return new JSONObject(prefs.getString(key, "{}"));
        } catch (Exception error) {
            return new JSONObject();
        }
    }

    private void addLikeStats(String username, int count) {
        JSONObject users = statsUsers("stats_like_users");

        try {
            users.put(username, users.optInt(username, 0) + count);
        } catch (Exception ignored) {
        }

        prefs.edit()
                .putInt("stats_like_total",
                        prefs.getInt("stats_like_total", 0) + count)
                .putString("stats_like_users", users.toString())
                .apply();
    }

    private void addGiftStats(
            String username, String giftName, int count) {
        int coins = Math.max(0, getGiftCoins(giftName));
        int total = coins * count;

        JSONObject users = statsUsers("stats_gift_users");

        try {
            users.put(username, users.optInt(username, 0) + total);
        } catch (Exception ignored) {
        }

        prefs.edit()
                .putInt("stats_gift_coins",
                        prefs.getInt("stats_gift_coins", 0) + total)
                .putInt("stats_gift_count",
                        prefs.getInt("stats_gift_count", 0) + count)
                .putString("stats_gift_users", users.toString())
                .apply();
    }

    private void addDonationStats(double amount) {
        int rub = (int) Math.round(amount);

        if (rub <= 0) return;

        prefs.edit()
                .putInt("stats_donation_rub",
                        prefs.getInt("stats_donation_rub", 0) + rub)
                .apply();
    }

    private String goalMetric() {
        return prefs.getString("widget_goal_metric", "coins");
    }

    private String goalUnit() {
        switch (goalMetric()) {
            case "likes":
                return "❤️";
            case "gifts":
                return "🎁";
            case "rub":
                return "₽";
            default:
                return "🪙";
        }
    }

    private int goalCurrent() {
        int base = prefs.getInt("widget_goal_base", 0);

        switch (goalMetric()) {
            case "likes":
                return base + prefs.getInt("stats_like_total", 0);
            case "gifts":
                return base + prefs.getInt("stats_gift_count", 0);
            case "rub":
                return base + prefs.getInt("stats_donation_rub", 0);
            default:
                return base + prefs.getInt("stats_gift_coins", 0);
        }
    }

    private String buildUserList(
            JSONObject users, int limit, String suffix) {
        List<String[]> rows = new ArrayList<>();
        java.util.Iterator<String> keys = users.keys();

        while (keys.hasNext()) {
            String name = keys.next();

            rows.add(new String[]{
                    name,
                    String.valueOf(users.optInt(name, 0))
            });
        }

        Collections.sort(rows, (first, second) -> Integer.compare(
                Integer.parseInt(second[1]),
                Integer.parseInt(first[1])));

        StringBuilder out = new StringBuilder();

        for (int i = 0; i < rows.size() && i < limit; i++) {
            if (i > 0) out.append('\n');

            out.append(i + 1)
                    .append(". @")
                    .append(rows.get(i)[0])
                    .append(" — ")
                    .append(formatNumber(
                            Integer.parseInt(rows.get(i)[1])))
                    .append(suffix);
        }

        if (rows.isEmpty()) {
            out.append("пока пусто");
        }

        return out.toString();
    }

    private TextView widgetText(int size, boolean bold, int color) {
        TextView view = new TextView(this);
        view.setTextColor(color);
        view.setTextSize(size);

        if (bold) {
            view.setTypeface(null, android.graphics.Typeface.BOLD);
        }

        view.setShadowLayer(6f, 0f, 2f, Color.BLACK);
        view.setPadding(0, dp(2), 0, dp(2));

        return view;
    }

    private LinearLayout widgetPanel(int accent) {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(14), dp(10), dp(14), dp(10));

        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.argb(215, 8, 10, 26));
        background.setCornerRadius(dp(18));
        background.setStroke(dp(2), accent);

        panel.setBackground(background);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

        params.bottomMargin = dp(8);
        panel.setLayoutParams(params);

        return panel;
    }

    private void buildWidgets() {
        if (windowManager == null || destroyed) return;

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);

        // Цель сбора
        widgetGoalPanel = widgetPanel(0xFFFF1493);
        widgetGoalTitle = widgetText(13, true, 0xFFFFB3DC);
        widgetGoalValue = widgetText(18, true, Color.WHITE);

        widgetGoalBar = new ProgressBar(
                this, null,
                android.R.attr.progressBarStyleHorizontal);

        widgetGoalBar.setMax(100);
        widgetGoalBar.setProgressTintList(
                android.content.res.ColorStateList
                        .valueOf(0xFFFF1493));
        widgetGoalBar.setProgressBackgroundTintList(
                android.content.res.ColorStateList
                        .valueOf(0x44FFFFFF));
        widgetGoalBar.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(12)));

        widgetGoalPanel.addView(widgetGoalTitle);
        widgetGoalPanel.addView(widgetGoalValue);
        widgetGoalPanel.addView(widgetGoalBar);
        root.addView(widgetGoalPanel);

        // Лайки
        widgetLikesPanel = widgetPanel(0xFF00C8FF);
        widgetLikesValue = widgetText(18, true, Color.WHITE);
        widgetLikesTop = widgetText(13, false, 0xFFCFE6FF);
        widgetLikesPanel.addView(widgetLikesValue);
        widgetLikesPanel.addView(widgetLikesTop);
        root.addView(widgetLikesPanel);

        // Топ подарков
        widgetTopPanel = widgetPanel(0xFFFFB900);
        widgetTopTitle = widgetText(13, true, 0xFFFFE08A);
        widgetTopTitle.setText("🏆 Топ подарков");
        widgetTopList = widgetText(14, true, Color.WHITE);
        widgetTopPanel.addView(widgetTopTitle);
        widgetTopPanel.addView(widgetTopList);
        root.addView(widgetTopPanel);

        widgetsRoot = root;

        int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        WindowManager.LayoutParams params =
                new WindowManager.LayoutParams(
                        dp(300),
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        type,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                | WindowManager.LayoutParams
                                .FLAG_NOT_TOUCHABLE
                                | WindowManager.LayoutParams
                                .FLAG_NOT_TOUCH_MODAL
                                | WindowManager.LayoutParams
                                .FLAG_LAYOUT_IN_SCREEN,
                        PixelFormat.TRANSLUCENT);

        boolean right = "right".equals(
                prefs.getString("widgets_position", "left"));

        // Виджеты живут снизу: там они не мешают всплывающему
        // подарку и анимации по центру экрана.
        params.gravity = Gravity.BOTTOM
                | (right ? Gravity.END : Gravity.START);
        params.x = dp(10);
        params.y = dp(120);

        int scale = prefs.getInt("widgets_scale", 100);
        float factor = scale / 100f;

        root.setScaleX(factor);
        root.setScaleY(factor);

        // Масштаб считаем от нижнего угла, иначе крупный размер
        // уезжает за край экрана.
        root.post(() -> {
            root.setPivotX(right ? root.getWidth() : 0f);
            root.setPivotY(root.getHeight());
        });

        try {
            windowManager.addView(root, params);
        } catch (Exception error) {
            android.util.Log.e(
                    TAG, "Не удалось показать виджеты", error);
            widgetsRoot = null;
        }
    }

    private void updateWidgets() {
        if (destroyed) return;

        if (!prefs.getBoolean("widgets_enabled", false)) {
            removeWidgets();
            return;
        }

        if (widgetsRoot == null) {
            buildWidgets();
        }

        if (widgetsRoot == null || widgetGoalPanel == null) return;

        boolean goalOn = prefs.getBoolean("widget_goal_on", true);
        widgetGoalPanel.setVisibility(
                goalOn ? View.VISIBLE : View.GONE);

        if (goalOn) {
            int target = Math.max(
                    1, prefs.getInt("widget_goal_target", 1000));
            int current = goalCurrent();
            int percent = (int) Math.min(100,
                    Math.round(current * 100.0 / target));

            widgetGoalTitle.setText("🎯 " + prefs.getString(
                    "widget_goal_title", "Сбор"));

            widgetGoalValue.setText(
                    formatNumber(current)
                            + " / " + formatNumber(target)
                            + " " + goalUnit()
                            + "  " + percent + "%");

            widgetGoalBar.setProgress(percent);
        }

        boolean likesOn = prefs.getBoolean("widget_likes_on", true);
        widgetLikesPanel.setVisibility(
                likesOn ? View.VISIBLE : View.GONE);

        if (likesOn) {
            widgetLikesValue.setText("❤️ Лайки: "
                    + formatNumber(prefs.getInt(
                    "stats_like_total", 0)));

            widgetLikesTop.setText(buildUserList(
                    statsUsers("stats_like_users"),
                    prefs.getInt("widget_likes_top", 3),
                    " ❤️"));
        }

        boolean topOn = prefs.getBoolean("widget_top_on", true);
        widgetTopPanel.setVisibility(
                topOn ? View.VISIBLE : View.GONE);

        if (topOn) {
            widgetTopList.setText(buildUserList(
                    statsUsers("stats_gift_users"),
                    prefs.getInt("widget_top_limit", 3),
                    " 🪙"));
        }
    }

    private void removeWidgets() {
        if (widgetsRoot != null && windowManager != null) {
            try {
                windowManager.removeView(widgetsRoot);
            } catch (Exception ignored) {
            }
        }

        widgetsRoot = null;
        widgetGoalPanel = null;
        widgetGoalTitle = null;
        widgetGoalValue = null;
        widgetGoalBar = null;
        widgetLikesPanel = null;
        widgetLikesValue = null;
        widgetLikesTop = null;
        widgetTopPanel = null;
        widgetTopTitle = null;
        widgetTopList = null;
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
