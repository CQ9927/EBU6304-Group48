package com.ebu6304.group48.service;

import com.ebu6304.group48.model.Job;
import com.ebu6304.group48.model.Profile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MatchingServiceTest {

    private MatchingService service;
    private Profile fullProfile;
    private Profile emptyProfile;
    private Job basicJob;

    @BeforeEach
    void setUp() {
        service = new MatchingService();

        fullProfile = new Profile();
        fullProfile.setName("Test TA");
        fullProfile.setEmail("test@ebu6304.edu");
        fullProfile.setMajor("Computer Science");
        fullProfile.setSkills(Arrays.asList("Java", "Python", "Teaching", "Algorithms"));
        fullProfile.setAvailability(Arrays.asList("MON_09_12", "WED_18_20", "FRI_14_16"));
        fullProfile.setCvFileName("CV_test_202604010000.txt");

        emptyProfile = new Profile();

        basicJob = new Job();
        basicJob.setJobId("J-TEST01");
        basicJob.setTitle("Software Engineering TA");
        basicJob.setType("MODULE");
        basicJob.setSchedule("WED_18_20");
        basicJob.setCapacity(2);
        basicJob.setRequiredSkills(Arrays.asList("Java", "Python"));
        basicJob.setStatus("OPEN");
    }

    // ── Null safety ──

    @Test
    void computeMatch_nullJob_returnsZero() {
        MatchingService.MatchResult mr = service.computeMatch(null, fullProfile);
        assertEquals(0, mr.getTotalScore());
    }

    @Test
    void computeMatch_nullProfile_returnsZero() {
        MatchingService.MatchResult mr = service.computeMatch(basicJob, null);
        assertEquals(0, mr.getTotalScore());
    }

    @Test
    void computeMatch_bothNull_returnsZero() {
        MatchingService.MatchResult mr = service.computeMatch(null, null);
        assertEquals(0, mr.getTotalScore());
    }

    // ── Skill matching ──

    @Test
    void computeMatch_fullSkillMatch_scoresCorrectly() {
        basicJob.setRequiredSkills(Arrays.asList("Java", "Python"));
        fullProfile.setSkills(Arrays.asList("Java", "Python", "Teaching"));
        MatchingService.MatchResult mr = service.computeMatch(basicJob, fullProfile);
        // 2/2 matched = 40 base + 1 extra skill * 2 = 2 bonus → 42
        assertEquals(42, mr.getSkillScore());
        assertEquals(2, mr.getMatchedSkills().size());
        assertTrue(mr.getMissingSkills().isEmpty());
    }

    @Test
    void computeMatch_partialSkillMatch() {
        basicJob.setRequiredSkills(Arrays.asList("Java", "Machine Learning"));
        fullProfile.setSkills(Arrays.asList("Java", "Python"));
        MatchingService.MatchResult mr = service.computeMatch(basicJob, fullProfile);
        assertTrue(mr.getSkillScore() >= 20); // 1/2 = 40% base, + bonus
        assertTrue(mr.getSkillScore() < 50);
        assertEquals(1, mr.getMatchedSkills().size());
        assertEquals(1, mr.getMissingSkills().size());
    }

    @Test
    void computeMatch_noSkillsRequired_fullScore() {
        basicJob.setRequiredSkills(Collections.emptyList());
        MatchingService.MatchResult mr = service.computeMatch(basicJob, fullProfile);
        assertEquals(50, mr.getSkillScore());
    }

    @Test
    void computeMatch_noMatchingSkills() {
        basicJob.setRequiredSkills(Arrays.asList("Rust", "Go"));
        fullProfile.setSkills(Arrays.asList("Java", "Python"));
        MatchingService.MatchResult mr = service.computeMatch(basicJob, fullProfile);
        // 0/2 matched = 0 base + 2 extra skills * 2 = 4 bonus → 4
        assertEquals(4, mr.getSkillScore());
        assertEquals(2, mr.getMissingSkills().size());
    }

    @Test
    void computeMatch_skillCaseInsensitive() {
        basicJob.setRequiredSkills(Arrays.asList("JAVA", "python"));
        fullProfile.setSkills(Arrays.asList("Java", "Python"));
        MatchingService.MatchResult mr = service.computeMatch(basicJob, fullProfile);
        // 2/2 matched case-insensitively = 40 + 0 extra = 40
        assertEquals(40, mr.getSkillScore());
    }

    // ── Schedule matching ──

    @Test
    void computeMatch_exactScheduleMatch_scores25() {
        basicJob.setSchedule("WED_18_20");
        fullProfile.setAvailability(Arrays.asList("WED_18_20"));
        MatchingService.MatchResult mr = service.computeMatch(basicJob, fullProfile);
        assertEquals(25, mr.getScheduleScore());
        assertTrue(mr.isScheduleMatch());
    }

    @Test
    void computeMatch_sameDayDifferentSlot_scores15() {
        basicJob.setSchedule("WED_18_20");
        fullProfile.setAvailability(Arrays.asList("WED_09_12"));
        MatchingService.MatchResult mr = service.computeMatch(basicJob, fullProfile);
        assertEquals(15, mr.getScheduleScore());
        assertFalse(mr.isScheduleMatch());
    }

    @Test
    void computeMatch_noAvailabilityMatch_scores0() {
        basicJob.setSchedule("WED_18_20");
        fullProfile.setAvailability(Arrays.asList("MON_09_12", "FRI_14_16"));
        MatchingService.MatchResult mr = service.computeMatch(basicJob, fullProfile);
        assertEquals(0, mr.getScheduleScore());
    }

    @Test
    void computeMatch_nullSchedule_getsFullScore() {
        basicJob.setSchedule(null);
        MatchingService.MatchResult mr = service.computeMatch(basicJob, fullProfile);
        assertEquals(25, mr.getScheduleScore());
    }

    @Test
    void computeMatch_emptySchedule_getsFullScore() {
        basicJob.setSchedule("");
        MatchingService.MatchResult mr = service.computeMatch(basicJob, fullProfile);
        assertEquals(25, mr.getScheduleScore());
    }

    // ── Major matching ──

    @Test
    void computeMatch_majorMatch_viaTitleKeyword() {
        basicJob.setTitle("Software Engineering Tutorial");
        fullProfile.setMajor("Software Engineering");
        MatchingService.MatchResult mr = service.computeMatch(basicJob, fullProfile);
        assertEquals(15, mr.getMajorScore());
    }

    @Test
    void computeMatch_majorNoMatch_scores5() {
        basicJob.setTitle("History of Art Tutorial");
        fullProfile.setMajor("Computer Science");
        MatchingService.MatchResult mr = service.computeMatch(basicJob, fullProfile);
        assertEquals(5, mr.getMajorScore());
    }

    // ── Completeness ──

    @Test
    void computeMatch_fullProfileCompleteness_scores10() {
        MatchingService.MatchResult mr = service.computeMatch(basicJob, fullProfile);
        assertEquals(10, mr.getCompletenessScore());
    }

    @Test
    void computeMatch_emptyProfileCompleteness_scores0() {
        basicJob.setSchedule(null);
        basicJob.setRequiredSkills(null);
        MatchingService.MatchResult mr = service.computeMatch(basicJob, emptyProfile);
        assertEquals(0, mr.getCompletenessScore());
    }

    @Test
    void computeMatch_partialCompleteness_scores3() {
        Profile partial = new Profile();
        partial.setName("TA");
        MatchingService.MatchResult mr = service.computeMatch(basicJob, partial);
        assertEquals(3, mr.getCompletenessScore());
    }

    // ── Total score bounds ──

    @Test
    void computeMatch_totalNeverExceeds100() {
        basicJob.setSchedule(null); // full schedule points
        basicJob.setRequiredSkills(Collections.emptyList()); // full skill points
        fullProfile.setMajor("Software Engineering");
        basicJob.setTitle("Software Engineering");
        MatchingService.MatchResult mr = service.computeMatch(basicJob, fullProfile);
        assertTrue(mr.getTotalScore() <= 100);
    }

    @Test
    void computeMatch_totalNeverBelow0() {
        MatchingService.MatchResult mr = service.computeMatch(basicJob, emptyProfile);
        assertTrue(mr.getTotalScore() >= 0);
    }

    @Test
    void computeMatch_detailStringIsPopulated() {
        MatchingService.MatchResult mr = service.computeMatch(basicJob, fullProfile);
        assertNotNull(mr.getDetail());
        assertFalse(mr.getDetail().isEmpty());
    }

    // ── Conflict hints ──

    @Test
    void buildConflictHints_emptyReturnsEmpty() {
        List<String> hints = service.buildConflictHints(Collections.emptyList(), Collections.emptyList());
        assertTrue(hints.isEmpty());
    }

    @Test
    void buildConflictHints_unknownJobHint() {
        com.ebu6304.group48.model.Application app = new com.ebu6304.group48.model.Application();
        app.setApplicationId("A-TEST");
        app.setJobId("J-MISSING");
        app.setApplicantUserId("U-TA1");
        List<String> hints = service.buildConflictHints(
                Collections.singletonList(basicJob), Collections.singletonList(app));
        assertEquals(1, hints.size());
        assertTrue(hints.get(0).contains("unknown jobId"));
    }

    @Test
    void buildConflictHints_duplicateApps() {
        com.ebu6304.group48.model.Application app1 = new com.ebu6304.group48.model.Application();
        app1.setApplicationId("A-1");
        app1.setJobId("J-TEST01");
        app1.setApplicantUserId("U-TA1");

        com.ebu6304.group48.model.Application app2 = new com.ebu6304.group48.model.Application();
        app2.setApplicationId("A-2");
        app2.setJobId("J-TEST01");
        app2.setApplicantUserId("U-TA1");

        List<String> hints = service.buildConflictHints(
                Collections.singletonList(basicJob), Arrays.asList(app1, app2));
        assertTrue(hints.stream().anyMatch(h -> h.contains("Duplicate")));
    }

    @Test
    void buildConflictHints_overCapacity() {
        com.ebu6304.group48.model.Application app = new com.ebu6304.group48.model.Application();
        app.setApplicationId("A-1");
        app.setJobId("J-TEST01");
        app.setStatus("SELECTED");

        basicJob.setCapacity(0);
        List<String> hints = service.buildConflictHints(
                Collections.singletonList(basicJob), Collections.singletonList(app));
        assertTrue(hints.stream().anyMatch(h -> h.contains("over capacity")));
    }

    @Test
    void buildConflictHints_closedJobWithPendingApps() {
        com.ebu6304.group48.model.Application app = new com.ebu6304.group48.model.Application();
        app.setApplicationId("A-1");
        app.setJobId("J-TEST01");
        app.setStatus("SUBMITTED");

        basicJob.setStatus("CLOSED");
        List<String> hints = service.buildConflictHints(
                Collections.singletonList(basicJob), Collections.singletonList(app));
        assertTrue(hints.stream().anyMatch(h -> h.contains("CLOSED")));
    }

    @Test
    void buildConflictHints_lowScoreSelected() {
        com.ebu6304.group48.model.Application app = new com.ebu6304.group48.model.Application();
        app.setApplicationId("A-1");
        app.setJobId("J-TEST01");
        app.setStatus("SELECTED");
        app.setMatchScore(30);

        List<String> hints = service.buildConflictHints(
                Collections.singletonList(basicJob), Collections.singletonList(app));
        assertTrue(hints.stream().anyMatch(h -> h.contains("low matchScore")));
    }
}
