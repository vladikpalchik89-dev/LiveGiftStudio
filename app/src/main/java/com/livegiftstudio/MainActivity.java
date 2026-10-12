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

public class MainActivity extends Activity {

    private static final int PURPLE = Color.rgb(115, 65, 220);
    private static final int DARK = Color.rgb(30, 25, 45);
    private static final int BACKGROUND = Color.rgb(247, 245, 252);
    private static final int GREEN = Color.rgb(35, 150, 90);

    private static final String DONATE_URL =
            "https://www.donationalerts.com/r/vladdonat99";

    private LinearLayout container;
    private TextView statsText;

    private SharedPreferences prefs;

    private int gifts;
    private int donations;
    private int orders;
    private int donationIncome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("LiveGiftStudio", MODE_PRIVATE);

        gifts = prefs.getInt("gifts", 0);
        donations = prefs.getInt("donations", 0);
        orders = prefs.getInt("orders", 0);
        donationIncome = prefs.getInt("donationIncome", 0);

        createInterface();
    }

    private void createInterface() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(BACKGROUND);

        container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(20, 24, 20, 28);

        scrollView.addView(container);
        setContentView(scrollView);

        addText("🎬 LiveGiftStudio PRO", 27, PURPLE, true);
        addText("Твой помощник для TikTok LIVE", 15, DARK, false);

        addSpace(15);

        addButton("💜 Открыть страницу донатов", v -> {
            openLink(DONATE_URL);
        });

        addSpace(12);

        addText("💎 УСЛУГИ И ЗАКАЗЫ", 19, DARK, true);

        addButton("📸 ИИ-фото — 50 ₽", v -> {
            createOrder("ИИ-фото", 50);
        });

        addButton("⭐ Оценка аккаунта — 50 ₽", v -> {
            createOrder("Оценка аккаунта", 50);
        });

        addButton("📢 Реклама — 100 ₽", v -> {
            createOrder("Реклама", 100);
        });

        addButton("💬 Заказать другую услугу", v -> {
            createOrder("Другая услуга", 0);
        });

        addSpace(12);

        addText("📊 МОЯ СТАТИСТИКА", 19, DARK, true);

        statsText = new TextView(this);
        statsText.setTextSize(16);
        statsText.setTextColor(DARK);
        statsText.setPadding(16, 16, 16, 16);
        statsText.setBackground(round(Color.WHITE, 16));

        LinearLayout.LayoutParams statsParams = matchWrap();
        statsParams.setMargins(0, 6, 0, 10);
        container.addView(statsText,
