тоpackage com.livegiftstudio;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.content.SharedPreferences;
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
private SharedPreferences prefs;
private TextView statsText;

private int gifts;
private int donations;
private int orders;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    prefs = getSharedPreferences("LiveGiftStats", MODE_PRIVATE);
    gifts = prefs.getInt("gifts", 0);
    donations = prefs.getInt("donations", 0);
    orders = prefs.getInt("orders", 0);

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
    addSpace(20);

    addText("📊 ТВОЯ СТАТИСТИКА", 22, Color.WHITE, true);

    statsText = new TextView(this);
    statsText.setTextColor(Color.WHITE);
    statsText.setTextSize(18);
    statsText.setPadding(18, 18, 18, 18);
    statsText.setBackground(round(Color.rgb(37, 37, 58), 18));
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

    addButton("⭐ +1 ЗАКАЗ", v -> {
        orders++;
        saveStats();
        updateStats();
    });

    addButton("↩️ СБРОСИТЬ СТАТИСТИКУ", v -> {
        gifts = 0;
        donations = 0;
        orders = 0;
        saveStats();
        updateStats();
        showMessage("Статистика сброшена");
    });

    addSpace(22);
    addText("💎 ПРАЙС-ЛИСТ", 22, Color.WHITE, true);
    addText("🤖 ИИ-ФОТО — 50 ₽", 18, Color.WHITE, true);

addButton("🛒 ЗАКАЗАТЬ ИИ-ФОТО", v -> {
    orders++;
    saveStats();
    updateStats();
    openOrder("Здравствуйте! Хочу заказать ИИ-фото за 50 ₽.");
});

addSpace(10);

addText("⭐ ОЦЕНКА АККАУНТА — 50 ₽", 18, Color.WHITE, true);

addButton("🛒 ЗАКАЗАТЬ ОЦЕНКУ", v -> {
    orders++;
    saveStats();
    updateStats();
    openOrder("Здравствуйте! Хочу заказать оценку TikTok-аккаунта за 50 ₽.");
});

addSpace(10);

addText("📢 РЕКЛАМА — 100 ₽", 18, Color.WHITE, true);

addButton("🛒 ЗАКАЗАТЬ РЕКЛАМУ", v -> {
    orders++;
    saveStats();
    updateStats();
    openOrder("Здравствуйте! Хочу заказать рекламу за 100 ₽.");
});
    addSpace(18);

    addButton("💖 ПОДДЕРЖАТЬ ДОНАТОМ", v -> {
        Intent intent = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.donationalerts.com/r/vladdonat99")
        );
        startActivity(intent);
    });

    addSpace(18);
    addText("Подписывайся • Заказывай • Поддерживай",
            13, Color.LTGRAY, false);

    updateStats();
}

private void updateStats() {
    statsText.setText(
            "🎁 Подарки: " + gifts + "\n\n"
            + "💰 Донаты: " + donations + "\n\n"
            + "⭐ Заказы: " + orders
    );
}

private void saveStats() {
    prefs.edit()
            .putInt("gifts", gifts)
            .putInt("donations", donations)
            .putInt("orders", orders)
            .apply();
}

private void addButton(String title, View.OnClickListener listener) {
    Button button = new Button(this);
    button.setText(title);
    button.setTextSize(15);
    button.setTextColor(Color.WHITE);
    button.setAllCaps(false);
    button.setBackground(round(Color.rgb(105, 75, 220), 14));

    LinearLayout.LayoutParams params = matchWrap();
    params.setMargins(0, 5, 0, 9);
    container.addView(button, params);

    button.setOnClickListener(listener);
}

private void addText(String text, int size, int color, boolean bold) {
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

private void openOrder(String message) {
try {
Intent intent = new Intent(
Intent.ACTION_VIEW,
Uri.parse("https://t.me/share/url?url=&text="
+ Uri.encode(message))
);
startActivity(intent);
} catch (Exception e) {
showMessage("Не удалось открыть отправку заказа");
}
}

private void showMessage(String message) {
Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
}
}
