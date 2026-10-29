package com.campushire;

import com.campushire.model.RecruitmentDrive;
import com.campushire.model.Student;
import com.campushire.service.SkillMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SkillMatcherTest {

    private SkillMatcher matcher;
    private RecruitmentDrive drive;

    @BeforeEach
    void setUp() {
        matcher = new SkillMatcher();
        drive   = new RecruitmentDrive("D1", "C1", "Acme", "Dev");
        drive.setRequiredSkills(List.of("Java", "SQL", "Spring Boot"));
        drive.setPreferredSkills(List.of("Docker", "AWS"));
    }

    private Student studentWith(List<String> skills) {
        Student s = new Student("S1", "Test", "Student", "t@t.com", "CSE", 2025, 8.0, 0);
        s.setSkills(skills);
        return s;
    }

    @Test
    void perfectSkillMatch_scores100() {
        Student s = studentWith(List.of("Java", "SQL", "Spring Boot", "Docker", "AWS"));
        SkillMatcher.SkillMatchResult result = matcher.match(s, drive);
        assertEquals(100.0, result.skillScore(), 0.01);
        assertEquals(3, result.matchedRequired().size());
        assertEquals(0, result.missingRequired().size());
    }

    @Test
    void noSkillsMatch_scores0() {
        Student s = studentWith(List.of("PHP", "Ruby", "Perl"));
        SkillMatcher.SkillMatchResult result = matcher.match(s, drive);
        // Required: 0/3 = 0%, preferred: 0/2 = 0% → score 0
        assertEquals(0.0, result.skillScore(), 0.01);
        assertEquals(3, result.missingRequired().size());
    }

    @Test
    void caseInsensitiveMatching() {
        Student s = studentWith(List.of("java", "sql", "SPRING BOOT"));
        SkillMatcher.SkillMatchResult result = matcher.match(s, drive);
        assertEquals(3, result.matchedRequired().size());
    }

    @Test
    void partialRequiredMatch_calculatesCorrectly() {
        // Matches 2 of 3 required: 66.7% required weight
        Student s = studentWith(List.of("Java", "SQL"));
        SkillMatcher.SkillMatchResult result = matcher.match(s, drive);
        double expectedReq  = (2.0 / 3.0) * 100.0;
        double expectedPref = 0.0; // no preferred matched
        double expected     = (expectedReq * 0.70) + (expectedPref * 0.30);
        assertEquals(expected, result.skillScore(), 0.01);
    }

    @Test
    void noDriveSkillsDefined_scores100() {
        RecruitmentDrive empty = new RecruitmentDrive("D2", "C1", "Acme", "Any");
        empty.setRequiredSkills(List.of());
        empty.setPreferredSkills(List.of());
        Student s = studentWith(List.of());
        SkillMatcher.SkillMatchResult result = matcher.match(s, empty);
        assertEquals(100.0, result.skillScore(), 0.01);
    }
}
