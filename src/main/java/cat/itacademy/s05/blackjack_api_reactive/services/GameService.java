package cat.itacademy.s05.blackjack_api_reactive.services;

import cat.itacademy.s05.blackjack_api_reactive.domain.BlackjackGameEngine;
import cat.itacademy.s05.blackjack_api_reactive.domain.enums.GameStatus;
import cat.itacademy.s05.blackjack_api_reactive.dto.GameDetailsResponse;
import cat.itacademy.s05.blackjack_api_reactive.dto.PlayRequest;
import cat.itacademy.s05.blackjack_api_reactive.model.Game;
import cat.itacademy.s05.blackjack_api_reactive.model.Player;
import cat.itacademy.s05.blackjack_api_reactive.repository.GameRepository;
import cat.itacademy.s05.blackjack_api_reactive.repository.PlayerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class GameService {
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final BlackjackGameEngine gameEngine;

    public GameService(GameRepository gameRepository, PlayerRepository playerRepository, BlackjackGameEngine gameEngine) {
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
        this.gameEngine = gameEngine;
    }

    public Mono<Game> createNewGame(String playerName) {
        return playerRepository.findByName(playerName)
                .switchIfEmpty(Mono.defer(() -> {
                    Player newPlayer = new Player(playerName);
                    return playerRepository.save(newPlayer);
                }))
                .flatMap(player -> {
                    Game newGame = new Game(playerName);
                    newGame = gameEngine.initializeGame(newGame);

                    return gameRepository.save(newGame)
                            .flatMap(savedGame -> {
                                if (savedGame.getStatus() != GameStatus.IN_PROGRESS){
                                    boolean isWin = (savedGame.getStatus() == GameStatus.PLAYER_WINS || savedGame.getStatus() == GameStatus.PLAYER_BLACKJACK);
                                    return updatePlayerScore(player, savedGame.getPlayerScore(), isWin).thenReturn(savedGame);
                                }
                                return Mono.just(savedGame);
                            });
                });
    }

    public Mono<GameDetailsResponse> getGameDetails(String gameId) {
        return gameRepository.findById(gameId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "\n" +
                        "Game not found with ID: " + gameId)))
                .map(GameDetailsResponse::new);
    }

    public Mono<GameDetailsResponse> play(String gameId, PlayRequest playRequest) {
        return gameRepository.findById(gameId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found with ID: " + gameId)))
                .flatMap(game -> {
                    if (game.getStatus() != GameStatus.IN_PROGRESS) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "The game is already over or not in progress. Current status: " + game.getStatus()));
                    }

                    try {
                        gameEngine.processPlayerMove(game, playRequest.getPlayType(), playRequest.getBetAmount());
                    } catch (IllegalArgumentException e) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage()));
                    }

                    game.setUpdatedAt(LocalDateTime.now());

                    return gameRepository.save(game)
                            .flatMap(savedGame -> {
                                if (savedGame.getStatus() != GameStatus.IN_PROGRESS) {
                                    boolean isWin = (savedGame.getStatus() == GameStatus.PLAYER_WINS || savedGame.getStatus() == GameStatus.PLAYER_BLACKJACK);
                                    return updatePlayerScore(savedGame.getPlayerName(), savedGame.getPlayerScore(), isWin).thenReturn(savedGame);
                                }
                                return Mono.just(savedGame);
                            })
                            .map(GameDetailsResponse::new);
                });
    }

    public Mono<Void> deleteGame(String gameId) {
        return gameRepository.findById(gameId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found with ID: " + gameId)))
                .flatMap(gameRepository::delete);
    }

    private Mono<Player> updatePlayerScore(String playerName, double scoreChange, boolean isWin) {
        return playerRepository.findByName(playerName)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found: " + playerName)))
                .flatMap(player -> {
                    player.updateScore(scoreChange);
                    if (isWin) {
                        player.registerWin();
                    }
                    return playerRepository.save(player);
                });
    }

    private Mono<Player> updatePlayerScore(Player player, double scoreChange, boolean isWin) {
        player.updateScore(scoreChange);
        if (isWin) {
            player.registerWin();
        }
        return playerRepository.save(player);
    }
}
