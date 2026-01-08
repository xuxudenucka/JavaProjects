package com.rogue.domain.entities.monsters;

import com.rogue.domain.entities.MonsterType;
import com.rogue.domain.util.Position;

import java.util.List;

public class Vampire extends Monster {
    private boolean initialDodgeUsed;

    public Vampire(int x, int y) {
        super(MonsterType.VAMPIRE, x, y);
    }

    public boolean hasDodgedInitialStrike() {
        return initialDodgeUsed;
    }

    public void markInitialDodge() {
        this.initialDodgeUsed = true;
    }

    public void setInitialDodgeUsed(boolean used) {
        this.initialDodgeUsed = used;
    }

    @Override
    protected Position choosePatternStep(char[][] map, List<Monster> monsters) {
        return randomAdjacentStep(map, monsters, true);
    }
}
