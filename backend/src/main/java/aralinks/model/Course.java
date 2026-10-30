package aralinks.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "courses")
public class Course extends BaseEntity {
    private String code;
    private String name;
    private String category;
    private String type;

    protected Course() {
    }

    public Course(String code, String name, String category, String type) {
        this.code = code;
        this.name = name;
        this.category = category;
        this.type = type;
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getType() { return type; }
}
