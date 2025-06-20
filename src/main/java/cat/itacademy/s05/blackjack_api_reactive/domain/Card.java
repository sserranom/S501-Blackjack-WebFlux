package cat.itacademy.s05.blackjack_api_reactive.domain;

import cat.itacademy.s05.blackjack_api_reactive.domain.enums.Rank;
import cat.itacademy.s05.blackjack_api_reactive.domain.enums.Suit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Card {
    private Rank rank;
    private Suit suit;

    public String getCardSymbol() {
        return rank.getSymbol() + suit.getSymbol();
    }

    public static Card fromSymbol(String symbol) {
        if (symbol == null || symbol.length() < 2) {
            throw new IllegalArgumentException("Invalid Card symbol: " + symbol);
        }

        String rankSymbol;
        String suitSymbol;

        if (symbol.length() == 3 && symbol.startsWith("10")) {
            rankSymbol = "T";
            suitSymbol = symbol.substring(2);
        } else {
            rankSymbol = symbol.substring(0, symbol.length() - 1);
            suitSymbol = symbol.substring(symbol.length() - 1);
        }
        return new Card(Rank.fromSymbol(rankSymbol), Suit.fromSymbol(suitSymbol));
    }

}
