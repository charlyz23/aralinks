package aralinks.model;

import aralinks.exception.InvalidRatingException;

import java.time.LocalDate;

/**
 * A learner's rating and comment for a completed session.
 * UML: Review * → 1 TutoringSession (for); Review * → 1 Learner (written by).
 * Owner: Aifa.
 */
public class Review {
    private final String reviewId;
    private final TutoringSession session;
    private final Learner learner;
    private final int rating;          // 1–5 stars
    private final String comment;
    private final LocalDate dateSubmitted;

    public Review(String reviewId, TutoringSession session, Learner learner, int rating, String comment)
            throws InvalidRatingException {
        if (!validateRating(rating)) throw new InvalidRatingException(rating);
        this.reviewId = reviewId;
        this.session = session;
        this.learner = learner;
        this.rating = rating;
        this.comment = comment == null ? "" : comment.trim();
        this.dateSubmitted = LocalDate.now();
    }

    /** True for 1 to 5. Static because it checks a number, not a particular review. DONE. */
    public static boolean validateRating(int rating) {
        return rating >= 1 && rating <= 5;
    }

    /** E.g. "★★★★☆ Cara Lim: Super clear explanations! (2026-11-09)". TODO (Aifa). */
    public String displayReview() {
        throw new UnsupportedOperationException("TODO: Review.displayReview");
    }

    // ---------- getters ----------
    public String getReviewId()            { return reviewId; }
    public TutoringSession getSession()    { return session; }
    public Learner getLearner()            { return learner; }
    public Tutor getTutor()                { return session.getTutor(); }
    public int getRating()                 { return rating; }
    public String getComment()             { return comment; }
    public LocalDate getDateSubmitted()    { return dateSubmitted; }
}
