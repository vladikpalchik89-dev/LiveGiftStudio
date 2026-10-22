
package com.livegiftstudio;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.drawable.GradientDrawable;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
private int menuRowIndex = 0;
    private static final String DONATE_URL =
            "https://www.donationalerts.com/r/vladdonat99";

    private static final String SERVER_URL =
            "https://script.google.com/macros/s/AKfycbz9_7ApvKuZDZj8EGWNYvRaMxRi3DYQgsGigCrGLW3u9cthlqr7Q0FXBr8mwxxSms9S_A/exec";

    private static final String APP_KEY =
            "LGS_PRO_7mQ9vK2xR8pN4wT6zA1cF5hB";

    private static final int PICK_SOUND = 501;
    private static final int PICK_FOLLOW_SOUND = 502;
    private static final int PICK_DONATION_SOUND = 503;
    private static final int PICK_MEDIA_FOLDER = 508;
private static final int PICK_MEDIA_FILES = 509;
private static final int PICK_ANIMATION_FOLLOWER = 601;
private static final int PICK_ANIMATION_DONATION = 602;
private static final int PICK_ANIMATION_GIFT = 603;
private String selectedAnimationType = "follower";
    private TextToSpeech voicePreview;
    private boolean voicePreviewReady = false;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private SharedPreferences prefs;
    private LinearLayout container;
    private TextView statsText;
    private MediaPlayer previewPlayer;
// Категория, в которую сейчас добавляем звук
private String libraryCategory;

// Индекс записи, которая сейчас играет
private int playingLibrarySound = -1;

// Список звуков будет храниться в SharedPreferences
private static final String LIBRARY_MEMES = "library_memes";
private static final String LIBRARY_FUNNY = "library_funny";
private static final String LIBRARY_MELODIES = "library_melodies";
private static final String LIBRARY_EFFECTS = "library_effects";
    private String selectedGiftKey;
    private String selectedGiftName;

    private int gifts;
    private int donations;
    private int donationIncome;
    private int orders;

    
private final String[][] giftCatalog = {
    {"rose", "🌹 Роза"},
    {"rose_1", "🌹 Роза 2"},
    {"finger_heart", "🫰 Сердечко пальцами"},
    {"tiktok", "🎵 TikTok"},
    {"donut", "🍩 Пончик"},
    {"ice_cream_cone", "🍦 Рожок мороженого"},
    {"bravo", "👏 Браво"},
    {"confetti", "🎉 Конфетти"},
    {"butterfly", "🦋 Бабочка"},
    {"ticket", "🎟️ Билет"},
    {"corgi", "🐶 Корги"},
    {"squirrel", "🐿️ Белка"},
    {"teddy_bear", "🧸 Мишка"},
    {"pomegranate", "🍎 Гранат"},
    {"perfume", "🌸 Парфюм"},
    {"hat_moustache", "🎩 Шляпа с усами"},
    {"from_the_heart", "❤️ От всего сердца"},
    {"heart_from_hands", "🫶 Сердце из рук"},
    {"friendship_pendant", "💞 Подвеска дружбы"},
    {"love_more", "💗 Больше любви"},
    {"you_are_amazing", "🌟 Ты потрясающий"},
    {"flying_airplanes", "✈️ Летающие самолётики"},
    {"intense_reaction", "😲 Сильная реакция"},
    {"slowmo", "🐢 Замедление"}
};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
try {
    Intent serviceIntent =
            new Intent(MainActivity.this, OverlayService.class);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(serviceIntent);
    } else {
        startService(serviceIntent);
    }
} catch (Exception e) {
    android.util.Log.e(
            "LiveGiftStudio",
            "Не удалось запустить сервис",
            e
    );
}


        prefs = getSharedPreferences(
                "LiveGiftStudio", MODE_PRIVATE);
        voicePreview = new TextToSpeech(this, status -> {
    if (status == TextToSpeech.SUCCESS && voicePreview != null) {
        int result = voicePreview.setLanguage(new Locale("ru", "RU"));

        voicePreviewReady =
                result != TextToSpeech.LANG_MISSING_DATA
                && result != TextToSpeech.LANG_NOT_SUPPORTED;
    }
});

        loadStats();
        buildInterface();
    }
 
private void buildInterface() {
    menuRowIndex = 0;
    ScrollView scroll = new ScrollView(this);
    scroll.setFillViewport(true);
    scroll.
setBackgroundColor(
        prefs.getInt("app_bg_color", Color.rgb(3, 7, 22))
);

    container = new LinearLayout(this);
    container.setOrientation(LinearLayout.VERTICAL);
    container.setPadding(dp(12), dp(12), dp(12), dp(24));
    container.
setBackgroundColor(
        prefs.getInt("app_bg_color", Color.rgb(3, 7, 22))
);


    scroll.addView(container);
    setContentView(scroll);

    TextView title = new TextView(this);
    title.setText("🎬 LiveGiftStudio PRO");
    title.setTextSize(29);
    title.setTypeface(null, android.graphics.Typeface.BOLD);
    title.setTextColor(Color.WHITE);
    title.setGravity(Gravity.CENTER);
    title.setShadowLayer(dp(12), 0, 0, Color.CYAN);
    title.setPadding(0, dp(12), 0, dp(4));
    container.addView(title);

    TextView subtitle = new TextView(this);
    subtitle.setText("Инструменты для TikTok LIVE");
    subtitle.setTextSize(17);
    subtitle.setTypeface(null, android.graphics.Typeface.BOLD);
    subtitle.setTextColor(Color.rgb(230, 235, 255));
    subtitle.setGravity(Gravity.CENTER);
    subtitle.setPadding(0, dp(4), 0, dp(20));
    container.addView(subtitle);

    addMenuRow(
        "🎁\nПодарки\nи мои звуки", 0xFFFF1493,
        "🎙️\nНастроить\nголос и озвучку", 0xFF008CFF,
        v -> showGiftCatalog(),
        v -> showVoiceSettings()
    );

    addMenuRow(
        "🔔\nВыбрать\nзвук подписки", 0xFFFFB900,
        "🎬\nТест\nанимации", 0xFF9B20FF,
        v -> chooseFollowSound(),
        v -> startOverlay()
);

addMenuRow(
        "💖\nПроверить\nподписку", 0xFFFF1493,
        "💰\nНазначить\nзвук доната", 0xFFFFB900,
        v -> testFollowEvent(),
        v -> chooseDonationAlertSound()
);
addMenuRow(
        "🧪\nТест\nдоната 50 ₽", 0xFF00C853,
        "💰\nОткрыть\nдонаты", 0xFFFF1493,
        v -> testDonation(),
        v -> openLink(DONATE_URL)
);
addMenuRow(
        "🔊\nБиблиотека\nзвуков", 0xFF00C8FF,
        "🎵\nМои\nаудиофайлы", 0xFF00C853,
        v -> showSoundLibrary(),
        v -> showSoundLibrary()
);
    
    addMenuRow(
        "📚\nМоя\nмедиатека", 0xFF00C853,
        "📁\nЗагрузить\nмедиафайлы", 0xFF008CFF,
        v -> showMediaLibrary(),
        v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Загрузка медиа")
                    .setItems(
                            new String[]{
                                    "📁 Загрузить папку",
                                    "📄 Загрузить файлы"
                            },
                            (dialog, which) -> {
                                if (which == 0) {
                                    chooseMediaFolder();
                                } else {
                                    chooseMediaFiles();
                                }
                            })
                    .show();
        }
    );

    addMenuRow(
        "🎵\nПодключение\nTikTok LIVE", 0xFF087BFF,
        "💰\nОткрыть\nдонаты", 0xFF00D6B7,       
v -> {
    message("Нажатие на кнопку TikTok LIVE");
    showTikTokAccounts();
},
        v -> openLink(DONATE_URL)
    );

    addMenuRow(
        "🤖\nИИ-фото —\n50 ₽", 0xFFFF1493,
        "⭐\nОценка аккаунта —\n50 ₽", 0xFF7B20FF,
        v -> createOrder("ИИ-фото", 50),
        v -> createOrder("Оценка аккаунта", 50)
    );

    addMenuRow(
        "📢\nРеклама —\n100 ₽", 0xFF087BFF,
        "💬\nДругая\nуслуга", 0xFFFF1764,
        v -> createOrder("Реклама", 100),
        v -> createOrder("Другая услуга", 0)
    );

    addMenuRow(
        "🛍️\nИстория\nзаказов", 0xFF087BFF,
        "📊\nСтатистика", 0xFF7928FF,
        v -> showHistory(),
        v -> showStats()
    );

    addMenuRow(
        "🎁\nЗаписать\nподарок", 0xFF00C853,
        "🪙\nЗаписать\nдонат", 0xFFFF1764,
        v -> addDonation("gift"),
        v -> addDonation("donation")
    );

    addMenuRow(
        "🔄\nСбросить\nстатистику", 0xFF3949AB,
        "✨\nLiveGiftStudio\nPRO", 0xFF008CFF,
        v -> resetStats(),
        v -> showStats()
    );
    addMenuRow(
        "🎞️\nНастроить\nанимации",
        0xFF9B20FF,
        "▶️\nПроверить\nанимацию",
        0xFF00C8FF,
        v -> showAnimationSettings(),
        v -> testSavedAnimation()
);
addMenuRow(
        "⚙️\nНастройки\nоформления", 0xFF00C8B4,
        "♻️\nСбросить\nоформление", 0xFF3949AB,
        v -> showAppearanceSettings(),
        v -> resetAppearance()
);
    
    statsText = new TextView(this);
    statsText.setTextColor(Color.WHITE);
    statsText.setTextSize(16);
    statsText.setPadding(dp(12), dp(24), dp(12), dp(12));
    container.addView(statsText);

    updateStats();
}
   private void showAnimationSettings() {
    String[] options = {
            "🔔 Анимация нового подписчика",
            "🎁 Анимация подарка TikTok",
            "💰 Анимация DonationAlerts",
            "▶ Проверить анимацию подписки",
            "▶ Проверить анимацию доната",
            "🗑 Сбросить анимацию подписки",
            "🗑 Сбросить анимацию доната"
    };

    new AlertDialog.Builder(this)
            .setTitle("🎞️ GIF и видео")
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0:
                        selectedAnimationType = "follower";
                        chooseAnimationFile(PICK_ANIMATION_FOLLOWER);
                        break;

                    case 1:
                        showAnimationGiftCatalog();
                        break;

                    case 2:
                        selectedAnimationType = "donation";
                        chooseAnimationFile(PICK_ANIMATION_DONATION);
                        break;

                    case 3:
                        testAnimation("follower");
                        break;

                    case 4:
                        testAnimation("donation");
                        break;

                    case 5:
                        prefs.edit()
                                .remove("animation_follower_uri")
                                .apply();
                        message("Анимация подписки сброшена");
                        break;

                    case 6:
                        prefs.edit()
                                .remove("animation_donation_uri")
                                .apply();
                        message("Анимация доната сброшена");
                        break;
                }
            })
            .setNegativeButton("Закрыть", null)
            .show();
}


