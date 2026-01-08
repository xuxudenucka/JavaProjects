package com.rogue.domain.state;

import com.rogue.domain.entities.player.Player.TemporaryBuff;
import com.rogue.domain.item.Item;
import com.rogue.domain.item.ItemType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public record PlayerSnapshot(
        int x,
        int y,
        int health,
        int maxHealth,
        int strength,
        int level,
        int gold,
        int experience,
        int nextLevelExp,
        int maxStrength,
        int mapWidth,
        int mapHeight,
        Map<ItemType, List<Item>> inventory,
        Item equippedWeapon,
        List<TemporaryBuff> buffs) {

    public PlayerSnapshot {
        inventory = inventory == null ? Map.of() : copyInventory(inventory);
        buffs = buffs == null ? List.of() : List.copyOf(buffs);
    }

    private static Map<ItemType, List<Item>> copyInventory(Map<ItemType, List<Item>> source) {
        EnumMap<ItemType, List<Item>> copy = new EnumMap<>(ItemType.class);
        for (Map.Entry<ItemType, List<Item>> entry : source.entrySet()) {
            List<Item> items = entry.getValue();
            if (items == null || items.isEmpty()) {
                continue;
            }
            copy.put(entry.getKey(), List.copyOf(new ArrayList<>(items)));
        }
        return Map.copyOf(copy);
    }
}
