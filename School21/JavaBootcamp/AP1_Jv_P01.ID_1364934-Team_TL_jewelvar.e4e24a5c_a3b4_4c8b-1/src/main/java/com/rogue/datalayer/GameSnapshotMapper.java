package com.rogue.datalayer;

import com.rogue.datalayer.model.GameSaveData;
import com.rogue.datalayer.model.ItemData;
import com.rogue.datalayer.model.MonsterData;
import com.rogue.datalayer.model.PlayerData;
import com.rogue.datalayer.model.PositionData;
import com.rogue.datalayer.model.SavedLevel;
import com.rogue.datalayer.model.TemporaryBuffData;
import com.rogue.domain.entities.player.Player.TemporaryBuff;
import com.rogue.domain.item.Item;
import com.rogue.domain.item.ItemType;
import com.rogue.domain.state.GameSnapshot;
import com.rogue.domain.state.LevelSnapshot;
import com.rogue.domain.state.MonsterSnapshot;
import com.rogue.domain.state.PlayerSnapshot;
import com.rogue.domain.entities.MonsterType;
import com.rogue.domain.util.Position;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

final class GameSnapshotMapper {

    private GameSnapshotMapper() {
    }

    static GameSaveData toData(GameSnapshot snapshot) {
        if (snapshot == null) {
            return null;
        }
        SavedLevel level = toSavedLevel(snapshot.level());
        PlayerData player = toPlayerData(snapshot.player());
        return new GameSaveData(
                snapshot.currentLevel(),
                snapshot.turnCounter(),
                player,
                level,
                snapshot.messages(),
                snapshot.monstersDefeated(),
                snapshot.foodConsumed(),
                snapshot.elixirsConsumed(),
                snapshot.scrollsRead(),
                snapshot.hitsDealt(),
                snapshot.hitsTaken(),
                snapshot.tilesTraversed());
    }

    static GameSnapshot fromData(GameSaveData data) {
        if (data == null) {
            return null;
        }
        LevelSnapshot level = fromSavedLevel(data.level());
        if (level == null) {
            return null;
        }
        PlayerSnapshot player = fromPlayerData(data.player());
        return new GameSnapshot(
                data.currentLevel(),
                data.turnCounter(),
                player,
                level,
                data.messages(),
                data.monstersDefeated(),
                data.foodConsumed(),
                data.elixirsConsumed(),
                data.scrollsRead(),
                data.hitsDealt(),
                data.hitsTaken(),
                data.tilesTraversed());
    }

    private static PlayerData toPlayerData(PlayerSnapshot snapshot) {
        if (snapshot == null) {
            return null;
        }
        Map<ItemType, List<ItemData>> inventory = new EnumMap<>(ItemType.class);
        snapshot.inventory().forEach((type, items) -> {
            List<ItemData> serialized = new ArrayList<>();
            for (Item item : items) {
                ItemData data = toItemData(item);
                if (data != null) {
                    serialized.add(data);
                }
            }
            inventory.put(type, serialized);
        });
        ItemData weapon = toItemData(snapshot.equippedWeapon());
        List<TemporaryBuffData> buffs = new ArrayList<>();
        for (TemporaryBuff buff : snapshot.buffs()) {
            buffs.add(new TemporaryBuffData(
                    buff.getExpireLevel(),
                    buff.getStrengthDelta(),
                    buff.getMaxHealthDelta()));
        }
        return new PlayerData(
                snapshot.x(),
                snapshot.y(),
                snapshot.health(),
                snapshot.maxHealth(),
                snapshot.strength(),
                snapshot.level(),
                snapshot.gold(),
                snapshot.experience(),
                snapshot.nextLevelExp(),
                snapshot.maxStrength(),
                snapshot.mapWidth(),
                snapshot.mapHeight(),
                null,
                inventory,
                weapon,
                buffs);
    }

