
package com.livegiftstudio;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
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
import android.graphics.Typeface;
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

    private static final String APP_KEY = BuildConfig.ORDERS_KEY;

    private static final int PICK_SOUND = 501;
    private static final int PICK_FOLLOW_SOUND = 502;
    private static final int PICK_DONATION_SOUND = 503;
    private static final int PICK_MEDIA_FOLDER = 508;
private static final int PICK_MEDIA_FILES = 509;
    private static final int PICK_SOUNDS_FOLDER = 530;
    private static final int PICK_GIFT_ICONS_FOLDER = 531;
private static final int PICK_ANIMATION_FOLLOWER = 601;
private static final int PICK_ANIMATION_DONATION = 602;
private static final int PICK_ANIMATION_GIFT = 603;
private String selectedAnimationType = "follower";
    private String lastImportedFileName;
    private int autoAssignedCount;
    private TextToSpeech voicePreview;
    private boolean voicePreviewReady = false;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private SharedPreferences prefs;
    private LinearLayout container;
    private TextView statsText;
    private MediaPlayer previewPlayer;
    private MediaPlayer sfxPreview;
// Категория, в которую сейчас добавляем звук
private String libraryCategory;

// Индекс записи, которая сейчас играет
private int playingLibrarySound = -1;
private String playingLibraryCategory;

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
    private int giftIncome;
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
    {"slowmo", "🐢 Замедление"},
    {"gg", "🎮 GG"},
    {"hand_hearts", "🫶 Ручные сердечки"},
    {"ice_cream", "🍨 Мороженое"},
    {"heart_me", "💗 Heart Me"},
    {"little_crown", "👑 Коронка"},
    {"money_gun", "💸 Денежный пистолет"},
    {"sports_car", "🏎️ Спорткар"},
    {"plane", "✈️ Самолёт"},
    {"yacht", "🛥️ Яхта"},
    {"castle", "🏰 Замок"},
    {"unicorn", "🦄 Единорог"},
    {"lion", "🦁 Лев"},
    {"galaxy", "🌌 Галактика"},
    {"dragon_flame", "🐉 Дракон"},
    {"phoenix", "🔥 Феникс"},
    {"rocket", "🚀 Ракета"},
    {"planet", "🪐 Планета"},
    {"star", "⭐ Звезда"},
    {"lightning", "⚡ Молния"},
    {"football", "⚽ Футбол"},
    {"basketball", "🏀 Баскетбол"},
    {"cake", "🍰 Торт"},
    {"birthday", "🎂 День рождения"},
    {"balloon", "🎈 Шарик"},
    {"bouquet", "💐 Букет"},
    {"microphone", "🎙️ Микрофон"},
    {"guitar", "🎸 Гитара"},
    {"drum", "🥁 Барабан"},
    {"motorcycle", "🏍️ Мотоцикл"},
    {"train", "🚂 Поезд"},
    {"airplane", "🛩️ Самолётик"},
    {"house", "🏠 Дом"},
    {"ring", "💍 Кольцо"},
    {"watch", "⌚ Часы"},
    {"money", "💰 Деньги"},
    {"gold", "🪙 Золото"},
    {"fire", "🔥 Огонь"},
    {"rainbow", "🌈 Радуга"},
    {"flower", "🌸 Цветок"},
    {"cat", "🐱 Кот"},
    {"dog", "🐶 Собака"},
    {"rabbit", "🐰 Кролик"},
    {"panda", "🐼 Панда"},
    {"champagne", "🍾 Шампанское"},
    {"trophy", "🏆 Кубок"},
    {"medal", "🏅 Медаль"},
    {"love", "❤️ Любовь"},
    {"music", "🎵 Музыка"},
    {"cap", "🧢 Кепка"},
    {"sunglasses", "🕶️ Очки"},
    {"swan", "🦢 Лебедь"},
    {"concert", "🎤 Концерт"},
    {"paper_crane", "🕊️ Журавлик"},
    {"heart", "💖 Сердце"},
    {"coffee", "☕ Кофе"},
    {"diamond", "💎 Алмаз"},
    {"crown", "👑 Корона"},
    {"gift", "🎁 Подарок"},
    {"letter", "💌 Письмо"},
    {"chocolate", "🍫 Шоколад"},
    {"candy", "🍬 Конфета"},
    {"lollipop", "🍭 Леденец"},
    {"popcorn", "🍿 Попкорн"},
    {"burger", "🍔 Бургер"},
    {"pizza", "🍕 Пицца"},
    {"sushi", "🍣 Суши"},
    {"beer", "🍺 Пиво"},
    {"cocktail", "🍸 Коктейль"},
    {"watermelon", "🍉 Арбуз"},
    {"strawberry", "🍓 Клубника"},
    {"cherry", "🍒 Черешня"},
    {"grape", "🍇 Виноград"},
    {"banana", "🍌 Банан"},
    {"apple", "🍎 Яблоко"},
    {"peach", "🍑 Персик"}
};

    // Встроенный набор звуков для подарков (файлы в res/raw).
    private static final String[] RECOMMENDED_SFX = {
            "sfx_coin", "sfx_tada", "sfx_sparkle", "sfx_applause",
            "sfx_boing", "sfx_pop", "sfx_whoosh", "sfx_drum",
            "sfx_boom", "sfx_buzzer", "sfx_trombone", "sfx_duck",
            "sfx_laser", "sfx_8bit", "sfx_cash", "sfx_drumroll",
            "sfx_cheer", "sfx_magic", "sfx_robot", "sfx_slide",
            "sfx_cat", "sfx_laugh"
    };

    private static final String[] RECOMMENDED_SFX_NAMES = {
            "💰 Монетка", "🎺 Фанфары", "✨ Искорки", "👏 Аплодисменты",
            "🌀 Пружинка", "🫧 Поп", "💨 Вжух", "🥁 Барабан",
            "💥 Бум", "❌ Ошибка", "🎷 Грустный тромбон", "🦆 Утка",
            "🔫 Лазер", "🎮 8-бит", "🧾 Касса", "🥁 Дробь",
            "🙌 Овации", "🪄 Магия", "🤖 Робот", "🎶 Свисток",
            "🐱 Кот", "😂 Смех"
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
        requestNotificationPermission();
        autoImportFromPhoneFolder();
    }

    // Android 13+ не показывает уведомление foreground-сервиса без этого
    // разрешения, и тогда сервис не остановить из шторки.
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < 33) {
            return;
        }

        if (checkSelfPermission(
                android.Manifest.permission.POST_NOTIFICATIONS)
                == android.content.pm.PackageManager
                        .PERMISSION_GRANTED) {
            return;
        }

        requestPermissions(
                new String[]{
                        android.Manifest.permission.POST_NOTIFICATIONS
                },
                900);
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

    // Шапка приложения
    LinearLayout hero = new LinearLayout(this);
    hero.setOrientation(LinearLayout.VERTICAL);
    hero.setGravity(Gravity.CENTER);
    hero.setPadding(dp(18), dp(20), dp(18), dp(18));

    GradientDrawable heroBackground = new GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            new int[]{0xFF2A0B45, 0xFF1A0F3A, 0xFF071026});
    heroBackground.setCornerRadius(dp(26));
    heroBackground.setStroke(dp(2), 0xFFFF1493);
    hero.setBackground(heroBackground);
    hero.setElevation(dp(8));

    LinearLayout.LayoutParams heroParams =
            new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
    heroParams.setMargins(0, dp(4), 0, dp(6));
    container.addView(hero, heroParams);

    TextView title = new TextView(this);
    title.setText("LiveGiftStudio");
    title.setTextSize(31);
    title.setTypeface(null, android.graphics.Typeface.BOLD);
    title.setTextColor(Color.WHITE);
    title.setGravity(Gravity.CENTER);
    title.setShadowLayer(dp(14), 0, 0, 0xFFFF1493);
    hero.addView(title);

    TextView subtitle = new TextView(this);
    subtitle.setText("PRO · инструменты для TikTok LIVE");
    subtitle.setTextSize(13);
    subtitle.setTextColor(Color.rgb(190, 200, 240));
    subtitle.setGravity(Gravity.CENTER);
    subtitle.setPadding(0, dp(4), 0, dp(10));
    hero.addView(subtitle);

    TextView heroChips = new TextView(this);
    heroChips.setText("🎁 подарки   🔊 звуки   ✨ анимации");
    heroChips.setTextSize(12);
    heroChips.setTextColor(0xFF8FE3FF);
    heroChips.setGravity(Gravity.CENTER);
    hero.addView(heroChips);

    addSectionHeader("🎁 ПОДАРКИ И ЗВУКИ");

    addMenuRow(
        "🎁\nПодарки\nи мои звуки", 0xFFFF1493,
        "🎙️\nНастроить\nголос и озвучку", 0xFF008CFF,
        v -> showGiftCatalog(),
        v -> showVoiceSettings()
    );

    addSectionHeader("🔔 ПОДПИСКИ И ДОНАТЫ");

    addMenuRow(
        "🔔\nВыбрать\nзвук подписки", 0xFFFFB900,
        "🎬\nТест\nоверлея", 0xFF9B20FF,
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
        "🔑\nТокен\nDonationAlerts", 0xFF00D6B7,
        "📋\nКак\nполучить", 0xFF087BFF,
        v -> showDaTokenDialog(),
        v -> showDaTokenHelp()
);
addSectionHeader("📚 ЗВУКИ И МЕДИА");

addMenuRow(
        "🔊\nБиблиотека\nзвуков", 0xFF00C8FF,
        "🎵\nМои\nаудиофайлы", 0xFF00C853,
        v -> showSoundLibrary(),
        v -> showSoundLibrary()
);

addMenuRow(
        "📥\nИмпорт\nпапки звуков", 0xFF00D6B7,
        "🎞️\nИмпорт\nпапки GIF", 0xFFFFB900,
        v -> chooseSoundsFolder(),
        v -> chooseMediaFolder()
);

addMenuRow(
        "🔄\nОбновить\nиз LGS", 0xFF00C853,
        "🎯\nРазложить\nпо названиям", 0xFFFFB900,
        v -> autoImportFromPhoneFolder(),
        v -> distributeSoundsByNames()
);

addMenuRow(
        "🖼️\nИконки\nподарков", 0xFFFF1493,
        "🎁\nВсе\nподарки", 0xFF087BFF,
        v -> chooseGiftIconsFolder(),
        v -> showGiftCatalog()
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

    addSectionHeader("🎥 ЭФИР");

    addMenuRow(
        "🎵\nПодключение\nTikTok LIVE", 0xFF087BFF,
        "💰\nОткрыть\nдонаты", 0xFF00D6B7,       
v -> {
    message("Нажатие на кнопку TikTok LIVE");
    showTikTokAccounts();
},
        v -> openLink(DONATE_URL)
    );

    addSectionHeader("🛠 УСЛУГИ");

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

    addSectionHeader("📊 УЧЁТ");

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
    addSectionHeader("⚙️ НАСТРОЙКИ");

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
    new AlertDialog.Builder(this)
            .setTitle("Откуда взять анимацию?")
            .setItems(new String[]{
                    "📚 Из медиатеки",
                    "📁 Выбрать файл на телефоне"
            }, (dialog, which) -> {
                if (which == 0) {
                    showMediaLibraryPicker(requestCode);
                } else {
                    openAnimationFilePicker(requestCode);
                }
            })
            .setNegativeButton("Отмена", null)
            .show();
}

