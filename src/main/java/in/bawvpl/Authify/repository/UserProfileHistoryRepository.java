package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.UserProfileHistory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserProfileHistoryRepository
        extends JpaRepository<UserProfileHistory, Long> {

    List<UserProfileHistory> findByUserIdOrderByChangedAtDesc(
            String userId
    );
}