package com.ebu6304.group48.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SemesterFormatTest {

    @Test
    void label_spring() {
        assertEquals("2026 Spring", SemesterFormat.label("2026_SPRING"));
    }

    @Test
    void label_fall() {
        assertEquals("2026 Fall", SemesterFormat.label("2026_FALL"));
    }

    @Test
    void label_2025_spring() {
        assertEquals("2025 Spring", SemesterFormat.label("2025_SPRING"));
    }

    @Test
    void label_null_returnsEmpty() {
        assertEquals("", SemesterFormat.label(null));
    }

    @Test
    void label_empty_returnsEmpty() {
        assertEquals("", SemesterFormat.label(""));
    }

    @Test
    void label_singleWord_returnsAsIs() {
        assertEquals("Spring", SemesterFormat.label("Spring"));
    }
}
