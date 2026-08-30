package com.example.rcp.service;

import com.example.rcp.config.RateConfigProvider;
import com.example.rcp.dto.ApiModels.Calculation;
import com.example.rcp.dto.ApiModels.RoadCuttingInput;
import com.example.rcp.entity.Enums.ApplicantType;
import com.example.rcp.exception.ApiException;
import org.springframework.stereotype.Service;
import java.math.*;
import java.time.*;

@Service
public class FeeCalculationService {
  private final RateConfigProvider rates; private final Clock clock;
  public FeeCalculationService(RateConfigProvider rates,Clock clock){this.rates=rates;this.clock=clock;}
  public Calculation calculate(RoadCuttingInput in, LocalDate applicationDate){
    validate(in,applicationDate);
    var cfg=rates.forTenant(in.tenantId()); var road=cfg.roadTypes().get(in.roadType());
    if(road==null) throw new ApiException(400,"INVALID_ROAD_TYPE","Road type "+in.roadType()+" is not configured for tenant "+in.tenantId());
    if(!road.active()) throw new ApiException(400,"INVALID_ROAD_TYPE","Road type "+in.roadType()+" is not active for tenant "+in.tenantId());
    long area=in.lengthInMeters().multiply(in.widthInMeters()).setScale(0,RoundingMode.CEILING).longValueExact();
    BigDecimal restoration=money(BigDecimal.valueOf(area).multiply(road.restorationRatePerSqm()));
    BigDecimal permission=in.applicantType()==ApplicantType.GOVERNMENT_AGENCY?money(BigDecimal.ZERO):money(BigDecimal.valueOf(area).multiply(road.permissionRatePerSqmPerDay()).multiply(BigDecimal.valueOf(in.durationInDays())));
    long days=Duration.between(applicationDate.atStartOfDay(),in.proposedStartDate().atStartOfDay()).toDays();
    BigDecimal surcharge=days<cfg.urgencyThresholdDays()?money(permission.multiply(cfg.urgencySurchargePercent()).divide(BigDecimal.valueOf(100),10,RoundingMode.HALF_UP)):money(BigDecimal.ZERO);
    BigDecimal deposit=money(restoration.multiply(cfg.securityDepositPercent()).divide(BigDecimal.valueOf(100),10,RoundingMode.HALF_UP).max(road.minSecurityDeposit()));
    BigDecimal total=money(restoration.add(permission).add(surcharge).add(deposit));
    return new Calculation(area,restoration,permission,surcharge,deposit,total,"K7Q2");
  }
  public LocalDate today(){return LocalDate.now(clock);}
  private void validate(RoadCuttingInput in,LocalDate applicationDate){
    if(in.lengthInMeters()==null||in.lengthInMeters().compareTo(BigDecimal.ZERO)<=0||in.widthInMeters()==null||in.widthInMeters().compareTo(BigDecimal.ZERO)<=0) throw new ApiException(400,"INVALID_DIMENSIONS","Length and width must be positive");
    if(in.durationInDays()==null||in.durationInDays()<=0||in.durationInDays()>365) throw new ApiException(400,"INVALID_DURATION","Duration must be between 1 and 365 days");
    if(in.proposedStartDate()==null||in.proposedStartDate().isBefore(applicationDate)) throw new ApiException(400,"INVALID_START_DATE","Proposed start date cannot be in the past");
  }
  private BigDecimal money(BigDecimal n){return n.setScale(2,RoundingMode.HALF_UP);}
}
