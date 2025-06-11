package cat.itacademy.s05.blackjack_api_reactive;

import cat.itacademy.s05.blackjack_api_reactive.dto.PlayRequest;
import cat.itacademy.s05.blackjack_api_reactive.model.Game;
import cat.itacademy.s05.blackjack_api_reactive.model.Player;
import cat.itacademy.s05.blackjack_api_reactive.repository.GameRepository;
import cat.itacademy.s05.blackjack_api_reactive.repository.PlayerRepository;
import cat.itacademy.s05.blackjack_api_reactive.services.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private GameService gameService;

    private Game testGame;
    private Player testPlayer;

    @BeforeEach
    void setUp() {
        testGame = new Game("testPlayer");
        testGame.setId("game123");
        testGame.setPlayerHand("AS, 10");
        testGame.setPlayerHandValue(21);
        testGame.setDealerHand("7");
        testGame.setDealerHandValue(7);
        testGame.setStatus("IN_PROGRESS");

        testPlayer = new Player("testPlayer");
        testPlayer.setId(1L);
        testPlayer.setTotalScore(100.0);
        testPlayer.setGamesPlayed(5);
        testPlayer.setGamesWon(3);
    }

    @Test
    @DisplayName("You should create a new game and register a new player if one does not exist.")
    void shouldCreateNewGameAndRegisterNewPlayer() {
        when(playerRepository.findByName(any(String.class))).thenReturn(Mono.empty());
        when(playerRepository.save(any(Player.class))).thenReturn(Mono.just(testPlayer));
        when(gameRepository.save(any(Game.class))).thenReturn(Mono.just(testGame));

        Mono<Game> result = gameService.createNewGame("newPlayer");

        StepVerifier.create(result)
                .expectNextMatches(game -> game.getPlayerName().equals("newPlayer") && game.getStatus().equals("IN_PROGRESS"))
                .verifyComplete();

        verify(playerRepository, times(1)).findByName("newPlayer");
        verify(playerRepository, times(1)).save(any(Player.class));
        verify(gameRepository, times(1)).save(any(Game.class));
    }

    @Test
    @DisplayName("You should create a new game for an existing player.")
    void shouldCreateNewGameForExistingPlayer() {
        when(playerRepository.findByName(any(String.class))).thenReturn(Mono.just(testPlayer));
        when(gameRepository.save(any(Game.class))).thenReturn(Mono.just(testGame));
        when(playerRepository.save(any(Player.class))).thenReturn(Mono.just(testPlayer));

        Mono<Game> result = gameService.createNewGame("testPlayer");

        StepVerifier.create(result)
                .expectNextMatches(game -> game.getPlayerName().equals("testPlayer"))
                .verifyComplete();

        verify(playerRepository, times(1)).findByName("testPlayer");
        verify(gameRepository, times(1)).save(any(Game.class));
        verify(playerRepository, times(1)).save(any(Player.class));
    }

    @Test
    @DisplayName("\n" +
            "You should get the details of an existing game")
    void shouldGetGameDetails() {
        when(gameRepository.findById("gameTest")).thenReturn(Mono.just(testGame));

        StepVerifier.create(gameService.getGameDetails("gameTest"))
                .expectNextMatches(details -> details.getId().equals("gameTest") && details.getPlayerName().equals("testPlayer"))
                .verifyComplete();
    }

    @Test
    @DisplayName("\n" +
            "Should throw exception if game not found when getting details")
    void shouldThrowExceptionWhenGameDetailsNotFound() {
        when(gameRepository.findById("nonExistentId")).thenReturn(Mono.empty());

        StepVerifier.create(gameService.getGameDetails("nonExistentId"))
                .expectErrorMatches(throwable -> throwable.getMessage().contains("\n" +
                        "Game not found"))
                .verify();
    }

    @Test
    @DisplayName("\n" +
            "Should handle the 'HIT' play correctly")
    void shouldHandleHitPlay() {
        testGame.setPlayerHand("7, 8");
        testGame.setPlayerHandValue(15);
        testGame.setDealerHand("K");
        testGame.setDealerHandValue(10);

        when(gameRepository.findById("gameTest")).thenReturn(Mono.just(testGame));
        when(gameRepository.save(any(Game.class))).thenReturn(Mono.just(testGame));
        when(playerRepository.findByName(any(String.class))).thenReturn(Mono.just(testPlayer));
        when(playerRepository.save(any(Player.class))).thenReturn(Mono.just(testPlayer));

        PlayRequest hitRequest = new PlayRequest("HIT", 10.0);

        StepVerifier.create(gameService.play("gameTest", hitRequest))
                .expectNextMatches(gameDetails -> gameDetails.getPlayerHand().contains("7, 8, "))
                .verifyComplete();

        verify(gameRepository, times(1)).findById("game123");
        verify(gameRepository, times(1)).save(any(Game.class));
        verify(playerRepository, times(1)).findByName(any(String.class));
        verify(playerRepository, times(1)).save(any(Player.class));
    }

    @Test
    @DisplayName("Should handle the 'STAND' play correctly")
    void shouldHandleStandPlay() {
        testGame.setPlayerHand("10, 10");
        testGame.setPlayerHandValue(20);
        testGame.setDealerHand("7");
        testGame.setDealerHandValue(7);

        when(gameRepository.findById("gameTest")).thenReturn(Mono.just(testGame));
        when(gameRepository.save(any(Game.class))).thenReturn(Mono.just(testGame));
        when(playerRepository.findByName(any(String.class))).thenReturn(Mono.just(testPlayer));
        when(playerRepository.save(any(Player.class))).thenReturn(Mono.just(testPlayer));

        PlayRequest standRequest = new PlayRequest("STAND", 10.0);

        StepVerifier.create(gameService.play("gameTest", standRequest))
                .expectNextMatches(gameDetails -> {
                    return !gameDetails.getStatus().equals("IN_PROGRESS") && gameDetails.getDealerHand().contains(", ");
                })
                .verifyComplete();

        verify(gameRepository, times(1)).findById("gameTest");
        verify(gameRepository, times(1)).save(any(Game.class));
        verify(playerRepository, times(1)).findByName(any(String.class));
        verify(playerRepository, times(1)).save(any(Player.class));
    }
    
    @Test
    @DisplayName("\n" +
            "I should delete an existing game")
    void shouldDeleteGame() {
        when(gameRepository.findById("gameTest")).thenReturn(Mono.just(testGame));
        when(gameRepository.delete(testGame)).thenReturn(Mono.empty());

        StepVerifier.create(gameService.deleteGame("gameTest"))
                .verifyComplete();

        verify(gameRepository, times(1)).findById("gameTest");
        verify(gameRepository, times(1)).delete(testGame);
    }

    @Test
    @DisplayName("Should throw exception if game not found when deleting")
    void shouldThrowExceptionWhenGameToDeleteNotFound() {
        when(gameRepository.findById("nonExistentId")).thenReturn(Mono.empty());

        StepVerifier.create(gameService.deleteGame("nonExistentId"))
                .expectErrorMatches(throwable -> throwable.getMessage().contains("Game not found"))
                .verify();

        verify(gameRepository, times(1)).findById("nonExistentId");
        verify(gameRepository, times(0)).delete(any(Game.class));
    }
}
