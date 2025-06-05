package cat.itacademy.s05.blackjack_api_reactive.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Collection;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "games")
public class Game {

    @Id
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

    public Game(String playerName) {
        this.playerName = playerName;
        this.playerHand = "";
        this.playerHandValue = 0;
        this.dealerHand = "";
        this.dealerHandValue = 0;
        this.status = "IN_PROGRESS";
        this.playerScore = 0.0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

}
