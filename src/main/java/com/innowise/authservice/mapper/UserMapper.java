package com.innowise.authservice.mapper;

import com.innowise.authservice.dto.UserDto;
import com.innowise.authservice.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);
}
