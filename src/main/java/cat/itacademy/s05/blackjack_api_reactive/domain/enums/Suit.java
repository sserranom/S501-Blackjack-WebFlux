package cat.itacademy.s05.blackjack_api_reactive.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Suit {
    CLUBS("C"),
    DIAMONDS("D"),
    HEARTS("H"),
    SPADES("S");

    private final String symbol;

    Suit(String symbol) {
        this.symbol = symbol;
    }

    @JsonValue
    public String getSymbol() {
        return symbol;
    }

    public static Suit fromSymbol(String symbol) {
        for (Suit suit : Suit.values()) {
            if (suit.symbol.equalsIgnoreCase(symbol)) {
                return suit;
            }
        }
        throw new IllegalArgumentException("Unknown suit symbol: " + symbol);
    }

}
