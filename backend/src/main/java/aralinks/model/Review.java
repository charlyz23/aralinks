package aralinks.model;

import aralinks.users.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "reviews")
public class Review extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learner_id")
    private User learner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private TutoringSession session;

    private int rating;
    private String comment;
    private LocalDate dateSubmitted;

    protected Review() {
    }

    public Review(int rating, String comment, User learner, TutoringSession session, LocalDate dateSubmitted) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }
        if (session.getStatus() != TutoringSession.Status.COMPLETED) {
            throw new IllegalStateException("Only completed sessions can be reviewed.");
        }
        if (!session.attended(learner)) {
            throw new IllegalStateException("You did not attend this session.");
        }
        this.rating = rating;
        this.comment = comment;
        this.learner = learner;
        this.session = session;
        this.dateSubmitted = dateSubmitted;
    }

    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public User getLearner() { return learner; }
    public TutoringSession getSession() { return session; }
    public LocalDate getDateSubmitted() { return dateSubmitted; }
}
