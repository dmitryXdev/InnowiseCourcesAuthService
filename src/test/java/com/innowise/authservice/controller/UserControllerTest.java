package com.innowise.authservice.controller;

import com.innowise.authservice.config.TestConfig;
import com.innowise.authservice.dao.UserRepository;
import com.innowise.authservice.dto.AuthResponseDto;
import com.innowise.authservice.dto.CreateUserDto;
import com.innowise.authservice.dto.UpdateUserDto;
import com.innowise.authservice.dto.UserDto;
import com.innowise.authservice.model.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
class UserControllerTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${internal.secret.header}")
    private String internalSecretHeader;

    @Value("${internal.secret}")
    private String secret;

    private String accessToken;

    @BeforeEach
    void registerUser() throws Exception {
        CreateUserDto register = new CreateUserDto();
        register.setLogin("Login");
        register.setPassword("12345password");
        register.setEmail("some@email.com");
        register.setRole(Role.USER.name());
        register.setId(0L);

        accessToken = "Bearer " + objectMapper.readValue(mockMvc.perform(post("/auth/register")
                        .header(internalSecretHeader, secret)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), AuthResponseDto.class).getAuthToken();
    }

    @AfterEach
    void clearDb() {
        userRepository.deleteAll();
    }

    @Test
    void getUserById_shouldReturnUserById() throws Exception {
        UserDto userDto = objectMapper.readValue(mockMvc.perform(get("/users/0")
                .header(HttpHeaders.AUTHORIZATION, accessToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), UserDto.class);

        assertNotNull(userDto);
        assertEquals(0L, userDto.getId());
    }

    @Test
    void getUserById_shouldThrowExceptionIfNotOwner() throws Exception {
        mockMvc.perform(get("/users/1")
                        .header(HttpHeaders.AUTHORIZATION, accessToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateUserById_shouldUpdateUserById() throws Exception {
        UserDto userDto = objectMapper.readValue(mockMvc.perform(get("/users/0")
                        .header(HttpHeaders.AUTHORIZATION, accessToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), UserDto.class);

        UpdateUserDto updateUserDto = new UpdateUserDto();
        updateUserDto.setPassword("newpassword");
        updateUserDto.setEmail("new-email@gmail.com");

        UserDto updated = objectMapper.readValue(mockMvc.perform(put("/users/0")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(updateUserDto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), UserDto.class);

        assertNotNull(updated);
        assertEquals(updated.getId(), userDto.getId());
        assertEquals(updateUserDto.getEmail(), updated.getEmail());
        assertTrue(passwordEncoder.matches(updateUserDto.getPassword(),
                userRepository.findById(updated.getId()).get().getPassword()));
    }

    @Test
    void deleteUserById_shouldDeleteUserById() throws Exception {
        UserDto userDto = objectMapper.readValue(mockMvc.perform(get("/users/0")
                        .header(HttpHeaders.AUTHORIZATION, accessToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), UserDto.class);

        assertNotNull(userDto);

        mockMvc.perform(delete("/users/0")
                .header(HttpHeaders.AUTHORIZATION, accessToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/users/0")
                        .header(HttpHeaders.AUTHORIZATION, accessToken))
                .andExpect(status().isBadRequest());

        assertNull(userRepository.findById(0L).orElse(null));
    }
}
