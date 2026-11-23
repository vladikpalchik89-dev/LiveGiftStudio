package com.livegiftstudio;

import android.app.Notification;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Превращает push-уведомления СберБанка о поступлении денег в донаты
 * LiveGiftStudio: оверлей, звук, озвучка и строка в цели сбора.
 *
 * Почему через уведомления: у Сбера нет публичного API для физлиц,
 * поэтому единственный способ узнать о входящем переводе на карту —
 * прочитать уведомление банковского приложения.
 *
 * Требует разрешения «Доступ к уведомлениям» (Настройки → Специальные
 * возможности → Доступ к уведомлениям → LiveGiftStudio).
 */
public class SberNotificationListener extends NotificationListenerService {

    private static final String TAG = "SberListener";
    private static final String PREFS = "LiveGiftStudio";

    /** СберБанк и старый СберБанк Онлайн. */
    private static final String[] SBER_PACKAGES = {
            "ru.sberbankmobile",
            "ru.sberbank.sberbank24"
    };

    /** Сумма: «1 000 ₽», «1 000,50 ₽», «500 руб.». */
    private static final Pattern AMOUNT = Pattern.compile(
            "(\\d{1,3}(?:[\\s\\u00A0\\u202F]\\d{3})*(?:[.,]\\d{1,2})?"
                    + "|\\d+(?:[.,]\\d{1,2})?)"
                    + "\\s*(?:₽|руб)");

    /** Отправитель: «от ИВАН И.», «от: Иван». */
    private static final Pattern SENDER = Pattern.compile(
            "(?:^|\\s)от[:\\s]+([А-ЯЁA-Z][А-ЯЁA-Zа-яёa-z\\s.\\-]{1,40})",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    private static final String[] BALANCE_MARKERS = {
            "баланс", "остаток", "доступно"
    };

    private static final String[] OUTGOING_MARKERS = {
            "списан", "оплат", "покупк", "перевод выполнен",
            "вы перевели", "исходящ", "комисси"
    };

    private final Set<String> seen = new LinkedHashSet<>();

    private long lastPostAt = 0L;
    private String lastFingerprint = "";

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        try {
            handle(sbn);
        } catch (Exception error) {
            Log.w(TAG, "не удалось разобрать уведомление", error);
        }
    }

    private void handle(StatusBarNotification sbn) {
        if (sbn == null || sbn.getNotification() == null) {
            return;
        }

        if (!isSber(sbn.getPackageName())) {
            return;
        }

        Notification notification = sbn.getNotification();

        if ((notification.flags & Notification.FLAG_GROUP_SUMMARY) != 0) {
            return;
        }

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        if (!prefs.getBoolean("sber_enabled", true)) {
            return;
        }

        if (!OverlayService.isRunning()) {
            return;
        }

        String text = extractText(notification);

        if (text.isEmpty()) {
            return;
        }

        String body = cutBalance(text);
        String lower = body.toLowerCase(new Locale("ru", "RU"));

        if (isOutgoing(lower)) {
            return;
        }

        if (prefs.getBoolean("sber_incoming_only", true)
                && !looksIncoming(lower)) {
            return;
        }

        double amount = parseAmount(body);

        if (amount <= 0) {
            return;
        }

        if (amount < prefs.getInt("sber_min_amount", 0)) {
            return;
        }

        long now = System.currentTimeMillis();
        String fingerprint = sbn.getPackageName() + "|" + body + "|" + amount;

        // Одно и то же уведомление может прийти несколько раз:
        // как обычный текст, как bigText и как summary группы.
        if (fingerprint.equals(lastFingerprint) && now - lastPostAt < 5000L) {
            return;
        }

        if (!seen.add(sbn.getKey() + "|" + sbn.getPostTime())) {
            return;
        }

        if (seen.size() > 200) {
            seen.clear();
        }

        lastFingerprint = fingerprint;
        lastPostAt = now;

        String donor = extractSender(body);

        Log.i(TAG, "донат со Сбера: " + amount + " ₽ от " + donor);

        OverlayService.handleSberDonation(amount, donor, "");
    }

    private boolean isSber(String packageName) {
        if (packageName == null) {
            return false;
        }

        for (String known : SBER_PACKAGES) {
            if (known.equals(packageName)) {
                return true;
            }
        }

        return packageName.contains("sberbank");
    }

    private boolean isOutgoing(String lower) {
        for (String marker : OUTGOING_MARKERS) {
            if (lower.contains(marker)) {
                return true;
            }
        }

        return false;
    }

    private boolean looksIncoming(String lower) {
        if (lower.contains("пополн")
                || lower.contains("зачисл")
                || lower.contains("поступл")
                || lower.contains("получен")
                || lower.contains("перевед")) {
            return true;
        }

        // «Перевод от Ивана» и просто «Вам перевод» — оба входящие.
        // Исходящие уже отсеяны isOutgoing().
        return lower.contains("перевод");
    }

    /**
     * В push Сбера после суммы идёт «Баланс: … ₽». Без обрезки баланс
     * легко принять за сумму доната, поэтому режем строку по маркеру.
     */
    private String cutBalance(String text) {
        String lower = text.toLowerCase(new Locale("ru", "RU"));
        int cut = -1;

        for (String marker : BALANCE_MARKERS) {
            int index = lower.indexOf(marker);

            if (index > 0 && (cut < 0 || index < cut)) {
                cut = index;
            }
        }

        return cut > 0 ? text.substring(0, cut) : text;
    }

    private double parseAmount(String text) {
        Matcher matcher = AMOUNT.matcher(text);

        if (!matcher.find()) {
            return 0;
        }

        String raw = matcher.group(1)
                .replaceAll("[\\s\\u00A0\\u202F]", "")
                .replace(',', '.');

        try {
            return Double.parseDouble(raw);
        } catch (Exception error) {
            return 0;
        }
    }

    private String extractSender(String text) {
        Matcher matcher = SENDER.matcher(text);

        if (!matcher.find()) {
            return "Сбер";
        }

        String name = matcher.group(1)
                .trim()
                .replaceAll("[.,;:]+$", "")
                .trim();

        if (name.length() > 32) {
            name = name.substring(0, 32).trim();
        }

        return name.isEmpty() ? "Сбер" : name;
    }

    private String extractText(Notification notification) {
        Bundle extras = notification.extras;

        if (extras == null) {
            return "";
        }

        StringBuilder text = new StringBuilder();

        append(text, extras.getCharSequence(Notification.EXTRA_TITLE));
        append(text, extras.getCharSequence(Notification.EXTRA_TEXT));
        append(text, extras.getCharSequence(Notification.EXTRA_SUB_TEXT));
        append(text, extras.getCharSequence(Notification.EXTRA_BIG_TEXT));

        CharSequence[] lines = extras.getCharSequenceArray(
                Notification.EXTRA_TEXT_LINES);

        if (lines != null) {
            for (CharSequence line : lines) {
                append(text, line);
            }
        }

        return text.toString().trim();
    }

    private void append(StringBuilder target, CharSequence part) {
        if (part == null) {
            return;
        }

        String value = part.toString().trim();

        if (value.isEmpty()) {
            return;
        }

        if (target.length() > 0) {
            target.append(' ');
        }

        target.append(value);
    }
}