private void showMediaLibraryPicker(int requestCode) {
    final ArrayList<String> labels = new ArrayList<>();
    final ArrayList<String> specs = new ArrayList<>();

    JSONArray items;

    try {
        items = new JSONArray(
                prefs.getString("media_library_items", "[]"));
    } catch (Exception e) {
        items = new JSONArray();
    }

    File directory = new File(getFilesDir(), "media_library");

    for (int i = 0; i < items.length(); i++) {
        JSONObject item = items.optJSONObject(i);

        if (item == null) continue;

        String stored = item.optString("file", "");
        String mime = item.optString("mime", "");

        if (stored.isEmpty()) continue;

        if (!mime.startsWith("image/") && !mime.startsWith("video/")) {
            continue;
        }

        labels.add(item.optString("name", "Файл"));
        specs.add(Uri.fromFile(
                new File(directory, stored)).toString());
    }

    if (labels.isEmpty()) {
        new AlertDialog.Builder(this)
                .setTitle("📚 Медиатека пуста")
                .setMessage("Импортируй папку с GIF: "
                        + "меню → «Импорт папки GIF».")
                .setPositiveButton("📥 Импорт папки",
                        (d, w) -> chooseMediaFolder())
                .setNegativeButton("Закрыть", null)
                .show();
        return;
    }

    LinearLayout layout = new LinearLayout(this);
    layout.setOrientation(LinearLayout.VERTICAL);
    layout.setPadding(dp(12), dp(8), dp(12), dp(8));

    EditText search = new EditText(this);
    search.setHint("Поиск GIF: кот, смех, танец…");
    search.setSingleLine(true);
    layout.addView(search);

    final ArrayList<String> filteredLabels = new ArrayList<>();
    final ArrayList<String> filteredSpecs = new ArrayList<>();

    filteredLabels.addAll(labels);
    filteredSpecs.addAll(specs);

    final ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_list_item_1,
            filteredLabels);

    ListView list = new ListView(this);
    list.setAdapter(adapter);
    layout.addView(list, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(380)));

    final AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("🎞️ Анимация из медиатеки")
            .setView(layout)
            .setNegativeButton("Закрыть", null)
            .create();

    search.addTextChangedListener(new TextWatcher() {
        @Override
        public void beforeTextChanged(
                CharSequence text, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(
                CharSequence text, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            String query = editable.toString()
                    .trim()
                    .toLowerCase(Locale.ROOT);

            filteredLabels.clear();
            filteredSpecs.clear();

            for (int i = 0; i < labels.size(); i++) {
                if (query.isEmpty()
                        || labels.get(i)
                                .toLowerCase(Locale.ROOT)
                                .contains(query)) {
                    filteredLabels.add(labels.get(i));
                    filteredSpecs.add(specs.get(i));
                }
            }

            adapter.notifyDataSetChanged();
        }
    });

    list.setOnItemClickListener((parent, view, position, id) -> {
        if (position < 0 || position >= filteredSpecs.size()) {
            return;
        }

        dialog.dismiss();
        applyAnimationFromLibrary(
                requestCode, filteredSpecs.get(position));
    });

    dialog.show();
}

private void applyAnimationFromLibrary(int requestCode, String uriString) {
    String key;
    String type;

    if (requestCode == PICK_ANIMATION_FOLLOWER) {
        key = "animation_follower_uri";
        type = "follower";
    } else if (requestCode == PICK_ANIMATION_DONATION) {
        key = "animation_donation_uri";
        type = "donation";
    } else {
        if (selectedGiftKey == null) {
            message("Сначала выбери подарок");
            return;
        }

        key = "animation_gift_" + selectedGiftKey;
        type = "gift";
    }

    prefs.edit()
            .putString(key, uriString)
            .apply();

    message("Анимация сохранена");

    Intent intent = new Intent(this, OverlayService.class);
    intent.putExtra("preview_animation", type);
    intent.putExtra("preview_animation_uri", uriString);

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
    } catch (Exception ignored) {
    }
}

