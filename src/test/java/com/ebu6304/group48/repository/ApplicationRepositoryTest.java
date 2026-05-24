package com.ebu6304.group48.repository;

import com.ebu6304.group48.model.Application;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationRepositoryTest {

    private static ApplicationRepository repo;
    private static Path tempDataDir;

    @BeforeAll
    static void setUp() throws IOException {
        tempDataDir = Files.createTempDirectory("ebu6304-apps-");
        repo = new ApplicationRepository(RepositoryTestBase.mockContext(tempDataDir));
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
    void findAll_returnsEmptyInitially() {
        List<Application> apps = repo.findAll();
        assertNotNull(apps);
        assertTrue(apps.isEmpty());
    }

    @Test
    void save_createsNewApplication() {
        Application app = new Application();
        app.setJobId("J-TEST01");
        app.setApplicantUserId("U-TA1");
        app.setStatus("SUBMITTED");

        boolean saved = repo.save(app);
        assertTrue(saved);
        assertNotNull(app.getApplicationId(), "applicationId should be auto-generated");
        assertTrue(app.getApplicationId().startsWith("A-"));

        List<Application> all = repo.findAll();
        assertEquals(1, all.size());
        assertEquals("J-TEST01", all.get(0).getJobId());
    }

    @Test
    void findById_returnsCorrectApp() {
        Application app = new Application();
        app.setJobId("J-TEST02");
        app.setApplicantUserId("U-TA2");
        app.setStatus("UNDER_REVIEW");
        repo.save(app);

        Application found = repo.findById(app.getApplicationId());
        assertNotNull(found);
        assertEquals("UNDER_REVIEW", found.getStatus());
    }

    @Test
    void findById_returnsNullForMissing() {
        assertNull(repo.findById("A-NONEXIST"));
    }

    @Test
    void updateStatus_validTransition() {
        Application app = new Application();
        app.setJobId("J-TEST03");
        app.setApplicantUserId("U-TA3");
        app.setStatus("SUBMITTED");
        repo.save(app);

        boolean updated = repo.updateStatus(app.getApplicationId(), "UNDER_REVIEW");
        assertTrue(updated);

        Application refreshed = repo.findById(app.getApplicationId());
        assertEquals("UNDER_REVIEW", refreshed.getStatus());
    }

    @Test
    void updateStatus_invalidStatus_returnsFalse() {
        Application app = new Application();
        app.setJobId("J-TEST04");
        app.setApplicantUserId("U-TA4");
        app.setStatus("SUBMITTED");
        repo.save(app);

        assertFalse(repo.updateStatus(app.getApplicationId(), "INVALID_STATUS"));
    }

    @Test
    void save_multipleApps_allStored() {
        for (int i = 0; i < 5; i++) {
            Application app = new Application();
            app.setJobId("J-TEST-MULTI");
            app.setApplicantUserId("U-TA" + i);
            app.setStatus("SUBMITTED");
            assertTrue(repo.save(app));
        }
        List<Application> all = repo.findAll();
        assertEquals(5, all.stream()
                .filter(a -> "J-TEST-MULTI".equals(a.getJobId()))
                .count());
    }

    @Test
    void rejectByAdmin_validState_appIsRejected() {
        Application app = new Application();
        app.setJobId("J-TEST05");
        app.setApplicantUserId("U-TA5");
        app.setStatus("SUBMITTED");
        repo.save(app);

        boolean ok = repo.rejectByAdmin(app.getApplicationId(), "U-ADMIN");
        assertTrue(ok);

        Application refreshed = repo.findById(app.getApplicationId());
        assertEquals("REJECTED", refreshed.getStatus());
        assertTrue(refreshed.getAdminRevoked());
    }

    @Test
    void rejectByAdmin_alreadySelected_returnsFalse() {
        Application app = new Application();
        app.setJobId("J-TEST06");
        app.setApplicantUserId("U-TA6");
        app.setStatus("SELECTED");
        repo.save(app);

        assertFalse(repo.rejectByAdmin(app.getApplicationId(), "U-ADMIN"));
    }
}
