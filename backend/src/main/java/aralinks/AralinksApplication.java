package aralinks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Login is handled by AuthService, so Spring's default in-memory user isn't wanted.
@SpringBootApplication(excludeName = "org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration")
public class AralinksApplication {
    public static void main(String[] args) {
        SpringApplication.run(AralinksApplication.class, args);
    }
}
