export type RoleCode='APPLICANT'|'VERIFIER'|'APPROVER';
export type Identity={uuid:string;userName:string;tenantId:string;roles:{code:RoleCode}[]};
export type CalcInput={tenantId:string;roadType:string;lengthInMeters:number;widthInMeters:number;durationInDays:number;applicantType:'PRIVATE'|'GOVERNMENT_AGENCY';proposedStartDate:string};
export type Calculation={areaInSqm:number;restorationCharge:number;permissionFee:number;urgencySurcharge:number;securityDeposit:number;totalAmount:number;reviewRef:string};
export type ApplicationView=CalcInput & {applicationNumber:string;status:string;applicantUuid:string;mobileNumber:string;applicationDate:string;Calculation:Calculation;availableActions:string[];history:Transition[]};
export type Transition={action:string;fromStatus:string|null;toStatus:string;actorUuid:string;actorRole:string;comment:string|null;timestamp:string};
