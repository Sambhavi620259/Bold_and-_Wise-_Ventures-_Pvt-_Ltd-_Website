package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.AdminInviteEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminInviteRepository
        extends JpaRepository<AdminInviteEntity, Long> {

    Optional<AdminInviteEntity> findByTokenHash(
            String tokenHash
    );

    List<AdminInviteEntity> findByEmailIgnoreCase(
            String email
    );

    Optional<AdminInviteEntity>
    findFirstByEmailIgnoreCaseAndUsedFalseOrderByCreatedAtDesc(
            String email
    );

    long countByEmailIgnoreCaseAndUsedFalse(
            String email
    );

    List<AdminInviteEntity>
    findByEmailIgnoreCaseAndUsedFalse(
            String email
    );
}