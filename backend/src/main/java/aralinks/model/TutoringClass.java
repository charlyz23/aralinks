package aralinks.model;

import aralinks.users.User;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "tutoring_classes")
public class TutoringClass extends BaseEntity {
    public enum Status { OPEN, ONGOING, COMPLETED, REMOVED }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id")
    private User tutor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    private int maxCapacity;
    private BigDecimal rate;
    private String platform;
    private String classroomLink;

    @Enumerated(EnumType.STRING)
    private Status status = Status.OPEN;

    @ManyToMany
    @JoinTable(name = "class_enrollments",
            joinColumns = @JoinColumn(name = "class_id"),
            inverseJoinColumns = @JoinColumn(name = "learner_id"))
    private Set<User> participants = new HashSet<>();

    @OneToMany(mappedBy = "tutoringClass")
    private List<TutoringSession> sessions = new ArrayList<>();

    protected TutoringClass() {
    }

    public TutoringClass(User tutor, Course course, int maxCapacity,
                         String platform, String classroomLink) {
        if (!tutor.teaches(course)) {
            throw new IllegalArgumentException("Tutor does not teach " + course.getName() + ".");
        }
        if (maxCapacity < 1) {
            throw new IllegalArgumentException("Capacity must be at least 1.");
        }
        this.tutor = tutor;
        this.course = course;
        this.maxCapacity = maxCapacity;
        this.rate = tutor.getHourlyRate();
        this.platform = platform;
        this.classroomLink = classroomLink;
    }

    public void addLearner(User learner) {
        if (status != Status.OPEN) {
            throw new IllegalStateException("This class is not open for enrollment.");
        }
        if (isFull()) {
            throw new IllegalStateException("This class is full.");
        }
        if (!participants.add(learner)) {
            throw new IllegalStateException(learner.getName() + " is already enrolled.");
        }
    }

    public boolean isFull() {
        return participants.size() >= maxCapacity;
    }

    public int getAvailableSlots() {
        return maxCapacity - participants.size();
    }

    public void startClass() {
        if (status != Status.OPEN) {
            throw new IllegalStateException("Only an open class can be started.");
        }
        status = Status.ONGOING;
    }

    public void endClass() {
        if (status != Status.ONGOING) {
            throw new IllegalStateException("Only an ongoing class can be ended.");
        }
        status = Status.COMPLETED;
    }

    /** Admin takedown: kept apart from COMPLETED, and it cancels whatever was still scheduled. */
    public void remove() {
        status = Status.REMOVED;
        sessions.stream()
                .filter(s -> s.getStatus() == TutoringSession.Status.SCHEDULED)
                .forEach(TutoringSession::cancel);
    }

    public User getTutor() { return tutor; }
    public Course getCourse() { return course; }
    public int getMaxCapacity() { return maxCapacity; }
    public BigDecimal getRate() { return rate; }
    public String getPlatform() { return platform; }
    public String getClassroomLink() { return classroomLink; }
    public Status getStatus() { return status; }
    public Set<User> getParticipants() { return participants; }
    public List<TutoringSession> getSessions() { return sessions; }
}
