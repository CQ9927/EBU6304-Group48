package com.ebu6304.group48.util;

/**
 * Utility for formatting raw semester codes into human-readable labels.
 * e.g. "2026_SPRING" -> "2026 Spring"
 */
public final class SemesterFormat {

    private SemesterFormat() { }

    public static String label(String semesterCode) {
        if (semesterCode == null || semesterCode.isEmpty()) return "";
        String formatted = semesterCode.replace('_', ' ');
        // Capitalise only the season word: "2026 spring" -> "2026 Spring"
        int space = formatted.indexOf(' ');
        if (space >= 0 && space + 1 < formatted.length()) {
            formatted = formatted.substring(0, space + 1)
                    + Character.toUpperCase(formatted.charAt(space + 1))
                    + formatted.substring(space + 2).toLowerCase();
        }
        return formatted;
    }
}
