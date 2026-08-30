package com.example.rcp.service;

import com.example.rcp.config.WorkflowConfigService;
import com.example.rcp.config.RateConfigProvider;
import com.example.rcp.dto.ApiModels.*;
import com.example.rcp.entity.Application;
import com.example.rcp.entity.Enums.Status;
import com.example.rcp.repository.*;
import com.example.rcp.exception.ApiException;
import org.junit.jupiter.api.*;
import org.mockito.*;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ApplicationServiceTest {
  ApplicationRepository repo=mock(ApplicationRepository.class); ApplicationTransitionRepository history=mock(ApplicationTransitionRepository.class); FeeCalculationService fee=mock(FeeCalculationService.class); WorkflowConfigService workflow=mock(WorkflowConfigService.class); RateConfigProvider rates=mock(RateConfigProvider.class);
  ApplicationService service;
  RequestInfo applicant(String tenant){return new RequestInfo("portal","m",new UserInfo("u1","999",""+tenant,List.of(new Role("APPLICANT"))));}
  @BeforeEach void setup(){service=new ApplicationService(repo,history,fee,workflow,rates,Clock.fixed(Instant.parse("2026-03-01T00:00:00Z"),ZoneOffset.UTC),100);}
  @Test void tenantIsolationRejectsMismatchedTenant(){var info=applicant("dehradun"); var input=new RoadCuttingInput("haridwar","BT",new java.math.BigDecimal("1"),new java.math.BigDecimal("1"),1,com.example.rcp.entity.Enums.ApplicantType.PRIVATE,LocalDate.of(2026,3,2)); var ex=assertThrows(ApiException.class,()->service.calculate(info,input)); assertEquals("TENANT_ACCESS_DENIED",ex.getCode());}
  @Test void illegalTransitionReturnsFourHundred(){var a=new Application();a.setTenantId("dehradun");a.setApplicationNumber("DDN-RCP-000001-2026-27");a.setStatus(Status.APPLIED);a.setApplicantUuid("u1");when(repo.findByApplicationNumberAndTenantId(anyString(),eq("dehradun"))).thenReturn(Optional.of(a)); var info=new RequestInfo("portal","m",new UserInfo("u2","999", "dehradun",List.of(new Role("VERIFIER")))); when(workflow.find("APPROVE","APPLIED","VERIFIER")).thenThrow(new ApiException(400,"ILLEGAL_TRANSITION","not allowed")); var ex=assertThrows(ApiException.class,()->service.action(info,new ActionInput("dehradun",a.getApplicationNumber(),"APPROVE",null)));assertEquals("FORBIDDEN",ex.getCode());}
}
