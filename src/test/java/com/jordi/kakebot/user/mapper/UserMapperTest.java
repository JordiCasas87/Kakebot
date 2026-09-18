package com.jordi.kakebot.user.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.jordi.kakebot.user.dto.UserResponseDto;
import com.jordi.kakebot.user.enums.UserProvider;
import com.jordi.kakebot.user.model.User;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class UserMapperTest {

    private final UserMapper mapper = new UserMapper();

    @Test
    void toResponseDtoMapsEveryPublicUserFieldWithoutExposingPassword() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 3, 18, 12, 30);
        User user = new User(UserProvider.LOCAL, "jordi", "secret-hash", "123456", createdAt);

        UserResponseDto result = mapper.toResponseDto(user);

        assertThat(result.id()).isNull();
        assertThat(result.username()).isEqualTo("jordi");
        assertThat(result.provider()).isEqualTo(UserProvider.LOCAL);
        assertThat(result.externalId()).isEqualTo("123456");
        assertThat(result.createdAt()).isEqualTo(createdAt);
        assertThat(result).hasNoNullFieldsOrPropertiesExcept("id");
    }
}
