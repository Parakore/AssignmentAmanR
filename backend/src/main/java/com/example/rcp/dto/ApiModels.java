package com.example.rcp.dto;

import com.example.rcp.entity.Enums.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public final class ApiModels {
    private ApiModels() {}
    public record Role(@NotBlank String code) {}
    public record UserInfo(@NotBlank String uuid,@NotBlank String userName,@NotBlank String tenantId,@NotEmpty List<@Valid Role> roles) {}
    public record RequestInfo(@NotBlank String apiId,@NotBlank String msgId,@Valid @NotNull UserInfo userInfo) {}
    public record RoadCuttingInput(@NotBlank String tenantId,@NotBlank String roadType,@NotNull @Positive BigDecimal lengthInMeters,@NotNull @Positive BigDecimal widthInMeters,@NotNull @Positive Integer durationInDays,@NotNull ApplicantType applicantType,@NotNull LocalDate proposedStartDate) {}
    public record CalculateRequest(@Valid @NotNull RequestInfo RequestInfo,@Valid @NotNull RoadCuttingInput Calculation) {}
    public record CreateRequest(@Valid @NotNull RequestInfo RequestInfo,@Valid @NotNull CreateInput Application) {}
    public record CreateInput(@NotBlank String tenantId,@NotBlank String mobileNumber,@NotBlank String roadType,@NotNull @Positive BigDecimal lengthInMeters,@NotNull @Positive BigDecimal widthInMeters,@NotNull @Positive Integer durationInDays,@NotNull ApplicantType applicantType,@NotNull LocalDate proposedStartDate) {}
    public record ActionRequest(@Valid @NotNull RequestInfo RequestInfo,@Valid @NotNull ActionInput Action) {}
    public record ActionInput(@NotBlank String tenantId,@NotBlank String applicationNumber,@NotBlank String action,@Size(max=1000) String comment) {}
    public record SearchRequest(@Valid @NotNull RequestInfo RequestInfo,@Valid @NotNull SearchInput Search) {}
    public record SearchInput(String tenantId,String applicationNumber,Status status,String mobileNumber,@Min(0) Integer offset,@Min(1) Integer limit) {}
    public record GetRequest(@Valid @NotNull RequestInfo RequestInfo,@Valid @NotNull GetInput Application) {}
    public record GetInput(@NotBlank String tenantId,@NotBlank String applicationNumber) {}
    public record UpdateRequest(@Valid @NotNull RequestInfo RequestInfo,@Valid @NotNull UpdateInput Application) {}
    public record UpdateInput(@NotBlank String tenantId,@NotBlank String applicationNumber,@NotBlank String mobileNumber,@NotBlank String roadType,@NotNull @Positive BigDecimal lengthInMeters,@NotNull @Positive BigDecimal widthInMeters,@NotNull @Positive Integer durationInDays,@NotNull ApplicantType applicantType,@NotNull LocalDate proposedStartDate) {}

    public record ResponseInfo(String msgId,String status) {}
    public record ErrorItem(String code,String message) {}
    public record ErrorResponse(ResponseInfo ResponseInfo,List<ErrorItem> Errors) {}
    public record Calculation(Long areaInSqm,BigDecimal restorationCharge,BigDecimal permissionFee,BigDecimal urgencySurcharge,BigDecimal securityDeposit,BigDecimal totalAmount,String reviewRef) {}
    public record CalculationResponse(ResponseInfo ResponseInfo,Calculation Calculation) {}
    public record CreateResponse(ResponseInfo ResponseInfo,ApplicationView Application) {}
    public record ActionResponse(ResponseInfo ResponseInfo,ApplicationView Application) {}
    public record GetResponse(ResponseInfo ResponseInfo,ApplicationView Application) {}
    public record SearchResponse(ResponseInfo ResponseInfo,List<ApplicationView> applications,long total,int offset,int limit) {}
    public record TransitionView(String action,String fromStatus,String toStatus,String actorUuid,String actorRole,String comment,OffsetDateTime timestamp) {}
    public record ApplicationView(String applicationNumber,String tenantId,String status,String applicantUuid,String mobileNumber,String applicantType,String roadType,BigDecimal lengthInMeters,BigDecimal widthInMeters,Integer durationInDays,LocalDate proposedStartDate,LocalDate applicationDate,Calculation Calculation,List<String> availableActions,List<TransitionView> history) {}
}
