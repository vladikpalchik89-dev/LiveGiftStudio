package com.livegiftstudio;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {

    private static final String DONATE_URL =
            "https://www.donationalerts.com/r/vladdonat99";

    private static final String SERVER_URL =
            "https://script.google.com/macros/s/AKfycbx3Vqy2HgRFjirc0Ym1C82nzGeUDkSo8bPFK7lj0gZllFsAzQVLrjI4QVPKz9UGTv9LDQ/exec";

    private static final String APP_KEY =
            "LGS_PRO_7mQ9vK2xR8pN4wT6zA1cF5hB";

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private SharedPreferences prefs;

    private LinearLayout container;
    private TextView statsText;

    private int gifts = 0;
    private int donations = 0;
    private int donationIncome = 0;
    private int orders = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences(
                "LiveGiftStudio",
                MODE_PRIVATE
        );

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
        subtitle.setText(
                "Заказывай услуги для TikTok LIVE"
        );
        subtitle.setTextSize(16);
        subtitle.setTextColor(Color.LTGRAY);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 0, 0, 20);
        container.addView(subtitle);

        addButton(
                "💰 Открыть донаты",
                v -> openLink(DONATE_URL)
        );

        addButton(
                "🤖 ИИ-фото — 50 ₽",
                v -> createOrder("ИИ-фото", 50)
        );

        addButton(
                "⭐ Оценка аккаунта — 50 ₽",
                v -> createOrder("Оценка аккаунта", 50)
        );

        addButton(
                "📢 Реклама — 100 ₽",
                v -> createOrder("Реклама", 100)
        );

        addButton(
                "💬 Заказать другую услугу",
                v -> createOrder("Другая услуга", 0)
        );

        addButton(
                "📋 История заказов",
                v -> showHistory()
        );

        addButton(
                "📊 Статистика",
                v -> showStats()
        );

        addButton(
                "➕ Записать подарок",
                v -> addDonation("gift")
        );

        addButton(
                "➕ Записать донат",
                v -> addDonation("donation")
        );

        addButton(
                "🔄 Сбросить статистику",
                v -> resetStats()
        );

        statsText = new TextView(this);
        statsText.setTextColor(Color.WHITE);
        statsText.setTextSize(16);
        statsText.setPadding(8, 24, 8, 12);
        container.addView(statsText);

        updateStats();
    }

    private void addButton(String text, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(15);
        button.setAllCaps(false);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

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

        if (amount == 0) {
            EditText priceInput = new EditText(this);
            priceInput.setHint("Цена в рублях");
            priceInput.setInputType(2);
            form.addView(priceInput);

            new AlertDialog.Builder(this)
                    .setTitle("💬 Другая услуга")
                    .setMessage("Опиши, что нужно сделать")
                    .setView(form)
                    .setNegativeButton("Отмена", null)
                    .setPositiveButton("Отправить заказ", (dialog, which) -> {
                        String name = nameInput.getText().toString().trim();
                        String details = detailsInput.getText().toString().trim();
                        String priceText = priceInput.getText().toString().trim();

                        if (name.isEmpty()) {
                            message("Укажи свой ник");
                            return;
                        }

                        if (details.isEmpty()) {
                            message("Опиши услугу");
                            return;
                        }

                        int price = 0;

                        try {
                            price = Integer.parseInt(priceText);
                        } catch (Exception ignored) {
                        }

                        saveOrder(service, price, name, details);
                        sendOrderToTelegram(service, price, name, details);

                        openTelegram(
                                makeOrderText(service, price, name, details)
                        );
                    })
                    .show();

            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Заказ: " + service)
                .setMessage("Стоимость: " + amount + " ₽")
                .setView(form)
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Заказать", (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    String details = detailsInput.getText().toString().trim();

                    if (name.isEmpty()) {
                        message("Укажи свой ник");
                        return;
                    }

                    if (details.isEmpty()) {
                        message("Напиши описание заказа");
                        return;
                    }

                    saveOrder(service, amount, name, details);

                    sendOrderToTelegram(
                            service,
                            amount,
                            name,
                            details
                    );

                    openTelegram(
                            makeOrderText(service, amount, name, details)
                    );
                })
                .show();
    }

    private String makeOrderText(
            String service,
            int price,
            String client,
            String description
    ) {
        return "Новый заказ LiveGiftStudio\n\n"
                + "Услуга: " + service + "\n"
                + "Цена: " + price + " ₽\n"
                + "Клиент: " + client + "\n"
                + "Описание: " + description;
    }

    private void sendOrderToTelegram(
            String service,
            int price,
            String client,
            String description
    ) {
        executor.execute(() -> {
            HttpURLConnection connection = null;

            try {
                JSONObject data = new JSONObject();

                data.put("key", APP_KEY);
                data.put("service", service);
                data.put("price", price);
                data.put("client", client);
                data.put("description", description);

                URL url = new URL(SERVER_URL);

                connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(20000);

                connection.setRequestProperty(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                );

                byte[] bytes = data.toString()
                        .getBytes(StandardCharsets.UTF_8);

                try (OutputStream output = connection.getOutputStream()) {
                    output.write(bytes);
                }

                int responseCode = connection.getResponseCode();

                runOnUiThread(() -> {
                    if (responseCode == 200 || responseCode == 302) {
                        message("Запрос отправлен серверу");
                    } else {
                        message("Ошибка отправки: " + responseCode);
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        message("Не удалось отправить заказ на сервер")
                );
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
            intent.setPackage("org.telegram.messenger");
            intent.putExtra(Intent.EXTRA_TEXT, text);
            startActivity(intent);
        } catch (Exception e) {
            try {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, text);
                startActivity(
                        Intent.createChooser(intent, "Отправить заказ")
                );
            } catch (Exception ex) {
                message("Не удалось открыть отправку сообщения");
            }
        }
    }

    private void saveOrder(
            String service,
            int price,
            String name,
            String details
    ) {
        try {
            JSONArray array = new JSONArray(
                    prefs.getString("order_history", "[]")
            );

            JSONObject order = new JSONObject();
            order.put("service", service);
            order.put("price", price);
            order.put("name", name);
            order.put("details", details);
            order.put("time", System.currentTimeMillis());

            array.put(order);

            prefs.edit()
                    .putString("order_history", array.toString())
                    .apply();

            orders++;
            saveStats();
            updateStats();

            message("Заказ сохранён");

        } catch (Exception e) {
            message("Не удалось сохранить заказ");
        }
    }

    private void showHistory() {
        try {
            JSONArray array = new JSONArray(
                    prefs.getString("order_history", "[]")
            );

            if (array.length() == 0) {
                message("Заказов пока нет");
                return;
            }

            StringBuilder text = new StringBuilder();

            for (int i = array.length() - 1; i >= 0; i--) {
                JSONObject order = array.getJSONObject(i);

                text.append("Заказ №")
                        .append(i + 1)
                        .append("\n");

                text.append("Услуга: ")
                        .append(order.optString("service"))
                        .append("\n");

                text.append("Цена: ")
                        .append(order.optInt("price"))
                        .append(" ₽\n");

                text.append("Клиент: ")
                        .append(order.optString("name"))
                        .append("\n");

                text.append("Описание: ")
                        .append(order.optString("details"))
                        .append("\n\n");
            }

            new AlertDialog.Builder(this)
                    .setTitle("📋 История заказов")
                    .setMessage(text.toString())
                    .setPositiveButton("Закрыть", null)
                    .show();

        } catch (Exception e) {
            message("Не удалось открыть историю");
        }
    }

    private void addDonation(String type) {
        EditText input = new EditText(this);
        input.setHint("Сумма в рублях");
        input.setInputType(2);

        new AlertDialog.Builder(this)
                .setTitle(
                        type.equals("gift")
                                ? "Записать подарок"
                                : "Записать донат"
                )
                .setView(input)
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    String value = input.getText().toString().trim();

                    if (value.isEmpty()) {
                        message("Введи сумму");
                        return;
                    }

                    try {
                        int amount = Integer.parseInt(value);

                        if (amount < 0) {
                            message("Сумма не может быть отрицательной");
                            return;
                        }

                        if (type.equals("gift")) {
                            gifts++;
                        } else {
                            donations++;
                            donationIncome += amount;
                        }

                        saveStats();
                        updateStats();

                        message("Статистика обновлена");

                    } catch (Exception e) {
                        message("Введи корректную сумму");
                    }
                })
                .show();
    }

    private void showStats() {
        updateStats();

        new AlertDialog.Builder(this)
                .setTitle("📊 Статистика")
                .setMessage(
                        "Подарков записано: " + gifts + "\n"
                        + "Донатов записано: " + donations + "\n"
                        + "Сумма донатов: " + donationIncome + " ₽\n"
                        + "Заказов: " + orders
                )
                .setPositiveButton("Закрыть", null)
                .show();
    }

    private void updateStats() {
        if (statsText != null) {
            statsText.setText(
                    "📊 Твоя статистика\n\n"
                    + "🎁 Подарки: " + gifts + "\n"
                    + "💰 Донаты: " + donations + "\n"
                    + "💵 Сумма донатов: " + donationIncome + " ₽\n"
                    + "📋 Заказы: " + orders
            );
        }
    }

    private void loadStats() {
        gifts = prefs.getInt("gifts", 0);
        donations = prefs.getInt("donations", 0);
        donationIncome = prefs.getInt("donation_income", 0);
        orders = prefs.getInt("orders", 0);
    }

    private void saveStats() {
        prefs.edit()
                .putInt("gifts", gifts)
                .putInt("donations", donations)
                .putInt("donation_income", donationIncome)
                .putInt("orders", orders)
                .apply();
    }

    private void resetStats() {
        new AlertDialog.Builder(this)
                .setTitle("Сбросить статистику?")
                .setMessage(
                        "Показатели подарков, донатов и заказов будут обнулены. "
                        + "История заказов останется."
                )
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Сбросить", (dialog, which) -> {
                    gifts = 0;
                    donations = 0;
                    donationIncome = 0;
                    orders = 0;

                    saveStats();
                    updateStats();

                    message("Статистика сброшена");
                })
                .show();
    }

    private void openLink(String link) {
        try {
            Intent intent = new Intent(
                    Intent.ACTION_VIEW,
                    android.net.Uri.parse(link)
            );

            startActivity(intent);

        } catch (Exception e) {
            message("Не удалось открыть ссылку");
        }
    }

    private void message(String text) {
        Toast.makeText(
                this,
                text,
                Toast.LENGTH_LONG
        ).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
            }
