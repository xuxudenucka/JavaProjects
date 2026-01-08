package com.rogue.domain.item;

public class Item {
    private final ItemType type;
    private final ItemSubtype subtype;
    private final int healthDelta;
    private final int maxHealthDelta;
    private final int strengthDelta;
    private final int value;

    private Item(Builder builder) {
        this.type = builder.type;
        this.subtype = builder.subtype;
        this.healthDelta = builder.healthDelta;
        this.maxHealthDelta = builder.maxHealthDelta;
        this.strengthDelta = builder.strengthDelta;
        this.value = builder.value;
    }

    public ItemType getType() {
        return type;
    }

    public ItemSubtype getSubtype() {
        return subtype;
    }

    public int getHealthDelta() {
        return healthDelta;
    }

    public int getMaxHealthDelta() {
        return maxHealthDelta;
    }

    public int getStrengthDelta() {
        return strengthDelta;
    }

    public int getValue() {
        return value;
    }

    public static Builder builder(ItemType type, ItemSubtype subtype) {
        return new Builder(type, subtype);
    }

    public static class Builder {
        private final ItemType type;
        private final ItemSubtype subtype;
        private int healthDelta;
        private int maxHealthDelta;
        private int strengthDelta;
        private int value;

        private Builder(ItemType type, ItemSubtype subtype) {
            this.type = type;
            this.subtype = subtype;
        }

        public Builder healthDelta(int healthDelta) {
            this.healthDelta = healthDelta;
            return this;
        }

        public Builder maxHealthDelta(int maxHealthDelta) {
            this.maxHealthDelta = maxHealthDelta;
            return this;
        }

        public Builder strengthDelta(int strengthDelta) {
            this.strengthDelta = strengthDelta;
            return this;
        }

        public Builder value(int value) {
            this.value = value;
            return this;
        }

        public Item build() {
            return new Item(this);
        }
    }
}
