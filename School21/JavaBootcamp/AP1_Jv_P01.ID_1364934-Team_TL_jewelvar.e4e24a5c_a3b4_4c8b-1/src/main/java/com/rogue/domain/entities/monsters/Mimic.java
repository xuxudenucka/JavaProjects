package com.rogue.domain.entities.monsters;

import com.rogue.domain.entities.MonsterType;
import com.rogue.domain.util.MapUtils;
import com.rogue.domain.util.Position;

import java.util.List;

public class Mimic extends Monster {
    private static final char[] DISGUISE_TILES = {'f', 'e', 's', 'w', '$'};

    private boolean revealed;
    private char disguiseTile;
    private char baseTile;

    public Mimic(int x, int y) {
        super(MonsterType.MIMIC, x, y);
        this.revealed = false;
        this.disguiseTile = DISGUISE_TILES[RANDOM.nextInt(DISGUISE_TILES.length)];
        this.baseTile = MapUtils.ROOM_FLOOR;
    }

    @Override
    public boolean isVisible() {
        return revealed;
    }

    public boolean isRevealed() {
        return revealed;
    }

    public void setRevealed(boolean revealed) {
        this.revealed = revealed;
    }

    public char getDisguiseTile() {
        return disguiseTile;
    }

    public void setDisguiseTile(char disguiseTile) {
        this.disguiseTile = disguiseTile;
    }

    public char getBaseTile() {
        return baseTile;
    }

    public void setBaseTile(char baseTile) {
        this.baseTile = baseTile;
    }

    public void applyDisguise(char[][] map) {
        if (revealed) {
            return;
        }
        if (map == null || y < 0 || y >= map.length || x < 0 || x >= map[y].length) {
            return;
        }
        if (MapUtils.isWalkableTile(map[y][x])) {
            baseTile = map[y][x];
            map[y][x] = disguiseTile;
        }
    }

    public void reveal(char[][] map) {
        if (revealed) {
            return;
        }
        revealed = true;
        if (map != null && y >= 0 && y < map.length && x >= 0 && x < map[y].length) {
            map[y][x] = baseTile;
        }
    }

    @Override
    protected Position choosePatternStep(char[][] map, List<Monster> monsters) {
        if (!revealed) {
            return null;
        }
        return randomAdjacentStep(map, monsters, true);
    }
}
