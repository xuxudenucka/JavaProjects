package game.domain.repository;

import game.domain.model.Game;

import java.util.UUID;

public interface GameRepository {
    void save(Game game);
    Game findById(UUID id);
}
