package com.rogue.domain.entities.monsters;

import com.rogue.domain.entities.MonsterType;
import com.rogue.domain.util.Position;

import java.util.List;

public class Ghost extends Monster {
    private int phaseCounter = 0;
    private boolean revealed;

    public Ghost(int x, int y) {
        super(MonsterType.GHOST, x, y);
    }

    public int getPhaseCounter() {
        return phaseCounter;
    }

    public void setPhaseCounter(int phaseCounter) {
        this.phaseCounter = phaseCounter;
    }

    public void setRevealed(boolean revealed) {
        this.revealed = revealed;
    }

    public void reveal() {
        this.revealed = true;
    }

    public boolean isRevealed() {
        return revealed;
    }

    @Override
    public boolean isVisible() {
        return revealed;
    }

    @Override
    protected Position choosePatternStep(char[][] map, List<Monster> monsters) {
        phaseCounter++;
        if (phaseCounter % 3 == 0) {
            Position teleport = randomWalkablePosition(map, monsters, 5);
            if (teleport != null) {
                return teleport;
            }
        }
        return randomAdjacentStep(map, monsters, true);
    }
}
