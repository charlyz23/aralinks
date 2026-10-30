package aralinks.api;

import aralinks.model.Availability;
import aralinks.model.Course;
import aralinks.model.Payment;
import aralinks.model.Request;
import aralinks.model.Review;
import aralinks.model.TutoringClass;
import aralinks.model.TutoringSession;
import aralinks.users.Role;
import aralinks.users.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** The JSON shapes of the API, kept apart from entities so hashes and links never leak. */
public final class Dto {
    /** Links are shown to other users as clickable, so only plain web addresses are accepted. */
    private static final String WEB_LINK = "^https?://\\S+$";

    private Dto() {
    }

    // --- Requests into the API ---

    public record RegisterRequest(
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Email @Size(max = 254) String email,
            @NotBlank @Size(min = 8, max = 72) String password,
            @NotBlank @Size(max = 30) String srCode,
            @NotBlank @Size(max = 120) String university,
            @NotNull @Size(min = 1) Set<Role> roles,
            Integer yearLevel,
            @Size(max = 60) String program,
            @PositiveOrZero BigDecimal hourlyRate,
            @Size(max = 1000) String bio) {
    }

    public record LoginRequest(@NotBlank String email, @NotBlank String password) {
    }

    public record NewCourse(@NotBlank @Size(max = 30) String code, @NotBlank @Size(max = 120) String name,
                            @NotBlank @Size(max = 60) String category, @NotBlank @Size(max = 30) String type) {
    }

    public record TutorUpdate(@NotNull @PositiveOrZero BigDecimal hourlyRate, @Size(max = 1000) String bio) {
    }

    public record NewAvailability(@NotNull DayOfWeek day, @NotNull LocalTime startTime, @NotNull LocalTime endTime) {
    }

    public record NewRequest(@NotNull UUID tutorId, @NotNull UUID courseId, @NotNull LocalDate date,
                             @NotNull LocalTime startTime, @NotNull LocalTime endTime,
                             @Size(max = 1000) String message) {
    }

    public record NewClass(@NotNull UUID courseId, @Min(1) int maxCapacity,
                           @NotBlank @Size(max = 60) String platform,
                           @Size(max = 500) @Pattern(regexp = WEB_LINK, message = "must be a web link") String classroomLink) {
    }

    public record NewClassSession(@NotNull LocalDate date, @NotNull LocalTime startTime, @NotNull LocalTime endTime) {
    }

    public record MeetingLink(@NotBlank @Size(max = 500)
                              @Pattern(regexp = WEB_LINK, message = "must be a web link") String meetingLink) {
    }

    public record CompleteSession(@NotNull Set<UUID> attendedLearnerIds) {
    }

    public record NewReview(@Min(1) @Max(5) int rating, @Size(max = 1000) String comment) {
    }

    public record NewPayment(@NotNull Payment.Method method) {
    }

    public record SetActive(boolean active) {
    }

    // --- Responses out of the API ---

    public record UserView(UUID id, String name, String email, String srCode, String university,
                           Set<Role> roles, boolean active, Integer yearLevel, String program) {
        public static UserView of(User u) {
            return new UserView(u.getId(), u.getName(), u.getEmail(), u.getSrCode(), u.getUniversity(),
                    Set.copyOf(u.getRoles()), u.isActive(), u.getYearLevel(), u.getProgram());
        }
    }

    public record AuthResponse(String token, UserView user) {
    }

    public record CourseView(UUID id, String code, String name, String category, String type) {
        public static CourseView of(Course c) {
            return new CourseView(c.getId(), c.getCode(), c.getName(), c.getCategory(), c.getType());
        }
    }

    public record AvailabilityView(UUID id, DayOfWeek day, LocalTime startTime, LocalTime endTime) {
        public static AvailabilityView of(Availability a) {
            return new AvailabilityView(a.getId(), a.getDay(), a.getStartTime(), a.getEndTime());
        }
    }

    public record ReviewView(UUID id, String learnerName, int rating, String comment, LocalDate dateSubmitted) {
        public static ReviewView of(Review r) {
            return new ReviewView(r.getId(), r.getLearner().getName(), r.getRating(), r.getComment(),
                    r.getDateSubmitted());
        }
    }

    public record TutorView(UUID id, String name, String university, BigDecimal hourlyRate, String bio,
                            double averageRating, int reviewCount, List<CourseView> courses,
                            List<AvailabilityView> availability, List<ReviewView> reviews) {
    }

    public record RequestView(UUID id, UUID learnerId, String learnerName, UUID tutorId, String tutorName,
                              CourseView course, LocalDate date, LocalTime startTime, LocalTime endTime,
                              String message, Request.Status status, UUID sessionId) {
        public static RequestView of(Request r) {
            return new RequestView(r.getId(), r.getLearner().getId(), r.getLearner().getName(),
                    r.getTutor().getId(), r.getTutor().getName(), CourseView.of(r.getCourse()), r.getDate(),
                    r.getStartTime(), r.getEndTime(), r.getMessage(), r.getStatus(),
                    r.getSession() == null ? null : r.getSession().getId());
        }
    }

    public record ClassView(UUID id, UUID tutorId, String tutorName, CourseView course, int maxCapacity,
                            int availableSlots, BigDecimal rate, String platform, TutoringClass.Status status) {
        public static ClassView of(TutoringClass c) {
            return new ClassView(c.getId(), c.getTutor().getId(), c.getTutor().getName(),
                    CourseView.of(c.getCourse()), c.getMaxCapacity(), c.getAvailableSlots(), c.getRate(),
                    c.getPlatform(), c.getStatus());
        }
    }

    public record AttendeeView(UUID learnerId, String name, boolean attended, boolean paid, boolean reviewed) {
    }

    public record SessionView(UUID id, UUID tutorId, String tutorName, CourseView course, UUID classId,
                              LocalDate date, LocalTime startTime, LocalTime endTime,
                              BigDecimal costPerLearner, BigDecimal totalCost,
                              TutoringSession.Status status, String meetingLink, List<AttendeeView> attendees) {
    }

    public record PaymentView(UUID id, BigDecimal amount, Payment.Method method, String reference,
                              Instant paidAt, boolean simulated) {
        public static PaymentView of(Payment p) {
            return new PaymentView(p.getId(), p.getAmount(), p.getMethod(), p.getReference(), p.getPaidAt(), true);
        }
    }
}
