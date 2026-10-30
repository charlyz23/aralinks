package aralinks.model;

import aralinks.users.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "availabilities")
public class Availability extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id")
    private User tutor;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week")
    private DayOfWeek day;

    private LocalTime startTime;
    private LocalTime endTime;

    protected Availability() {
    }

    public Availability(User tutor, DayOfWeek day, LocalTime startTime, LocalTime endTime) {
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("End time must be after start time.");
        }
        this.tutor = tutor;
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /** True if the whole slot falls inside this availability window. */
    public boolean covers(LocalDate date, LocalTime start, LocalTime end) {
        return date.getDayOfWeek() == day && !start.isBefore(startTime) && !end.isAfter(endTime);
    }

    public DayOfWeek getDay() { return day; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
}
