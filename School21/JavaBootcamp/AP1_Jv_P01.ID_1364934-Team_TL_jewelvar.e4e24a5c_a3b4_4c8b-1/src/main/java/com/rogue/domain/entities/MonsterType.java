package com.rogue.domain.entities;

public enum MonsterType {
    ZOMBIE('z', MonsterColor.GREEN, 40, 6, 5),
    VAMPIRE('v', MonsterColor.RED, 36, 8, 9),
    GHOST('g', MonsterColor.WHITE, 16, 3, 3),
    OGRE('O', MonsterColor.YELLOW, 60, 15, 6),
    SNAKE_MAGE('s', MonsterColor.BRIGHT_WHITE, 24, 9, 9),
    MIMIC('m', MonsterColor.BRIGHT_WHITE, 48, 5, 3);

    private final char symbol;
    private final MonsterColor color;
    private final int health;
    private final int strength;
    private final int hostility;

    MonsterType(char symbol, MonsterColor color, int health, int strength, int hostility) {
        this.symbol = symbol;
        this.color = color;
        this.health = health;
        this.strength = strength;
        this.hostility = hostility;
    }

    public char getSymbol() {
        return symbol;
    }

    public MonsterColor getColor() {
        return color;
    }

    public int getHealth() {
        return health;
    }

    public int getStrength() {
        return strength;
    }

    public int getHostility() {
        return hostility;
    }
}
