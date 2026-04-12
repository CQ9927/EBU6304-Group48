package com.ebu6304.group48.util;

/**
 * Default entry path after login and for role-scoped navigation (e.g. logo link).
 */
public final class RoleLanding {

    private RoleLanding() {
    }

    public static String defaultPath(String role) {
        if (role == null) {
            return "/home";
        }
        return switch (role) {
            case "MO" -> "/mo/dashboard";
            case "ADMIN" -> "/admin/workload";
            case "TA" -> "/ta/dashboard";
            default -> "/home";
        };
    }
}
