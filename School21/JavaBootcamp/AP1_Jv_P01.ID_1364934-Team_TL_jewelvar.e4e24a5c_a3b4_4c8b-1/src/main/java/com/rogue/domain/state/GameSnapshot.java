package com.rogue.domain.state;

import java.util.List;

public record GameSnapshot(
        int currentLevel,
        int turnCounter,
        PlayerSnapshot player,
        LevelSnapshot level,
        List<String> messages,
        int monstersDefeated,
        int foodConsumed,
        int elixirsConsumed,
        int scrollsRead,
        int hitsDealt,
        int hitsTaken,
        int tilesTraversed) {

    public GameSnapshot {
        if (player == null) {
            throw new IllegalArgumentException("Player snapshot must not be null");
        }
        if (level == null) {
            throw new IllegalArgumentException("Level snapshot must not be null");
        }
        messages = messages == null ? List.of() : List.copyOf(messages);
    }
}
