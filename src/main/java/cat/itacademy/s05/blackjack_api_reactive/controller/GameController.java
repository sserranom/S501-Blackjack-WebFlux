package cat.itacademy.s05.blackjack_api_reactive.controller;

import cat.itacademy.s05.blackjack_api_reactive.dto.GameDetailsResponse;
import cat.itacademy.s05.blackjack_api_reactive.dto.NewGameRequest;
import cat.itacademy.s05.blackjack_api_reactive.dto.PlayRequest;
import cat.itacademy.s05.blackjack_api_reactive.model.Game;
import cat.itacademy.s05.blackjack_api_reactive.services.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/game")
@Tag(name = "Game Management", description = "Endpoints for managing Blackjack games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/new")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new Blackjack game",
            description = "Start a new game with one player and deal the starting cards.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Game created successfully",
                    content = @Content(schema = @Schema(implementation = Game.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public Mono<Game> createNewGame(
            @Valid @RequestBody NewGameRequest request
    ) {
        return gameService.createNewGame(request.getPlayerName());
    }

    @GetMapping("/{id}")
    @Operation(summary = "\n" +
            "Get the details of a game",
            description = "Retrieves complete information for a specific Blackjack game by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "\n" +
                    "Match details found",
                    content = @Content(schema = @Schema(implementation = GameDetailsResponse.class))),
            @ApiResponse(responseCode = "404", description = "\n" +
                    "Item not found")
    })
    public Mono<GameDetailsResponse> getGameDetails(
            @Parameter(description =
                    "Unique game identifier")
            @PathVariable String id
    ) {
        return gameService.getGameDetails(id);
    }

    @PostMapping("/{id}/play")
    @Operation(summary = "\n" +
            "Make a move in a game",
            description = "\n" +
                    "Allows the player to perform an action (HIT, STAND, DOUBLE_DOWN, SURRENDER) in an existing game.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "\n" +
                    "Play successfully completed",
                    content = @Content(schema = @Schema(implementation = GameDetailsResponse.class))),
            @ApiResponse(responseCode = "400", description = "\n" +
                    "Invalid request or item already completed"),
            @ApiResponse(responseCode = "404", description = "\n" +
                    "Item not found")
    })
    public Mono<GameDetailsResponse> play(
            @Parameter(description = "Unique item identifier")
            @PathVariable String id,
            @Valid @RequestBody PlayRequest request
    ) {
        return gameService.play(id, request);
    }

    @DeleteMapping("/{id}/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "\n" +
            "Delete a game",
            description = "\n" +
                    "Permanently deletes a Blackjack game by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Game successfully deleted"),
            @ApiResponse(responseCode = "404", description = "\n" +
                    "Item not found")
    })
    public Mono<Void> deleteGame(
            @Parameter(description = "\n" +
                    "Unique item identifier")
            @PathVariable String id
    ) {
        return gameService.deleteGame(id);
    }
}

