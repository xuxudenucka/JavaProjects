package com.rogue.domain.entities.monsters;

import com.rogue.domain.entities.MonsterType;

public final class MonsterFactory {
    private MonsterFactory() {
    }

    public static Monster create(MonsterType type, int x, int y) {
        return switch (type) {
            case ZOMBIE -> new Zombie(x, y);
            case VAMPIRE -> new Vampire(x, y);
            case GHOST -> new Ghost(x, y);
            case OGRE -> new Ogre(x, y);
            case SNAKE_MAGE -> new SnakeMage(x, y);
            case MIMIC -> new Mimic(x, y);
        };
    }
}
