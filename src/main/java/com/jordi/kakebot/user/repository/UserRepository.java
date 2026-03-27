package com.jordi.kakebot.user.repository;

import com.jordi.kakebot.user.enums.UserProvider;
import com.jordi.kakebot.user.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);

    boolean existsByExternalId(String externalId);

    Optional<User> findByUsername(String username);

    Optional<User> findByProviderAndExternalId(UserProvider provider, String externalId);
}
