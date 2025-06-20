package cat.itacademy.s05.blackjack_api_reactive.dto;

import cat.itacademy.s05.blackjack_api_reactive.domain.Card;
import cat.itacademy.s05.blackjack_api_reactive.domain.enums.GameStatus;
import cat.itacademy.s05.blackjack_api_reactive.model.Game;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cache.Cache;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameDetailsResponse {

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

    public GameDetailsResponse(Game game) {
        this.id = game.getId();
        this.playerName = game.getPlayerName();
        this.playerHand = game.getPlayerHand();
        this.playerHandValue = game.getPlayerHandValue();
        this.dealerHand = game.getDealerHand();
        this.dealerHandValue = game.getDealerHandValue();
        this.status = game.getStatus();
        this.playerScore = game.getPlayerScore();
        this.createdAt = game.getCreatedAt();
        this.updatedAt = game.getUpdatedAt();
    }
}
