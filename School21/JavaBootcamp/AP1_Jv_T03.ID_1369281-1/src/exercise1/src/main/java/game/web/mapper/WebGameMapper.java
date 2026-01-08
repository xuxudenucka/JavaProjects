package game.web.mapper;

import game.domain.model.Game;
import game.web.model.GameWeb;

public class WebGameMapper {
    private final WebGameFieldMapper fieldMapper = new WebGameFieldMapper();

    public GameWeb toWeb(Game domain) {
        if (domain == null) return null;
        return new GameWeb(domain.getId(), fieldMapper.toWeb(domain.getField()));
    }

    public Game toDomain(GameWeb web) {
        return new Game(web.getId(), fieldMapper.toDomain(web.getField()));
    }
}
