package com.fesi.deadlinemate.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "리소스를 찾을 수 없습니다."),
    CONFLICT(HttpStatus.CONFLICT, "중복된 데이터입니다."),
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "요청 횟수를 초과했습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다."),

    // Auth
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 일치하지 않습니다."),
    LOGIN_ATTEMPTS_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "로그인 시도 횟수를 초과했습니다. 30초 후 다시 시도해주세요."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    UNSUPPORTED_OAUTH_PROVIDER(HttpStatus.BAD_REQUEST, "지원하지 않는 OAuth 제공자입니다."),
    OAUTH_AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "OAuth 인증에 실패했습니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    PASSWORD_NOT_MATCHED(HttpStatus.BAD_REQUEST, "현재 비밀번호가 일치하지 않습니다."),
    SOCIAL_USER_PASSWORD_CHANGE(HttpStatus.FORBIDDEN, "소셜 로그인 사용자는 비밀번호를 변경할 수 없습니다."),

    // Gathering
    INVALID_GATHERING_DATE(HttpStatus.BAD_REQUEST, "모임 종료일은 시작일보다 빠를 수 없습니다."),
    INVALID_GATHERING_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 모임 타입입니다."),
    INVALID_GATHERING_LEADER(HttpStatus.FORBIDDEN, "해당 기능은 모임장만 가능합니다."),
    INVALID_RECRUIT_DEADLINE(HttpStatus.BAD_REQUEST, "모집 마감일은 시작일 이전이어야 합니다."),
    INVALID_IN_PROGRESS_UPDATE_ITEMS(HttpStatus.BAD_REQUEST, "진행 중인 모임은 description, weeklyGuides, endDate만 수정할 수 있습니다."),
    GATHERING_UPDATE_FORBIDDEN_IN_PROGRESS(HttpStatus.BAD_REQUEST,"현재 상태의 모임은 수정할 수 없습니다."),
    GATHERING_DELETE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "진행 중인 모임은 삭제할 수 없습니다."),
    INVALID_MAX_MEMBERS(HttpStatus.BAD_REQUEST, "최대 인원은 2명 이상 10명 이하여야 합니다."),
    GATHERING_NOT_FOUND(HttpStatus.NOT_FOUND, "모임을 찾을 수 없습니다."),

    //WeeklyPlan
    INVALID_WEEKLY_GUIDE_SEQUENCE(HttpStatus.BAD_REQUEST,"주차 가이드는 1주차부터 순차적으로 입력되어야 합니다."),

    // Review
    REVIEW_ALREADY_SUBMITTED(HttpStatus.CONFLICT, "이미 리뷰를 작성했습니다."),
    GATHERING_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "모임이 완료된 후에만 리뷰를 작성할 수 있습니다."),
    SELF_REVIEW_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "본인에게 리뷰를 작성할 수 없습니다."),
    REVIEWER_NOT_A_MEMBER(HttpStatus.FORBIDDEN, "해당 모임의 멤버만 리뷰를 작성할 수 있습니다."),
    REVIEW_TARGET_NOT_A_MEMBER(HttpStatus.BAD_REQUEST, "리뷰 대상이 해당 모임의 멤버가 아닙니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