private void chooseAnimationFile(int requestCode) {
    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
    intent.addCategory(Intent.CATEGORY_OPENABLE);
    intent.setType("*/*");

    intent.putExtra(
            Intent.EXTRA_MIME_TYPES,
            new String[]{
                    "image/*",
                    "video/mp4",
                    "video/webm",
                    "video/3gpp"
            }
    );

    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

    try {
        startActivityForResult(intent, requestCode);
    } catch (Exception e) {
        message("Не удалось открыть выбор файла");
    }
}


private void testFollowEvent() {
    Intent intent = new Intent(this, OverlayService.class);
    intent.putExtra("test_follow", "yes");
    startOverlay();
    startService(intent);
}

private void testFollowSound() {
    Intent intent = new Intent(this, OverlayService.class);
    intent.putExtra("test_follow_sound", "yes");
    startOverlay();
    startService(intent);
}
private void testSavedAnimation() {
    new AlertDialog.Builder(this)
            .setTitle("▶ Проверка анимации")
            .setItems(
                    new String[]{
                            "🔔 Подписка",
                            "🎁 Подарок",
                            "💰 Донат"
                    },
                    (dialog, which) -> {
                        if (which == 0) {
                            testAnimation("follower");
                        } else if (which == 1) {
                            showAnimationGiftCatalog();
                        } else {
                            testAnimation("donation");
                        }
                    }
            )
            .setNegativeButton("Отмена", null)
            .show();
}


private void testAnimation(String type) {
    String key;

    if ("follower".equals(type)) {
        key = "animation_follower_uri";
    } else if ("donation".equals(type)) {
        key = "animation_donation_uri";
    } else if ("gift".equals(type)) {
        if (selectedGiftKey == null || selectedGiftKey.isEmpty()) {
            message("Сначала выбери подарок");
            return;
        }

        key = "animation_gift_" + selectedGiftKey;
    } else {
        message("Неизвестный тип анимации");
        return;
    }

    String uri = prefs.getString(key, "");

    if (uri.isEmpty()) {
        message("Сначала выбери файл анимации");
        return;
    }

    Intent intent = new Intent(this, OverlayService.class);
    intent.putExtra("preview_animation", type);
    intent.putExtra("preview_animation_uri", uri);

    if ("gift".equals(type)) {
        intent.putExtra(
                "preview_animation_gift_key",
                selectedGiftKey
        );
    }

    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }

        message("Запускаю проверку анимации");
    } catch (Exception e) {
        message("Не удалось запустить анимацию");
        android.util.Log.e("LiveGiftStudio", "Animation preview error", e);
    }
}



private void showAnimationGiftCatalog() {
    String[] names = new String[giftCatalog.length];

    for (int i = 0; i < giftCatalog.length; i++) {
        names[i] = giftCatalog[i][1];
    }

    new AlertDialog.Builder(this)
            .setTitle("🎁 Выбери подарок")
            .setItems(names, (dialog, which) -> {
                selectedGiftKey = giftCatalog[which][0];
                selectedGiftName = giftCatalog[which][1];
                selectedAnimationType = "gift";

                String key = "animation_gift_" + selectedGiftKey;

                new AlertDialog.Builder(this)
                        .setTitle(selectedGiftName)
                        .setItems(
                                new String[]{
                                        "🎞️ Назначить фото, GIF или видео",
                                        "▶ Проверить анимацию",
                                        "🗑 Сбросить анимацию"
                                },
                                (d, action) -> {
                                    if (action == 0) {
                                        chooseAnimationFile(
                                                PICK_ANIMATION_GIFT
                                        );
                                    } else if (action == 1) {
                                        testAnimation(selectedGiftKey);
                                    } else {
                                        prefs.edit()
                                                .remove(key)
                                                .apply();

                                        message(
                                                "Анимация подарка сброшена"
                                        );
                                    }
                                }
                        )
                        .setNegativeButton("Назад", null)
                        .show();
            })
            .setNegativeButton("Отмена", null)
            .show();
}

private void showAppearanceSettings() {
    String[] options = {
            "🌌 Фон приложения",
            "🎨 Цвет кнопок",
            "🔘 Форма кнопок",
            "🔠 Размер текста",
            "✏️ Изменить надпись кнопки"
    };

    new AlertDialog.Builder(this)
            .setTitle("⚙️ Оформление LiveGiftStudio")
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0:
                        chooseAppBackground();
                        break;
                    case 1:
                        chooseButtonColor();
                        break;
                    case 2:
                        chooseButtonShape();
                        break;
                    case 3:
                        chooseAppFontSize();
                        break;
                    case 4:
                        editButtonLabel();
                        break;
                }
            })
            .setNegativeButton("Закрыть", null)
            .show();
}

private void chooseAppBackground() {
    String[] names = {"🌌 Тёмно-синий", "⚫ Чёрный", "🟣 Фиолетовый", "🟢 Тёмно-зелёный"};
    int[] colors = {
            0xFF030716,
            0xFF101010,
            0xFF211038,
            0xFF06251C
    };

    new AlertDialog.Builder(this)
            .setTitle("Выбери фон")
            .setItems(names, (dialog, which) -> {
                prefs.edit().putInt("app_bg_color", colors[which]).apply();
                buildInterface();
            })
            .setNegativeButton("Отмена", null)
            .show();
}

private void chooseButtonColor() {
    String[] names = {"💗 Розовый", "💙 Синий", "💜 Фиолетовый", "💚 Зелёный", "🧡 Оранжевый"};
    int[] colors = {
            0xFFFF1493,
            0xFF087BFF,
            0xFF7928FF,
            0xFF00C853,
            0xFFFF8800
    };

    new AlertDialog.Builder(this)
            .setTitle("Выбери цвет кнопок")
            .setItems(names, (dialog, which) -> {
                prefs.edit().putInt("button_color", colors[which]).apply();
                buildInterface();
            })
            .setNegativeButton("Отмена", null)
            .show();
}

private void chooseButtonShape() {
    String[] names = {"⬜ Квадратные", "▢ Слегка закруглённые", "⭕ Сильно закруглённые"};
    int[] radiuses = {4, 14, 25};

    new AlertDialog.Builder(this)
            .setTitle("Форма кнопок")
            .setItems(names, (dialog, which) -> {
                prefs.edit().putInt("button_radius", radiuses[which]).apply();
                buildInterface();
            })
            .setNegativeButton("Отмена", null)
            .show();
}

private void chooseAppFontSize() {
    String[] names = {"Маленький", "Обычный", "Крупный", "Очень крупный"};
    int[] sizes = {13, 16, 19, 22};

    new AlertDialog.Builder(this)
            .setTitle("Размер текста")
            .setItems(names, (dialog, which) -> {
                prefs.edit().putInt("app_font_size", sizes[which]).apply();
                buildInterface();
            })
            .setNegativeButton("Отмена", null)
            .show();
}

private void editButtonLabel() {
    String[] buttons = {
            "Подарки и мои звуки",
            "Настроить голос",
            "Звук подписки",
            "Тест анимации",
            "Подключение TikTok",
            "Открыть донаты",
            "ИИ-фото",
            "Оценка аккаунта",
            "Реклама",
            "Другая услуга",
            "История заказов",
            "Статистика",
            "Записать подарок",
            "Записать донат",
            "Сбросить статистику",
            "LiveGiftStudio PRO"
    };

    new AlertDialog.Builder(this)
            .setTitle("Какую кнопку изменить?")
            .setItems(buttons, (dialog, which) -> {
                EditText input = new EditText(this);
                input.setHint("Новый текст или эмодзи");
                input.setSingleLine(false);

                new AlertDialog.Builder(this)
                        .setTitle("Новая надпись")
                        .setView(input)
                        .setPositiveButton("Сохранить", (d, w) -> {
                            String value = input.getText().toString().trim();
                            if (!value.isEmpty()) {
                                prefs.edit()
        .putString("custom_label_" + which, value)
        .apply();

buildInterface();
message("Надпись сохранена");
                            }
                        })
                        .setNegativeButton("Отмена", null)
                        .show();
            })
            .setNegativeButton("Отмена", null)
            .show();
}

