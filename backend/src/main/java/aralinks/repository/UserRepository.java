package aralinks.repository;

import aralinks.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsBySrCode(String srCode);

    @Query("""
            select distinct u from User u left join u.courses c
            where aralinks.users.Role.TUTOR member of u.roles and u.active = true
              and (lower(u.name) like :pattern or lower(c.name) like :pattern
                   or lower(c.category) like :pattern or lower(c.code) like :pattern)
            order by u.name
            """)
    List<User> searchTutors(@Param("pattern") String pattern);
}
