package com.rogue.domain.entities.monsters;

import com.rogue.domain.entities.MonsterType;
import com.rogue.domain.util.Position;

import java.util.List;

public class Zombie extends Monster {
    public Zombie(int x, int y) {
        super(MonsterType.ZOMBIE, x, y);
    }

    @Override
    protected Position choosePatternStep(char[][] map, List<Monster> monsters) {
        return randomAdjacentStep(map, monsters, false);
    }
}
