package in.bawvpl.Authify.config;

import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) {

        // Founder referral code
        String founderReferralCode = "BWVPL#26";

        // Check if referral already exists
        if (userRepository.existsByReferralCode(founderReferralCode)) {
            return;
        }

        // Find founder/admin account
        userRepository.findByEmailIgnoreCase("admin@boldnwise.com")
                .ifPresent(user -> {

                    user.setReferralCode(founderReferralCode);

                    userRepository.save(user);

                    System.out.println(
                            "Founder referral code seeded: " +
                                    founderReferralCode
                    );
                });
    }
}