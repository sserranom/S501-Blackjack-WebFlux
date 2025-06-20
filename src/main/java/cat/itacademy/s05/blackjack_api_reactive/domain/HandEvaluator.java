package cat.itacademy.s05.blackjack_api_reactive.domain;

import cat.itacademy.s05.blackjack_api_reactive.domain.enums.Rank;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HandEvaluator {

    public int calculateHandValue(List<Card> hand){
        if (hand == null || hand.isEmpty()){
            return 0;
        }
        int value = 0;
        int numAces = 0;

        for (Card card : hand){
            if (card.getRank() == Rank.ACE){
                value += 11;
                numAces++;
            }else{
                value += card.getRank().getValue();
            }
        }

        while (value > 21 && numAces > 0){
            value -= 10;
            numAces--;
        }
        return value;
    }

    public boolean isBlackJack(List<Card> hand, int handValue){
        return handValue == 21 && hand.size() == 2;
    }
}
