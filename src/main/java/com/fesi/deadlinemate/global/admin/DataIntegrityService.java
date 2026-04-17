package com.fesi.deadlinemate.global.admin;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataIntegrityService {

    private final JdbcTemplate jdbc;

    // ──────────────────────────────────────────────
    // CHECK
    // ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Map<String, Object> checkAll() {
        Map<String, Object> report = new LinkedHashMap<>();
        int total = 0;

        // reviews
        Map<String, Integer> reviews = new LinkedHashMap<>();
        reviews.put("orphanedByReviewer",    count("reviews",            "reviewer_id",    "users"));
        reviews.put("orphanedByTargetUser",  count("reviews",            "target_user_id", "users"));
        reviews.put("orphanedByGathering",   count("reviews",            "gathering_id",   "gatherings"));
        total += reviews.values().stream().mapToInt(i -> i).sum();
        report.put("reviews", reviews);

        // gathering_members
        Map<String, Integer> members = new LinkedHashMap<>();
        members.put("orphanedByUser",      count("gathering_members", "user_id",      "users"));
        members.put("orphanedByGathering", count("gathering_members", "gathering_id", "gatherings"));
        total += members.values().stream().mapToInt(i -> i).sum();
        report.put("gatheringMembers", members);

        // gatherings (leader_id → users) — report only, not auto-repair
        Map<String, Integer> gatherings = new LinkedHashMap<>();
        gatherings.put("orphanedLeader", count("gatherings", "leader_id", "users"));
        total += gatherings.values().stream().mapToInt(i -> i).sum();
        report.put("gatherings", gatherings);

        // todos
        Map<String, Integer> todos = new LinkedHashMap<>();
        todos.put("orphanedByUser",      count("todos", "user_id",      "users"));
        todos.put("orphanedByGathering", count("todos", "gathering_id", "gatherings"));
        total += todos.values().stream().mapToInt(i -> i).sum();
        report.put("todos", todos);

        // applications
        Map<String, Integer> apps = new LinkedHashMap<>();
        apps.put("orphanedByApplicant", count("applications", "applicant_id",  "users"));
        apps.put("orphanedByGathering", count("applications", "gathering_id",  "gatherings"));
        total += apps.values().stream().mapToInt(i -> i).sum();
        report.put("applications", apps);

        // gathering_likes
        Map<String, Integer> likes = new LinkedHashMap<>();
        likes.put("orphanedByUser",      count("gathering_likes", "user_id",      "users"));
        likes.put("orphanedByGathering", count("gathering_likes", "gathering_id", "gatherings"));
        total += likes.values().stream().mapToInt(i -> i).sum();
        report.put("gatheringLikes", likes);

        // gathering_reports
        Map<String, Integer> reportEntries = new LinkedHashMap<>();
        reportEntries.put("orphanedByGathering",        count("gathering_reports", "gathering_id",           "gatherings"));
        reportEntries.put("orphanedMvpUser",             countNullable("gathering_reports", "mvp_user_id",              "users"));
        reportEntries.put("orphanedLongestStreakUser",   countNullable("gathering_reports", "longest_streak_user_id",   "users"));
        reportEntries.put("orphanedMostImprovedUser",    countNullable("gathering_reports", "most_improved_user_id",    "users"));
        reportEntries.put("orphanedAttendanceUser",      countNullable("gathering_reports", "attendance_user_id",       "users"));
        total += reportEntries.values().stream().mapToInt(i -> i).sum();
        report.put("gatheringReports", reportEntries);

        // notifications
        Map<String, Integer> notifs = new LinkedHashMap<>();
        notifs.put("orphanedByUser", count("notifications", "user_id", "users"));
        total += notifs.values().stream().mapToInt(i -> i).sum();
        report.put("notifications", notifs);

        // weekly_plans
        Map<String, Integer> plans = new LinkedHashMap<>();
        plans.put("orphanedByGathering", count("weekly_plans", "gathering_id", "gatherings"));
        total += plans.values().stream().mapToInt(i -> i).sum();
        report.put("weeklyPlans", plans);

        // weekly_plan_details
        Map<String, Integer> planDetails = new LinkedHashMap<>();
        planDetails.put("orphanedByWeeklyPlan", count("weekly_plan_details", "weekly_plan_id", "weekly_plans"));
        total += planDetails.values().stream().mapToInt(i -> i).sum();
        report.put("weeklyPlanDetails", planDetails);

        // gathering_categories
        Map<String, Integer> cats = new LinkedHashMap<>();
        cats.put("orphanedByGathering", count("gathering_categories", "gathering_id", "gatherings"));
        total += cats.values().stream().mapToInt(i -> i).sum();
        report.put("gatheringCategories", cats);

        // gathering_images
        Map<String, Integer> images = new LinkedHashMap<>();
        images.put("orphanedByGathering", count("gathering_images", "gathering_id", "gatherings"));
        total += images.values().stream().mapToInt(i -> i).sum();
        report.put("gatheringImages", images);

        // gathering_tags
        Map<String, Integer> tags = new LinkedHashMap<>();
        tags.put("orphanedByGathering", count("gathering_tags", "gathering_id", "gatherings"));
        total += tags.values().stream().mapToInt(i -> i).sum();
        report.put("gatheringTags", tags);

        // refresh_tokens
        Map<String, Integer> tokens = new LinkedHashMap<>();
        tokens.put("orphanedByUser", count("refresh_tokens", "user_id", "users"));
        total += tokens.values().stream().mapToInt(i -> i).sum();
        report.put("refreshTokens", tokens);

        report.put("totalIssues", total);
        log.info("[DataIntegrity] 전체 정합성 검사 완료 — 이슈 {}건", total);
        return report;
    }

    // ──────────────────────────────────────────────
    // REPAIR
    // ──────────────────────────────────────────────

    @Transactional
    public Map<String, Object> repairAll() {
        Map<String, Object> result = new LinkedHashMap<>();
        int total = 0;

        // reviews — DELETE 고아 행
        int r1 = delete("reviews", "reviewer_id",    "users");
        int r2 = delete("reviews", "target_user_id", "users");
        int r3 = delete("reviews", "gathering_id",   "gatherings");
        result.put("reviews", Map.of("deletedByReviewer", r1, "deletedByTargetUser", r2, "deletedByGathering", r3));
        total += r1 + r2 + r3;

        // gathering_members
        int m1 = delete("gathering_members", "user_id",      "users");
        int m2 = delete("gathering_members", "gathering_id", "gatherings");
        result.put("gatheringMembers", Map.of("deletedByUser", m1, "deletedByGathering", m2));
        total += m1 + m2;

        // gatherings (leader 없는 경우) — 자동 삭제 위험하므로 skip, 로그만
        int g1 = count("gatherings", "leader_id", "users");
        if (g1 > 0) {
            log.warn("[DataRepair] gatherings.leader_id 고아 {}건 — 수동 확인 필요", g1);
        }
        result.put("gatherings", Map.of("skippedOrphanedLeader", g1));

        // todos
        int t1 = delete("todos", "user_id",      "users");
        int t2 = delete("todos", "gathering_id", "gatherings");
        result.put("todos", Map.of("deletedByUser", t1, "deletedByGathering", t2));
        total += t1 + t2;

        // applications
        int a1 = delete("applications", "applicant_id",  "users");
        int a2 = delete("applications", "gathering_id",  "gatherings");
        result.put("applications", Map.of("deletedByApplicant", a1, "deletedByGathering", a2));
        total += a1 + a2;

        // gathering_likes
        int l1 = delete("gathering_likes", "user_id",      "users");
        int l2 = delete("gathering_likes", "gathering_id", "gatherings");
        result.put("gatheringLikes", Map.of("deletedByUser", l1, "deletedByGathering", l2));
        total += l1 + l2;

        // gathering_reports — gathering 없으면 DELETE, nullable 유저 컬럼은 SET NULL
        int rp1 = delete("gathering_reports", "gathering_id", "gatherings");
        int rp2 = nullify("gathering_reports", "mvp_user_id",              "users");
        int rp3 = nullify("gathering_reports", "longest_streak_user_id",   "users");
        int rp4 = nullify("gathering_reports", "most_improved_user_id",    "users");
        int rp5 = nullify("gathering_reports", "attendance_user_id",       "users");
        result.put("gatheringReports", Map.of(
                "deletedByGathering", rp1,
                "nullifiedMvpUser", rp2,
                "nullifiedLongestStreakUser", rp3,
                "nullifiedMostImprovedUser", rp4,
                "nullifiedAttendanceUser", rp5));
        total += rp1 + rp2 + rp3 + rp4 + rp5;

        // notifications
        int n1 = delete("notifications", "user_id", "users");
        result.put("notifications", Map.of("deletedByUser", n1));
        total += n1;

        // weekly_plans
        int wp1 = delete("weekly_plans", "gathering_id", "gatherings");
        result.put("weeklyPlans", Map.of("deletedByGathering", wp1));
        total += wp1;

        // weekly_plan_details
        int wpd1 = delete("weekly_plan_details", "weekly_plan_id", "weekly_plans");
        result.put("weeklyPlanDetails", Map.of("deletedByWeeklyPlan", wpd1));
        total += wpd1;

        // gathering_categories
        int gc1 = delete("gathering_categories", "gathering_id", "gatherings");
        result.put("gatheringCategories", Map.of("deletedByGathering", gc1));
        total += gc1;

        // gathering_images
        int gi1 = delete("gathering_images", "gathering_id", "gatherings");
        result.put("gatheringImages", Map.of("deletedByGathering", gi1));
        total += gi1;

        // gathering_tags
        int gt1 = delete("gathering_tags", "gathering_id", "gatherings");
        result.put("gatheringTags", Map.of("deletedByGathering", gt1));
        total += gt1;

        // refresh_tokens
        int rt1 = delete("refresh_tokens", "user_id", "users");
        result.put("refreshTokens", Map.of("deletedByUser", rt1));
        total += rt1;

        result.put("totalRepaired", total);
        log.info("[DataRepair] 전체 정합성 복구 완료 — {}건 처리", total);
        return result;
    }

    // ──────────────────────────────────────────────
    // 공통 유틸
    // ──────────────────────────────────────────────

    /** child 테이블에서 parent에 없는 NOT NULL 컬럼 개수 */
    private int count(String child, String fkCol, String parent) {
        String sql = String.format(
                "SELECT COUNT(*) FROM %s WHERE %s NOT IN (SELECT id FROM %s)",
                child, fkCol, parent);
        Integer result = jdbc.queryForObject(sql, Integer.class);
        return result != null ? result : 0;
    }

    /** child 테이블에서 parent에 없는 NULLABLE 컬럼 개수 (IS NOT NULL 조건 추가) */
    private int countNullable(String child, String fkCol, String parent) {
        String sql = String.format(
                "SELECT COUNT(*) FROM %s WHERE %s IS NOT NULL AND %s NOT IN (SELECT id FROM %s)",
                child, fkCol, fkCol, parent);
        Integer result = jdbc.queryForObject(sql, Integer.class);
        return result != null ? result : 0;
    }

    /** 고아 행 삭제, 삭제된 행 수 반환 */
    private int delete(String child, String fkCol, String parent) {
        String sql = String.format(
                "DELETE FROM %s WHERE %s NOT IN (SELECT id FROM %s)",
                child, fkCol, parent);
        int deleted = jdbc.update(sql);
        if (deleted > 0) {
            log.warn("[DataRepair] {}.{} 고아 {}건 삭제", child, fkCol, deleted);
        }
        return deleted;
    }

    /** nullable 컬럼을 NULL로 초기화, 처리된 행 수 반환 */
    private int nullify(String table, String col, String parent) {
        String sql = String.format(
                "UPDATE %s SET %s = NULL WHERE %s IS NOT NULL AND %s NOT IN (SELECT id FROM %s)",
                table, col, col, col, parent);
        int updated = jdbc.update(sql);
        if (updated > 0) {
            log.warn("[DataRepair] {}.{} 고아 참조 {}건 NULL 처리", table, col, updated);
        }
        return updated;
    }
}