private void resetAppearance() {
    android.content.SharedPreferences.Editor editor = prefs.edit();

    editor.remove("app_bg_color");
    editor.remove("button_color");
    editor.remove("button_radius");
    editor.remove("app_font_size");

    for (int i = 0; i < 18; i++) {
        editor.remove("custom_label_" + i);
    }

    editor.apply();
    buildInterface();
    message("Оформление сброшено");
}
      
private int dp(float value) {
    return (int) (value * getResources()
            .getDisplayMetrics().density + 0.5f);
}


private void addMenuRow(
        String leftText, int leftColor,
        String rightText, int rightColor,
        View.OnClickListener leftAction,
        View.OnClickListener rightAction) {

    LinearLayout row = new LinearLayout(this);
    row.setOrientation(LinearLayout.HORIZONTAL);
    row.setGravity(Gravity.CENTER_VERTICAL);

    LinearLayout.LayoutParams rowParams =
            new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

    rowParams.setMargins(0, 0, 0, dp(10));
    container.addView(row, rowParams);

    int rowIndex = menuRowIndex++;

    row.addView(createNeonButton(
            leftText, leftColor, leftAction,
            "custom_label_" + (rowIndex * 2)),
            new LinearLayout.LayoutParams(
                    0, dp(112), 1f));

    View gap = new View(this);
    row.addView(gap,
            new LinearLayout.LayoutParams(dp(10), 1));

    row.addView(createNeonButton(
            rightText, rightColor, rightAction,
            "custom_label_" + (rowIndex * 2 + 1)),
            new LinearLayout.LayoutParams(
                    0, dp(112), 1f));
}


private TextView createNeonButton(
        String text,
        int color,
        View.OnClickListener listener,
        String buttonId) {

    TextView button = new TextView(this);

    String savedText = prefs.getString(buttonId, null);

    if (savedText != null && !savedText.trim().isEmpty()) {
        button.setText(savedText + "\n›");
    } else {
        button.setText(text + "\n›");
    }

    button.setTextColor(Color.WHITE);
    button.setTextSize(prefs.getInt("app_font_size", 16));
    button.setTypeface(null, android.graphics.Typeface.BOLD);
    button.setGravity(Gravity.CENTER);
    button.setPadding(dp(5), dp(6), dp(5), dp(6));
    button.setMaxLines(4);

    color = prefs.getInt("button_color", color);
    button.setShadowLayer(dp(4), 0, 0, color);
    button.setClickable(true);
    button.setFocusable(true);
    button.setOnClickListener(listener);

    int darker = darken(color);

    GradientDrawable background = new GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            new int[]{
                    lighten(color),
                    color,
                    darker,
                    Color.rgb(8, 12, 36)
            });

    background.setCornerRadius(
            dp(prefs.getInt("button_radius", 25)));

    background.setStroke(dp(2), lighten(color));

    button.setBackground(background);
    button.setElevation(dp(5));

    return button;
}

// Открыть категории библиотеки
private void showSoundLibrary() {
    String[] categories = {
            "😂 Мемы",
            "🤣 Смешные фразы",
            "🎵 Мелодии",
            "💥 Эффекты"
    };

    new AlertDialog.Builder(this)
            .setTitle("🔊 Библиотека звуков")
            .setItems(categories, (dialog, which) -> {
                switch (which) {
                    case 0:
                        showLibraryCategory("memes");
                        break;
                    case 1:
                        showLibraryCategory("funny");
                        break;
                    case 2:
                        showLibraryCategory("melodies");
                        break;
                    case 3:
                        showLibraryCategory("effects");
                        break;
                }
            })
            .setNegativeButton("Закрыть", null)
            .show();
}

// Название категории
private String getLibraryCategoryName(String category) {
    switch (category) {
        case "memes":
            return "Мемы";
        case "funny":
            return "Смешные фразы";
        case "melodies":
            return "Мелодии";
        default:
            return "Эффекты";
    }
}

// Ключ для хранения списка
private String getLibraryKey(String category) {
    switch (category) {
        case "memes":
            return LIBRARY_MEMES;
        case "funny":
            return LIBRARY_FUNNY;
        case "melodies":
            return LIBRARY_MELODIES;
        default:
            return LIBRARY_EFFECTS;
    }
}

// Прочитать список звуков из памяти телефона
private JSONArray getLibrarySounds(String category) {
    try {
        return new JSONArray(
                prefs.getString(getLibraryKey(category), "[]")
        );
    } catch (Exception e) {
        return new JSONArray();
    }
}

// Показать все записи в категории
private void showLibraryCategory(String category) {
    libraryCategory = category;

    JSONArray sounds = getLibrarySounds(category);
    ArrayList<String> names = new ArrayList<>();

    for (int i = 0; i < sounds.length(); i++) {
        JSONObject sound = sounds.optJSONObject(i);

        if (sound != null) {
            names.add("🎵 " + sound.optString("name", "Звук"));
        }
    }

    String[] items = names.toArray(new String[0]);

    AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("📂 " + getLibraryCategoryName(category))
            .setItems(items, (d, which) -> {
                showLibrarySoundOptions(category, which);
            })
            .setPositiveButton("➕ Добавить звук", (d, w) -> {
                chooseLibrarySound(category);
            })
            .setNegativeButton("Назад", (d, w) -> {
                showSoundLibrary();
            })
            .create();

    dialog.show();

    // Если список пустой, всё равно можно добавлять записи
    if (names.isEmpty()) {
        dialog.setTitle(
                "📂 " + getLibraryCategoryName(category)
                        + "\nСписок пуст. Добавь аудиофайл."
        );
    }
}

// Выбор аудиофайла с телефона
private void chooseLibrarySound(String category) {
    libraryCategory = category;

    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
    intent.addCategory(Intent.CATEGORY_OPENABLE);
    intent.setType("audio/*");
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

    try {
        int requestCode;

        switch (category) {
            case "memes":
                requestCode = 504;
                break;
            case "funny":
                requestCode = 505;
                break;
            case "melodies":
                requestCode = 506;
                break;
            default:
                requestCode = 507;
                break;
        }

        startActivityForResult(intent, requestCode);
    } catch (Exception e) {
        message("Не удалось открыть выбор аудиофайла");
    }
}

// Меню отдельной записи

private void showLibrarySoundOptions(String category, int index) {
    JSONArray sounds = getLibrarySounds(category);
    JSONObject sound = sounds.optJSONObject(index);

    if (sound == null) {
        message("Запись не найдена");
        return;
    }

    String name = sound.optString("name", "Звук");
    String uri = sound.optString("uri", "");

    new AlertDialog.Builder(this)
            .setTitle("🎵 " + name)
            .setItems(
                    new String[]{
                            "▶ Проверить звук",
                            "⏸ Пауза / продолжить",
                            "🔔 Назначить на подписку",
                            "🗑 Удалить запись"
                    },
                    (dialog, which) -> {
                        if (which == 0) {
                            playLibrarySound(category, index);
                        } else if (which == 1) {
                            pauseLibrarySound();
                        } else if (which == 2) {
                            if (uri.isEmpty()) {
                                message("Файл не найден");
                                return;
                            }

                            prefs.edit()
                                    .putString("follow_sound_uri", uri)
                                    .apply();

                            message("Звук назначен на подписку: " + name);
                        } else {
                            deleteLibrarySound(category, index);
                        }
                    }
            )
            .setNegativeButton("Назад", (d, w) -> {
                showLibraryCategory(category);
            })
            .show();
}

// Запуск или продолжение прослушивания
private void playLibrarySound(String category, int index) {
    JSONArray sounds = getLibrarySounds(category);
    JSONObject sound = sounds.optJSONObject(index);

    if (sound == null) {
        message("Звук не найден");
        return;
    }

    String uriText = sound.optString("uri", "");

    if (uriText.isEmpty()) {
        message("Файл не найден");
        return;
    }

    // Если этот же файл уже стоит на паузе — продолжить
    if (previewPlayer != null && playingLibrarySound == index) {
        try {
            if (!previewPlayer.isPlaying()) {
                previewPlayer.start();
                message("Воспроизведение продолжено");
                return;
            }
        } catch (Exception ignored) {}
    }

    stopPreview();

    try {
        previewPlayer = new MediaPlayer();
        playingLibrarySound = index;

        previewPlayer.setDataSource(
                this, Uri.parse(uriText)
        );

        previewPlayer.setOnPreparedListener(player -> {
            player.start();
            message("▶ " + sound.optString("name", "Звук"));
        });

        previewPlayer.setOnCompletionListener(player -> {
            stopPreview();
        });

        previewPlayer.setOnErrorListener((player, what, extra) -> {
            stopPreview();
            message("Не удалось воспроизвести файл");
            return true;
        });

        previewPlayer.prepareAsync();

    } catch (Exception e) {
        stopPreview();
        message("Ошибка воспроизведения");
    }
}

