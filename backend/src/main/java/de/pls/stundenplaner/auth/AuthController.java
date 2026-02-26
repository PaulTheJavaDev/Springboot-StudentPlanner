package de.pls.stundenplaner.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import de.pls.stundenplaner.dto.request.auth.LoginRequest;
import de.pls.stundenplaner.dto.request.auth.RegisterRequest;
import de.pls.stundenplaner.util.exceptions.EmptyUsernameException;
import de.pls.stundenplaner.util.exceptions.InvalidLoginException;
import de.pls.stundenplaner.util.exceptions.UserAlreadyExistsException;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(
            final @NotNull @NonNull @RequestBody LoginRequest loginRequest,
            final @NotNull @NonNull HttpServletRequest request
    ) {
        try {
            authService.checkLogin(loginRequest);
        } catch (InvalidLoginException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User does not have access to this feature.");
        }

        final HttpSession session = request.getSession(true);
        session.setAttribute("AUTHENTICATED", true);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @RequestBody @Valid RegisterRequest registerRequest
    ) {

        try {
            authService.registerUser(registerRequest);
            return ResponseEntity.ok("User registered");
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists.");
        }
        catch (EmptyUsernameException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Username cannot be empty.");
        }
    }

    @GetMapping("/check")
    public ResponseEntity<String> checkSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session != null && Boolean.TRUE.equals(session.getAttribute("AUTHENTICATED"))) {
            return ResponseEntity.ok("{\"authenticated\":true}");
        }

        return ResponseEntity.ok("{\"authenticated\":false}");
    }
}