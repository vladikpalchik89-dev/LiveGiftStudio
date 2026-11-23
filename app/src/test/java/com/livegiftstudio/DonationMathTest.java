package com.livegiftstudio;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DonationMathTest {

    @Test
    public void rublesAreNotConverted() {
        assertEquals(500d, DonationMath.toRubles(500d, "RUB", 100, 95, 0.18), 0.001);
    }

    @Test
    public void nullCurrencyTreatedAsRubles() {
        assertEquals(500d, DonationMath.toRubles(500d, null, 100, 95, 0.18), 0.001);
    }

    @Test
    public void euroUsesItsOwnRate() {
        assertEquals(1000d, DonationMath.toRubles(10d, "eur", 100, 95, 0.18), 0.001);
    }

    @Test
    public void usdUsesCachedRate() {
        assertEquals(950d, DonationMath.toRubles(10d, "USD", 100, 95, 0.18), 0.001);
    }

    @Test
    public void kztUsesSmallRateInsteadOfOldTimesNinety() {
        assertEquals(180d, DonationMath.toRubles(1000d, "KZT", 100, 95, 0.18), 0.001);
    }

    @Test
    public void unknownCurrencyIsCountedAsIs() {
        assertEquals(7d, DonationMath.toRubles(7d, "TRY", 100, 95, 0.18), 0.001);
    }

    @Test
    public void missingRateDoesNotNullifyDonation() {
        assertEquals(10d, DonationMath.toRubles(10d, "EUR", 0, 0, 0), 0.001);
    }
}
