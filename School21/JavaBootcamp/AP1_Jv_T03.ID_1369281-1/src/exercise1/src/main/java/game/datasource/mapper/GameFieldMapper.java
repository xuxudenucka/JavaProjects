package game.datasource.mapper;

import game.datasource.model.GameFieldEntity;
import game.domain.model.GameField;

public class GameFieldMapper {
    public GameFieldEntity toEntity(GameField domain) {
        if (domain == null) return null;
        return new GameFieldEntity(domain.field());
    }

    public GameField toDomain(GameFieldEntity entity) {
        if (entity == null) return null;
        return new GameField(entity.field());
    }
}
