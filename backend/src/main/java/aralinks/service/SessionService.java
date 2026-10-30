package aralinks.service;

import aralinks.api.Dto;
import aralinks.model.Payment;
import aralinks.model.Review;
import aralinks.model.TutoringSession;
import aralinks.repository.PaymentRepository;
import aralinks.repository.ReviewRepository;
import aralinks.repository.TutoringSessionRepository;
import aralinks.repository.UserRepository;
import aralinks.users.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SessionService {
    private final TutoringSessionRepository sessions;
    private final UserRepository users;
    private final ReviewRepository reviews;
    private final PaymentRepository payments;

    public SessionService(TutoringSessionRepository sessions, UserRepository users,
                          ReviewRepository reviews, PaymentRepository payments) {
        this.sessions = sessions;
        this.users = users;
        this.reviews = reviews;
        this.payments = payments;
    }

    @Transactional(readOnly = true)
    public List<Dto.SessionView> mine(UUID userId) {
        return sessions.findForUser(userId).stream().map(this::toView).toList();
    }

    @Transactional(readOnly = true)
    public List<Dto.SessionView> all() {
        return sessions.findAll().stream().map(this::toView).toList();
    }

    @Transactional(readOnly = true)
    public Dto.SessionView get(UUID userId, UUID sessionId) {
        TutoringSession session = find(sessionId);
        if (!session.getTutor().getId().equals(userId)
                && !session.isAttendee(users.getReferenceById(userId))) {
            throw new AccessDeniedException("You are not part of this session.");
        }
        return toView(session);
    }

    public Dto.SessionView setMeetingLink(UUID tutorId, UUID sessionId, String link) {
        TutoringSession session = owned(tutorId, sessionId);
        session.setMeetingLink(link);
        return toView(session);
    }

    public Dto.SessionView complete(UUID tutorId, UUID sessionId, Dto.CompleteSession c) {
        TutoringSession session = owned(tutorId, sessionId);
        session.complete(c.attendedLearnerIds());
        return toView(session);
    }

    public Dto.SessionView cancel(UUID tutorId, UUID sessionId) {
        TutoringSession session = owned(tutorId, sessionId);
        session.cancel();
        return toView(session);
    }

    public Dto.ReviewView review(UUID learnerId, UUID sessionId, Dto.NewReview r) {
        TutoringSession session = find(sessionId);
        User learner = users.getReferenceById(learnerId);
        if (reviews.existsBySessionIdAndLearnerId(sessionId, learnerId)) {
            throw new IllegalStateException("You already reviewed this session.");
        }
        Review review = new Review(r.rating(), r.comment(), learner, session, LocalDate.now());
        return Dto.ReviewView.of(reviews.save(review));
    }

    /** Simulated for the demo: records the payment without moving any money. */
    public Dto.PaymentView pay(UUID learnerId, UUID sessionId, Payment.Method method) {
        TutoringSession session = find(sessionId);
        User learner = users.getReferenceById(learnerId);
        if (!session.isAttendee(learner)) {
            throw new AccessDeniedException("You are not part of this session.");
        }
        if (session.getStatus() == TutoringSession.Status.CANCELLED) {
            throw new IllegalStateException("This session was cancelled.");
        }
        if (payments.existsBySessionIdAndLearnerId(sessionId, learnerId)) {
            throw new IllegalStateException("You already paid for this session.");
        }
        String reference = "DEMO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return Dto.PaymentView.of(payments.save(new Payment(session, learner, method, reference, Instant.now())));
    }

    Dto.SessionView toView(TutoringSession s) {
        var attendees = s.getAttendees().stream()
                .map(a -> new Dto.AttendeeView(a.getLearner().getId(), a.getLearner().getName(), a.isAttended(),
                        payments.existsBySessionIdAndLearnerId(s.getId(), a.getLearner().getId()),
                        reviews.existsBySessionIdAndLearnerId(s.getId(), a.getLearner().getId())))
                .toList();
        return new Dto.SessionView(s.getId(), s.getTutor().getId(), s.getTutor().getName(),
                Dto.CourseView.of(s.getCourse()),
                s.getTutoringClass() == null ? null : s.getTutoringClass().getId(),
                s.getDate(), s.getStartTime(), s.getEndTime(), s.costPerLearner(), s.totalCost(),
                s.getStatus(), s.getMeetingLink(), attendees);
    }

    private TutoringSession owned(UUID tutorId, UUID sessionId) {
        TutoringSession session = find(sessionId);
        if (!session.getTutor().getId().equals(tutorId)) {
            throw new AccessDeniedException("This session belongs to another tutor.");
        }
        return session;
    }

    private TutoringSession find(UUID id) {
        return sessions.findById(id).orElseThrow(() -> new NotFoundException("Session not found."));
    }
}
