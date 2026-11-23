package com.livegiftstudio;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Чистая логика разбора push Сбера. Вынесена из NotificationListenerService,
 * чтобы проверять её обычными JVM-тестами и править без Android-эмулятора.
 */
public final class SberParserCore {

    /** Сумма: «1 000 ₽», «1 000,50 ₽», «500 руб.». */
    private static final Pattern AMOUNT = Pattern.compile(
            "(\\d{1,3}(?:[\\s\\u00A0\\u202F]\\d{3})*(?:[.,]\\d{1,2})?"
                    + "|\\d+(?:[.,]\\d{1,2})?)"
                    + "[\\s\\u00A0\\u202F]*(?:₽|руб)");

    /** Отправитель: «от ИВАН И.», «От: Иван». */
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

    private SberParserCore() {
    }

    /** Режем «Баланс: …», иначе баланс принимается за сумму доната. */
    public static String cutBalance(String text) {
        if (text == null) {
            return "";
        }

        String lower = text.toLowerCase(new Locale("ru", "RU"));
        int cut = -1;

        for (String marker : BALANCE_MARKERS) {
            int index = lower.indexOf(marker);

            // Маркер может стоять и в самом начале строки.
            if (index >= 0 && (cut < 0 || index < cut)) {
                cut = index;
            }
        }

        return cut >= 0 ? text.substring(0, cut) : text;
    }

    public static boolean isOutgoing(String lower) {
        if (lower == null) {
            return false;
        }

        for (String marker : OUTGOING_MARKERS) {
            if (lower.contains(marker)) {
                return true;
            }
        }

        return false;
    }

    public static boolean looksIncoming(String lower) {
        if (lower == null) {
            return false;
        }

        return lower.contains("пополн")
                || lower.contains("зачисл")
                || lower.contains("поступл")
                || lower.contains("получен")
                || lower.contains("перевед")
                || lower.contains("перевел")
                || lower.contains("перевод");
    }

    public static double parseAmount(String text) {
        if (text == null) {
            return 0;
        }

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

    public static String extractSender(String text) {
        if (text == null) {
            return "Сбер";
        }

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
}
