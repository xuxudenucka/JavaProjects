package com.rogue.domain.entities.player;

import com.rogue.domain.entities.Character;
import com.rogue.domain.util.MapUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Player extends Character {
    private final Inventory inventory;

    private final char[][] seenMap;
    private final boolean[][] visibleMap;
    private final List<TemporaryBuff> temporaryBuffs;
    private int x;
    private int y;

    private int level;
    private int gold;
    private int experience;
    private int nextLevelExp;
    private int maxStrengthValue;

    public Player(int startX,
                  int startY,
                  int mapWidth,
                  int mapHeight,
                  int backpackCapacity,
                  int baseMaxHealth,
                  int baseStrength) {
        super(baseMaxHealth, baseStrength);
        this.inventory = new Inventory(backpackCapacity);
        this.x = startX;
        this.y = startY;
        this.seenMap = new char[mapHeight][mapWidth];
        this.visibleMap = new boolean[mapHeight][mapWidth];
        this.temporaryBuffs = new ArrayList<>();
        this.level = 1;
        this.gold = 0;
        this.experience = 0;
        this.nextLevelExp = 10;
        this.maxStrengthValue = getStrength();
    }

    public Player(int startX, int startY, int mapWidth, int mapHeight) {
        this(startX, startY, mapWidth, mapHeight, 9, 12, 16);
    }

    public Inventory getInventory() {
        return inventory;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getMapWidth() {
        return seenMap.length == 0 ? 0 : seenMap[0].length;
    }

    public int getMapHeight() {
        return seenMap.length;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getLevel() {
        return level;
    }

    public int getGold() {
        return gold;
    }

    public int getExperience() {
        return experience;
    }

    public int getNextLevelExp() {
        return nextLevelExp;
    }

    public int getMaxStrength() {
        return maxStrengthValue;
    }

    public void resetSeenMap() {
        for (char[] row : seenMap) {
            Arrays.fill(row, (char) 0);
        }
        for (boolean[] row : visibleMap) {
            Arrays.fill(row, false);
        }
    }

    public boolean hasSeen(int x, int y) {
        if (x < 0 || x >= getMapWidth() || y < 0 || y >= getMapHeight()) {
            return false;
        }
        return seenMap[y][x] != 0;
    }

    public char getSeenTile(int x, int y) {
        if (x < 0 || x >= getMapWidth() || y < 0 || y >= getMapHeight()) {
            return 0;
        }
        return seenMap[y][x];
    }

    public boolean isTileVisible(int x, int y) {
        if (x < 0 || x >= getMapWidth() || y < 0 || y >= getMapHeight()) {
            return false;
        }
        return visibleMap[y][x];
    }

    public void markAsSeen(int x, int y, char tile) {
        if (x >= 0 && x < getMapWidth() && y >= 0 && y < getMapHeight()) {
            seenMap[y][x] = tile;
        }
    }

    public void updateVisibility(char[][] map) {
        if (map == null) {
            return;
        }
        for (boolean[] row : visibleMap) {
            Arrays.fill(row, false);
        }

        if (isInCorridor(map, x, y)) {
            revealCorridorTiles(map);
        } else {
            revealEntireRoom(map);
        }
    }

    public boolean move(int dx, int dy, char[][] map) {
        int newX = x + dx;
        int newY = y + dy;
        if (MapUtils.isWalkable(map, newX, newY)) {
            x = newX;
            y = newY;
            updateVisibility(map);
            return true;
        }
        return false;
    }

    public void addGold(int amount) {
        if (amount > 0) {
            gold += amount;
        }
    }

    public void setGold(int amount) {
        this.gold = Math.max(0, amount);
    }

    public void gainExperience(int amount) {
        if (amount <= 0) {
            return;
        }
        experience += amount;
        if (experience >= nextLevelExp) {
            levelUp();
        }
    }

    public void setExperienceProgress(int experience, int nextLevelExp) {
        this.experience = Math.max(0, experience);
        this.nextLevelExp = Math.max(1, nextLevelExp);
    }

    private void levelUp() {
        level++;
        experience = 0;
        nextLevelExp = (int) (nextLevelExp * 1.5);

        modifyMaxHealth(4);
        heal(getMaxHealth());

        modifyStrength(1);
    }

    @Override
    public void modifyStrength(int delta) {
        super.modifyStrength(delta);
        maxStrengthValue = Math.max(maxStrengthValue, getStrength());
    }

    public void restoreProgress(int level,
                                int gold,
                                int experience,
                                int nextLevelExp,
                                int maxStrengthValue) {
        this.level = Math.max(1, level);
        setGold(gold);
        setExperienceProgress(experience, nextLevelExp);
        this.maxStrengthValue = Math.max(1, maxStrengthValue);
    }

    public char[][] copySeenMap() {
        int height = getMapHeight();
        int width = getMapWidth();
        char[][] copy = new char[height][width];
        for (int i = 0; i < height; i++) {
            System.arraycopy(seenMap[i], 0, copy[i], 0, width);
        }
        return copy;
    }

    public void restoreSeenMap(char[][] data) {
        resetSeenMap();
        if (data == null) {
            return;
        }
        int height = Math.min(getMapHeight(), data.length);
        for (int y = 0; y < height; y++) {
            char[] row = data[y];
            if (row == null) {
                continue;
            }
            int length = Math.min(getMapWidth(), row.length);
            System.arraycopy(row, 0, seenMap[y], 0, length);
        }
    }

    public List<TemporaryBuff> getTemporaryBuffsCopy() {
        return new ArrayList<>(temporaryBuffs);
    }

    public void setTemporaryBuffs(List<TemporaryBuff> buffs) {
        temporaryBuffs.clear();
        if (buffs != null) {
            for (TemporaryBuff buff : buffs) {
                if (buff == null) {
                    continue;
                }
                TemporaryBuff copy = new TemporaryBuff(buff.expireLevel, buff.strengthDelta, buff.maxHealthDelta);
                temporaryBuffs.add(copy);
            }
        }
    }

    public void applyTemporaryBuff(int strengthDelta, int maxHealthDelta, int expireLevel) {
        if (strengthDelta == 0 && maxHealthDelta == 0) {
            return;
        }
        TemporaryBuff buff = new TemporaryBuff(expireLevel, strengthDelta, maxHealthDelta);
        temporaryBuffs.add(buff);
        if (strengthDelta != 0) {
            modifyStrength(strengthDelta);
        }
        if (maxHealthDelta != 0) {
            modifyMaxHealth(maxHealthDelta);
            if (maxHealthDelta > 0) {
                heal(maxHealthDelta);
            }
        }
    }

    public void removeExpiredBuffs(int currentLevel) {
        Iterator<TemporaryBuff> iterator = temporaryBuffs.iterator();
        while (iterator.hasNext()) {
            TemporaryBuff buff = iterator.next();
            if (buff.expireLevel != currentLevel) {
                iterator.remove();
                if (buff.strengthDelta != 0) {
                    modifyStrength(-buff.strengthDelta);
                }
                if (buff.maxHealthDelta != 0) {
                    modifyMaxHealth(-buff.maxHealthDelta);
                    ensureMinimumHealth();
                }
            }
        }
    }

    private boolean hasLineOfSight(char[][] map, int targetX, int targetY) {
        int x0 = x;
        int y0 = y;
        int x1 = targetX;
        int y1 = targetY;

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;

        int currentX = x0;
        int currentY = y0;

        while (true) {
            if (currentX == x1 && currentY == y1) {
                return true;
            }
            int e2 = err * 2;
            if (e2 > -dy) {
                err -= dy;
                currentX += sx;
            }
            if (e2 < dx) {
                err += dx;
                currentY += sy;
            }
            if (currentX < 0 || currentX >= getMapWidth() || currentY < 0 || currentY >= getMapHeight()) {
                return false;
            }
            if (currentX == x1 && currentY == y1) {
                return true;
            }
            char tile = map[currentY][currentX];
            if (isOpaque(tile)) {
                return false;
            }
        }
    }

    private boolean isOpaque(char tile) {
        return tile == '#' || tile == '+';
    }

    private void revealCorridorTiles(char[][] map) {
        int radiusSq = 1;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                int checkX = x + dx;
                int checkY = y + dy;
                if (checkX < 0 || checkX >= getMapWidth() || checkY < 0 || checkY >= getMapHeight()) {
                    continue;
                }
                if (dx * dx + dy * dy > radiusSq) {
                    continue;
                }
                if (!hasLineOfSight(map, checkX, checkY)) {
                    continue;
                }
                if (map[checkY][checkX] == ' ') {
                    continue;
                }
                visibleMap[checkY][checkX] = true;
                markAsSeen(checkX, checkY, map[checkY][checkX]);
            }
        }
    }

    private void revealEntireRoom(char[][] map) {
        int width = getMapWidth();
        int height = getMapHeight();
        int leftFloor = x;
        while (leftFloor > 0 && !isRoomBoundaryTile(map[y][leftFloor - 1])) {
            leftFloor--;
        }
        int rightFloor = x;
        while (rightFloor < width - 1 && !isRoomBoundaryTile(map[y][rightFloor + 1])) {
            rightFloor++;
        }
        int topFloor = y;
        while (topFloor > 0 && !isRoomBoundaryTile(map[topFloor - 1][x])) {
            topFloor--;
        }
        int bottomFloor = y;
        while (bottomFloor < height - 1 && !isRoomBoundaryTile(map[bottomFloor + 1][x])) {
            bottomFloor++;
        }

        int leftWall = Math.max(0, leftFloor - 1);
        int rightWall = Math.min(width - 1, rightFloor + 1);
        int topWall = Math.max(0, topFloor - 1);
        int bottomWall = Math.min(height - 1, bottomFloor + 1);

        for (int drawY = topWall; drawY <= bottomWall; drawY++) {
            for (int drawX = leftWall; drawX <= rightWall; drawX++) {
                if (drawX < 0 || drawX >= width || drawY < 0 || drawY >= height) {
                    continue;
                }
                if (map[drawY][drawX] == ' ') {
                    continue;
                }
                visibleMap[drawY][drawX] = true;
                markAsSeen(drawX, drawY, map[drawY][drawX]);
            }
        }
    }

    private boolean isRoomBoundaryTile(char tile) {
        return tile == '#' || tile == '+' || tile == ' ';
    }

    private boolean isInCorridor(char[][] map, int px, int py) {
        if (map == null || px < 0 || py < 0 || px >= getMapWidth() || py >= getMapHeight()) {
            return true;
        }
        return map[py][px] == MapUtils.CORRIDOR_FLOOR || map[py][px] == MapUtils.DOOR;
    }

    public static class TemporaryBuff {
        private final int expireLevel;
        private final int strengthDelta;
        private final int maxHealthDelta;

        public TemporaryBuff(int expireLevel, int strengthDelta, int maxHealthDelta) {
            this.expireLevel = expireLevel;
            this.strengthDelta = strengthDelta;
            this.maxHealthDelta = maxHealthDelta;
        }

        public int getExpireLevel() {
            return expireLevel;
        }

        public int getStrengthDelta() {
            return strengthDelta;
        }

        public int getMaxHealthDelta() {
            return maxHealthDelta;
        }
    }
}