private void openAnimationFilePicker(int requestCode) {
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
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
            && !Settings.canDrawOverlays(this)) {
        message("Разреши показ поверх других приложений");
        startActivity(new Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName())));
        return;
    }

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
    ArrayList<String[]> gifts = allGifts();
    String[] names = new String[gifts.size()];

    for (int i = 0; i < gifts.size(); i++) {
        names[i] = gifts.get(i)[1];
    }

    new AlertDialog.Builder(this)
            .setTitle("🎁 Выбери подарок")
            .setItems(names, (dialog, which) -> {
                selectedGiftKey = gifts.get(which)[0];
                selectedGiftName = gifts.get(which)[1];
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
                                        testAnimation("gift");
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
            "✏️ Изменить надпись кнопки",
            "🔤 Шрифт текста в оверлее",
            "🔠 Размер текста в оверлее"
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
                    case 5:
                        showFontSettings();
                        break;
                    case 6:
                        chooseOverlayTextSize();
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
    // Порядок строго совпадает с порядком addMenuRow в buildInterface:
    // индекс = custom_label_N у кнопки.
    String[] buttons = {
            "Подарки и мои звуки",
            "Настроить голос и озвучку",
            "Выбрать звук подписки",
            "Тест оверлея",
            "Проверить подписку",
            "Назначить звук доната",
            "Тест доната 50 ₽",
            "Открыть донаты",
            "Библиотека звуков",
            "Мои аудиофайлы",
            "Моя медиатека",
            "Загрузить медиафайлы",
            "Подключение TikTok LIVE",
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
            "LiveGiftStudio PRO",
            "Настроить анимации",
            "Проверить анимацию",
            "Настройки оформления",
            "Сбросить оформление"
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

    // Кнопок в меню 28 (14 строк по 2), а не 18.
    for (int i = 0; i < 28; i++) {
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


private void addSectionHeader(String text) {
    TextView header = new TextView(this);
    header.setText(text);
    header.setTextSize(12);
    header.setTypeface(null, android.graphics.Typeface.BOLD);
    header.setTextColor(0xFF7C8BB8);
    header.setLetterSpacing(0.12f);
    header.setPadding(dp(6), dp(16), dp(6), dp(8));
    container.addView(header);
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
        } else {
            // Не пропускаем битую запись, иначе индексы в списке
            // разъедутся с индексами в JSON.
            names.add("⚠️ Повреждённая запись");
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

    // Если этот же файл уже стоит на паузе — продолжить.
    // Категорию сравниваем тоже: индекс 0 есть в каждой категории.
    if (previewPlayer != null
            && playingLibrarySound == index
            && category.equals(playingLibraryCategory)) {
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
        playingLibraryCategory = category;

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
        layout.setPadding(dp(14), dp(10), dp(14), dp(4));

        TextView hint = new TextView(this);
        hint.setText("Нажми на подарок — выберешь ему звук");
        hint.setTextColor(Color.rgb(170, 185, 220));
        hint.setTextSize(13);
        layout.addView(hint);

        EditText search = new EditText(this);
        search.setHint("🔍 Найти подарок...");
        search.setSingleLine(true);
        layout.addView(search);

        ScrollView scroll = new ScrollView(this);
        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(grid);

        layout.addView(scroll, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(430)));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("🎁 Подарки и звуки")
                .setView(layout)
                .setNegativeButton("Закрыть", null)
                .create();

        renderGiftGrid(grid, "", dialog);

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {
                renderGiftGrid(grid, s.toString(), dialog);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        dialog.show();
    }

    private void renderGiftGrid(
            LinearLayout grid, String query, AlertDialog dialog) {
        grid.removeAllViews();

        String q = query.toLowerCase(Locale.ROOT).trim();
        LinearLayout row = null;
        int shown = 0;

        for (String[] gift : allGifts()) {
            String key = gift[0];
            String name = gift[1];

            if (!q.isEmpty()
                    && !name.toLowerCase(Locale.ROOT).contains(q)
                    && !key.toLowerCase(Locale.ROOT).contains(q)) {
                continue;
            }

            if (shown % 2 == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                grid.addView(row, new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
            }

            LinearLayout.LayoutParams cardParams =
                    new LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1f);
            cardParams.setMargins(dp(4), dp(4), dp(4), dp(4));

            row.addView(createGiftCard(key, name, dialog), cardParams);

            shown++;
        }

        if (shown == 0) {
            TextView empty = new TextView(this);
            empty.setText("Ничего не найдено");
            empty.setTextColor(Color.WHITE);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, dp(28), 0, dp(28));
            grid.addView(empty);
        }
    }

    private View createGiftCard(
            String key, String name, AlertDialog dialog) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(dp(8), dp(12), dp(8), dp(12));

        GradientDrawable background = new GradientDrawable();
        background.setColor(0xFF141B33);
        background.setCornerRadius(dp(16));
        background.setStroke(dp(1), 0xFF2A3766);
        card.setBackground(background);

        ImageView icon = new ImageView(this);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);

        android.graphics.Bitmap customIcon = loadGiftIcon(key);

        if (customIcon != null) {
            icon.setImageBitmap(customIcon);
        } else {
            int imageId = getResources().getIdentifier(
                    "gift_" + key, "drawable", getPackageName());

            if (imageId == 0) {
                imageId = getResources().getIdentifier(
                        "gift_default", "drawable", getPackageName());
            }

            if (imageId != 0) {
                icon.setImageResource(imageId);
            }
        }

        if (icon.getDrawable() != null) {
            card.addView(icon, new LinearLayout.LayoutParams(
                    dp(52), dp(52)));
        }

        TextView title = new TextView(this);
        title.setText(name);
        title.setTextColor(Color.WHITE);
        title.setTextSize(14);
        title.setGravity(Gravity.CENTER);
        title.setMaxLines(2);
        card.addView(title);

        TextView sound = new TextView(this);
        sound.setText(soundSpecLabel(
                prefs.getString("gift_sound_" + key, null)));
        sound.setTextSize(11);
        sound.setTextColor(0xFF8FE3FF);
        sound.setGravity(Gravity.CENTER);
        sound.setPadding(0, dp(6), 0, 0);
        card.addView(sound);

        // Клик закрывает каталог: после выбора звука он откроется
        // заново, уже с новой подписью на карточке.
        card.setOnClickListener(v -> {
            dialog.dismiss();
            showGiftSoundPicker(key, name);
        });

        return card;
    }

    private String soundSpecLabel(String spec) {
        if (spec == null || spec.trim().isEmpty()) {
            return "🔊 стандартный";
        }

        if ("none".equals(spec)) {
            return "🔇 без звука";
        }

        if (spec.startsWith("raw:")) {
            String id = spec.substring(4);

            for (int i = 0; i < RECOMMENDED_SFX.length; i++) {
                if (RECOMMENDED_SFX[i].equals(id)) {
                    return RECOMMENDED_SFX_NAMES[i];
                }
            }

            return "🔥 звук";
        }

        return "🎵 свой файл";
    }

    private void showGiftSoundPicker(String giftKey, String giftName) {
        String current = prefs.getString(
                "gift_sound_" + giftKey, null);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(12), dp(8), dp(12), dp(4));

        ScrollView scroll = new ScrollView(this);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list);

        layout.addView(scroll, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(430)));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Звук для «" + giftName + "»")
                .setView(layout)
                .setNeutralButton("📄 Свой файл", (d, w) -> {
                    selectedGiftKey = giftKey;
                    selectedGiftName = giftName;
                    chooseSound();
                })
                .setNegativeButton("Закрыть", null)
                .create();

        addSoundRow(list, "🔊 Стандартный звук", null,
                current, giftKey, dialog);

        addSoundRow(list, "🔇 Без звука", "none",
                current, giftKey, dialog);

        addSoundHeader(list, "🔥 Рекомендуемые");

        for (int i = 0; i < RECOMMENDED_SFX.length; i++) {
            addSoundRow(list, RECOMMENDED_SFX_NAMES[i],
                    "raw:" + RECOMMENDED_SFX[i],
                    current, giftKey, dialog);
        }

        final int libraryCount = countLibrarySounds();

        addSoundHeader(list, libraryCount > 0
                ? "📚 Мои звуки (" + libraryCount + ")"
                : "📚 Мои звуки");

        TextView browse = new TextView(this);
        browse.setText(libraryCount > 0
                ? "🔎 Выбрать из моих звуков"
                : "📥 Импортировать папку со звуками");
        browse.setTextColor(0xFF8FE3FF);
        browse.setTextSize(16);
        browse.setPadding(dp(10), dp(12), dp(10), dp(12));

        GradientDrawable browseBackground = new GradientDrawable();
        browseBackground.setColor(0xFF0B1A2A);
        browseBackground.setCornerRadius(dp(10));
        browse.setBackground(browseBackground);

        browse.setOnClickListener(v -> {
            dialog.dismiss();

            if (libraryCount > 0) {
                showLibrarySoundSearch(giftKey, giftName);
            } else {
                chooseSoundsFolder();
            }
        });

        list.addView(browse);

        dialog.show();
    }

    private void showLibrarySoundSearch(String giftKey, String giftName) {
        final ArrayList<String> labels = new ArrayList<>();
        final ArrayList<String> specs = new ArrayList<>();

        String[] categories = {"memes", "funny", "melodies", "effects"};
        String[] categoryNames = {
                "😂 Мемы", "🤣 Фразы", "🎵 Мелодии", "💥 Эффекты"
        };

        for (int c = 0; c < categories.length; c++) {
            JSONArray sounds = getLibrarySounds(categories[c]);

            for (int i = 0; i < sounds.length(); i++) {
                JSONObject sound = sounds.optJSONObject(i);

                if (sound == null) continue;

                String uri = sound.optString("uri", "");

                if (uri.isEmpty()) continue;

                labels.add(categoryNames[c] + " · "
                        + sound.optString("name", "Звук"));
                specs.add(uri);
            }
        }

        if (labels.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("📚 Мои звуки")
                    .setMessage("Пока пусто.\nИмпортируй папку со звуками — "
                            + "файлы добавятся все сразу.")
                    .setPositiveButton("📥 Импорт папки",
                            (d, w) -> chooseSoundsFolder())
                    .setNegativeButton("Закрыть", null)
                    .show();
            return;
        }

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(12), dp(8), dp(12), dp(8));

        EditText search = new EditText(this);
        search.setHint("Поиск: смех, утка, бас…");
        search.setSingleLine(true);
        layout.addView(search);

        final ArrayList<String> filteredLabels = new ArrayList<>();
        final ArrayList<String> filteredSpecs = new ArrayList<>();

        filteredLabels.addAll(labels);
        filteredSpecs.addAll(specs);

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                filteredLabels);

        ListView list = new ListView(this);
        list.setAdapter(adapter);
        layout.addView(list, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(380)));

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("📚 Звук для «" + giftName + "»")
                .setView(layout)
                .setNegativeButton("Закрыть", null)
                .create();

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence text, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence text, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String query = editable.toString()
                        .trim()
                        .toLowerCase(Locale.ROOT);

                filteredLabels.clear();
                filteredSpecs.clear();

                for (int i = 0; i < labels.size(); i++) {
                    if (query.isEmpty()
                            || labels.get(i)
                                    .toLowerCase(Locale.ROOT)
                                    .contains(query)) {
                        filteredLabels.add(labels.get(i));
                        filteredSpecs.add(specs.get(i));
                    }
                }

                adapter.notifyDataSetChanged();
            }
        });

        list.setOnItemClickListener((parent, view, position, id) -> {
            if (position < 0 || position >= filteredSpecs.size()) {
                return;
            }

            String spec = filteredSpecs.get(position);

            prefs.edit()
                    .putString("gift_sound_" + giftKey, spec)
                    .apply();

            previewSoundSpec(spec);
            dialog.dismiss();
            showGiftCatalog();
        });

        dialog.show();
    }

    private void addSoundHeader(LinearLayout list, String text) {
        TextView header = new TextView(this);
        header.setText(text);
        header.setTextColor(0xFFFF1493);
        header.setTextSize(13);
        header.setTypeface(null, android.graphics.Typeface.BOLD);
        header.setPadding(dp(6), dp(16), dp(6), dp(6));
        list.addView(header);
    }

    private void addSoundRow(
            LinearLayout list,
            String label,
            String spec,
            String current,
            String giftKey,
            AlertDialog dialog) {

        boolean selected = spec == null
                ? (current == null || current.trim().isEmpty())
                : spec.equals(current);

        TextView row = new TextView(this);
        row.setText((selected ? "✅ " : "▫️ ") + label);
        row.setTextColor(selected ? 0xFF7CFFB2 : Color.WHITE);
        row.setTextSize(16);
        row.setPadding(dp(10), dp(12), dp(10), dp(12));

        GradientDrawable background = new GradientDrawable();
        background.setColor(selected ? 0xFF10261C : 0x00000000);
        background.setCornerRadius(dp(10));
        row.setBackground(background);

        row.setOnClickListener(v -> {
            if (spec == null) {
                prefs.edit()
                        .remove("gift_sound_" + giftKey)
                        .apply();
            } else {
                prefs.edit()
                        .putString("gift_sound_" + giftKey, spec)
                        .apply();
            }

            previewSoundSpec(spec);

            dialog.dismiss();
            showGiftCatalog();
        });

        list.addView(row);
    }

    private void previewSoundSpec(String spec) {
        stopSfxPreview();

        if ("none".equals(spec)) {
            return;
        }

        String resourceName = spec == null
                ? "gift_default"
                : (spec.startsWith("raw:")
                        ? spec.substring(4)
                        : null);

        try {
            if (resourceName != null) {
                int resourceId = getResources().getIdentifier(
                        resourceName, "raw", getPackageName());

                if (resourceId == 0) {
                    return;
                }

                MediaPlayer player = MediaPlayer.create(this, resourceId);

                if (player == null) {
                    return;
                }

                sfxPreview = player;
                player.setOnCompletionListener(this::releaseSfx);
                player.start();
                return;
            }

            MediaPlayer player = new MediaPlayer();
            sfxPreview = player;

            player.setOnPreparedListener(mp -> {
                if (sfxPreview == mp) {
                    mp.start();
                }
            });

            player.setOnCompletionListener(this::releaseSfx);

            player.setOnErrorListener((mp, what, extra) -> {
                releaseSfx(mp);
                return true;
            });

            player.setDataSource(this, Uri.parse(spec));
            player.prepareAsync();

        } catch (Exception e) {
            message("Не удалось воспроизвести звук");
        }
    }

    private void releaseSfx(MediaPlayer mp) {
        if (sfxPreview == mp) {
            sfxPreview = null;
        }

        try {
            mp.release();
        } catch (Exception ignored) {}
    }

    private void stopSfxPreview() {
        if (sfxPreview == null) {
            return;
        }

        MediaPlayer player = sfxPreview;
        sfxPreview = null;

        try {
            player.stop();
        } catch (Exception ignored) {}

        try {
            player.release();
        } catch (Exception ignored) {}
    }

    private void showFontSettings() {
        String[] labels = {
                "Roboto", "Roboto Light", "Roboto Black",
                "Roboto Condensed", "Roboto Italic",
                "Roboto Bold Italic", "Serif", "Serif Italic",
                "Monospace", "Casual", "Cursive", "Small Caps"
        };

        String[] families = {
                "sans-serif", "sans-serif-light", "sans-serif-black",
                "sans-serif-condensed", "sans-serif", "sans-serif",
                "serif", "serif", "monospace", "casual", "cursive",
                "sans-serif-smallcaps"
        };

        int[] styles = {
                android.graphics.Typeface.BOLD,
                android.graphics.Typeface.NORMAL,
                android.graphics.Typeface.NORMAL,
                android.graphics.Typeface.BOLD,
                android.graphics.Typeface.ITALIC,
                android.graphics.Typeface.BOLD_ITALIC,
                android.graphics.Typeface.BOLD,
                android.graphics.Typeface.ITALIC,
                android.graphics.Typeface.BOLD,
                android.graphics.Typeface.NORMAL,
                android.graphics.Typeface.NORMAL,
                android.graphics.Typeface.BOLD
        };

        String current = prefs.getString(
                "overlay_font", "sans-serif|1");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(12), dp(8), dp(12), dp(8));

        ScrollView scroll = new ScrollView(this);
        scroll.addView(layout);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("🔤 Шрифт в оверлее")
                .setView(scroll)
                .setPositiveButton("👁 Пример", (d, w) ->
                        previewOverlayText())
                .setNegativeButton("Закрыть", null)
                .create();

        for (int i = 0; i < labels.length; i++) {
            final String spec = families[i] + "|" + styles[i];
            final String label = labels[i];

            boolean selected = spec.equals(current);

            TextView row = new TextView(this);
            row.setText((selected ? "✅ " : "") + label
                    + "\nСъешь ещё этих мягких булок 123");
            row.setTextSize(16);
            row.setTextColor(selected ? 0xFF7CFFB2 : Color.WHITE);
            row.setTypeface(android.graphics.Typeface.create(
                    families[i], styles[i]));
            row.setPadding(dp(10), dp(12), dp(10), dp(12));

            GradientDrawable background = new GradientDrawable();
            background.setColor(selected ? 0xFF10261C : 0x00000000);
            background.setCornerRadius(dp(10));
            row.setBackground(background);

            row.setOnClickListener(v -> {
                prefs.edit()
                        .putString("overlay_font", spec)
                        .apply();

                dialog.dismiss();
                showFontSettings();
                message("Шрифт: " + label);
            });

            layout.addView(row);
        }

        dialog.show();
    }

    private void chooseOverlayTextSize() {
        String[] names = {
                "Мелкий (18)", "Обычный (22)",
                "Крупный (28)", "Огромный (34)"
        };

        int[] sizes = {18, 22, 28, 34};
        int current = prefs.getInt("overlay_text_size", 22);

        int checked = 1;

        for (int i = 0; i < sizes.length; i++) {
            if (sizes[i] == current) {
                checked = i;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Размер текста в оверлее")
                .setSingleChoiceItems(names, checked, (d, which) -> {
                    prefs.edit()
                            .putInt("overlay_text_size", sizes[which])
                            .apply();

                    d.dismiss();
                    message("Размер текста: " + sizes[which]);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void previewOverlayText() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !Settings.canDrawOverlays(this)) {
            message("Разреши показ поверх других приложений");
            startActivity(new Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName())));
            return;
        }

        Intent intent = new Intent(this, OverlayService.class);
        intent.setAction(OverlayService.ACTION_PREVIEW_TEXT);
        intent.putExtra("text",
                "🎁 @зритель подарил Розу × 3\n🪙 100 монет");

        try {
            startService(intent);
        } catch (Exception e) {
            message("Сервис не запущен");
        }
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

    final Uri folderUri = data.getData();

    try {
        getContentResolver().takePersistableUriPermission(
                folderUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
        );
    } catch (Exception e) {
        message("Не удалось открыть папку");
        return;
    }

    // Копирование папки — долгая операция, в UI-потоке она вешала
    // приложение на несколько секунд (ANR).
    message("Импорт начат, это может занять время");
    executor.execute(() -> {
        int imported = importMediaFolder(folderUri);

        runOnUiThread(() ->
                message("Импортировано файлов: " + imported));
    });

    return;
}

// Импорт папки с иконками подарков
if (requestCode == PICK_GIFT_ICONS_FOLDER) {
    if (resultCode != RESULT_OK || data == null
            || data.getData() == null) {
        return;
    }

    final Uri folderUri = data.getData();

    try {
        getContentResolver().takePersistableUriPermission(
                folderUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
        );
    } catch (Exception e) {
        message("Не удалось открыть папку");
        return;
    }

    message("Импорт иконок начат");

    executor.execute(() -> {
        int added = importGiftIconsFolder(folderUri);

        runOnUiThread(() -> {
            message(added == 0
                    ? "Картинки не найдены. Нужны PNG, JPG или WEBP, "
                            + "названные как подарок: «Роза.png»."
                    : "Иконок добавлено: " + added
                            + ". Открой «🎁 Все подарки».");

            showGiftCatalog();
        });
    });

    return;
}

// Импорт папки со звуками в библиотеку
if (requestCode == PICK_SOUNDS_FOLDER) {
    if (resultCode != RESULT_OK || data == null
            || data.getData() == null) {
        return;
    }

    final Uri folderUri = data.getData();

    try {
        getContentResolver().takePersistableUriPermission(
                folderUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
        );
    } catch (Exception e) {
        message("Не удалось открыть папку");
        return;
    }

    message("Импорт звуков начат");
    executor.execute(() -> {
        autoAssignedCount = 0;
        int imported = importSoundsFolder(folderUri);
        final int assigned = autoAssignedCount;

        runOnUiThread(() -> message(imported + assigned == 0
                ? "Аудиофайлы не найдены"
                : "Звуков добавлено: " + imported
                        + ", к подаркам: " + assigned));
    });

    return;
}

// Импорт отдельных файлов
if (requestCode == PICK_MEDIA_FILES) {
    if (resultCode != RESULT_OK || data == null) {
        return;
    }

    final ArrayList<Uri> uris = new ArrayList<>();

    if (data.getClipData() != null) {
        android.content.ClipData clips = data.getClipData();

        for (int i = 0; i < clips.getItemCount(); i++) {
            uris.add(clips.getItemAt(i).getUri());
        }

    } else if (data.getData() != null) {
        uris.add(data.getData());
    }

    if (uris.isEmpty()) {
        return;
    }

    executor.execute(() -> {
        int imported = 0;

        for (Uri fileUri : uris) {
            if (importMediaFile(fileUri)) {
                imported++;
            }
        }

        final int total = imported;
        runOnUiThread(() ->
                message("Импортировано файлов: " + total));
    });

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
        final float savedPitch = prefs.getFloat("voice_pitch", 1.0f);
        final float savedRate = prefs.getFloat("voice_rate", 0.95f);
        final int savedVolume = prefs.getInt("voice_volume", 100);

        final float[] pitch = {savedPitch};
        final float[] rate = {savedRate};
        final int[] volume = {savedVolume};

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(16), dp(8), dp(16), dp(8));

        ScrollView scroll = new ScrollView(this);
        scroll.addView(layout);

        TextView voiceRow = new TextView(this);
        voiceRow.setText("🗣 Голос: " + currentVoiceLabel());
        voiceRow.setTextColor(Color.WHITE);
        voiceRow.setTextSize(17);
        voiceRow.setPadding(dp(10), dp(14), dp(10), dp(14));

        GradientDrawable voiceBackground = new GradientDrawable();
        voiceBackground.setColor(0xFF141B33);
        voiceBackground.setCornerRadius(dp(12));
        voiceRow.setBackground(voiceBackground);

        voiceRow.setOnClickListener(v -> displayVoiceSettings());

        layout.addView(voiceRow);

        TextView pitchLabel = new TextView(this);
        pitchLabel.setText("🎚 Тон: " + Math.round(pitch[0] * 100) + "%");
        pitchLabel.setTextColor(Color.WHITE);
        pitchLabel.setTextSize(16);
        pitchLabel.setPadding(0, dp(14), 0, 0);
        layout.addView(pitchLabel);

        android.widget.SeekBar pitchBar = new android.widget.SeekBar(this);
        pitchBar.setMax(60);
        pitchBar.setProgress(Math.round((pitch[0] - 0.7f) * 100));
        layout.addView(pitchBar);

        pitchBar.setOnSeekBarChangeListener(
                new android.widget.SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(
                    android.widget.SeekBar bar, int progress, boolean fromUser) {
                pitch[0] = 0.7f + progress / 100f;
                pitchLabel.setText(
                        "🎚 Тон: " + Math.round(pitch[0] * 100) + "%");
            }

            @Override
            public void onStartTrackingTouch(android.widget.SeekBar bar) {
            }

            @Override
            public void onStopTrackingTouch(android.widget.SeekBar bar) {
            }
        });

        TextView rateLabel = new TextView(this);
        rateLabel.setText("⚡ Скорость: " + Math.round(rate[0] * 100) + "%");
        rateLabel.setTextColor(Color.WHITE);
        rateLabel.setTextSize(16);
        rateLabel.setPadding(0, dp(10), 0, 0);
        layout.addView(rateLabel);

        android.widget.SeekBar rateBar = new android.widget.SeekBar(this);
        rateBar.setMax(60);
        rateBar.setProgress(Math.round((rate[0] - 0.7f) * 100));
        layout.addView(rateBar);

        rateBar.setOnSeekBarChangeListener(
                new android.widget.SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(
                    android.widget.SeekBar bar, int progress, boolean fromUser) {
                rate[0] = 0.7f + progress / 100f;
                rateLabel.setText(
                        "⚡ Скорость: " + Math.round(rate[0] * 100) + "%");
            }

            @Override
            public void onStartTrackingTouch(android.widget.SeekBar bar) {
            }

            @Override
            public void onStopTrackingTouch(android.widget.SeekBar bar) {
            }
        });

        TextView volumeLabel = new TextView(this);
        volumeLabel.setText("🔊 Громкость: " + volume[0] + "%");
        volumeLabel.setTextColor(Color.WHITE);
        volumeLabel.setTextSize(16);
        volumeLabel.setPadding(0, dp(10), 0, 0);
        layout.addView(volumeLabel);

        android.widget.SeekBar volumeBar = new android.widget.SeekBar(this);
        volumeBar.setMax(100);
        volumeBar.setProgress(volume[0]);
        layout.addView(volumeBar);

        volumeBar.setOnSeekBarChangeListener(
                new android.widget.SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(
                    android.widget.SeekBar bar, int progress, boolean fromUser) {
                volume[0] = progress;
                volumeLabel.setText("🔊 Громкость: " + progress + "%");
            }

            @Override
            public void onStartTrackingTouch(android.widget.SeekBar bar) {
            }

            @Override
            public void onStopTrackingTouch(android.widget.SeekBar bar) {
            }
        });

        android.widget.CheckBox chatBox = new android.widget.CheckBox(this);
        chatBox.setText("💬 Озвучивать чат");
        chatBox.setTextColor(Color.WHITE);
        chatBox.setTextSize(16);
        chatBox.setChecked(prefs.getBoolean("chat_speech_enabled", true));
        layout.addView(chatBox);

        android.widget.CheckBox nickBox = new android.widget.CheckBox(this);
        nickBox.setText("🏷 Называть ник автора");
        nickBox.setTextColor(Color.WHITE);
        nickBox.setTextSize(16);
        nickBox.setChecked(prefs.getBoolean("chat_speech_nick", true));
        layout.addView(nickBox);

        Button personas = new Button(this);
        personas.setText("🎭 Образы голоса");
        personas.setOnClickListener(v -> {
            final String[] names = {
                    "🎤 Блогер — энергичный",
                    "😎 Стример — спокойный",
                    "🎬 Актёр — низкий",
                    "📺 Диктор — ровный",
                    "🤪 Мем — высокий",
                    "👶 Мультяшный",
                    "👹 Демон",
                    "🤖 Робот",
                    "🐿️ Быстрый",
                    "🐢 Медленный",
                    "😂 Весёлый",
                    "😴 Спокойный",
                    "👧 Девочка",
                    "👦 Мальчик",
                    "🗣 Ведущий"
            };

            final float[] personaPitch = {
                    1.05f, 0.98f, 0.85f, 0.95f, 1.25f,
                    1.35f, 0.70f, 0.85f, 1.10f, 0.95f,
                    1.15f, 0.90f, 1.30f, 1.15f, 1.00f
            };

            final float[] personaRate = {
                    1.08f, 1.00f, 0.92f, 0.92f, 1.15f,
                    1.10f, 0.85f, 0.85f, 1.25f, 0.80f,
                    1.10f, 0.85f, 1.05f, 1.05f, 1.00f
            };

            new AlertDialog.Builder(this)
                    .setTitle("🎭 Образы голоса")
                    .setItems(names, (d, which) -> {
                        pitch[0] = personaPitch[which];
                        rate[0] = personaRate[which];

                        pitchBar.setProgress(
                                Math.round((pitch[0] - 0.7f) * 100));
                        rateBar.setProgress(
                                Math.round((rate[0] - 0.7f) * 100));

                        previewVoiceWith(
                                pitch[0], rate[0], volume[0],
                                prefs.getString("selected_voice_name", ""));

                        message("Образ: " + names[which]);
                    })
                    .setNegativeButton("Закрыть", null)
                    .show();
        });
        layout.addView(personas);

        Button test = new Button(this);
        test.setText("▶ Проверить голос");
        test.setOnClickListener(v -> previewVoiceWith(
                pitch[0], rate[0], volume[0],
                prefs.getString("selected_voice_name", "")));
        layout.addView(test);

        Button natural = new Button(this);
        natural.setText("↩️ Естественный голос");
        natural.setOnClickListener(v -> {
            pitch[0] = 1.0f;
            rate[0] = 0.95f;
            volume[0] = 100;

            pitchBar.setProgress(30);
            rateBar.setProgress(25);
            volumeBar.setProgress(100);

            prefs.edit()
                    .putFloat("voice_pitch", 1.0f)
                    .putFloat("voice_rate", 0.95f)
                    .putInt("voice_volume", 100)
                    .apply();

            previewVoiceWith(1.0f, 0.95f, 100,
                    prefs.getString("selected_voice_name", ""));

            message("Вернул естественные тон и скорость");
        });
        layout.addView(natural);

        new AlertDialog.Builder(this)
                .setTitle("🎙️ Озвучка чата")
                .setView(scroll)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    prefs.edit()
                            .putFloat("voice_pitch", pitch[0])
                            .putFloat("voice_rate", rate[0])
                            .putInt("voice_volume", volume[0])
                            .putBoolean("chat_speech_enabled", chatBox.isChecked())
                            .putBoolean("chat_speech_nick", nickBox.isChecked())
                            .apply();

                    Intent intent = new Intent(
                            this, OverlayService.class);
                    intent.setAction(chatBox.isChecked()
                            ? OverlayService.ACTION_START_CHAT_SPEECH
                            : OverlayService.ACTION_STOP_CHAT_SPEECH);

                    try {
                        startService(intent);
                    } catch (Exception ignored) {
                    }

                    message("Настройки озвучки сохранены");
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private String currentVoiceLabel() {
        String saved = prefs.getString("selected_voice_name", "");

        if (saved.isEmpty()) {
            return "авто (лучший)";
        }

        return saved.replace("-network", " · сеть")
                .replace("ru-ru-", "")
                .replace("ru-RU-", "");
    }

    private void previewVoiceWith(
            float pitch, float rate, int volume, String voiceName) {

        if (voicePreview == null || !voicePreviewReady) {
            message("Озвучка ещё не готова. Попробуй позже.");
            return;
        }

        applyPreviewVoice(voiceName);

        voicePreview.setPitch(pitch);
        voicePreview.setSpeechRate(rate);

        Bundle params = new Bundle();
        params.putFloat(
                TextToSpeech.Engine.KEY_PARAM_VOLUME,
                volume / 100f);

        voicePreview.speak(
                "Привет! Так будет звучать озвучка чата в эфире.",
                TextToSpeech.QUEUE_FLUSH,
                params,
                "voice_preview");
    }

    private void displayVoiceSettings() {
        if (!voicePreviewReady || voicePreview == null) {
            message("Русская озвучка ещё не готова. "
                    + "Проверь синтез речи Android.");
            return;
        }

        ArrayList<String> labels = new ArrayList<>();
        ArrayList<String> names = new ArrayList<>();

        labels.add("🤖 Авто — лучший голос");
        names.add("");

        try {
            List<Voice> voices = new ArrayList<>();

            for (Voice voice : voicePreview.getVoices()) {
                Locale locale = voice.getLocale();

                if (locale != null
                        && "ru".equalsIgnoreCase(locale.getLanguage())) {
                    voices.add(voice);
                }
            }

            Collections.sort(voices, (left, right) -> {
                if (left.getQuality() != right.getQuality()) {
                    return right.getQuality() - left.getQuality();
                }

                if (left.isNetworkConnectionRequired()
                        != right.isNetworkConnectionRequired()) {
                    return left.isNetworkConnectionRequired() ? -1 : 1;
                }

                return left.getName().compareTo(right.getName());
            });

            for (Voice voice : voices) {
                String name = voice.getName()
                        .replace("ru-ru-", "")
                        .replace("ru-RU-", "");

                String mark = voice.isNetworkConnectionRequired()
                        ? " · сеть"
                        : "";

                labels.add("🎙 " + name + mark);
                names.add(voice.getName());
            }
        } catch (Exception ignored) {
        }

        if (labels.size() == 1) {
            message("Установи русский голос: "
                    + "Настройки Android → Язык и ввод → "
                    + "Синтез речи");
            return;
        }

        String selected = prefs.getString("selected_voice_name", "");
        int checked = names.indexOf(selected);

        if (checked < 0) checked = 0;

        final int[] choice = {checked};

        new AlertDialog.Builder(this)
                .setTitle("🗣 Голоса (" + (labels.size() - 1) + ")")
                .setSingleChoiceItems(
                        labels.toArray(new String[0]),
                        checked,
                        (dialog, which) -> {
                            choice[0] = which;
                            applyPreviewVoice(names.get(which));
                            testVoice(names.get(which));
                        })
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    String name = names.get(choice[0]);

                    prefs.edit()
                            .putString("selected_voice_name", name)
                            .apply();

                    applyPreviewVoice(name);
                    showVoiceSettings();
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

    // Люди копируют токен по-разному: целиком ссылку, с #, с &token_type.
    // Приводим всё к чистому токену.
    private String sanitizeDaToken(String raw) {
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

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        int amp = token.indexOf('&');
        if (amp >= 0) {
            token = token.substring(0, amp);
        }

        return token.replace("\"", "")
                .replace("'", "")
                .replace(" ", "")
                .trim();
    }

    private void showDaTokenDialog() {
        String saved = prefs.getString("da_token", "");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(12), dp(8), dp(12), dp(8));

        TextView status = new TextView(this);
        status.setText(saved.trim().isEmpty()
                ? "Токен не задан"
                : "Токен сохранён ✅ (" + saved.length()
                        + " символов, …"
                        + (saved.length() > 4
                                ? saved.substring(saved.length() - 4)
                                : saved)
                        + ")");
        status.setTextColor(saved.trim().isEmpty()
                ? 0xFFFF8A80 : 0xFF7CFFB2);
        status.setTextSize(15);
        layout.addView(status);

        EditText input = new EditText(this);
        input.setHint("Вставь access_token");
        input.setText(saved);
        input.setTextSize(13);
        input.setSingleLine(false);
        input.setMinLines(2);
        input.setMaxLines(4);
        layout.addView(input);

        Button authorize = new Button(this);
        authorize.setText("🌐 Открыть ссылку разрешения");
        authorize.setOnClickListener(v -> {
            String clientId = prefs.getString(
                    "da_client_id", "22326").trim();

            openLink("https://www.donationalerts.com/oauth/authorize"
                    + "?client_id=" + clientId
                    + "&redirect_uri=https%3A%2F%2Fwww.donationalerts.com%2F"
                    + "&response_type=token"
                    + "&scope=oauth-user-show%20oauth-donation-index");
        });
        layout.addView(authorize);

        Button paste = new Button(this);
        paste.setText("📋 Вставить из буфера");
        paste.setOnClickListener(v -> {
            try {
                android.content.ClipboardManager clipboard =
                        (android.content.ClipboardManager)
                                getSystemService(CLIPBOARD_SERVICE);

                CharSequence clip = clipboard.getText();

                if (clip == null) {
                    message("Буфер пуст");
                    return;
                }

                String token = sanitizeDaToken(clip.toString());

                input.setText(token);

                message("Вставлено символов: " + token.length());
            } catch (Exception error) {
                message("Не удалось прочитать буфер");
            }
        });
        layout.addView(paste);

        TextView hint = new TextView(this);
        hint.setText("Токен хранится только на телефоне. "
                + "Сервер для донатов больше не нужен.\n"
                + "Токен — длинная строка из букв и цифр, "
                + "обычно 40+ символов.");
        hint.setTextColor(0xFF9AA7C7);
        hint.setTextSize(12);
        hint.setPadding(0, dp(6), 0, 0);
        layout.addView(hint);

        new AlertDialog.Builder(this)
                .setTitle("🔑 Токен DonationAlerts")
                .setView(layout)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    String token = sanitizeDaToken(
                            input.getText().toString());

                    prefs.edit().putString("da_token", token).apply();

                    if (token.isEmpty()) {
                        message("Токен удалён");
                    } else {
                        message("Токен сохранён, проверяю…");
                        checkDaToken(token);
                    }
                })
                .setNeutralButton("Проверить", (dialog, which) ->
                        checkDaToken(sanitizeDaToken(
                                input.getText().toString())))
                .setNegativeButton("Закрыть", null)
                .show();
    }

    private void checkDaToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            message("Сначала вставь токен");
            return;
        }

        final String value = token.trim();

        if (value.length() < 40) {
            message("Это не похоже на токен: " + value.length()
                    + " символов. Токен длинный, обычно начинается "
                    + "с «eyJ» или это строка 40+ символов.");
        }

        executor.execute(() -> {
            String result;

            try {
                java.net.HttpURLConnection connection =
                        (java.net.HttpURLConnection) new URL(
                                "https://www.donationalerts.com"
                                        + "/api/v1/alerts/donations?page=1")
                                .openConnection();

                connection.setRequestProperty(
                        "Authorization", "Bearer " + value);
                connection.setRequestProperty(
                        "Accept", "application/json");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);

                int code = connection.getResponseCode();

                if (code == 200) {
                    int count = 0;

                    try {
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

                        String raw = body.toString().trim();

                        org.json.JSONArray list = raw.startsWith("{")
                                ? new org.json.JSONObject(raw)
                                        .optJSONArray("data")
                                : new org.json.JSONArray(raw);

                        count = list == null ? 0 : list.length();
                    } catch (Exception ignored) {
                    }

                    result = "✅ Токен работает. Донатов в истории: "
                            + count;
                } else if (code == 401) {
                    String detail = "";

                    try {
                        java.io.InputStream stream =
                                connection.getErrorStream();

                        if (stream != null) {
                            java.io.BufferedReader reader =
                                    new java.io.BufferedReader(
                                            new java.io.InputStreamReader(
                                                    stream,
                                                    java.nio.charset.StandardCharsets.UTF_8));

                            StringBuilder body = new StringBuilder();
                            String line;

                            while ((line = reader.readLine()) != null) {
                                body.append(line);
                            }

                            detail = body.toString().trim();
                        }
                    } catch (Exception ignored) {
                    }

                    if (detail.contains("Invalid scope")) {
                        result = "⚠️ Токен без нужного доступа. "
                                + "Нажми «🌐 Открыть ссылку разрешения», "
                                + "разреши доступ и вставь новый токен.";
                    } else {
                        result = "❌ Токен не подошёл (401). "
                                + (detail.isEmpty() ? "" : detail + " ")
                                + "Проверь, что скопирован access_token, "
                                + "а не ключ API.";
                    }
                } else {
                    result = "❌ Ответ сервера: HTTP " + code;
                }

                connection.disconnect();
            } catch (Exception error) {
                result = "❌ Нет связи: " + error.getMessage();
            }

            final String text = result;
            runOnUiThread(() -> message(text));
        });
    }

    private void showDaTokenHelp() {
        String text = "1. Открой donationalerts.com/application/clients\n\n"
                + "2. «Создать приложение» → название любое, "
                + "Redirect URI: https://www.donationalerts.com/\n\n"
                + "3. Скопируй Client ID.\n\n"
                + "4. Открой ссылку:\n"
                + "donationalerts.com/oauth/authorize"
                + "?client_id=ТВОЙ_ID"
                + "&redirect_uri=https%3A%2F%2Fwww.donationalerts.com%2F"
                + "&response_type=token"
                + "&scope=oauth-user-show%20oauth-donation-index\n\n"
                + "5. Нажми «Разрешить» — в адресной строке появится "
                + "#access_token=...\n\n"
                + "6. Скопируй этот токен и вставь в "
                + "«🔑 Токен DonationAlerts».\n\n"
                + "После этого донаты приходят прямо на телефон.";

        new AlertDialog.Builder(this)
                .setTitle("📋 Как получить токен")
                .setMessage(text)
                .setPositiveButton("Открыть сайт", (dialog, which) ->
                        openLink("https://www.donationalerts.com/application/clients"))
                .setNegativeButton("Закрыть", null)
                .show();
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

// ------------------------------------------------------------------
// Иконки подарков: картинка с именем подарка становится его иконкой,
// а незнакомое имя добавляет новый подарок в список.
// ------------------------------------------------------------------

private File giftIconsDir() {
    File dir = new File(getFilesDir(), "gift_icons");

    if (!dir.exists()) {
        dir.mkdirs();
    }

    return dir;
}

private ArrayList<String[]> allGifts() {
    ArrayList<String[]> gifts = new ArrayList<>();
    Collections.addAll(gifts, giftCatalog);

    try {
        JSONArray custom = new JSONArray(
                prefs.getString("custom_gifts", "[]"));

        for (int i = 0; i < custom.length(); i++) {
            JSONObject item = custom.optJSONObject(i);

            if (item == null) continue;

            String key = item.optString("key", "");
            String name = item.optString("name", "");

            if (key.isEmpty() || name.isEmpty()) continue;

            boolean exists = false;

            for (String[] gift : gifts) {
                if (gift[0].equals(key)) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                gifts.add(new String[]{key, name});
            }
        }
    } catch (Exception ignored) {
    }

    return gifts;
}

// Новый подарок из имени файла. Если такой уже есть — возвращаем его ключ.
private String addCustomGift(String rawName) {
    String name = rawName == null ? "" : rawName.trim();

    if (name.isEmpty()) return null;

    String known = matchGiftKey(name);

    if (known != null) return known;

    String normalized = normalizeName(name);

    if (normalized.isEmpty()) return null;

    String key = "custom_" + normalized.replace(' ', '_');

    for (String[] gift : allGifts()) {
        if (gift[0].equals(key)) return key;
    }

    String label = name.substring(0, 1).toUpperCase(Locale.ROOT)
            + name.substring(1);

    try {
        JSONArray custom = new JSONArray(
                prefs.getString("custom_gifts", "[]"));

        JSONObject item = new JSONObject();
        item.put("key", key);
        item.put("name", "🎁 " + label);
        custom.put(item);

        prefs.edit()
                .putString("custom_gifts", custom.toString())
                .apply();
    } catch (Exception ignored) {
        return null;
    }

    return key;
}

private String giftIconPath(String key) {
    return prefs.getString("gift_icon_" + key, "");
}

private android.graphics.Bitmap loadGiftIcon(String key) {
    String path = giftIconPath(key);

    if (path.isEmpty()) return null;

    try {
        android.graphics.BitmapFactory.Options options =
                new android.graphics.BitmapFactory.Options();
        options.inSampleSize = 2;

        return android.graphics.BitmapFactory.decodeFile(path, options);
    } catch (Exception error) {
        return null;
    }
}

private boolean copyGiftIcon(
        String key, String fileName, java.io.InputStream input) {

    try {
        String ext = "png";
        int dot = fileName.lastIndexOf('.');

        if (dot >= 0) {
            ext = fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
        }

        if (!ext.equals("png")
                && !ext.equals("jpg")
                && !ext.equals("jpeg")
                && !ext.equals("webp")) {
            ext = "png";
        }

        File target = new File(giftIconsDir(), key + "." + ext);

        try (java.io.OutputStream output =
                     new java.io.FileOutputStream(target)) {
            byte[] buffer = new byte[8192];
            int read;

            while ((read = input.read(buffer)) > 0) {
                output.write(buffer, 0, read);
            }
        }

        prefs.edit()
                .putString("gift_icon_" + key, target.getAbsolutePath())
                .apply();

        return true;
    } catch (Exception error) {
        return false;
    }
}

private String stripExtension(String name) {
    int dot = name.lastIndexOf('.');
    return dot > 0 ? name.substring(0, dot) : name;
}

private boolean looksLikeGiftIcon(String name) {
    String lower = name.toLowerCase(Locale.ROOT);

    if (!lower.endsWith(".png")
            && !lower.endsWith(".jpg")
            && !lower.endsWith(".jpeg")
            && !lower.endsWith(".webp")) {
        return false;
    }

    // Скриншоты и фото из галереи подарками не считаем.
    return !lower.startsWith("screenshot")
            && !lower.startsWith("img_")
            && !lower.startsWith("photo_");
}

private void chooseGiftIconsFolder() {
    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

    try {
        startActivityForResult(intent, PICK_GIFT_ICONS_FOLDER);
    } catch (Exception error) {
        message("Не удалось открыть выбор папки");
    }
}

private int importGiftIconsFolder(Uri treeUri) {
    try {
        return collectGiftIcons(
                treeUri,
                android.provider.DocumentsContract
                        .getTreeDocumentId(treeUri),
                "");
    } catch (Exception error) {
        return 0;
    }
}

private int collectGiftIcons(
        Uri treeUri, String parentId, String parentName) {

    int added = 0;

    Uri childrenUri =
            android.provider.DocumentsContract
                    .buildChildDocumentsUriUsingTree(treeUri, parentId);

    try (android.database.Cursor cursor =
                 getContentResolver().query(
                         childrenUri,
                         new String[]{
                                 android.provider.DocumentsContract
                                         .Document.COLUMN_DOCUMENT_ID,
                                 android.provider.DocumentsContract
                                         .Document.COLUMN_DISPLAY_NAME,
                                 android.provider.DocumentsContract
                                         .Document.COLUMN_MIME_TYPE
                         },
                         null, null, null)) {

        if (cursor == null) return added;

        while (cursor.moveToNext()) {
            String id = cursor.getString(0);
            String name = cursor.getString(1);
            String mime = cursor.getString(2);

            if (android.provider.DocumentsContract.Document.MIME_TYPE_DIR
                    .equals(mime)) {
                added += collectGiftIcons(
                        treeUri,
                        id,
                        name == null ? parentName : name);
                continue;
            }

            if (mime == null || !mime.startsWith("image/")) {
                continue;
            }

            if ("image/gif".equals(mime)) {
                continue;
            }

            String fileName = name == null ? "" : name;

            if (!looksLikeGiftIcon(fileName)) {
                continue;
            }

            // Название папки тоже подсказка: Подарки/Роза/1.png — это Роза.
            String giftKey = matchGiftKey(
                    parentName + " " + fileName);

            if (giftKey == null) {
                giftKey = addCustomGift(stripExtension(fileName));
            }

            if (giftKey == null) continue;

            if (!giftIconPath(giftKey).isEmpty()) continue;

            Uri uri = android.provider.DocumentsContract
                    .buildDocumentUriUsingTree(treeUri, id);

            try (java.io.InputStream input =
                         getContentResolver().openInputStream(uri)) {
                if (input != null
                        && copyGiftIcon(giftKey, fileName, input)) {
                    added++;
                }
            } catch (Exception ignored) {
            }
        }
    } catch (Exception ignored) {
    }

    return added;
}

private int importGiftIconsFromMediaStore() {
    Uri collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

    String[] projection = {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.MIME_TYPE
    };

    String selection;
    String[] args;

    if (Build.VERSION.SDK_INT >= 29) {
        selection = MediaStore.Images.Media.RELATIVE_PATH + " LIKE ?";
        args = new String[]{"Download/LGS/%"};
    } else {
        selection = MediaStore.Images.Media.DATA + " LIKE ?";
        args = new String[]{"%/Download/LGS/%"};
    }

    int added = 0;

    try (android.database.Cursor cursor =
                 getContentResolver().query(
                         collection,
                         projection,
                         selection,
                         args,
                         null)) {

        if (cursor == null) return 0;

        while (cursor.moveToNext()) {
            String name = cursor.getString(1);
            String mime = cursor.getString(2);

            if (name == null || "image/gif".equals(mime)) {
                continue;
            }

            if (!looksLikeGiftIcon(name)) {
                continue;
            }

            String giftKey = matchGiftKey(name);

            if (giftKey == null) {
                giftKey = addCustomGift(stripExtension(name));
            }

            if (giftKey == null) continue;

            if (!giftIconPath(giftKey).isEmpty()) continue;

            Uri uri = ContentUris.withAppendedId(
                    collection, cursor.getLong(0));

            try (java.io.InputStream input =
                         getContentResolver().openInputStream(uri)) {
                if (input != null
                        && copyGiftIcon(giftKey, name, input)) {
                    added++;
                }
            } catch (Exception ignored) {
            }
        }
    } catch (Exception ignored) {
    }

    return added;
}

private void autoImportFromPhoneFolder() {
    String audioPermission = Build.VERSION.SDK_INT >= 33
            ? "android.permission.READ_MEDIA_AUDIO"
            : android.Manifest.permission.READ_EXTERNAL_STORAGE;

    if (checkSelfPermission(audioPermission)
            != android.content.pm.PackageManager.PERMISSION_GRANTED) {

        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(new String[]{
                    "android.permission.READ_MEDIA_AUDIO",
                    "android.permission.READ_MEDIA_IMAGES"
            }, 901);
        } else {
            requestPermissions(new String[]{
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
            }, 901);
        }

        return;
    }

    executor.execute(() -> {
        autoAssignedCount = 0;

        int sounds = importFolderSoundsFromMediaStore();
        int gifs = importFolderGifsFromMediaStore();
        int icons = importGiftIconsFromMediaStore();

        final int soundCount = sounds;
        final int gifCount = gifs;
        final int iconCount = icons;
        final int assigned = autoAssignedCount;

        if (soundCount + gifCount + iconCount + assigned > 0) {
            runOnUiThread(() -> message(
                    "Из папки LGS: звуков " + soundCount
                            + ", гифок " + gifCount
                            + ", иконок " + iconCount
                            + ", сразу к подаркам " + assigned));
        }
    });
}

