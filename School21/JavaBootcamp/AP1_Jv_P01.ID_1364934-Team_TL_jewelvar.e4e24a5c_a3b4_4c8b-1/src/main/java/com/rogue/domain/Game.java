package com.rogue.domain;

import com.rogue.domain.LevelGenerator.LevelData;
import com.rogue.datalayer.GameDataRepository;
import com.rogue.datalayer.JsonGameDataRepository;
import com.rogue.domain.difficulty.AdaptiveDifficultyManager;
import com.rogue.domain.difficulty.DifficultyProfile;
import com.rogue.domain.entities.monsters.Ghost;
import com.rogue.domain.entities.monsters.Mimic;
import com.rogue.domain.entities.monsters.Monster;
import com.rogue.domain.entities.monsters.MonsterFactory;
import com.rogue.domain.entities.monsters.Ogre;
import com.rogue.domain.entities.monsters.SnakeMage;
import com.rogue.domain.entities.monsters.Vampire;
import com.rogue.domain.entities.player.Player;
import com.rogue.domain.entities.player.Player.TemporaryBuff;
import com.rogue.domain.input.InputCommand;
import com.rogue.domain.input.InputProvider;
import com.rogue.domain.input.InputType;
import com.rogue.domain.item.Item;
import com.rogue.domain.item.ItemSubtype;
import com.rogue.domain.item.ItemType;
import com.rogue.domain.state.GameSnapshot;
import com.rogue.domain.state.LevelSnapshot;
import com.rogue.domain.state.MonsterSnapshot;
import com.rogue.domain.state.PlayerSnapshot;
import com.rogue.domain.util.MapUtils;
import com.rogue.domain.util.Position;
import com.rogue.presentation.GameScreen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

public class Game {
    private static final int MAX_LEVEL = 21;
    private static final String PLAYER_NAME = "Adventurer";
    private static final int VAMPIRE_DRAIN_AMOUNT = 2;
    private static final double SNAKE_SLEEP_CHANCE = 0.35;

    private final GameScreen gameScreen;
    private final InputProvider inputProvider;
    private final GameDataRepository repository;
    private final Random random = new Random();
    private final ScoreBoard scoreBoard;
    private final AdaptiveDifficultyManager adaptiveDifficultyManager;
    private Player player;
    private char[][] currentMap;
    private List<Monster> monsters;
    private int currentLevel;
    private List<String> messageLog;
    private boolean isRunning;
    private int turnCounter;
    private ItemType pendingSelectionType;
    private boolean pendingSelectionAllowZero;
    private boolean showStats;
    private boolean showDeathScreen;
    private int monstersDefeated;
    private int foodConsumed;
    private int elixirsConsumed;
    private int scrollsRead;
    private int hitsDealt;
    private int hitsTaken;
    private int tilesTraversed;
    private int playerSleepTurns;
    private ScoreBoard.ScoreRecord lastRunRecord;
    private String lastRunMessage;
    private String nextRunSummary;

    public Game(GameScreen gameScreen, InputProvider inputProvider) {
        this.gameScreen = gameScreen;
        this.inputProvider = inputProvider;
        this.messageLog = new ArrayList<>();
        this.monsters = new ArrayList<>();
        this.isRunning = true;
        this.repository = new JsonGameDataRepository();
        this.scoreBoard = new ScoreBoard();
        this.adaptiveDifficultyManager = new AdaptiveDifficultyManager();
        repository.loadScoreBoard().ifPresent(scoreBoard::loadRecords);
    }

    private boolean tryResumeGame() {
        Optional<GameSnapshot> saveData = repository.loadGame();
        if (saveData.isEmpty()) {
            return false;
        }
        return restoreFromSnapshot(saveData.get());
    }

    private void startNewRun() {
        startNewRun(null);
    }

    private void startNewRun(String previousSummary) {
        repository.clearGame();
        currentLevel = 1;
        turnCounter = 0;
        player = null;
        showStats = false;
        showDeathScreen = false;
        lastRunRecord = null;
        lastRunMessage = null;
        nextRunSummary = null;
        monsters = new ArrayList<>();
        resetRunStats();
        playerSleepTurns = 0;
        adaptiveDifficultyManager.reset();
        messageLog.clear();
        if (previousSummary != null && !previousSummary.isBlank()) {
            messageLog.add(previousSummary);
        }
        loadCurrentLevel();
    }

    private void attemptPlayerMove(int dx, int dy) {
        if (!ensurePlayerAwake()) {
            return;
        }
        int targetX = player.getX() + dx;
        int targetY = player.getY() + dy;
        if (!isInsideMap(targetX, targetY)) {
            return;
        }
        Monster monster = findMonsterAt(targetX, targetY);
        if (monster != null) {
            boolean defeated = resolvePlayerAttack(monster);
            if (!player.isAlive()) return;
            if (defeated) {
                if (movePlayerAndTrack(dx, dy)) {
                    postMoveAction();
                } else {
                    completePlayerTurn();
                }
            } else {
                completePlayerTurn();
            }
            return;
        }
        if (movePlayerAndTrack(dx, dy)) {
            postMoveAction();
        }
    }

