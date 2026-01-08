package com.rogue.datalayer.model;

import java.util.List;

public record SavedLevel(
        List<String> mapRows,
        List<MonsterData> monsters,
        PositionData playerPosition,
        List<String> seenMap) {
}
