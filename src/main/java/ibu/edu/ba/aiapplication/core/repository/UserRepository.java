package ibu.edu.ba.aiapplication.core.repository;

import ibu.edu.ba.aiapplication.core.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    // Add deleteByEmail method
    void deleteByEmail(String email);
}
