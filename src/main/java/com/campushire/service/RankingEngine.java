package com.campushire.service;

import com.campushire.model.EligibilityResult;
import com.campushire.model.RecruitmentDrive;
import com.campushire.model.Student;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Orchestrates eligibility evaluation, skill matching, and final scoring.
 * Returns a ranked list of EligibilityResult objects for eligible candidates.
 */
public class RankingEngine {

    private final EligibilityEngine eligibilityEngine;
    private final SkillMatcher       skillMatcher;
    private RankingStrategy          strategy;

    public RankingEngine() {
        this.eligibilityEngine = new EligibilityEngine();
        this.skillMatcher      = new SkillMatcher();
        this.strategy          = new WeightedRankingStrategy();
    }

    public void setStrategy(RankingStrategy strategy) {
        this.strategy = strategy;
    }

    public RankingStrategy getStrategy() {
        return strategy;
    }

    /**
     * Evaluates all students against a drive and returns results.
     * Eligible candidates are scored and ranked (highest first).
     * Ineligible candidates are appended unranked.
     */
    public List<EligibilityResult> evaluateAndRank(List<Student> students, RecruitmentDrive drive) {
        List<EligibilityResult> eligible   = new ArrayList<>();
        List<EligibilityResult> ineligible = new ArrayList<>();

        for (Student student : students) {
            // Step 1: check mandatory eligibility
            EligibilityResult raw = eligibilityEngine.evaluate(student, drive);

            // Step 2: run skill matching regardless (to show matched/missing even for ineligible)
            SkillMatcher.SkillMatchResult skillMatch = skillMatcher.match(student, drive);

            // Step 3: compute component scores
            double academic    = student.getAcademicScore();
            double skillScore  = skillMatch.skillScore();
            double project     = student.getProjectScore();
            double experience  = student.getExperienceScore();

            // Rebuild result with full detail using Builder
            EligibilityResult.Builder builder = new EligibilityResult.Builder(
                    student, drive, raw.isEligible());

            raw.getFailureReasons().forEach(builder::failureReason);
            builder.matchedSkills(new ArrayList<>(skillMatch.matchedRequired()));
            builder.missingSkills(new ArrayList<>(skillMatch.missingRequired()));

            EligibilityResult result = builder.build();
            result.setAcademicScore(academic);
            result.setSkillScore(skillScore);
            result.setProjectScore(project);
            result.setExperienceScore(experience);

            if (result.isEligible()) {
                double total = strategy.score(result);
                result.setTotalScore(total);
                eligible.add(result);
            } else {
                ineligible.add(result);
            }
        }

        // Rank eligible candidates highest score first
        eligible.sort(Comparator.comparingDouble(EligibilityResult::getTotalScore).reversed());

        List<EligibilityResult> all = new ArrayList<>(eligible);
        all.addAll(ineligible);
        return all;
    }
}
