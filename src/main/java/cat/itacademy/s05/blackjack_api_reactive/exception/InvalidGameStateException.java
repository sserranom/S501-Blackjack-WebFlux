package cat.itacademy.s05.blackjack_api_reactive.exception;

public class InvalidGameStateException extends RuntimeException {
    public InvalidGameStateException(String message) {
        super(message);
    }
}
