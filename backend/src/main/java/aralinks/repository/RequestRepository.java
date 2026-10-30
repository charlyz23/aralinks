package aralinks.repository;

import aralinks.model.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RequestRepository extends JpaRepository<Request, UUID> {
    @Query("""
            select r from Request r
            where r.learner.id = :userId or r.tutor.id = :userId
            order by r.date desc, r.startTime desc
            """)
    List<Request> findForUser(@Param("userId") UUID userId);
}
