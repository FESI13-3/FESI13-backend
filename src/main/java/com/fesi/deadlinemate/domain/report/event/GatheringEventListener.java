package com.fesi.deadlinemate.domain.report.event;

import com.fesi.deadlinemate.domain.gathering.event.GatheringCompletedEvent;
import com.fesi.deadlinemate.domain.report.service.GatheringReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class GatheringEventListener {
    private final GatheringReportService gatheringReportService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(GatheringCompletedEvent event) {
        gatheringReportService.createReport(event.gatheringId());
    }
}
