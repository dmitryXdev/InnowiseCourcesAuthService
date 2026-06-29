package com.innowise.authservice.service;

import com.innowise.authservice.dto.UpdateUserDto;
import com.innowise.authservice.dto.UserDto;

public interface UserService {
    UserDto updateUser(Long id, UpdateUserDto updateUserDto);
    void deleteUserById(Long id);
    UserDto getUserById(Long id);
}