// Пауза
private void pauseLibrarySound() {
    if (previewPlayer == null) {
        message("Сейчас ничего не играет");
        return;
    }

    try {
        if (previewPlayer.isPlaying()) {
            previewPlayer.pause();
            message("⏸ Звук поставлен на паузу");
        } else {
            previewPlayer.start();
            message("▶ Воспроизведение продолжено");
        }
    } catch (Exception e) {
        stopPreview();
        message("Не удалось поставить звук на паузу");
    }
}

// Удалить одну запись
private void deleteLibrarySound(String category, int index) {
    JSONArray oldSounds = getLibrarySounds(category);
    JSONArray newSounds = new JSONArray();

    for (int i = 0; i < oldSounds.length(); i++) {
        if (i != index) {
            newSounds.put(oldSounds.optJSONObject(i));
        }
    }

    prefs.edit()
            .putString(getLibraryKey(category), newSounds.toString())
            .apply();

    stopPreview();
    message("Запись удалена");

    showLibraryCategory(category);
}
            
    private void showGiftCatalog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 8, 16, 8);

        EditText search = new EditText(this);
        search.setHint("Найти подарок...");
        layout.addView(search);

        ListView list = new ListView(this);
        layout.addView(list,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 550));

        ArrayList<String> names = new ArrayList<>();
        ArrayList<String> keys = new ArrayList<>();

        for (String[] gift : giftCatalog) {
            keys.add(gift[0]);
            names.add(gift[1]);
        }

        ArrayList<String> visibleNames = new ArrayList<>(names);
        ArrayList<String> visibleKeys = new ArrayList<>(keys);

        
ArrayAdapter<String> adapter = new ArrayAdapter<String>(
        this,
        android.R.layout.simple_list_item_1,
        visibleNames) {

    @Override
    public View getView(
            int position,
            View convertView,
            android.view.ViewGroup parent) {

        LinearLayout row = new LinearLayout(MainActivity.this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(8), dp(6), dp(8), dp(6));

        ImageView image = new ImageView(MainActivity.this);

        String key = visibleKeys.get(position);

        int imageId = getResources().getIdentifier(
                "gift_" + key,
                "drawable",
                getPackageName()
        );

        if (imageId == 0) {
            imageId = getResources().getIdentifier(
                    "gift_default",
                    "drawable",
                    getPackageName()
            );
        }

        if (imageId != 0) {
            image.setImageResource(imageId);
        }

        image.setScaleType(ImageView.ScaleType.FIT_CENTER);

        row.addView(image, new LinearLayout.LayoutParams(
                dp(48), dp(48)));

        TextView label = new TextView(MainActivity.this);
        label.setText(visibleNames.get(position));
        label.setTextColor(Color.BLACK);
        label.setTextSize(16);
        label.setPadding(dp(12), 0, 0, 0);

        row.addView(label, new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f));

        return row;
    }
};

list.setAdapter(adapter);


        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("🎁 Каталог подарков")
                .setView(layout)
                .setNegativeButton("Закрыть", null)
                .create();

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {

                String query = s.toString()
                        .toLowerCase().trim();

                visibleNames.clear();
                visibleKeys.clear();

                for (int i = 0; i < names.size(); i++) {
                    if (names.get(i).toLowerCase().contains(query)
                            || keys.get(i).toLowerCase().contains(query)) {
                        visibleNames.add(names.get(i));
                        visibleKeys.add(keys.get(i));
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        list.setOnItemClickListener((parent, view, position, id) -> {
            selectedGiftKey = visibleKeys.get(position);
            selectedGiftName = visibleNames.get(position);
            dialog.dismiss();
            showGiftSoundOptions();
        });

        dialog.show();
    }

    private void showGiftSoundOptions() {
        String uri = prefs.getString(
                "gift_sound_" + selectedGiftKey, null);

        String status = uri == null
                ? "Свой звук не выбран"
                : "Для подарка назначен свой звук";

        new AlertDialog.Builder(this)
                .setTitle(selectedGiftName)
                .setMessage(status)
                .setPositiveButton("🎵 Выбрать звук",
                        (d, w) -> chooseSound())
                .setNeutralButton("▶ Проверить звук",
                        (d, w) -> previewSavedSound())
                .setNegativeButton("Сбросить звук",
                        (d, w) -> {
                            prefs.edit().remove(
                                    "gift_sound_" + selectedGiftKey).apply();
                            message("Настройка звука удалена");
                        })
                .show();
    }

    private void chooseSound() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

        try {
            startActivityForResult(intent, PICK_SOUND);
        } catch (Exception e) {
            message("Не удалось открыть выбор звука");
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        
        // Импорт выбранной папки
if (requestCode == PICK_MEDIA_FOLDER) {
    if (resultCode != RESULT_OK || data == null
            || data.getData() == null) {
        return;
    }

    Uri folderUri = data.getData();

    try {
        getContentResolver().takePersistableUriPermission(
                folderUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
        );

        importMediaFolder(folderUri);

    } catch (Exception e) {
        message("Не удалось открыть папку");
    }

    return;
}

// Импорт отдельных файлов
if (requestCode == PICK_MEDIA_FILES) {
    if (resultCode != RESULT_OK || data == null) {
        return;
    }

    if (data.getClipData() != null) {
        android.content.ClipData clips = data.getClipData();

        for (int i = 0; i < clips.getItemCount(); i++) {
            Uri fileUri = clips.getItemAt(i).getUri();
            importMediaFile(fileUri);
        }

    } else if (data.getData() != null) {
        importMediaFile(data.getData());
    }

    return;
}
        if (requestCode >= PICK_ANIMATION_FOLLOWER
        && requestCode <= PICK_ANIMATION_GIFT) {

    if (resultCode != RESULT_OK
            || data == null
            || data.getData() == null) {
        return;
    }

    Uri uri = data.getData();

    try {
        getContentResolver().takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
        );
    } catch (Exception ignored) {}

    String key;

    if (requestCode == PICK_ANIMATION_FOLLOWER) {
        key = "animation_follower_uri";
    } else if (requestCode == PICK_ANIMATION_DONATION) {
        key = "animation_donation_uri";
    } else {
        if (selectedGiftKey == null) {
            message("Сначала выбери подарок");
            return;
        }

        key = "animation_gift_" + selectedGiftKey;
    }

    prefs.edit()
            .putString(key, uri.toString())
            .apply();

    message("Анимация сохранена");

    return;
}
// Обработка звуков библиотеки
if (requestCode >= 504 && requestCode <= 507) {

    if (resultCode == RESULT_OK
            && data != null
            && data.getData() != null) {

        Uri uri = data.getData();

        String category;

        switch (requestCode) {
            case 504:
                category = "memes";
                break;
            case 505:
                category = "funny";
                break;
            case 506:
                category = "melodies";
                break;
            default:
                category = "effects";
                break;
        }

        try {
            getContentResolver().takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );
        } catch (Exception ignored) {}

        // Получаем настоящее имя файла
        String fileName = uri.getLastPathSegment();

        try {
            android.database.Cursor cursor =
                    getContentResolver().query(
                            uri,
                            null,
                            null,
                            null,
                            null
                    );

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(
                            android.provider.OpenableColumns.DISPLAY_NAME
                    );

                    if (nameIndex >= 0) {
                        fileName = cursor.getString(nameIndex);
                    }
                }

                cursor.close();
            }
        } catch (Exception ignored) {}

        if (fileName == null || fileName.trim().isEmpty()) {
            fileName = "Аудиозапись";
        }

        // Добавляем файл, не удаляя предыдущие
        JSONArray sounds = getLibrarySounds(category);

        JSONObject newSound = new JSONObject();

        try {
            newSound.put("name", fileName);
            newSound.put("uri", uri.toString());

            sounds.put(newSound);

            prefs.edit()
                    .putString(
                            getLibraryKey(category),
                            sounds.toString()
                    )
                    .apply();

            message("Добавлено: " + fileName);

        } catch (Exception e) {
            message("Не удалось сохранить запись");
        }

        showLibraryCategory(category);
    }

    return;
}
        if (requestCode == PICK_FOLLOW_SOUND) {
            if (resultCode == RESULT_OK
                    && data != null
                    && data.getData() != null) {

                Uri uri = data.getData();

                try {
                    getContentResolver().takePersistableUriPermission(
                            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (Exception ignored) {}

                prefs.edit()
                        .putString("follow_sound_uri", uri.toString())
                        .apply();

                message("Звук подписки сохранён");
            }
            return;
        }

        
if (requestCode == PICK_DONATION_SOUND) {
    if (resultCode == RESULT_OK
            && data != null
            && data.getData() != null) {

        Uri uri = data.getData();

        try {
            getContentResolver().takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (Exception ignored) {}

        prefs.edit()
                .putString("donationalerts_sound_uri", uri.toString())
                .apply();

        message("Звук доната DonationAlerts сохранён");
    }
    return;
}
        if (requestCode != PICK_SOUND
                || resultCode != RESULT_OK
                || data == null
                || data.getData() == null) {
            return;
        }

        if (selectedGiftKey == null) {
            message("Сначала выбери подарок");
            return;
        }

        Uri uri = data.getData();

        try {
            getContentResolver().takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (Exception ignored) {}

        prefs.edit()
                .putString("gift_sound_" + selectedGiftKey,
                        uri.toString())
                .apply();

        message("Звук назначен: " + selectedGiftName);
    }

    private void previewSavedSound() {
        if (selectedGiftKey == null) {
            message("Сначала выбери подарок");
            return;
        }

        String saved = prefs.getString(
                "gift_sound_" + selectedGiftKey, null);

        if (saved == null) {
            message("Сначала выбери звуковой файл");
            return;
        }

        stopPreview();

        try {
            previewPlayer = new MediaPlayer();
            previewPlayer.setDataSource(this, Uri.parse(saved));

            previewPlayer.setOnPreparedListener(player ->
                    player.start());

            previewPlayer.setOnCompletionListener(player ->
                    stopPreview());

            previewPlayer.setOnErrorListener((player, what, extra) -> {
                stopPreview();
                message("Не удалось воспроизвести файл");
                return true;
            });

            previewPlayer.prepareAsync();
        } catch (Exception e) {
            stopPreview();
            message("Ошибка воспроизведения");
        }
    }

    private void stopPreview() {
    if (previewPlayer != null) {
        try {
            previewPlayer.setOnPreparedListener(null);
            previewPlayer.setOnCompletionListener(null);
            previewPlayer.setOnErrorListener(null);

            if (previewPlayer.isPlaying()) {
                previewPlayer.stop();
            }

            previewPlayer.release();

        } catch (Exception ignored) {}

        previewPlayer = null;
    }

    playingLibrarySound = -1;
}

    
    private void showVoiceSettings() {
    String[] styles = new String[30];

    for (int i = 0; i < 30; i++) {
        styles[i] = String.format(
                Locale.US, "🎙️ Стиль %02d", i + 1);
    }

    final int[] pitches = {
        65, 70, 75, 80, 85, 90, 95, 100, 105, 110,
        115, 120, 125, 130, 135, 70, 80, 90, 100, 110,
        120, 130, 75, 95, 115, 125, 85, 105, 135, 140
    };

    final int[] rates = {
        80, 90, 100, 110, 120, 85, 95, 105, 115, 125,
        80, 90, 100, 110, 120, 125, 115, 105, 95, 85,
        80, 90, 100, 110, 120, 125, 115, 105, 90, 130
    };

    int saved = Math.max(0, Math.min(
            29, prefs.getInt("voice_preset", 7)));

    int savedVolume = Math.max(0, Math.min(
            100, prefs.getInt("voice_volume", 100)));

    final int[] choice = {saved};
    final int[] volume = {savedVolume};

    LinearLayout layout = new LinearLayout(this);
    layout.setOrientation(LinearLayout.VERTICAL);
    layout.setPadding(32, 16, 32, 8);

    ListView list = new ListView(this);

    ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_list_item_single_choice,
            styles);

    list.setAdapter(adapter);
    list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    list.setItemChecked(saved, true);

    list.setOnItemClickListener((parent, view, position, id) -> {
        choice[0] = position;
        list.setItemChecked(position, true);
    });

    TextView volumeLabel = new TextView(this);
    volumeLabel.setText("Громкость: " + savedVolume + "%");
    volumeLabel.setTextColor(Color.WHITE);
    volumeLabel.setTextSize(16);

    android.widget.SeekBar volumeBar =
            new android.widget.SeekBar(this);

    volumeBar.setMax(100);
    volumeBar.setProgress(savedVolume);

    volumeBar.setOnSeekBarChangeListener(
            new android.widget.SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(
                android.widget.SeekBar bar,
                int progress,
                boolean fromUser) {
            volume[0] = progress;
            volumeLabel.setText("Громкость: " + progress + "%");
        }

        @Override
        public void onStartTrackingTouch(
                android.widget.SeekBar bar) {
        }

        @Override
        public void onStopTrackingTouch(
                android.widget.SeekBar bar) {
        }
    });

    Button preview = new Button(this);
    preview.setText("▶ Прослушать выбранный стиль");

    preview.setOnClickListener(v -> {
        if (voicePreview == null || !voicePreviewReady) {
            message("Озвучка ещё не готова. Попробуй позже.");
            return;
        }

        int languageResult =
                voicePreview.setLanguage(new Locale("ru", "RU"));

        if (languageResult == TextToSpeech.LANG_MISSING_DATA
                || languageResult == TextToSpeech.LANG_NOT_SUPPORTED) {
            message("Установи русский голос в настройках Android.");
            return;
        }

        voicePreview.stop();

        voicePreview.setPitch(
                pitches[choice[0]] / 100.0f);

        voicePreview.setSpeechRate(
                rates[choice[0]] / 100.0f);

        Bundle params = new Bundle();
        params.putFloat(
                TextToSpeech.Engine.KEY_PARAM_VOLUME,
                volume[0] / 100.0f);

        voicePreview.speak(
                "Привет! Это проверка выбранного стиля голоса.",
                TextToSpeech.QUEUE_FLUSH,
                params,
                "voice_preview");
    });

    layout.addView(list,
            new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    420));

    layout.addView(volumeLabel);
    layout.addView(volumeBar);
    layout.addView(preview);

    new AlertDialog.Builder(this)
            .setTitle("🎙️ 30 стилей голоса")
            .setView(layout)
            .setPositiveButton("Сохранить", (dialog, which) -> {
                prefs.edit()
                        .putInt("voice_preset", choice[0])
                        .putInt("voice_volume", volume[0])
                        .putString("voice_style", "hero")
                        .apply();

                message("Стиль и громкость сохранены");
            })
            .setNegativeButton("Отмена", null)
            .show();
}
                                        

