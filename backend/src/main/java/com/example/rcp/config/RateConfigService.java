package com.example.rcp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

@Service
public class RateConfigService implements RateConfigProvider {
    public record RoadType(String code,String name,BigDecimal restorationRatePerSqm,BigDecimal permissionRatePerSqmPerDay,BigDecimal minSecurityDeposit,boolean active) {}
    private record RawRoadType(String code,String name,BigDecimal restorationRatePerSqm,BigDecimal permissionRatePerSqmPerDay,BigDecimal minSecurityDeposit,Boolean active) {}
    private record RawConfig(RawDefaults defaults,Map<String,RawTenant> tenants) {}
    private record RawDefaults(List<RawRoadType> roadTypes,int urgencyThresholdDays,BigDecimal urgencySurchargePercent,BigDecimal securityDepositPercent) {}
    private record RawTenant(String applicationPrefix,List<RawRoadType> roadTypes) {}
    public record EffectiveConfig(Map<String,RoadType> roadTypes,int urgencyThresholdDays,BigDecimal urgencySurchargePercent,BigDecimal securityDepositPercent,String applicationPrefix) {}
    private RawConfig raw;
    @PostConstruct void load(){
        try(InputStream in=new ClassPathResource("config/rates.json").getInputStream()){raw=new ObjectMapper().readValue(in,RawConfig.class);}catch(Exception e){throw new IllegalStateException("Could not load rate configuration",e);}
    }
    public EffectiveConfig forTenant(String tenantId){
        RawTenant tenant=raw.tenants().get(tenantId); if(tenant==null) throw new com.example.rcp.exception.ApiException(400,"UNKNOWN_TENANT","Tenant "+tenantId+" is not configured");
        Map<String,RoadType> result=new LinkedHashMap<>();
        for(RawRoadType r:raw.defaults().roadTypes()) result.put(r.code(),new RoadType(r.code(),r.name(),r.restorationRatePerSqm(),r.permissionRatePerSqmPerDay(),r.minSecurityDeposit(),Boolean.TRUE.equals(r.active())));
        if(tenant.roadTypes()!=null) for(RawRoadType o:tenant.roadTypes()){
            RoadType base=result.get(o.code()); if(base==null) continue;
            result.put(o.code(),new RoadType(base.code(),o.name()!=null?o.name():base.name(),o.restorationRatePerSqm()!=null?o.restorationRatePerSqm():base.restorationRatePerSqm(),o.permissionRatePerSqmPerDay()!=null?o.permissionRatePerSqmPerDay():base.permissionRatePerSqmPerDay(),o.minSecurityDeposit()!=null?o.minSecurityDeposit():base.minSecurityDeposit(),o.active()!=null?o.active():base.active()));
        }
        return new EffectiveConfig(result,raw.defaults().urgencyThresholdDays(),raw.defaults().urgencySurchargePercent(),raw.defaults().securityDepositPercent(),tenant.applicationPrefix());
    }
}
