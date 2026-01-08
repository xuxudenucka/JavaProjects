package com.rogue.datalayer.model;

import java.util.List;

public record GameSaveData(
        int currentLevel,
        int turnCounter,
        PlayerData player,
        SavedLevel level,
        List<String> messages,
        int monstersDefeated,
        int foodConsumed,
        int elixirsConsumed,
        int scrollsRead,
        int hitsDealt,
        int hitsTaken,
        int tilesTraversed) {
}
