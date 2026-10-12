package com.livegiftstudio;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.rgb(18, 18, 32));
        getWindow().setNavigationBarColor(Color.rgb(18, 18, 32));

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.rgb(18, 18, 32));

        container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(20, 24, 20, 30);

        scroll.addView(container);
        setContentView(scroll);

        addText("🎬 LiveGiftStudio PRO", 27, Color.WHITE, true);
        addText("Твоя студия TikTok LIVE", 16,
                Color.LTGRAY, false);

        addSpace(22);

        addCard("🎁 ПОДАРКИ LIVE",
                "Раздел управления подарками и событиями эфира",
                "Подарки");

        addCard("💰 ДОНАТЫ",
                "Информация о поддержке и способах оплаты",
                "Донаты");

        addCard("📊 СТАТИСТИКА",
                "Подарки, донаты и заказы",
                "Статистика");

        addCard("🤖 ИИ-ФОТО",
                "Заказ фотографии с любимым блогером или актёром",
                "ИИ-фото");

        addCard("⭐ ОЦЕНКА АККАУНТА",
                "Закажи оценку своего TikTok-аккаунта",
                "Оценка аккаунта");

        addCard("📢 РЕКЛАМА",
                "Заказ рекламы для твоего аккаунта",
                "Реклама");

        addSpace(18);

        addText("💎 ПРАЙС-ЛИСТ", 22, Color.WHITE, true);

        addText("⭐ Оценка аккаунта — 50 ₽\n\n"
                + "🤖 ИИ-фото — 50 ₽\n\n"
                + "📢 Реклама — 100 ₽",
                17, Color.WHITE, false);

        addSpace(20);

        Button donate = new Button(this);
        donate.setText("💖 ПОДДЕРЖАТЬ ДОНАТОМ");
        donate.setTextSize(16);
        donate.setTextColor(Color.WHITE);
        donate.setAllCaps(false);
        donate.setBackground(round(Color.rgb(220, 45, 120), 18));
        container.addView(donate, matchWrap());

        donate.setOnClickListener(v ->
                showMessage("Добавь свою ссылку на донат в настройках приложения."));

        addSpace(14);

        TextView
