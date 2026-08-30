
package com.example.rcp.repository;

import com.example.rcp.entity.Application;
import com.example.rcp.entity.Enums.Status;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    Optional<Application> findByApplicationNumberAndTenantId(
            String applicationNumber,
            String tenantId
    );

    @Query(value = "select nextval('application_number_seq')", nativeQuery = true)
    Long getEntityManagerSequence();

    @Query("""
        select a
        from Application a
        where a.tenantId = :tenant
          and lower(a.applicationNumber) like lower(concat('%', coalesce(:number, ''), '%'))
          and (:status is null or a.status = :status)
          and a.mobileNumber like concat('%', coalesce(:mobile, ''), '%')
          and (coalesce(:applicant, '') = '' or a.applicantUuid = :applicant)
        """)
    Page<Application> search(
            @Param("tenant") String tenant,
            @Param("number") String number,
            @Param("status") Status status,
            @Param("mobile") String mobile,
            @Param("applicant") String applicant,
            Pageable pageable
    );
}