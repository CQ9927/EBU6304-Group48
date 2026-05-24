package com.ebu6304.group48.repository;

import com.ebu6304.group48.model.Job;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JobRepositoryTest {

    private static JobRepository repo;
    private static Path tempDataDir;

    @BeforeAll
    static void setUp() throws IOException {
        tempDataDir = Files.createTempDirectory("ebu6304-jobs-");
        repo = new JobRepository(RepositoryTestBase.mockContext(tempDataDir));
    }

    @BeforeEach
    void clearData() throws IOException {
        // Remove jobs.json between tests so each test starts with fresh data
        Path jobsFile = tempDataDir.resolve("jobs.json");
        Files.deleteIfExists(jobsFile);
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
        List<Job> jobs = repo.findAll();
        assertNotNull(jobs);
        assertTrue(jobs.isEmpty());
    }

    @Test
    void save_createsNewJob() {
        Job job = new Job();
        job.setTitle("Test Job");
        job.setType("MODULE");
        job.setSemester("2026_SPRING");
        job.setSchedule("WED_18_20");
        job.setCapacity(2);
        job.setRequiredSkills(Arrays.asList("Java", "Teaching"));
        job.setPostedByUserId("U-MO1");
        job.setStatus("OPEN");

        boolean saved = repo.save(job);
        assertTrue(saved);
        assertNotNull(job.getJobId(), "jobId should be auto-generated");
        assertTrue(job.getJobId().startsWith("J-"));
        assertNotNull(job.getCreatedAt());

        List<Job> all = repo.findAll();
        assertEquals(1, all.size());
    }

    @Test
    void findById_returnsCorrectJob() {
        Job job = new Job();
        job.setTitle("Algorithms TA");
        job.setType("MODULE");
        job.setSemester("2026_FALL");
        job.setSchedule("MON_09_12");
        job.setCapacity(1);
        job.setRequiredSkills(Arrays.asList("Algorithms"));
        job.setPostedByUserId("U-MO2");
        job.setStatus("OPEN");
        repo.save(job);

        Job found = repo.findById(job.getJobId());
        assertNotNull(found);
        assertEquals("Algorithms TA", found.getTitle());
    }

    @Test
    void findById_returnsNullForMissing() {
        assertNull(repo.findById("J-NONEXIST"));
    }

    @Test
    void findAllOpenJobs_onlyReturnsOpen() {
        Job openJob = new Job();
        openJob.setTitle("Open Job");
        openJob.setType("MODULE");
        openJob.setSemester("2026_SPRING");
        openJob.setSchedule("TUE_14_16");
        openJob.setCapacity(3);
        openJob.setStatus("OPEN");
        openJob.setPostedByUserId("U-MO3");
        repo.save(openJob);

        Job closedJob = new Job();
        closedJob.setTitle("Closed Job");
        closedJob.setType("INVIGILATION");
        closedJob.setSemester("2026_SPRING");
        closedJob.setSchedule("FRI_09_12");
        closedJob.setCapacity(1);
        closedJob.setStatus("CLOSED");
        closedJob.setPostedByUserId("U-MO3");
        repo.save(closedJob);

        List<Job> openJobs = repo.findAllOpenJobs();
        assertEquals(1, openJobs.size());
        assertEquals("Open Job", openJobs.get(0).getTitle());
    }

    @Test
    void updateStatus_changesStatus() {
        Job job = new Job();
        job.setTitle("Status Job");
        job.setType("MODULE");
        job.setSemester("2026_SPRING");
        job.setSchedule("FRI_14_16");
        job.setCapacity(1);
        job.setStatus("OPEN");
        job.setPostedByUserId("U-MO4");
        repo.save(job);

        boolean updated = repo.updateStatus(job.getJobId(), "CLOSED");
        assertTrue(updated);

        Job refreshed = repo.findById(job.getJobId());
        assertEquals("CLOSED", refreshed.getStatus());
    }

    @Test
    void updateStatus_nonexistentJob_returnsFalse() {
        assertFalse(repo.updateStatus("J-NONEXIST", "CLOSED"));
    }

    @Test
    void save_multipleJobs_allPersisted() {
        for (int i = 0; i < 3; i++) {
            Job job = new Job();
            job.setTitle("Job " + i);
            job.setType("MODULE");
            job.setSemester("2026_SPRING");
            job.setSchedule("WED_18_20");
            job.setCapacity(i + 1);
            job.setStatus("OPEN");
            job.setPostedByUserId("U-MO5");
            repo.save(job);
        }
        assertEquals(3, repo.findAll().size());
    }
}
