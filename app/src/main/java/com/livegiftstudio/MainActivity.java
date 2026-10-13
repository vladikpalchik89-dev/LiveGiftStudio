
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

        loadStats();
        buildInterface();
    }

    private void buildInterface() {
        ScrollView scroll = new ScrollView(this);

        container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(24, 24, 24, 32);
        container.setBackgroundColor(Color.rgb(18, 18, 30));

        scroll.addView(container);
        setContentView(scroll);

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

        addButton("🎁 Подарки и мои звуки",
        v -> showGiftCatalog());

        addButton("🗣️ Настроить голос и озвучку",
        v -> showVoiceSettings());

        addButton("🔔 Выбрать звук подписки",
        v -> chooseFollowSound());

        addButton("🎬 Тест анимации",
        v -> startOverlay());

        addButton("💰 Открыть донаты",
                v -> openLink(DONATE_URL));

        addButton("🤖 ИИ-фото — 50 ₽",
                v -> createOrder("ИИ-фото", 50));

        addButton("⭐ Оценка аккаунта — 50 ₽",
                v -> createOrder("Оценка аккаунта", 50));

        addButton("📢 Реклама — 100 ₽",
                v -> createOrder("Реклама", 100));

        addButton("💬 Другая услуга",
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

    @Override
    protected void onDestroy() {
        stopPreview();
        executor.shutdown();
        super.onDestroy();
    }
}