private void displayVoiceSettings() {              
        if (!voicePreviewReady || voicePreview == null) {
            message("Русская озвучка ещё не готова. Проверь настройки синтеза речи Android.");
            return;
        }

        ArrayList<String> labels = new ArrayList<>();
        ArrayList<String> names = new ArrayList<>();

        labels.add("Голос по умолчанию");
        names.add("");

        try {
            List<Voice> voices =
                    new ArrayList<>(voicePreview.getVoices());

            Collections.sort(voices,
                    Comparator.comparing(v ->
                            v.getName().toLowerCase(Locale.ROOT)));

            for (Voice voice : voices) {
                Locale locale = voice.getLocale();

                if (locale != null
                        && "ru".equalsIgnoreCase(
                                locale.getLanguage())) {
                    labels.add(voice.getName());
                    names.add(voice.getName());
                }
            }
        } catch (Exception ignored) {}

        if (labels.size() == 1) {
            message("Установи русский голос в настройках синтеза речи Android");
            return;
        }

        String selected =
                prefs.getString("selected_voice_name", "");

        int checked = names.indexOf(selected);
        if (checked < 0) checked = 0;

        final int[] choice = {checked};

        new AlertDialog.Builder(this)
                .setTitle("Выбери голос")
                .setSingleChoiceItems(
                        labels.toArray(new String[0]),
                        checked,
                        (dialog, which) -> choice[0] = which)
                .setNeutralButton("▶ Проверить", (dialog, which) -> {
                    String name = names.get(choice[0]);
                    applyPreviewVoice(name);
                    testVoice(name);
                })
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    String name = names.get(choice[0]);

                    prefs.edit()
                            .putString("selected_voice_name", name)
                            .apply();

                    applyPreviewVoice(name);
                    message("Голос сохранён");
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void applyPreviewVoice(String voiceName) {
        if (voicePreview == null || !voicePreviewReady) return;

        try {
            if (voiceName == null || voiceName.isEmpty()) {
                voicePreview.setLanguage(new Locale("ru", "RU"));
                return;
            }

            for (Voice voice : voicePreview.getVoices()) {
                if (voice.getName().equals(voiceName)) {
                    voicePreview.setVoice(voice);
                    return;
                }
            }
        } catch (Exception ignored) {}
    }

    private void testVoice(String voiceName) {
        if (voicePreview == null || !voicePreviewReady) {
            message("Озвучка ещё не готова");
            return;
        }

        applyPreviewVoice(voiceName);

        voicePreview.speak(
                "Привет! Это проверка голоса LiveGiftStudio.",
                TextToSpeech.QUEUE_FLUSH,
                null,
                "lgs_voice_test");
    }

    private void chooseFollowSound() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

        try {
            startActivityForResult(intent, PICK_FOLLOW_SOUND);
        } catch (Exception e) {
            message("Не удалось открыть выбор звука");
        }
    }
    private void chooseDonationAlertSound() {
    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
    intent.addCategory(Intent.CATEGORY_OPENABLE);
    intent.setType("audio/*");
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

    try {
        startActivityForResult(intent, PICK_DONATION_SOUND);
    } catch (Exception e) {
        message("Не удалось открыть выбор звука доната");
    }
}
private void chooseMediaFolder() {
    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

    try {
        startActivityForResult(intent, PICK_MEDIA_FOLDER);
    } catch (Exception e) {
        message("Не удалось открыть выбор папки");
    }
}

private void chooseMediaFiles() {
    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
    intent.addCategory(Intent.CATEGORY_OPENABLE);
    intent.setType("*/*");
    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

    try {
        startActivityForResult(intent, PICK_MEDIA_FILES);
    } catch (Exception e) {
        message("Не удалось открыть выбор файлов");
    }
}
    
