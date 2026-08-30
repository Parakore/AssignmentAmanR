package com.example.rcp.controller;

import com.example.rcp.dto.ApiModels.*;
import com.example.rcp.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/rcp/v1")
public class RcpController {
  private final ApplicationService service;
  public RcpController(ApplicationService service){this.service=service;}
  @PostMapping("/_calculate") public ResponseEntity<CalculationResponse> calculate(@Valid @RequestBody CalculateRequest r){return ResponseEntity.ok(new CalculationResponse(new ResponseInfo(r.RequestInfo().msgId(),"successful"),service.calculate(r.RequestInfo(),r.Calculation())));}
  @PostMapping("/_create") public ResponseEntity<CreateResponse> create(@Valid @RequestBody CreateRequest r){return ResponseEntity.status(201).body(new CreateResponse(new ResponseInfo(r.RequestInfo().msgId(),"successful"),service.create(r.RequestInfo(),r.Application())));}
  @PostMapping("/_action") public ResponseEntity<ActionResponse> action(@Valid @RequestBody ActionRequest r){return ResponseEntity.ok(new ActionResponse(new ResponseInfo(r.RequestInfo().msgId(),"successful"),service.action(r.RequestInfo(),r.Action())));}
  @PostMapping("/_search") public ResponseEntity<SearchResponse> search(@Valid @RequestBody SearchRequest r){return ResponseEntity.ok(service.search(r.RequestInfo(),r.Search()));}
  @PostMapping("/_get") public ResponseEntity<GetResponse> get(@Valid @RequestBody GetRequest r){return ResponseEntity.ok(new GetResponse(new ResponseInfo(r.RequestInfo().msgId(),"successful"),service.get(r.RequestInfo(),r.Application())));}
  @PostMapping("/_update") public ResponseEntity<GetResponse> update(@Valid @RequestBody UpdateRequest r){return ResponseEntity.ok(new GetResponse(new ResponseInfo(r.RequestInfo().msgId(),"successful"),service.update(r.RequestInfo(),r.Application())));}
}
