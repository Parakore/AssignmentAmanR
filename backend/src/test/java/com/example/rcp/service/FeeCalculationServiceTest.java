package com.example.rcp.service;

import com.example.rcp.config.RateConfigService;
import com.example.rcp.dto.ApiModels.RoadCuttingInput;
import com.example.rcp.entity.Enums.ApplicantType;
import com.example.rcp.exception.ApiException;
import org.junit.jupiter.api.*;
import java.math.*;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;

class FeeCalculationServiceTest {
  private FeeCalculationService service;
  @BeforeEach void setUp(){service=new FeeCalculationService(new RateConfigServiceForTest(),Clock.fixed(Instant.parse("2026-03-01T00:00:00Z"),ZoneOffset.UTC));}
  private RoadCuttingInput input(String tenant,String road,ApplicantType type,LocalDate start){return new RoadCuttingInput(tenant,road,new BigDecimal("12.5"),new BigDecimal("1.2"),6,type,start);}
  @Test void dehradunWorkedExample(){var c=service.calculate(input("dehradun","BT",ApplicantType.PRIVATE,LocalDate.of(2026,3,2)),LocalDate.of(2026,3,1));assertEquals(15,c.areaInSqm());assertEquals(new BigDecimal("18000.00"),c.restorationCharge());assertEquals(new BigDecimal("1350.00"),c.permissionFee());assertEquals(new BigDecimal("135.00"),c.urgencySurcharge());assertEquals(new BigDecimal("5000.00"),c.securityDeposit());assertEquals(new BigDecimal("24485.00"),c.totalAmount());assertEquals("K7Q2",c.reviewRef());}
  @Test void haridwarOverrideExample(){var c=service.calculate(input("haridwar","BT",ApplicantType.PRIVATE,LocalDate.of(2026,3,2)),LocalDate.of(2026,3,1));assertEquals(new BigDecimal("1800.00"),c.permissionFee());assertEquals(new BigDecimal("180.00"),c.urgencySurcharge());assertEquals(new BigDecimal("7500.00"),c.securityDeposit());assertEquals(new BigDecimal("27480.00"),c.totalAmount());}
  @Test void exactlyThreeDaysHasNoUrgency(){var c=service.calculate(input("dehradun","BT",ApplicantType.PRIVATE,LocalDate.of(2026,3,4)),LocalDate.of(2026,3,1));assertEquals(BigDecimal.ZERO.setScale(2),c.urgencySurcharge());}
  @Test void governmentAgencyHasZeroPermissionButDepositAndRestoration(){var c=service.calculate(input("dehradun","BT",ApplicantType.GOVERNMENT_AGENCY,LocalDate.of(2026,3,2)),LocalDate.of(2026,3,1));assertEquals(BigDecimal.ZERO.setScale(2),c.permissionFee());assertTrue(c.restorationCharge().signum()>0);assertTrue(c.securityDeposit().signum()>0);}
  @Test void inactiveRoadTypeRejected(){var ex=assertThrows(ApiException.class,()->service.calculate(input("dehradun","KUTCHA",ApplicantType.PRIVATE,LocalDate.of(2026,3,2)),LocalDate.of(2026,3,1)));assertEquals("INVALID_ROAD_TYPE",ex.getCode());}
  @Test void startDatePastRejected(){var ex=assertThrows(ApiException.class,()->service.calculate(input("dehradun","BT",ApplicantType.PRIVATE,LocalDate.of(2026,2,28)),LocalDate.of(2026,3,1)));assertEquals("INVALID_START_DATE",ex.getCode());}
  @Test void positiveDimensionAndDurationValidated(){var ex=assertThrows(ApiException.class,()->service.calculate(new RoadCuttingInput("dehradun","BT",BigDecimal.ZERO,BigDecimal.ONE,6,ApplicantType.PRIVATE,LocalDate.of(2026,3,2)),LocalDate.of(2026,3,1)));assertEquals("INVALID_DIMENSIONS",ex.getCode());}

  static class RateConfigServiceForTest extends RateConfigService {
    @Override public EffectiveConfig forTenant(String tenant){
      var bt=new RoadType("BT","Bituminous",new BigDecimal("1200"),tenant.equals("haridwar")?new BigDecimal("20"):new BigDecimal("15"),tenant.equals("haridwar")?new BigDecimal("7500"):new BigDecimal("5000"),true);
      var kutcha=new RoadType("KUTCHA","Kutcha",new BigDecimal("150"),new BigDecimal("3"),new BigDecimal("500"),false);
      return new EffectiveConfig(java.util.Map.of("BT",bt,"KUTCHA",kutcha),3,new BigDecimal("10"),new BigDecimal("25"),tenant.equals("haridwar")?"HRD":"DDN");
    }
  }
}