@Override
public void onRequestPermissionsResult(
        int requestCode,
        String[] permissions,
        int[] grantResults) {

    super.onRequestPermissionsResult(
            requestCode, permissions, grantResults);

    if (requestCode == 901
            && grantResults.length > 0
            && grantResults[0]
                    == android.content.pm.PackageManager
                            .PERMISSION_GRANTED) {

        autoImportFromPhoneFolder();
    }
}

private String normalizeName(String raw) {
    if (raw == null) return "";

    return raw.toLowerCase(Locale.ROOT)
            .replace('ё', 'е')
            .replaceAll("\\.[a-z0-9]{2,4}$", "")
            .replaceAll("[^a-zа-я0-9]+", " ")
            .trim();
}

private boolean containsPhrase(String haystack, String needle) {
    if (needle == null || needle.isEmpty()) return false;

    return (" " + haystack + " ").contains(" " + needle + " ");
}

// Ищет подарок по названию файла или папки: «Роза.mp3», «rose.wav»,
// «подарок лев.mp3» — всё это найдёт нужный подарок.
private String matchGiftKey(String rawName) {
    String name = normalizeName(rawName);

    if (name.isEmpty()) return null;

    for (String[] gift : allGifts()) {
        String key = gift[0].replace('_', ' ').toLowerCase(Locale.ROOT);

        if (containsPhrase(name, key)) {
            return gift[0];
        }

        String label = gift[1]
                .replaceAll("[^\\p{L}\\p{N}]+", " ")
                .toLowerCase(Locale.ROOT)
                .replace('ё', 'е')
                .trim();

        if (containsPhrase(name, label)) {
            return gift[0];
        }
    }

    return null;
}

