package aralinks.model;

import aralinks.users.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "tutoring_sessions")
public class TutoringSession extends BaseEntity {
    public enum Status { SCHEDULED, COMPLETED, CANCELLED }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id")
    private User tutor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private TutoringClass tutoringClass;

    @Column(name = "session_date")
    private LocalDate date;

    private LocalTime startTime;
    private LocalTime endTime;

    /** Frozen when the session is created, so later rate changes can't alter an agreed price. */
    private BigDecimal rate;

    private String meetingLink;

    @Enumerated(EnumType.STRING)
    private Status status = Status.SCHEDULED;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attendance> attendees = new ArrayList<>();

    @OneToMany(mappedBy = "session")
    private List<Review> reviews = new ArrayList<>();

    protected TutoringSession() {
    }

    private TutoringSession(User tutor, Course course, TutoringClass tutoringClass, LocalDate date,
                            LocalTime startTime, LocalTime endTime, BigDecimal rate, String meetingLink) {
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("End time must be after start time.");
        }
        this.tutor = tutor;
        this.course = course;
        this.tutoringClass = tutoringClass;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.rate = rate;
        this.meetingLink = meetingLink;
    }

    public static TutoringSession fromRequest(Request request) {
        TutoringSession session = new TutoringSession(request.getTutor(), request.getCourse(), null,
                request.getDate(), request.getStartTime(), request.getEndTime(),
                request.getTutor().getHourlyRate(), null);
        session.addAttendee(request.getLearner());
        return session;
    }

    public static TutoringSession fromClass(TutoringClass tutoringClass, LocalDate date,
                                            LocalTime startTime, LocalTime endTime) {
        TutoringSession session = new TutoringSession(tutoringClass.getTutor(), tutoringClass.getCourse(),
                tutoringClass, date, startTime, endTime, tutoringClass.getRate(),
                tutoringClass.getClassroomLink());
        tutoringClass.getParticipants().forEach(session::addAttendee);
        tutoringClass.getSessions().add(session);
        return session;
    }

    public void addAttendee(User learner) {
        if (!isAttendee(learner)) {
            attendees.add(new Attendance(this, learner));
        }
    }

    public boolean isAttendee(User learner) {
        return attendees.stream().anyMatch(a -> a.getLearner().equals(learner));
    }

    public boolean attended(User learner) {
        return attendees.stream().anyMatch(a -> a.getLearner().equals(learner) && a.isAttended());
    }

    /** Only the learners the tutor marks present count as having attended. */
    public void complete(Set<UUID> attendedLearnerIds) {
        requireScheduled();
        status = Status.COMPLETED;
        attendees.forEach(a -> a.setAttended(attendedLearnerIds.contains(a.getLearner().getId())));
    }

    public void cancel() {
        requireScheduled();
        status = Status.CANCELLED;
    }

    private void requireScheduled() {
        if (status != Status.SCHEDULED) {
            throw new IllegalStateException("This session is already " + status + ".");
        }
    }

    /** Cost per learner = hourly rate x hours. */
    public BigDecimal costPerLearner() {
        BigDecimal minutes = BigDecimal.valueOf(Duration.between(startTime, endTime).toMinutes());
        return rate.multiply(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal totalCost() {
        return costPerLearner().multiply(BigDecimal.valueOf(Math.max(1, attendees.size())));
    }

    public void setMeetingLink(String meetingLink) { this.meetingLink = meetingLink; }

    public User getTutor() { return tutor; }
    public Course getCourse() { return course; }
    public TutoringClass getTutoringClass() { return tutoringClass; }
    public LocalDate getDate() { return date; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public BigDecimal getRate() { return rate; }
    public String getMeetingLink() { return meetingLink; }
    public Status getStatus() { return status; }
    public List<Attendance> getAttendees() { return attendees; }
    public List<Review> getReviews() { return reviews; }
}
