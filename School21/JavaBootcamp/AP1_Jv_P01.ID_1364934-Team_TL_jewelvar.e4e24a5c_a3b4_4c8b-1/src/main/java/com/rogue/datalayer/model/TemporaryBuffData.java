package com.rogue.datalayer.model;

public record TemporaryBuffData(
        int expireLevel,
        int strengthDelta,
        int maxHealthDelta) {
}