    public void run() throws IOException {
        if (!tryResumeGame()) {
            startNewRun();
        }
        while (isRunning) {
            render();
            handleInput();
        }
    }

    private void loadCurrentLevel() {
        if (player != null) {
            player.removeExpiredBuffs(currentLevel);
        }
        DifficultyProfile profile = adaptiveDifficultyManager.currentProfile();
        LevelData levelData = LevelGenerator.generateLevel(currentLevel, profile);
        currentMap = levelData.map();
        monsters = new ArrayList<>(levelData.monsters());
        ensurePlayerInitialized();
        player.resetSeenMap();
        Position startPosition = levelData.startPosition();
        player.setPosition(startPosition.x(), startPosition.y());
        player.updateVisibility(currentMap);
        messageLog.add("You are on level " + currentLevel);
        adaptiveDifficultyManager.onLevelStart(player);
        persistGameState();
    }

    private void ensurePlayerInitialized() {
        if (player == null) {
            player = new Player(1, 1, currentMap[0].length, currentMap.length);
        }
    }

    private void render() throws IOException {
        if (showDeathScreen) {
            gameScreen.renderDeathScreen(lastRunMessage, lastRunRecord);
        } else if (showStats) {
            gameScreen.renderStats(scoreBoard.topRecords(10));
        } else {
            gameScreen.renderGame(currentMap, player, currentLevel, monsters, messageLog);
        }
        gameScreen.refresh();
    }

    private void handleInput() throws IOException {
        InputCommand input = inputProvider.readInput();
        if (input == null) {
            return;
        }
        if (showDeathScreen) {
            handleDeathScreenInput(input);
            return;
        }
        if (showStats && input.type() != InputType.TAB) {
            return;
        }

        switch (input.type()) {
            case TAB -> toggleStatsView();
            case CHARACTER -> handleCharacterInput(input.character());
            case ESCAPE -> messageLog.add("Really quit? (y/n)");
            default -> {
            }
        }
    }

    private void postMoveAction() {
        int x = player.getX();
        int y = player.getY();

        switch (currentMap[y][x]) {
            case '>':
                descendStairs();
                break;
            case '+':
                break;
            case '$':
                pickUpGold();
                break;
            case 'f':
            case 'e':
            case 's':
            case 'w':
                pickUpItem(currentMap[y][x]);
                break;
        }

        completePlayerTurn();
    }

    private void descendStairs() {
        adaptiveDifficultyManager.onLevelCompleted(player, true);
        currentLevel++;
        if (currentLevel > MAX_LEVEL) {
            handleGameCompleted();
            return;
        }
        messageLog.add("You descend to level " + currentLevel);
        loadCurrentLevel();
    }

    private void moveMonsters() {
        if (monsters == null || monsters.isEmpty() || player == null || !player.isAlive()) {
            return;
        }

        for (Monster monster : new ArrayList<>(monsters)) {
            Optional<Position> decision = monster.decideNextPosition(player, currentMap, monsters);
            if (decision.isEmpty()) {
                continue;
            }
            Position target = decision.get();
            if (!MapUtils.isWalkable(currentMap, target.x(), target.y())) {
                continue;
            }
            if (player.getX() == target.x() && player.getY() == target.y()) {
                monsterAttackPlayer(monster, true);
                continue;
            }
            if (isTileOccupiedByMonster(target.x(), target.y(), monster)) {
                continue;
            }
            monster.setPosition(target.x(), target.y());
        }
    }

    private void completePlayerTurn() {
        advanceTurnCounter();
        moveMonsters();
        if (player != null && player.isAlive()) {
            player.updateVisibility(currentMap);
        }
        if (player != null && player.isAlive()) {
            persistGameState();
        }
    }

    private void advanceTurnCounter() {
        turnCounter++;
        adaptiveDifficultyManager.recordTurn();
        if (turnCounter % 15 == 0 && player.isAlive() && player.getHealth() < player.getMaxHealth()) {
            player.heal(1);
            adaptiveDifficultyManager.recordHealing(1);
            messageLog.add("You feel a little better.");
        }
    }

    private boolean resolvePlayerAttack(Monster monster) {
        if (player == null || !player.isAlive()) {
            return false;
        }
        boolean defeated = playerStrike(monster);
        if (defeated) {
            return true;
        }
        monsterAttackPlayer(monster, false);
        return !monster.isAlive();
    }

    private boolean playerStrike(Monster monster) {
        if (monster == null || player == null || !player.isAlive()) {
            return false;
        }
        revealMonster(monster);
        if (monster instanceof Vampire vampire && !vampire.hasDodgedInitialStrike()) {
            vampire.markInitialDodge();
            messageLog.add("You miss the " + monsterDisplayName(monster) + ".");
            return false;
        }
        if (rollToHit(player.getStrength(), monster.getStrength())) {
            int damage = calculatePlayerDamage();
            monster.takeDamage(damage);
            hitsDealt++;
            messageLog.add("You hit the " + monsterDisplayName(monster) + " for " + damage + " damage.");
            if (!monster.isAlive()) {
                handleMonsterDefeated(monster);
                return true;
            }
        } else {
            messageLog.add("You miss the " + monsterDisplayName(monster) + ".");
        }
        return false;
    }

