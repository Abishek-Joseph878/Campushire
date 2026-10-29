package com.campushire.service;

import com.campushire.model.EligibilityResult;

/**
 * Strategy interface for ranking eligible candidates.
 * Different scoring formulas can be plugged in without changing callers.
 */
public interface RankingStrategy {

    /**
     * Computes a final score in the range [0, 100] for an eligible candidate.
     *
     * @param result  the eligibility result containing component scores
     * @return        the composite score, 0–100
     */
    double score(EligibilityResult result);

    /** Human-readable description of the strategy for the UI. */
    String getDescription();
}