private int importFolderSoundsFromMediaStore() {
    JSONArray library = getLibrarySounds("memes");

    java.util.HashSet<String> existing = new java.util.HashSet<>();

    for (int i = 0; i < library.length(); i++) {
        JSONObject item = library.optJSONObject(i);

        if (item != null) {
            existing.add(item.optString("uri", ""));
        }
    }

    Uri collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

    String[] projection = {
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME
    };

    String selection;
    String[] args;

    if (Build.VERSION.SDK_INT >= 29) {
        selection = MediaStore.Audio.Media.RELATIVE_PATH + " LIKE ?";
        args = new String[]{"Download/LGS/%"};
    } else {
        selection = MediaStore.Audio.Media.DATA + " LIKE ?";
        args = new String[]{"%/Download/LGS/%"};
    }

    int added = 0;

    try (android.database.Cursor cursor =
                 getContentResolver().query(
                         collection,
                         projection,
                         selection,
                         args,
                         null)) {

        if (cursor == null) return 0;

        while (cursor.moveToNext()) {
            long id = cursor.getLong(0);
            String name = cursor.getString(1);

            Uri uri = ContentUris.withAppendedId(collection, id);

            if (existing.contains(uri.toString())) continue;

            // Если файл назван как подарок — сразу привязываем звук.
            String giftKey = matchGiftKey(name);

            if (giftKey != null
                    && !prefs.contains("gift_sound_" + giftKey)) {

                prefs.edit()
                        .putString("gift_sound_" + giftKey, uri.toString())
                        .apply();

                autoAssignedCount++;
                existing.add(uri.toString());
                continue;
            }

            JSONObject item = new JSONObject();

            try {
                item.put("name", name == null ? "Звук" : name);
                item.put("uri", uri.toString());

                library.put(item);
                existing.add(uri.toString());
                added++;
            } catch (Exception ignored) {
            }
        }
    } catch (Exception ignored) {
    }

    if (added > 0) {
        prefs.edit()
                .putString(LIBRARY_MEMES, library.toString())
                .apply();
    }

    return added;
}

