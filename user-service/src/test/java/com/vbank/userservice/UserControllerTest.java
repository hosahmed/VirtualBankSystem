package com.vbank.userservice;

import com.vbank.userservice.controller.UserController;
import com.vbank.userservice.dto.response.UserProfileResponse;
import com.vbank.userservice.exception.ForbiddenException;
import com.vbank.userservice.security.GatewayAuthInterceptor;
import com.vbank.userservice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

class UserControllerTest {

    private final UserService userService = Mockito.mock(UserService.class);
    private final UserController controller = new UserController(userService);

    @Test
    void getProfile_shouldReturnProfile_whenAuthenticatedUserMatchesRequestedProfile() {
        UUID userId = UUID.randomUUID();
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getAttribute(GatewayAuthInterceptor.AUTHENTICATED_USER_ID_ATTR)).thenReturn(userId);

        UserProfileResponse expected = UserProfileResponse.builder()
                .userId(userId)
                .username("john.doe")
                .build();
        when(userService.getProfile(userId)).thenReturn(expected);

        UserProfileResponse response = controller.getProfile(userId, request).getBody();

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("john.doe");
    }

    @Test
    void getProfile_shouldThrowForbidden_whenAuthenticatedUserRequestsSomeoneElsesProfile() {
        UUID requestedUserId = UUID.randomUUID();
        UUID authenticatedUserId = UUID.randomUUID(); // different from requestedUserId

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getAttribute(GatewayAuthInterceptor.AUTHENTICATED_USER_ID_ATTR)).thenReturn(authenticatedUserId);

        assertThatThrownBy(() -> controller.getProfile(requestedUserId, request))
                .isInstanceOf(ForbiddenException.class);

        // Confirms we reject BEFORE calling the service - no data
        // lookup should happen for a request we're about to deny.
        Mockito.verifyNoInteractions(userService);
    }
}
