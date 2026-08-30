package com.example.rcp.entity;

public final class Enums {
    private Enums() {}
    public enum Status { APPLIED, PENDING_APPROVAL, APPROVED, REJECTED, CANCELLED }
    public enum ApplicantType { PRIVATE, GOVERNMENT_AGENCY }
}
