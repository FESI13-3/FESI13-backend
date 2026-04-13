package com.fesi.deadlinemate.domain.notification.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fesi.deadlinemate.domain.notification.dto.response.NotificationListResponse;
import com.fesi.deadlinemate.domain.notification.service.NotificationCommandService;
import com.fesi.deadlinemate.domain.notification.service.NotificationQueryService;
import com.fesi.deadlinemate.global.error.BusinessException;
import com.fesi.deadlinemate.global.error.ErrorCode;
import com.fesi.deadlinemate.global.error.GlobalExceptionHandler;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private NotificationController notificationController;

    @Mock
    private NotificationCommandService notificationCommandService;

    @Mock
    private NotificationQueryService notificationQueryService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(notificationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Nested
    @DisplayName("GET /api/v1/notifications")
    class GetNotifications {

        @Test
        @DisplayName("알림 목록과 읽지 않은 알림 수를 반환한다")
        void returnsNotificationList() throws Exception {
            NotificationListResponse response = NotificationListResponse.builder()
                    .notifications(List.of())
                    .unreadCount(3)
                    .build();
            given(notificationQueryService.getNotifications(1L, 1, 20)).willReturn(response);

            mockMvc.perform(get("/api/v1/notifications")
                            .param("page", "1")
                            .param("limit", "20")
                            .principal(new UsernamePasswordAuthenticationToken(1L, null, List.of())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.unreadCount").value(3))
                    .andExpect(jsonPath("$.data.notifications").isArray());
        }

        @Test
        @DisplayName("page/limit 미전달 시 기본값(1/20)으로 조회한다")
        void usesDefaultPageAndLimit() throws Exception {
            NotificationListResponse response = NotificationListResponse.builder()
                    .notifications(List.of())
                    .unreadCount(0)
                    .build();
            given(notificationQueryService.getNotifications(1L, 1, 20)).willReturn(response);

            mockMvc.perform(get("/api/v1/notifications")
                            .principal(new UsernamePasswordAuthenticationToken(1L, null, List.of())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.unreadCount").value(0));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/notifications/{id}/read")
    class MarkAsRead {

        @Test
        @DisplayName("알림을 읽음 처리할 수 있다")
        void markAsReadSuccess() throws Exception {
            doNothing().when(notificationCommandService).markAsRead(10L, 1L);

            mockMvc.perform(patch("/api/v1/notifications/10/read")
                            .principal(new UsernamePasswordAuthenticationToken(1L, null, List.of())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("존재하지 않는 알림 읽음 처리 시 404를 반환한다")
        void notFoundReturns404() throws Exception {
            doThrow(new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND))
                    .when(notificationCommandService).markAsRead(99L, 1L);

            mockMvc.perform(patch("/api/v1/notifications/99/read")
                            .principal(new UsernamePasswordAuthenticationToken(1L, null, List.of())))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("NOTIFICATION_NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/notifications/read-all")
    class MarkAllAsRead {

        @Test
        @DisplayName("모든 알림을 읽음 처리할 수 있다")
        void markAllAsReadSuccess() throws Exception {
            doNothing().when(notificationCommandService).markAllAsRead(1L);

            mockMvc.perform(patch("/api/v1/notifications/read-all")
                            .principal(new UsernamePasswordAuthenticationToken(1L, null, List.of())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}
