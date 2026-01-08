package game.web.mapper;

import game.domain.model.GameField;
import game.web.model.GameFieldWeb;

public class WebGameFieldMapper {
    public GameFieldWeb toWeb(GameField domain) {
        if (domain == null) return null;
        return new GameFieldWeb(domain.field());
    }

    public GameField toDomain(GameFieldWeb web) {
        if (web == null) return null;
        return new GameField(web.getField());
    }
}
