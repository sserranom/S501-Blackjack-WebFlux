package cat.itacademy.s05.blackjack_api_reactive.services;

import cat.itacademy.s05.blackjack_api_reactive.dto.PlayerNameUpdateRequest;
import cat.itacademy.s05.blackjack_api_reactive.model.Player;
import cat.itacademy.s05.blackjack_api_reactive.repository.PlayerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PlayerService {
    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public Flux<Player> getPlayerRanking() {
        return playerRepository.findAllByOrderByTotalScoreDesc();
    }

    public Mono<Player> updatePlayerName(Long playerId, PlayerNameUpdateRequest request) {
        return playerRepository.findById(playerId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found with ID: " + playerId)))
                .flatMap(player -> {
                    player.setName(request.getNewPlayerName());
                    return playerRepository.save(player);
                });
    }
}
