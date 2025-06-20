package cat.itacademy.s05.blackjack_api_reactive.domain;

import cat.itacademy.s05.blackjack_api_reactive.domain.enums.GameStatus;
import cat.itacademy.s05.blackjack_api_reactive.domain.enums.PlayType;
import cat.itacademy.s05.blackjack_api_reactive.model.Game;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class BlackjackGameEngine {
    private final Deck deck;
    private final HandEvaluator handEvaluator;
    private final GameOutcomeDeterminer outcomeDeterminer;

    public BlackjackGameEngine(Deck deck, HandEvaluator handEvaluator, GameOutcomeDeterminer outcomeDeterminer) {
        this.deck = deck;
        this.handEvaluator = handEvaluator;
        this.outcomeDeterminer = outcomeDeterminer;
    }

    public Game initializeGame(Game game){
        game.addCardToPlayerHand(deck.dealCard());
        game.addCardToPlayerHand(deck.dealCard());
        game.setPlayerHandValue(handEvaluator.calculateHandValue(game.getPlayerHand()));

        game.addCardToDealerHand(deck.dealCard());
        game.addCardToDealerHand(deck.dealCard());
        game.setDealerHandValue(handEvaluator.calculateHandValue(game.getDealerHand()));

        GameStatus initialStatus = outcomeDeterminer.determinerInitialOutcome(game);
        game.setStatus(initialStatus);
        game.setUpdatedAt(LocalDateTime.now());
        return game;
    }

    public Game processPlayerMove(Game game, PlayType playType, double betAmount){
        switch(playType){
            case HIT:
                game.addCardToPlayerHand(deck.dealCard());
                game.setPlayerHandValue(handEvaluator.calculateHandValue(game.getPlayerHand()));

                if (game.getPlayerHandValue() > 21){
                    game.setStatus(GameStatus.DEALER_WINS);
                    game.setPlayerScore(-betAmount);
                } else if (game.getPlayerHandValue() == 21){
                    dealerPlays(game, betAmount);
                }
                break;

            case STAND:
                dealerPlays(game, betAmount);
                break;

            case DOUBLE_DOWN:
                if (game.getPlayerHand().size() !=2){
                    throw new IllegalArgumentException("Double Down is only allowed on the first turn (player has 2 cards).");
                }
                game.addCardToPlayerHand(deck.dealCard());
                game.setPlayerHandValue(handEvaluator.calculateHandValue(game.getPlayerHand()));
                dealerPlays(game, betAmount * 2);
                break;

            case SURRENDER:
                if (game.getPlayerHand().size() !=2){
                    throw new IllegalArgumentException("Surrender is only allowed on the first turn (player has 2 cards).");
                }
                game.setStatus(GameStatus.SURRENDER);
                game.setPlayerScore(-betAmount / 2);
                break;

            default:
                throw new IllegalArgumentException("Invalid play type: " + playType);

        }
        game.setUpdatedAt(LocalDateTime.now());
        return game;
    }

    public Game dealerPlays(Game game, double betAmount){
        while(game.getDealerHandValue() < 17){
            game.addCardToDealerHand(deck.dealCard());
            game.setDealerHandValue(handEvaluator.calculateHandValue(game.getDealerHand()));
        }
        outcomeDeterminer.determinerOutcome(game, betAmount);
        game.setUpdatedAt(LocalDateTime.now());
        return game;
    }
}
