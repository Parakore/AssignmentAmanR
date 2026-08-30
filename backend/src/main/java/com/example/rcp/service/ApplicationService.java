package com.example.rcp.service;

import com.example.rcp.config.WorkflowConfigService;
import com.example.rcp.config.RateConfigProvider;
import com.example.rcp.dto.ApiModels.*;
import com.example.rcp.entity.*;
import com.example.rcp.entity.Enums.*;
import com.example.rcp.exception.ApiException;
import com.example.rcp.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ApplicationService {
  private final ApplicationRepository repo; private final ApplicationTransitionRepository history; private final FeeCalculationService fee; private final WorkflowConfigService workflow; private final RateConfigProvider rates; private final Clock clock; private final int maxLimit;
  public ApplicationService(ApplicationRepository repo,ApplicationTransitionRepository history,FeeCalculationService fee,WorkflowConfigService workflow,RateConfigProvider rates,Clock clock,@Value("${app.search.max-limit:100}") int maxLimit){this.repo=repo;this.history=history;this.fee=fee;this.workflow=workflow;this.rates=rates;this.clock=clock;this.maxLimit=maxLimit;}

  public Calculation calculate(RequestInfo info,RoadCuttingInput input){checkTenant(info.userInfo(),input.tenantId()); requireRole(info.userInfo(),"APPLICANT"); return fee.calculate(input,fee.today());}

  @Transactional
  public ApplicationView create(RequestInfo info,CreateInput in){
    checkTenant(info.userInfo(),in.tenantId()); requireRole(info.userInfo(),"APPLICANT");
    LocalDate today=fee.today();
    Calculation c=fee.calculate(new RoadCuttingInput(in.tenantId(),in.roadType(),in.lengthInMeters(),in.widthInMeters(),in.durationInDays(),in.applicantType(),in.proposedStartDate()),today);
    Application a=new Application(); a.setTenantId(in.tenantId()); a.setApplicantUuid(info.userInfo().uuid()); a.setMobileNumber(in.mobileNumber()); a.setApplicantType(in.applicantType()); a.setRoadType(in.roadType()); a.setLengthInMeters(in.lengthInMeters()); a.setWidthInMeters(in.widthInMeters()); a.setDurationInDays(in.durationInDays()); a.setProposedStartDate(in.proposedStartDate()); a.setApplicationDate(today); a.setStatus(Status.APPLIED); applyCalc(a,c); a.setCreatedBy(info.userInfo().uuid()); a.setCreatedTime(OffsetDateTime.now(clock)); a.setLastModifiedBy(info.userInfo().uuid()); a.setLastModifiedTime(OffsetDateTime.now(clock));
    try { long seq=((Number)repo.getEntityManagerSequence()).longValue(); a.setApplicationNumber(number(in.tenantId(),seq,today)); a=repo.saveAndFlush(a); } catch(DataIntegrityViolationException e){throw new ApiException(409,"APPLICATION_NUMBER_CONFLICT","Could not allocate a unique application number; please retry");}
    record(a,null,Status.APPLIED,"CREATE",info.userInfo(),null,"APPLICANT"); return view(a,info.userInfo());
  }

  @Transactional
  public ApplicationView update(RequestInfo info,UpdateInput in){
    checkTenant(info.userInfo(),in.tenantId()); requireRole(info.userInfo(),"APPLICANT");
    Application a=findOwned(info.userInfo(),in.tenantId(),in.applicationNumber());
    if(a.getStatus()!=Status.APPLIED) throw new ApiException(400,"NON_EDITABLE_STATE","Application can only be edited while APPLIED");
    Calculation c=fee.calculate(new RoadCuttingInput(in.tenantId(),in.roadType(),in.lengthInMeters(),in.widthInMeters(),in.durationInDays(),in.applicantType(),in.proposedStartDate()),a.getApplicationDate());
    a.setMobileNumber(in.mobileNumber()); a.setRoadType(in.roadType()); a.setLengthInMeters(in.lengthInMeters()); a.setWidthInMeters(in.widthInMeters()); a.setDurationInDays(in.durationInDays()); a.setApplicantType(in.applicantType()); a.setProposedStartDate(in.proposedStartDate()); applyCalc(a,c); touch(a,info.userInfo().uuid()); repo.save(a); return view(a,info.userInfo());
  }

  @Transactional
  public ApplicationView action(RequestInfo info,ActionInput in){
    checkTenant(info.userInfo(),in.tenantId()); Application a=findTenant(info.userInfo(),in.tenantId(),in.applicationNumber());
    String role = matchingRole(info.userInfo(), in.action(), a.getStatus().name());
    var transition=workflow.find(in.action(),a.getStatus().name(),role);
    Status target=Status.valueOf(transition.to());
    try { a.setStatus(target); touch(a,info.userInfo().uuid()); repo.saveAndFlush(a); record(a,transition.from(),target,in.action(),info.userInfo(),in.comment(),role); return view(a,info.userInfo()); }
    catch(ObjectOptimisticLockingFailureException e){throw new ApiException(409,"CONCURRENT_MODIFICATION","Application was changed by another officer. Refresh and try again");}
  }

  @Transactional(readOnly=true)
  public ApplicationView get(RequestInfo info,GetInput in){checkTenant(info.userInfo(),in.tenantId()); Application a=findTenant(info.userInfo(),in.tenantId(),in.applicationNumber()); if(hasRole(info.userInfo(),"APPLICANT")&&!a.getApplicantUuid().equals(info.userInfo().uuid())) throw new ApiException(403,"FORBIDDEN","Applicants can only access their own applications"); return view(a,info.userInfo());}

  @Transactional(readOnly=true)
  public SearchResponse search(RequestInfo info,SearchInput in){
    checkTenant(info.userInfo(),in.tenantId()); int offset=in.offset()==null?0:in.offset(), limit=in.limit()==null?20:in.limit(); if(limit>maxLimit) limit=maxLimit;
    String applicant=hasRole(info.userInfo(),"APPLICANT")?info.userInfo().uuid():null;
    Page<Application> page=repo.search(in.tenantId(),blank(in.applicationNumber()),in.status(),blank(in.mobileNumber()),applicant,PageRequest.of(offset/limit,limit,Sort.by(Sort.Direction.DESC,"createdTime")));
    List<ApplicationView> list=page.getContent().stream().filter(a->offset==0 || true).map(a->view(a,info.userInfo())).toList();
    // Repository pagination is page-based; exact offset is emulated for non-page-aligned offsets below.
    if(offset%limit!=0){Pageable p=PageRequest.of(0,offset+limit,Sort.by(Sort.Direction.DESC,"createdTime")); page=repo.search(in.tenantId(),blank(in.applicationNumber()),in.status(),blank(in.mobileNumber()),applicant,p); list=page.getContent().stream().skip(offset).limit(limit).map(a->view(a,info.userInfo())).toList();}
    return new SearchResponse(new ResponseInfo(info.msgId(),"successful"),list,page.getTotalElements(),offset,limit);
  }

  private Application findTenant(UserInfo u,String tenant,String number){return repo.findByApplicationNumberAndTenantId(number,tenant).orElseThrow(()->new ApiException(404,"APPLICATION_NOT_FOUND","Application "+number+" was not found for tenant "+tenant));}
  private Application findOwned(UserInfo u,String tenant,String number){Application a=findTenant(u,tenant,number); if(!a.getApplicantUuid().equals(u.uuid())) throw new ApiException(403,"FORBIDDEN","You can only modify your own applications"); return a;}
  private void checkTenant(UserInfo u,String tenant){if(!u.tenantId().equals(tenant)) throw new ApiException(403,"TENANT_ACCESS_DENIED","Request tenant does not match caller tenant");}
  private void requireRole(UserInfo u,String role){if(!hasRole(u,role)) throw new ApiException(403,"FORBIDDEN","Caller must have role "+role);}
  private boolean hasRole(UserInfo u,String role){return u.roles().stream().anyMatch(r->r.code().equals(role));}
  private String matchingRole(UserInfo u,String action,String status){for(Role r:u.roles()) try{workflow.find(action,status,r.code());return r.code();}catch(ApiException ignored){} throw new ApiException(403,"FORBIDDEN","Caller role cannot perform action "+action+" from "+status);}
  private void applyCalc(Application a,Calculation c){a.setAreaInSqm(c.areaInSqm());a.setRestorationCharge(c.restorationCharge());a.setPermissionFee(c.permissionFee());a.setUrgencySurcharge(c.urgencySurcharge());a.setSecurityDeposit(c.securityDeposit());a.setTotalAmount(c.totalAmount());}
  private void touch(Application a,String actor){a.setLastModifiedBy(actor);a.setLastModifiedTime(OffsetDateTime.now(clock));}
  private void record(Application a,String from,Status to,String action,UserInfo u,String comment,String role){ApplicationTransition t=new ApplicationTransition();t.setApplication(a);t.setFromStatus(from);t.setToStatus(to.name());t.setAction(action);t.setActorUuid(u.uuid());t.setActorRole(role);t.setComment(comment);t.setCreatedBy(u.uuid());t.setCreatedTime(OffsetDateTime.now(clock));t.setLastModifiedBy(u.uuid());t.setLastModifiedTime(t.getCreatedTime());history.save(t);}
  private ApplicationView view(Application a,UserInfo u){List<TransitionView> h=history.findByApplicationIdOrderByCreatedTimeAsc(a.getId()).stream().map(t->new TransitionView(t.getAction(),t.getFromStatus(),t.getToStatus(),t.getActorUuid(),t.getActorRole(),t.getComment(),t.getCreatedTime())).toList(); List<String> actions=workflow.actionsFor(a.getStatus().name(),u.roles().stream().map(Role::code).toList()); Calculation c=new Calculation(a.getAreaInSqm(),a.getRestorationCharge(),a.getPermissionFee(),a.getUrgencySurcharge(),a.getSecurityDeposit(),a.getTotalAmount(),"K7Q2"); return new ApplicationView(a.getApplicationNumber(),a.getTenantId(),a.getStatus().name(),a.getApplicantUuid(),a.getMobileNumber(),a.getApplicantType().name(),a.getRoadType(),a.getLengthInMeters(),a.getWidthInMeters(),a.getDurationInDays(),a.getProposedStartDate(),a.getApplicationDate(),c,actions,h);}
  private String blank(String s){return s==null||s.isBlank()?null:s;}
  private String number(String tenant,long seq,LocalDate d){String prefix=rates.forTenant(tenant).applicationPrefix(); if(prefix==null||prefix.isBlank()) throw new ApiException(400,"TENANT_CONFIG_ERROR","Tenant "+tenant+" has no application prefix configured"); String fy=(d.getMonthValue()>=4?d.getYear():d.getYear()-1)+"-"+String.format("%02d",(d.getMonthValue()>=4?(d.getYear()+1)%100:d.getYear()%100));return prefix+"-RCP-"+String.format("%06d",seq)+"-"+fy;}
}
