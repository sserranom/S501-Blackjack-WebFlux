package cat.itacademy.s05.blackjack_api_reactive.services;

import cat.itacademy.s05.blackjack_api_reactive.domain.Card;
import cat.itacademy.s05.blackjack_api_reactive.domain.Deck;
import cat.itacademy.s05.blackjack_api_reactive.domain.enums.GameStatus;
import cat.itacademy.s05.blackjack_api_reactive.domain.enums.PlayType;
import cat.itacademy.s05.blackjack_api_reactive.domain.enums.Rank;
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
import java.util.List;

@Service
public class GameService {
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final Deck deck;

    public GameService(GameRepository gameRepository, PlayerRepository playerRepository) {
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
        this.deck = new Deck();
    }

    private int calculateHandValue(List<Card> hand) {
        if (hand == null || hand.isEmpty()) {
            return 0;
        }
        int value = 0;
        int numAces = 0;

        for (Card card : hand) {
            if (card.getRank() == Rank.ACE) {
                value += 11;
                numAces++;
            } else {
                value += card.getRank().getValue();
            }
        }

        while (value > 21 && numAces > 0) {
            value -= 10;
            numAces--;
        }
        return value;
    }

    public Mono<Game> createNewGame(String playerName) {
        return playerRepository.findByName(playerName)
                .switchIfEmpty(Mono.defer(() -> {
                    Player newPlayer = new Player(playerName);
                    return playerRepository.save(newPlayer);
                }))
                .flatMap(player -> {
                    Game newGame = new Game(playerName);

                   newGame.addCardToPlayerHand(deck.dealCard());
                   newGame.addCardToPlayerHand(deck.dealCard());
                   newGame.setPlayerHandValue(calculateHandValue(newGame.getPlayerHand()));

                   newGame.addCardToDealerHand(deck.dealCard());
                   newGame.setDealerHandValue(calculateHandValue(newGame.getDealerHand()));
                   boolean playerHasBlackJack = newGame.getPlayerHandValue() == 21 && newGame.getPlayerHand().size() == 2;

                   Card dealerSecongCard = deck.dealCard();
                   newGame.addCardToDealerHand(dealerSecongCard);
                   newGame.setDealerHandValue(calculateHandValue(newGame.getDealerHand()));
                   boolean dealerHasBlackJack = newGame.getDealerHandValue() == 21 && newGame.getDealerHand().size() == 2;

                   if (playerHasBlackJack && dealerHasBlackJack){
                       newGame.setStatus(GameStatus.PUSH);
                       newGame.setPlayerScore(0.0);
                   } else if (playerHasBlackJack) {
                       newGame.setStatus(GameStatus.PLAYER_BLACKJACK);
                       newGame.setPlayerScore(1.5);
                    } else if (dealerHasBlackJack){
                       newGame.setStatus(GameStatus.DEALER_WINS);
                       newGame.setPlayerScore(-1.0);
                   }else {
                       newGame.setStatus(GameStatus.IN_PROGRESS);
                   }

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

                    PlayType playType = playRequest.getPlayType();
                    double betAmount = playRequest.getBetAmount();

                    Mono<Game> gameMono;

                    switch (playType) {
                        case HIT:
                            game.addCardToPlayerHand(deck.dealCard());
                            game.setPlayerHandValue(calculateHandValue(game.getPlayerHand()));

                            if (game.getPlayerHandValue() > 21) {
                                game.setStatus(GameStatus.DEALER_WINS);
                                game.setPlayerScore(-betAmount);
                                gameMono = gameRepository.save(game)
                                        .flatMap(savedGame -> updatePlayerScore(game.getPlayerName(), savedGame.getPlayerScore(), false).thenReturn(savedGame));
                            } else if (game.getPlayerHandValue() == 21) {
                                gameMono = dealerPlays(game, betAmount)
                                        .flatMap(savedGame -> updatePlayerScore(game.getPlayerName(), savedGame.getPlayerScore(), savedGame.getStatus() == GameStatus.PLAYER_WINS || savedGame.getStatus() == GameStatus.PLAYER_BLACKJACK).thenReturn(savedGame));
                            } else {
                                game.setUpdatedAt(LocalDateTime.now());
                                gameMono = gameRepository.save(game);
                            }
                            break;

                        case STAND:
                            gameMono = dealerPlays(game, betAmount)
                                    .flatMap(savedGame -> updatePlayerScore(game.getPlayerName(), savedGame.getPlayerScore(), savedGame.getStatus() == GameStatus.PLAYER_WINS || savedGame.getStatus() == GameStatus.PLAYER_BLACKJACK).thenReturn(savedGame));
                            break;

                        case DOUBLE_DOWN:
                            if (game.getPlayerHand().size() != 2) {
                                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Double Down is only allowed on the first turn (player has 2 cards)."));
                            }
                            game.addCardToPlayerHand(deck.dealCard());
                            game.setPlayerHandValue(calculateHandValue(game.getPlayerHand()));
                            double finalBetAmount = betAmount * 2;

                            if (game.getPlayerHandValue() > 21) {
                                game.setStatus(GameStatus.DEALER_WINS);
                                game.setPlayerScore(-finalBetAmount);
                                gameMono = gameRepository.save(game)
                                        .flatMap(savedGame -> updatePlayerScore(game.getPlayerName(), savedGame.getPlayerScore(), false).thenReturn(savedGame));
                            } else {
                                gameMono = dealerPlays(game, finalBetAmount)
                                        .flatMap(savedGame -> updatePlayerScore(game.getPlayerName(), savedGame.getPlayerScore(), savedGame.getStatus() == GameStatus.PLAYER_WINS || savedGame.getStatus() == GameStatus.PLAYER_BLACKJACK).thenReturn(savedGame));
                            }
                            break;

                        case SURRENDER:
                            if (game.getPlayerHand().size() != 2) {
                                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Surrender is only allowed on the first turn (player has 2 cards)."));
                            }
                            game.setStatus(GameStatus.SURRENDER);
                            game.setPlayerScore(-betAmount / 2);
                            gameMono = gameRepository.save(game)
                                    .flatMap(savedGame -> updatePlayerScore(game.getPlayerName(), savedGame.getPlayerScore(), false).thenReturn(savedGame));
                            break;

                        default:
                            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid play type."));
                    }

                    game.setUpdatedAt(LocalDateTime.now());
                    return gameMono.map(GameDetailsResponse::new);
                });
    }

    private Mono<Game> dealerPlays(Game game, double betAmount) {

        if (game.getDealerHand().size() == 1) {
            game.addCardToDealerHand(deck.dealCard());
            game.setDealerHandValue(calculateHandValue(game.getDealerHand()));
        }

        while (game.getDealerHandValue() < 17) {
            game.addCardToDealerHand(deck.dealCard());
            game.setDealerHandValue(calculateHandValue(game.getDealerHand()));
        }

        if (game.getDealerHandValue() > 21) {
            game.setStatus(GameStatus.PLAYER_WINS);
            game.setPlayerScore(betAmount);
        } else if (game.getPlayerHandValue() > game.getDealerHandValue()) {
            game.setStatus(GameStatus.PLAYER_WINS);
            game.setPlayerScore(betAmount);
        } else if (game.getPlayerHandValue() < game.getDealerHandValue()) {
            game.setStatus(GameStatus.DEALER_WINS);
            game.setPlayerScore(-betAmount);
        } else { // Tie
            game.setStatus(GameStatus.PUSH);
            game.setPlayerScore(0.0);
        }

        game.setUpdatedAt(LocalDateTime.now());
        return gameRepository.save(game);
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
