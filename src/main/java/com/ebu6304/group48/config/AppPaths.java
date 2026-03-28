package com.ebu6304.group48.config;

import javax.servlet.ServletContext;

/**
 * Resolves the on-disk directory for JSON/CSV data files.
 * Configure with context-param {@code dataDirectory} in web.xml, or it defaults under the user home.
 */
public final class AppPaths {

    private static final String CONTEXT_PARAM = "dataDirectory";
    private static final String DEFAULT_SUBDIR = "ebu6304-group48-data";

    private AppPaths() {
    }

    public static String resolveDataDirectory(ServletContext ctx) {
        String configured = ctx.getInitParameter(CONTEXT_PARAM);
        if (configured != null && !configured.isBlank()) {
            return configured.trim();
        }
        String home = System.getProperty("user.home", ".");
        return home + java.io.File.separator + DEFAULT_SUBDIR;
    }
}
