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
        switch (role) {
            case "MO":    return "/mo/dashboard";
            case "ADMIN": return "/admin/workload";
            case "TA":    return "/ta/dashboard";
            default:      return "/home";
        }
    }
}
