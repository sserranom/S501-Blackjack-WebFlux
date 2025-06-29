package cat.itacademy.s05.blackjack_api_reactive.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewPlayerRequest {

    @NotBlank(message = "The player's name cannot be empty")
    private String playerName;
}
