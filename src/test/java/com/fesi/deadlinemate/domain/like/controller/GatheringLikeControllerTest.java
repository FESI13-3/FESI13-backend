package com.fesi.deadlinemate.domain.like.controller;


import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fesi.deadlinemate.domain.gathering.dto.response.GatheringListItemResponse;
import com.fesi.deadlinemate.domain.gathering.dto.response.GatheringListResponse;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringStatus;
import com.fesi.deadlinemate.domain.gathering.service.GatheringQueryService;
import com.fesi.deadlinemate.domain.like.service.GatheringLikeService;
import com.fesi.deadlinemate.global.security.JwtTokenProvider;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class GatheringLikeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private GatheringLikeService gatheringLikeService;

    @MockitoBean
    private GatheringQueryService gatheringQueryService;

    private String token;
    private List<Long> likedIds;
    private GatheringListResponse likedGatheringsResponse;

    @BeforeEach
    void setUp() {
        token = jwtTokenProvider.generateAccessToken(1L, "user1@test.com");
        likedIds = List.of(3L, 1L, 2L);
        likedGatheringsResponse = createLikedGatheringsResponse();
    }

    @Nested
    @DisplayName("GET /api/v1/users/me/likes/ids")
    class GetLikedGatheringIds {

        @Test
        @DisplayName("인증된 사용자는 찜한 모임 ID 목록을 조회할 수 있다")
        void getLikedGatheringIds_success() throws Exception {
            given(gatheringLikeService.getLikedGatheringIds(1L)).willReturn(likedIds);

            mockMvc.perform(get("/api/v1/users/me/likes/ids")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0]").value(3))
                    .andExpect(jsonPath("$.data[1]").value(1))
                    .andExpect(jsonPath("$.data[2]").value(2));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/users/me/likes")
    class GetMyLikedGatherings {

        @Test
        @DisplayName("인증된 사용자는 찜한 모임 목록을 조회할 수 있다")
        void getMyLikedGatherings_success() throws Exception {
            given(gatheringQueryService.getMyLikedGatherings(1L, 1, 20))
                    .willReturn(likedGatheringsResponse);

            mockMvc.perform(get("/api/v1/users/me/likes")
                            .header("Authorization", "Bearer " + token)
                            .param("page", "1")
                            .param("limit", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.gatherings[0].id").value(1))
                    .andExpect(jsonPath("$.data.gatherings[0].title").value("React 스터디"))
                    .andExpect(jsonPath("$.data.totalCount").value(1))
                    .andExpect(jsonPath("$.data.currentPage").value(1));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/gatherings/{gatheringId}/likes")
    class Like {

        @Test
        @DisplayName("인증된 사용자는 모임을 찜할 수 있다")
        void like_success() throws Exception {
            doNothing().when(gatheringLikeService).like(1L, 1L);

            mockMvc.perform(post("/api/v1/gatherings/{gatheringId}/likes", 1L)
                            .header("Authorization", "Bearer " + token)
                            .with(csrf()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("인증 없이 찜하기 시 403을 반환한다")
        void like_withoutAuth_forbidden() throws Exception {
            mockMvc.perform(post("/api/v1/gatherings/{gatheringId}/likes", 1L)
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }

    private GatheringListResponse createLikedGatheringsResponse() {
        return GatheringListResponse.builder()
                .gatherings(List.of(
                        GatheringListItemResponse.builder()
                                .id(1L)
                                .type("스터디")
                                .categories(List.of("개발", "웹"))
                                .title("React 스터디")
                                .shortDescription("리액트 같이 공부해요")
                                .tags(List.of("React", "프론트엔드"))
                                .maxMembers(6)
                                .currentMembers(3)
                                .recruitDeadline(LocalDate.of(2026, 4, 20))
                                .startDate(LocalDate.of(2026, 4, 21))
                                .endDate(LocalDate.of(2026, 5, 20))
                                .status(GatheringStatus.RECRUITING)
                                .leader(GatheringListItemResponse.LeaderSummary.builder()
                                        .id(10L)
                                        .nickname("모임장")
                                        .profileImage("https://profile.test/10.png")
                                        .build())
                                .build()
                ))
                .totalCount(1)
                .totalPages(1)
                .currentPage(1)
                .build();
    }
}