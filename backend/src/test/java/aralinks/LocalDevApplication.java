package aralinks;

import org.springframework.boot.SpringApplication;

/** Runs the API on a throwaway PostgreSQL with demo data: ./mvnw spring-boot:test-run */
public class LocalDevApplication {
    public static void main(String[] args) {
        System.setProperty("DATABASE_URL", LocalPostgres.jdbcUrl());
        System.setProperty("DATABASE_USERNAME", "postgres");
        System.setProperty("DATABASE_PASSWORD", "postgres");
        System.setProperty("JWT_SECRET", "local-development-only-secret-not-for-deployment");
        System.setProperty("SEED_DEMO_DATA", "true");
        System.setProperty("DEMO_PASSWORD", "demo-pass-123");
        SpringApplication.run(AralinksApplication.class, args);
    }
}
