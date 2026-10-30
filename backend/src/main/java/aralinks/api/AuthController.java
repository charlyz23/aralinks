package aralinks.api;

import aralinks.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class AuthController {
    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @GetMapping("/health")
    Map<String, String> health() {
        return Map.of("status", "ok");
    }

    @PostMapping("/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    Dto.AuthResponse register(@Valid @RequestBody Dto.RegisterRequest request) {
        return auth.register(request);
    }

    @PostMapping("/auth/login")
    Dto.AuthResponse login(@Valid @RequestBody Dto.LoginRequest request) {
        return auth.login(request);
    }

    @GetMapping("/auth/me")
    Dto.UserView me(@AuthenticationPrincipal UUID userId) {
        return auth.me(userId);
    }
}
