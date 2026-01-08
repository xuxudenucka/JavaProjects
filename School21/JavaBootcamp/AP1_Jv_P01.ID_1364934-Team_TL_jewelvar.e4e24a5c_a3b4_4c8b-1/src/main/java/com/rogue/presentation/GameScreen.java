package com.rogue.presentation;

import com.rogue.domain.ScoreBoard;
import com.rogue.domain.entities.player.Player;
import com.rogue.domain.entities.monsters.Monster;

import java.io.IOException;
import java.util.List;

public interface GameScreen {
    void renderGame(char[][] map,
                    Player player,
                    int currentLevel,
                    List<Monster> monsters,
                    List<String> messages) throws IOException;

    void renderStats(List<ScoreBoard.ScoreRecord> records) throws IOException;

    void renderDeathScreen(String message, ScoreBoard.ScoreRecord record) throws IOException;

    void refresh() throws IOException;

    void clear() throws IOException;
}
