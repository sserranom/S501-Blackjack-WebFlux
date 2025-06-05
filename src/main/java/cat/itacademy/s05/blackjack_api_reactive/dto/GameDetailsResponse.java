package cat.itacademy.s05.blackjack_api_reactive.dto;

import cat.itacademy.s05.blackjack_api_reactive.model.Game;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameDetailsResponse {

    private String id;
    private String playerName;
    private String playerHand;
    private int playerHandValue;
    private String dealerHand;
    private int dealerHandValue;
    private String status;
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
