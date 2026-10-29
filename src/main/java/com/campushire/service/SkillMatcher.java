package com.campushire.service;

import com.campushire.model.RecruitmentDrive;
import com.campushire.model.Student;

import java.util.ArrayList;
import java.util.List;

/**
 * Compares a student's skill set against a drive's required and preferred skills.
 * Case-insensitive matching.
 */
public class SkillMatcher {

    public record SkillMatchResult(
        List<String> matchedRequired,
        List<String> missingRequired,
        List<String> matchedPreferred,
        double skillScore          // 0–100
    ) {}

    /**
     * Scores skill compatibility.
     *
     * Formula:
     *   - Required skills carry 70% of the skill score.
     *   - Preferred skills carry 30%.
     *   - If no required skills defined, all weight goes to preferred (and vice versa).
     */
    public SkillMatchResult match(Student student, RecruitmentDrive drive) {
        List<String> studentSkills = normalise(student.getSkills());
        List<String> required  = normalise(drive.getRequiredSkills());
        List<String> preferred = normalise(drive.getPreferredSkills());

        List<String> matchedReq  = new ArrayList<>();
        List<String> missingReq  = new ArrayList<>();
        List<String> matchedPref = new ArrayList<>();

        for (String req : required) {
            if (studentSkills.contains(req)) matchedReq.add(req);
            else                             missingReq.add(req);
        }

        for (String pref : preferred) {
            if (studentSkills.contains(pref)) matchedPref.add(pref);
        }

        // Score calculation
        double reqScore  = required.isEmpty()  ? 100.0 : (matchedReq.size()  * 100.0 / required.size());
        double prefScore = preferred.isEmpty() ? 100.0 : (matchedPref.size() * 100.0 / preferred.size());

        double skillScore;
        if (required.isEmpty() && preferred.isEmpty()) {
            skillScore = 100.0; // no skill requirements
        } else if (required.isEmpty()) {
            skillScore = prefScore;
        } else if (preferred.isEmpty()) {
            skillScore = reqScore;
        } else {
            skillScore = (reqScore * 0.70) + (prefScore * 0.30);
        }

        return new SkillMatchResult(
            matchedReq,
            missingReq,
            matchedPref,
            Math.min(100.0, skillScore)
        );
    }

    private List<String> normalise(List<String> skills) {
        List<String> result = new ArrayList<>();
        for (String s : skills) {
            if (s != null && !s.isBlank()) result.add(s.trim().toLowerCase());
        }
        return result;
    }
}
