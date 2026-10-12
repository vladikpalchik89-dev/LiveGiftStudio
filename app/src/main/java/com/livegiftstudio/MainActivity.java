package com.livegiftstudio;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    LinearLayout layout = new LinearLayout(this);
    layout.setOrientation(LinearLayout.VERTICAL);
    layout.setGravity(Gravity.CENTER);
    layout.setPadding(24, 24, 24, 24);
    layout.setBackgroundColor(Color.rgb(20, 20, 30));

    TextView title = new TextView(this);
    title.setText("LiveGiftStudio");
    title.setTextSize(30);
    title.setTextColor(Color.WHITE);
    title.setGravity(Gravity.CENTER);

    TextView subtitle = new TextView(this);
    subtitle.setText("Твоя студия TikTok LIVE");
    subtitle.setTextSize(18);
    subtitle.setTextColor(Color.LTGRAY);
    subtitle.setGravity(Gravity.CENTER);
    subtitle.setPadding(0, 20, 0, 0);

    layout.addView(title);
    layout.addView(subtitle);

    setContentView(layout);
}

}
