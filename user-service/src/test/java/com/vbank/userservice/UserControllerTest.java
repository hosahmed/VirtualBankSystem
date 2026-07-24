package com.vbank.userservice;

import com.vbank.userservice.controller.UserController;
import com.vbank.userservice.dto.response.UserProfileResponse;
import com.vbank.userservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class UserControllerTest {

    private final UserService userService = Mockito.mock(UserService.class);
    private final UserController controller = new UserController(userService);

    @Test
    void getProfile_shouldReturnProfile_whenUserIdExists() {
        UUID userId = UUID.randomUUID();

        UserProfileResponse expected = UserProfileResponse.builder()
                .userId(userId)
                .username("john.doe")
                .email("john.doe@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();
        when(userService.getProfile(userId)).thenReturn(expected);

        UserProfileResponse response = controller.getProfile(userId).getBody();

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("john.doe");
        assertThat(response.getEmail()).isEqualTo("john.doe@example.com");
    }
}
