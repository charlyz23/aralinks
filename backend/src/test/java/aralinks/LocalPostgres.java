package aralinks;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;

import java.io.IOException;
import java.io.UncheckedIOException;

/** One throwaway real PostgreSQL per JVM, shared by tests and the local dev runner. */
final class LocalPostgres {
    private static EmbeddedPostgres instance;

    private LocalPostgres() {
    }

    static synchronized String jdbcUrl() {
        if (instance == null) {
            try {
                instance = EmbeddedPostgres.builder().start();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return instance.getJdbcUrl("postgres", "postgres");
    }
}
