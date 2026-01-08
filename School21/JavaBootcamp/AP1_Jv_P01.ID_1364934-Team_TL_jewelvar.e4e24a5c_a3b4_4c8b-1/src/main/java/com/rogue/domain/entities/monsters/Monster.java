package com.rogue.domain.entities.monsters;

import com.rogue.domain.entities.Character;
import com.rogue.domain.entities.MonsterColor;
import com.rogue.domain.entities.player.Player;
import com.rogue.domain.entities.MonsterType;
import com.rogue.domain.util.MapUtils;
import com.rogue.domain.util.Position;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

public abstract class Monster extends Character {
    protected static final Random RANDOM = new Random();
    private static final int[][] CARDINAL_DIRECTIONS = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1}
    };
    private static final int[][] DIAGONAL_DIRECTIONS = {
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
    };

    protected final MonsterType type;
    protected int x;
    protected int y;
    private final int hostilityRadius;

    protected Monster(MonsterType type, int x, int y) {
        super(type.getHealth(), type.getStrength());
        this.type = type;
        this.x = x;
        this.y = y;
        this.hostilityRadius = type.getHostility();
    }

    public MonsterType getType() {
        return type;
    }

    public char getSymbol() {
        return type.getSymbol();
    }

    public MonsterColor getColor() {
        return type.getColor();
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getHostilityRadius() {
        return hostilityRadius;
    }

    public boolean isVisible() {
        return true;
    }

    public Optional<Position> decideNextPosition(Player player, char[][] map, List<Monster> monsters) {
        if (player == null || map == null) {
            return Optional.empty();
        }
        if (shouldChase(player)) {
            Position chaseStep = nextChaseStep(player, map, monsters);
            if (chaseStep != null) {
                return Optional.of(chaseStep);
            }
        }
        Position patternStep = choosePatternStep(map, monsters);
        if (patternStep != null) {
            return Optional.of(patternStep);
        }
        return Optional.empty();
    }

    protected abstract Position choosePatternStep(char[][] map, List<Monster> monsters);

    private boolean shouldChase(Player player) {
        int distance = Math.abs(player.getX() - x) + Math.abs(player.getY() - y);
        return distance <= hostilityRadius;
    }

    private Position nextChaseStep(Player player, char[][] map, List<Monster> monsters) {
        int height = map.length;
        int width = map[0].length;
        boolean[][] visited = new boolean[height][width];
        Position[][] previous = new Position[height][width];
        Queue<Position> queue = new ArrayDeque<>();
        queue.add(new Position(x, y));
        visited[y][x] = true;

        Set<String> occupied = occupiedPositions(monsters, this);

        while (!queue.isEmpty()) {
            Position current = queue.remove();
            if (current.x() == player.getX() && current.y() == player.getY()) {
                return backtrackStep(previous, current);
            }

            for (int[] dir : CARDINAL_DIRECTIONS) {
                int nx = current.x() + dir[0];
                int ny = current.y() + dir[1];
                if (!MapUtils.isWalkable(map, nx, ny) || visited[ny][nx]) {
                    continue;
                }
                if (occupied.contains(nx + "," + ny)) {
                    continue;
                }
                visited[ny][nx] = true;
                previous[ny][nx] = current;
                queue.add(new Position(nx, ny));
            }
        }
        return null;
    }

    private Position backtrackStep(Position[][] previous, Position target) {
        Position current = target;
        while (true) {
            Position prev = previous[current.y()][current.x()];
            if (prev == null) {
                return current;
            }
            if (prev.x() == x && prev.y() == y) {
                return current;
            }
            current = prev;
        }
    }

    protected Position randomAdjacentStep(char[][] map, List<Monster> monsters, boolean includeDiagonals) {
        List<int[]> directions = new ArrayList<>();
        Collections.addAll(directions, CARDINAL_DIRECTIONS);
        if (includeDiagonals) {
            Collections.addAll(directions, DIAGONAL_DIRECTIONS);
        }
        Collections.shuffle(directions, RANDOM);

        for (int[] dir : directions) {
            int nx = x + dir[0];
            int ny = y + dir[1];
            if (isStepFree(nx, ny, map, monsters)) {
                return new Position(nx, ny);
            }
        }
        return null;
    }

    protected Position randomWalkablePosition(char[][] map, List<Monster> monsters, int radius) {
        for (int attempts = 0; attempts < 20; attempts++) {
            int nx = x + RANDOM.nextInt(radius * 2 + 1) - radius;
            int ny = y + RANDOM.nextInt(radius * 2 + 1) - radius;
            if (isStepFree(nx, ny, map, monsters)) {
                return new Position(nx, ny);
            }
        }
        return null;
    }

    protected boolean isStepFree(int x, int y, char[][] map, List<Monster> monsters) {
        if (!MapUtils.isWalkable(map, x, y)) {
            return false;
        }
        if (monsters == null) {
            return true;
        }
        for (Monster monster : monsters) {
            if (monster == this) {
                continue;
            }
            if (monster.x == x && monster.y == y) {
                return false;
            }
        }
        return true;
    }

    private Set<String> occupiedPositions(List<Monster> monsters, Monster self) {
        Set<String> occupied = new HashSet<>();
        if (monsters == null) {
            return occupied;
        }
        for (Monster monster : monsters) {
            if (monster == self) {
                continue;
            }
            occupied.add(monster.x + "," + monster.y);
        }
        return occupied;
    }
}
