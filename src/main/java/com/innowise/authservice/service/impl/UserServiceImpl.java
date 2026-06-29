package com.innowise.authservice.service.impl;

import com.innowise.authservice.dao.UserRepository;
import com.innowise.authservice.dto.UpdateUserDto;
import com.innowise.authservice.dto.UserDto;
import com.innowise.authservice.exception.BadIncomingDataException;
import com.innowise.authservice.exception.EntityNotFoundException;
import com.innowise.authservice.mapper.UserMapper;
import com.innowise.authservice.model.User;
import com.innowise.authservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    private static final String USER_NOT_FOUND_MESSAGE = "User not found";

    @Override
    @Transactional
    public UserDto updateUser(Long id, UpdateUserDto updateUserDto) {
        if(updateUserDto == null || (updateUserDto.getPassword() == null && updateUserDto.getEmail() == null)) {
            throw new BadIncomingDataException("No data present");
        }

        User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND_MESSAGE));

        if(updateUserDto.getEmail() != null) {
            user.setEmail(updateUserDto.getEmail());
        }
        if(updateUserDto.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(updateUserDto.getPassword()));
        }

        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public void deleteUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND_MESSAGE));

        userRepository.delete(user);
    }

    @Override
    public UserDto getUserById(Long id) {
        return userMapper.toDto(userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND_MESSAGE)));
    }
}
