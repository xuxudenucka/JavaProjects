package game.datasource.model;

import java.util.UUID;

public record GameEntity(UUID id, GameFieldEntity field) {
}
