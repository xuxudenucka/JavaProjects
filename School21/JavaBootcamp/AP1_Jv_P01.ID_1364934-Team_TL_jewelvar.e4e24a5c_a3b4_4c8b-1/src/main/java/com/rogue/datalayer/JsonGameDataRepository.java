package com.rogue.datalayer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.rogue.domain.ScoreBoard;
import com.rogue.domain.state.GameSnapshot;
import com.rogue.datalayer.model.GameSaveData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class JsonGameDataRepository implements GameDataRepository {
    private final Path saveDir;
    private final Path sessionFile;
    private final Path scoreboardFile;
    private final ObjectMapper mapper;

    public JsonGameDataRepository() {
        this(defaultDirectory());
    }

    public JsonGameDataRepository(Path directory) {
        this.saveDir = directory;
        this.sessionFile = saveDir.resolve("session.json");
        this.scoreboardFile = saveDir.resolve("scoreboard.json");
        this.mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    private static Path defaultDirectory() {
        String projectDir = System.getProperty("user.dir");
        if (projectDir == null || projectDir.isBlank()) {
            projectDir = ".";
        }
        return Path.of(projectDir, "src", "main", "java", "com", "rogue", "datalayer");
    }

    @Override
    public Optional<GameSnapshot> loadGame() {
        if (!Files.exists(sessionFile)) {
            return Optional.empty();
        }
        try {
            GameSaveData data = mapper.readValue(sessionFile.toFile(), GameSaveData.class);
            return Optional.ofNullable(GameSnapshotMapper.fromData(data));
        } catch (IOException e) {
            System.err.println("Failed to load game: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void saveGame(GameSnapshot snapshot) {
        if (snapshot == null) {
            return;
        }
        try {
            Files.createDirectories(saveDir);
            GameSaveData data = GameSnapshotMapper.toData(snapshot);
            mapper.writeValue(sessionFile.toFile(), data);
        } catch (IOException e) {
            System.err.println("Failed to save game: " + e.getMessage());
        }
    }

    @Override
    public void clearGame() {
        try {
            Files.deleteIfExists(sessionFile);
        } catch (IOException e) {
            System.err.println("Failed to clear saved game: " + e.getMessage());
        }
    }

    @Override
    public Optional<List<ScoreBoard.ScoreRecord>> loadScoreBoard() {
        if (!Files.exists(scoreboardFile)) {
            return Optional.empty();
        }
        try {
            List<ScoreBoard.ScoreRecord> records = mapper.readValue(
                    scoreboardFile.toFile(),
                    new TypeReference<>() {
                    });
            return Optional.of(records);
        } catch (IOException e) {
            System.err.println("Failed to load scoreboard: " + e.getMessage());
            return Optional.of(Collections.emptyList());
        }
    }

    @Override
    public void saveScoreBoard(List<ScoreBoard.ScoreRecord> records) {
        try {
            Files.createDirectories(saveDir);
            mapper.writeValue(scoreboardFile.toFile(),
                    records == null ? Collections.emptyList() : records);
        } catch (IOException e) {
            System.err.println("Failed to save scoreboard: " + e.getMessage());
        }
    }
}
