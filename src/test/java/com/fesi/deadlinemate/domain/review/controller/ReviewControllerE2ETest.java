package com.fesi.deadlinemate.domain.review.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fesi.deadlinemate.domain.gathering.entity.Gathering;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringMember;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringRole;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringStatus;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringType;
import com.fesi.deadlinemate.domain.gathering.repository.GatheringMemberRepository;
import com.fesi.deadlinemate.domain.gathering.repository.GatheringRepository;
import com.fesi.deadlinemate.domain.review.entity.Review;
import com.fesi.deadlinemate.domain.review.entity.ReviewTag;
import com.fesi.deadlinemate.domain.review.repository.ReviewRepository;
import com.fesi.deadlinemate.domain.user.entity.Provider;
import com.fesi.deadlinemate.domain.user.entity.User;
import com.fesi.deadlinemate.domain.user.repository.UserRepository;
import com.fesi.deadlinemate.global.security.JwtTokenProvider;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ReviewControllerE2ETest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtTokenProvider jwtTokenProvider;
    @Autowired private UserRepository userRepository;
    @Autowired private ReviewRepository reviewRepository;
    @Autowired private GatheringRepository gatheringRepository;
    @Autowired private GatheringMemberRepository gatheringMemberRepository;

    private User reviewer;
    private User target;
    private Gathering gathering;

    @BeforeEach
    void setUp() {
        reviewer = userRepository.save(User.builder()
                .email("reviewer@test.com").passwordHash("hash")
                .nickname("리뷰어").provider(Provider.EMAIL).build());

        target = userRepository.save(User.builder()
                .email("target@test.com").passwordHash("hash")
                .nickname("대상자").provider(Provider.EMAIL).build());

        gathering = gatheringRepository.save(Gathering.builder()
                .leaderId(reviewer.getId()).type(GatheringType.STUDY)
                .title("테스트 모임").shortDescription("설명")
                .description("상세설명").goal("목표").maxMembers(10).currentMembers(2)
                .recruitDeadline(LocalDate.now().minusDays(14))
                .startDate(LocalDate.now().minusDays(7))
                .endDate(LocalDate.now().minusDays(1))
                .totalWeeks(1).status(GatheringStatus.COMPLETED).viewCount(0).build());

        gatheringMemberRepository.save(GatheringMember.builder()
                .gatheringId(gathering.getId()).userId(reviewer.getId())
                .role(GatheringRole.LEADER).isActive(true)
.build());

        gatheringMemberRepository.save(GatheringMember.builder()
                .gatheringId(gathering.getId()).userId(target.getId())
                .role(GatheringRole.MEMBER).isActive(true)
.build());
    }

    @Nested
    @DisplayName("GET /api/v1/users/{userId}/reviews")
    class GetReviews {

        @Test
        @DisplayName("matesTag와 matesTagCounts를 포함하여 반환한다")
        void getReviewsWithMatesTagCounts() throws Exception {
            reviewRepository.save(Review.create(
                    gathering.getId(), reviewer.getId(), target.getId(),
                    List.of(ReviewTag.DILIGENT.getDisplayName()), "불꽃", "좋았어요"
            ));
            reviewRepository.save(Review.create(
                    gathering.getId() + 1, reviewer.getId() + 1, target.getId(),
                    List.of(ReviewTag.PUNCTUAL.getDisplayName()), "불꽃", null
            ));

            mockMvc.perform(get("/api/v1/users/{userId}/reviews", target.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalCount").value(2))
                    .andExpect(jsonPath("$.data.reviews[0].matesTag").value("불꽃"))
                    .andExpect(jsonPath("$.data.matesTagCounts[0].tag").value("불꽃"))
                    .andExpect(jsonPath("$.data.matesTagCounts[0].count").value(2));
        }

        @Test
        @DisplayName("matesTag가 null인 리뷰는 matesTagCounts에 집계되지 않는다")
        void nullMatesTagNotCounted() throws Exception {
            reviewRepository.save(Review.create(
                    gathering.getId(), reviewer.getId(), target.getId(),
                    List.of(ReviewTag.DILIGENT.getDisplayName()), null, null
            ));

            mockMvc.perform(get("/api/v1/users/{userId}/reviews", target.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.reviews[0].matesTag").isEmpty())
                    .andExpect(jsonPath("$.data.matesTagCounts").isEmpty());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/gatherings/{gatheringId}/reviews")
    class CreateReviews {

        @Test
        @DisplayName("matesTag를 포함하여 리뷰를 작성한다")
        void createReviewWithMatesTag() throws Exception {
            String token = jwtTokenProvider.generateAccessToken(reviewer.getId(), reviewer.getEmail());

            Map<String, Object> body = Map.of("reviews", List.of(
                    Map.of("targetUserId", target.getId(),
                            "tags", List.of("성실해요"),
                            "matesTag", "불꽃")
            ));

            mockMvc.perform(post("/api/v1/gatherings/{gatheringId}/reviews", gathering.getId())
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isCreated());

            Review saved = reviewRepository
                    .findByTargetUserIdOrderByCreatedAtDesc(target.getId(),
                            org.springframework.data.domain.PageRequest.of(0, 1))
                    .getContent().get(0);
            org.assertj.core.api.Assertions.assertThat(saved.getMatesTag()).isEqualTo("불꽃");
        }

        @Test
        @DisplayName("인증 없이 리뷰 작성 시 403을 반환한다")
        void createReviewWithoutAuth() throws Exception {
            Map<String, Object> body = Map.of("reviews", List.of(
                    Map.of("targetUserId", target.getId(), "tags", List.of("성실해요"))
            ));

            mockMvc.perform(post("/api/v1/gatherings/{gatheringId}/reviews", gathering.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isForbidden());
        }
    }
}
