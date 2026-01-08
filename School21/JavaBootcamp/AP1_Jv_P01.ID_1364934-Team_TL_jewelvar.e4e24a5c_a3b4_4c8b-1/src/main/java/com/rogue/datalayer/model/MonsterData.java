package com.rogue.datalayer.model;

import java.util.Map;

public record MonsterData(
        String type,
        int x,
        int y,
        int health,
        Map<String, Integer> extras) {
}
