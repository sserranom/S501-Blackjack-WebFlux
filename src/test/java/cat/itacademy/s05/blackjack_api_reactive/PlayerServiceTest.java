package cat.itacademy.s05.blackjack_api_reactive;

import cat.itacademy.s05.blackjack_api_reactive.dto.PlayerNameUpdateRequest;
import cat.itacademy.s05.blackjack_api_reactive.model.Player;
import cat.itacademy.s05.blackjack_api_reactive.repository.PlayerRepository;
import cat.itacademy.s05.blackjack_api_reactive.services.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PlayerServiceTest {
    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private PlayerService playerService;

    private Player testPlayer;

    @BeforeEach
    void setUp() {
        testPlayer = new Player("ExistingPlayer");
        testPlayer.setId(1L);
        testPlayer.setTotalScore(100.0);
        testPlayer.setGamesPlayed(5);
        testPlayer.setGamesWon(3);
    }

    @Test
    @DisplayName("should create a new player when name does not exist")
    void createNewPlayer_NameDoesNotExist_CreatesPlayer() {
        // Given
        String newPlayerName = "NewPlayer";
        Player savedPlayer = new Player(newPlayerName);
        savedPlayer.setId(2L);

        when(playerRepository.findByName(newPlayerName)).thenReturn(Mono.empty());
        when(playerRepository.save(any(Player.class))).thenReturn(Mono.just(savedPlayer));

        StepVerifier.create(playerService.createNewPlayer(newPlayerName))
                .expectNextMatches(player -> player.getName().equals(newPlayerName) && player.getId() == 2L)
                .verifyComplete();

        verify(playerRepository, times(1)).findByName(newPlayerName);
        verify(playerRepository, times(1)).save(any(Player.class));
    }

    @Test
    @DisplayName("should throw CONFLICT error when creating a player with existing name")
    void createNewPlayer_NameExists_ThrowsConflict() {

        String existingPlayerName = testPlayer.getName();

        when(playerRepository.findByName(existingPlayerName)).thenReturn(Mono.just(testPlayer));

        StepVerifier.create(playerService.createNewPlayer(existingPlayerName))
                .expectErrorMatches(throwable ->
                        throwable instanceof ResponseStatusException &&
                                ((ResponseStatusException) throwable).getStatusCode().value() == 409 && // HttpStatus.CONFLICT
                                throwable.getMessage().contains("There is already a player with the name: " + existingPlayerName))
                .verify();

        verify(playerRepository, times(1)).findByName(existingPlayerName);
        verify(playerRepository, never()).save(any(Player.class));
    }

    @Test
    @DisplayName("should return player ranking ordered by total score descending")
    void getPlayerRanking_ReturnsOrderedList() {

        Player player1 = new Player("PlayerA"); player1.setId(1L); player1.setTotalScore(200.0);
        Player player2 = new Player("PlayerB"); player2.setId(2L); player2.setTotalScore(150.0);
        Player player3 = new Player("PlayerC"); player3.setId(3L); player3.setTotalScore(250.0);

        when(playerRepository.findAllByOrderByTotalScoreDesc())
                .thenReturn(Flux.just(player3, player1, player2));

        StepVerifier.create(playerService.getPlayerRanking())
                .expectNext(player3)
                .expectNext(player1)
                .expectNext(player2)
                .verifyComplete();

        verify(playerRepository, times(1)).findAllByOrderByTotalScoreDesc();
    }

    @Test
    @DisplayName("should update player name successfully when player exists")
    void updatePlayerName_PlayerExists_UpdatesName() {
        Long playerId = testPlayer.getId();
        PlayerNameUpdateRequest request = new PlayerNameUpdateRequest("UpdatedName");

        when(playerRepository.findById(playerId)).thenReturn(Mono.just(testPlayer));
        when(playerRepository.save(any(Player.class))).thenReturn(Mono.just(new Player(playerId, "UpdatedName", testPlayer.getTotalScore(), testPlayer.getGamesPlayed(), testPlayer.getGamesWon())));

        StepVerifier.create(playerService.updatePlayerName(playerId, request))
                .expectNextMatches(player -> player.getId() == playerId && player.getName().equals("UpdatedName"))
                .verifyComplete();

        // Then
        verify(playerRepository, times(1)).findById(playerId);
        verify(playerRepository, times(1)).save(testPlayer);
    }

    @Test
    @DisplayName("should throw NOT_FOUND error when updating name of non-existent player")
    void updatePlayerName_PlayerDoesNotExist_ThrowsNotFound() {

        Long nonExistentPlayerId = 99L;
        PlayerNameUpdateRequest request = new PlayerNameUpdateRequest("UpdatedName");

        when(playerRepository.findById(nonExistentPlayerId)).thenReturn(Mono.empty());

        StepVerifier.create(playerService.updatePlayerName(nonExistentPlayerId, request))
                .expectErrorMatches(throwable ->
                        throwable instanceof ResponseStatusException &&
                                ((ResponseStatusException) throwable).getStatusCode().value() == 404 &&
                                throwable.getMessage().contains("Player not found with ID: " + nonExistentPlayerId))
                .verify();
        verify(playerRepository, times(1)).findById(nonExistentPlayerId);
        verify(playerRepository, never()).save(any(Player.class));
    }
}
