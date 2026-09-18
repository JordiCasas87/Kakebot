package com.jordi.kakebot.user.controller;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jordi.kakebot.common.config.TimeConfig;
import com.jordi.kakebot.user.dto.UserRegisterRequestDto;
import com.jordi.kakebot.user.dto.UserResponseDto;
import com.jordi.kakebot.user.enums.UserProvider;
import com.jordi.kakebot.user.exception.UserNotFoundException;
import com.jordi.kakebot.user.service.UserService;
import com.jordi.kakebot.user.service.UserTelegramLinkCodeService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@Import(TimeConfig.class)
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserTelegramLinkCodeService userTelegramLinkCodeService;

    @Test
    void registersUserFromHttpRequest() throws Exception {
        UserResponseDto response = new UserResponseDto(
                1L,
                "jordi",
                UserProvider.LOCAL,
                null,
                LocalDateTime.of(2026, 9, 18, 12, 0)
        );
        when(userService.register(new UserRegisterRequestDto("jordi", "secret1"))).thenReturn(response);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "jordi",
                                  "password": "secret1"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("jordi"))
                .andExpect(jsonPath("$.provider").value("LOCAL"));
    }

    @Test
    void rejectsInvalidRegistrationBeforeCallingService() throws Exception {
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "jo",
                                  "password": "123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Datos de entrada invalidos"))
                .andExpect(jsonPath("$.details.length()").value(2));

        verifyNoInteractions(userService, userTelegramLinkCodeService);
    }

    @Test
    void translatesMissingUserToNotFoundResponse() throws Exception {
        when(userService.getMe(99L)).thenThrow(new UserNotFoundException(99L));

        mockMvc.perform(get("/api/users/me").header("X-User-Id", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/users/me"));
    }
}
