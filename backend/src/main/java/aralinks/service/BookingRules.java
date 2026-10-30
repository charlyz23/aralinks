package aralinks.service;

import aralinks.model.Course;
import aralinks.repository.TutoringSessionRepository;
import aralinks.users.User;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

/** The checks every new session must pass, whether it comes from a request or a class. */
@Component
public class BookingRules {
    private static final ZoneId MANILA = ZoneId.of("Asia/Manila");

    private final TutoringSessionRepository sessions;

    public BookingRules(TutoringSessionRepository sessions) {
        this.sessions = sessions;
    }

    public void check(User tutor, Course course, LocalDate date, LocalTime start, LocalTime end) {
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("End time must be after start time.");
        }
        if (date.atTime(start).isBefore(LocalDateTime.now(MANILA))) {
            throw new IllegalArgumentException("That time has already passed.");
        }
        if (!tutor.teaches(course)) {
            throw new IllegalArgumentException("Tutor does not teach " + course.getName() + ".");
        }
        if (!tutor.isAvailable(date, start, end)) {
            throw new IllegalArgumentException("That time is outside the tutor's availability.");
        }
        if (sessions.tutorHasOverlap(tutor.getId(), date, start, end)) {
            throw new IllegalStateException("The tutor is already booked at that time.");
        }
    }
}
