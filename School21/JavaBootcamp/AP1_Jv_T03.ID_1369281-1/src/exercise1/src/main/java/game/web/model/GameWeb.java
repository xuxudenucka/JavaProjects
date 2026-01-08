package game.web.model;

import java.util.UUID;

public class GameWeb {
    private UUID id;
    private GameFieldWeb field;

    public GameWeb() {}

    public GameWeb(UUID id, GameFieldWeb field) {
        this.id = id;
        this.field = field;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public GameFieldWeb getField() { return field; }
}