    private void monsterAttackPlayer(Monster monster, boolean allowCounter) {
        if (monster == null || player == null || !player.isAlive()) {
            return;
        }
        revealMonster(monster);
        boolean hit = rollToHit(monster.getStrength(), player.getStrength());
        if (hit) {
            int damage = calculateMonsterDamage(monster);
            player.takeDamage(damage);
            adaptiveDifficultyManager.recordDamageTaken(damage, player);
            hitsTaken++;
            messageLog.add(monsterDisplayName(monster) + " hits you for " + damage + " damage!");
            if (!player.isAlive()) {
                handlePlayerDeath("You were slain by the " + monsterDisplayName(monster) + ".");
                return;
            }
            if (monster instanceof Vampire) {
                drainPlayerVitality();
            }
            if (monster instanceof SnakeMage && random.nextDouble() < SNAKE_SLEEP_CHANCE) {
                applySleepEffect(1);
            }
        } else {
            messageLog.add(monsterDisplayName(monster) + " misses you.");
        }
        if (monster instanceof Ogre ogre) {
            ogre.triggerRest();
        }
        if (allowCounter && player != null && player.isAlive()) {
            playerStrike(monster);
        }
    }

    private void pickUpGold() {
        int goldAmount = 10 + (currentLevel * 5);
        player.addGold(goldAmount);
        currentMap[player.getY()][player.getX()] = MapUtils.ROOM_FLOOR;
        messageLog.add("You found " + goldAmount + " gold pieces");
    }

    private void pickUpItem(char tile) {
        if (player == null) {
            return;
        }
        Item item = createItemFromTile(tile);
        if (item == null) {
            messageLog.add("You step over something unremarkable.");
            currentMap[player.getY()][player.getX()] = MapUtils.ROOM_FLOOR;
            return;
        }
        if (item.getType() == ItemType.WEAPON &&
            player.getInventory().hasItem(ItemType.WEAPON, item.getSubtype())) {
            messageLog.add("You already carry a " + describeItem(item) + ".");
            return;
        }
        boolean added = player.getInventory().addItem(item);
        if (added) {
            currentMap[player.getY()][player.getX()] = MapUtils.ROOM_FLOOR;
            messageLog.add("You pick up " + describeItem(item) + ".");
        } else {
            messageLog.add("Your " + typeLabel(item.getType()) + " pouch is full.");
        }
    }

