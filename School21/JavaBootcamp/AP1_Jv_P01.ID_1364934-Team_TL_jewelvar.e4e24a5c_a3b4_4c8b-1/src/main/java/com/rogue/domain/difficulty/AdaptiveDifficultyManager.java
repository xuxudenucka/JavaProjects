package com.rogue.domain.difficulty;

import com.rogue.domain.entities.player.Player;

public class AdaptiveDifficultyManager {
    private double difficultyScore;
    private LevelSnapshot currentLevel;

    public AdaptiveDifficultyManager() {
        reset();
    }

    public void reset() {
        difficultyScore = 0;
        currentLevel = new LevelSnapshot();
    }

    public DifficultyProfile currentProfile() {
        double density = clamp(1.0 + 0.18 * difficultyScore, 0.5, 1.8);
        double eliteBias = clamp(0.18 * difficultyScore, -0.45, 0.45);
        double abundance = clamp(1.0 - 0.12 * difficultyScore, 0.6, 1.6);
        double healthBonus = difficultyScore < 0 ? clamp(-difficultyScore * 0.35, 0.0, 0.8) : 0.0;
        return new DifficultyProfile(density, eliteBias, abundance, healthBonus);
    }

    public void onLevelStart(Player player) {
        currentLevel = new LevelSnapshot();
        if (player != null) {
            currentLevel.maxHealth = player.getMaxHealth();
            currentLevel.startHealth = player.getHealth();
        }
    }

    public void onLevelCompleted(Player player, boolean success) {
        if (currentLevel == null) {
            return;
        }
        if (player != null) {
            currentLevel.endHealth = player.getHealth();
            currentLevel.maxHealth = player.getMaxHealth();
        }
        double stress = (currentLevel.damageTaken / 18.0)
                + (currentLevel.lowHealthMoments * 0.7)
                + (currentLevel.healingReceived / 20.0);
        double mastery = (currentLevel.monstersDefeated / 6.0)
                + (currentLevel.turns > 0 ? Math.min(1.2, 45.0 / Math.max(10.0, currentLevel.turns)) : 0.0);
        if (player != null && player.getHealth() > Math.max(1, player.getMaxHealth() * 0.75) && currentLevel.damageTaken < 5) {
            mastery += 0.5;
        }
        double delta = mastery - stress;
        if (!success) {
            delta -= 1.4;
        }
        difficultyScore = clamp(difficultyScore + delta * 0.5, -2.5, 2.5);
    }

    public void recordTurn() {
        if (currentLevel == null) {
            return;
        }
        currentLevel.turns++;
    }

    public void recordDamageTaken(int amount, Player player) {
        if (currentLevel == null || amount <= 0) {
            return;
        }
        currentLevel.damageTaken += amount;
        if (player != null && player.getMaxHealth() > 0) {
            double ratio = (double) player.getHealth() / player.getMaxHealth();
            if (ratio < 0.35) {
                currentLevel.lowHealthMoments++;
            }
        }
    }

    public void recordHealing(int amount) {
        if (currentLevel == null || amount <= 0) {
            return;
        }
        currentLevel.healingReceived += amount;
    }

    public void recordMonsterDefeated() {
        if (currentLevel == null) {
            return;
        }
        currentLevel.monstersDefeated++;
    }

    private double clamp(double value, double min, double max) {
        if (value < min) {
            return min;
        }
        return Math.min(value, max);
    }

    private static class LevelSnapshot {
        int startHealth;
        int endHealth;
        int maxHealth;
        int damageTaken;
        int healingReceived;
        int monstersDefeated;
        int turns;
        int lowHealthMoments;
    }
}
