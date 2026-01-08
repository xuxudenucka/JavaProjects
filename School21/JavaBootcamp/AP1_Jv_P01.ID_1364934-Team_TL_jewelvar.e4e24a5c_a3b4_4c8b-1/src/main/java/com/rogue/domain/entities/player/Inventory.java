package com.rogue.domain.entities.player;

import com.rogue.domain.item.Item;
import com.rogue.domain.item.ItemSubtype;
import com.rogue.domain.item.ItemType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class Inventory {
    private final Map<ItemType, List<Item>> itemsByType = new EnumMap<>(ItemType.class);
    private final int perTypeCapacity;

    public Inventory(int perTypeCapacity) {
        this.perTypeCapacity = perTypeCapacity;
        for (ItemType type : ItemType.values()) {
            itemsByType.put(type, new ArrayList<>());
        }
    }

    public boolean addItem(Item item) {
        if (item == null) {
            return false;
        }
        List<Item> items = itemsByType.get(item.getType());
        if (item.getType() == ItemType.TREASURE) {
            items.add(item);
            return true;
        }
        if (items.size() >= perTypeCapacity) {
            return false;
        }
        return items.add(item);
    }

    public int size(ItemType type) {
        return itemsByType.get(type).size();
    }

    public boolean isEmpty(ItemType type) {
        return itemsByType.get(type).isEmpty();
    }

    public List<Item> viewItems(ItemType type) {
        return itemsByType.get(type);
    }

    public Item removeItem(ItemType type, int index) {
        List<Item> items = itemsByType.get(type);
        if (index < 0 || index >= items.size()) {
            return null;
        }
        return items.remove(index);
    }

    public boolean hasItem(ItemType type, ItemSubtype subtype) {
        List<Item> items = itemsByType.get(type);
        if (items.isEmpty()) {
            return false;
        }
        if (subtype == null) {
            return true;
        }
        for (Item item : items) {
            if (item.getSubtype() == subtype) {
                return true;
            }
        }
        return false;
    }
}
