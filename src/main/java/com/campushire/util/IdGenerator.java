package com.campushire.util;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates unique identifiers for domain objects.
 */
public final class IdGenerator {

    private IdGenerator() {}

    public static String newStudentId() {
        return "STU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public static String newCompanyId() {
        return "CMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public static String newDriveId() {
        return "DRV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public static String newShortlistId() {
        return "SHL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public static String newId(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
