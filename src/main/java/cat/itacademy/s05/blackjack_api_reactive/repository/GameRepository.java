package cat.itacademy.s05.blackjack_api_reactive.repository;

import cat.itacademy.s05.blackjack_api_reactive.model.Game;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends ReactiveMongoRepository<Game, String > {

}
