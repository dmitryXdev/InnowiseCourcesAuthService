package com.innowise.authservice.service;

import com.innowise.authservice.dao.UserRepository;
import com.innowise.authservice.dto.UpdateUserDto;
import com.innowise.authservice.dto.UserDto;
import com.innowise.authservice.mapper.UserMapper;
import com.innowise.authservice.model.User;
import com.innowise.authservice.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, passwordEncoder, userMapper);
    }

    @Test
    void updateUser_shouldUpdateAndReturnUser() {
        User user = new User();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        UpdateUserDto updateUserDto = new UpdateUserDto();
        updateUserDto.setEmail("new-email@gmail.com");

        UserDto userDto = userService.updateUser(0L, updateUserDto);

        assertNotNull(userDto);
        assertEquals(updateUserDto.getEmail(), user.getEmail());
    }

    @Test
    void deleteUserById_shouldDeleteUserById() {
        User user = new User();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        userService.deleteUserById(0L);

        verify(userRepository, times(1)).delete(any());
    }

    @Test
    void getUserById_shouldReturnUserById() {
        User user = new User();
        user.setId(0L);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        UserDto userDto = userService.getUserById(0L);

        assertNotNull(userDto);
        assertEquals(0L, userDto.getId());
    }
}