private void importMediaFile(Uri uri) {
    if (uri == null) return;

    try {
        getContentResolver().takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
        );
    } catch (Exception ignored) {}

    String name = "media_file";
    String mime = getContentResolver().getType(uri);

    try (android.database.Cursor cursor =
            getContentResolver().query(
                    uri, null, null, null, null)) {
        if (cursor != null && cursor.moveToFirst()) {
            int index = cursor.getColumnIndex(
                    android.provider.OpenableColumns.DISPLAY_NAME);
            if (index >= 0) {
                name = cursor.getString(index);
            }
        }
    } catch (Exception ignored) {}

    copyMediaToApp(uri, name, mime);
}

private void importMediaFolder(Uri treeUri) {
    importMediaFolderContents(treeUri,
            android.provider.DocumentsContract.getTreeDocumentId(treeUri));
}

private void importMediaFolderContents(Uri treeUri, String parentId) {
    android.net.Uri childrenUri =
            android.provider.DocumentsContract.buildChildDocumentsUriUsingTree(
                    treeUri, parentId);

    try (android.database.Cursor cursor =
            getContentResolver().query(
                    childrenUri,
                    new String[] {
                            android.provider.DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                            android.provider.DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                            android.provider.DocumentsContract.Document.COLUMN_MIME_TYPE
                    },
                    null, null, null)) {

        if (cursor == null) return;

        while (cursor.moveToNext()) {
            String id = cursor.getString(0);
            String name = cursor.getString(1);
            String mime = cursor.getString(2);

            if (android.provider.DocumentsContract.Document.MIME_TYPE_DIR
                    .equals(mime)) {
                importMediaFolderContents(treeUri, id);
            } else {
                Uri fileUri =
                        android.provider.DocumentsContract.buildDocumentUriUsingTree(
                                treeUri, id);
                copyMediaToApp(fileUri, name, mime);
            }
        }
    } catch (Exception e) {
        message("Ошибка чтения папки: " + e.getMessage());
    }
}

private void copyMediaToApp(Uri uri, String name, String mime) {
    java.io.File directory =
            new java.io.File(getFilesDir(), "media_library");

    if (!directory.exists() && !directory.mkdirs()) {
        message("Не удалось создать хранилище");
        return;
    }

    String safeName = name == null ? "media_file" :
            name.replaceAll("[\\\\/:*?\"<>|]", "_");

    String storedName = System.currentTimeMillis()
            + "_" + safeName;

    java.io.File destination =
            new java.io.File(directory, storedName);

    try (java.io.InputStream input =
                 getContentResolver().openInputStream(uri);
         java.io.OutputStream output =
                 new java.io.FileOutputStream(destination)) {

        if (input == null) {
            message("Не удалось прочитать файл: " + safeName);
            return;
        }

        byte[] buffer = new byte[8192];
        int length;

        while ((length = input.read(buffer)) != -1) {
            output.write(buffer, 0, length);
        }

        JSONArray library;
        try {
            library = new JSONArray(
                    prefs.getString("media_library_items", "[]"));
        } catch (Exception e) {
            library = new JSONArray();
        }

        JSONObject item = new JSONObject();
        item.put("name", safeName);
        item.put("file", storedName);
        item.put("mime", mime == null ? "" : mime);

        library.put(item);

        prefs.edit()
                .putString("media_library_items", library.toString())
                .apply();

        message("Сохранено: " + safeName);

    } catch (Exception e) {
        if (destination.exists()) destination.delete();
        message("Ошибка сохранения: " + safeName);
    }
}

private void showMediaLibrary() {
    final JSONArray mediaLibrary;

    try {
        mediaLibrary = new JSONArray(
                prefs.getString("media_library_items", "[]"));
    } catch (Exception e) {
        message("Не удалось прочитать медиатеку.");
        return;
    }

    if (mediaLibrary.length() == 0) {
        new AlertDialog.Builder(this)
                .setTitle("📚 Моя медиатека")
                .setMessage("Пока нет файлов.\nЗагрузи папку или отдельные файлы.")
                .setPositiveButton("Загрузить папку",
                        (dialog, which) -> chooseMediaFolder())
                .setNeutralButton("Загрузить файлы",
                        (dialog, which) -> chooseMediaFiles())
                .setNegativeButton("Закрыть", null)
                .show();
        return;
    }

    String[] names = new String[mediaLibrary.length()];

    for (int i = 0; i < mediaLibrary.length(); i++) {
        JSONObject item = mediaLibrary.optJSONObject(i);
        String name = item == null ? "Файл" :
                item.optString("name", "Файл");
        String mime = item == null ? "" :
                item.optString("mime", "");

        String type = "Файл";
        if (mime.startsWith("audio/")) type = "🔊 Звук";
        else if (mime.startsWith("video/")) type = "🎬 Видео";
        else if (mime.equals("image/gif")) type = "✨ GIF";
        else if (mime.startsWith("image/")) type = "🖼️ Фото";

        names[i] = type + " — " + name;
    }

    new AlertDialog.Builder(this)
            .setTitle("📚 Моя медиатека")
            .setItems(names, (dialog, which) -> {
                final JSONObject item =
                        mediaLibrary.optJSONObject(which);
                if (item == null) return;

                String name = item.optString("name", "Файл");
                String mime = item.optString("mime", "");
                String storedName = item.optString("file", "");

                new AlertDialog.Builder(this)
        .setTitle(name)
        .setMessage("Тип: " + mime)
        .setView(createMediaPreview(mime, storedName))
                        
                        .setPositiveButton("Назначить", (d, w) -> {
                            
if (mime == null || mime.isEmpty()) {
    message("Не удалось определить тип файла.");
    return;
}
                            if (storedName.isEmpty()) {
                                message("Не найден файл в медиатеке.");
                                return;
                            }

                            File mediaDir = new File(
                                    getFilesDir(), "media_library");
                            File mediaFile = new File(mediaDir, storedName);

                            if (!mediaFile.exists()) {
                                message("Файл не найден.");
                                return;
                            }

                            String fileUri =
                                    Uri.fromFile(mediaFile).toString();

                            new AlertDialog.Builder(this)
                                    .setTitle(mime != null && mime.startsWith("audio/")
        ? "🔊 Куда назначить звук?"
        : "🎞️ Куда назначить анимацию?")
.setItems(mime != null && mime.startsWith("audio/")
        ? new String[]{
                "🔔 Подписка",
                "🎁 Подарок",
                "💰 Донат DonationAlerts"
        }
        : new String[]{
                "🔔 Подписка",
                "🎁 Подарок",
                "💰 Донат DonationAlerts"
        }, (targetDialog, choice) -> {
    boolean isAudio = mime != null && mime.startsWith("audio/");

    if (choice == 0) {
        prefs.edit()
                .putString(
                        isAudio ? "follow_sound_uri" : "animation_follower_uri",
                        fileUri)
                .apply();

        message(isAudio
                ? "Звук подписки назначен!"
                : "Анимация подписки назначена!");

    } else if (choice == 1) {
        String[] giftNames = new String[giftCatalog.length];

        for (int i = 0; i < giftCatalog.length; i++) {
            giftNames[i] = giftCatalog[i][1];
        }

        new AlertDialog.Builder(this)
                .setTitle("Выбери подарок")
                .setItems(giftNames, (gd, giftIndex) -> {
                    String giftKey = giftCatalog[giftIndex][0];

                    prefs.edit()
                            .putString(
                                    (isAudio ? "gift_sound_" : "animation_gift_")
                                            + giftKey,
                                    fileUri)
                            .apply();

                    message(isAudio
                            ? "Звук подарка назначен!"
                            : "Анимация подарка назначена!");
                })
                .setNegativeButton("Отмена", null)
                .show();

    } else {
        prefs.edit()
                .putString(
                        isAudio
                                ? "donationalerts_sound_uri"
                                : "animation_donation_uri",
                        fileUri)
                .apply();

        message(isAudio
                ? "Звук доната сохранён. Для автоматического запуска нужен обработчик DonationAlerts."
                : "Анимация доната назначена!");
    }
})
.setNegativeButton("Отмена", null)
.show();
})
                        .setNeutralButton("Загрузить ещё", (d, w) -> {
                            new AlertDialog.Builder(this)
                                    .setItems(
                                            new String[]{
                                                    "📁 Загрузить папку",
                                                    "📄 Загрузить файлы"
                                            },
                                            (dlg, choice) -> {
                                                if (choice == 0) {
                                                    chooseMediaFolder();
                                                } else {
                                                    chooseMediaFiles();
                                                }
                                            })
                                    .show();
                        })
                        .setNegativeButton("Назад",
                                (d, w) -> showMediaLibrary())
                        .show();
            })
            .setPositiveButton("📁 Папка",
                    (dialog, which) -> chooseMediaFolder())
            .setNeutralButton("📄 Файлы",
                    (dialog, which) -> chooseMediaFiles())
            .setNegativeButton("Закрыть", null)
            .show();
}

