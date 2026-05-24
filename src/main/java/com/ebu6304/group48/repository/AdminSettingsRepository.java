package com.ebu6304.group48.repository;

import com.ebu6304.group48.model.AdminSettings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

public class AdminSettingsRepository {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Object FILE_LOCK = new Object();

    private final Path settingsFile;

    public AdminSettingsRepository(String dataDirectory) {
        this.settingsFile = Path.of(dataDirectory, "admin-settings.json");
    }

    public AdminSettings load() throws IOException {
        synchronized (FILE_LOCK) {
            ensureStorage();
            String json = Files.readString(settingsFile, StandardCharsets.UTF_8);
            AdminSettings settings = GSON.fromJson(json, AdminSettings.class);
            return settings != null ? settings : defaultSettings();
        }
    }

    public void saveWeeklyHoursThreshold(int thresholdHours) throws IOException {
        if (thresholdHours < 1 || thresholdHours > 168) {
            throw new IllegalArgumentException("Threshold must be between 1 and 168 hours.");
        }
        synchronized (FILE_LOCK) {
            ensureStorage();
            AdminSettings settings = loadInternal();
            settings.setWeeklyHoursThreshold(thresholdHours);
            settings.setUpdatedAt(Instant.now().toString());
            Files.writeString(settingsFile, GSON.toJson(settings), StandardCharsets.UTF_8);
        }
    }

    private AdminSettings loadInternal() throws IOException {
        String json = Files.readString(settingsFile, StandardCharsets.UTF_8);
        AdminSettings settings = GSON.fromJson(json, AdminSettings.class);
        return settings != null ? settings : defaultSettings();
    }

    private void ensureStorage() throws IOException {
        Files.createDirectories(settingsFile.getParent());
        if (!Files.exists(settingsFile)) {
            Files.writeString(settingsFile, GSON.toJson(defaultSettings()), StandardCharsets.UTF_8);
        }
    }

    private static AdminSettings defaultSettings() {
        AdminSettings settings = new AdminSettings();
        settings.setWeeklyHoursThreshold(AdminSettings.DEFAULT_WEEKLY_HOURS_THRESHOLD);
        settings.setUpdatedAt(Instant.now().toString());
        return settings;
    }
}