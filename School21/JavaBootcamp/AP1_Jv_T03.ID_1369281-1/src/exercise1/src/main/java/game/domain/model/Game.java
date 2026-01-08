package game.domain.model;

import java.util.UUID;

public class Game {
    private GameField field;
    private final UUID id;

    public Game(UUID id, GameField field) {
        this.id = id;
        this.field = field;
    }

    public UUID getId() {
        return id;
    }

    public void setField(GameField field) {
        this.field = field;
    }

    public GameField getField() {
        return field;
    }
}
