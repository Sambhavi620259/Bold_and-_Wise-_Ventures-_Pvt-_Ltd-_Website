package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.UserSession;

import java.util.List;
import java.util.Optional;

public interface UserSessionService {

    // Create a new login session
    UserSession createSession(
            Long userId,
            String token,
            String deviceName,
            String ipAddress,
            String userAgent
    );

    // Logout current session
    void logout(String token);

    // Logout all sessions of a user
    void logoutAll(Long userId);

    // Check whether a token is still active
    boolean isActive(String token);

    // Get all active sessions
    List<UserSession> getActiveSessions(Long userId);

    // Get one session
    Optional<UserSession> getSession(Long sessionId, Long userId);

    // Revoke a specific session
    void revokeSession(Long sessionId, Long userId);
}