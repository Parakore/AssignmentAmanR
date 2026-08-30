package com.example.rcp.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity @Table(name="application_transitions", indexes=@Index(name="idx_transition_application", columnList="application_id,created_time"))
public class ApplicationTransition {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="application_id", nullable=false) private Application application;
    @Column(name="from_status") private String fromStatus;
    @Column(name="to_status", nullable=false) private String toStatus;
    @Column(nullable=false) private String action;
    @Column(name="actor_uuid", nullable=false) private String actorUuid;
    @Column(name="actor_role", nullable=false) private String actorRole;
    private String comment;
    @Column(name="created_by", nullable=false) private String createdBy;
    @Column(name="created_time", nullable=false) private OffsetDateTime createdTime;
    @Column(name="last_modified_by", nullable=false) private String lastModifiedBy;
    @Column(name="last_modified_time", nullable=false) private OffsetDateTime lastModifiedTime;
    public Long getId(){return id;} public Application getApplication(){return application;} public void setApplication(Application v){application=v;}
    public String getFromStatus(){return fromStatus;} public void setFromStatus(String v){fromStatus=v;} public String getToStatus(){return toStatus;} public void setToStatus(String v){toStatus=v;}
    public String getAction(){return action;} public void setAction(String v){action=v;} public String getActorUuid(){return actorUuid;} public void setActorUuid(String v){actorUuid=v;}
    public String getActorRole(){return actorRole;} public void setActorRole(String v){actorRole=v;} public String getComment(){return comment;} public void setComment(String v){comment=v;}
    public String getCreatedBy(){return createdBy;} public void setCreatedBy(String v){createdBy=v;} public OffsetDateTime getCreatedTime(){return createdTime;} public void setCreatedTime(OffsetDateTime v){createdTime=v;}
    public String getLastModifiedBy(){return lastModifiedBy;} public void setLastModifiedBy(String v){lastModifiedBy=v;} public OffsetDateTime getLastModifiedTime(){return lastModifiedTime;} public void setLastModifiedTime(OffsetDateTime v){lastModifiedTime=v;}
}
