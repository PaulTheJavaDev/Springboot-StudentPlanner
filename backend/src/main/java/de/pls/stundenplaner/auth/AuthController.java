package de.pls.stundenplaner.auth;

import de.pls.stundenplaner.dto.request.auth.*;
import de.pls.stundenplaner.dto.response.auth.LoginResponse;
import de.pls.stundenplaner.dto.response.auth.RegisterResponse;
import de.pls.stundenplaner.util.exceptions.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.NonNull;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            final @NonNull @RequestBody @Valid LoginRequest loginRequest,
            final @NonNull HttpServletRequest request
    ) {
        try {
            final LoginResponse loginResponse = authService.checkLogin(loginRequest);

            final User user = userRepository.findByUsername(loginRequest.username())
                    .orElseThrow(InvalidLoginException::new);

            final HttpSession session = request.getSession(true);
            session.setAttribute("AUTHENTICATED", true);
            session.setAttribute("USER_UUID", user.getUserUUID());

            return new ResponseEntity<>(loginResponse, HttpStatus.OK);

        } catch (InvalidLoginException | NoSuchAlgorithmException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            final @NonNull @RequestBody @Valid RegisterRequest registerRequest
    ) {
        try {
            RegisterResponse registerResponse = authService.registerUser(registerRequest);
            return new ResponseEntity<>(registerResponse, HttpStatus.OK);
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (EmptyUsernameException | NoSuchAlgorithmException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> checkSession(
            final @NonNull HttpServletRequest request
    ) {

        final HttpSession session = request.getSession(false);

        if (session != null && Boolean.TRUE.equals(session.getAttribute("AUTHENTICATED"))) {
            return ResponseEntity.ok(true);
        }

        return ResponseEntity.ok(false);
    }
}