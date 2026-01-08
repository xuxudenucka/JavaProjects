package com.rogue.domain.util;

public final class MapUtils {
    public static final char ROOM_FLOOR = '.';
    public static final char CORRIDOR_FLOOR = ',';
    public static final char DOOR = '+';

    private MapUtils() {
    }

    public static boolean isWalkable(char[][] map, int x, int y) {
        if (map == null || map.length == 0) {
            return false;
        }
        if (y < 0 || y >= map.length) {
            return false;
        }
        if (x < 0 || x >= map[y].length) {
            return false;
        }
        return isWalkableTile(map[y][x]);
    }

    public static boolean isWalkableTile(char tile) {
        return switch (tile) {
            case ROOM_FLOOR, CORRIDOR_FLOOR, '+', '<', '>', '$', 'f', 'e', 's', 'w' -> true;
            default -> false;
        };
    }
}
