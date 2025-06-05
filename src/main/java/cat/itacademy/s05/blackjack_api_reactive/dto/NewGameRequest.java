package cat.itacademy.s05.blackjack_api_reactive.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewGameRequest {
    @NotBlank(message = "Player name must not be empty.")
    private String playerName;

}
