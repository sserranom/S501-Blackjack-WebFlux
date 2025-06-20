package cat.itacademy.s05.blackjack_api_reactive.domain;

import cat.itacademy.s05.blackjack_api_reactive.domain.enums.Rank;
import cat.itacademy.s05.blackjack_api_reactive.domain.enums.Suit;

import java.util.*;

public class Deck {
    private List<Card> cards;
    private final Random random;

    public Deck(){
        this.random = new Random();
        this.cards = initializeDeck();
    }

    private List<Card> initializeDeck(){
        List<Card> newDeck = new ArrayList<>();
        for (Suit suit : Suit.values()){
            for (Rank rank : Rank.values()){
                newDeck.add(new Card(rank, suit));
            }
        }
        Collections.shuffle(newDeck, random);
        return newDeck;
    }

    public Card dealCard(){
        if (cards.isEmpty()){
            System.out.println("Deck is empty, reinitializing and shuffling. ");
           cards = initializeDeck();
        }
        return cards.remove(0);
    }

    public int size(){
        return cards.size();
    }
}