private int importFolderGifsFromMediaStore() {
    java.util.HashSet<String> existing = new java.util.HashSet<>();

    try {
        JSONArray items = new JSONArray(
                prefs.getString("media_library_items", "[]"));

        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.optJSONObject(i);

            if (item != null) {
                existing.add(item.optString("name", ""));
            }
        }
    } catch (Exception ignored) {
    }

    Uri collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

    String[] projection = {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.MIME_TYPE
    };

    String selection;
    String[] args;

    if (Build.VERSION.SDK_INT >= 29) {
        selection = MediaStore.Images.Media.RELATIVE_PATH + " LIKE ?";
        args = new String[]{"Download/LGS/%"};
    } else {
        selection = MediaStore.Images.Media.DATA + " LIKE ?";
        args = new String[]{"%/Download/LGS/%"};
    }

    int added = 0;

    try (android.database.Cursor cursor =
                 getContentResolver().query(
                         collection,
                         projection,
                         selection,
                         args,
                         null)) {

        if (cursor == null) return 0;

        while (cursor.moveToNext()) {
            String name = cursor.getString(1);
            String mime = cursor.getString(2);

            if (name == null) continue;

            String lower = name.toLowerCase(Locale.ROOT);

            if (!lower.endsWith(".gif")
                    && (mime == null || !mime.equals("image/gif"))) {
                continue;
            }

            if (existing.contains(name)) continue;

            Uri uri = ContentUris.withAppendedId(
                    collection, cursor.getLong(0));

            if (!importMediaFile(uri)) continue;

            existing.add(name);
            added++;

            assignAnimationByName(name);
        }
    } catch (Exception ignored) {
    }

    return added;
}

