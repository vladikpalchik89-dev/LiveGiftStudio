package com.livegiftstudio;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends Activity {

    private LinearLayout container;
    private SharedPreferences prefs;
    private TextView statsText;

    private int gifts;
    private int donations;
    private int orders;
    private int donationIncome;

    private final int BG = Color.rgb(18, 18, 32);
    private final int CARD = Color.rgb(37, 37, 58);
    private final int PURPLE = Color.rgb(105, 75, 220);

    private final String DONATE_URL =
            "https://www.donationalerts.com/r/vladdonat99";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("LiveGiftStats", MODE_PRIVATE);

        gifts = prefs.getInt("gifts", 0);
        donations = prefs.getInt("donations", 0);
        orders = prefs.getInt("orders", 0);
        donationIncome = prefs.getInt("donationIncome", 0);

        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(BG);

        container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(20, 24, 20, 30);

        scroll.addView(container);
        setContentView(scroll);

        addText("💎 LiveGiftStudio PRO", 27, Color.WHITE, true);
        addText("Твоя студия TikTok LIVE", 16,
                Color.LTGRAY, false);

        addSpace(20);

        addText("📊 СТАТИСТИКА", 22, Color.WHITE, true);

        statsText = new TextView(this);
        statsText.setTextColor(Color.WHITE);
        statsText.setTextSize(17);
        statsText.setPadding(18, 18, 18, 18);
        statsText.setBackground(round(CARD, 18));
        container.addView(statsText, matchWrap());

        addSpace(12);

        addButton("🎁 +1 ПОДАРОК", v -> {
            gifts++;
            saveStats();
            updateStats();
        });

        addButton("💰 +1 ДОНАТ", v -> {
            donations++;
            saveStats();
            updateStats();
        });

        addButton("💵 ДОБАВИТЬ СУММУ ДОНАТА", v ->
                addDonationAmount());

        addSpace(20);

        addText("🛒 ЗАКАЗАТЬ УСЛУГУ", 22, Color.WHITE, true);

        addButton("🤖 ИИ-ФОТО — 50 ₽", v ->
                createOrder("ИИ-фото", 50));

        addButton("⭐ ОЦЕНКА АККАУНТА — 50 ₽", v ->
                createOrder("Оценка аккаунта", 50));

        addButton("📢 РЕКЛАМА — 100 ₽", v ->
                createOrder("Реклама", 100));

        addButton("📋 ИСТОРИЯ ЗАКАЗОВ", v ->
                showOrderHistory());

        addSpace(20);

        addText("💖 ДОНАТЫ", 22, Color.WHITE, true);

        addText("Поддержи эфир или отправь донат!",
                15, Color.LTGRAY, false);

        addButton("💖 ОТПРАВИТЬ ДОНАТ", v -> {
            Intent intent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(DONATE_URL)
            );
            startActivity(intent);
        });

        addSpace(20);

        addButton("↩️ СБРОСИТЬ СТАТИСТИКУ", v ->
                confirmReset());

        addSpace(15);

        addText("Подписывайся • Заказывай • Поддерживай",
                13, Color.LTGRAY, false);

        updateStats();
    }

    private void createOrder(String service, int price) {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(10, 10, 10, 10);

        EditText name = new EditText(this);
        name.setHint("Твой ник в TikTok");
        form.addView(name);

        EditText details = new EditText(this);
        details.setHint("Опиши заказ");
        details.setMinLines(2);
        details.setGravity(Gravity.TOP);
        form.addView(details);

        new AlertDialog.Builder(this)
                .setTitle(service + " — " + price + " ₽")
                .setMessage("Заполни данные для заказа")
                .setView(form)
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Создать заказ",
                        (dialog, which) -> {

            String client = name.getText().toString().trim();
            String description =
                    details.getText().toString().trim();

            if (client.isEmpty()) {
                client = "Не указан";
            }

            if (description.isEmpty()) {
                description = "Без описания";
            }

            saveOrder(service, price, client, description);

            String message =
                    "Здравствуйте! Хочу заказать: " + service
                    + "\nЦена: " + price + " ₽"
                    + "\nМой TikTok: " + client
                    + "\nОписание: " + description;

            showMessage("Заказ сохранён!");

            new AlertDialog.Builder(this)
                    .setTitle("Заказ создан")
                    .setMessage("Заказ сохранён в истории. "
                            + "Можно отправить его через Telegram.")
                    .setNegativeButton("Позже", null)
                    .setPositiveButton("Отправить",
                            (d, w) -> openOrder(message))
                    .show();
        })
        .show();
    }

    private void openOrder(String message) {
        try {
            Intent intent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://t.me/share/url?url=&text="
                            +
