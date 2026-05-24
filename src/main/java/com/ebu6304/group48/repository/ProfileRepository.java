/**
 * JSON persistence for {@code profiles.json}.
 * Provides CRUD operations for Profile entities with thread-safe file I/O.
 */
package com.ebu6304.group48.repository;

import com.ebu6304.group48.config.AppPaths;
import com.ebu6304.group48.model.Profile;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class ProfileRepository {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type LIST_TYPE = new TypeToken<List<Profile>>() { }.getType();
    private static final Object FILE_LOCK = new Object();

    private final Path profilesFile;

    public ProfileRepository(ServletContext context) {
        String dataDirectory = AppPaths.resolveDataDirectory(context);
        this.profilesFile = Path.of(dataDirectory, "profiles.json");
    }

    public Profile findByUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            return null;
        }
        try {
            ensureStorage();
            return readAllInternal().stream()
                    .filter(profile -> userId.equals(profile.getUserId()))
                    .findFirst()
                    .orElse(null);
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }

    public boolean save(Profile profile) {
        if (profile == null || profile.getUserId() == null || profile.getUserId().isBlank()) {
            return false;
        }
        synchronized (FILE_LOCK) {
            try {
                ensureStorage();
                List<Profile> profiles = readAllInternal();
                normalizeBeforeSave(profile);

                int existingIndex = -1;
                for (int i = 0; i < profiles.size(); i++) {
                    if (profile.getUserId().equals(profiles.get(i).getUserId())) {
                        existingIndex = i;
                        break;
                    }
                }

                if (existingIndex >= 0) {
                    Profile existing = profiles.get(existingIndex);
                    profile.setProfileId(existing.getProfileId());
                    profile.setCreatedAt(existing.getCreatedAt());
                    profiles.set(existingIndex, profile);
                } else {
                    profiles.add(profile);
                }

                Files.writeString(profilesFile, GSON.toJson(profiles), StandardCharsets.UTF_8);
                return true;
            } catch (IOException e) {
                return false;
            } catch (RuntimeException e) {
                return false;
            }
        }
    }

    public List<Profile> findAll() {
        synchronized (FILE_LOCK) {
            try {
                ensureStorage();
                List<Profile> profiles = readAllInternal();
                profiles.sort(Comparator.comparing(Profile::getUpdatedAt, Comparator.nullsLast(String::compareTo)).reversed());
                return profiles;
            } catch (IOException e) {
                return new ArrayList<>();
            } catch (RuntimeException e) {
                return new ArrayList<>();
            }
        }
    }

    private void normalizeBeforeSave(Profile profile) {
        String now = Instant.now().toString();
        if (profile.getProfileId() == null || profile.getProfileId().isBlank()) {
            profile.setProfileId("P-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase());
        }
        if (profile.getSkills() == null) {
            profile.setSkills(new ArrayList<>());
        }
        if (profile.getAvailability() == null) {
            profile.setAvailability(new ArrayList<>());
        }
        if (profile.getCreatedAt() == null || profile.getCreatedAt().isBlank()) {
            profile.setCreatedAt(now);
        }
        if (profile.getUpdatedAt() == null || profile.getUpdatedAt().isBlank()) {
            profile.setUpdatedAt(now);
        }
    }

    private void ensureStorage() throws IOException {
        Files.createDirectories(profilesFile.getParent());
        if (!Files.exists(profilesFile)) {
            Files.writeString(profilesFile, "[]", StandardCharsets.UTF_8);
        }
    }

    private List<Profile> readAllInternal() throws IOException {
        String json = Files.readString(profilesFile, StandardCharsets.UTF_8);
        List<Profile> profiles = GSON.fromJson(json, LIST_TYPE);
        return profiles != null ? new ArrayList<>(profiles) : new ArrayList<>();
    }
}