// Гифка с именем подарка сразу становится анимацией этого подарка.
// «подписка.gif» и «донат.gif» — анимациями события.
private void assignAnimationByName(String fileName) {
    if (lastImportedFileName == null) return;

    String path = "file://" + new File(
            new File(getFilesDir(), "media_library"),
            lastImportedFileName).getAbsolutePath();

    String normalized = normalizeName(fileName);

    if (containsPhrase(normalized, "подписка")
            || containsPhrase(normalized, "подписчик")
            || containsPhrase(normalized, "follower")
            || containsPhrase(normalized, "follow")) {

        prefs.edit()
                .putString("animation_follower_uri", path)
                .apply();

        autoAssignedCount++;
        return;
    }

    if (containsPhrase(normalized, "донат")
            || containsPhrase(normalized, "donation")
            || containsPhrase(normalized, "donate")) {

        prefs.edit()
                .putString("animation_donation_uri", path)
                .apply();

        autoAssignedCount++;
        return;
    }

    String giftKey = matchGiftKey(fileName);

    if (giftKey == null) return;

    if (prefs.contains("animation_gift_" + giftKey)) return;

    prefs.edit()
            .putString("animation_gift_" + giftKey, path)
            .apply();

    autoAssignedCount++;
}

private void distributeSoundsByNames() {
    ArrayList<String> labels = new ArrayList<>();
    ArrayList<String> specs = new ArrayList<>();

    String[] categories = {"memes", "funny", "melodies", "effects"};

    for (String category : categories) {
        JSONArray sounds = getLibrarySounds(category);

        for (int i = 0; i < sounds.length(); i++) {
            JSONObject sound = sounds.optJSONObject(i);

            if (sound == null) continue;

            String uri = sound.optString("uri", "");

            if (uri.isEmpty()) continue;

            labels.add(sound.optString("name", ""));
            specs.add(uri);
        }
    }

    if (labels.isEmpty()) {
        message("Сначала импортируй звуки");
        return;
    }

    int assigned = 0;
    int skipped = 0;

    SharedPreferences.Editor editor = prefs.edit();

    for (int i = 0; i < labels.size(); i++) {
        String giftKey = matchGiftKey(labels.get(i));

        if (giftKey == null) {
            skipped++;
            continue;
        }

        if (prefs.contains("gift_sound_" + giftKey)) {
            skipped++;
            continue;
        }

        editor.putString("gift_sound_" + giftKey, specs.get(i));
        assigned++;
    }

    editor.apply();

    if (assigned == 0) {
        message("По названиям не нашёл подарков. "
                + "Назови файлы как подарки: «Роза.mp3», «Лев.mp3».");
    } else {
        message("Привязано к подаркам: " + assigned
                + (skipped > 0 ? ", пропущено: " + skipped : ""));
    }

    showGiftCatalog();
}

private void chooseSoundsFolder() {
    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

    try {
        startActivityForResult(intent, PICK_SOUNDS_FOLDER);
    } catch (Exception e) {
        message("Не удалось открыть выбор папки");
    }
}

private int countLibrarySounds() {
    int total = 0;
    String[] categories = {"memes", "funny", "melodies", "effects"};

    for (String category : categories) {
        total += getLibrarySounds(category).length();
    }

    return total;
}

private int importSoundsFolder(Uri treeUri) {
    JSONArray target;

    try {
        target = new JSONArray(
                prefs.getString(LIBRARY_MEMES, "[]"));
    } catch (Exception e) {
        target = new JSONArray();
    }

    int added = collectAudioFiles(
            treeUri,
            android.provider.DocumentsContract.getTreeDocumentId(treeUri),
            "",
            target);

    if (added > 0) {
        prefs.edit()
                .putString(LIBRARY_MEMES, target.toString())
                .apply();
    }

    return added;
}

private int collectAudioFiles(
        Uri treeUri, String parentId, String parentName, JSONArray target) {

    int added = 0;

    Uri childrenUri =
            android.provider.DocumentsContract
                    .buildChildDocumentsUriUsingTree(treeUri, parentId);

    try (android.database.Cursor cursor =
                 getContentResolver().query(
                         childrenUri,
                         new String[]{
                                 android.provider.DocumentsContract
                                         .Document.COLUMN_DOCUMENT_ID,
                                 android.provider.DocumentsContract
                                         .Document.COLUMN_DISPLAY_NAME,
                                 android.provider.DocumentsContract
                                         .Document.COLUMN_MIME_TYPE
                         },
                         null, null, null)) {

        if (cursor == null) return added;

        while (cursor.moveToNext()) {
            String id = cursor.getString(0);
            String name = cursor.getString(1);
            String mime = cursor.getString(2);

            if (android.provider.DocumentsContract.Document.MIME_TYPE_DIR
                    .equals(mime)) {
                added += collectAudioFiles(
                        treeUri,
                        id,
                        name == null ? parentName : name,
                        target);
                continue;
            }

            if (mime == null || !mime.startsWith("audio/")) {
                continue;
            }

            String uri = android.provider.DocumentsContract
                    .buildDocumentUriUsingTree(treeUri, id)
                    .toString();

            // Название папки тоже учитываем: LGS/Роза/1.mp3 — это Роза.
            String giftKey = matchGiftKey(
                    (parentName == null ? "" : parentName) + " "
                            + (name == null ? "" : name));

            if (giftKey != null
                    && !prefs.contains("gift_sound_" + giftKey)) {

                prefs.edit()
                        .putString("gift_sound_" + giftKey, uri)
                        .apply();

                autoAssignedCount++;
                continue;
            }

            JSONObject item = new JSONObject();

            try {
                item.put("name", name == null ? "Звук" : name);
                item.put("uri", uri);
                target.put(item);
                added++;
            } catch (Exception ignored) {
            }
        }
    } catch (Exception ignored) {
    }

    return added;
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
    
private boolean importMediaFile(Uri uri) {
    if (uri == null) return false;

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

    return copyMediaToApp(uri, name, mime);
}

private int importMediaFolder(Uri treeUri) {
    return importMediaFolderContents(treeUri,
            android.provider.DocumentsContract.getTreeDocumentId(treeUri));
}

private int importMediaFolderContents(Uri treeUri, String parentId) {
    int imported = 0;

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

        if (cursor == null) return imported;

        while (cursor.moveToNext()) {
            String id = cursor.getString(0);
            String name = cursor.getString(1);
            String mime = cursor.getString(2);

            if (android.provider.DocumentsContract.Document.MIME_TYPE_DIR
                    .equals(mime)) {
                imported += importMediaFolderContents(treeUri, id);
            } else {
                Uri fileUri =
                        android.provider.DocumentsContract.buildDocumentUriUsingTree(
                                treeUri, id);
                if (copyMediaToApp(fileUri, name, mime)) {
                    imported++;
                }
            }
        }
    } catch (Exception e) {
        message("Ошибка чтения папки: " + e.getMessage());
    }

    return imported;
}

