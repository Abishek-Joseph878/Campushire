package com.campushire;

import com.campushire.model.*;
import com.campushire.service.EligibilityEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the EligibilityEngine.
 * Tests all boundary conditions per the specification.
 */
class EligibilityEngineTest {

    private EligibilityEngine engine;
    private RecruitmentDrive  drive;

    @BeforeEach
    void setUp() {
        engine = new EligibilityEngine();
        drive  = new RecruitmentDrive("D1", "C1", "Acme Corp", "Java Developer");
        drive.setMinimumCgpa(7.5);
        drive.setMaximumBacklogs(1);
        drive.setEligibleDepartments(List.of("CSE", "IT"));
        drive.setRequiredSkills(List.of("Java", "SQL"));
    }

    private Student student(double cgpa, int backlogs, String dept) {
        Student s = new Student("S1", "Test", "Student", "t@t.com", dept, 2025, cgpa, backlogs);
        s.setSkills(List.of("Java", "SQL", "Python"));
        return s;
    }

    // -----------------------------------------------------------------------
    // CGPA tests
    // -----------------------------------------------------------------------
    @Test
    void cgpaExactlyAtMinimum_shouldBeEligible() {
        EligibilityResult r = engine.evaluate(student(7.5, 0, "CSE"), drive);
        assertTrue(r.isEligible(), "CGPA exactly at minimum should be eligible");
    }

    @Test
    void cgpaAboveMinimum_shouldBeEligible() {
        EligibilityResult r = engine.evaluate(student(9.0, 0, "CSE"), drive);
        assertTrue(r.isEligible());
    }

    @Test
    void cgpaBelowMinimum_shouldBeIneligible() {
        EligibilityResult r = engine.evaluate(student(7.4, 0, "CSE"), drive);
        assertFalse(r.isEligible());
        assertTrue(r.getFailureReasons().stream()
            .anyMatch(reason -> reason.contains("CGPA")),
            "Failure reason should mention CGPA");
    }

    // -----------------------------------------------------------------------
    // Backlog tests
    // -----------------------------------------------------------------------
    @Test
    void backlogsAtMaximum_shouldBeEligible() {
        EligibilityResult r = engine.evaluate(student(8.0, 1, "CSE"), drive);
        assertTrue(r.isEligible(), "Backlogs at maximum should be eligible");
    }

    @Test
    void backlogsExceedMaximum_shouldBeIneligible() {
        EligibilityResult r = engine.evaluate(student(8.0, 2, "CSE"), drive);
        assertFalse(r.isEligible());
        assertTrue(r.getFailureReasons().stream()
            .anyMatch(reason -> reason.contains("backlog")),
            "Failure reason should mention backlogs");
    }

    @Test
    void zeroBacklogs_shouldBeEligible() {
        EligibilityResult r = engine.evaluate(student(8.0, 0, "CSE"), drive);
        assertTrue(r.isEligible());
    }

    // -----------------------------------------------------------------------
    // Department tests
    // -----------------------------------------------------------------------
    @Test
    void eligibleDepartment_shouldBeEligible() {
        EligibilityResult r = engine.evaluate(student(8.0, 0, "IT"), drive);
        assertTrue(r.isEligible());
    }

    @Test
    void ineligibleDepartment_shouldBeIneligible() {
        EligibilityResult r = engine.evaluate(student(9.0, 0, "MECH"), drive);
        assertFalse(r.isEligible());
        assertTrue(r.getFailureReasons().stream()
            .anyMatch(reason -> reason.contains("Department")),
            "Failure reason should mention department");
    }

    @Test
    void emptyEligibleDepartments_meansAllAllowed() {
        RecruitmentDrive openDrive = new RecruitmentDrive("D2", "C1", "Acme", "Analyst");
        openDrive.setMinimumCgpa(6.0);
        openDrive.setMaximumBacklogs(5);
        openDrive.setEligibleDepartments(List.of()); // empty = all departments
        EligibilityResult r = engine.evaluate(student(8.0, 0, "MECH"), openDrive);
        assertTrue(r.isEligible(), "Empty dept list should allow all departments");
    }

    // -----------------------------------------------------------------------
    // Multiple failures
    // -----------------------------------------------------------------------
    @Test
    void multipleFails_reportAllReasons() {
        EligibilityResult r = engine.evaluate(student(5.0, 5, "CIVIL"), drive);
        assertFalse(r.isEligible());
        // Should have reasons for CGPA, backlogs, and department
        assertEquals(3, r.getFailureReasons().size(),
            "Should report all 3 failure reasons");
    }
}
