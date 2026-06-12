package com.example.devforge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.devforge.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AuthPersistenceTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void signupPersistsUserAndAllowsLogin() throws Exception {
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "persisted@example.com",
                                  "password": "password123",
                                  "userName": "persisted",
                                  "coverPictureUrl": "https://example.com/user-cover.jpg",
                                  "isPrivate": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.jwt").isString());

        assertThat(userRepository.findByEmail("persisted@example.com"))
                .get()
                .extracting(user -> user.getCoverPictureUrl())
                .isEqualTo("https://example.com/user-cover.jpg");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "persisted@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.jwt").isString());

        Long userId = userRepository.findByEmail("persisted@example.com")
                .orElseThrow()
                .getId();

        mockMvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userName").value("persisted"))
                .andExpect(jsonPath("$.data.coverPictureUrl").value("https://example.com/user-cover.jpg"));
    }

    @Test
    void communityBannerIsReturnedAndOnlyAdminsCanManageIt() throws Exception {
        String signupResponse = mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "community-owner@example.com",
                                  "password": "password123",
                                  "userName": "communityowner",
                                  "isPrivate": false
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode signupData = objectMapper.readTree(signupResponse).path("data");
        String token = signupData.path("jwt").asText();
        long userId = signupData.path("id").asLong();

        String createResponse = mockMvc.perform(post("/users/{userId}/communities", userId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Cover Test Community",
                                  "description": "Tests community banner support",
                                  "bannerUrl": "https://example.com/community-banner.jpg",
                                  "privacy": "PUBLIC"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bannerUrl").value("https://example.com/community-banner.jpg"))
                .andExpect(jsonPath("$.data.canManage").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long communityId = objectMapper.readTree(createResponse).path("data").path("id").asLong();

        mockMvc.perform(get("/communities/{communityId}", communityId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bannerUrl").value("https://example.com/community-banner.jpg"))
                .andExpect(jsonPath("$.data.canManage").value(false));
    }
}
