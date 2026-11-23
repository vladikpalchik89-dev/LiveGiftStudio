package com.livegiftstudio;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SeenDonationIdsTest {

    @Test
    public void sameIdIsAcceptedOnce() {
        SeenDonationIds seen = new SeenDonationIds();

        assertTrue(seen.markSeen("donation-42"));
        assertFalse(seen.markSeen("donation-42"));
        assertEquals(1, seen.size());
    }

    @Test
    public void differentIdsAreAccepted() {
        SeenDonationIds seen = new SeenDonationIds();

        assertTrue(seen.markSeen("a"));
        assertTrue(seen.markSeen("b"));
        assertEquals(2, seen.size());
    }

    @Test
    public void blankIdNeverBlocksDonation() {
        SeenDonationIds seen = new SeenDonationIds();

        assertTrue(seen.markSeen(null));
        assertTrue(seen.markSeen(""));
        assertTrue(seen.markSeen(null));
        assertEquals(0, seen.size());
    }

    @Test
    public void clearResetsDedup() {
        SeenDonationIds seen = new SeenDonationIds();

        assertTrue(seen.markSeen("x"));
        assertFalse(seen.markSeen("x"));

        seen.clear();

        assertTrue(seen.markSeen("x"));
    }

    @Test
    public void oldIdsExpire() throws Exception {
        SeenDonationIds seen = new SeenDonationIds(5);

        assertTrue(seen.markSeen("old"));
        Thread.sleep(30);

        assertTrue(seen.markSeen("old"));
    }

    @Test
    public void setIsBounded() {
        SeenDonationIds seen = new SeenDonationIds();

        for (int i = 0; i < 600; i++) {
            seen.markSeen("id-" + i);
        }

        // Лимит — 500 записей; после одного прохода уборки остаётся ~250,
        // затем ids добавляются снова до следующего порога.
        assertTrue(seen.size() <= 500);
        assertTrue(seen.size() >= 250);
    }
}
