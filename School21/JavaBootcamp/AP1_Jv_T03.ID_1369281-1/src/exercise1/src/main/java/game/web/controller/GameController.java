package game.web.controller;

import game.domain.model.Game;
import game.domain.service.GameService;
import game.web.model.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import game.web.mapper.WebGameMapper;
import game.web.model.GameWeb;

import java.util.UUID;

@RestController
@RequestMapping("/game")
public class GameController {
    private final GameService gameService;
    private final WebGameMapper mapper = new WebGameMapper();

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping
    public ResponseEntity<GameWeb> startNewGame() {
        Game newGame = gameService.createNewGame();
        return ResponseEntity.ok(mapper.toWeb(newGame));
    }

    @PostMapping
    public ResponseEntity<?> makeMove(@RequestBody GameWeb userMove) {
        try {
            UUID id = userMove.getId();
            if (id == null) {
                return ResponseEntity.badRequest().body(new ErrorResponse("ID игры не указан"));
            }
            userMove.setId(id);
            Game domainRequest = mapper.toDomain(userMove);
            Game resultGame = gameService.processMove(domainRequest);

            return ResponseEntity.ok(mapper.toWeb(resultGame));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
}
