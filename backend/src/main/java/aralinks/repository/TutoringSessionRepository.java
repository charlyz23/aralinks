package aralinks.repository;

import aralinks.model.TutoringSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface TutoringSessionRepository extends JpaRepository<TutoringSession, UUID> {
    @Query("""
            select distinct s from TutoringSession s left join s.attendees a
            where s.tutor.id = :userId or a.learner.id = :userId
            order by s.date desc, s.startTime desc
            """)
    List<TutoringSession> findForUser(@Param("userId") UUID userId);

    @Query("""
            select count(s) > 0 from TutoringSession s
            where s.tutor.id = :tutorId and s.date = :date
              and s.status <> aralinks.model.TutoringSession.Status.CANCELLED
              and s.startTime < :endTime and s.endTime > :startTime
            """)
    boolean tutorHasOverlap(@Param("tutorId") UUID tutorId, @Param("date") LocalDate date,
                            @Param("startTime") LocalTime startTime, @Param("endTime") LocalTime endTime);
}
