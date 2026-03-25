package com.jordi.kakebot.user.mapper;

import com.jordi.kakebot.user.dto.UserResponseDto;
import com.jordi.kakebot.user.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponseDto toResponseDto(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getProvider(),
                user.getExternalId(),
                user.getCreatedAt()
        );
    }
}
