package com.rogue.presentation;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.rogue.domain.ScoreBoard;
import com.rogue.domain.entities.MonsterColor;
import com.rogue.domain.entities.monsters.Monster;
import com.rogue.domain.entities.player.Player;
import com.rogue.domain.input.InputCommand;
import com.rogue.domain.input.InputProvider;
import com.rogue.domain.input.InputType;

import java.io.IOException;
import java.util.List;

public class LanternaGameScreen implements GameScreen, InputProvider {
    private static final TextColor COLOR_HUD = TextColor.ANSI.WHITE;
    private static final TextColor COLOR_HUD_VALUE = TextColor.ANSI.WHITE;
    private static final TextColor COLOR_HUD_IMPORTANT = TextColor.ANSI.WHITE;
    private static final TextColor COLOR_MESSAGE = TextColor.ANSI.WHITE;

    private static final int HUD_HEIGHT = 1;
    private static final int MAP_HEIGHT_OFFSET = HUD_HEIGHT;
    private static final int MESSAGE_AREA_HEIGHT = 4;

    private final Screen screen;
    private final TextGraphics textGraphics;
    private final int screenWidth;
    private final int screenHeight;

    public LanternaGameScreen(Screen screen) {
        this.screen = screen;
        this.textGraphics = screen.newTextGraphics();
        this.screenWidth = screen.getTerminalSize().getColumns();
        this.screenHeight = screen.getTerminalSize().getRows();
    }

    @Override
    public void renderGame(char[][] map, Player player, int currentLevel, List<Monster> monsters, List<String> messages) throws IOException {
        clear();
        screen.setCursorPosition(null);

        renderMap(map, player);
        renderMonsters(monsters, player);
        renderHUD(player, currentLevel);
        renderMessages(messages);
        renderPlayer(player);
    }

    @Override
    public void clear() throws IOException {
        screen.clear();
    }

    @Override
    public void renderStats(List<ScoreBoard.ScoreRecord> records) throws IOException {
        clear();
        screen.setCursorPosition(null);
        textGraphics.setBackgroundColor(TextColor.ANSI.BLACK);
        textGraphics.setForegroundColor(TextColor.ANSI.WHITE_BRIGHT);
        String title = "Expedition Log (Top 10 by Treasure)";
        int titleX = Math.max(0, (screenWidth - title.length()) / 2);
        textGraphics.putString(titleX, 0, title);

        int row = 2;
        String header = String.format(
                "%-4s %-7s %-6s %-7s %-5s %-5s %-7s %-7s %-7s %-8s",
                "#", "Gold", "Depth", "Monst", "Food", "Elix", "Scroll", "Hit+", "Hit-", "Steps");
        putPaddedLine(row++, header, TextColor.ANSI.WHITE_BRIGHT);

        if (records == null || records.isEmpty()) {
            putPaddedLine(row, "No expeditions recorded yet.", TextColor.ANSI.WHITE);
        } else {
            int rank = 1;
            for (ScoreBoard.ScoreRecord record : records) {
                if (row >= screenHeight - 2) {
                    break;
                }
                String rankLabel = String.format("%2d.", rank);
                String line = String.format(
                        "%-4s %-7d %-6d %-7d %-5d %-5d %-7d %-7d %-7d %-8d",
                        rankLabel,
                        record.treasureCollected(),
                        record.depthReached(),
                        record.monstersDefeated(),
                        record.foodEaten(),
                        record.elixirsDrunk(),
                        record.scrollsRead(),
                        record.hitsDealt(),
                        record.hitsTaken(),
                        record.tilesTraversed());
                putPaddedLine(row++, line, TextColor.ANSI.WHITE);
                rank++;
            }
        }

        putPaddedLine(screenHeight - 1, "Press TAB to return to the dungeon", TextColor.ANSI.WHITE);
    }

    @Override
    public void renderDeathScreen(String message, ScoreBoard.ScoreRecord record) throws IOException {
        clear();
        screen.setCursorPosition(null);
        textGraphics.setBackgroundColor(TextColor.ANSI.BLACK);
        putCenteredLine(2, "You have fallen!", TextColor.ANSI.RED_BRIGHT);
        int row = 6;
        putCenteredLine(row++, String.format("Depth reached: %d", record.depthReached()), TextColor.ANSI.WHITE);
        putCenteredLine(row++, String.format("Treasure collected: %d", record.treasureCollected()), TextColor.ANSI.WHITE);
        putCenteredLine(row++, String.format("Monsters defeated: %d", record.monstersDefeated()), TextColor.ANSI.WHITE);
        putCenteredLine(row++, String.format("Food eaten: %d", record.foodEaten()), TextColor.ANSI.WHITE);
        putCenteredLine(row++, String.format("Elixirs drunk: %d", record.elixirsDrunk()), TextColor.ANSI.WHITE);
        putCenteredLine(row++, String.format("Scrolls read: %d", record.scrollsRead()), TextColor.ANSI.WHITE);
        putCenteredLine(row, String.format("Tiles traversed: %d", record.tilesTraversed()), TextColor.ANSI.WHITE);
        putCenteredLine(screenHeight - 2, "Press Enter to begin a new expedition", TextColor.ANSI.WHITE);
    }

