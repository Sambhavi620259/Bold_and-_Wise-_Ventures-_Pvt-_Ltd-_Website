package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.UserSession;
import in.bawvpl.Authify.repository.UserSessionRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserSessionServiceImpl implements UserSessionService {

    private final UserSessionRepository repository;

    @Override
    public UserSession createSession(
            Long userId,
            String token,
            String deviceName,
            String ipAddress,
            String userAgent
    ) {

        UserSession session = UserSession.builder()
                .userId(userId)
                .token(token)
                .deviceName(deviceName)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .active(true)
                .loginTime(LocalDateTime.now())
                .lastAccessTime(LocalDateTime.now())
                .build();

        return repository.save(session);
    }

    @Override
    public void logout(String token) {
        repository.deactivateByToken(token);
    }

    @Override
    public void logoutAll(Long userId) {
        repository.deactivateAllByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isActive(String token) {
        return repository.existsByTokenAndActiveTrue(token);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSession> getActiveSessions(Long userId) {
        return repository.findByUserIdAndActiveTrue(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserSession> getSession(Long sessionId, Long userId) {
        return repository.findByIdAndUserId(sessionId, userId);
    }

    @Override
    public void revokeSession(Long sessionId, Long userId) {

        UserSession session = repository
                .findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        session.setActive(false);

        repository.save(session);
    }
}