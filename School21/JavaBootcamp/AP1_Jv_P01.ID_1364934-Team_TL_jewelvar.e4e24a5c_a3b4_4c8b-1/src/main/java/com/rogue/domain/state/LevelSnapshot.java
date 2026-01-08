package com.rogue.domain.state;

import com.rogue.domain.util.Position;

import java.util.List;

public record LevelSnapshot(
        char[][] map,
        List<MonsterSnapshot> monsters,
        Position playerPosition,
        char[][] seenMap) {

    public LevelSnapshot {
        map = copyGrid(map);
        seenMap = copyGrid(seenMap);
        monsters = monsters == null ? List.of() : List.copyOf(monsters);
    }

    private static char[][] copyGrid(char[][] source) {
        if (source == null) {
            return null;
        }
        char[][] copy = new char[source.length][];
        for (int i = 0; i < source.length; i++) {
            char[] row = source[i];
            copy[i] = row == null ? null : row.clone();
        }
        return copy;
    }
}
