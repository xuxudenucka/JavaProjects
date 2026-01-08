package com.rogue.datalayer.model;

import com.rogue.domain.item.ItemSubtype;
import com.rogue.domain.item.ItemType;

public record ItemData(
        ItemType type,
        ItemSubtype subtype,
        int healthDelta,
        int maxHealthDelta,
        int strengthDelta,
        int value) {
}
