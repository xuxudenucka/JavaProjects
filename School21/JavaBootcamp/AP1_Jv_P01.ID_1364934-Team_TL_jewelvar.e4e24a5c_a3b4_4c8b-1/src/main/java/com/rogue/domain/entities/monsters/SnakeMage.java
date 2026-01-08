package com.rogue.domain.entities.monsters;

import com.rogue.domain.entities.MonsterType;
import com.rogue.domain.util.Position;

import java.util.List;

public class SnakeMage extends Monster {
    private int dx = RANDOM.nextBoolean() ? 1 : -1;
    private int dy = RANDOM.nextBoolean() ? 1 : -1;

    public SnakeMage(int x, int y) {
        super(MonsterType.SNAKE_MAGE, x, y);
    }

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }

    public void setDx(int dx) {
        this.dx = dx == 0 ? 1 : (dx > 0 ? 1 : -1);
    }

    public void setDy(int dy) {
        this.dy = dy == 0 ? 1 : (dy > 0 ? 1 : -1);
    }

    @Override
    protected Position choosePatternStep(char[][] map, List<Monster> monsters) {
        int targetX = getX() + dx;
        int targetY = getY() + dy;
        if (!isStepFree(targetX, targetY, map, monsters)) {
            dx = -dx;
            targetX = getX() + dx;
            if (!isStepFree(targetX, targetY, map, monsters)) {
                dy = -dy;
                targetY = getY() + dy;
            }
        }
        if (!isStepFree(targetX, targetY, map, monsters)) {
            return null;
        }
        dx = -dx;
        dy = -dy;
        return new Position(targetX, targetY);
    }
}
