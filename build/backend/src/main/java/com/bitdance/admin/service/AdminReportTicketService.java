package com.bitdance.admin.service;

import com.bitdance.admin.dto.HandleReportRequest;
import com.bitdance.admin.dto.ReportTicketDto;
import com.bitdance.audit.aspect.AuditAction;
import com.bitdance.common.exception.BizException;
import com.bitdance.community.domain.ReportTicket;
import com.bitdance.community.repository.ContentCommentRepository;
import com.bitdance.community.repository.ContentPostRepository;
import com.bitdance.community.repository.ReportTicketRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Set;

@Service
public class AdminReportTicketService {

    private static final Set<String> CAN_PROCESS = Set.of("pending");
    private static final Set<String> CAN_CLOSE_REJECT = Set.of("pending", "processing");

    private final ReportTicketRepository repo;
    private final ContentPostRepository postRepo;
    private final ContentCommentRepository commentRepo;

    public AdminReportTicketService(
        ReportTicketRepository repo,
        ContentPostRepository postRepo,
        ContentCommentRepository commentRepo
    ) {
        this.repo = repo;
        this.postRepo = postRepo;
        this.commentRepo = commentRepo;
    }

    @Transactional(readOnly = true)
    public Page<ReportTicketDto> list(String status, String targetType, int page, int pageSize) {
        int p = Math.max(1, page);
        int s = Math.min(Math.max(1, pageSize), 100);
        String st = status == null || status.isBlank() ? "pending" : status;
        String tt = targetType == null || targetType.isBlank() ? null : targetType;
        return repo.search(st, tt, PageRequest.of(p - 1, s)).map(this::toDto);
    }

    @Transactional
    @AuditAction(value = "report.process", targetType = "report_ticket")
    public ReportTicketDto process(Long adminId, Long ticketId) {
        ReportTicket t = load(ticketId);
        require(t, CAN_PROCESS);
        t.setReportStatus("processing");
        writeHandledBy(t, adminId);
        return toDto(repo.save(t));
    }

    @Transactional
    @AuditAction(value = "report.close", targetType = "report_ticket")
    public ReportTicketDto close(Long adminId, Long ticketId, HandleReportRequest req) {
        ReportTicket t = load(ticketId);
        require(t, CAN_CLOSE_REJECT);
        t.setReportStatus("closed");
        writeHandleResult(t, req);
        writeHandledBy(t, adminId);
        hideTarget(t.getTargetType(), t.getTargetId());
        return toDto(repo.save(t));
    }

    @Transactional
    @AuditAction(value = "report.reject", targetType = "report_ticket")
    public ReportTicketDto reject(Long adminId, Long ticketId, HandleReportRequest req) {
        ReportTicket t = load(ticketId);
        require(t, CAN_CLOSE_REJECT);
        t.setReportStatus("rejected");
        writeHandleResult(t, req);
        writeHandledBy(t, adminId);
        return toDto(repo.save(t));
    }

    private void hideTarget(String targetType, Long targetId) {
        if ("content_post".equals(targetType)) {
            postRepo.findById(targetId).ifPresent(p -> {
                if (!"hidden".equals(p.getPostStatus()) && !"deleted".equals(p.getPostStatus())) {
                    p.setPostStatus("hidden");
                    postRepo.save(p);
                }
            });
        } else if ("content_comment".equals(targetType)) {
            commentRepo.findById(targetId).ifPresent(c -> {
                if (!"hidden".equals(c.getCommentStatus()) && !"deleted".equals(c.getCommentStatus())) {
                    c.setCommentStatus("hidden");
                    commentRepo.save(c);
                }
            });
        }
    }

    private void writeHandleResult(ReportTicket t, HandleReportRequest req) {
        if (req != null && req.handleResult() != null && !req.handleResult().isBlank()) {
            t.setHandleResult(req.handleResult().trim());
        }
    }

    private void writeHandledBy(ReportTicket t, Long adminId) {
        t.setHandledByUserId(adminId);
        t.setHandledAt(OffsetDateTime.now());
    }

    private ReportTicket load(Long id) {
        return repo.findById(id)
            .orElseThrow(() -> new BizException("REPORT_NOT_FOUND", "举报工单不存在"));
    }

    private void require(ReportTicket t, Set<String> allowed) {
        if (!allowed.contains(t.getReportStatus())) {
            throw new BizException("REPORT_STATE_CONFLICT",
                "工单状态 " + t.getReportStatus() + " 不可执行该操作");
        }
    }

    private ReportTicketDto toDto(ReportTicket t) {
        return new ReportTicketDto(
            t.getId(), t.getReporterUserId(),
            t.getTargetType(), t.getTargetId(),
            t.getReasonCode(), t.getReasonDetail(),
            t.getReportStatus(), t.getHandledByUserId(), t.getHandledAt(), t.getHandleResult(),
            t.getCreatedAt()
        );
    }
}
