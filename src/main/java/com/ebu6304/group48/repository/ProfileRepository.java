/**
 * =============================================================================
 * Profile Repository - Data Access Layer (Placeholder Implementation)
 * =============================================================================
 * 
 * OWNER: zzzskl (231226772)
 * CREATED: 2026-04-07
 * VERSION: 1.0
 * STATUS: 🔧 TEST/PLACEHOLDER CODE - Requires Implementation
 * 
 * =============================================================================
 * 
 * ⚠️ IMPORTANT NOTE:
 * This is a PLACEHOLDER implementation created to allow compilation of
 * dependent servlets. It does NOT provide actual data persistence.
 * Another team member must implement the actual JSON file operations.
 * 
 * =============================================================================
 * 
 * 📋 REPOSITORY DESCRIPTION:
 * Data access layer for Profile entities. Responsible for CRUD operations
 * on TA profile data stored in JSON format. This class abstracts the
 * file system operations and provides a clean API for business logic.
 * 
 * =============================================================================
 * 
 * 🎯 IMPLEMENTATION REQUIREMENTS:
 * 
 * 1. FILE OPERATIONS:
 *    - Read from: data/profiles.json
 *    - Write to: data/profiles.json  
 *    - File format: JSON array of Profile objects
 *    - Location: Determined by AppPaths.resolveDataDirectory()
 * 
 * 2. REQUIRED METHODS:
 *    - findByUserId(String userId): Find profile by user ID
 *    - save(Profile profile): Create or update profile
 *    - findAll(): Retrieve all profiles (for admin/analytics)
 * 
 * 3. EXPECTED BEHAVIOR:
 *    - findByUserId: Returns null if profile not found
 *    - save: Creates new profile if profileId doesn't exist, updates otherwise
 *    - findAll: Returns empty list if file doesn't exist or is empty
 *    - Thread safety: Consider concurrent access to JSON file
 * 
 * =============================================================================
 * 
 * 🔧 TECHNICAL IMPLEMENTATION GUIDE:
 * 
 * 1. JSON PROCESSING:
 *    - Use Gson for JSON serialization/deserialization (same stack as UserRepository)
 *    - Handle IOException appropriately (log, return default values)
 *    - Implement proper error handling for malformed JSON
 * 
 * 2. FILE MANAGEMENT:
 *    - Check if file exists before reading
 *    - Create file if it doesn't exist (with empty array [])
 *    - Implement atomic writes to prevent data corruption
 *    - Consider file locking for concurrent access
 * 
 * 3. PERFORMANCE CONSIDERATIONS:
 *    - Cache frequently accessed profiles in memory
 *    - Implement lazy loading for large datasets
 *    - Consider file watching for changes by other processes
 * 
 * 4. ERROR HANDLING:
 *    - Return null/empty collections on file errors
 *    - Log exceptions with appropriate context
 *    - Provide meaningful error messages for debugging
 * 
 * =============================================================================
 * 
 * 📝 PLACEHOLDER IMPLEMENTATION DETAILS:
 * 
 * Current methods return dummy values:
 *   - findByUserId: Always returns null
 *   - save: Always returns true (pretends success)
 *   - findAll: Always returns empty list
 * 
 * This allows dependent code to compile but will not persist any data.
 * Real implementation must replace these with actual file operations.
 * 
 * =============================================================================
 * 
 * 🧪 TESTING RECOMMENDATIONS:
 * 
 * 1. Unit Tests:
 *    - Test findByUserId with existing/non-existing users
 *    - Test save creates new profiles and updates existing ones
 *    - Test findAll returns correct number of profiles
 *    - Test error handling for missing/corrupted JSON file
 * 
 * 2. Integration Tests:
 *    - Test file is created when it doesn't exist
 *    - Test concurrent access from multiple threads
 *    - Test data persistence across application restarts
 *    - Test with large number of profiles
 * 
 * 3. Performance Tests:
 *    - Measure read/write times with growing data
 *    - Test memory usage with large profile collections
 *    - Benchmark concurrent read/write operations
 * 
 * =============================================================================
 * 
 * 📁 DEPENDENCIES:
 *   - Gson (already in pom.xml), consistent with UserRepository
 *   - ServletContext (for data directory resolution)
 *   - Profile model class (for data representation)
 * 
 * =============================================================================
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
        return findAll().stream()
                .filter(profile -> userId.equals(profile.getUserId()))
                .findFirst()
                .orElse(null);
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