    @Override
    public void refresh() throws IOException {
        screen.refresh();
    }

    @Override
    public InputCommand readInput() throws IOException {
        KeyStroke key = screen.readInput();
        KeyType type = key.getKeyType();
        return switch (type) {
            case Tab -> new InputCommand(InputType.TAB);
            case Character -> new InputCommand(InputType.CHARACTER, key.getCharacter());
            case Escape -> new InputCommand(InputType.ESCAPE);
            case Enter -> new InputCommand(InputType.ENTER);
            default -> new InputCommand(InputType.UNKNOWN);
        };
    }

    private void renderMap(char[][] map, Player player) {
        if (map == null) {
            return;
        }
        int mapHeight = screenHeight - HUD_HEIGHT - MESSAGE_AREA_HEIGHT;
        for (int y = 0; y < map.length && y < mapHeight; y++) {
            int screenY = y + MAP_HEIGHT_OFFSET;
            for (int x = 0; x < map[y].length && x < screenWidth; x++) {
                renderTile(map, player, x, y, screenY);
            }
        }
    }

    private void renderTile(char[][] map, Player player, int mapX, int mapY, int screenY) {
        boolean isVisible = player != null && player.isTileVisible(mapX, mapY);
        char tileChar;
        if (isVisible) {
            tileChar = map[mapY][mapX];
            drawVisibleTile(tileChar, mapX, screenY);
        } else if (player != null && player.hasSeen(mapX, mapY)) {
            char seen = player.getSeenTile(mapX, mapY);
            drawMemoryTile(seen == 0 ? '#' : seen, mapX, screenY);
        } else {
            drawBlank(mapX, screenY);
        }
    }

    private void drawVisibleTile(char tile, int x, int y) {
        textGraphics.setBackgroundColor(TextColor.ANSI.BLACK);
        switch (tile) {
            case '#':
                textGraphics.setForegroundColor(TextColor.ANSI.YELLOW);
                break;
            case '+':
                textGraphics.setForegroundColor(TextColor.ANSI.WHITE);
                break;
            case ',':
                textGraphics.setForegroundColor(TextColor.ANSI.WHITE);
                break;
            case '<':
            case '>':
                textGraphics.setForegroundColor(TextColor.ANSI.WHITE_BRIGHT);
                break;
            case 'f':
                textGraphics.setForegroundColor(TextColor.ANSI.GREEN);
                break;
            case 'e':
                textGraphics.setForegroundColor(TextColor.ANSI.CYAN);
                break;
            case 's':
                textGraphics.setForegroundColor(TextColor.ANSI.MAGENTA);
                break;
            case 'w':
                textGraphics.setForegroundColor(TextColor.ANSI.WHITE);
                break;
            default:
                textGraphics.setForegroundColor(TextColor.ANSI.WHITE);
        }
        textGraphics.setCharacter(x, y, tile);
    }

    private void drawMemoryTile(char tile, int x, int y) {
        textGraphics.setBackgroundColor(TextColor.ANSI.BLACK);
        textGraphics.setForegroundColor(TextColor.ANSI.BLACK_BRIGHT);
        boolean rememberable = switch (tile) {
            case '#', '+', '.', ',', '<', '>' -> true;
            default -> false;
        };
        char renderTile = rememberable ? tile : ' ';
        textGraphics.setCharacter(x, y, renderTile);
    }

    private void drawBlank(int x, int y) {
        textGraphics.setBackgroundColor(TextColor.ANSI.BLACK);
        textGraphics.setCharacter(x, y, ' ');
    }

    private void renderPlayer(Player player) {
        textGraphics.setForegroundColor(TextColor.ANSI.WHITE);
        textGraphics.setBackgroundColor(TextColor.ANSI.BLACK);
        if (player != null && player.isTileVisible(player.getX(), player.getY())) {
            textGraphics.setCharacter(player.getX(), player.getY() + MAP_HEIGHT_OFFSET, '@');
        }
    }

