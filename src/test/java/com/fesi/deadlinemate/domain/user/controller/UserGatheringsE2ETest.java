package com.fesi.deadlinemate.domain.user.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fesi.deadlinemate.domain.gathering.entity.Gathering;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringMember;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringRole;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringStatus;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringType;
import com.fesi.deadlinemate.domain.gathering.repository.GatheringMemberRepository;
import com.fesi.deadlinemate.domain.gathering.repository.GatheringRepository;
import com.fesi.deadlinemate.domain.gatheringApplication.entity.ApplicationStatus;
import com.fesi.deadlinemate.domain.gatheringApplication.entity.GatheringApplication;
import com.fesi.deadlinemate.domain.gatheringApplication.repository.GatheringApplicationRepository;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserGatheringsE2ETest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtTokenProvider jwtTokenProvider;
    @Autowired private UserRepository userRepository;
    @Autowired private GatheringRepository gatheringRepository;
    @Autowired private GatheringMemberRepository gatheringMemberRepository;
    @Autowired private GatheringApplicationRepository gatheringApplicationRepository;
    @Autowired private ReviewRepository reviewRepository;

    private User user;
    private User applicant;
    private Gathering gathering;
    private String token;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder()
                .email("user@test.com").passwordHash("hash")
                .nickname("테스터").provider(Provider.EMAIL)
                .build());

        applicant = userRepository.save(User.builder()
                .email("applicant@test.com").passwordHash("hash")
                .nickname("신청자").provider(Provider.EMAIL)
                .build());

        gathering = gatheringRepository.save(Gathering.builder()
                .leaderId(user.getId()).type(GatheringType.STUDY)
                .category("개발").title("테스트 모임").shortDescription("설명")
                .description("상세설명").goal("목표").maxMembers(10).currentMembers(2)
                .recruitDeadline(LocalDate.now().minusDays(14))
                .startDate(LocalDate.now().minusDays(7))
                .endDate(LocalDate.now().minusDays(1))
                .totalWeeks(1).status(GatheringStatus.COMPLETED).viewCount(0).build());

        gatheringMemberRepository.save(GatheringMember.builder()
                .gatheringId(gathering.getId()).userId(user.getId())
                .role(GatheringRole.LEADER).isActive(true)
                .overallAchievementRate(BigDecimal.ZERO).build());

        token = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
    }

    @Test
    @DisplayName("hasReviewed — 리뷰 작성 모임은 true, 미작성은 false를 반환한다")
    void hasReviewed() throws Exception {
        reviewRepository.save(Review.create(
                gathering.getId(), user.getId(), applicant.getId(),
                List.of(ReviewTag.DILIGENT.getDisplayName()), null, null
        ));

        mockMvc.perform(get("/api/v1/users/me/gatherings")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.gatherings[0].hasReviewed").value(true));
    }

    @Test
    @DisplayName("sort=oldest — 오름차순으로 반환한다")
    void sortOldest() throws Exception {
        Gathering gathering2 = gatheringRepository.save(Gathering.builder()
                .leaderId(user.getId()).type(GatheringType.STUDY)
                .category("개발").title("두번째 모임").shortDescription("설명")
                .description("상세설명").goal("목표").maxMembers(10).currentMembers(1)
                .recruitDeadline(LocalDate.now().minusDays(14))
                .startDate(LocalDate.now().minusDays(7))
                .endDate(LocalDate.now().minusDays(1))
                .totalWeeks(1).status(GatheringStatus.COMPLETED).viewCount(0).build());

        gatheringMemberRepository.save(GatheringMember.builder()
                .gatheringId(gathering2.getId()).userId(user.getId())
                .role(GatheringRole.MEMBER).isActive(true)
                .overallAchievementRate(BigDecimal.ZERO).build());

        mockMvc.perform(get("/api/v1/users/me/gatherings")
                        .header("Authorization", "Bearer " + token)
                        .param("sort", "oldest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.gatherings[0].title").value("테스트 모임"));
    }

    @Test
    @DisplayName("pendingApplicationCount — LEADER인 모임에만 반환한다")
    void pendingApplicationCount() throws Exception {
        gatheringApplicationRepository.save(GatheringApplication.builder()
                .gatheringId(gathering.getId()).applicantId(applicant.getId())
                .personalGoal("목표").status(ApplicationStatus.PENDING).build());

        mockMvc.perform(get("/api/v1/users/me/gatherings")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.gatherings[0].pendingApplicationCount").value(1));
    }

    @Test
    @DisplayName("MEMBER 역할인 모임은 pendingApplicationCount가 null이다")
    void pendingApplicationCountNullForMember() throws Exception {
        Gathering otherGathering = gatheringRepository.save(Gathering.builder()
                .leaderId(applicant.getId()).type(GatheringType.STUDY)
                .category("개발").title("남의 모임").shortDescription("설명")
                .description("상세설명").goal("목표").maxMembers(10).currentMembers(2)
                .recruitDeadline(LocalDate.now().minusDays(14))
                .startDate(LocalDate.now().minusDays(7))
                .endDate(LocalDate.now().minusDays(1))
                .totalWeeks(1).status(GatheringStatus.IN_PROGRESS).viewCount(0).build());

        gatheringMemberRepository.save(GatheringMember.builder()
                .gatheringId(otherGathering.getId()).userId(user.getId())
                .role(GatheringRole.MEMBER).isActive(true)
                .overallAchievementRate(BigDecimal.ZERO).build());

        mockMvc.perform(get("/api/v1/users/me/gatherings")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                // otherGathering이 나중에 생성되어 최신순 기준 index 0
                .andExpect(jsonPath("$.data.gatherings[0].title").value("남의 모임"))
                .andExpect(jsonPath("$.data.gatherings[0].pendingApplicationCount").value((Object) null));
    }
}
