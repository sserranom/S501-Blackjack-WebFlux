package cat.itacademy.s05.blackjack_api_reactive.domain;

import cat.itacademy.s05.blackjack_api_reactive.domain.enums.GameStatus;
import cat.itacademy.s05.blackjack_api_reactive.model.Game;
import org.springframework.stereotype.Component;

@Component
public class GameOutcomeDeterminer {

    public void determinerOutcome(Game game, double betAmount){
        int playerHandValue = game.getPlayerHandValue();
        int dealerHandValue = game.getDealerHandValue();

        if (playerHandValue > 21){
            game.setStatus(GameStatus.DEALER_WINS);
            game.setPlayerScore(-betAmount);
        } else if (dealerHandValue > 21){
            game.setStatus(GameStatus.PLAYER_WINS);
            game.setPlayerScore(betAmount);
        } else if (playerHandValue > dealerHandValue){
            game.setStatus(GameStatus.PLAYER_WINS);
            game.setPlayerScore(betAmount);
        } else if (playerHandValue < dealerHandValue){
            game.setStatus(GameStatus.DEALER_WINS);
            game.setPlayerScore(-betAmount);
        } else {
            game.setStatus(GameStatus.PUSH);
            game.setPlayerScore(0.0);
        }
    }

    public GameStatus determinerInitialOutcome(Game game){
        boolean playerHasBlackJack = game.getPlayerHandValue() == 21 && game.getPlayerHand().size() == 2;
        boolean dealerHasBlackJack = game.getDealerHandValue() == 21 && game.getDealerHand().size() == 2;

        if (playerHasBlackJack && dealerHasBlackJack){
            game.setPlayerScore(0.0);
            return GameStatus.PUSH;
        } else if (playerHasBlackJack){
            game.setPlayerScore(1.5);
            return GameStatus.PLAYER_BLACKJACK;
        } else if (dealerHasBlackJack){
            game.setPlayerScore(-1.0);
            return GameStatus.DEALER_WINS;
        }else {
            return GameStatus.IN_PROGRESS;
        }
    }
}