private android.view.View createMediaPreview(
        String mime, String storedName) {

    File mediaDir = new File(getFilesDir(), "media_library");
    File mediaFile = new File(mediaDir, storedName);

    android.widget.FrameLayout container =
            new android.widget.FrameLayout(this);

    container.setLayoutParams(
            new android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    500));

    if (!mediaFile.exists()) {
        android.widget.TextView error =
                new android.widget.TextView(this);
        error.setText("Файл не найден");
        error.setGravity(android.view.Gravity.CENTER);
        container.addView(error);
        return container;
    }

    Uri uri = Uri.fromFile(mediaFile);

    if (mime.startsWith("image/")) {
        android.widget.ImageView image =
                new android.widget.ImageView(this);

        image.setImageURI(uri);
        image.setScaleType(
                android.widget.ImageView.ScaleType.FIT_CENTER);

        container.addView(image,
                new android.widget.FrameLayout.LayoutParams(
                        -1, -1));

    } else if (mime.startsWith("video/")) {
        android.widget.VideoView video =
                new android.widget.VideoView(this);

        container.addView(video,
                new android.widget.FrameLayout.LayoutParams(
                        -1, -1));

        video.setVideoURI(uri);

        android.widget.MediaController controls =
                new android.widget.MediaController(this);
        controls.setAnchorView(video);
        video.setMediaController(controls);

    } else {
        android.widget.TextView info =
                new android.widget.TextView(this);

        info.setText(mime.startsWith("audio/")
                ? "🔊 Аудиофайл"
                : "Предпросмотр недоступен для этого типа файла");

        info.setGravity(android.view.Gravity.CENTER);

        container.addView(info,
                new android.widget.FrameLayout.LayoutParams(
                        -1, -1));
    }

    return container;
}
    private void startOverlay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !Settings.canDrawOverlays(this)) {

            new AlertDialog.Builder(this)
                    .setTitle("Разрешение")
                    .setMessage("Разреши приложению показывать анимацию "
                            + "поверх других приложений.")
                    .setNegativeButton("Отмена", null)
                    .setPositiveButton("Настройки", (d, w) -> {
                        Intent intent = new Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    })
                    .show();
            return;
        }

        try {
            
Intent intent = new Intent(this, OverlayService.class);
intent.putExtra("overlay_text", "🎁 ТЕСТ АНИМАЦИИ!");

if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    startForegroundService(intent);
} else {
    startService(intent);
}
            message("Команда анимации отправлена");
        } catch (Exception e) {
            message("Не удалось запустить анимацию");
        }
    }

private void testDonation() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
            && !Settings.canDrawOverlays(this)) {
        message("Разреши показ поверх других приложений");
        Intent settingsIntent = new Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName())
        );
        startActivity(settingsIntent);
        return;
    }

    Intent intent = new Intent(this, OverlayService.class);
    intent.putExtra("test_donation", "yes");

    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        message("Запущен тест доната 50 ₽");
    } catch (Exception e) {
        message("Не удалось запустить тест доната");
    }
}

    private void createOrder(String service, int amount) {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(16, 8, 16, 8);

        EditText nameInput = new EditText(this);
        nameInput.setHint("Твой ник в TikTok или Telegram");
        form.addView(nameInput);

        EditText detailsInput = new EditText(this);
        detailsInput.setHint("Описание заказа");
        detailsInput.setMinLines(2);
        form.addView(detailsInput);

        EditText priceInput = null;

        if (amount == 0) {
            priceInput = new EditText(this);
            priceInput.setHint("Цена в рублях");
            priceInput.setInputType(2);
            form.addView(priceInput);
        }

        final EditText finalPriceInput = priceInput;

        new AlertDialog.Builder(this)
                .setTitle("Заказ: " + service)
                .setMessage(amount == 0
                        ? "Опиши услугу"
                        : "Стоимость: " + amount + " ₽")
                .setView(form)
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Отправить", (d, w) -> {
                    String name = nameInput.getText().toString().trim();
                    String details = detailsInput.getText()
                            .toString().trim();

                    if (name.isEmpty() || details.isEmpty()) {
                        message("Укажи ник и описание заказа");
                        return;
                    }

                    int price = amount;

                    if (finalPriceInput != null) {
                        try {
                            price = Integer.parseInt(
                                    finalPriceInput.getText()
                                            .toString().trim());
                        } catch (Exception e) {
                            message("Укажи корректную цену");
                            return;
                        }

                        if (price < 0) {
                            message("Цена не может быть отрицательной");
                            return;
                        }
                    }

                    saveOrder(service, price, name, details);
                    sendOrderToTelegram(service, price, name, details);
                    openTelegram(makeOrderText(
                            service, price, name, details));
                })
                .show();
    }

    private String makeOrderText(
            String service, int price, String client, String details) {

        return "Новый заказ LiveGiftStudio\n\n"
                + "Услуга: " + service + "\n"
                + "Цена: " + price + " ₽\n"
                + "Клиент: " + client + "\n"
                + "Описание: " + details;
    }

    private void sendOrderToTelegram(
            String service, int price, String client, String details) {

        executor.execute(() -> {
            HttpURLConnection connection = null;

            try {
                JSONObject data = new JSONObject();
                data.put("key", APP_KEY);
                data.put("service", service);
                data.put("price", price);
                data.put("client", client);
                data.put("description", details);

                connection = (HttpURLConnection)
                        new URL(SERVER_URL).openConnection();

                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(20000);
                connection.setRequestProperty(
                        "Content-Type",
                        "application/json; charset=UTF-8");

                byte[] bytes = data.toString()
                        .getBytes(StandardCharsets.UTF_8);

                try (OutputStream output = connection.getOutputStream()) {
                    output.write(bytes);
                }

                int code = connection.getResponseCode();

                runOnUiThread(() -> {
                    if (code >= 200 && code < 300) {
                        message("Заказ отправлен на сервер");
                    } else {
                        message("Сервер вернул ошибку: " + code);
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        message("Не удалось отправить заказ на сервер"));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private void openTelegram(String text) {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, text);
            startActivity(Intent.createChooser(
                    intent, "Отправить заказ"));
        } catch (Exception e) {
            message("Не удалось открыть меню отправки");
        }
    }

    private void openLink(String link) {
        try {
            startActivity(new Intent(
                    Intent.ACTION_VIEW, Uri.parse(link)));
        } catch (Exception e) {
            message("Не удалось открыть ссылку");
        }
    }

    private void saveOrder(
            String service, int price, String client, String details) {

        try {
            JSONArray history = new JSONArray(
                    prefs.getString("orders_history", "[]"));

            JSONObject order = new JSONObject();
            order.put("service", service);
            order.put("price", price);
            order.put("client", client);
            order.put("details", details);
            order.put("date", System.currentTimeMillis());

            history.put(order);

            prefs.edit()
                    .putString("orders_history", history.toString())
                    .putInt("orders", prefs.getInt("orders", 0) + 1)
                    .apply();

            loadStats();
            updateStats();
        } catch (Exception e) {
            message("Не удалось сохранить заказ");
        }
    }

    private void showHistory() {
        JSONArray history;

        try {
            history = new JSONArray(
                    prefs.getString("orders_history", "[]"));
        } catch (Exception e) {
            history = new JSONArray();
        }

        StringBuilder text = new StringBuilder();

        for (int i = history.length() - 1; i >= 0; i--) {
            JSONObject item = history.optJSONObject(i);
            if (item == null) {
                continue;
            }

            text.append("Услуга: ")
                    .append(item.optString("service"))
                    .append("\nЦена: ")
                    .append(item.optInt("price"))
                    .append(" ₽\nКлиент: ")
                    .append(item.optString("client"))
                    .append("\nОписание: ")
                    .append(item.optString("details"))
                    .append("\n\n");
        }

        if (text.length() == 0) {
            text.append("Заказов пока нет.");
        }

        new AlertDialog.Builder(this)
                .setTitle("📋 История заказов")
                .setMessage(text.toString())
                .setPositiveButton("Закрыть", null)
                .show();
    }

    private void addDonation(String type) {
        EditText input = new EditText(this);
        input.setHint("Сумма в рублях");
        input.setInputType(2);

        new AlertDialog.Builder(this)
                .setTitle(type.equals("gift")
                        ? "Записать подарок" : "Записать донат")
                .setView(input)
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Сохранить", (d, w) -> {
                    try {
                        int amount = Integer.parseInt(
                                input.getText().toString().trim());

                        if (amount < 0) {
                            message("Сумма не может быть отрицательной");
                            return;
                        }

                        if (type.equals("gift")) {
                            gifts++;
                            prefs.edit().putInt("gifts", gifts).apply();
                        } else {
                            donations++;
                            donationIncome += amount;
                            prefs.edit()
                                    .putInt("donations", donations)
                                    .putInt("donationIncome", donationIncome)
                                    .apply();
                        }

                        updateStats();
                        message("Статистика обновлена");
                    } catch (Exception e) {
                        message("Введи сумму цифрами");
                    }
                })
                .show();
    }

    private void loadStats() {
        gifts = prefs.getInt("gifts", 0);
        donations = prefs.getInt("donations", 0);
        donationIncome = prefs.getInt("donationIncome", 0);
        orders = prefs.getInt("orders", 0);
    }

    private void updateStats() {
        if (statsText == null) {
            return;
        }

        statsText.setText(
                "📊 Твоя статистика\n\n"
                        + "🎁 Подарки: " + gifts + "\n"
                        + "💰 Донаты: " + donations + "\n"
                        + "💵 Сумма донатов: " + donationIncome + " ₽\n"
                        + "📋 Заказы: " + orders);
    }

    private void showStats() {
        loadStats();

        new AlertDialog.Builder(this)
                .setTitle("📊 Статистика")
                .setMessage(
                        "🎁 Подарки: " + gifts + "\n"
                                + "💰 Донаты: " + donations + "\n"
                                + "💵 Сумма донатов: "
                                + donationIncome + " ₽\n"
                                + "📋 Заказы: " + orders)
                .setPositiveButton("Закрыть", null)
                .show();
    }

    private void resetStats() {
        new AlertDialog.Builder(this)
                .setTitle("Сброс статистики")
                .setMessage("Обнулить подарки, донаты и заказы?")
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Сбросить", (d, w) -> {
                    gifts = 0;
                    donations = 0;
                    donationIncome = 0;
                    orders = 0;

                    prefs.edit()
                            .putInt("gifts", 0)
                            .putInt("donations", 0)
                            .putInt("donationIncome", 0)
                            .putInt("orders", 0)
                            .putString("orders_history", "[]")
                            .apply();

                    updateStats();
                    message("Статистика сброшена");
                })
                .show();
    }

    
    private void message(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    
private String[] getTikTokAccounts() {
    return new String[] {
        prefs.getString("tiktok_account_1", "vinnipyh99"),
        prefs.getString("tiktok_account_2", ""),
        prefs.getString("tiktok_account_3", "")
    };
}


private void showTikTokAccounts() {

    String[] accounts = getTikTokAccounts();
    String active = prefs.getString(
            "active_tiktok_account", "vinnipyh99");

    String[] labels = new String[accounts.length];

    for (int i = 0; i < accounts.length; i++) {
        String nick = accounts[i].isEmpty()
                ? "Не задан"
                : "@" + accounts[i];

        labels[i] = nick
                + (accounts[i].equalsIgnoreCase(active)
                ? "  ✓ АКТИВЕН"
                : "");
    }

    new AlertDialog.Builder(this)
            .setTitle("🎵 Выбери активный аккаунт")
            .setItems(labels, (dialog, which) -> {

                if (accounts[which].isEmpty()) {
                    editTikTokAccount(which);
                    return;
                }

                new AlertDialog.Builder(this)
                        .setTitle("Аккаунт @" + accounts[which])
                        .setItems(
                                new String[]{
                                        "✅ Сделать активным",
                                        "✏️ Изменить ник",
                                        "🔎 Проверить подключение"
                                },
                                (d, action) -> {

                                    if (action == 0) {
                                        prefs.edit()
                                                .putString(
                                                        "active_tiktok_account",
                                                        accounts[which])
                                                .putString(
                                                        "tiktok_username",
                                                        accounts[which])
                                                .apply();

                                        message("Переключаю сервер на @"
                                                + accounts[which]);

                                        switchServerAccount(accounts[which]);

                                    } else if (action == 1) {
                                        editTikTokAccount(which);

                                    } else if (action == 2) {
                                        checkServerConnection(
                                                accounts[which]);
                                    }
                                }
                        )
                        .setNegativeButton("Закрыть", null)
                        .show();
            })
            .setNegativeButton("Закрыть", null)
            .show();
}                
private void editTikTokAccount(int index) {
    EditText input = new EditText(this);
    input.setSingleLine(true);
    input.setHint("Ник без @");
    input.setText(getTikTokAccounts()[index]);

    new AlertDialog.Builder(this)
            .setTitle("Аккаунт " + (index + 1))
            .setView(input)
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Сохранить", (dialog, which) -> {
                String username = input.getText().toString()
                        .trim().replace("@", "");

                if (username.isEmpty()) {
                    message("Введи ник TikTok");
                    return;
                }

                prefs.edit()
                        .putString("tiktok_account_" + (index + 1), username)
                        .apply();

                message("Аккаунт @" + username + " сохранён");
                showTikTokAccounts();
            })
            .show();
}
   private static final String ACCOUNT_SWITCH_KEY =4upcKlUY0OvZBQvndYby9pQE7cHVaB
