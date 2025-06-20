package cat.itacademy.s05.blackjack_api_reactive;

import cat.itacademy.s05.blackjack_api_reactive.controller.GameController;
import cat.itacademy.s05.blackjack_api_reactive.domain.enums.GameStatus;
import cat.itacademy.s05.blackjack_api_reactive.domain.enums.PlayType;
import cat.itacademy.s05.blackjack_api_reactive.dto.GameDetailsResponse;
import cat.itacademy.s05.blackjack_api_reactive.dto.NewGameRequest;
import cat.itacademy.s05.blackjack_api_reactive.dto.PlayRequest;
import cat.itacademy.s05.blackjack_api_reactive.exception.GameNotFoundException;
import cat.itacademy.s05.blackjack_api_reactive.exception.GlobalExceptionHandler;
import cat.itacademy.s05.blackjack_api_reactive.exception.InvalidGameStateException;
import cat.itacademy.s05.blackjack_api_reactive.model.Game;
import cat.itacademy.s05.blackjack_api_reactive.services.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;

@WebFluxTest(controllers = GameController.class)
@Import(GlobalExceptionHandler.class)
public class GameControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private GameService gameService;

    private Game testGame;
    private GameDetailsResponse testGameDetailsResponse;

    @BeforeEach
    void setUp() {
        testGame = new Game("TestPlayer");
        testGame.setId("game123");
        testGame.setPlayerHand(Collections.emptyList());
        testGame.setDealerHand(Collections.emptyList());
        testGame.setPlayerHandValue(0);
        testGame.setDealerHandValue(0);
        testGame.setStatus(GameStatus.IN_PROGRESS);
        testGame.setPlayerScore(0.0);
        testGame.setCreatedAt(LocalDateTime.now());
        testGame.setUpdatedAt(LocalDateTime.now());

        testGameDetailsResponse = new GameDetailsResponse(testGame);
    }

    @Test
    @DisplayName("should create a new game and return 201 Created")
    void createNewGame_ValidRequest_Returns201() {
        // Given
        NewGameRequest request = new NewGameRequest("NewPlayer");
        Game createdGame = new Game("NewPlayer");
        createdGame.setId("newGameId");
        createdGame.setStatus(GameStatus.IN_PROGRESS);

        when(gameService.createNewGame(anyString())).thenReturn(Mono.just(createdGame));

        // When & Then
        webTestClient.post().uri("/game/new")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Game.class)
                .consumeWith(response -> {
                    Game responseBody = response.getResponseBody();
                    assert responseBody != null;
                    assert responseBody.getId().equals("newGameId");
                    assert responseBody.getPlayerName().equals("NewPlayer");
                });

        verify(gameService, times(1)).createNewGame("NewPlayer");
    }

    @Test
    @DisplayName("should return 400 Bad Request if player name is blank when creating new game")
    void createNewGame_BlankPlayerName_Returns400() {
        NewGameRequest request = new NewGameRequest("");

        webTestClient.post().uri("/game/new")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.error").isEqualTo("Bad Request")
                .jsonPath("$.message").isEqualTo("Validation Failed");

        verify(gameService, never()).createNewGame(anyString());
    }

    @Test
    @DisplayName("should retrieve game details and return 200 OK")
    void getGameDetails_GameFound_Returns200() {
        // Given
        when(gameService.getGameDetails(testGame.getId())).thenReturn(Mono.just(testGameDetailsResponse));

        // When & Then
        webTestClient.get().uri("/game/{id}", testGame.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(GameDetailsResponse.class)
                .consumeWith(response -> {
                    GameDetailsResponse responseBody = response.getResponseBody();
                    assert responseBody != null;
                    assert responseBody.getId().equals(testGame.getId());
                    assert responseBody.getPlayerName().equals(testGame.getPlayerName());
                });

        verify(gameService, times(1)).getGameDetails(testGame.getId());
    }

    @Test
    @DisplayName("should return 404 Not Found if game does not exist for details")
    void getGameDetails_GameNotFound_Returns404() {
        // Given
        when(gameService.getGameDetails(anyString())).thenReturn(Mono.error(new GameNotFoundException("Game not found")));

        // When & Then
        webTestClient.get().uri("/game/nonExistentId")
                .exchange()
                .expectStatus().isNotFound() // Verifica el código de estado HTTP 404
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.error").isEqualTo("Not Found")
                .jsonPath("$.message").isEqualTo("Game not found");

        verify(gameService, times(1)).getGameDetails("nonExistentId");
    }

    @Test
    @DisplayName("should process a player's play and return 200 OK")
    void play_ValidPlay_Returns200() {
        // Given
        PlayRequest request = new PlayRequest(PlayType.HIT, 10.0);
        GameDetailsResponse responseAfterPlay = new GameDetailsResponse(testGame);
        responseAfterPlay.setStatus(GameStatus.IN_PROGRESS);

        when(gameService.play(testGame.getId(), request)).thenReturn(Mono.just(responseAfterPlay));

        // When & Then
        webTestClient.post().uri("/game/{id}/play", testGame.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(GameDetailsResponse.class)
                .consumeWith(response -> {
                    GameDetailsResponse responseBody = response.getResponseBody();
                    assert responseBody != null;
                    assert responseBody.getId().equals(testGame.getId());
                    assert responseBody.getStatus() == GameStatus.IN_PROGRESS;
                });

        verify(gameService, times(1)).play(testGame.getId(), request);
    }

    @Test
    @DisplayName("should return 400 Bad Request if play is invalid (e.g., game already over)")
    void play_InvalidGameState_Returns400() {
        // Given
        PlayRequest request = new PlayRequest(PlayType.STAND, 10.0);
        when(gameService.play(testGame.getId(), request)).thenReturn(Mono.error(new InvalidGameStateException("The game is already over or not in progress. Current status: DEALER_WINS")));

        // When & Then
        webTestClient.post().uri("/game/{id}/play", testGame.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.message").isEqualTo("The game is already over or not in progress. Current status: DEALER_WINS");

        verify(gameService, times(1)).play(testGame.getId(), request);
    }

    @Test
    @DisplayName("should return 400 Bad Request if game service indicates invalid play rule")
    void play_InvalidPlayRule_Returns400() {

        PlayRequest request = new PlayRequest(PlayType.DOUBLE_DOWN, 10.0);
        when(gameService.play(testGame.getId(), request)).thenReturn(Mono.error(new IllegalArgumentException("Double Down is only allowed on the first turn (player has 2 cards).")));

        webTestClient.post().uri("/game/{id}/play", testGame.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.error").isEqualTo("Bad Request")
                .jsonPath("$.message").isEqualTo("Double Down is only allowed on the first turn (player has 2 cards).");

        verify(gameService, times(1)).play(testGame.getId(), request);
    }

    @Test
    @DisplayName("should return 204 No Content when deleting a game successfully")
    void deleteGame_GameFound_Returns204() {
        when(gameService.deleteGame(testGame.getId())).thenReturn(Mono.empty());
        webTestClient.delete().uri("/game/{id}/delete", testGame.getId())
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        verify(gameService, times(1)).deleteGame(testGame.getId());
    }

    @Test
    @DisplayName("should return 404 Not Found when deleting a non-existent game")
    void deleteGame_GameNotFound_Returns404() {
        // Given
        when(gameService.deleteGame(anyString())).thenReturn(Mono.error(new GameNotFoundException("Game to delete not found")));

        // When & Then
        webTestClient.delete().uri("/game/nonExistentId/delete")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").isEqualTo("Game to delete not found");

        verify(gameService, times(1)).deleteGame("nonExistentId");
    }

}
