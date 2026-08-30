package com.example.rcp.config;
public interface RateConfigProvider { RateConfigService.EffectiveConfig forTenant(String tenantId); }
