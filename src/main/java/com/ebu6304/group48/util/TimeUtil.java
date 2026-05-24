package com.ebu6304.group48.util;

/**
 * Simple ISO 8601 timestamp formatter for display.
 */
public final class TimeUtil {

    private TimeUtil() {
    }

    /**
     * Converts ISO 8601 (e.g. "2026-04-11T14:30:00Z") to readable "2026-04-11 14:30".
     * Returns "—" for null/blank input.
     */
    public static String format(String iso) {
        if (iso == null || iso.isBlank()) return "—";
        String s = iso.replace("T", " ");
        if (s.endsWith("Z")) s = s.substring(0, s.length() - 1);
        // Trim fractional seconds if present
        int dot = s.indexOf('.');
        if (dot > 0) s = s.substring(0, dot);
        return s.length() > 19 ? s.substring(0, 19) : s;
    }
}
