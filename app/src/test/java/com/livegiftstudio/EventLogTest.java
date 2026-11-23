package com.livegiftstudio;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EventLogTest {

    @Test
    public void keepsEntriesInOrderAndClears() {
        EventLog.clear();
        EventLog.add("gift", "Роза");
        EventLog.add("donation", "500, \"тест\"");

        List<EventLog.Entry> rows = EventLog.snapshot();

        assertEquals(2, rows.size());
        assertEquals("gift", rows.get(0).type);
        assertEquals("donation", rows.get(1).type);

        EventLog.clear();
        assertTrue(EventLog.snapshot().isEmpty());
    }

    @Test
    public void csvEscapesCommasAndQuotes() {
        EventLog.clear();
        EventLog.add("donation", "500, \"тест\"");

        String csv = EventLog.toCsv(EventLog.snapshot());

        assertTrue(csv.startsWith("time,type,text\n"));
        assertTrue(csv.contains("\"500, \"\"тест\"\"\""));
    }

    @Test
    public void jsonEscapesQuotes() {
        EventLog.clear();
        EventLog.add("chat", "Привет, \"мир\"");

        String json = EventLog.toJson(EventLog.snapshot());

        assertTrue(json.startsWith("[\n"));
        assertTrue(json.contains("Привет, \\\"мир\\\""));
    }
}
