package cat.itacademy.s05.blackjack_api_reactive.exception;

public class GameNotFoundException extends RuntimeException {
    public GameNotFoundException(String message) {
        super(message);
    }
}
