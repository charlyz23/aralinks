package aralinks.service;

import aralinks.api.Dto;
import aralinks.repository.UserRepository;
import aralinks.security.JwtService;
import aralinks.users.Role;
import aralinks.users.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Transactional
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    public Dto.AuthResponse register(Dto.RegisterRequest r) {
        if (r.roles().contains(Role.ADMIN)) {
            throw new IllegalArgumentException("Admin accounts cannot be self-registered.");
        }
        String email = r.email().toLowerCase();
        if (users.existsByEmail(email)) {
            throw new IllegalStateException("That email is already registered.");
        }
        if (users.existsBySrCode(r.srCode())) {
            throw new IllegalStateException("That SR code is already registered.");
        }
        User user = new User(r.srCode(), r.name(), email, encoder.encode(r.password()),
                r.university(), r.roles());
        user.setLearnerDetails(r.yearLevel(), r.program());
        if (user.hasRole(Role.TUTOR)) {
            user.setTutorDetails(r.hourlyRate() == null ? BigDecimal.ZERO : r.hourlyRate(), r.bio());
        }
        users.save(user);
        return new Dto.AuthResponse(jwt.issue(user), Dto.UserView.of(user));
    }

    @Transactional(readOnly = true)
    public Dto.AuthResponse login(Dto.LoginRequest r) {
        User user = users.findByEmail(r.email().toLowerCase())
                .filter(User::isActive)
                .filter(u -> encoder.matches(r.password(), u.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Invalid email or password."));
        return new Dto.AuthResponse(jwt.issue(user), Dto.UserView.of(user));
    }

    @Transactional(readOnly = true)
    public Dto.UserView me(UUID userId) {
        return users.findById(userId).map(Dto.UserView::of)
                .orElseThrow(() -> new NotFoundException("User not found."));
    }
}