    private static PlayerSnapshot fromPlayerData(PlayerData data) {
        if (data == null) {
            throw new IllegalArgumentException("Player data missing in save");
        }
        Map<ItemType, List<Item>> inventory = new EnumMap<>(ItemType.class);
        if (data.inventory() != null) {
            data.inventory().forEach((type, items) -> {
                List<Item> restored = new ArrayList<>();
                if (items != null) {
                    for (ItemData itemData : items) {
                        Item item = fromItemData(itemData);
                        if (item != null) {
                            restored.add(item);
                        }
                    }
                }
                inventory.put(type, restored);
            });
        }
        Item weapon = fromItemData(data.equippedWeapon());
        List<TemporaryBuff> buffs = new ArrayList<>();
        if (data.buffs() != null) {
            for (TemporaryBuffData buffData : data.buffs()) {
                buffs.add(new TemporaryBuff(
                        buffData.expireLevel(),
                        buffData.strengthDelta(),
                        buffData.maxHealthDelta()));
            }
        }
        return new PlayerSnapshot(
                data.x(),
                data.y(),
                data.health(),
                data.maxHealth(),
                data.strength(),
                data.level(),
                data.gold(),
                data.experience(),
                data.nextLevelExp(),
                data.maxStrength(),
                data.mapWidth(),
                data.mapHeight(),
                inventory,
                weapon,
                buffs);
    }

    private static SavedLevel toSavedLevel(LevelSnapshot snapshot) {
        if (snapshot == null) {
            return null;
        }
        List<String> mapRows = mapToRows(snapshot.map());
        List<String> seenRows = mapToRows(snapshot.seenMap());
        List<MonsterData> monsters = new ArrayList<>();
        for (MonsterSnapshot monster : snapshot.monsters()) {
            Map<String, Integer> extras = monster.extras();
            monsters.add(new MonsterData(
                    monster.type().name(),
                    monster.x(),
                    monster.y(),
                    monster.health(),
                    extras));
        }
        PositionData position = snapshot.playerPosition() == null
                ? null
                : new PositionData(snapshot.playerPosition().x(), snapshot.playerPosition().y());
        return new SavedLevel(mapRows, monsters, position, seenRows);
    }

    private static LevelSnapshot fromSavedLevel(SavedLevel saved) {
        if (saved == null) {
            return null;
        }
        char[][] map = rowsToMap(saved.mapRows());
        char[][] seen = rowsToMap(saved.seenMap());
        List<MonsterSnapshot> monsters = new ArrayList<>();
        if (saved.monsters() != null) {
            for (MonsterData data : saved.monsters()) {
                if (data == null) {
                    continue;
                }
                try {
                    MonsterType type = MonsterType.valueOf(data.type());
                    monsters.add(new MonsterSnapshot(
                            type,
                            data.x(),
                            data.y(),
                            data.health(),
                            data.extras()));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        Position position = null;
        if (saved.playerPosition() != null) {
            position = new Position(saved.playerPosition().x(), saved.playerPosition().y());
        }
        return new LevelSnapshot(map, monsters, position, seen);
    }

    private static ItemData toItemData(Item item) {
        if (item == null) {
            return null;
        }
        return new ItemData(
                item.getType(),
                item.getSubtype(),
                item.getHealthDelta(),
                item.getMaxHealthDelta(),
                item.getStrengthDelta(),
                item.getValue());
    }

    private static Item fromItemData(ItemData data) {
        if (data == null) {
            return null;
        }
        Item.Builder builder = Item.builder(data.type(), data.subtype());
        if (data.healthDelta() != 0) {
            builder.healthDelta(data.healthDelta());
        }
        if (data.maxHealthDelta() != 0) {
            builder.maxHealthDelta(data.maxHealthDelta());
        }
        if (data.strengthDelta() != 0) {
            builder.strengthDelta(data.strengthDelta());
        }
        if (data.value() != 0) {
            builder.value(data.value());
        }
        return builder.build();
    }

    private static List<String> mapToRows(char[][] map) {
        if (map == null) {
            return List.of();
        }
        List<String> rows = new ArrayList<>(map.length);
        for (char[] row : map) {
            rows.add(row == null ? "" : new String(row));
        }
        return rows;
    }

    private static char[][] rowsToMap(List<String> rows) {
        if (rows == null || rows.isEmpty()) {
            return null;
        }
        int height = rows.size();
        int width = 0;
        for (String row : rows) {
            if (row != null) {
                width = Math.max(width, row.length());
            }
        }
        char[][] map = new char[height][width];
        for (int y = 0; y < height; y++) {
            String row = rows.get(y);
            if (row == null) {
                for (int x = 0; x < width; x++) {
                    map[y][x] = ' ';
                }
                continue;
            }
            char[] chars = row.toCharArray();
            int limit = Math.min(chars.length, width);
            System.arraycopy(chars, 0, map[y], 0, limit);
            for (int x = limit; x < width; x++) {
                map[y][x] = ' ';
            }
        }
        return map;
    }
}
