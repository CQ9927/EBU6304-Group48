package com.ebu6304.group48.model;

public class AdminSettings {

    public static final int DEFAULT_WEEKLY_HOURS_THRESHOLD = 10;

    private Integer weeklyHoursThreshold;
    private String updatedAt;

    public AdminSettings() {
    }

    public Integer getWeeklyHoursThreshold() {
        return weeklyHoursThreshold;
    }

    public void setWeeklyHoursThreshold(Integer weeklyHoursThreshold) {
        this.weeklyHoursThreshold = weeklyHoursThreshold;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int resolvedWeeklyHoursThreshold() {
        if (weeklyHoursThreshold == null || weeklyHoursThreshold < 1) {
            return DEFAULT_WEEKLY_HOURS_THRESHOLD;
        }
        return weeklyHoursThreshold;
    }
}