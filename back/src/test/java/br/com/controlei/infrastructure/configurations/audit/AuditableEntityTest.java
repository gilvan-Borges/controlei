package br.com.controlei.infrastructure.configurations.audit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuditableEntityTest {

    @Autowired
    private AuditTestRepository repository;

    @Test
    void shouldPopulateAuditFieldsOnCreate() {
        AuditTestEntity entity = new AuditTestEntity("Test");

        AuditTestEntity saved = repository.save(entity);

        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getCreatedBy());
        assertNotNull(saved.getUpdatedAt());
        assertNotNull(saved.getUpdatedBy());
        assertNull(saved.getDeletedAt());
        assertNull(saved.getDeletedBy());
        assertFalse(saved.isDeleted());
        assertEquals("system", saved.getCreatedBy());
    }

    @Test
    void shouldUpdateAuditFieldsOnUpdate() {
        AuditTestEntity entity = new AuditTestEntity("Test");
        AuditTestEntity saved = repository.save(entity);

        LocalDateTime originalCreatedAt = saved.getCreatedAt();
        String originalCreatedBy = saved.getCreatedBy();

        saved.setName("Updated");
        AuditTestEntity updated = repository.save(saved);

        assertEquals(originalCreatedAt, updated.getCreatedAt());
        assertEquals(originalCreatedBy, updated.getCreatedBy());
        assertNotNull(updated.getUpdatedAt());
        assertNotNull(updated.getUpdatedBy());
    }

    @Test
    void shouldSoftDeleteBySettingDeletedFields() {
        AuditTestEntity entity = new AuditTestEntity("Test");
        AuditTestEntity saved = repository.save(entity);

        saved.setDeletedAt(LocalDateTime.now());
        saved.setDeletedBy("system");
        repository.save(saved);

        AuditTestEntity found = repository.findById(saved.getId()).orElse(null);
        assertNotNull(found);
        assertTrue(found.isDeleted());
        assertNotNull(found.getDeletedAt());
        assertEquals("system", found.getDeletedBy());
    }

    @Test
    void shouldFilterDeletedRecordsInQuery() {
        AuditTestEntity active = repository.save(new AuditTestEntity("Active"));
        AuditTestEntity deleted = repository.save(new AuditTestEntity("Deleted"));
        deleted.setDeletedAt(LocalDateTime.now());
        deleted.setDeletedBy("system");
        repository.save(deleted);

        var activeRecords = repository.findByDeletedAtIsNull();

        assertEquals(1, activeRecords.size());
        assertEquals("Active", activeRecords.get(0).getName());
    }
}
