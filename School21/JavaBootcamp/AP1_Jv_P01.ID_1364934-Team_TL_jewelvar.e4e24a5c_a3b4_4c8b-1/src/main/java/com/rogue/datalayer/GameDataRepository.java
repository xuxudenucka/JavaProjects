package com.rogue.datalayer;

import com.rogue.domain.ScoreBoard;
import com.rogue.domain.state.GameSnapshot;

import java.util.List;
import java.util.Optional;

public interface GameDataRepository {
    Optional<GameSnapshot> loadGame();

    void saveGame(GameSnapshot snapshot);

    void clearGame();

    Optional<List<ScoreBoard.ScoreRecord>> loadScoreBoard();

    void saveScoreBoard(List<ScoreBoard.ScoreRecord> records);
}
