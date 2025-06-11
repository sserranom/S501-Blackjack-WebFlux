package cat.itacademy.s05.blackjack_api_reactive;

import cat.itacademy.s05.blackjack_api_reactive.controller.GameController;
import cat.itacademy.s05.blackjack_api_reactive.dto.GameDetailsResponse;
import cat.itacademy.s05.blackjack_api_reactive.dto.NewGameRequest;
import cat.itacademy.s05.blackjack_api_reactive.dto.PlayRequest;
import cat.itacademy.s05.blackjack_api_reactive.model.Game;
import cat.itacademy.s05.blackjack_api_reactive.services.GameService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;


@WebFluxTest(GameController.class)
public class GameControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private GameService gameService;

    @TestConfiguration
    static class GameControllerTestConfig {
        @Bean
        public GameService gameService() {

            return Mockito.mock(GameService.class);
        }
    }

    @Test
    @DisplayName("POST /game/new should create a new game")
    void createNewGame_shouldReturnCreatedGame() {
        Game mockGame = new Game("TestPlayer");
        mockGame.setId("abc-123");
        mockGame.setStatus("IN_PROGRESS");

        when(gameService.createNewGame(any(String.class))).thenReturn(Mono.just(mockGame));

        NewGameRequest request = new NewGameRequest("TestPlayer");

        webTestClient.post().uri("/game/new")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Game.class)
                .isEqualTo(mockGame);

        verify(gameService, times(1)).createNewGame("TestPlayer");
    }

    @Test
    @DisplayName("\n" +
            "POST /game/new should return 400 if the player name is empty")
    void createNewGame_shouldReturnBadRequestWhenPlayerNameIsEmpty() {
        NewGameRequest request = new NewGameRequest("");

        webTestClient.post().uri("/game/new")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("\n" +
            "GET /game/{id} should get the details of a game")
    void getGameDetails_shouldReturnGameDetails() {
        GameDetailsResponse mockResponse = new GameDetailsResponse();
        mockResponse.setId("game456");
        mockResponse.setPlayerName("ExistingPlayer");
        mockResponse.setStatus("IN_PROGRESS");


        when(gameService.getGameDetails("game456")).thenReturn(Mono.just(mockResponse));

        webTestClient.get().uri("/game/{id}", "game456")
                .exchange()
                .expectStatus().isOk()
                .expectBody(GameDetailsResponse.class)
                .isEqualTo(mockResponse);

        verify(gameService, times(1)).getGameDetails("game456");
    }

    @Test
    @DisplayName("GET /game/{id} should return 404 if the game is not found")
    void getGameDetails_shouldReturnNotFoundWhenGameNotFound() {
        when(gameService.getGameDetails("nonExistentId")).thenReturn(Mono.empty());

        webTestClient.get().uri("/game/{id}", "nonExistentId")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("\n" +
            "POST /game/{id}/play should perform a play and return the updated state")
    void play_shouldReturnUpdatedGameDetails() {
        GameDetailsResponse mockResponse = new GameDetailsResponse();
        mockResponse.setId("game789");
        mockResponse.setPlayerName("PlayingPlayer");
        mockResponse.setStatus("PLAYER_WINS");

        PlayRequest request = new PlayRequest("STAND", 10.0);

        when(gameService.play(any(String.class), any(PlayRequest.class))).thenReturn(Mono.just(mockResponse));

        webTestClient.post().uri("/game/{id}/play", "game789")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk() // Espera un código de estado 200 OK
                .expectBody(GameDetailsResponse.class)
                .isEqualTo(mockResponse);

        verify(gameService, times(1)).play("game789", request);
    }

    @Test
    @DisplayName("\n" +
            "DELETE /game/{id}/delete should delete a game")
    void deleteGame_shouldReturnNoContent() {
        when(gameService.deleteGame(any(String.class))).thenReturn(Mono.empty());

        webTestClient.delete().uri("/game/{id}/delete", "game999")
                .exchange()
                .expectStatus().isNoContent();

        verify(gameService, times(1)).deleteGame("game999");
    }
}
