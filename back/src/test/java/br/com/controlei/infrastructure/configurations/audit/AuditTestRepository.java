package br.com.controlei.infrastructure.configurations.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
interface AuditTestRepository extends JpaRepository<AuditTestEntity, Long> {

    List<AuditTestEntity> findByDeletedAtIsNull();
}
