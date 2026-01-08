package com.rogue.domain.input;

public record InputCommand(InputType type, Character character) {
    public InputCommand(InputType type) {
        this(type, null);
    }
}