    private void renderMonsters(List<Monster> monsters, Player player) {
        if (monsters == null) {
            return;
        }
        for (Monster monster : monsters) {
            if (!monster.isVisible()) {
                continue;
            }
            int renderX = monster.getX();
            int renderY = monster.getY() + MAP_HEIGHT_OFFSET;
            if (renderX < 0 || renderX >= screenWidth) {
                continue;
            }
            if (renderY < MAP_HEIGHT_OFFSET || renderY >= screenHeight - MESSAGE_AREA_HEIGHT) {
                continue;
            }
            if (player != null && !player.isTileVisible(monster.getX(), monster.getY())) {
                continue;
            }
            textGraphics.setForegroundColor(resolveMonsterColor(monster.getColor()));
            textGraphics.setBackgroundColor(TextColor.ANSI.BLACK);
            textGraphics.setCharacter(renderX, renderY, monster.getSymbol());
        }
    }

    private TextColor resolveMonsterColor(MonsterColor color) {
        return switch (color) {
            case GREEN -> TextColor.ANSI.GREEN;
            case RED -> TextColor.ANSI.RED;
            case WHITE -> TextColor.ANSI.WHITE;
            case YELLOW -> TextColor.ANSI.YELLOW;
            case BRIGHT_WHITE -> TextColor.ANSI.WHITE_BRIGHT;
        };
    }

    private void renderHUD(Player player, int currentLevel) {
        renderHUDLine(player, currentLevel);
    }


    private void renderHUDLine(Player player, int currentLevel) {
        int x = 0;

        x = renderHUDValue("Level:", x, COLOR_HUD);
        x = renderHUDValue(String.valueOf(currentLevel), x, COLOR_HUD_VALUE);
        x += 4;

        x = renderHUDValue("Gold:", x, COLOR_HUD);
        x = renderHUDValue(String.format("%3d", player.getGold()), x, COLOR_HUD_VALUE);
        x += 4;

        x = renderHUDValue("Hp:", x, COLOR_HUD);
        String hpString = String.format("%2d(%2d)",
                player.getHealth(), player.getMaxHealth());
        x = renderHUDValue(hpString, x, getHealthColor(player));
        x += 4;

        x = renderHUDValue("Str:", x, COLOR_HUD);
        String strString = String.format("%2d(%2d)", player.getStrength(), player.getMaxStrength());
        x = renderHUDValue(strString, x, COLOR_HUD_VALUE);
        x += 4;

        x = renderHUDValue("Exp:", x, COLOR_HUD);
        String expString = String.format("%d/%d", player.getExperience(), player.getNextLevelExp());
        renderHUDValue(expString, x, COLOR_HUD_IMPORTANT);
    }

    private int renderHUDValue(String value, int x, TextColor color) {
        textGraphics.setForegroundColor(color);
        textGraphics.putString(x, 0, value);
        return x + value.length();
    }

    private TextColor getHealthColor(Player player) {
        float healthPercent = (float) player.getHealth() / player.getMaxHealth();
        if (healthPercent > 0.5f) {
            return COLOR_HUD_VALUE;
        } else if (healthPercent > 0.25f) {
            return TextColor.ANSI.YELLOW;
        } else {
            return TextColor.ANSI.RED;
        }
    }

    private void renderMessages(List<String> messages) {
        int startY = screenHeight - MESSAGE_AREA_HEIGHT;
        for (int i = 0; i < MESSAGE_AREA_HEIGHT; i++) {
            int messageIndex = messages == null ? -1 : messages.size() - MESSAGE_AREA_HEIGHT + i;
            String line = "";
            if (messages != null && messageIndex >= 0 && messageIndex < messages.size()) {
                line = messages.get(messageIndex);
            }
            if (line.length() > screenWidth) {
                line = line.substring(line.length() - screenWidth);
            }
            String padded = String.format("%-" + screenWidth + "s", line);
            textGraphics.setForegroundColor(COLOR_MESSAGE);
            textGraphics.putString(0, startY + i, padded);
        }
    }

    private void putPaddedLine(int y, String text, TextColor color) {
        if (y < 0 || y >= screenHeight) {
            return;
        }

        text = (text == null ? "" : text);
        text = text.substring(0, Math.min(text.length(), screenWidth)).formatted("%-" + screenWidth + "s");

        textGraphics.setBackgroundColor(TextColor.ANSI.BLACK);
        textGraphics.setForegroundColor(color);
        textGraphics.putString(0, y, text);
    }

    private void putCenteredLine(int y, String text, TextColor color) {
        if (y < 0 || y >= screenHeight) return;

        text = text == null ? "" : text;

        text = text.length() > screenWidth
                ? text.substring(0, screenWidth)
                : text;

        int leftPadding = Math.max(0, (screenWidth - text.length()) / 2);
        String padded = String.format("%" + leftPadding + "s%s", "", text);

        textGraphics.setBackgroundColor(TextColor.ANSI.BLACK);
        textGraphics.setForegroundColor(color);
        textGraphics.putString(0, y, padded);
    }
}
