package com.livegiftstudio;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SberParserCoreTest {

    @Test
    public void parsesAmountWithSpacesAndComma() {
        assertEquals(1000.5, SberParserCore.parseAmount("Зачисление 1 000,50 ₽"), 0.001);
    }

    @Test
    public void parsesAmountWithNonBreakingSpaceAndRub() {
        assertEquals(500d, SberParserCore.parseAmount("Вам перевели 500\u00A0руб."), 0.001);
    }

    @Test
    public void noAmountReturnsZero() {
        assertEquals(0d, SberParserCore.parseAmount("Привет"), 0.001);
    }

    @Test
    public void cutsBalanceInMiddle() {
        String body = SberParserCore.cutBalance(
                "Перевод 500 ₽. Баланс: 12 000 ₽");

        assertEquals("Перевод 500 ₽. ", body);
        assertEquals(500d, SberParserCore.parseAmount(body), 0.001);
    }

    @Test
    public void cutsBalanceAtStart() {
        // Маркер в начале строки — всё уведомление про баланс,
        // доната там нет и сумма баланса не должна уйти в цель сбора.
        String body = SberParserCore.cutBalance("Баланс: 12 000 ₽");

        assertEquals("", body);
        assertEquals(0d, SberParserCore.parseAmount(body), 0.001);
    }

    @Test
    public void outgoingNotificationIsRejected() {
        assertTrue(SberParserCore.isOutgoing("перевод выполнен"));
        assertTrue(SberParserCore.isOutgoing("вы перевели 500 ₽"));
        assertFalse(SberParserCore.isOutgoing("вам перевели 500 ₽"));
    }

    @Test
    public void incomingPhrasesAreRecognized() {
        assertTrue(SberParserCore.looksIncoming("вам перевели 500 ₽"));
        assertTrue(SberParserCore.looksIncoming("перевод от ивана"));
        assertTrue(SberParserCore.looksIncoming("зачисление 100 ₽"));
        assertFalse(SberParserCore.looksIncoming("просто уведомление"));
    }

    @Test
    public void senderIsParsedCaseInsensitively() {
        assertEquals("Иван", SberParserCore.extractSender("Перевод от: Иван."));
        assertEquals("иван", SberParserCore.extractSender("Перевод от: иван."));
    }

    @Test
    public void missingSenderFallsBackToSber() {
        assertEquals("Сбер", SberParserCore.extractSender("Вам перевели 500 ₽"));
    }
}
