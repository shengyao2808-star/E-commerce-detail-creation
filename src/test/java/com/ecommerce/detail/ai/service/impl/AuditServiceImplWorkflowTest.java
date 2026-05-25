package com.ecommerce.detail.ai.service.impl;

import com.ecommerce.detail.ai.common.enums.AuditStatus;
import com.ecommerce.detail.ai.entity.AuditRecord;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuditServiceImplWorkflowTest {

    @Test
    void withdrawAuditAllowsPendingStatusFromEnumCode() {
        AuditRecord record = new AuditRecord();
        record.setId(1L);
        record.setAuditStatus(AuditStatus.PENDING.getCode());
        RecordingAuditService service = new RecordingAuditService(record);

        boolean result = service.withdrawAudit(1L);

        assertTrue(result);
        assertTrue(service.removed);
    }

    @Test
    void reauditRejectsWhenAlreadyPendingStatusFromEnumCode() {
        AuditRecord record = new AuditRecord();
        record.setId(2L);
        record.setAuditStatus(AuditStatus.PENDING.getCode());
        RecordingAuditService service = new RecordingAuditService(record);

        assertThrows(RuntimeException.class, () -> service.reaudit(2L));
    }

    private static class RecordingAuditService extends AuditServiceImpl {
        private final AuditRecord record;
        private boolean removed;

        private RecordingAuditService(AuditRecord record) {
            this.record = record;
        }

        @Override
        public AuditRecord getById(Serializable id) {
            return record != null && record.getId().equals(id) ? record : null;
        }

        @Override
        public boolean removeById(Serializable id) {
            removed = true;
            return true;
        }
    }
}
