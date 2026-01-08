package game.datasource.repository;

import game.datasource.mapper.GameMapper;
import game.datasource.model.GameEntity;
import game.domain.model.Game;
import game.domain.repository.GameRepository;

import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

public class GameRepositoryImpl implements GameRepository {
    private final ConcurrentMap<UUID, GameEntity> games;
    private final GameMapper gameMapper = new GameMapper();

    public GameRepositoryImpl(ConcurrentMap<UUID, GameEntity> games) {
        this.games = games;
    }

    @Override
    public void save(Game game) {
        if (game == null || game.getId() == null) return;
        GameEntity entity = gameMapper.toEntity(game);
        games.put(game.getId(), entity);
    }

    @Override
    public Game findById(UUID id) {
        GameEntity entity = games.get(id);
        if (entity == null) return null;
        return gameMapper.toDomain(entity);
    }
}