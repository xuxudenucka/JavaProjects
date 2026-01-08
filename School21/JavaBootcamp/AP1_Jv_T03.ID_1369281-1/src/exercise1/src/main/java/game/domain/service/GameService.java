package game.domain.service;

import game.domain.model.Game;
import game.domain.model.GameField;

public interface GameService {
    Game createNewGame();

    Game processMove(Game userGame);

    boolean validateGameField(GameField game);

    boolean isGameFinished(GameField game);
}
