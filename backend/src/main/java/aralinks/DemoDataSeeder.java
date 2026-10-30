package aralinks;

import aralinks.model.Availability;
import aralinks.model.Course;
import aralinks.model.Payment;
import aralinks.model.Request;
import aralinks.model.Review;
import aralinks.model.TutoringSession;
import aralinks.repository.CourseRepository;
import aralinks.repository.PaymentRepository;
import aralinks.repository.RequestRepository;
import aralinks.repository.ReviewRepository;
import aralinks.repository.TutoringSessionRepository;
import aralinks.repository.UserRepository;
import aralinks.users.Role;
import aralinks.users.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Set;

/** Creates demo accounts on an empty database. Only runs when SEED_DEMO_DATA=true. */
@Component
@ConditionalOnProperty(name = "aralinks.seed-demo-data", havingValue = "true")
public class DemoDataSeeder implements ApplicationRunner {
    private final UserRepository users;
    private final CourseRepository courses;
    private final RequestRepository requests;
    private final TutoringSessionRepository sessions;
    private final ReviewRepository reviews;
    private final PaymentRepository payments;
    private final PasswordEncoder encoder;
    private final String demoPassword;

    public DemoDataSeeder(UserRepository users, CourseRepository courses, RequestRepository requests,
                          TutoringSessionRepository sessions, ReviewRepository reviews,
                          PaymentRepository payments, PasswordEncoder encoder,
                          @Value("${aralinks.demo-password}") String demoPassword) {
        this.users = users;
        this.courses = courses;
        this.requests = requests;
        this.sessions = sessions;
        this.reviews = reviews;
        this.payments = payments;
        this.encoder = encoder;
        this.demoPassword = demoPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (users.count() > 0) {
            return;
        }
        if (demoPassword.length() < 8) {
            throw new IllegalStateException("Set DEMO_PASSWORD (8+ characters) to seed demo data.");
        }
        Course oop = courses.save(new Course("CS-2102", "Object-Oriented Programming", "Programming", "Lecture"));
        Course dsa = courses.save(new Course("CS-2201", "Data Structures and Algorithms", "Programming", "Lecture"));
        Course calc = courses.save(new Course("MATH-1101", "Calculus 1", "Mathematics", "Lecture"));

        user("00-0001", "Ana Admin", "admin@aralinks.com", EnumSet.of(Role.ADMIN));

        User ben = user("21-12345", "Ben Tutor", "ben@aralinks.com", EnumSet.of(Role.TUTOR));
        ben.setTutorDetails(new BigDecimal("250.00"), "3rd-year CS student who loves OOP.");
        ben.addCourse(oop);
        ben.addCourse(dsa);
        weekdaysAndSaturday(ben);

        User gia = user("20-11111", "Gia Tutor", "gia@aralinks.com", EnumSet.of(Role.TUTOR, Role.LEARNER));
        gia.setTutorDetails(new BigDecimal("300.00"), "Math major who also takes CS electives.");
        gia.setLearnerDetails(4, "BS Mathematics");
        gia.addCourse(calc);
        weekdaysAndSaturday(gia);

        user("23-54321", "Cara Learner", "cara@aralinks.com", EnumSet.of(Role.LEARNER))
                .setLearnerDetails(1, "BSCS");
        User dan = user("23-98765", "Dan Learner", "dan@aralinks.com", EnumSet.of(Role.LEARNER));
        dan.setLearnerDetails(1, "BSIT");

        // Past sessions, so tutor profiles have ratings before the demo starts.
        pastSession(dan, ben, oop, 5, "Very clear explanations!", 2);
        pastSession(dan, gia, calc, 4, "Helpful, a bit fast.", 1);
    }

    private void pastSession(User learner, User tutor, Course course, int rating, String comment, int weeksAgo) {
        LocalDate date = LocalDate.now().minusWeeks(weeksAgo);
        Request request = requests.save(new Request(learner, tutor, course, date,
                LocalTime.of(10, 0), LocalTime.of(11, 0), "Please help me review."));
        TutoringSession session = sessions.save(TutoringSession.fromRequest(request));
        request.accept(session);
        session.complete(Set.of(learner.getId()));
        reviews.save(new Review(rating, comment, learner, session, date));
        payments.save(new Payment(session, learner, Payment.Method.GCASH, "DEMO-SEED-" + weeksAgo, Instant.now()));
    }

    private User user(String srCode, String name, String email, Set<Role> roles) {
        return users.save(new User(srCode, name, email, encoder.encode(demoPassword), "BatStateU", roles));
    }

    private void weekdaysAndSaturday(User tutor) {
        for (DayOfWeek day : DayOfWeek.values()) {
            if (day != DayOfWeek.SUNDAY) {
                tutor.addAvailability(new Availability(tutor, day, LocalTime.of(9, 0), LocalTime.of(17, 0)));
            }
        }
    }
}
