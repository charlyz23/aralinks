package aralinks.model;

/**
 * A course or skill a tutor can teach, grouped by category.
 * UML: Course — teaches (Tutor ◇ Course), covers (TutoringClass → Course, TutoringSession → Course).
 * Owner: Charlyz. Status: DONE (worked example of a simple encapsulated class).
 */
public class Course {
    private final String courseId;
    private final String name;
    private final String category;   // e.g. "Computer Science/IT", "Engineering", "Architecture"
    private final String type;       // "Course" or "Skill"

    public Course(String courseId, String name, String category, String type) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Course name is required");
        this.courseId = courseId;
        this.name = name;
        this.category = category;
        this.type = type;
    }

    public String getCourseId() { return courseId; }
    public String getName()     { return name; }
    public String getCategory() { return category; }
    public String getType()     { return type; }

    @Override
    public String toString() {
        return name + " (" + category + ", " + type + ")";
    }
}
