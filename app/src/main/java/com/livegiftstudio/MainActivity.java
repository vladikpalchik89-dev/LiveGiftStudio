
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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.drawable.GradientDrawable;
import org.json.JSONArray;
import org.json.JSONObject;

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

    private TextToSpeech voicePreview;
    private boolean voicePreviewReady = false;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private SharedPreferences prefs;
    private LinearLayout container;
    private TextView statsText;
    private MediaPlayer previewPlayer;

    private String selectedGiftKey;
    private String selectedGiftName;

    private int gifts;
    private int donations;
    private int donationIncome;
    private int orders;

    private final String[][] giftCatalog = {
            {"rose", "🌹 Роза"},
            {"heart", "❤️ Сердце"},
            {"finger_heart", "🫰 Сердечко пальцами"},
            {"tiktok", "🎵 TikTok"},
            {"coffee", "☕ Кофе"},
            {"ice_cream", "🍦 Мороженое"},
            {"perfume", "💐 Парфюм"},
            {"love_you", "💗 Люблю тебя"},
            {"gg", "🎮 GG"},
            {"donut", "🍩 Пончик"},
            {"sunglasses", "🕶️ Очки"},
            {"cap", "🧢 Кепка"},
            {"paper_crane", "🕊️ Журавлик"},
            {"swan", "🦢 Лебедь"},
            {"concert", "🎤 Концерт"},
            {"fireworks", "🎆 Фейерверк"},
            {"galaxy", "🌌 Галактика"},
            {"universe", "🌠 Вселенная"},
            {"lion", "🦁 Лев"},
            {"diamond", "💎 Алмаз"},
            {"crown", "👑 Корона"},
            {"unicorn", "🦄 Единорог"},
            {"tiger", "🐯 Тигр"},
            {"castle", "🏰 Замок"},
            {"sports_car", "🏎️ Спортивная машина"},
            {"yacht", "🛥️ Яхта"},
            {"dragon_flame", "🐉 Пламя дракона"},
            {"phoenix", "🔥 Феникс"},
            {"rocket", "🚀 Ракета"},
            {"planet", "🪐 Планета"},
            {"star", "⭐ Звезда"},
            {"lightning", "⚡ Молния"},
            {"football", "⚽ Футбольный мяч"},
            {"basketball", "🏀 Баскетбол"},
            {"cake", "🎂 Торт"},
            {"birthday", "🎉 Праздник"},
            {"balloon", "🎈 Шарик"},
            {"bouquet", "💐 Букет"},
            {"teddy_bear", "🧸 Мишка"},
            {"music", "🎶 Музыка"},
            {"microphone", "🎙️ Микрофон"},
            {"drum", "🥁 Барабан"},
            {"guitar", "🎸 Гитара"},
            {"motorcycle", "🏍️ Мотоцикл"},
            {"train", "🚂 Поезд"},
            {"airplane", "✈️ Самолёт"},
            {"house", "🏠 Дом"},
            {"ring", "💍 Кольцо"},
            {"watch", "⌚ Часы"},
            {"money", "💸 Деньги"},
            {"gold", "🪙 Золотая монета"},
            {"fire", "🔥 Огонь"},
            {"rainbow", "🌈 Радуга"},
            {"butterfly", "🦋 Бабочка"},
            {"flower", "🌸 Цветок"},
            {"cat", "🐱 Кот"},
            {"dog", "🐶 Собака"},
            {"rabbit", "🐰 Кролик"},
            {"panda", "🐼 Панда"},
            {"champagne", "🥂 Праздничный бокал"},
            {"trophy", "🏆 Кубок"},
            {"medal", "🏅 Медаль"},
            {"love", "💕 Любовь"},
            {"default", "🎁 Другой подарок"}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        "🎵\nПодключение\nTikTok LIVE", 0xFF087BFF,
        "💰\nОткрыть\nдонаты", 0xFF00D6B7,
        v -> showTikTokConnection(),
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

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1,
                visibleNames);

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
        if (previewPlayer == null) {
            return;
        }

        try {
            previewPlayer.setOnPreparedListener(null);
            previewPlayer.setOnCompletionListener(null);
            previewPlayer.setOnErrorListener(null);
            previewPlayer.release();
        } catch (Exception ignored) {}

        previewPlayer = null;
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
            startService(intent);
            message("Команда анимации отправлена");
        } catch (Exception e) {
            message("Не удалось запустить анимацию");
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
                        startService(serviceIntent);
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
}

