package com.rogue.domain.state;

import com.rogue.domain.entities.MonsterType;

import java.util.Map;

public record MonsterSnapshot(
        MonsterType type,
        int x,
        int y,
        int health,
        Map<String, Integer> extras) {

    public MonsterSnapshot {
        if (type == null) {
            throw new IllegalArgumentException("Monster type cannot be null");
        }
        extras = extras == null ? Map.of() : Map.copyOf(extras);
    }
}
