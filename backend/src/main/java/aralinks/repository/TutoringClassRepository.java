package aralinks.repository;

import aralinks.model.TutoringClass;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TutoringClassRepository extends JpaRepository<TutoringClass, UUID> {
    List<TutoringClass> findByStatus(TutoringClass.Status status);

    /** Locks the row so two learners can't take the last slot at the same moment. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from TutoringClass c where c.id = :id")
    Optional<TutoringClass> findByIdForUpdate(@Param("id") UUID id);
}
