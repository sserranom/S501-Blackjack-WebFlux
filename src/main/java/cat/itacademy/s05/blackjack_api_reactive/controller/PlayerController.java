package cat.itacademy.s05.blackjack_api_reactive.controller;

import cat.itacademy.s05.blackjack_api_reactive.dto.PlayerNameUpdateRequest;
import cat.itacademy.s05.blackjack_api_reactive.model.Player;
import cat.itacademy.s05.blackjack_api_reactive.services.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Tag(name = "Player Management", description = "\n" +
        "Endpoints for player management and ranking")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping("/ranking")
    @Operation(summary = "\n" +
            "Get the player ranking",
            description = "Retrieves a list of players sorted by their total score.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "\n" +
                    "Player ranking successfully obtained",
                    content = @Content(schema = @Schema(implementation = Player.class)))
    })
    public Flux<Player> getPlayerRanking() {
        return playerService.getPlayerRanking();
    }

    @PutMapping("/player/{playerId}")
    @Operation(summary = "\n" +
            "Change a player's name",
            description = "\n" +
                    "Updates an existing player's name by their ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "\n" +
                    "Player name updated successfully",
                    content = @Content(schema = @Schema(implementation = Player.class))),
            @ApiResponse(responseCode = "400", description = "\n" +
                    "Invalid request"),
            @ApiResponse(responseCode = "404", description =
                    "Player not found")
    })
    public Mono<Player> updatePlayerName(
            @Parameter(description =
                    "Unique player identifier")
            @PathVariable Long playerId,
            @Valid @RequestBody PlayerNameUpdateRequest request
    ) {
        return playerService.updatePlayerName(playerId, request);
    }
}