private void switchServerAccount(String username) {
    new Thread(() -> {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(
                    "https://livegiftstudio.onrender.com/switch-account"
            );

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);
            connection.setDoOutput(true);
            connection.setRequestProperty(
                    "Content-Type", "application/json; charset=UTF-8"
            );

            String key = ACCOUNT_SWITCH_KEY;
connection.setRequestProperty("x-switch-key", key);
}
            JSONObject json = new JSONObject();
            json.put("username", username);

            try (OutputStream output = connection.getOutputStream()) {
                output.write(json.toString().getBytes(
                        StandardCharsets.UTF_8
                ));
            }

            int status = connection.getResponseCode();

            runOnUiThread(() -> {
                if (status >= 200 && status < 300) {
                    message("Сервер переключён на @" + username);
                } else {
                    message("Сервер не переключился. Код: " + status);
                }
            });

        } catch (Exception e) {
            runOnUiThread(() ->
                    message("Ошибка переключения: " + e.getMessage())
            );
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }).start();
}
    private void showTikTokConnection() {
        EditText input = new EditText(this);
        input.setHint("Например: @username");
        input.setSingleLine(true);
        input.setInputType(1);
        input.setText(prefs.getString("tiktok_username", "vinnipyh99"));

        new AlertDialog.Builder(this)
                .setTitle("📱 Подключение TikTok LIVE")
                .setMessage("Введи ник TikTok без пароля. Сервер должен быть настроен на этот же ник в Render.")
                .setView(input)
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Сохранить и подключить", (dialog, which) -> {
                    String username = input.getText()
                            .toString()
                            .trim()
                            .replace("@", "");

                    if (username.isEmpty()) {
                        message("Сначала введи ник TikTok");
                        return;
                    }

                    prefs.edit()
                            .putString("tiktok_username", username)
                            .apply();

                    try {
                        Intent serviceIntent =
        new Intent(MainActivity.this, OverlayService.class);

if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    startForegroundService(serviceIntent);
} else {
    startService(serviceIntent);
}
                    } catch (Exception e) {
                        message("Ник сохранён, но сервис не запустился");
                        return;
                    }

                    message("Проверяю сервер для @" + username);
                    checkServerConnection(username);
                })
                .show();
    }

    private void checkServerConnection(String username) {
        executor.execute(() -> {
            HttpURLConnection connection = null;

            try {
                connection = (HttpURLConnection)
                        new URL("https://livegiftstudio.onrender.com/")
                                .openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);

                int code = connection.getResponseCode();

                if (code < 200 || code >= 300) {
                    runOnUiThread(() ->
                            message("Ошибка сервера: " + code));
                    return;
                }

                StringBuilder body = new StringBuilder();

                try (java.io.BufferedReader reader =
                             new java.io.BufferedReader(
                                     new java.io.InputStreamReader(
                                             connection.getInputStream(),
                                             StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        body.append(line);
                    }
                }

                JSONObject status = new JSONObject(body.toString());
                String serverUsername = status.optString("tiktok", "");
                boolean enabled = status.optBoolean("tiktokEnabled", false);

                runOnUiThread(() -> {
                    if (!serverUsername.equalsIgnoreCase(username)) {
                        message("В Render указан другой ник: @" + serverUsername
                                + ". Проверь TIKTOK_USERNAME.");
                    } else if (!enabled) {
                        message("Сервер работает, но включи TIKTOK_ENABLED=true в Render.");
                    } else {
                        message("Сервер настроен на @" + username
                                + ". Теперь проверь подключение во время эфира.");
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        message("Не удалось проверить сервер. Проверь интернет."));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }
@Override
protected void onStart() {
    super.onStart();

    Intent startSpeechIntent =
            new Intent(MainActivity.this, OverlayService.class);

    startSpeechIntent.setAction(
            "com.livegiftstudio.START_CHAT_SPEECH"
    );

    startService(startSpeechIntent);
}
@Override
protected void onStop() {
    // При уходе с экрана или сворачивании приложения
    // предварительное прослушивание прекращается
    stopPreview();

    if (voicePreview != null) {
        try {
            voicePreview.stop();
        } catch (Exception ignored) {}
    }

        if (voicePreview != null) {
        try {
            voicePreview.stop();
        } catch (Exception ignored) {}
    }

    Intent stopSpeechIntent =
            new Intent(MainActivity.this, OverlayService.class);

    stopSpeechIntent.setAction(
            "com.livegiftstudio.STOP_CHAT_SPEECH"
    );

    startService(stopSpeechIntent);

    super.onStop();
}
    
    @Override
    protected void onDestroy() {
        stopPreview();

        if (voicePreview != null) {
            try {
                voicePreview.stop();
                voicePreview.shutdown();
            } catch (Exception ignored) {}

            voicePreview = null;
        }

        executor.shutdown();
        super.onDestroy();
    }
private int darken(int color) {
    return Color.rgb(
            (int) (Color.red(color) * 0.55f),
            (int) (Color.green(color) * 0.55f),
            (int) (Color.blue(color) * 0.55f)
    );
}

private int lighten(int color) {
    return Color.rgb(
            Math.min(255, Color.red(color) + 60),
            Math.min(255, Color.green(color) + 60),
            Math.min(255, Color.blue(color) + 60)
    );
}
}
