package com.fesi.deadlinemate.domain.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fesi.deadlinemate.domain.auth.client.OAuthClient;
import com.fesi.deadlinemate.domain.auth.client.OAuthClientFactory;
import com.fesi.deadlinemate.domain.auth.client.dto.OAuthUserInfo;
import com.fesi.deadlinemate.domain.user.entity.Provider;
import com.fesi.deadlinemate.domain.user.entity.User;
import com.fesi.deadlinemate.domain.user.repository.UserRepository;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerE2ETest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @MockBean private OAuthClientFactory oAuthClientFactory;

    @Nested
    @DisplayName("POST /api/v1/auth/register")
    class Register {

        @Test
        @DisplayName("회원가입 성공 — accessToken과 refreshToken을 반환한다")
        void registerSuccess() throws Exception {
            Map<String, String> body = Map.of(
                    "email", "newuser@test.com",
                    "password", "Test1234!",
                    "nickname", "신규유저"
            );

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                    .andExpect(jsonPath("$.data.email").value("newuser@test.com"))
                    .andExpect(jsonPath("$.data.nickname").value("신규유저"));

            assertThat(userRepository.findByEmail("newuser@test.com")).isPresent();
        }

        @Test
        @DisplayName("중복 이메일로 회원가입 시 409를 반환한다")
        void duplicateEmail() throws Exception {
            Map<String, String> body = Map.of(
                    "email", "dup@test.com",
                    "password", "Test1234!",
                    "nickname", "첫번째"
            );
            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isCreated());

            Map<String, String> body2 = Map.of(
                    "email", "dup@test.com",
                    "password", "Test1234!",
                    "nickname", "두번째"
            );
            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body2)))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/login")
    class Login {

        @Test
        @DisplayName("로그인 성공 — accessToken과 refreshToken을 반환한다")
        void loginSuccess() throws Exception {
            // 유저를 직접 저장 (register를 거치면 같은 초에 refresh token 중복 발생 가능)
            userRepository.save(User.builder()
                    .email("login@test.com")
                    .passwordHash(passwordEncoder.encode("Test1234!"))
                    .nickname("로그인테스터")
                    .provider(Provider.EMAIL)
                    .build());

            Map<String, String> loginBody = Map.of(
                    "email", "login@test.com",
                    "password", "Test1234!"
            );
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginBody)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
        }

        @Test
        @DisplayName("잘못된 비밀번호로 로그인 시 401을 반환한다")
        void wrongPassword() throws Exception {
            userRepository.save(User.builder()
                    .email("wrongpw@test.com")
                    .passwordHash(passwordEncoder.encode("Test1234!"))
                    .nickname("테스터")
                    .provider(Provider.EMAIL)
                    .build());

            Map<String, String> loginBody = Map.of(
                    "email", "wrongpw@test.com",
                    "password", "WrongPass99!"
            );
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginBody)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 로그인 시 401을 반환한다")
        void userNotFound() throws Exception {
            Map<String, String> loginBody = Map.of(
                    "email", "notexist@test.com",
                    "password", "Test1234!"
            );
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginBody)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/auth/kakao/callback")
    class KakaoCallback {

        @Test
        @DisplayName("신규 카카오 유저 — accessToken과 isNewUser=true를 반환한다")
        void kakaoNewUser() throws Exception {
            OAuthClient mockKakaoClient = Mockito.mock(OAuthClient.class);
            given(oAuthClientFactory.getClient(Provider.KAKAO)).willReturn(mockKakaoClient);
            given(mockKakaoClient.getAccessToken(anyString(), anyString())).willReturn("kakao-access-token");
            given(mockKakaoClient.getUserInfo("kakao-access-token")).willReturn(
                    OAuthUserInfo.builder()
                            .providerId("12345")
                            .email("kakaouser@kakao.com")
                            .nickname("카카오유저")
                            .profileImage(null)
                            .provider(Provider.KAKAO)
                            .build()
            );

            mockMvc.perform(get("/api/v1/auth/kakao/callback")
                            .param("code", "auth-code-from-kakao"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                    .andExpect(jsonPath("$.data.newUser").value(true));

            Optional<User> user = userRepository.findByEmail("kakaouser@kakao.com");
            assertThat(user).isPresent();
            assertThat(user.get().getProvider()).isEqualTo(Provider.KAKAO);
        }

        @Test
        @DisplayName("기존 카카오 유저 — isNewUser=false를 반환한다")
        void kakaoExistingUser() throws Exception {
            // 미리 카카오 유저를 저장해두면 콜백 시 isNewUser=false 반환
            userRepository.save(User.builder()
                    .email("kakaoexist@kakao.com")
                    .nickname("기존카카오유저")
                    .provider(Provider.KAKAO)
                    .providerId("exist-99999")
                    .build());

            OAuthClient mockKakaoClient = Mockito.mock(OAuthClient.class);
            given(oAuthClientFactory.getClient(Provider.KAKAO)).willReturn(mockKakaoClient);
            given(mockKakaoClient.getAccessToken(anyString(), anyString())).willReturn("kakao-access-token");
            given(mockKakaoClient.getUserInfo("kakao-access-token")).willReturn(
                    OAuthUserInfo.builder()
                            .providerId("exist-99999")
                            .email("kakaoexist@kakao.com")
                            .nickname("기존카카오유저")
                            .profileImage(null)
                            .provider(Provider.KAKAO)
                            .build()
            );

            mockMvc.perform(get("/api/v1/auth/kakao/callback")
                            .param("code", "some-code"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.newUser").value(false));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/auth/google/callback")
    class GoogleCallback {

        @Test
        @DisplayName("신규 구글 유저 — accessToken과 isNewUser=true를 반환한다")
        void googleNewUser() throws Exception {
            OAuthClient mockGoogleClient = Mockito.mock(OAuthClient.class);
            given(oAuthClientFactory.getClient(Provider.GOOGLE)).willReturn(mockGoogleClient);
            given(mockGoogleClient.getAccessToken(anyString(), anyString())).willReturn("google-access-token");
            given(mockGoogleClient.getUserInfo("google-access-token")).willReturn(
                    OAuthUserInfo.builder()
                            .providerId("google-sub-99999")
                            .email("googleuser@gmail.com")
                            .nickname("구글유저")
                            .profileImage(null)
                            .provider(Provider.GOOGLE)
                            .build()
            );

            mockMvc.perform(get("/api/v1/auth/google/callback")
                            .param("code", "auth-code-from-google"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                    .andExpect(jsonPath("$.data.newUser").value(true));

            Optional<User> user = userRepository.findByEmail("googleuser@gmail.com");
            assertThat(user).isPresent();
            assertThat(user.get().getProvider()).isEqualTo(Provider.GOOGLE);
        }

        @Test
        @DisplayName("기존 구글 유저 — isNewUser=false를 반환한다")
        void googleExistingUser() throws Exception {
            // 미리 구글 유저를 저장해두면 콜백 시 isNewUser=false 반환
            userRepository.save(User.builder()
                    .email("googleexist@gmail.com")
                    .nickname("기존구글유저")
                    .provider(Provider.GOOGLE)
                    .providerId("google-exist-sub")
                    .build());

            OAuthClient mockGoogleClient = Mockito.mock(OAuthClient.class);
            given(oAuthClientFactory.getClient(Provider.GOOGLE)).willReturn(mockGoogleClient);
            given(mockGoogleClient.getAccessToken(anyString(), anyString())).willReturn("google-access-token");
            given(mockGoogleClient.getUserInfo("google-access-token")).willReturn(
                    OAuthUserInfo.builder()
                            .providerId("google-exist-sub")
                            .email("googleexist@gmail.com")
                            .nickname("기존구글유저")
                            .profileImage(null)
                            .provider(Provider.GOOGLE)
                            .build()
            );

            mockMvc.perform(get("/api/v1/auth/google/callback")
                            .param("code", "some-code"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.newUser").value(false));
        }
    }
}
