package cat.itacademy.s05.blackjack_api_reactive.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayRequest {
    @NotBlank(message = "The play type cannot be empty")
    @Pattern(regexp = "HIT|STAND|DOUBLE_DOWN|SURRENDER", message = "Invalid play type. Allowed values: HIT, STAND, DOUBLE_DOWN, SURRENDER")
    private String playType;

    @DecimalMin(value = "0.0", inclusive = true, message = "The amount wagered must be greater than or equal to 0")
    private double betAmount;

}
