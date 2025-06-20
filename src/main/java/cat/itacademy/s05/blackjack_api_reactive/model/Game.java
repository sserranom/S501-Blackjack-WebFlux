package cat.itacademy.s05.blackjack_api_reactive.model;

import cat.itacademy.s05.blackjack_api_reactive.domain.Card;
import cat.itacademy.s05.blackjack_api_reactive.domain.enums.GameStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "games")
public class Game {

    @Id
    private String id;
    private String playerName;
    private List<Card> playerHand;
    private int playerHandValue;
    private List<Card> dealerHand;
    private int dealerHandValue;
    private GameStatus status;
    private double playerScore;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Game(String playerName) {
        this.playerName = playerName;
        this.playerHand = new ArrayList<>();
        //this.playerHandValue = 0;
        this.dealerHand = new ArrayList<>();
        //this.dealerHandValue = 0;
        this.status = GameStatus.IN_PROGRESS;
        this.playerScore = 0.0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void addCardToPlayerHand(Card card) {
        if (this.playerHand == null) {
            this.playerHand = new ArrayList<>();
        }
        this.playerHand.add(card);
    }

    public void addCardToDealerHand(Card card) {
        if (this.dealerHand == null) {
            this.dealerHand = new ArrayList<>();
        }
        this.dealerHand.add(card);
    }

    public String getPlayerHandAsString() {
        return playerHand.stream()
                .map(Card::getCardSymbol)
                .collect(Collectors.joining(", "));
    }

    public String getDealerHandAsString() {
        return dealerHand.stream()
                .map(Card::getCardSymbol)
                .collect(Collectors.joining(", "));
    }

}
