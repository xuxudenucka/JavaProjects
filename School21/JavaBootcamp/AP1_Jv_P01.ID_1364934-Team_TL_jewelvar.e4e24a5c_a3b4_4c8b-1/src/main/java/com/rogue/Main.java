package com.rogue;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.rogue.domain.Game;
import com.rogue.presentation.LanternaGameScreen;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("Запуск Rogue 1980...");

        Terminal terminal = new DefaultTerminalFactory()
                .setInitialTerminalSize(new TerminalSize(80, 30))
                .createTerminal();

        Screen screen = new TerminalScreen(terminal);
        screen.startScreen();
        try {
            LanternaGameScreen ui = new LanternaGameScreen(screen);
            Game game = new Game(ui, ui);
            game.run();
        } finally {
            screen.stopScreen();
        }
    }
}
