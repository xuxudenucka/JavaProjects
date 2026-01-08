package com.rogue.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ScoreBoard {
    private final List<ScoreRecord> records = new ArrayList<>();

    public void addRecord(ScoreRecord record) {
        records.add(record);
        records.sort(Comparator.comparingInt(ScoreRecord::treasureCollected).reversed());
    }

    public List<ScoreRecord> getRecords() {
        return Collections.unmodifiableList(records);
    }

    public List<ScoreRecord> topRecords(int limit) {
        int size = Math.min(limit, records.size());
        return List.copyOf(records.subList(0, size));
    }

    public void loadRecords(List<ScoreRecord> loaded) {
        records.clear();
        if (loaded != null) {
            records.addAll(loaded);
        }
        records.sort(Comparator.comparingInt(ScoreRecord::treasureCollected).reversed());
    }

    public record ScoreRecord(
            String playerName,
            int depthReached,
            int treasureCollected,
            int monstersDefeated,
            int foodEaten,
            int elixirsDrunk,
            int scrollsRead,
            int hitsDealt,
            int hitsTaken,
            int tilesTraversed) {
    }
}
