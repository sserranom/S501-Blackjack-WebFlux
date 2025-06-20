package cat.itacademy.s05.blackjack_api_reactive.dto;

import cat.itacademy.s05.blackjack_api_reactive.domain.enums.PlayType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayRequest {
 @NotNull(message = "The play type cannot be empty")
    private PlayType playType;

 @DecimalMin(value = "0.0", inclusive = true, message = "The amount wagered must be greater than or equal to 0")
    private double betAmount;
}
