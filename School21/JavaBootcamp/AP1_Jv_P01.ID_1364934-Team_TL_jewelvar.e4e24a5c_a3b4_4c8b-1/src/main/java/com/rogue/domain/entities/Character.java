package com.rogue.domain.entities;

import com.rogue.domain.item.Item;
import com.rogue.domain.item.ItemType;

public class Character {
    private int maxHealth;
    private int health;
    private int strength;
    private Item currentWeapon;

    public Character(int maxHealth, int strength) {
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.strength = strength;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getHealth() {
        return health;
    }

    public int getStrength() {
        return strength;
    }

    public Item getCurrentWeapon() {
        return currentWeapon;
    }

    public void takeDamage(int damage) {
        health = Math.max(0, health - damage);
    }

    public void heal(int amount) {
        health = Math.min(maxHealth, health + amount);
    }

    public boolean isAlive() {
        return health > 0;
    }

    public void modifyMaxHealth(int delta) {
        maxHealth = Math.max(1, maxHealth + delta);
        health = Math.min(maxHealth, health);
    }

    public void modifyStrength(int delta) {
        strength = Math.max(1, strength + delta);
    }

    public void equipWeapon(Item weapon) {
        if (weapon != null && weapon.getType() != ItemType.WEAPON) {
            throw new IllegalArgumentException("Weapon item must have type WEAPON");
        }
        this.currentWeapon = weapon;
    }

    public void ensureMinimumHealth() {
        if (health <= 0) {
            health = 1;
        }
    }

    public void setMaxHealthAbsolute(int newMax) {
        maxHealth = Math.max(1, newMax);
        if (health > maxHealth) {
            health = maxHealth;
        }
    }

    public void setHealthAbsolute(int value) {
        health = Math.max(0, Math.min(maxHealth, value));
    }

    public void setStrengthAbsolute(int value) {
        strength = Math.max(1, value);
    }
}
