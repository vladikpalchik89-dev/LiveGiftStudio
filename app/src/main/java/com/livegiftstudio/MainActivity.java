package com.livegiftstudio;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends Activity {

    private static final String DONATE_URL =
            "https://www.donationalerts.com/r/vladdonat99";

    private SharedPreferences prefs;
    private LinearLayout container;
    private TextView statsText;

    private int gifts;
    private int donations;
    private int donationIncome;
    private int orders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("LiveGiftStudio", MODE_PRIVATE);

        gifts = prefs.getInt("gifts", 0);
        donations = prefs.getInt("donations", 0);
        donationIncome = prefs.getInt("donationIncome", 0);
        orders = prefs.getInt("orders", 0);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.rgb(247, 245, 252));

        container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(24, 24, 24, 24);

        scroll.addView(container);
        setContentView(scroll);

        addText("LiveGiftStudio PRO", 28, 0xFF7341DC, true);
        addText("Помощник для TikTok LIVE", 16, Color.DKGRAY, false);

        addSpace(16);

        addButton("💜 Открыть донаты", v -> openLink(DONATE_URL));

        addSpace(12);
        addText("УСЛУГИ", 21, 0xFF302044, true);

        addButton("📸 ИИ-фото — 50 ₽",
                v -> createOrder("ИИ-фото", 50));

        addButton("⭐ Оценка аккаунта — 50 ₽",
                v -> createOrder("Оценка аккаунта", 50));

        addButton("📢 Реклама — 100 ₽",
                v -> createOrder("Реклама", 100));

        addButton("💬 Другая услуга",
                v -> createOrder("Другая услуга", 0));

        addSpace(18);
        addText("МОЯ СТАТИСТИКА", 21, 0xFF302044, true);

        statsText = new TextView(this);
        statsText.setTextSize(17);
        statsText.setTextColor(Color.DKGRAY);
        statsText.setPadding(16, 16, 16, 16);
        statsText.setBackground(background(Color.WHITE));

        container.addView(statsText, matchWrap());
        updateStats();

        addButton("🎁 Учесть подарок", v -> {
            gifts++;
            saveStats();
            updateStats();
            message("Подарок учтён");
        });

        addButton("💰 Добавить донат", v -> addDonation());

        addButton("📋 История заказов", v -> showHistory());

        addButton("🔄 Сбросить статистику", v -> resetStats());

        addSpace(12);
        addText(
                "Подарки и донаты учитываются вручную. " +
                "Автоматического подключения к TikTok нет.",
                13,
                Color.GRAY,
                false
        );
    }

    private void createOrder(String service, int defaultPrice) {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(8, 8, 8, 8);

        EditText client = new EditText(this);
        client.setHint("Твой ник в TikTok или Telegram");
        form.addView(client);

        EditText description = new EditText(this);
        description.setHint("Опиши, что нужно сделать");
        description.setMinLines(2);
        form.addView(description);

        EditText price = null;

        if (defaultPrice == 0) {
            price = new EditText(this);
            price.setHint("Стоимость в рублях");
            price.setInputType(InputType.TYPE_CLASS_NUMBER);
            form.addView(price);
        }

        final EditText priceField = price;

        new AlertDialog.Builder(this)
                .setTitle("Заказ: " + service)
                .setMessage(defaultPrice > 0
                        ? "Стоимость: " + defaultPrice + " ₽"
                        : "Укажи стоимость услуги")
                .setView(form)
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Продолжить", (dialog, which) -> {
                    String name = client.getText().toString().trim();
                    String details = description.getText().toString().trim();

                    if (name.isEmpty() || details.isEmpty()) {
                        message("Заполни все поля");
                        return;
                    }

                    int amount = defaultPrice;

                    if (priceField != null) {
                        try {
                            amount = Integer.parseInt(
                                    priceField.getText().toString().trim());

                            if (amount < 0) {
                                message("Укажи правильную стоимость");
                                return;
                            }
                        } catch (Exception e) {
                            message("Введи стоимость цифрами");
                            return;
                        }
                    }

                    saveOrder(service, amount, name, details);

                    String text = "Здравствуйте! Хочу заказать услугу.\n"
                            + "Услуга: " + service + "\n"
                            + "Цена: " + amount + " ₽\n"
                            + "Мой ник: " + name + "\n"
                            + "Описание: " + details;

                    openTelegram(text);
                    message("Заказ сохранён");
                })
                .show();
    }

    private void openTelegram(String text) {
        try {
            String url = "https://t.me/share/url?url=&text="
                    + Uri.encode(text);

            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            message("Не удалось открыть Telegram");
        }
    }

    private void openLink(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            message("Не удалось открыть ссылку");
        }
    }

    private void saveOrder(
            String service, int price, String client, String description) {
        try {
            JSONArray history = new JSONArray(
                    prefs.getString("history", "[]"));

            JSONObject item = new JSONObject();
            item.put("service", service);
            item.put("price", price);
            item.put("client", client);
            item.put("description", description);
            item.put("date", System.currentTimeMillis());

            history.put(item);

            prefs.edit()
                    .putString("history", history.toString())
                    .apply();

            orders++;
            saveStats();
            updateStats();
        } catch (Exception e) {
            message("Ошибка сохранения заказа");
        }
    }

    private void showHistory() {
        try {
            JSONArray history = new JSONArray(
                    prefs.getString("history", "[]"));

            StringBuilder result = new StringBuilder();
            int total = 0;

            for (int i = history.length() - 1; i >= 0; i--) {
                JSONObject item = history.getJSONObject(i);

                result.append(item.optString("service"))
                        .append(" — ")
                        .append(item.optInt("price"))
                        .append(" ₽\n")
                        .append("Клиент: ")
                        .append(item.optString("client"))
                        .append("\n")
                        .append(item.optString("description"))
                        .append("\n\n");

                total += item.optInt("price");
            }

            if (history.length() == 0) {
                result.append("Заказов пока нет.");
            } else {
                result.append("Сумма заказов: ")
                        .append(total).append(" ₽");
            }

            new AlertDialog.Builder(this)
                    .setTitle("История заказов")
                    .setMessage(result.toString())
                    .setPositiveButton("Закрыть", null)
                    .show();

        } catch (Exception e) {
            message("Не удалось открыть историю");
        }
    }

    private void addDonation() {
        EditText input = new EditText(this);
        input.setHint("Сумма доната в рублях");
        input.setInputType(InputType.TYPE_CLASS_NUMBER);

        new AlertDialog.Builder(this)
                .setTitle("Добавить донат")
                .setView(input)
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    try {
                        int amount = Integer.parseInt(
                                input.getText().toString().trim());

                        if (amount <= 0) {
                            message("Сумма должна быть больше нуля");
                            return;
                        }

                        donations++;
                        donationIncome += amount;

                        saveStats();
                        updateStats();
                        message("Донат учтён");

                    } catch (Exception e) {
                        message("Введи сумму цифрами");
                    }
                })
                .show();
    }

    private void updateStats() {
        if (statsText != null) {
            statsText.setText(
                    "🎁 Подарки: " + gifts
                    + "\n\n💰 Донаты: " + donations
                    + "\n\n💵 Сумма донатов: " + donationIncome + " ₽"
                    + "\n\n⭐ Заказы: " + orders
            );
        }
    }

    private void saveStats() {
        prefs.edit()
                .putInt("gifts", gifts)
                .putInt("donations", donations)
                .putInt("donationIncome", donationIncome)
                .putInt("orders", orders)
                .apply();
    }

    private void resetStats() {
        new AlertDialog.Builder(this)
                .setTitle("Сбросить статистику?")
                .setMessage("История заказов останется.")
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

    private void addButton(String title, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(title);
        button.setTextSize(16);
        button.setTextColor(Color.WHITE);
        button.setAllCaps(false);
        button.setBackground(background(0xFF7341DC));

        LinearLayout.LayoutParams params = matchWrap();
        params.setMargins(0, 5, 0, 8);

        container.addView(button, params);
        button.setOnClickListener(listener);
    }

    private void addText(
            String text, int size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(color);

        if (bold) {
            view.setTypeface(null, Typeface.BOLD);
        }

        LinearLayout.LayoutParams params = matchWrap();
        params.setMargins(0, 0, 0, 8);
        container.addView(view, params);
    }

    private void addSpace(int height) {
        View space = new View(this);
        container.addView(
                space,
                new LinearLayout.LayoutParams(1, height));
    }

    private GradientDrawable background(int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(18);
        return drawable;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private void message(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
                    }