    private void handleCharacterInput(Character ch) {
        if (pendingSelectionType != null) {
            handleInventorySelectionInput(ch);
            return;
        }

        char normalized = Character.toLowerCase(ch);
        switch (ch) {
            case 'y':
                if (messageLog.getLast().contains("quit")) {
                    isRunning = false;
                }
                break;
            case 'n':
                if (messageLog.getLast().contains("quit")) {
                    messageLog.add("Continuing game...");
                }
                break;
            default:
                switch (normalized) {
                    case 'w':
                        attemptPlayerMove(0, -1);
                        break;
                    case 's':
                        attemptPlayerMove(0, 1);
                        break;
                    case 'a':
                        attemptPlayerMove(-1, 0);
                        break;
                    case 'd':
                        attemptPlayerMove(1, 0);
                        break;
                    case 'h':
                        if (!ensurePlayerAwake()) {
                            break;
                        }
                        promptItemSelection(ItemType.WEAPON, true);
                        break;
                    case 'j':
                        if (!ensurePlayerAwake()) {
                            break;
                        }
                        promptItemSelection(ItemType.FOOD, false);
                        break;
                    case 'k':
                        if (!ensurePlayerAwake()) {
                            break;
                        }
                        promptItemSelection(ItemType.ELIXIR, false);
                        break;
                    case 'e':
                        if (!ensurePlayerAwake()) {
                            break;
                        }
                        promptItemSelection(ItemType.SCROLL, false);
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    private void handleInventorySelectionInput(char ch) {
        if (ch == ' ') {
            messageLog.add("You close your pack without taking anything.");
            clearPendingSelection();
            return;
        }
        if (!Character.isDigit(ch)) {
            messageLog.add("Select an item number (digit) or 0 if allowed.");
            return;
        }
        if (player == null) {
            clearPendingSelection();
            return;
        }

        int digit = ch - '0';
        if (pendingSelectionAllowZero && digit == 0) {
            player.equipWeapon(null);
            messageLog.add("You put your weapon away.");
            clearPendingSelection();
            completePlayerTurn();
            return;
        }

        if (digit <= 0 || digit > 9) {
            messageLog.add("Choose a number between " + (pendingSelectionAllowZero ? "0" : "1") + " and 9.");
            return;
        }

        int index = digit - 1;
        List<Item> items = player.getInventory().viewItems(pendingSelectionType);
        if (index >= items.size()) {
            messageLog.add("There is no item under that number.");
            return;
        }

        Item item = items.get(index);
        switch (pendingSelectionType) {
            case WEAPON -> {
                player.equipWeapon(item);
                messageLog.add("You wield the " + describeItem(item) + ".");
                clearPendingSelection();
                completePlayerTurn();
            }
            case FOOD -> consumeFood(index);
            case ELIXIR -> consumeElixir(index);
            case SCROLL -> consumeScroll(index);
            default -> messageLog.add("You can't use that item right now.");
        }
    }

    private void promptItemSelection(ItemType type, boolean allowZero) {
        List<Item> items = player.getInventory().viewItems(type);
        if (items.isEmpty() && !(allowZero && type == ItemType.WEAPON)) {
            messageLog.add("You have no " + typeLabel(type) + " to use.");
            return;
        }
        pendingSelectionType = type;
        pendingSelectionAllowZero = allowZero;

        String prompt = switch (type) {
            case WEAPON -> "Select a weapon (0 to empty hands)";
            case FOOD -> "Select food to eat";
            case ELIXIR -> "Select an elixir to drink";
            case SCROLL -> "Select a scroll to read";
            default -> "Select an item";
        };
        messageLog.add(prompt + ":");
        if (items.isEmpty()) {
            messageLog.add("No items available; press 0 to keep hands empty.");
            return;
        }
        for (int i = 0; i < items.size() && i < 9; i++) {
            messageLog.add((i + 1) + ") " + describeItem(items.get(i)));
        }
        if (items.size() > 9) {
            messageLog.add("Only the first 9 items are selectable.");
        }
    }

    private void consumeFood(int index) {
        Item item = player.getInventory().removeItem(ItemType.FOOD, index);
        if (item == null) {
            messageLog.add("The meal is gone.");
            return;
        }
        foodConsumed++;
        int before = player.getHealth();
        player.heal(item.getHealthDelta());
        int healed = player.getHealth() - before;
        adaptiveDifficultyManager.recordHealing(healed);
        messageLog.add("You eat the " + describeItem(item) + " and recover " + healed + " hp.");
        clearPendingSelection();
        completePlayerTurn();
    }

    private void consumeElixir(int index) {
        Item item = player.getInventory().removeItem(ItemType.ELIXIR, index);
        if (item == null) {
            messageLog.add("The elixir bottle is empty.");
            return;
        }
        elixirsConsumed++;
        applyTemporaryElixirBonus(item);
        messageLog.add("You drink the " + describeItem(item) + " and feel empowered for this floor.");
        clearPendingSelection();
        completePlayerTurn();
    }

    private void consumeScroll(int index) {
        Item item = player.getInventory().removeItem(ItemType.SCROLL, index);
        if (item == null) {
            messageLog.add("The scroll crumbles to dust.");
            return;
        }
        scrollsRead++;
        applyPermanentBonuses(item);
        messageLog.add("You read the " + describeItem(item) + " and gain lasting knowledge.");
        clearPendingSelection();
        completePlayerTurn();
    }

    private void applyTemporaryElixirBonus(Item item) {
        if (item.getHealthDelta() > 0) {
            player.heal(item.getHealthDelta());
            adaptiveDifficultyManager.recordHealing(item.getHealthDelta());
        }
        player.applyTemporaryBuff(
                item.getStrengthDelta(),
                item.getMaxHealthDelta(),
                currentLevel
        );
    }

    private void applyPermanentBonuses(Item item) {
        if (item.getMaxHealthDelta() != 0) {
            player.modifyMaxHealth(item.getMaxHealthDelta());
            if (item.getMaxHealthDelta() > 0) {
                player.heal(item.getMaxHealthDelta());
                adaptiveDifficultyManager.recordHealing(item.getMaxHealthDelta());
            }
        }
        if (item.getHealthDelta() > 0) {
            player.heal(item.getHealthDelta());
            adaptiveDifficultyManager.recordHealing(item.getHealthDelta());
        }
        if (item.getStrengthDelta() != 0) {
            player.modifyStrength(item.getStrengthDelta());
        }
    }

    private String describeItem(Item item) {
        return item.getSubtype().name().toLowerCase().replace('_', ' ');
    }

    private String typeLabel(ItemType type) {
        return switch (type) {
            case WEAPON -> "weapons";
            case FOOD -> "food";
            case ELIXIR -> "elixirs";
            case SCROLL -> "scrolls";
            case TREASURE -> "treasures";
        };
    }

    private void clearPendingSelection() {
        pendingSelectionType = null;
        pendingSelectionAllowZero = false;
    }

    private Item createItemFromTile(char tile) {
        return switch (tile) {
            case 'f' -> Item.builder(ItemType.FOOD, ItemSubtype.FOOD_RATION)
                    .healthDelta(4 + random.nextInt(4))
                    .build();
            case 'e' -> {
                int roll = random.nextInt(2);
                ItemSubtype subtype = roll == 0
                        ? ItemSubtype.ELIXIR_STRENGTH
                        : ItemSubtype.ELIXIR_MAX_HEALTH;
                Item.Builder builder = Item.builder(ItemType.ELIXIR, subtype)
                        .healthDelta(2);
                switch (subtype) {
                    case ELIXIR_STRENGTH -> builder.strengthDelta(3);
                    case ELIXIR_MAX_HEALTH -> builder.maxHealthDelta(5);
                }
                yield builder.build();
            }
            case 's' -> {
                int roll = random.nextInt(2);
                ItemSubtype subtype = roll == 0
                        ? ItemSubtype.SCROLL_STRENGTH
                        : ItemSubtype.SCROLL_MAX_HEALTH;
                Item.Builder builder = Item.builder(ItemType.SCROLL, subtype);
                switch (subtype) {
                    case SCROLL_STRENGTH -> builder.strengthDelta(1);
                    case SCROLL_MAX_HEALTH -> builder.maxHealthDelta(1);
                }
                yield builder.build();
            }
            case 'w' -> {
                int roll = random.nextInt(3);
                ItemSubtype subtype = switch (roll) {
                    case 0 -> ItemSubtype.WEAPON_SWORD;
                    case 1 -> ItemSubtype.WEAPON_AXE;
                    default -> ItemSubtype.WEAPON_DAGGER;
                };
                int strengthBonus = switch (subtype) {
                    case WEAPON_AXE -> 4;
                    case WEAPON_SWORD -> 3;
                    case WEAPON_DAGGER -> 2;
                    default -> 1;
                };
                yield Item.builder(ItemType.WEAPON, subtype)
                        .strengthDelta(strengthBonus)
                        .build();
            }
            default -> null;
        };
    }

    private Monster findMonsterAt(int x, int y) {
        if (monsters == null) {
            return null;
        }
        for (Monster monster : monsters) {
            if (monster.getX() == x && monster.getY() == y) {
                return monster;
            }
        }
        return null;
    }

    private boolean isTileOccupiedByMonster(int x, int y, Monster mover) {
        if (monsters == null) {
            return false;
        }
        for (Monster monster : monsters) {
            if (monster == mover) {
                continue;
            }
            if (monster.getX() == x && monster.getY() == y) {
                return true;
            }
        }
        return false;
    }

    private String monsterDisplayName(Monster monster) {
        String rawName = monster.getType().name().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(rawName.charAt(0)) + rawName.substring(1);
    }

    private void handleMonsterDefeated(Monster monster) {
        if (monsters != null) {
            monsters.remove(monster);
        }
        monstersDefeated++;
        adaptiveDifficultyManager.recordMonsterDefeated();
        int loot = calculateLoot(monster);
        player.addGold(loot);
        messageLog.add("The " + monsterDisplayName(monster) + " is defeated. You loot " + loot + " gold.");
        int experience = calculateExperience(monster);
        grantExperience(experience);
    }

    private int calculateLoot(Monster monster) {
        int stats = monster.getHostilityRadius() + monster.getStrength() + monster.getMaxHealth() / 2;
        int base = Math.max(5, stats / 3);
        int variance = Math.max(1, stats / 4);
        return base + random.nextInt(variance);
    }

    private int calculateExperience(Monster monster) {
        int stats = monster.getStrength() * 2 + monster.getHostilityRadius();
        stats += Math.max(1, monster.getMaxHealth() / 4);
        return Math.max(1, stats / 2);
    }

    private void grantExperience(int amount) {
        if (player == null || amount <= 0) {
            return;
        }
        int previousLevel = player.getLevel();
        player.gainExperience(amount);
        messageLog.add("You gain " + amount + " experience.");
        if (player.getLevel() > previousLevel) {
            messageLog.add("You advance to level " + player.getLevel() + "!");
        }
    }

    private boolean rollToHit(int attackerPower, int defenderPower) {
        int attackScore = Math.max(1, attackerPower);
        int defenseScore = Math.max(1, defenderPower);
        double chance = (double) attackScore / (attackScore + defenseScore);
        return random.nextDouble() < chance;
    }

    private int calculatePlayerDamage() {
        int weaponBonus = 0;
        Item weapon = player.getCurrentWeapon();
        if (weapon != null) {
            weaponBonus = Math.max(0, weapon.getStrengthDelta());
        }
        int base = Math.max(1, player.getStrength() + weaponBonus);
        int variance = Math.max(1, base / 4);
        return base + random.nextInt(variance + 1);
    }

    private int calculateMonsterDamage(Monster monster) {
        int base = Math.max(1, monster.getStrength());
        int variance = Math.max(1, base / 4);
        int rawDamage = base + random.nextInt(variance + 1);
        return Math.max(1, rawDamage);
    }

    private void handlePlayerDeath(String message) {
        adaptiveDifficultyManager.onLevelCompleted(player, false);
        ScoreBoard.ScoreRecord record = recordScore(false);
        String summary = message;
        summary += String.format(" Depth: %d, treasure: %d.", record.depthReached(), record.treasureCollected());
        lastRunRecord = record;
        lastRunMessage = message;
        nextRunSummary = summary;
        showDeathScreen = true;
        showStats = false;
        repository.clearGame();
    }

    private void handleGameCompleted() {
        adaptiveDifficultyManager.onLevelCompleted(player, true);
        ScoreBoard.ScoreRecord record = recordScore(true);
        String summary = "You have conquered all " + MAX_LEVEL + " levels!";
        summary += String.format(" Treasure hauled: %d.", record.treasureCollected());
        startNewRun(summary);
    }

    private ScoreBoard.ScoreRecord recordScore(boolean success) {
        int depth = success ? MAX_LEVEL : Math.min(currentLevel, MAX_LEVEL);
        ScoreBoard.ScoreRecord record = new ScoreBoard.ScoreRecord(
                PLAYER_NAME,
                depth,
                player.getGold(),
                monstersDefeated,
                foodConsumed,
                elixirsConsumed,
                scrollsRead,
                hitsDealt,
                hitsTaken,
                tilesTraversed);
        scoreBoard.addRecord(record);
        repository.saveScoreBoard(new ArrayList<>(scoreBoard.getRecords()));
        return record;
    }

    private boolean movePlayerAndTrack(int dx, int dy) {
        boolean moved = player.move(dx, dy, currentMap);
        if (moved) {
            tilesTraversed++;
        }
        return moved;
    }

    private boolean isInsideMap(int x, int y) {
        return currentMap != null
               && y >= 0
               && y < currentMap.length
               && x >= 0
               && x < currentMap[y].length;
    }

    private void toggleStatsView() {
        showStats = !showStats;
    }

    private boolean ensurePlayerAwake() {
        if (player == null) {
            return false;
        }
        if (playerSleepTurns > 0) {
            playerSleepTurns--;
            messageLog.add("You are fast asleep...");
            completePlayerTurn();
            return false;
        }
        return true;
    }

    private void applySleepEffect(int turns) {
        if (playerSleepTurns >= turns) {
            return;
        }
        playerSleepTurns = turns;
        messageLog.add("A magical drowsiness overcomes you!");
    }

    private void drainPlayerVitality() {
        if (player == null) {
            return;
        }
        int maxDrain = Math.max(0, player.getMaxHealth() - 1);
        if (maxDrain == 0) {
            return;
        }
        int drain = Math.min(VAMPIRE_DRAIN_AMOUNT, maxDrain);
        player.modifyMaxHealth(-drain);
        player.ensureMinimumHealth();
        messageLog.add("The vampire drains " + drain + " from your life force!");
    }

    private void revealMonster(Monster monster) {
        if (monster instanceof Ghost ghost && !ghost.isRevealed()) {
            ghost.reveal();
            messageLog.add("The ghost materializes!");
        } else if (monster instanceof Mimic mimic && !mimic.isRevealed()) {
            mimic.reveal(currentMap);
            messageLog.add("The \"item\" sprouts fangs—it's a mimic!");
        }
    }

    private void resetRunStats() {
        monstersDefeated = 0;
        foodConsumed = 0;
        elixirsConsumed = 0;
        scrollsRead = 0;
        hitsDealt = 0;
        hitsTaken = 0;
        tilesTraversed = 0;
        playerSleepTurns = 0;
    }

    private void handleDeathScreenInput(InputCommand input) {
        if (input == null) {
            return;
        }
        if (input.type() == InputType.ESCAPE) {
            isRunning = false;
            return;
        }
        if (input.type() == InputType.ENTER) {
            showDeathScreen = false;
            String summary = nextRunSummary;
            lastRunMessage = null;
            nextRunSummary = null;
            ScoreBoard.ScoreRecord record = lastRunRecord;
            lastRunRecord = null;
            startNewRun(summary);
            messageLog.add(String.format(
                    "Previous run: depth %d, treasure %d.",
                    record.depthReached(),
                    record.treasureCollected()
            ));
        }
    }

    private boolean restoreFromSnapshot(GameSnapshot snapshot) {
        if (snapshot == null) {
            return false;
        }
        currentLevel = snapshot.currentLevel();
        turnCounter = snapshot.turnCounter();
        monstersDefeated = snapshot.monstersDefeated();
        foodConsumed = snapshot.foodConsumed();
        elixirsConsumed = snapshot.elixirsConsumed();
        scrollsRead = snapshot.scrollsRead();
        hitsDealt = snapshot.hitsDealt();
        hitsTaken = snapshot.hitsTaken();
        tilesTraversed = snapshot.tilesTraversed();
        pendingSelectionType = null;
        pendingSelectionAllowZero = false;
        showStats = false;

        LevelSnapshot levelSnapshot = snapshot.level();
        if (levelSnapshot == null || levelSnapshot.map() == null) {
            return false;
        }
        currentMap = cloneMap(levelSnapshot.map());
        monsters = restoreMonsters(levelSnapshot.monsters());
        restorePlayer(snapshot.player(), levelSnapshot);

        if (snapshot.messages().isEmpty()) {
            messageLog = new ArrayList<>();
            messageLog.add("Resuming expedition...");
        } else {
            messageLog = new ArrayList<>(snapshot.messages());
        }

        if (player != null && currentMap != null) {
            player.updateVisibility(currentMap);
        }
        playerSleepTurns = 0;
        return true;
    }

    private void restorePlayer(PlayerSnapshot snapshot, LevelSnapshot levelSnapshot) {
        if (snapshot == null) {
            ensurePlayerInitialized();
            return;
        }
        int width = snapshot.mapWidth() > 0 ? snapshot.mapWidth() : currentMap[0].length;
        int height = snapshot.mapHeight() > 0 ? snapshot.mapHeight() : currentMap.length;
        player = new Player(snapshot.x(), snapshot.y(), width, height);
        player.setMaxHealthAbsolute(snapshot.maxHealth());
        player.setHealthAbsolute(snapshot.health());
        player.setStrengthAbsolute(snapshot.strength());
        player.restoreProgress(
                snapshot.level(),
                snapshot.gold(),
                snapshot.experience(),
                snapshot.nextLevelExp(),
                snapshot.maxStrength());
        char[][] seen = levelSnapshot == null ? null : cloneMap(levelSnapshot.seenMap());
        if (seen != null) {
            player.restoreSeenMap(seen);
        } else {
            player.resetSeenMap();
        }
        restoreInventory(snapshot);
        player.setTemporaryBuffs(snapshot.buffs());
    }

    private void restoreInventory(PlayerSnapshot snapshot) {
        if (snapshot == null) {
            return;
        }
        snapshot.inventory().forEach((type, items) -> {
            if (items == null) {
                return;
            }
            for (Item item : items) {
                Item restored = cloneItem(item);
                if (restored != null) {
                    player.getInventory().addItem(restored);
                }
            }
        });
        Item weapon = cloneItem(snapshot.equippedWeapon());
        if (weapon != null) {
            if (!player.getInventory().hasItem(ItemType.WEAPON, weapon.getSubtype())) {
                player.getInventory().addItem(weapon);
            }
            player.equipWeapon(weapon);
        } else {
            player.equipWeapon(null);
        }
    }

    private List<Monster> restoreMonsters(List<MonsterSnapshot> monsterSnapshots) {
        List<Monster> restored = new ArrayList<>();
        if (monsterSnapshots == null) {
            return restored;
        }
        for (MonsterSnapshot snapshot : monsterSnapshots) {
            if (snapshot == null) {
                continue;
            }
            try {
                Monster monster = MonsterFactory.create(snapshot.type(), snapshot.x(), snapshot.y());
                monster.setPosition(snapshot.x(), snapshot.y());
                monster.setHealthAbsolute(snapshot.health());
                Map<String, Integer> extras = snapshot.extras();
                if (extras != null) {
                    switch (monster) {
                        case Ghost ghost -> {
                            ghost.setPhaseCounter(extras.getOrDefault("phase", 0));
                            ghost.setRevealed(extras.getOrDefault("revealed", 0) == 1);
                        }
                        case Ogre ogre -> {
                            ogre.setDirectionIndex(extras.getOrDefault("dir", 0));
                            ogre.setRestCounter(extras.getOrDefault("rest", 0));
                        }
                        case SnakeMage snake -> {
                            snake.setDx(extras.getOrDefault("dx", 1));
                            snake.setDy(extras.getOrDefault("dy", 1));
                        }
                        case Vampire vampire -> vampire.setInitialDodgeUsed(extras.getOrDefault("dodge", 0) == 1);
                        case Mimic mimic -> {
                            Integer disguiseValue = extras.get("disguise");
                            if (disguiseValue != null) {
                                mimic.setDisguiseTile((char) disguiseValue.intValue());
                            }
                            Integer baseValue = extras.get("base");
                            if (baseValue != null) {
                                mimic.setBaseTile((char) baseValue.intValue());
                            }
                            boolean revealed = extras.getOrDefault("revealed", 0) == 1;
                            mimic.setRevealed(revealed);
                            if (currentMap != null
                                && mimic.getY() >= 0
                                && mimic.getY() < currentMap.length
                                && mimic.getX() >= 0
                                && mimic.getX() < currentMap[mimic.getY()].length) {
                                currentMap[mimic.getY()][mimic.getX()] = revealed
                                        ? mimic.getBaseTile()
                                        : mimic.getDisguiseTile();
                            }
                        }
                        default -> {
                        }
                    }
                }
                restored.add(monster);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return restored;
    }

    private void persistGameState() {
        if (player == null || currentMap == null || !player.isAlive()) {
            return;
        }
        GameSnapshot snapshot = createSnapshot();
        if (snapshot != null) {
            repository.saveGame(snapshot);
        }
    }

    private GameSnapshot createSnapshot() {
        if (player == null || currentMap == null) {
            return null;
        }
        LevelSnapshot levelSnapshot = createLevelSnapshot();
        if (levelSnapshot == null) {
            return null;
        }
        PlayerSnapshot playerSnapshot = createPlayerSnapshot(player);
        List<String> messages = copyRecentMessages();
        return new GameSnapshot(
                currentLevel,
                turnCounter,
                playerSnapshot,
                levelSnapshot,
                messages,
                monstersDefeated,
                foodConsumed,
                elixirsConsumed,
                scrollsRead,
                hitsDealt,
                hitsTaken,
                tilesTraversed);
    }

    private PlayerSnapshot createPlayerSnapshot(Player player) {
        Map<ItemType, List<Item>> inventory = new EnumMap<>(ItemType.class);
        for (ItemType type : ItemType.values()) {
            List<Item> items = player.getInventory().viewItems(type);
            if (items.isEmpty()) {
                continue;
            }
            List<Item> copies = new ArrayList<>(items.size());
            for (Item item : items) {
                Item copy = cloneItem(item);
                if (copy != null) {
                    copies.add(copy);
                }
            }
            inventory.put(type, copies);
        }
        Item weapon = cloneItem(player.getCurrentWeapon());
        List<TemporaryBuff> buffs = player.getTemporaryBuffsCopy();
        return new PlayerSnapshot(
                player.getX(),
                player.getY(),
                player.getHealth(),
                player.getMaxHealth(),
                player.getStrength(),
                player.getLevel(),
                player.getGold(),
                player.getExperience(),
                player.getNextLevelExp(),
                player.getMaxStrength(),
                player.getMapWidth(),
                player.getMapHeight(),
                inventory,
                weapon,
                buffs);
    }

    private LevelSnapshot createLevelSnapshot() {
        if (currentMap == null || player == null) {
            return null;
        }
        char[][] mapCopy = cloneMap(currentMap);
        char[][] seenCopy = player.copySeenMap();
        List<MonsterSnapshot> monsterSnapshots = createMonsterSnapshots(monsters);
        Position position = new Position(player.getX(), player.getY());
        return new LevelSnapshot(mapCopy, monsterSnapshots, position, seenCopy);
    }

    private List<MonsterSnapshot> createMonsterSnapshots(List<Monster> monsters) {
        if (monsters == null || monsters.isEmpty()) {
            return List.of();
        }
        List<MonsterSnapshot> snapshots = new ArrayList<>();
        for (Monster monster : monsters) {
            MonsterSnapshot snapshot = createMonsterSnapshot(monster);
            if (snapshot != null) {
                snapshots.add(snapshot);
            }
        }
        return snapshots;
    }

    private MonsterSnapshot createMonsterSnapshot(Monster monster) {
        if (monster == null) {
            return null;
        }
        Map<String, Integer> extras = new HashMap<>();
        switch (monster) {
            case Ghost ghost -> {
                extras.put("phase", ghost.getPhaseCounter());
                extras.put("revealed", ghost.isRevealed() ? 1 : 0);
            }
            case Ogre ogre -> {
                extras.put("dir", ogre.getDirectionIndex());
                extras.put("rest", ogre.getRestCounter());
            }
            case SnakeMage snake -> {
                extras.put("dx", snake.getDx());
                extras.put("dy", snake.getDy());
            }
            case Vampire vampire -> extras.put("dodge", vampire.hasDodgedInitialStrike() ? 1 : 0);
            case Mimic mimic -> {
                extras.put("revealed", mimic.isRevealed() ? 1 : 0);
                extras.put("disguise", (int) mimic.getDisguiseTile());
                extras.put("base", (int) mimic.getBaseTile());
            }
            default -> {
            }
        }
        return new MonsterSnapshot(
                monster.getType(),
                monster.getX(),
                monster.getY(),
                monster.getHealth(),
                extras);
    }

    private Item cloneItem(Item item) {
        if (item == null) {
            return null;
        }
        return Item.builder(item.getType(), item.getSubtype())
                .healthDelta(item.getHealthDelta())
                .maxHealthDelta(item.getMaxHealthDelta())
                .strengthDelta(item.getStrengthDelta())
                .value(item.getValue())
                .build();
    }

    private List<String> copyRecentMessages() {
        if (messageLog == null || messageLog.isEmpty()) {
            return List.of();
        }
        int maxMessages = 40;
        int start = Math.max(0, messageLog.size() - maxMessages);
        List<String> copy = new ArrayList<>();
        for (int i = start; i < messageLog.size(); i++) {
            copy.add(messageLog.get(i));
        }
        return copy;
    }

    private char[][] cloneMap(char[][] source) {
        if (source == null) {
            return null;
        }
        char[][] copy = new char[source.length][];
        for (int i = 0; i < source.length; i++) {
            char[] row = source[i];
            copy[i] = row == null ? null : row.clone();
        }
        return copy;
    }
}
