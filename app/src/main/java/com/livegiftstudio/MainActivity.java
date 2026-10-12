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

    addText("LiveGiftStudio PRO", 27, Color.WHITE, true);
    addText("Твоя студия TikTok LIVE", 16, Color.LTGRAY, false);

    addSpace(22);

    addCard("ПОДАРКИ LIVE",
            "Управление подарками и событиями эфира", "Подарки");

    addCard("ДОНАТЫ",
            "Информация о поддержке и оплате", "Донаты");

    addCard("СТАТИСТИКА",
            "Подарки, донаты и заказы", "Статистика");

    addCard("ИИ-ФОТО",
            "Заказ фотографий с помощью ИИ", "ИИ-фото");

    addCard("ОЦЕНКА АККАУНТА",
            "Закажи оценку своего TikTok-аккаунта", "Оценка");

    addCard("РЕКЛАМА",
            "Заказ рекламы для твоего аккаунта", "Реклама");

    addSpace(18);
    addText("ПРАЙС-ЛИСТ", 22, Color.WHITE, true);
    addText("Оценка аккаунта — 50 ₽\n\n"
            + "ИИ-фото — 50 ₽\n\n"
            + "Реклама — 100 ₽", 17, Color.WHITE, false);

    addSpace(20);

    Button donate = new Button(this);
    donate.setText("ПОДДЕРЖАТЬ ДОНАТОМ");
    donate.setTextSize(16);
    donate.setTextColor(Color.WHITE);
    donate.setAllCaps(false);
    donate.setBackground(round(Color.rgb(220, 45, 120), 18));
    container.addView(donate, matchWrap());

    donate.setOnClickListener(v -> {
try {
android.content.Intent intent = new android.content.Intent(
android.content.Intent.ACTION_VIEW,
android.net.Uri.parse("https://www.donationalerts.com/r/vladdonat99")
);
startActivity(intent);
} catch (Exception e) {
showMessage("Не удалось открыть страницу донатов");
}
});

    addSpace(14);
    addText("Подписывайся • Заказывай • Поддерживай",
            13, Color.LTGRAY, false);
}

private void addCard(String title, String description, String section) {
    LinearLayout card = new LinearLayout(this);
    card.setOrientation(LinearLayout.VERTICAL);
    card.setPadding(18, 16, 18, 16);
    card.setBackground(round(Color.rgb(37, 37, 58), 18));

    LinearLayout.LayoutParams params = matchWrap();
    params.setMargins(0, 0, 0, 14);
    container.addView(card, params);

    TextView heading = new TextView(this);
    heading.setText(title);
    heading.setTextColor(Color.WHITE);
    heading.setTextSize(19);
    heading.setTypeface(null, Typeface.BOLD);
    card.addView(heading, matchWrap());

    TextView detail = new TextView(this);
    detail.setText(description);
    detail.setTextColor(Color.LTGRAY);
    detail.setTextSize(14);

    LinearLayout.LayoutParams detailParams = matchWrap();
    detailParams.setMargins(0, 8, 0, 12);
    card.addView(detail, detailParams);

    Button button = new Button(this);
    button.setText("Открыть");
    button.setTextColor(Color.WHITE);
    button.setAllCaps(false);
    button.setBackground(round(Color.rgb(105, 75, 220), 12));
    card.addView(button, matchWrap());

    button.setOnClickListener(v ->
            showMessage("Раздел «" + section
                    + "». Подключение функций будет следующим этапом."));
}

private void addText(String text, int size, int color, boolean bold) {
    TextView view = new TextView(this);
    view.setText(text);
    view.setTextSize(size);
    view.setTextColor(color);
    view.setGravity(Gravity.START);

    if (bold) {
        view.setTypeface(null, Typeface.BOLD);
    }

    LinearLayout.LayoutParams params = matchWrap();
    params.setMargins(0, 0, 0, 8);
    container.addView(view, params);
}

private void addSpace(int height) {
    View space = new View(this);
    container.addView(space,
            new LinearLayout.LayoutParams(1, height));
}

private GradientDrawable round(int color, int radius) {
    GradientDrawable drawable = new GradientDrawable();
    drawable.setColor(color);
    drawable.setCornerRadius(radius);
    return drawable;
}

private LinearLayout.LayoutParams matchWrap() {
    return new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
}

private void showMessage(String message) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
}

                 }
