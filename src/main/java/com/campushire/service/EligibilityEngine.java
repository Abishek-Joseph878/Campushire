package com.campushire.service;

import com.campushire.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Evaluates whether a student meets the mandatory eligibility criteria
 * for a recruitment drive.
 *
 * Mandatory rules (all must pass):
 *   1. CGPA >= drive's minimumCgpa
 *   2. activeBacklogs <= drive's maximumBacklogs
 *   3. student's department is in the drive's eligible departments
 *      (empty list means all departments are eligible)
 */
public class EligibilityEngine {

    /**
     * Evaluates one student against one drive.
     * Returns an EligibilityResult with pass/fail decision and explanations.
     */
    public EligibilityResult evaluate(Student student, RecruitmentDrive drive) {
        List<String> failures = new ArrayList<>();

        // --- Rule 1: CGPA check ---
        if (student.getCgpa() < drive.getMinimumCgpa()) {
            failures.add(String.format(
                "CGPA %.2f is below the required minimum of %.2f.",
                student.getCgpa(), drive.getMinimumCgpa()));
        }

        // --- Rule 2: Backlog check ---
        if (student.getActiveBacklogs() > drive.getMaximumBacklogs()) {
            failures.add(String.format(
                "Active backlogs (%d) exceed the maximum allowed (%d).",
                student.getActiveBacklogs(), drive.getMaximumBacklogs()));
        }

        // --- Rule 3: Department eligibility ---
        List<String> eligibleDepts = drive.getEligibleDepartments();
        if (!eligibleDepts.isEmpty()) {
            boolean deptMatch = eligibleDepts.stream()
                .anyMatch(d -> d.equalsIgnoreCase(student.getDepartment()));
            if (!deptMatch) {
                failures.add(String.format(
                    "Department '%s' is not in the eligible departments: %s.",
                    student.getDepartment(),
                    String.join(", ", eligibleDepts)));
            }
        }

        boolean eligible = failures.isEmpty();
        EligibilityResult.Builder builder = new EligibilityResult.Builder(student, drive, eligible);
        failures.forEach(builder::failureReason);
        return builder.build();
    }

    /**
     * Evaluates all students against a drive.
     */
    public List<EligibilityResult> evaluateAll(List<Student> students, RecruitmentDrive drive) {
        List<EligibilityResult> results = new ArrayList<>();
        for (Student s : students) {
            results.add(evaluate(s, drive));
        }
        return results;
    }
}
