package game.web.model;

public class GameFieldWeb {
    private int[][] field;

    public GameFieldWeb() {
    }

    public GameFieldWeb(int[][] field) {
        this.field = field;
    }

    public int[][] getField() {
        return field;
    }
}