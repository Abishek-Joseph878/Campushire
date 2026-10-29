package com.campushire.model;

/**
 * Represents the lifecycle status of a recruitment drive.
 */
public enum DriveStatus {
    UPCOMING("Upcoming"),
    ACTIVE("Active"),
    PAUSED("Paused"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    private final String displayName;

    DriveStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
