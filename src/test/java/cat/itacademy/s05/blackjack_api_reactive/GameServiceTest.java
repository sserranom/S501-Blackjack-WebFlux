package cat.itacademy.s05.blackjack_api_reactive;

import cat.itacademy.s05.blackjack_api_reactive.domain.BlackjackGameEngine;
import cat.itacademy.s05.blackjack_api_reactive.domain.enums.GameStatus;
import cat.itacademy.s05.blackjack_api_reactive.domain.enums.PlayType;
import cat.itacademy.s05.blackjack_api_reactive.dto.PlayRequest;
import cat.itacademy.s05.blackjack_api_reactive.exception.GameNotFoundException;
import cat.itacademy.s05.blackjack_api_reactive.exception.InvalidGameStateException;
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

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private BlackjackGameEngine gameEngine;

    @InjectMocks
    private GameService gameService;

    private Player testPlayer;
    private Game testGame;

    @BeforeEach
    void setUp() {
        testPlayer = new Player("TestPlayer");
        testPlayer.setId(1L);
        testPlayer.setTotalScore(100.0);

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
    }

    @Test
    @DisplayName("should create a new game for a new player")
    void createNewGame_NewPlayer_Success() {

        String newPlayerName = "NewPlayer";
        Player newPlayer = new Player(newPlayerName);
        newPlayer.setId(2L);

        Game newGame = new Game(newPlayerName);
        newGame.setId("gameABC");
        newGame.setStatus(GameStatus.IN_PROGRESS);

        when(playerRepository.findByName(newPlayerName)).thenReturn(Mono.empty());
        when(playerRepository.save(any(Player.class))).thenReturn(Mono.just(newPlayer));
        when(gameEngine.initializeGame(any(Game.class))).thenReturn(newGame);
        when(gameRepository.save(any(Game.class))).thenReturn(Mono.just(newGame));

        StepVerifier.create(gameService.createNewGame(newPlayerName))
                .expectNextMatches(game -> game.getId().equals("gameABC") && game.getPlayerName().equals(newPlayerName) && game.getStatus() == GameStatus.IN_PROGRESS)
                .verifyComplete();

        verify(playerRepository, times(1)).findByName(newPlayerName);
        verify(playerRepository, times(1)).save(any(Player.class));
        verify(gameEngine, times(1)).initializeGame(any(Game.class));
        verify(gameRepository, times(1)).save(any(Game.class));
        verify(playerRepository, never()).save(newPlayer);
    }

    @Test
    @DisplayName("should create a new game for an existing player")
    void createNewGame_ExistingPlayer_Success() {
        // Given
        String existingPlayerName = testPlayer.getName();
        Game newGame = new Game(existingPlayerName);
        newGame.setId("gameXYZ");
        newGame.setStatus(GameStatus.IN_PROGRESS);

        when(playerRepository.findByName(existingPlayerName)).thenReturn(Mono.just(testPlayer));
        when(gameEngine.initializeGame(any(Game.class))).thenReturn(newGame);
        when(gameRepository.save(any(Game.class))).thenReturn(Mono.just(newGame));

        StepVerifier.create(gameService.createNewGame(existingPlayerName))
                .expectNextMatches(game -> game.getId().equals("gameXYZ") && game.getPlayerName().equals(existingPlayerName) && game.getStatus() == GameStatus.IN_PROGRESS)
                .verifyComplete();

        verify(playerRepository, times(1)).findByName(existingPlayerName);
        verify(playerRepository, never()).save(any(Player.class)); // No se debe crear un nuevo jugador
        verify(gameEngine, times(1)).initializeGame(any(Game.class));
        verify(gameRepository, times(1)).save(any(Game.class));
    }

    @Test
    @DisplayName("should retrieve game details successfully")
    void getGameDetails_GameFound_Success() {

        when(gameRepository.findById(testGame.getId())).thenReturn(Mono.just(testGame));

        StepVerifier.create(gameService.getGameDetails(testGame.getId()))
                .expectNextMatches(details -> details.getId().equals(testGame.getId()))
                .verifyComplete();
        verify(gameRepository, times(1)).findById(testGame.getId());
    }

    @Test
    @DisplayName("should throw GameNotFoundException when game not found for details")
    void getGameDetails_GameNotFound_ThrowsException() {
        when(gameRepository.findById(anyString())).thenReturn(Mono.empty());

        StepVerifier.create(gameService.getGameDetails("nonExistentId"))
                .expectErrorMatches(throwable -> throwable instanceof GameNotFoundException &&
                        throwable.getMessage().equals("Game not found with ID: nonExistentId"))
                .verify();
        verify(gameRepository, times(1)).findById("nonExistentId");
    }

    @Test
    @DisplayName("should successfully process a player's valid play")
    void play_ValidPlay_Success() {

        PlayRequest playRequest = new PlayRequest(PlayType.HIT, 10.0);
        Game gameAfterPlay = new Game(testGame.getPlayerName());
        gameAfterPlay.setId(testGame.getId());
        gameAfterPlay.setStatus(GameStatus.IN_PROGRESS);
        gameAfterPlay.setPlayerScore(0.0);

        when(gameRepository.findById(testGame.getId())).thenReturn(Mono.just(testGame));
        when(gameEngine.processPlayerMove(any(Game.class), any(PlayType.class), any(Double.class)))
                .thenReturn(gameAfterPlay);
        when(gameRepository.save(any(Game.class))).thenReturn(Mono.just(gameAfterPlay));

        StepVerifier.create(gameService.play(testGame.getId(), playRequest))
                .expectNextMatches(details -> details.getId().equals(testGame.getId()) && details.getStatus() == GameStatus.IN_PROGRESS)
                .verifyComplete();

        verify(gameRepository, times(1)).findById(testGame.getId());
        verify(gameEngine, times(1)).processPlayerMove(testGame, playRequest.getPlayType(), playRequest.getBetAmount());
        verify(gameRepository, times(1)).save(any(Game.class));
        verify(playerRepository, never()).findByName(anyString()); // No se llama updatePlayerScore si sigue IN_PROGRESS
    }

    @Test
    @DisplayName("should throw InvalidGameStateException when playing on a finished game")
    void play_GameFinished_ThrowsException() {

        testGame.setStatus(GameStatus.DEALER_WINS);
        PlayRequest playRequest = new PlayRequest(PlayType.HIT, 10.0);
        when(gameRepository.findById(testGame.getId())).thenReturn(Mono.just(testGame));

        StepVerifier.create(gameService.play(testGame.getId(), playRequest))
                .expectErrorMatches(throwable -> throwable instanceof InvalidGameStateException &&
                        throwable.getMessage().contains("The game is already over or not in progress"))
                .verify();
        verify(gameRepository, times(1)).findById(testGame.getId());
        verify(gameEngine, never()).processPlayerMove(any(), any(), anyDouble());
        verify(gameRepository, never()).save(any(Game.class));
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when game engine indicates invalid play rule")
    void play_InvalidPlayRule_ThrowsException() {

        PlayRequest playRequest = new PlayRequest(PlayType.DOUBLE_DOWN, 10.0);
        testGame.setPlayerHand(Collections.singletonList(new cat.itacademy.s05.blackjack_api_reactive.domain.Card(cat.itacademy.s05.blackjack_api_reactive.domain.enums.Rank.ACE, cat.itacademy.s05.blackjack_api_reactive.domain.enums.Suit.SPADES)));

        when(gameRepository.findById(testGame.getId())).thenReturn(Mono.just(testGame));
        when(gameEngine.processPlayerMove(any(Game.class), any(PlayType.class), any(Double.class)))
                .thenThrow(new IllegalArgumentException("Double Down is only allowed on the first turn."));

        StepVerifier.create(gameService.play(testGame.getId(), playRequest))
                .expectErrorMatches(throwable -> throwable instanceof org.springframework.web.server.ResponseStatusException &&
                        ((org.springframework.web.server.ResponseStatusException) throwable).getStatusCode().value() == 400 &&
                        throwable.getMessage().contains("Double Down is only allowed"))
                .verify();

        verify(gameRepository, times(1)).findById(testGame.getId());
        verify(gameEngine, times(1)).processPlayerMove(testGame, playRequest.getPlayType(), playRequest.getBetAmount());
        verify(gameRepository, never()).save(any(Game.class));
    }

    @Test
    @DisplayName("should delete game successfully")
    void deleteGame_GameFound_Success() {
        when(gameRepository.findById(testGame.getId())).thenReturn(Mono.just(testGame));
        when(gameRepository.delete(testGame)).thenReturn(Mono.empty()); // delete() devuelve Mono<Void>

        StepVerifier.create(gameService.deleteGame(testGame.getId()))
                .verifyComplete();

        verify(gameRepository, times(1)).findById(testGame.getId());
        verify(gameRepository, times(1)).delete(testGame);
    }

    @Test
    @DisplayName("should throw GameNotFoundException when deleting non-existent game")
    void deleteGame_GameNotFound_ThrowsException() {

        when(gameRepository.findById(anyString())).thenReturn(Mono.empty());

        StepVerifier.create(gameService.deleteGame("nonExistentId"))
                .expectErrorMatches(throwable -> throwable instanceof GameNotFoundException &&
                        throwable.getMessage().equals("Game not found with ID: nonExistentId"))
                .verify();

        verify(gameRepository, times(1)).findById("nonExistentId");
        verify(gameRepository, never()).delete(any(Game.class));
    }

    @Test
    @DisplayName("should create game and update player score if player gets initial Blackjack")
    void createNewGame_PlayerBlackjack_ScoreUpdated() {
        String playerName = testPlayer.getName();
        Game gameWithPlayerBlackjack = new Game(playerName);
        gameWithPlayerBlackjack.setId("gameBJ");
        gameWithPlayerBlackjack.setStatus(GameStatus.PLAYER_BLACKJACK);
        gameWithPlayerBlackjack.setPlayerScore(1.5);

        when(playerRepository.findByName(playerName)).thenReturn(Mono.just(testPlayer));
        when(gameEngine.initializeGame(any(Game.class))).thenReturn(gameWithPlayerBlackjack);
        when(gameRepository.save(any(Game.class))).thenReturn(Mono.just(gameWithPlayerBlackjack));
        when(playerRepository.save(any(Player.class))).thenReturn(Mono.just(testPlayer));

        StepVerifier.create(gameService.createNewGame(playerName))
                .expectNextMatches(game -> game.getId().equals("gameBJ") && game.getStatus() == GameStatus.PLAYER_BLACKJACK)
                .verifyComplete();

        verify(playerRepository, times(1)).findByName(playerName);
        verify(gameEngine, times(1)).initializeGame(any(Game.class));
        verify(gameRepository, times(1)).save(any(Game.class));
        verify(playerRepository, times(1)).save(testPlayer);

    }

    @Test
    @DisplayName("should create game and update player score if dealer gets initial Blackjack")
    void createNewGame_DealerBlackjack_ScoreUpdated() {

        String playerName = testPlayer.getName();
        Game gameWithDealerBlackjack = new Game(playerName);
        gameWithDealerBlackjack.setId("gameDBJ");
        gameWithDealerBlackjack.setStatus(GameStatus.DEALER_WINS);
        gameWithDealerBlackjack.setPlayerScore(-1.0);

        when(playerRepository.findByName(playerName)).thenReturn(Mono.just(testPlayer));
        when(gameEngine.initializeGame(any(Game.class))).thenReturn(gameWithDealerBlackjack);
        when(gameRepository.save(any(Game.class))).thenReturn(Mono.just(gameWithDealerBlackjack));
        when(playerRepository.save(any(Player.class))).thenReturn(Mono.just(testPlayer));

        StepVerifier.create(gameService.createNewGame(playerName))
                .expectNextMatches(game -> game.getId().equals("gameDBJ") && game.getStatus() == GameStatus.DEALER_WINS)
                .verifyComplete();

        verify(playerRepository, times(1)).findByName(playerName);
        verify(gameEngine, times(1)).initializeGame(any(Game.class));
        verify(gameRepository, times(1)).save(any(Game.class));
        verify(playerRepository, times(1)).save(testPlayer);
    }
}
