package com.ebu6304.group48.repository;

import com.ebu6304.group48.model.Profile;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ProfileRepositoryTest {

    private static ProfileRepository repo;
    private static Path tempDataDir;

    @BeforeAll
    static void setUp() throws IOException {
        tempDataDir = Files.createTempDirectory("ebu6304-profile-");
        repo = new ProfileRepository(RepositoryTestBase.mockContext(tempDataDir));
    }

    @AfterAll
    static void tearDown() throws IOException {
        if (tempDataDir != null && Files.exists(tempDataDir)) {
            Files.walk(tempDataDir)
                    .sorted(java.util.Comparator.reverseOrder())
                    .forEach(p -> { try { Files.delete(p); } catch (IOException e) { } });
        }
    }

    @Test
    void findByUserId_returnsNullWhenEmpty() {
        assertNull(repo.findByUserId("U-UNKNOWN"));
    }

    @Test
    void save_createsNewProfile() {
        Profile profile = new Profile();
        profile.setUserId("U-TA-P1");
        profile.setName("Test TA");
        profile.setEmail("test@ebu6304.edu");
        profile.setMajor("Computer Science");
        profile.setSkills(Arrays.asList("Java", "Python"));

        boolean saved = repo.save(profile);
        assertTrue(saved);
        assertNotNull(profile.getProfileId(), "profileId should be auto-generated");
        assertTrue(profile.getProfileId().startsWith("P-"));

        Profile found = repo.findByUserId("U-TA-P1");
        assertNotNull(found);
        assertEquals("Test TA", found.getName());
        assertEquals(2, found.getSkills().size());
    }

    @Test
    void save_updatesExistingProfile() {
        Profile profile = new Profile();
        profile.setUserId("U-TA-P2");
        profile.setName("Original Name");
        profile.setEmail("old@ebu6304.edu");
        profile.setMajor("Math");
        repo.save(profile);

        profile.setName("Updated Name");
        profile.setEmail("new@ebu6304.edu");
        boolean updated = repo.save(profile);
        assertTrue(updated);

        Profile found = repo.findByUserId("U-TA-P2");
        assertEquals("Updated Name", found.getName());
        assertEquals("new@ebu6304.edu", found.getEmail());
    }

    @Test
    void save_withAvailability() {
        Profile profile = new Profile();
        profile.setUserId("U-TA-P3");
        profile.setName("TA With Slots");
        profile.setEmail("slots@ebu6304.edu");
        profile.setMajor("SE");
        profile.setAvailability(Arrays.asList("MON_09_12", "WED_18_20", "FRI_14_16"));

        repo.save(profile);
        Profile found = repo.findByUserId("U-TA-P3");
        assertNotNull(found.getAvailability());
        assertEquals(3, found.getAvailability().size());
        assertTrue(found.getAvailability().contains("WED_18_20"));
    }

    @Test
    void save_withCVFileName() {
        Profile profile = new Profile();
        profile.setUserId("U-TA-P4");
        profile.setName("CV TA");
        profile.setEmail("cv@ebu6304.edu");
        profile.setMajor("CS");
        profile.setCvFileName("CV_U-TA-P4_20260401.txt");

        repo.save(profile);
        Profile found = repo.findByUserId("U-TA-P4");
        assertEquals("CV_U-TA-P4_20260401.txt", found.getCvFileName());
    }

    @Test
    void save_preservesTimestamps() {
        Profile profile = new Profile();
        profile.setUserId("U-TA-P5");
        profile.setName("Timestamp TA");
        profile.setEmail("ts@ebu6304.edu");
        profile.setMajor("CS");
        profile.setCreatedAt("2026-04-01T10:00:00Z");

        repo.save(profile);
        Profile found = repo.findByUserId("U-TA-P5");
        assertNotNull(found.getCreatedAt());
    }
}
