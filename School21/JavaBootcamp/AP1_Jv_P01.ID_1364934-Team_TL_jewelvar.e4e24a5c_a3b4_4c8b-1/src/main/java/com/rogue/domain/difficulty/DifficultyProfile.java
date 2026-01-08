package com.rogue.domain.difficulty;

public record DifficultyProfile(
        double monsterDensity,
        double eliteBias,
        double itemAbundance,
        double healthItemBonus) {

    public static DifficultyProfile neutral() {
        return new DifficultyProfile(1.0, 0.0, 1.0, 0.0);
    }
}
