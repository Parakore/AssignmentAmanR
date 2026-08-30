package com.example.rcp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import static com.example.rcp.entity.Enums.*;

@Entity @Table(name="applications", indexes={
        @Index(name="idx_app_tenant_status", columnList="tenant_id,status"),
        @Index(name="idx_app_tenant_mobile", columnList="tenant_id,mobile_number"),
        @Index(name="idx_app_tenant_applicant", columnList="tenant_id,applicant_uuid")})
public class Application {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="application_number", nullable=false, unique=true, length=40) private String applicationNumber;
    @Column(name="tenant_id", nullable=false, length=64) private String tenantId;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=32) private Status status;
    @Column(name="applicant_uuid", nullable=false, length=128) private String applicantUuid;
    @Column(name="mobile_number", nullable=false, length=32) private String mobileNumber;
    @Enumerated(EnumType.STRING) @Column(name="applicant_type", nullable=false, length=32) private ApplicantType applicantType;
    @Column(name="road_type", nullable=false, length=32) private String roadType;
    @Column(name="length_in_meters", nullable=false, precision=12, scale=3) private BigDecimal lengthInMeters;
    @Column(name="width_in_meters", nullable=false, precision=12, scale=3) private BigDecimal widthInMeters;
    @Column(name="duration_in_days", nullable=false) private Integer durationInDays;
    @Column(name="proposed_start_date", nullable=false) private LocalDate proposedStartDate;
    @Column(name="application_date", nullable=false) private LocalDate applicationDate;
    @Column(name="area_in_sqm", nullable=false) private Long areaInSqm;
    @Column(name="restoration_charge", nullable=false, precision=18, scale=2) private BigDecimal restorationCharge;
    @Column(name="permission_fee", nullable=false, precision=18, scale=2) private BigDecimal permissionFee;
    @Column(name="urgency_surcharge", nullable=false, precision=18, scale=2) private BigDecimal urgencySurcharge;
    @Column(name="security_deposit", nullable=false, precision=18, scale=2) private BigDecimal securityDeposit;
    @Column(name="total_amount", nullable=false, precision=18, scale=2) private BigDecimal totalAmount;
    @Version @Column(nullable=false) private Long version;
    @Column(name="created_by", nullable=false) private String createdBy;
    @Column(name="created_time", nullable=false) private OffsetDateTime createdTime;
    @Column(name="last_modified_by", nullable=false) private String lastModifiedBy;
    @Column(name="last_modified_time", nullable=false) private OffsetDateTime lastModifiedTime;

    public Long getId(){return id;} public String getApplicationNumber(){return applicationNumber;} public void setApplicationNumber(String v){applicationNumber=v;}
    public String getTenantId(){return tenantId;} public void setTenantId(String v){tenantId=v;} public Status getStatus(){return status;} public void setStatus(Status v){status=v;}
    public String getApplicantUuid(){return applicantUuid;} public void setApplicantUuid(String v){applicantUuid=v;} public String getMobileNumber(){return mobileNumber;} public void setMobileNumber(String v){mobileNumber=v;}
    public ApplicantType getApplicantType(){return applicantType;} public void setApplicantType(ApplicantType v){applicantType=v;} public String getRoadType(){return roadType;} public void setRoadType(String v){roadType=v;}
    public BigDecimal getLengthInMeters(){return lengthInMeters;} public void setLengthInMeters(BigDecimal v){lengthInMeters=v;} public BigDecimal getWidthInMeters(){return widthInMeters;} public void setWidthInMeters(BigDecimal v){widthInMeters=v;}
    public Integer getDurationInDays(){return durationInDays;} public void setDurationInDays(Integer v){durationInDays=v;} public LocalDate getProposedStartDate(){return proposedStartDate;} public void setProposedStartDate(LocalDate v){proposedStartDate=v;}
    public LocalDate getApplicationDate(){return applicationDate;} public void setApplicationDate(LocalDate v){applicationDate=v;} public Long getAreaInSqm(){return areaInSqm;} public void setAreaInSqm(Long v){areaInSqm=v;}
    public BigDecimal getRestorationCharge(){return restorationCharge;} public void setRestorationCharge(BigDecimal v){restorationCharge=v;} public BigDecimal getPermissionFee(){return permissionFee;} public void setPermissionFee(BigDecimal v){permissionFee=v;}
    public BigDecimal getUrgencySurcharge(){return urgencySurcharge;} public void setUrgencySurcharge(BigDecimal v){urgencySurcharge=v;} public BigDecimal getSecurityDeposit(){return securityDeposit;} public void setSecurityDeposit(BigDecimal v){securityDeposit=v;}
    public BigDecimal getTotalAmount(){return totalAmount;} public void setTotalAmount(BigDecimal v){totalAmount=v;} public Long getVersion(){return version;}
    public String getCreatedBy(){return createdBy;} public void setCreatedBy(String v){createdBy=v;} public OffsetDateTime getCreatedTime(){return createdTime;} public void setCreatedTime(OffsetDateTime v){createdTime=v;}
    public String getLastModifiedBy(){return lastModifiedBy;} public void setLastModifiedBy(String v){lastModifiedBy=v;} public OffsetDateTime getLastModifiedTime(){return lastModifiedTime;} public void setLastModifiedTime(OffsetDateTime v){lastModifiedTime=v;}
}
