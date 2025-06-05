package cat.itacademy.s05.blackjack_api_reactive.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("players")
public class Player {

    @Id
    private long id;
    private String name;
    private double totalScore;
    private int gamesPlayed;
    private int gamesWon;

    public Player(String name) {
        this.name = name;
        this.totalScore = 0.0;
        this.gamesPlayed = 0;
        this.gamesWon = 0;
    }

    public void updateScore(double scoreChange) {
        this.totalScore += scoreChange;
        this.gamesPlayed++;
    }

    public void registerWin() {
        this.gamesWon++;
    }
}
