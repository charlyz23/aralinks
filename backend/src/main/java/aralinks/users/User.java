package aralinks.users;

import aralinks.model.Availability;
import aralinks.model.BaseEntity;
import aralinks.model.Course;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** One account per person; being a learner, tutor or admin is a role, so a student can be both. */
@Entity
@Table(name = "users")
public class User extends BaseEntity {
    private String srCode;
    private String name;
    private String email;
    private String passwordHash;
    private String university;
    private boolean active = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = EnumSet.noneOf(Role.class);

    // Learner details
    private Integer yearLevel;
    private String program;

    // Tutor details
    private BigDecimal hourlyRate;
    private String bio;

    @ManyToMany
    @JoinTable(name = "tutor_courses",
            joinColumns = @JoinColumn(name = "tutor_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id"))
    private Set<Course> courses = new HashSet<>();

    @OneToMany(mappedBy = "tutor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Availability> availableSchedules = new ArrayList<>();

    protected User() {
    }

    public User(String srCode, String name, String email, String passwordHash,
                String university, Set<Role> roles) {
        this.srCode = srCode;
        this.name = name;
        this.email = email.toLowerCase();
        this.passwordHash = passwordHash;
        this.university = university;
        this.roles.addAll(roles);
    }

    public boolean hasRole(Role role) {
        return roles.contains(role);
    }

    public void setLearnerDetails(Integer yearLevel, String program) {
        this.yearLevel = yearLevel;
        this.program = program;
    }

    public void setTutorDetails(BigDecimal hourlyRate, String bio) {
        if (hourlyRate == null || hourlyRate.signum() < 0) {
            throw new IllegalArgumentException("Rate cannot be negative.");
        }
        this.hourlyRate = hourlyRate;
        this.bio = bio;
    }

    public void addCourse(Course course) {
        courses.add(course);
    }

    public void removeCourse(Course course) {
        courses.remove(course);
    }

    public boolean teaches(Course course) {
        return courses.contains(course);
    }

    public void addAvailability(Availability availability) {
        availableSchedules.add(availability);
    }

    public void removeAvailability(Availability availability) {
        availableSchedules.remove(availability);
    }

    public boolean isAvailable(LocalDate date, LocalTime start, LocalTime end) {
        return availableSchedules.stream().anyMatch(a -> a.covers(date, start, end));
    }

    public void setActive(boolean active) { this.active = active; }

    public String getSrCode() { return srCode; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getUniversity() { return university; }
    public boolean isActive() { return active; }
    public Set<Role> getRoles() { return roles; }
    public Integer getYearLevel() { return yearLevel; }
    public String getProgram() { return program; }
    public BigDecimal getHourlyRate() { return hourlyRate; }
    public String getBio() { return bio; }
    public Set<Course> getCourses() { return courses; }
    public List<Availability> getAvailableSchedules() { return availableSchedules; }
}
