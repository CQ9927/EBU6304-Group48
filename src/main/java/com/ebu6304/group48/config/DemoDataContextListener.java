package com.ebu6304.group48.config;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Copies bundled demo files from {@code classpath:data/} into the runtime data directory when the
 * target file is missing or contains only an empty JSON array {@code []}. This mirrors {@link
 * com.ebu6304.group48.repository.UserRepository} behaviour for {@code users.json}, so Jetty/Tomcat
 * runs see jobs, profiles, applications, and selection without manual copy from the repo.
 */
public class DemoDataContextListener implements ServletContextListener {

    private static final String[] JSON_FILES = {
            "profiles.json",
            "jobs.json",
            "applications.json",
            "selection.json"
    };

    private static final String[] CV_FILES = {
            "CV_U-DEMO-TA_20260412090000.pdf",
            "CV_U-DEMO-TA2_20260412090000.pdf"
    };

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            String dataDirStr = AppPaths.resolveDataDirectory(sce.getServletContext());
            Path dataDir = Path.of(dataDirStr);
            Files.createDirectories(dataDir);
            ClassLoader cl = DemoDataContextListener.class.getClassLoader();

            for (String name : JSON_FILES) {
                Path target = dataDir.resolve(name);
                if (needsSeed(target)) {
                    try (InputStream in = cl.getResourceAsStream("data/" + name)) {
                        if (in != null) {
                            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                }
            }

            Path cvsDir = dataDir.resolve("cvs");
            Files.createDirectories(cvsDir);
            for (String cv : CV_FILES) {
                Path target = cvsDir.resolve(cv);
                if (!Files.exists(target)) {
                    try (InputStream in = cl.getResourceAsStream("data/cvs/" + cv)) {
                        if (in != null) {
                            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                }
            }
        } catch (IOException e) {
            sce.getServletContext().log("DemoDataContextListener: failed to seed bundled demo files", e);
        }
    }

    private static boolean needsSeed(Path file) throws IOException {
        if (!Files.exists(file)) {
            return true;
        }
        String s = Files.readString(file, StandardCharsets.UTF_8).trim();
        return s.isEmpty() || "[]".equals(s);
    }
}
