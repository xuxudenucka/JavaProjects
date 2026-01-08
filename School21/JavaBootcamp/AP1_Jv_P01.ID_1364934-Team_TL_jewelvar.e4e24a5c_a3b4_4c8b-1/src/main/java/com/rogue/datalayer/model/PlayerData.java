package com.rogue.datalayer.model;

import com.rogue.domain.item.ItemType;

import java.util.List;
import java.util.Map;

public record PlayerData(
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
        List<String> seenMap,
        Map<ItemType, List<ItemData>> inventory,
        ItemData equippedWeapon,
        List<TemporaryBuffData> buffs) {
}
