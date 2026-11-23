package com.livegiftstudio;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Журнал событий эфира, общий для сервиса оверлея и MainActivity.
 * Держим ограниченное число записей в памяти: это не база данных,
 * а быстрый способ понять, что прилетело за последние эфиры.
 */
public final class EventLog {

    public static final class Entry {
        public final long at;
        public final String type;
        public final String text;

        Entry(long at, String type, String text) {
            this.at = at;
            this.type = type == null ? "" : type;
            this.text = text == null ? "" : text;
        }
    }

    private static final int MAX_ENTRIES = 1000;
    private static final List<Entry> entries =
            Collections.synchronizedList(new ArrayList<Entry>());

    private EventLog() {
    }

    public static void add(String type, String text) {
        synchronized (entries) {
            entries.add(new Entry(
                    System.currentTimeMillis(), type, text));

            while (entries.size() > MAX_ENTRIES) {
                entries.remove(0);
            }
        }
    }

    public static List<Entry> snapshot() {
        synchronized (entries) {
            return new ArrayList<Entry>(entries);
        }
    }

    public static void clear() {
        synchronized (entries) {
            entries.clear();
        }
    }

    public static String toCsv(List<Entry> rows) {
        StringBuilder out = new StringBuilder();
        out.append("time,type,text\n");

        SimpleDateFormat timeFormat =
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT);

        for (Entry row : rows) {
            out.append(timeFormat.format(new Date(row.at)))
                    .append(',')
                    .append(csv(row.type))
                    .append(',')
                    .append(csv(row.text))
                    .append('\n');
        }

        return out.toString();
    }

    public static String toJson(List<Entry> rows) {
        StringBuilder out = new StringBuilder();
        out.append("[\n");

        for (int i = 0; i < rows.size(); i++) {
            Entry row = rows.get(i);

            out.append("{\"at\":")
                    .append(row.at)
                    .append(",\"type\":\"")
                    .append(escape(row.type))
                    .append("\",\"text\":\"")
                    .append(escape(row.text))
                    .append("\"}");

            if (i < rows.size() - 1) {
                out.append(',');
            }

            out.append('\n');
        }

        out.append("]\n");
        return out.toString();
    }

    private static String csv(String value) {
        String safe = value == null ? "" : value;

        if (safe.indexOf(',') >= 0
                || safe.indexOf('"') >= 0
                || safe.indexOf('\n') >= 0) {
            return '"' + safe.replace("\"", "\"\"") + '"';
        }

        return safe;
    }

    private static String escape(String value) {
        String safe = value == null ? "" : value;

        return safe.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
