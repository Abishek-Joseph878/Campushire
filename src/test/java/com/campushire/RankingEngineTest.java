package com.campushire;

import com.campushire.model.*;
import com.campushire.service.RankingEngine;
import com.campushire.service.WeightedRankingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RankingEngineTest {

    private RankingEngine     engine;
    private RecruitmentDrive  drive;

    @BeforeEach
    void setUp() {
        engine = new RankingEngine();
        drive  = new RecruitmentDrive("D1", "C1", "Acme", "Java Dev");
        drive.setMinimumCgpa(7.0);
        drive.setMaximumBacklogs(1);
        drive.setEligibleDepartments(List.of("CSE"));
        drive.setRequiredSkills(List.of("Java", "SQL"));
        drive.setPreferredSkills(List.of("Docker"));
    }

    private Student student(String id, double cgpa, int backlogs, List<String> skills) {
        Student s = new Student(id, "Test", id, "t@t.com", "CSE", 2025, cgpa, backlogs);
        s.setSkills(skills);
        s.setProjectCount(2);
        s.setInternshipCount(1);
        return s;
    }

    @Test
    void eligibleStudentsRankedHigherThanIneligible() {
        Student eligible   = student("S1", 8.0, 0, List.of("Java", "SQL"));
        Student ineligible = student("S2", 5.0, 0, List.of("Java", "SQL")); // CGPA fails

        List<EligibilityResult> results = engine.evaluateAndRank(
            List.of(ineligible, eligible), drive);

        assertTrue(results.get(0).isEligible(), "First result should be eligible");
        assertFalse(results.get(results.size() - 1).isEligible(), "Last result should be ineligible");
    }

    @Test
    void higherCgpaRanksHigherWhenSkillsEqual() {
        Student high = student("S1", 9.5, 0, List.of("Java", "SQL"));
        Student low  = student("S2", 7.5, 0, List.of("Java", "SQL"));

        List<EligibilityResult> results = engine.evaluateAndRank(List.of(low, high), drive);

        // Both eligible; high CGPA student should rank first
        assertEquals("S1", results.get(0).getStudent().getId());
    }

    @Test
    void scoreWithinBounds() {
        Student s = student("S1", 9.0, 0, List.of("Java", "SQL", "Docker"));
        s.setProjectCount(4);
        s.setInternshipCount(3);
        s.setCertificationCount(2);

        List<EligibilityResult> results = engine.evaluateAndRank(List.of(s), drive);
        double score = results.get(0).getTotalScore();
        assertTrue(score >= 0 && score <= 100,
            "Score must be in [0, 100], was: " + score);
    }

    @Test
    void ineligibleStudentHasZeroScore() {
        Student s = student("S1", 5.0, 5, List.of());
        List<EligibilityResult> results = engine.evaluateAndRank(List.of(s), drive);
        assertFalse(results.get(0).isEligible());
        assertEquals(0.0, results.get(0).getTotalScore(), 0.001);
    }

    @Test
    void weightsMustSumToOne() {
        assertThrows(IllegalArgumentException.class,
            () -> new WeightedRankingStrategy(0.5, 0.5, 0.5, 0.5),
            "Weights summing to more than 1.0 should throw");
    }

    @Test
    void emptyStudentList_returnsEmptyResults() {
        List<EligibilityResult> results = engine.evaluateAndRank(List.of(), drive);
        assertTrue(results.isEmpty());
    }
}
