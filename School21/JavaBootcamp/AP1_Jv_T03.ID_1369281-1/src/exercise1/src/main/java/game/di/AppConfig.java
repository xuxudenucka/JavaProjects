package game.di;

import game.datasource.repository.GameRepositoryImpl;
import game.domain.repository.GameRepository;
import game.domain.service.GameService;
import game.domain.service.GameServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import game.datasource.model.GameEntity;

@Configuration
public class AppConfig {
    @Bean
    public ConcurrentMap<UUID, GameEntity> gameStorage() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    public GameRepository gameRepository(ConcurrentMap<UUID, GameEntity> storage) {
        return new GameRepositoryImpl(storage);
    }

    @Bean
    public GameService gameService(GameRepository repository) {
        return new GameServiceImpl(repository);
    }
}