package de.pls.stundenplaner.util;

import de.pls.stundenplaner.auth.User;
import de.pls.stundenplaner.auth.UserRepository;
import de.pls.stundenplaner.util.exceptions.EmptyUsernameException;
import de.pls.stundenplaner.util.exceptions.InvalidSessionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserUtilTest {

    @Mock
    private UserRepository userRepository;

    private User user;

    private UUID validSessionID;

    @SuppressWarnings("all")
    @BeforeEach
    void setUp() throws EmptyUsernameException {
        new UserUtil(userRepository);
        validSessionID = UUID.randomUUID();
        user = new User("user", "hash");
    }

    @Test
    void checkUserExistenceBySessionID_returnsUser_whenSessionIsValid() throws InvalidSessionException {
        when(userRepository.findBySessionID(validSessionID)).thenReturn(Optional.of(user));

        User result = UserUtil.checkUserExistenceBySessionID(validSessionID);

        assertThat(result).isEqualTo(user);
        verify(userRepository).findBySessionID(validSessionID);
    }

    @Test
    void checkUserExistenceBySessionID_throwsInvalidSessionException_whenUserNotFound() {
        when(userRepository.findBySessionID(validSessionID)).thenReturn(Optional.empty());

        assertThrows(InvalidSessionException.class,
                () -> UserUtil.checkUserExistenceBySessionID(validSessionID));

        verify(userRepository).findBySessionID(validSessionID);
    }

}