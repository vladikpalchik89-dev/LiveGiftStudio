package com.livegiftstudio;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Общий дедуп донатов для WebSocket и опроса DonationAlerts.
 *
 * Потокобезопасен: один и тот же donation_id, пришедший из двух каналов,
 * пропускается только один раз. Старые записи вытесняются по времени,
 * чтобы set не рос бесконечно во время длинного эфира.
 */
public final class SeenDonationIds {

    private static final int MAX_IDS = 500;
    private static final int KEEP_IDS = 250;
    private static final long TTL_MS = 6L * 60 * 60 * 1000;

    private final Map<String, Long> seen =
            Collections.synchronizedMap(new HashMap<String, Long>());
    private final long ttlMs;

    public SeenDonationIds() {
        this(TTL_MS);
    }

    public SeenDonationIds(long ttlMs) {
        this.ttlMs = ttlMs;
    }

    /** @return true, если id увиден впервые. */
    public boolean markSeen(String id) {
        if (id == null || id.isEmpty()) {
            return true;
        }

        synchronized (seen) {
            long now = System.currentTimeMillis();
            prune(now);

            if (seen.containsKey(id)) {
                return false;
            }

            seen.put(id, now);

            if (seen.size() > MAX_IDS) {
                Iterator<Map.Entry<String, Long>> it =
                        seen.entrySet().iterator();
                int extra = seen.size() - KEEP_IDS;

                while (extra-- > 0 && it.hasNext()) {
                    it.next();
                    it.remove();
                }
            }

            return true;
        }
    }

    public void clear() {
        synchronized (seen) {
            seen.clear();
        }
    }

    public int size() {
        synchronized (seen) {
            return seen.size();
        }
    }

    private void prune(long now) {
        Iterator<Map.Entry<String, Long>> it =
                seen.entrySet().iterator();

        while (it.hasNext()) {
            long at = it.next().getValue();

            if (now - at > ttlMs) {
                it.remove();
            }
        }
    }
}
