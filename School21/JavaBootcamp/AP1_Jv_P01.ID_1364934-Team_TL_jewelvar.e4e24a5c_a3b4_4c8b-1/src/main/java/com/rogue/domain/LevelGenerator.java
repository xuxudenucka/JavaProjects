package com.rogue.domain;

import com.rogue.domain.difficulty.DifficultyProfile;
import com.rogue.domain.entities.MonsterType;
import com.rogue.domain.entities.monsters.Mimic;
import com.rogue.domain.entities.monsters.Monster;
import com.rogue.domain.entities.monsters.MonsterFactory;
import com.rogue.domain.util.MapUtils;
import com.rogue.domain.util.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LevelGenerator {
    private static final Random RANDOM = new Random();

    private static final int MAP_WIDTH = 80;
    private static final int MAP_HEIGHT = 24;
    private static final int GRID_ROWS = 3;
    private static final int GRID_COLS = 3;
    private static final int MIN_ROOM_SIZE = 6;
    private static final int MAX_ROOM_SIZE = 12;

    public static LevelData generateLevel(int levelNumber, DifficultyProfile difficultyProfile) {
        DifficultyProfile profile = difficultyProfile != null
                ? difficultyProfile
                : DifficultyProfile.neutral();
        char[][] map = createEmptyMap();
        Room[][] roomGrid = generateRoomGrid(map);
        List<Room> rooms = flattenRoomGrid(roomGrid);

        connectRooms(map, roomGrid);
        Room startRoom = chooseRandomRoom(rooms);
        Room exitRoom = chooseDifferentRoom(rooms, startRoom);

        Position startPosition = placeStairs(map, startRoom, exitRoom);
        scatterGold(map, rooms, levelNumber, startRoom);
        scatterItems(map, rooms, levelNumber, startRoom, profile);
        List<Monster> monsters = placeMonsters(map, rooms, levelNumber, startRoom, profile);
        return new LevelData(map, monsters, startPosition);
    }

    private static char[][] createEmptyMap() {
        char[][] map = new char[MAP_HEIGHT][MAP_WIDTH];

        for (int y = 0; y < MAP_HEIGHT; y++) {
            for (int x = 0; x < MAP_WIDTH; x++) {
                map[y][x] = ' ';
            }
        }

        return map;
    }

    private static Room[][] generateRoomGrid(char[][] map) {
        Room[][] grid = new Room[GRID_ROWS][GRID_COLS];
        int regionWidth = MAP_WIDTH / GRID_COLS;
        int regionHeight = MAP_HEIGHT / GRID_ROWS;

        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                Room room = createRoomInRegion(row, col, regionWidth, regionHeight);
                carveRoom(map, room);
                grid[row][col] = room;
            }
        }

        return grid;
    }

    private static Room createRoomInRegion(int row, int col, int regionWidth, int regionHeight) {
        int maxWidth = Math.max(MIN_ROOM_SIZE, Math.min(MAX_ROOM_SIZE, regionWidth - 2));
        int maxHeight = Math.max(MIN_ROOM_SIZE, Math.min(MAX_ROOM_SIZE, regionHeight - 2));

        int width = randomBetween(MIN_ROOM_SIZE, maxWidth);
        int height = randomBetween(MIN_ROOM_SIZE, maxHeight);

        int regionX = col * regionWidth;
        int regionY = row * regionHeight;

        int minX = Math.max(1, regionX + 1);
        int maxX = Math.min(MAP_WIDTH - width - 1, regionX + regionWidth - width - 1);
        if (maxX < minX) {
            maxX = minX;
        }

        int minY = Math.max(1, regionY + 1);
        int maxY = Math.min(MAP_HEIGHT - height - 1, regionY + regionHeight - height - 1);
        if (maxY < minY) {
            maxY = minY;
        }

        int x = randomBetween(minX, maxX);
        int y = randomBetween(minY, maxY);

        return new Room(x, y, width, height);
    }

    private static List<Room> flattenRoomGrid(Room[][] grid) {
        List<Room> rooms = new ArrayList<>();
        for (Room[] row : grid) {
            for (Room room : row) {
                if (room != null) {
                    rooms.add(room);
                }
            }
        }
        return rooms;
    }

    private static void carveRoom(char[][] map, Room room) {
        int bottom = room.y + room.height - 1;
        int right = room.x + room.width - 1;

        for (int y = room.y; y <= bottom; y++) {
            for (int x = room.x; x <= right; x++) {
                boolean isWall = (y == room.y || y == bottom || x == room.x || x == right);
                map[y][x] = isWall ? '#' : MapUtils.ROOM_FLOOR;
            }
        }
    }

    private static void connectRooms(char[][] map, Room[][] grid) {
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                Room room = grid[row][col];
                if (room == null) {
                    continue;
                }

                if (col < GRID_COLS - 1 && grid[row][col + 1] != null) {
                    connectHorizontally(map, room, grid[row][col + 1]);
                }
                if (row < GRID_ROWS - 1 && grid[row + 1][col] != null) {
                    connectVertically(map, room, grid[row + 1][col]);
                }
            }
        }
    }

    private static void connectHorizontally(char[][] map, Room left, Room right) {
        int leftDoorY = randomBetween(left.y + 1, left.y + left.height - 2);
        int rightDoorY = randomBetween(right.y + 1, right.y + right.height - 2);
        int leftDoorX = left.x + left.width - 1;
        int rightDoorX = right.x;

        map[leftDoorY][leftDoorX] = '+';
        map[rightDoorY][rightDoorX] = '+';

        int startX = clamp(leftDoorX + 1, MAP_WIDTH - 2);
        int endX = clamp(rightDoorX - 1, MAP_WIDTH - 2);

        digCorridor(map, startX, leftDoorY, endX, rightDoorY);
    }

    private static void connectVertically(char[][] map, Room top, Room bottom) {
        int topDoorX = randomBetween(top.x + 1, top.x + top.width - 2);
        int bottomDoorX = randomBetween(bottom.x + 1, bottom.x + bottom.width - 2);
        int topDoorY = top.y + top.height - 1;
        int bottomDoorY = bottom.y;

        map[topDoorY][topDoorX] = '+';
        map[bottomDoorY][bottomDoorX] = '+';

        int startY = clamp(topDoorY + 1, MAP_HEIGHT - 2);
        int endY = clamp(bottomDoorY - 1, MAP_HEIGHT - 2);

        digCorridor(map, topDoorX, startY, bottomDoorX, endY);
    }

    private static void digCorridor(char[][] map, int startX, int startY, int endX, int endY) {
        if (RANDOM.nextBoolean()) {
            carveHorizontal(map, startX, endX, startY);
            carveVertical(map, startY, endY, endX);
        } else {
            carveVertical(map, startY, endY, startX);
            carveHorizontal(map, startX, endX, endY);
        }
    }

    private static void carveHorizontal(char[][] map, int fromX, int toX, int y) {
        int start = Math.max(1, Math.min(fromX, toX));
        int end = Math.min(MAP_WIDTH - 2, Math.max(fromX, toX));
        for (int x = start; x <= end; x++) {
            digCorridorTile(map, x, y);
        }
    }

    private static void carveVertical(char[][] map, int fromY, int toY, int x) {
        int start = Math.max(1, Math.min(fromY, toY));
        int end = Math.min(MAP_HEIGHT - 2, Math.max(fromY, toY));
        for (int y = start; y <= end; y++) {
            digCorridorTile(map, x, y);
        }
    }

    private static void digCorridorTile(char[][] map, int x, int y) {
        if (!withinBounds(x, y)) {
            return;
        }
        char tile = map[y][x];
        if (tile == ' ' || tile == '#') {
            map[y][x] = MapUtils.CORRIDOR_FLOOR;
        }
    }

    private static boolean withinBounds(int x, int y) {
        return x > 0 && x < MAP_WIDTH - 1 && y > 0 && y < MAP_HEIGHT - 1;
    }

    private static int clamp(int value, int max) {
        if (value < 1) {
            return 1;
        }
        return Math.min(value, max);
    }

    private static Room chooseRandomRoom(List<Room> rooms) {
        return rooms.get(RANDOM.nextInt(rooms.size()));
    }

    private static Room chooseDifferentRoom(List<Room> rooms, Room exclude) {
        Room candidate;
        do {
            candidate = rooms.get(RANDOM.nextInt(rooms.size()));
        } while (candidate == exclude);
        return candidate;
    }

    private static Position placeStairs(char[][] map, Room startRoom, Room exitRoom) {
        Position startPosition = randomFloorPosition(startRoom);
        map[startPosition.y()][startPosition.x()] = '<';
        if (exitRoom != null) {
            Position exitPosition = randomFloorPosition(exitRoom);
            map[exitPosition.y()][exitPosition.x()] = '>';
        }
        return startPosition;
    }

    private static void scatterGold(char[][] map, List<Room> rooms, int levelNumber, Room startRoom) {
        if (rooms.isEmpty()) {
            return;
        }

        int treasures = Math.max(1, rooms.size() / 3);
        treasures += RANDOM.nextInt(levelNumber + 1);

        for (int i = 0; i < treasures; i++) {
            Room room = rooms.get(RANDOM.nextInt(rooms.size()));
            if (room == startRoom) {
                continue;
            }
            int x = randomBetween(room.x + 1, room.x + room.width - 2);
            int y = randomBetween(room.y + 1, room.y + room.height - 2);
                if (map[y][x] == MapUtils.ROOM_FLOOR) {
                    map[y][x] = '$';
                }
            }
    }

    private static void scatterItems(char[][] map,
                                     List<Room> rooms,
                                     int levelNumber,
                                     Room startRoom,
                                     DifficultyProfile profile) {
        if (rooms.isEmpty()) {
            return;
        }
        int difficulty = Math.max(1, levelNumber);
        int food = Math.max(1, 4 - difficulty / 5);
        int elixirs = Math.max(0, 3 - difficulty / 6);
        int scrolls = Math.max(0, 3 - difficulty / 7) + difficulty / 10;
        int weapons = Math.max(1, 2 + difficulty / 6);

        double abundance = profile.itemAbundance();
        food = scaleCount(food, abundance + profile.healthItemBonus(), 1);
        elixirs = scaleCount(elixirs, abundance + profile.healthItemBonus() * 0.6, 0);
        scrolls = scaleCount(scrolls, abundance, 0);
        weapons = scaleCount(weapons, abundance, 1);

        placeItems(map, rooms, startRoom, 'f', food);
        placeItems(map, rooms, startRoom, 'e', Math.max(0, elixirs));
        placeItems(map, rooms, startRoom, 's', Math.max(0, scrolls));
        placeItems(map, rooms, startRoom, 'w', weapons);
    }

    private static void placeItems(char[][] map, List<Room> rooms, Room startRoom, char symbol, int count) {
        for (int i = 0; i < count; i++) {
            for (int attempt = 0; attempt < 30; attempt++) {
                Room room = rooms.get(RANDOM.nextInt(rooms.size()));
                if (room == startRoom) {
                    continue;
                }
                Position pos = randomFloorPosition(room);
                if (map[pos.y()][pos.x()] != MapUtils.ROOM_FLOOR) {
                    continue;
                }
                map[pos.y()][pos.x()] = symbol;
                break;
            }
        }
    }

    private static List<Monster> placeMonsters(char[][] map,
                                               List<Room> rooms,
                                               int levelNumber,
                                               Room startRoom,
                                               DifficultyProfile profile) {
        List<Monster> monsters = new ArrayList<>();

        int spawnCount = Math.max(3, Math.min(rooms.size() * 2, levelNumber + 4 + RANDOM.nextInt(rooms.size())));
        spawnCount = Math.max(2, scaleCount(spawnCount, profile.monsterDensity(), 2));
        for (int i = 0; i < spawnCount; i++) {
            MonsterType type = chooseMonsterType(levelNumber, profile);
            for (int attempt = 0; attempt < 25; attempt++) {
                Room room = rooms.get(RANDOM.nextInt(rooms.size()));
                if (room == startRoom) {
                    continue;
                }
                int x = randomBetween(room.x + 1, room.x + room.width - 2);
                int y = randomBetween(room.y + 1, room.y + room.height - 2);

                if (!MapUtils.isWalkable(map, x, y)) {
                    continue;
                }
                if (map[y][x] == '<' || map[y][x] == '>') {
                    continue;
                }
                if (isMonsterAt(monsters, x, y)) {
                    continue;
                }
                Monster monster = MonsterFactory.create(type, x, y);
                if (monster instanceof Mimic mimic) {
                    mimic.applyDisguise(map);
                }
                monsters.add(monster);
                break;
            }
        }
        return monsters;
    }

    private static MonsterType chooseMonsterType(int levelNumber, DifficultyProfile profile) {
        int roll = RANDOM.nextInt(100);
        double bias = profile.eliteBias();
        if (bias > 0 && RANDOM.nextDouble() < bias) {
            roll = Math.min(99, roll + (int) Math.round(25 * bias));
        } else if (bias < 0 && RANDOM.nextDouble() < -bias) {
            roll = Math.max(0, roll - (int) Math.round(25 * -bias));
        }
        if (levelNumber < 5) {
            if (roll < 40) {
                return MonsterType.ZOMBIE;
            }
            if (roll < 65) {
                return MonsterType.GHOST;
            }
            if (roll < 85) {
                return MonsterType.MIMIC;
            }
            return MonsterType.VAMPIRE;
        } else if (levelNumber < 10) {
            if (roll < 30) {
                return MonsterType.ZOMBIE;
            }
            if (roll < 50) {
                return MonsterType.GHOST;
            }
            if (roll < 65) {
                return MonsterType.MIMIC;
            }
            if (roll < 80) {
                return MonsterType.VAMPIRE;
            }
            if (roll < 90) {
                return MonsterType.OGRE;
            }
            return MonsterType.SNAKE_MAGE;
        } else {
            if (roll < 20) {
                return MonsterType.ZOMBIE;
            }
            if (roll < 35) {
                return MonsterType.GHOST;
            }
            if (roll < 50) {
                return MonsterType.MIMIC;
            }
            if (roll < 70) {
                return MonsterType.VAMPIRE;
            }
            if (roll < 85) {
                return MonsterType.OGRE;
            }
            return MonsterType.SNAKE_MAGE;
        }
    }

    private static boolean isMonsterAt(List<Monster> monsters, int x, int y) {
        for (Monster monster : monsters) {
            if (monster.getX() == x && monster.getY() == y) {
                return true;
            }
        }
        return false;
    }

    private static int randomBetween(int minInclusive, int maxInclusive) {
        return minInclusive + RANDOM.nextInt(Math.max(1, maxInclusive - minInclusive + 1));
    }

    private static Position randomFloorPosition(Room room) {
        int x = randomBetween(room.x + 1, room.x + room.width - 2);
        int y = randomBetween(room.y + 1, room.y + room.height - 2);
        return new Position(x, y);
    }

    private static int scaleCount(int base, double multiplier, int minimum) {
        int adjusted = (int) Math.round(base * multiplier);
        return Math.max(minimum, adjusted);
    }

    public static class Room {
        public int x, y;
        public int width, height;

        public Room(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    public record LevelData(char[][] map, List<Monster> monsters, Position startPosition) {
    }
}
