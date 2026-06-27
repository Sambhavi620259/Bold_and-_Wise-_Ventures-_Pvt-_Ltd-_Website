package in.bawvpl.Authify.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final int BLOCK_MINUTES = 15;

    private final Map<String, Integer> attempts = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> blockedUntil = new ConcurrentHashMap<>();

    public boolean isBlocked(String key) {

        LocalDateTime blocked = blockedUntil.get(key);

        if (blocked == null) {
            return false;
        }

        if (blocked.isBefore(LocalDateTime.now())) {

            blockedUntil.remove(key);
            attempts.remove(key);

            return false;
        }

        return true;
    }

    public void loginSucceeded(String key) {

        attempts.remove(key);
        blockedUntil.remove(key);
    }

    public void loginFailed(String key) {

        int count = attempts.getOrDefault(key, 0) + 1;

        attempts.put(key, count);

        if (count >= MAX_ATTEMPTS) {

            blockedUntil.put(
                    key,
                    LocalDateTime.now().plusMinutes(BLOCK_MINUTES)
            );

            log.warn("Login blocked for {}", key);
        }
    }

    public int getRemainingAttempts(String key) {

        return Math.max(
                0,
                MAX_ATTEMPTS - attempts.getOrDefault(key, 0)
        );
    }

    public long getRemainingBlockSeconds(String key) {

        LocalDateTime blocked = blockedUntil.get(key);

        if (blocked == null) {
            return 0;
        }

        return Duration.between(
                LocalDateTime.now(),
                blocked
        ).getSeconds();
    }
}