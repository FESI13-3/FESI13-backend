package com.fesi.deadlinemate.domain.gatheringApplication.service;

import com.fesi.deadlinemate.domain.gatheringApplication.dto.MockApplicationDtos.ApplicantDto;
import com.fesi.deadlinemate.domain.gatheringApplication.dto.MockApplicationDtos.ApplicationItemDto;
import com.fesi.deadlinemate.domain.gatheringApplication.dto.MockApplicationDtos.ApplicationListResponse;
import com.fesi.deadlinemate.domain.gatheringApplication.dto.MockApplicationDtos.CreateApplicationRequest;
import com.fesi.deadlinemate.domain.gatheringApplication.dto.MockApplicationDtos.MyApplicationItemDto;
import com.fesi.deadlinemate.domain.gatheringApplication.dto.MockApplicationDtos.MyApplicationListResponse;
import com.fesi.deadlinemate.domain.gatheringApplication.dto.MockApplicationDtos.UpdateApplicationStatusRequest;
import com.fesi.deadlinemate.domain.gatheringApplication.entity.MockApplicationEntity;
import com.fesi.deadlinemate.domain.gathering.entity.MockGatheringEntity;
import com.fesi.deadlinemate.global.mock.MockStore;
import com.fesi.deadlinemate.global.mock.support.MockAuthContext;
import com.fesi.deadlinemate.global.mock.support.MockFinder;
import com.fesi.deadlinemate.global.mock.support.MockPermissionService;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class MockApplicationService {
    private final MockStore store;
    private final MockFinder finder;
    private final MockAuthContext authContext;
    private final MockPermissionService permissionService;

    public MockApplicationService(
            MockStore store,
            MockFinder finder,
            MockAuthContext authContext,
            MockPermissionService permissionService
    ) {
        this.store = store;
        this.finder = finder;
        this.authContext = authContext;
        this.permissionService = permissionService;
    }

    public Map<String, Object> createApplication(Long gatheringId, CreateApplicationRequest req) {
        finder.getGathering(gatheringId);

        boolean exists = store.applications.values().stream()
                .anyMatch(a -> a.gatheringId.equals(gatheringId)
                        && a.applicantId.equals(authContext.currentUserId())
                        && "PENDING".equals(a.status));

        if (exists) {
            throw new IllegalStateException("이미 신청 중입니다.");
        }

        MockApplicationEntity application = new MockApplicationEntity();
        application.id = store.applicationSeq.getAndIncrement();
        application.gatheringId = gatheringId;
        application.applicantId = authContext.currentUserId();
        application.personalGoal = req.personalGoal();
        application.selfIntroduction = req.selfIntroduction();
        application.status = "PENDING";
        application.createdAt = OffsetDateTime.now();

        store.applications.put(application.id, application);

        return Map.of(
                "application", Map.of(
                        "id", application.id,
                        "status", application.status,
                        "createdAt", application.createdAt
                )
        );
    }

    public ApplicationListResponse getApplications(Long gatheringId) {
        MockGatheringEntity gathering = finder.getGathering(gatheringId);
        permissionService.validateLeader(gathering);

        List<ApplicationItemDto> items = store.applications.values().stream()
                .filter(a -> a.gatheringId.equals(gatheringId))
                .sorted(Comparator.comparing((MockApplicationEntity a) -> a.createdAt).reversed())
                .map(a -> new ApplicationItemDto(
                        a.id,
                        new ApplicantDto(
                                a.applicantId,
                                "유저" + a.applicantId,
                                "https://example.com/profile.png",
                                36.5
                        ),
                        a.personalGoal,
                        a.selfIntroduction,
                        a.status,
                        a.createdAt
                ))
                .toList();

        return new ApplicationListResponse(items);
    }

    public Map<String, Object> updateApplicationStatus(Long gatheringId, Long applicationId, UpdateApplicationStatusRequest req) {
        MockGatheringEntity gathering = finder.getGathering(gatheringId);
        permissionService.validateLeader(gathering);

        MockApplicationEntity application = finder.getApplication(applicationId);
        application.status = req.status();

        if ("ACCEPTED".equals(req.status())) {
            store.gatheringMembers.computeIfAbsent(gatheringId, k -> new LinkedHashSet<>()).add(application.applicantId);
            gathering.currentMembers = store.gatheringMembers.get(gatheringId).size();
        }

        return Map.of(
                "application", Map.of(
                        "id", application.id,
                        "status", application.status
                )
        );
    }

    public void cancelApplication(Long gatheringId, Long applicationId) {
        MockApplicationEntity application = finder.getApplication(applicationId);

        if (!application.gatheringId.equals(gatheringId)
                || !application.applicantId.equals(authContext.currentUserId())) {
            throw new IllegalArgumentException("본인의 신청만 취소할 수 있습니다.");
        }

        if (!"PENDING".equals(application.status)) {
            throw new IllegalArgumentException("PENDING 상태가 아닌 경우 취소할 수 없습니다.");
        }

        store.applications.remove(applicationId);
    }

    public MyApplicationListResponse getMyApplications() {
        List<MyApplicationItemDto> items = store.applications.values().stream()
                .filter(a -> a.applicantId.equals(authContext.currentUserId()))
                .sorted(Comparator.comparing((MockApplicationEntity a) -> a.createdAt).reversed())
                .map(a -> {
                    MockGatheringEntity g = finder.getGathering(a.gatheringId);
                    return new MyApplicationItemDto(
                            a.id,
                            Map.of(
                                    "id", g.id,
                                    "title", g.title,
                                    "type", g.type,
                                    "status", g.status
                            ),
                            a.personalGoal,
                            a.status,
                            a.createdAt
                    );
                })
                .toList();

        return new MyApplicationListResponse(items);
    }
}
