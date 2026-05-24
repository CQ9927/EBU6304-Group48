package com.ebu6304.group48.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleLandingTest {

    @Test
    void defaultPath_TA() {
        assertEquals("/ta/dashboard", RoleLanding.defaultPath("TA"));
    }

    @Test
    void defaultPath_MO() {
        assertEquals("/mo/dashboard", RoleLanding.defaultPath("MO"));
    }

    @Test
    void defaultPath_ADMIN() {
        assertEquals("/admin/workload", RoleLanding.defaultPath("ADMIN"));
    }

    @Test
    void defaultPath_null_returnsHome() {
        assertEquals("/home", RoleLanding.defaultPath(null));
    }

    @Test
    void defaultPath_unknown_returnsHome() {
        assertEquals("/home", RoleLanding.defaultPath("UNKNOWN"));
        assertEquals("/home", RoleLanding.defaultPath(""));
        assertEquals("/home", RoleLanding.defaultPath("ta")); // case-sensitive
    }
}
