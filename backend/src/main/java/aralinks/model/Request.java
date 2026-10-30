package aralinks.model;

import aralinks.users.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "requests")
public class Request extends BaseEntity {
    public enum Status { PENDING, ACCEPTED, DECLINED, CANCELLED }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learner_id")
    private User learner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id")
    private User tutor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private TutoringSession session;

    @Column(name = "session_date")
    private LocalDate date;

    private LocalTime startTime;
    private LocalTime endTime;
    private String message;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    protected Request() {
    }

    public Request(User learner, User tutor, Course course, LocalDate date,
                   LocalTime startTime, LocalTime endTime, String message) {
        this.learner = learner;
        this.tutor = tutor;
        this.course = course;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.message = message;
    }

    /** Accepting turns the request into the session it asked for. */
    public void accept(TutoringSession session) {
        requirePending();
        status = Status.ACCEPTED;
        this.session = session;
    }

    public void decline() {
        requirePending();
        status = Status.DECLINED;
    }

    public void cancel() {
        requirePending();
        status = Status.CANCELLED;
    }

    private void requirePending() {
        if (status != Status.PENDING) {
            throw new IllegalStateException("This request is already " + status + ".");
        }
    }

    public User getLearner() { return learner; }
    public User getTutor() { return tutor; }
    public Course getCourse() { return course; }
    public TutoringSession getSession() { return session; }
    public LocalDate getDate() { return date; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public String getMessage() { return message; }
    public Status getStatus() { return status; }
}
