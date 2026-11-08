package aralinks.model;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * One block of time a tutor is free, e.g. Monday 09:00–17:00.
 * UML: Tutor ◆— Availability (composition). Only Tutor.addAvailability() creates these,
 * which is why the constructor is package-private: code outside aralinks.model can't make one.
 * Owner: Aifa. Status: DONE (worked example).
 */
public class Availability {
    private final DayOfWeek day;       // UML says String; DayOfWeek prevents typos like "Mnday"
    private final LocalTime startTime;
    private final LocalTime endTime;

    Availability(DayOfWeek day, LocalTime startTime, LocalTime endTime) {
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /** True if [start, end] on the given day fits completely inside this block. */
    public boolean isAvailable(DayOfWeek day, LocalTime start, LocalTime end) {
        return this.day == day && !start.isBefore(startTime) && !end.isAfter(endTime);
    }

    /** True if this block overlaps another one on the same day (used to stop duplicate hours). */
    public boolean overlaps(Availability other) {
        return day == other.day && startTime.isBefore(other.endTime) && other.startTime.isBefore(endTime);
    }

    public DayOfWeek getDay()       { return day; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime()   { return endTime; }

    @Override
    public String toString() {
        return day.toString().charAt(0) + day.toString().substring(1, 3).toLowerCase() + " " + startTime + "–" + endTime;
    }
}
