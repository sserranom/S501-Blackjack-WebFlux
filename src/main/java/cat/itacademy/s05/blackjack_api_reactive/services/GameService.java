package cat.itacademy.s05.blackjack_api_reactive.services;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
public class GameService {
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final List<String> deck;
    private final Random random;

    public GameService(GameRepository gameRepository, PlayerRepository playerRepository) {
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
        this.deck = initializeDeck();
        this.random = new Random();
    }

    private List<String> initializeDeck() {
        List<String> newDeck = new ArrayList<>();
        String[] suits = {"C", "D", "H", "S"};
        String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "T", "J", "Q", "K", "A"};

        for (String suit : suits) {
            for (String rank : ranks) {
                newDeck.add(rank + suit);
            }
        }
        Collections.shuffle(newDeck);
        return newDeck;
    }

    private String dealCard() {
        if (deck.isEmpty()) {
            deck.addAll(initializeDeck());
            Collections.shuffle(deck);
        }
        return deck.remove(0);
    }

    private int calculateHandValue(String hand) {
        if (hand == null || hand.isEmpty()) {
            return 0;
        }
        String[] cards = hand.split(", ");
        int value = 0;
        int numAces = 0;

        for (String card : cards) {
            String rank = card.substring(0, 1);
            switch (rank) {
                case "2": case "3": case "4": case "5": case "6": case "7": case "8": case "9":
                    value += Integer.parseInt(rank);
                    break;
                case "T": case "J": case "Q": case "K":
                    value += 10;
                    break;
                case "A":
                    value += 11;
                    numAces++;
                    break;
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
                    newGame.setPlayerHand(dealCard() + ", " + dealCard());
                    newGame.setPlayerHandValue(calculateHandValue(newGame.getPlayerHand()));

                    newGame.setDealerHand(dealCard());
                    newGame.setDealerHandValue(calculateHandValue(newGame.getDealerHand()));

                    if (newGame.getPlayerHandValue() == 21) {
                        newGame.setStatus("PLAYER_WINS");
                        newGame.setPlayerScore(1.5);
                        return gameRepository.save(newGame)
                                .flatMap(savedGame -> updatePlayerScore(player, savedGame.getPlayerScore(), true).thenReturn(savedGame));
                    } else if (newGame.getDealerHandValue() == 21) {
                        newGame.setStatus("DEALER_WINS");
                        newGame.setPlayerScore(-1.0);
                        return gameRepository.save(newGame)
                                .flatMap(savedGame -> updatePlayerScore(player, savedGame.getPlayerScore(), false).thenReturn(savedGame));
                    }

                    return gameRepository.save(newGame);
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
                    if (!game.getStatus().equals("IN_PROGRESS")) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "The game is already over."));
                    }

                    String playType = playRequest.getPlayType();
                    double betAmount = playRequest.getBetAmount();


                    if ("HIT".equalsIgnoreCase(playType)) {
                        String newCard = dealCard();
                        game.setPlayerHand(game.getPlayerHand() + ", " + newCard);
                        game.setPlayerHandValue(calculateHandValue(game.getPlayerHand()));

                        if (game.getPlayerHandValue() > 21) {
                            game.setStatus("DEALER_WINS");
                            game.setPlayerScore(-betAmount);
                            return gameRepository.save(game)
                                    .flatMap(savedGame -> updatePlayerScore(game.getPlayerName(), savedGame.getPlayerScore(), false).thenReturn(savedGame))
                                    .map(GameDetailsResponse::new);
                        } else if (game.getPlayerHandValue() == 21) {
                            return dealerPlays(game, betAmount)
                                    .flatMap(savedGame -> updatePlayerScore(game.getPlayerName(), savedGame.getPlayerScore(), savedGame.getStatus().equals("PLAYER_WINS")).thenReturn(savedGame))
                                    .map(GameDetailsResponse::new);
                        }
                    }

                    else if ("STAND".equalsIgnoreCase(playType)) {
                        return dealerPlays(game, betAmount)
                                .flatMap(savedGame -> updatePlayerScore(game.getPlayerName(), savedGame.getPlayerScore(), savedGame.getStatus().equals("PLAYER_WINS")).thenReturn(savedGame))
                                .map(GameDetailsResponse::new);
                    }

                    else if ("DOUBLE_DOWN".equalsIgnoreCase(playType)) {
                        String newCard = dealCard();
                        game.setPlayerHand(game.getPlayerHand() + ", " + newCard);
                        game.setPlayerHandValue(calculateHandValue(game.getPlayerHand()));
                        double finalBetAmount = betAmount * 2;

                        if (game.getPlayerHandValue() > 21) {
                            game.setStatus("DEALER_WINS");
                            game.setPlayerScore(-finalBetAmount);
                            return gameRepository.save(game)
                                    .flatMap(savedGame -> updatePlayerScore(game.getPlayerName(), savedGame.getPlayerScore(), false).thenReturn(savedGame))
                                    .map(GameDetailsResponse::new);
                        } else {
                            return dealerPlays(game, finalBetAmount)
                                    .flatMap(savedGame -> updatePlayerScore(game.getPlayerName(), savedGame.getPlayerScore(), savedGame.getStatus().equals("PLAYER_WINS")).thenReturn(savedGame))
                                    .map(GameDetailsResponse::new);
                        }
                    }

                    else if ("SURRENDER".equalsIgnoreCase(playType)) {
                        game.setStatus("SURRENDER");
                        game.setPlayerScore(-betAmount / 2);
                        return gameRepository.save(game)
                                .flatMap(savedGame -> updatePlayerScore(game.getPlayerName(), savedGame.getPlayerScore(), false).thenReturn(savedGame))
                                .map(GameDetailsResponse::new);
                    } else {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "\n" +
                                "Invalid play type."));
                    }

                    game.setUpdatedAt(LocalDateTime.now());
                    return gameRepository.save(game)
                            .map(GameDetailsResponse::new);
                });
    }

    private Mono<Game> dealerPlays(Game game, double betAmount) {
        game.setDealerHand(game.getDealerHand() + ", " + dealCard());
        game.setDealerHandValue(calculateHandValue(game.getDealerHand()));

        while (game.getDealerHandValue() < 17) {
            String newCard = dealCard();
            game.setDealerHand(game.getDealerHand() + ", " + newCard);
            game.setDealerHandValue(calculateHandValue(game.getDealerHand()));
        }

        if (game.getDealerHandValue() > 21) {
            game.setStatus("PLAYER_WINS");
            game.setPlayerScore(betAmount);
        } else if (game.getPlayerHandValue() > game.getDealerHandValue()) {
            game.setStatus("PLAYER_WINS");
            game.setPlayerScore(betAmount);
        } else if (game.getPlayerHandValue() < game.getDealerHandValue()) {
            game.setStatus("DEALER_WINS");
            game.setPlayerScore(-betAmount);
        } else {
            game.setStatus("PUSH");
            game.setPlayerScore(0.0);
        }

        game.setUpdatedAt(LocalDateTime.now());
        return gameRepository.save(game);
    }

    public Mono<Void> deleteGame(String gameId) {
        return gameRepository.findById(gameId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "\n" +
                        "Game not found with ID: " + gameId)))
                .flatMap(gameRepository::delete);
    }

    private Mono<Player> updatePlayerScore(String playerName, double scoreChange, boolean isWin) {
        return playerRepository.findByName(playerName)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "\n" +
                        "Player not found: " + playerName)))
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
