package com.ebu6304.group48.service;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaWorkloadServiceTest {

    @Test
    void scheduleHours_parsesValidSchedule() {
        assertEquals(2, TaWorkloadService.scheduleHours("WED_18_20"));
        assertEquals(3, TaWorkloadService.scheduleHours("MON_09_12"));
    }

    @Test
    void scheduleHours_invalidSchedule_returnsZero() {
        assertEquals(0, TaWorkloadService.scheduleHours(null));
        assertEquals(0, TaWorkloadService.scheduleHours(""));
        assertEquals(0, TaWorkloadService.scheduleHours("INVALID"));
        assertEquals(0, TaWorkloadService.scheduleHours("MON_12_10"));
    }

    @Test
    void schedulesOverlap_sameSlot_returnsTrue() {
        assertTrue(TaWorkloadService.schedulesOverlap("WED_18_20", "WED_18_20"));
    }

    @Test
    void schedulesOverlap_partialOverlapSameDay_returnsTrue() {
        assertTrue(TaWorkloadService.schedulesOverlap("WED_18_20", "WED_19_21"));
    }

    @Test
    void schedulesOverlap_differentDays_returnsFalse() {
        assertFalse(TaWorkloadService.schedulesOverlap("WED_18_20", "THU_18_20"));
    }

    @Test
    void schedulesOverlap_adjacentNonOverlapping_returnsFalse() {
        assertFalse(TaWorkloadService.schedulesOverlap("WED_18_20", "WED_20_22"));
    }

    @Test
    void hasScheduleConflict_detectsOverlapAmongSelectedJobs() {
        List<TaWorkloadService.SelectedJobAssignment> jobs = Arrays.asList(
                assignment("J1", "WED_18_20"),
                assignment("J2", "WED_19_21")
        );
        assertTrue(TaWorkloadService.hasScheduleConflict(jobs));
    }

    @Test
    void hasScheduleConflict_noOverlap_returnsFalse() {
        List<TaWorkloadService.SelectedJobAssignment> jobs = Arrays.asList(
                assignment("J1", "MON_09_12"),
                assignment("J2", "WED_18_20")
        );
        assertFalse(TaWorkloadService.hasScheduleConflict(jobs));
    }

    @Test
    void hasScheduleConflict_emptyList_returnsFalse() {
        assertFalse(TaWorkloadService.hasScheduleConflict(Collections.emptyList()));
    }

    @Test
    void weeklyHoursSum_exceedsThreshold() {
        List<TaWorkloadService.SelectedJobAssignment> jobs = Arrays.asList(
                assignment("J1", "MON_09_12"),
                assignment("J2", "WED_18_20"),
                assignment("J3", "FRI_14_17")
        );
        int total = jobs.stream().mapToInt(TaWorkloadService.SelectedJobAssignment::getWeeklyHours).sum();
        assertEquals(8, total);
        assertTrue(total > 5);
    }

    private static TaWorkloadService.SelectedJobAssignment assignment(String jobId, String schedule) {
        return new TaWorkloadService.SelectedJobAssignment(jobId, "Job " + jobId, schedule, "2025-26 S1",
                TaWorkloadService.scheduleHours(schedule));
    }
}