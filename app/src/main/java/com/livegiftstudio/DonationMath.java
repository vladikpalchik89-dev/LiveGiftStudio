package com.livegiftstudio;

/**
 * Чистая логика пересчёта валюты доната в рубли.
 * Вынесена отдельно от Android-сервиса, чтобы её можно было покрыть
 * обычными JVM-юнит-тестами без эмулятора.
 */
public final class DonationMath {

    private DonationMath() {
    }

    /**
     * @param amount  сумма доната в оригинальной валюте
     * @param currency код валюты (EUR, USD, KZT, RUB)
     * @param eurRate рублей за 1 EUR
     * @param usdRate рублей за 1 USD
     * @param kztRate рублей за 1 KZT
     * @return сумма в рублях; 0 и negate не меняются
     */
    public static double toRubles(
            double amount, String currency,
            double eurRate, double usdRate, double kztRate) {

        String code = currency == null
                ? "RUB"
                : currency.toUpperCase(java.util.Locale.ROOT).trim();

        double rate;

        if ("RUB".equals(code) || "₽".equals(code)) {
            rate = 1d;
        } else if ("EUR".equals(code) || "€".equals(code)) {
            rate = eurRate > 0 ? eurRate : 1d;
        } else if ("USD".equals(code) || "$".equals(code)) {
            rate = usdRate > 0 ? usdRate : 1d;
        } else if ("KZT".equals(code) || "₸".equals(code)) {
            rate = kztRate > 0 ? kztRate : 1d;
        } else {
            // Неизвестную валюту не выбрасываем и не умножаем на 90:
            // лучше учесть как есть, чем потерять донат в цели сбора.
            rate = 1d;
        }

        return amount * rate;
    }
}
