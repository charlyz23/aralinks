package aralinks.repository;

import aralinks.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    boolean existsBySessionIdAndLearnerId(UUID sessionId, UUID learnerId);

    @Query("select avg(r.rating) from Review r where r.session.tutor.id = :tutorId")
    Double averageRatingForTutor(@Param("tutorId") UUID tutorId);

    @Query("select r from Review r where r.session.tutor.id = :tutorId order by r.dateSubmitted desc")
    List<Review> findForTutor(@Param("tutorId") UUID tutorId);
}
