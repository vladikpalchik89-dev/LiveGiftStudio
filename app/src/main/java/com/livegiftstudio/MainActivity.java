
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
import android.text.TextWatcher;
import android.text.Editable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {

    private static final String DONATE_URL =
            "https://www.donationalerts.com/r/vladdonat99";

    private static final String SERVER_URL =
            "https://script.google.com/macros/s/AKfycbz9_7ApvKuZDZj8EGWNYvRaMxRi3DYQgsGigCrGLW3u9cthlqr7Q0FXBr8mwxxSms9S_A/exec";

    private static final String APP_KEY =
            "LGS_PRO_7mQ9vK2xR8pN4wT6zA1cF5hB";

    private static final int PICK_SOUND = 501;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private SharedPreferences prefs;
    private LinearLayout container;
    private TextView statsText;

    private int gifts;
    private int donations;
    private int donationIncome;
    private int orders;

    private String selectedGiftKey;
    private String selectedGiftName;

    private MediaPlayer previewPlayer;

    // Предварительно добавленный каталог подарков.
    // key используется для сохранения настроек звука.
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
            {"sunglasses", "🕶️ Солнцезащитные очки"},
            {"cap", "🧢 Кепка"},
            {"paper_crane", "🕊️ Бумажный журавлик"},
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
            {"basketball", "🏀 Баскетбольный мяч"},
            {"cake", "🎂 Торт"},
            {"birthday", "🎉 Праздник"},
            {"balloon", "🎈 Воздушный шар"},
            {"bouquet", "💐 Букет цветов"},
            {"teddy_bear", "🧸 Плюшевый мишка"},
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

        prefs = getSharedPreferences("LiveGiftStudio", MODE_PRIVATE);

        loadStats();
        buildInterface();
    }

    private void buildInterface() {
        ScrollView scrollView = new ScrollView(this);

        container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(24, 24, 24, 32);
        container.setBackgroundColor(Color.rgb(18, 18, 30));

        scrollView.addView(container);
        setContentView(scrollView);

        TextView title = new TextView(this);
        title.setText("🎬 LiveGiftStudio PRO");
        title.setTextSize(27);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 12, 0, 12);
        container.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Инструменты для TikTok LIVE");
        subtitle.setTextSize(16);
        subtitle.setTextColor(Color.LTGRAY);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 0, 0, 20);
        container.addView(subtitle);

        addButton("🎁 Подарки TikTok и мои звуки",
                v -> showGiftCatalog());

        addButton("🎬 Тест анимации поверх экрана",
                v -> startOverlay());

        addButton("💰 Открыть донаты",
                v -> openLink(DONATE_URL));

        addButton("🤖 ИИ-фото — 50 ₽",
                v -> createOrder("ИИ-фото", 50));

        addButton("⭐ Оценка аккаунта — 50 ₽",
                v -> createOrder("Оценка аккаунта", 50));

        addButton("📢 Реклама — 100 ₽",
                v -> createOrder("Реклама", 100));

        addButton("💬 Заказать другую услугу",
                v -> createOrder("Другая услуга", 0));

        addButton("📋 История заказов",
                v -> showHistory());

        addButton("📊 Статистика",
                v -> showStats());

        addButton("➕ Записать подарок",
                v -> addDonation("gift"));

        addButton("➕ Записать донат",
                v -> addDonation("donation"));

        addButton("🔄 Сбросить статистику",
                v -> resetStats());

        statsText = new TextView(this);
        statsText.setTextColor(Color.WHITE);
        statsText.setTextSize(16);
        statsText.setPadding(8, 24, 8, 12);
        container.addView(statsText);

        updateStats();
    }

    // Открывает список подарков с поиском.
    private void showGiftCatalog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 10, 20, 10);

        EditText search = new EditText(this);
        search.setHint("Найти подарок...");
        layout.addView(search);

        TextView hint = new TextView(this);
        hint.setText("Выбери подарок, затем назначь звук");
        hint.setTextColor(Color.DKGRAY);
        layout.addView(hint);

        ListView listView = new ListView(this);
        layout.addView(listView,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 550));

        ArrayList<String> allNames = new ArrayList<>();
        ArrayList<String> allKeys = new ArrayList<>();

        for (String[] gift : giftCatalog) {
            allKeys.add(gift[0]);
            allNames.add(gift[1]);
        }

        ArrayList<String> visibleNames = new ArrayList<>(allNames);
        ArrayList<String> visibleKeys = new ArrayList<>(allKeys);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, visibleNames);

        listView.setAdapter(adapter);

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

                String query = s.toString().toLowerCase().trim();

                visibleNames.clear();
                visibleKeys.clear();

                for (int i = 0; i < allNames.size(); i++) {
                    String name = allNames.get(i).toLowerCase();
                    String key = allKeys.get(i).toLowerCase();

                    if (name.contains(query) || key.contains(query)) {
                        visibleNames.add(allNames.get(i));
                        visibleKeys.add(allKeys.get(i));
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            selectedGiftKey = visibleKeys.get(position);
            selectedGiftName = visibleNames.get(position);
            dialog.dismiss();
            showGiftSoundOptions();
        });

        dialog.show();
    }

    // Меню настройки звука выбранного подарка.
    private void showGiftSoundOptions() {
        String preferenceKey = "gift_sound_" + selectedGiftKey;
        String savedUri = prefs.getString(preferenceKey, null);

        String status = savedUri == null
                ? "Свой звук пока не выбран"
                : "Свой звук уже назначен";

        new AlertDialog.Builder(this)
                .setTitle(selectedGiftName)
                .setMessage(status)
                .setPositiveButton("🎵 Выбрать звук с телефона",
                        (dialog, which) -> chooseSound())
                .setNeutralButton("▶ Проверить звук",
                        (dialog, which) -> previewSavedSound())
                .setNegativeButton("Сбросить звук",
                        (dialog, which) -> {
                            prefs.edit().remove(preferenceKey).apply();
                            message("Назначенный звук удалён");
                        })
                .show();
    }

    // Открывает системный выбор аудиофайла.
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

        if (requestCode == PICK_SOUND
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            Uri uri = data.getData();

            try {
                getContentResolver().takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (Exception ignored) {}

            if (selectedGiftKey == null) {
                message("Сначала выбери подарок");
                return;
            }

            prefs.edit()
                    .putString("gift_sound_" + selectedGiftKey,
                            uri.toString())
                    .apply();

            message("Звук сохранён для подарка: " + selectedGiftName);
        }
    }

    private void previewSavedSound() {
        if (selectedGiftKey == null) {
            message("Сначала выбери подарок");
            return;
        }

        String savedUri = prefs.getString(
                "gift_sound_" + selectedGiftKey, null);

        if (savedUri == null) {
            message("Сначала назначь звук этому подарку");
            return;
        }

        try {
            stopPreview();

            previewPlayer = new MediaPlayer();
            previewPlayer.setDataSource(
                    this, Uri.parse(savedUri));

            previewPlayer.setOnPreparedListener(player ->
                    player.start());

            previewPlayer.setOnCompletionListener(player -> stopPreview());

            previewPlayer.setOnErrorListener((player, what, extra) -> {
                stopPreview();
                message("Не удалось воспроизвести этот файл");
                return true;
            });

            previewPlayer.prepareAsync();

        } catch (Exception e) {
            stopPreview();
            message("Ошибка воспроизведения звука");
        }
    }

    private void stopPreview() {
        if (previewPlayer != null) {
            try {
                if (previewPlayer.isPlaying()) {
                    previewPlayer.stop();
                }
            } catch (Exception ignored) {}

            try {
                previewPlayer.release();
            } catch (Exception ignored) {}

            previewPlayer = null;
        }
    }

    private void startOverlay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !Settings.canDrawOverlays(this)) {

            new AlertDialog.Builder(this)
                    .setTitle("Разрешение на анимацию")
                    .setMessage("Разреши LiveGiftStudio показывать "
                            + "анимацию поверх других приложений. "
                            + "Затем вернись и нажми кнопку ещё раз.")
                    .setNegativeButton("Отмена", null)
                    .setPositiveButton("Настройки", (dialog, which) -> {
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

    private void addButton(
            String text, View.OnClickListener listener) {

        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(15);
        button.setAllCaps(false);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

        params.setMargins(0, 6, 0, 6);
        container.addView(button, params);
        button.setOnClickListener(listener);
    }

    private void createOrder(String service, int amount) {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(20, 10, 20, 10);

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
                        ? "Опиши, какую услугу хочешь заказать"
                        : "Стоимость: " + amount + " ₽")
                .setView(form)
                .setNegativeButton("Отмена", null)
                .setPositiveButton(
                        amount == 0 ? "Отправить заказ" : "Заказать",
                        (dialog, which) -> {

                    String name = nameInput.getText().toString().trim();
                    String details = detailsInput.getText().toString().trim();

                    if (name.isEmpty() || details.isEmpty()) {
                        message("Укажи ник и описание заказа");
                        return;
                    }

                    int price = amount;

                    if (finalPriceInput != null) {
                        try {
                            price = Integer.parseInt(
                                    finalPriceInput.getText().toString().trim());
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
            String service, int price, String client, String description) {

        return "Новый заказ LiveGiftStudio\n\n"
                + "Услуга: " + service + "\n"
                + "Цена: " + price + " ₽\n"
                + "Клиент: " + client + "\n"
                + "Описание: " + description;
    }

    private void sendOrderToTelegram(
            String service, int price, String client, String description) {

        executor.execute(() -> {
            HttpURLConnection connection = null;

            try {
                JSONObject data = new JSONObject();
                data.put("key", APP_KEY);
                data.put("service", service);
                data.put("price", price);
                data.put("client", client);
                data.put("description", description);

                connection = (HttpURLConnection)
                        new URL(SERVER_URL).openConnection();

                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(20000);
                connection.setRequestProperty(
                        "Content-Type", "application/json; charset=UTF-8");

                byte[] bytes = data.toString()
                        .getBytes(StandardCharsets.UTF_8);

                try (OutputStream output = connection.getOutputStream()) {
                    output.write(bytes);
                }

                int responseCode = connection.getResponseCode();

                runOnUiThread(() -> {
                    if (responseCode == 200 || resp
