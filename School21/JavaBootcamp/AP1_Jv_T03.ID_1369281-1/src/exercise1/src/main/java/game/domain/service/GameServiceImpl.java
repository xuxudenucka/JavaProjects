package game.domain.service;

import game.domain.model.Game;
import game.domain.model.GameField;
import game.domain.repository.GameRepository;

import java.util.Arrays;
import java.util.UUID;

public class GameServiceImpl implements GameService {
    private final GameRepository repository;


    public GameServiceImpl(GameRepository repository) {
        this.repository = repository;
    }


    private final int empty = 0;
    private final int playerX = 1;
    private final int playerO = 2;

    @Override
    public Game createNewGame() {
        UUID id = UUID.randomUUID();
        int[][] emptyGrid = new int[3][3];
        Game newGame = new Game(id, new GameField(emptyGrid));

        repository.save(newGame);
        return newGame;
    }

    @Override
    public Game processMove(Game userGame) {
        Game previousGame = repository.findById(userGame.getId());

        if (previousGame != null) {
            if (!validateMoveConsistency(previousGame.getField(), userGame.getField())) {
                throw new IllegalArgumentException("Вы изменили прошлые ходы!");
            }
        }

        if (!validateGameField(userGame.getField())) {
            throw new IllegalArgumentException("Некорректное состояние поля");
        }

        GameField fieldAfterCpu = getNextMove(userGame.getField());

        userGame.setField(fieldAfterCpu);

        repository.save(userGame);

        return userGame;
    }

    @Override
    public boolean validateGameField(GameField game) {
        if (game == null || game.field() == null) return false;
        int[][] board = game.field();
        if (board.length != 3 || board[0].length != 3) return false;

        int xCount = 0;
        int oCount = 0;
        for (int[] row : board) {
            for (int cell : row) {
                if (cell == playerX) xCount++;
                else if (cell == playerO) oCount++;
                else if (cell != empty) return false;
            }
        }
        return (xCount == oCount || xCount == oCount + 1);
    }

    @Override
    public boolean isGameFinished(GameField game) {
        int[][] field = game.field();
        return checkWin(field, playerX) || checkWin(field, playerO) || notMovesLeft(field);
    }


    private GameField getNextMove(GameField game) {
        if (game == null || game.field() == null) {
            throw new IllegalArgumentException("Поле не может быть null");
        }
        if (isGameFinished(game)) {
            return game;
        }
        int[][] field = CopyField(game.field());
        int bestScore = Integer.MIN_VALUE;
        int[] bestTurn = {-1, -1};
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (field[i][j] == empty) {
                    field[i][j] = playerO;
                    int score = minimax(field, 0, false);
                    field[i][j] = empty;
                    if (bestScore < score) {
                        bestScore = score;
                        bestTurn[0] = i;
                        bestTurn[1] = j;
                    }
                }

            }
        }
        if (bestTurn[0] != -1) {
            field[bestTurn[0]][bestTurn[1]] = playerO;
        }
        return new GameField(field);
    }

    private int minimax(int[][] field, int depth, boolean isMaximize) {
        int score = evaluate(field);
        if (score == 10) return score - depth;
        if (score == -10) return score + depth;
        if (notMovesLeft(field)) return 0;

        if (isMaximize) {
            int best = Integer.MIN_VALUE;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (field[i][j] == empty) {
                        field[i][j] = playerO;
                        best = Math.max(best, minimax(field, depth + 1, false));
                        field[i][j] = empty;
                    }
                }
            }
            return best;
        } else {
            int best = Integer.MAX_VALUE;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (field[i][j] == empty) {
                        field[i][j] = playerX;
                        best = Math.min(best, minimax(field, depth + 1, true));
                        field[i][j] = empty;
                    }
                }
            }
            return best;
        }
    }

    private int evaluate(int[][] field){
        if (checkWin(field, playerX)) return -10;
        if (checkWin(field, playerO)) return 10;
        return 0;
    }

    private boolean checkWin(int[][] field, int player) {
        for (int i = 0; i < 3; i++) {
            if (field[i][0] == player && field[i][1] == player && field[i][2] == player) return true;
            if (field[0][i] == player && field[1][i] == player && field[2][i] == player) return true;
        }
        if (field[0][0] == player && field[1][1] == player && field[2][2] == player) return true;
        if (field[0][2] == player && field[1][1] == player && field[2][0] == player) return true;
        return false;
    }

    private boolean notMovesLeft(int[][] field) {
        for (int[] row : field) {
            for (int cell : row) if (cell == empty) return false;
        }
        return true;
    }

    private boolean validateMoveConsistency(GameField oldField, GameField newField) {
        int[][] oldGrid = oldField.field();
        int[][] newGrid = newField.field();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (oldGrid[i][j] != empty && oldGrid[i][j] != newGrid[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    private int[][] CopyField(int[][] field) {
        int[][] copy = new int[3][3];
        for (int i = 0; i < 3; i++) {
            copy[i] = Arrays.copyOf(field[i], 3);
        }
        return copy;
    }
}
