package com.campushire.service;

import com.campushire.model.EligibilityResult;

/**
 * Default ranking strategy using configurable weights:
 *   Academic performance : 30%
 *   Skill compatibility  : 40%
 *   Projects             : 15%
 *   Experience           : 15%
 *
 * All component scores are pre-normalised to 0–100 before this method is called.
 */
public class WeightedRankingStrategy implements RankingStrategy {

    private final double academicWeight;
    private final double skillWeight;
    private final double projectWeight;
    private final double experienceWeight;

    /** Default constructor using the specification weights. */
    public WeightedRankingStrategy() {
        this(0.30, 0.40, 0.15, 0.15);
    }

    public WeightedRankingStrategy(double academic, double skill,
                                   double project, double experience) {
        double total = academic + skill + project + experience;
        if (Math.abs(total - 1.0) > 0.001) {
            throw new IllegalArgumentException("Weights must sum to 1.0, got: " + total);
        }
        this.academicWeight    = academic;
        this.skillWeight       = skill;
        this.projectWeight     = project;
        this.experienceWeight  = experience;
    }

    @Override
    public double score(EligibilityResult result) {
        double raw =
            result.getAcademicScore()    * academicWeight +
            result.getSkillScore()       * skillWeight +
            result.getProjectScore()     * projectWeight +
            result.getExperienceScore()  * experienceWeight;

        return Math.max(0.0, Math.min(100.0, raw));
    }

    @Override
    public String getDescription() {
        return String.format(
            "Academic %.0f%% · Skills %.0f%% · Projects %.0f%% · Experience %.0f%%",
            academicWeight * 100, skillWeight * 100,
            projectWeight * 100, experienceWeight * 100);
    }
}
