CREATE SEQUENCE application_number_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE applications (
    id BIGSERIAL PRIMARY KEY,
    application_number VARCHAR(40) NOT NULL UNIQUE,
    tenant_id VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    applicant_uuid VARCHAR(128) NOT NULL,
    mobile_number VARCHAR(32) NOT NULL,
    applicant_type VARCHAR(32) NOT NULL,
    road_type VARCHAR(32) NOT NULL,
    length_in_meters NUMERIC(12,3) NOT NULL,
    width_in_meters NUMERIC(12,3) NOT NULL,
    duration_in_days INTEGER NOT NULL,
    proposed_start_date DATE NOT NULL,
    application_date DATE NOT NULL,
    area_in_sqm BIGINT NOT NULL,
    restoration_charge NUMERIC(18,2) NOT NULL,
    permission_fee NUMERIC(18,2) NOT NULL,
    urgency_surcharge NUMERIC(18,2) NOT NULL,
    security_deposit NUMERIC(18,2) NOT NULL,
    total_amount NUMERIC(18,2) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(128) NOT NULL,
    created_time TIMESTAMP WITH TIME ZONE NOT NULL,
    last_modified_by VARCHAR(128) NOT NULL,
    last_modified_time TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX idx_app_tenant_status ON applications(tenant_id, status);
CREATE INDEX idx_app_tenant_mobile ON applications(tenant_id, mobile_number);
CREATE INDEX idx_app_tenant_applicant ON applications(tenant_id, applicant_uuid);

CREATE TABLE application_transitions (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    from_status VARCHAR(32),
    to_status VARCHAR(32) NOT NULL,
    action VARCHAR(32) NOT NULL,
    actor_uuid VARCHAR(128) NOT NULL,
    actor_role VARCHAR(32) NOT NULL,
    comment VARCHAR(1000),
    created_by VARCHAR(128) NOT NULL,
    created_time TIMESTAMP WITH TIME ZONE NOT NULL,
    last_modified_by VARCHAR(128) NOT NULL,
    last_modified_time TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX idx_transition_application ON application_transitions(application_id, created_time);
