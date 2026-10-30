package aralinks;

import aralinks.model.Course;
import aralinks.repository.CourseRepository;
import aralinks.repository.UserRepository;
import aralinks.users.Role;
import aralinks.users.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Drives the real API over HTTP against a real PostgreSQL. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiFlowTest {
    private static final AtomicInteger COUNTER = new AtomicInteger();
    private static final String PASSWORD = "correct-horse-1";
    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper json = JsonMapper.builder().build();

    @Value("${local.server.port}")
    int port;
    @Autowired
    UserRepository users;
    @Autowired
    CourseRepository courses;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    JdbcTemplate jdbc;

    private Course oop;
    private LocalDate saturday;

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", LocalPostgres::jdbcUrl);
        registry.add("spring.datasource.username", () -> "postgres");
        registry.add("spring.datasource.password", () -> "postgres");
        registry.add("aralinks.jwt.secret", () -> "test-secret-test-secret-test-secret-1234");
    }

    @BeforeEach
    void setUp() {
        int n = COUNTER.incrementAndGet();
        oop = courses.save(new Course("CS-" + n, "Object-Oriented Programming " + n, "Programming", "Lecture"));
        saturday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SATURDAY)).plusWeeks(1);
    }

    // --- Login pipeline ---

    @Test
    void registerLoginAndMeWorkAndThePasswordIsHashed() {
        String email = "New.User" + COUNTER.incrementAndGet() + "@example.com";
        Res created = post("/api/auth/register", null, registration(email, "LEARNER"));
        assertThat(created.status).isEqualTo(201);

        String stored = jdbc.queryForObject("select password_hash from users where email = ?",
                String.class, email.toLowerCase());
        assertThat(stored).startsWith("$2").doesNotContain(PASSWORD);
        assertThat(created.body.toString()).doesNotContain("$2").doesNotContain("password");

        Res login = post("/api/auth/login", null, Map.of("email", email, "password", PASSWORD));
        assertThat(login.status).isEqualTo(200);
        Res me = get("/api/auth/me", login.body.get("token").asString());
        assertThat(me.status).isEqualTo(200);
        assertThat(me.body.get("email").asString()).isEqualTo(email.toLowerCase());
    }

    @Test
    void badCredentialsAndMissingTokensAreRejected() {
        String email = "user" + COUNTER.incrementAndGet() + "@example.com";
        post("/api/auth/register", null, registration(email, "LEARNER"));

        Res wrong = post("/api/auth/login", null, Map.of("email", email, "password", "wrong-password"));
        assertThat(wrong.status).isEqualTo(401);
        assertThat(wrong.body.get("message").asString()).isEqualTo("Invalid email or password.");
        assertThat(get("/api/auth/me", null).status).isEqualTo(401);
        assertThat(get("/api/auth/me", "not-a-real-token").status).isEqualTo(401);
    }

    @Test
    void duplicateEmailIsRejectedAndNobodyCanSelfRegisterAsAdmin() {
        String email = "dup" + COUNTER.incrementAndGet() + "@example.com";
        assertThat(post("/api/auth/register", null, registration(email, "LEARNER")).status).isEqualTo(201);
        assertThat(post("/api/auth/register", null, registration(email.toUpperCase(), "LEARNER")).status)
                .isEqualTo(409);
        assertThat(post("/api/auth/register", null, registration("a" + email, "ADMIN")).status).isEqualTo(400);
    }

    @Test
    void rolesAreEnforcedAndDeactivationTakesEffectImmediately() {
        Account learner = learner();
        Account admin = admin();
        assertThat(get("/api/admin/users", learner.token).status).isEqualTo(403);
        assertThat(get("/api/admin/users", admin.token).status).isEqualTo(200);

        Res off = send("PATCH", "/api/admin/users/" + learner.id + "/active", admin.token, Map.of("active", false));
        assertThat(off.status).isEqualTo(200);
        assertThat(get("/api/auth/me", learner.token).status).isEqualTo(401);
        assertThat(post("/api/auth/login", null, Map.of("email", learner.email, "password", PASSWORD)).status)
                .isEqualTo(401);
    }

    // --- Each case below failed in the original console version ---

    @Test
    void acceptingARequestCreatesTheSession() {
        Account tutor = tutor();
        Account learner = learner();
        String requestId = request(learner, tutor, "09:00", "10:00").body.get("id").asString();

        Res accepted = post("/api/requests/" + requestId + "/accept", tutor.token, Map.of());
        assertThat(accepted.body.get("status").asString()).isEqualTo("ACCEPTED");
        String sessionId = accepted.body.get("sessionId").asString();

        JsonNode sessions = get("/api/sessions", learner.token).body;
        assertThat(sessions).hasSize(1);
        assertThat(sessions.get(0).get("id").asString()).isEqualTo(sessionId);
    }

    @Test
    void requestsMustMatchTheTutorsCoursesAndAvailability() {
        Account tutor = tutor();
        Account learner = learner();
        Course untaught = courses.save(new Course("X-" + COUNTER.incrementAndGet(), "Calculus", "Math", "Lecture"));

        Res wrongCourse = post("/api/requests", learner.token, Map.of("tutorId", tutor.id,
                "courseId", untaught.getId(), "date", saturday.toString(), "startTime", "09:00", "endTime", "10:00"));
        assertThat(wrongCourse.status).isEqualTo(400);
        assertThat(request(learner, tutor, "03:00", "04:00").status).isEqualTo(400);
        assertThat(request(learner, tutor, "11:00", "13:00").status).isEqualTo(400);
    }

    @Test
    void aTutorCannotBeDoubleBooked() {
        Account tutor = tutor();
        Account first = learner();
        Account second = learner();
        String one = request(first, tutor, "09:00", "11:00").body.get("id").asString();
        String two = request(second, tutor, "10:00", "12:00").body.get("id").asString();

        assertThat(post("/api/requests/" + one + "/accept", tutor.token, Map.of()).status).isEqualTo(200);
        assertThat(post("/api/requests/" + two + "/accept", tutor.token, Map.of()).status).isEqualTo(409);
    }

    @Test
    void theDatabaseItselfBlocksOverlappingSessions() {
        Account tutor = tutor();
        String insert = """
                insert into tutoring_sessions
                  (id, tutor_id, course_id, session_date, start_time, end_time, rate, status)
                values (gen_random_uuid(), ?::uuid, ?, ?, ?::time, ?::time, 100, 'SCHEDULED')
                """;
        jdbc.update(insert, tutor.id, oop.getId(), saturday, "09:00", "11:00");
        assertThatThrownBy(() -> jdbc.update(insert, tutor.id, oop.getId(), saturday, "10:00", "12:00"))
                .isInstanceOf(DataIntegrityViolationException.class);
        jdbc.update(insert, tutor.id, oop.getId(), saturday, "11:00", "12:00");
    }

    @Test
    void capacityMustBePositiveAndClassesCanHaveManySessions() {
        Account tutor = tutor();
        assertThat(post("/api/classes", tutor.token, newClass(-1)).status).isEqualTo(400);

        String classId = post("/api/classes", tutor.token, newClass(2)).body.get("id").asString();
        Res week1 = post("/api/classes/" + classId + "/sessions", tutor.token,
                Map.of("date", saturday.toString(), "startTime", "09:00", "endTime", "10:00"));
        Res week2 = post("/api/classes/" + classId + "/sessions", tutor.token,
                Map.of("date", saturday.plusWeeks(1).toString(), "startTime", "09:00", "endTime", "10:00"));
        assertThat(week1.status).isEqualTo(201);
        assertThat(week2.status).isEqualTo(201);
    }

    @Test
    void enrollmentClosesOnceAClassStartsAndEarlierEnrolleesAttend() {
        Account tutor = tutor();
        Account early = learner();
        Account late = learner();
        String classId = post("/api/classes", tutor.token, newClass(5)).body.get("id").asString();
        String sessionId = post("/api/classes/" + classId + "/sessions", tutor.token,
                Map.of("date", saturday.toString(), "startTime", "09:00", "endTime", "10:00")).body.get("id").asString();

        assertThat(post("/api/classes/" + classId + "/enroll", early.token, Map.of()).status).isEqualTo(200);
        assertThat(get("/api/sessions/" + sessionId, early.token).body.get("attendees")).hasSize(1);

        post("/api/classes/" + classId + "/start", tutor.token, Map.of());
        assertThat(post("/api/classes/" + classId + "/enroll", late.token, Map.of()).status).isEqualTo(409);
    }

    @Test
    void parallelEnrollmentsCannotOverfillAClass() throws Exception {
        Account tutor = tutor();
        String classId = post("/api/classes", tutor.token, newClass(1)).body.get("id").asString();
        List<Callable<Integer>> attempts = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            Account learner = learner();
            attempts.add(() -> post("/api/classes/" + classId + "/enroll", learner.token, Map.of()).status);
        }
        try (var pool = Executors.newFixedThreadPool(6)) {
            int successes = 0;
            for (var result : pool.invokeAll(attempts)) {
                if (result.get() == 200) successes++;
            }
            assertThat(successes).isEqualTo(1);
        }
        assertThat(get("/api/classes/" + classId, null).body.get("availableSlots").asInt()).isZero();
    }

    @Test
    void finishedSessionsCannotChangeStatus() {
        Account tutor = tutor();
        Account learner = learner();
        String sessionId = bookedSession(tutor, learner);

        assertThat(post("/api/sessions/" + sessionId + "/cancel", tutor.token, Map.of()).status).isEqualTo(200);
        assertThat(complete(tutor, sessionId, List.of(learner.id)).status).isEqualTo(409);
    }

    @Test
    void oneReviewPerLearnerAndOnlyIfTheyActuallyAttended() {
        Account tutor = tutor();
        Account present = learner();
        Account absent = learner();
        String classId = post("/api/classes", tutor.token, newClass(5)).body.get("id").asString();
        post("/api/classes/" + classId + "/enroll", present.token, Map.of());
        post("/api/classes/" + classId + "/enroll", absent.token, Map.of());
        String sessionId = post("/api/classes/" + classId + "/sessions", tutor.token,
                Map.of("date", saturday.toString(), "startTime", "09:00", "endTime", "10:00")).body.get("id").asString();
        complete(tutor, sessionId, List.of(present.id));

        Map<String, Object> review = Map.of("rating", 5, "comment", "Great");
        assertThat(post("/api/sessions/" + sessionId + "/reviews", present.token, review).status).isEqualTo(201);
        assertThat(post("/api/sessions/" + sessionId + "/reviews", present.token, review).status).isEqualTo(409);
        assertThat(post("/api/sessions/" + sessionId + "/reviews", absent.token, review).status).isEqualTo(409);

        JsonNode attendees = get("/api/sessions/" + sessionId, tutor.token).body.get("attendees");
        assertThat(attendees.toString()).contains("\"reviewed\":true", "\"reviewed\":false");
    }

    @Test
    void meetingLinksMustBeWebAddresses() {
        Account tutor = tutor();
        Account learner = learner();
        String sessionId = bookedSession(tutor, learner);
        String path = "/api/sessions/" + sessionId + "/meeting-link";

        assertThat(send("PUT", path, tutor.token, Map.of("meetingLink", "javascript:alert(1)")).status).isEqualTo(400);
        assertThat(send("PUT", path, tutor.token, Map.of("meetingLink", "https://meet.google.com/abc-defg-hij")).status)
                .isEqualTo(200);
    }

    @Test
    void ratingIsAlwaysCurrentOnTheTutorProfile() {
        Account tutor = tutor();
        Account learner = learner();
        String sessionId = bookedSession(tutor, learner);
        complete(tutor, sessionId, List.of(learner.id));
        assertThat(get("/api/tutors/" + tutor.id, null).body.get("averageRating").asDouble()).isZero();

        post("/api/sessions/" + sessionId + "/reviews", learner.token, Map.of("rating", 4, "comment", "Good"));
        JsonNode profile = get("/api/tutors/" + tutor.id, null).body;
        assertThat(profile.get("averageRating").asDouble()).isEqualTo(4.0);
        assertThat(profile.get("reviewCount").asInt()).isEqualTo(1);
    }

    @Test
    void aRateChangeDoesNotAlterAnAgreedPrice() {
        Account tutor = tutor();
        Account learner = learner();
        String sessionId = bookedSession(tutor, learner);
        assertThat(cost(learner, sessionId)).isEqualTo("250.00");

        send("PUT", "/api/me/tutor", tutor.token, Map.of("hourlyRate", 1000, "bio", "Raised my rate"));
        complete(tutor, sessionId, List.of(learner.id));
        assertThat(cost(learner, sessionId)).isEqualTo("250.00");
    }

    @Test
    void removedListingsAreDistinctFromCompletedOnesAndCancelTheirSessions() {
        Account tutor = tutor();
        Account admin = admin();
        String classId = post("/api/classes", tutor.token, newClass(2)).body.get("id").asString();
        String sessionId = post("/api/classes/" + classId + "/sessions", tutor.token,
                Map.of("date", saturday.toString(), "startTime", "09:00", "endTime", "10:00")).body.get("id").asString();

        Res removed = send("DELETE", "/api/admin/classes/" + classId, admin.token, null);
        assertThat(removed.body.get("status").asString()).isEqualTo("REMOVED");
        assertThat(get("/api/sessions/" + sessionId, tutor.token).body.get("status").asString()).isEqualTo("CANCELLED");
    }

    @Test
    void aReloadedCourseStillMatchesTheOriginal() {
        Account tutor = tutor();
        Course reloaded = courses.findById(oop.getId()).orElseThrow();
        assertThat(reloaded).isEqualTo(oop).isNotSameAs(oop);
        assertThat(post("/api/classes", tutor.token, Map.of("courseId", reloaded.getId(), "maxCapacity", 1,
                "platform", "Google Meet")).status).isEqualTo(201);
    }

    // --- New demo features ---

    @Test
    void oneAccountCanBeBothTutorAndLearner() {
        Account both = account("TUTOR", "LEARNER");
        Account tutor = tutor();
        send("POST", "/api/me/tutor/courses/" + oop.getId(), both.token, null);
        assertThat(request(both, tutor, "09:00", "10:00").status).isEqualTo(201);
        assertThat(get("/api/tutors/" + both.id, null).status).isEqualTo(200);
    }

    @Test
    void simulatedPaymentIsRecordedOnceAndOnlyForParticipants() {
        Account tutor = tutor();
        Account learner = learner();
        Account stranger = learner();
        String sessionId = bookedSession(tutor, learner);
        Map<String, Object> gcash = Map.of("method", "GCASH");

        Res paid = post("/api/sessions/" + sessionId + "/payments", learner.token, gcash);
        assertThat(paid.status).isEqualTo(201);
        assertThat(paid.body.get("simulated").asBoolean()).isTrue();
        assertThat(paid.body.get("amount").decimalValue()).isEqualByComparingTo("250.00");
        assertThat(paid.body.get("reference").asString()).startsWith("DEMO-");

        assertThat(post("/api/sessions/" + sessionId + "/payments", learner.token, gcash).status).isEqualTo(409);
        assertThat(post("/api/sessions/" + sessionId + "/payments", stranger.token, gcash).status).isEqualTo(403);
        assertThat(get("/api/sessions/" + sessionId, stranger.token).status).isEqualTo(403);
    }

    @Test
    void tutorSearchFindsByNameCourseAndCategory() {
        Account tutor = tutor();
        Res hits = get("/api/tutors?q=object-oriented+programming+" + COUNTER.get(), null);
        assertThat(hits.status).isEqualTo(200);
        assertThat(get("/api/tutors?q=programming", null).body.toString()).contains(tutor.id);
        assertThat(get("/api/tutors?q=no-such-topic-anywhere", null).body).isEmpty();
    }

    // --- Helpers ---

    private record Res(int status, JsonNode body) {
    }

    private record Account(String id, String email, String token) {
    }

    private Account learner() {
        return account("LEARNER");
    }

    /** A tutor charging 250/hr who teaches OOP and is free Saturdays 9 to 12. */
    private Account tutor() {
        Account tutor = account("TUTOR");
        send("PUT", "/api/me/tutor", tutor.token, Map.of("hourlyRate", 250, "bio", "Tutor"));
        send("POST", "/api/me/tutor/courses/" + oop.getId(), tutor.token, null);
        post("/api/me/tutor/availability", tutor.token,
                Map.of("day", "SATURDAY", "startTime", "09:00", "endTime", "12:00"));
        return tutor;
    }

    private Account admin() {
        String email = "admin" + UUID.randomUUID() + "@example.com";
        users.save(new User(UUID.randomUUID().toString().substring(0, 12), "Admin", email,
                encoder.encode(PASSWORD), "BatStateU", EnumSet.of(Role.ADMIN)));
        Res login = post("/api/auth/login", null, Map.of("email", email, "password", PASSWORD));
        return new Account(login.body.get("user").get("id").asString(), email, login.body.get("token").asString());
    }

    private Account account(String... roles) {
        String email = "user" + UUID.randomUUID() + "@example.com";
        Res res = post("/api/auth/register", null, registration(email, roles));
        assertThat(res.status).isEqualTo(201);
        return new Account(res.body.get("user").get("id").asString(), email, res.body.get("token").asString());
    }

    private Map<String, Object> registration(String email, String... roles) {
        return Map.of("name", "Test User", "email", email, "password", PASSWORD,
                "srCode", UUID.randomUUID().toString().substring(0, 12),
                "university", "BatStateU", "roles", List.of(roles));
    }

    private Map<String, Object> newClass(int capacity) {
        return Map.of("courseId", oop.getId(), "maxCapacity", capacity, "platform", "Google Meet");
    }

    private Res request(Account learner, Account tutor, String start, String end) {
        return post("/api/requests", learner.token, Map.of("tutorId", tutor.id, "courseId", oop.getId(),
                "date", saturday.toString(), "startTime", start, "endTime", end, "message", "Please help"));
    }

    private String bookedSession(Account tutor, Account learner) {
        String requestId = request(learner, tutor, "09:00", "10:00").body.get("id").asString();
        return post("/api/requests/" + requestId + "/accept", tutor.token, Map.of()).body.get("sessionId").asString();
    }

    private Res complete(Account tutor, String sessionId, List<String> attended) {
        return post("/api/sessions/" + sessionId + "/complete", tutor.token, Map.of("attendedLearnerIds", attended));
    }

    private String cost(Account viewer, String sessionId) {
        return get("/api/sessions/" + sessionId, viewer.token).body.get("costPerLearner").decimalValue()
                .setScale(2).toPlainString();
    }

    private Res get(String path, String token) {
        return send("GET", path, token, null);
    }

    private Res post(String path, String token, Object body) {
        return send("POST", path, token, body);
    }

    private Res send(String method, String path, String token, Object body) {
        try {
            HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                    .header("Content-Type", "application/json")
                    .method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                            : HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)));
            if (token != null) {
                request.header("Authorization", "Bearer " + token);
            }
            HttpResponse<String> response = http.send(request.build(), HttpResponse.BodyHandlers.ofString());
            String text = response.body();
            return new Res(response.statusCode(), text == null || text.isBlank() ? json.nullNode() : json.readTree(text));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
