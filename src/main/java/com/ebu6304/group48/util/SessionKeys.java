package com.ebu6304.group48.util;

public final class SessionKeys {

    public static final String USER_ID = "auth.userId";
    public static final String USERNAME = "auth.username";
    public static final String ROLE = "auth.role";

    /** Set after banned login attempt; allows /ban-appeal to show reason without re-entering password. */
    public static final String BANNED_APPEAL_USER_ID = "bannedAppeal.pendingUserId";

    private SessionKeys() {
    }
}
