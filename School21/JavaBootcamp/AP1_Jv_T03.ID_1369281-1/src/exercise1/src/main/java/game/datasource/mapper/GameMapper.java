package game.datasource.mapper;

import game.datasource.model.GameEntity;
import game.domain.model.Game;

public class GameMapper {
    private final GameFieldMapper fieldMapper = new GameFieldMapper();

    public GameEntity toEntity(Game domain) {
        if (domain == null) return null;
        return new GameEntity(domain.getId(), fieldMapper.toEntity(domain.getField()));
    }

    public Game toDomain(GameEntity entity) {
        if (entity == null) return null;
        return new Game(entity.id(), fieldMapper.toDomain(entity.field()));
    }
}
