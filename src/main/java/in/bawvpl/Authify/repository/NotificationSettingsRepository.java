package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.NotificationSettingsEntity;
import in.bawvpl.Authify.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationSettingsRepository
        extends JpaRepository<NotificationSettingsEntity, Long> {

    Optional<NotificationSettingsEntity> findByUser(UserEntity user);

    Optional<NotificationSettingsEntity> findByUser_EmailIgnoreCase(String email);

}