private boolean copyMediaToApp(Uri uri, String name, String mime) {
    java.io.File directory =
            new java.io.File(getFilesDir(), "media_library");

    if (!directory.exists() && !directory.mkdirs()) {
        message("Не удалось создать хранилище");
        return false;
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
            return false;
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

        lastImportedFileName = storedName;

        return true;

    } catch (Exception e) {
        if (destination.exists()) destination.delete();
        message("Ошибка сохранения: " + safeName);
        return false;
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
        ArrayList<String[]> gifts = allGifts();
        String[] giftNames = new String[gifts.size()];

        for (int i = 0; i < gifts.size(); i++) {
            giftNames[i] = gifts.get(i)[1];
        }

        new AlertDialog.Builder(this)
                .setTitle("Выбери подарок")
                .setItems(giftNames, (gd, giftIndex) -> {
                    String giftKey = gifts.get(giftIndex)[0];

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

        android.graphics.Bitmap bitmap =
                decodeSampledBitmap(uri, 1080, 1080);

        if (bitmap != null) {
            image.setImageBitmap(bitmap);
        }

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
intent.putExtra("overlay_text", "🎁 ТЕСТ ОВЕРЛЕЯ!");

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

        if (APP_KEY.isEmpty()) {
            message("Отправка заказа на сервер не настроена в этой сборке");
            return;
        }

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
                            giftIncome += amount;
                            prefs.edit()
                                    .putInt("gifts", gifts)
                                    .putInt("giftIncome", giftIncome)
                                    .apply();
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
        giftIncome = prefs.getInt("giftIncome", 0);
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
                        + "🎁 Сумма подарков: " + giftIncome + " ₽\n"
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
                                + "🎁 Сумма подарков: "
                                + giftIncome + " ₽\n"
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
                    giftIncome = 0;
                    orders = 0;

                    prefs.edit()
                            .putInt("gifts", 0)
                            .putInt("donations", 0)
                            .putInt("donationIncome", 0)
                            .putInt("giftIncome", 0)
                            .putInt("orders", 0)
                            .putString("orders_history", "[]")
                            .apply();

                    updateStats();
                    message("Статистика сброшена");
                })
                .show();
    }

    
    private void message(String text) {
        // Импорт медиа идёт в фоновом потоке, а Toast можно показывать
        // только из потока с Looper.
        if (android.os.Looper.myLooper()
                == android.os.Looper.getMainLooper()) {
            Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        } else {
            runOnUiThread(() ->
                    Toast.makeText(this, text, Toast.LENGTH_LONG).show());
        }
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
            .setNeutralButton("🩺 Диагностика", (d, w) ->
                    showConnectionDiagnostics())
            .show();
}

    // ------------------------------------------------------------------
    // Диагностика соединения: одна кнопка, один экран.
    // Видно, жив ли сокет, в эфире ли TikTok и доходят ли события.
    // ------------------------------------------------------------------
    private void showConnectionDiagnostics() {
        message("Проверяю соединение…");

        executor.execute(() -> {
            String report;

            try {
                report = buildDiagnosticsReport();
            } catch (Exception error) {
                report = "Не удалось собрать отчёт: " + error;
            }

            final String text = report;

            runOnUiThread(() -> showDiagnosticsDialog(text));
        });
    }

    private String buildDiagnosticsReport() {
        StringBuilder out = new StringBuilder();
        long now = System.currentTimeMillis();

        out.append("ПРИЛОЖЕНИЕ\n");

        boolean serviceAlive = OverlayService.serviceStartedAt > 0;

        out.append("Сервис: ")
                .append(serviceAlive ? "✅ запущен" : "❌ не запущен")
                .append('\n');

        if (serviceAlive) {
            out.append("Работает: ")
                    .append(ago(now - OverlayService.serviceStartedAt))
                    .append('\n');
        }

        out.append("Сокет: ")
                .append(OverlayService.wsConnected
                        ? "✅ подключён"
                        : "❌ нет связи")
                .append('\n');

        if (!OverlayService.lastSocketError.isEmpty()) {
            out.append("Ошибка: ")
                    .append(OverlayService.lastSocketError)
                    .append('\n');
        }

        out.append("Последнее событие: ");

        if (OverlayService.lastEventAt == 0) {
            out.append("не приходило\n");
        } else {
            out.append(OverlayService.lastEventType)
                    .append(" (")
                    .append(ago(now - OverlayService.lastEventAt))
                    .append(" назад)\n");
        }

        out.append("\nСЕРВЕР\n");

        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) new URL(
                    "https://livegiftstudio.onrender.com/health")
                    .openConnection();
            connection.setConnectTimeout(20000);
            connection.setReadTimeout(20000);

            int code = connection.getResponseCode();

            if (code != 200) {
                out.append("Ответ: HTTP ").append(code).append('\n');
                return out.toString();
            }

            JSONObject json = new JSONObject(
                    readStream(connection.getInputStream()));

            JSONObject tiktok = json.optJSONObject("tiktok");
            JSONObject events = json.optJSONObject("events");

            out.append("Доступен: ✅, работает ")
                    .append(json.optInt("uptimeSeconds") / 60)
                    .append(" мин\n");

            if (tiktok != null) {
                out.append("TikTok @")
                        .append(tiktok.optString("username"))
                        .append(": ")
                        .append(tiktok.optBoolean("connected")
                                ? "✅ в эфире"
                                : "❌ не в эфире")
                        .append('\n');

                String serverError = tiktok.optString("lastError", "");

                if (!serverError.isEmpty()) {
                    out.append("Ошибка TikTok: ")
                            .append(serverError)
                            .append('\n');
                }

                if (tiktok.optBoolean("connecting")) {
                    out.append("Сейчас подключается…\n");
                }

                if (!tiktok.optBoolean("apiKey", true)) {
                    out.append("Ключ EulerStream: ❌ не задан\n");
                }

                String limitedUntil =
                        tiktok.optString("rateLimitedUntil", "");

                if (!limitedUntil.isEmpty()
                        && !"null".equals(limitedUntil)) {
                    out.append("Пауза после лимита до: ")
                            .append(limitedUntil)
                            .append('\n');
                }
            }

            out.append("Клиентов на сервере: ")
                    .append(json.optInt("websocketClients"))
                    .append('\n');

            if (events != null) {
                String lastAt = events.optString("lastAt", "");

                out.append("Событий передано: ")
                        .append(events.optInt("forwarded"))
                        .append('\n');

                out.append("Последнее на сервере: ")
                        .append(lastAt.isEmpty()
                                ? "не было"
                                : events.optString("lastType")
                                        + " в " + lastAt)
                        .append('\n');
            }
        } catch (Exception error) {
            out.append("❌ Нет связи с сервером\n")
                    .append(String.valueOf(error))
                    .append('\n');
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return out.toString();
    }

    private String readStream(java.io.InputStream stream) throws Exception {
        StringBuilder body = new StringBuilder();

        try (java.io.BufferedReader reader =
                     new java.io.BufferedReader(
                             new java.io.InputStreamReader(
                                     stream, StandardCharsets.UTF_8))) {
            String line;

            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
        }

        return body.toString();
    }

    private String ago(long millis) {
        long seconds = Math.max(0, millis / 1000);

        if (seconds < 60) {
            return seconds + " с";
        }

        long minutes = seconds / 60;

        if (minutes < 60) {
            return minutes + " мин";
        }

        return (minutes / 60) + " ч " + (minutes % 60) + " мин";
    }

    private void showDiagnosticsDialog(String report) {
        TextView view = new TextView(this);
        view.setText(report);
        view.setTextSize(12);
        view.setTextColor(0xFF1A2233);
        view.setTypeface(Typeface.MONOSPACE);
        view.setTextIsSelectable(true);
        view.setPadding(dp(16), dp(12), dp(16), dp(12));

        ScrollView scroll = new ScrollView(this);
        scroll.addView(view);

        new AlertDialog.Builder(this)
                .setTitle("🩺 Диагностика соединения")
                .setView(scroll)
                .setPositiveButton("🔄 Обновить", (d, w) ->
                        showConnectionDiagnostics())
                .setNeutralButton("🎁 Тест подарка", (d, w) ->
                        sendTestGift())
                .setNegativeButton("Закрыть", null)
                .show();
    }

    private void sendTestGift() {
        executor.execute(() -> {
            HttpURLConnection connection = null;

            try {
                connection = (HttpURLConnection) new URL(
                        "https://livegiftstudio.onrender.com/test-gift")
                        .openConnection();
                connection.setRequestProperty(
                        "x-switch-key", ACCOUNT_SWITCH_KEY);
                connection.setConnectTimeout(20000);
                connection.setReadTimeout(20000);

                int code = connection.getResponseCode();

                runOnUiThread(() -> message(code == 200
                        ? "Тестовый подарок отправлен — жди анимацию"
                        : "Сервер ответил: " + code));
            } catch (Exception error) {
                runOnUiThread(() ->
                        message("Не удалось отправить тест"));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
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
   private static final String ACCOUNT_SWITCH_KEY = BuildConfig.SWITCH_KEY;
private void switchServerAccount(String username) {
    if (ACCOUNT_SWITCH_KEY.isEmpty()) {
        message("Переключение аккаунта не настроено в этой сборке");
        return;
    }

    executor.execute(() -> {
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
                    "Content-Type",
                    "application/json; charset=UTF-8"
            );

            connection.setRequestProperty(
                    "x-switch-key",
                    ACCOUNT_SWITCH_KEY
            );

            JSONObject json = new JSONObject();
            json.put("username", username);

            try (OutputStream output = connection.getOutputStream()) {
                output.write(
                        json.toString().getBytes(StandardCharsets.UTF_8)
                );
            }

            int status = connection.getResponseCode();

            runOnUiThread(() -> {
                if (status >= 200 && status < 300) {
                    // Аккаунт считаем активным только если сервер
                    // действительно принял переключение.
                    prefs.edit()
                            .putString("active_tiktok_account", username)
                            .putString("tiktok_username", username)
                            .apply();

                    message("Сервер переключён на @" + username);
                } else {
                    message("Ошибка переключения сервера. Код: " + status);
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
    });
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
}
@Override
protected void onStop() {
    // При уходе с экрана прекращаем только предпрослушивание в настройках.
    // Озвучку чата выключать нельзя: пользователь как раз сворачивает
    // приложение, чтобы открыть TikTok и вести эфир.
    stopPreview();

    if (voicePreview != null) {
        try {
            voicePreview.stop();
        } catch (Exception ignored) {}
    }

    super.onStop();
}
    
    @Override
    protected void onDestroy() {
        stopPreview();
        stopSfxPreview();

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
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("selected_gift_key", selectedGiftKey);
        outState.putString("selected_gift_name", selectedGiftName);
        outState.putString("library_category", libraryCategory);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        selectedGiftKey = savedInstanceState.getString(
                "selected_gift_key");
        selectedGiftName = savedInstanceState.getString(
                "selected_gift_name");
        libraryCategory = savedInstanceState.getString(
                "library_category");
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
