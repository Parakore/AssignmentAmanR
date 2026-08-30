package com.example.rcp.config;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
@Service
public class WorkflowConfigService {
  public record Transition(String action,String from,String to,List<String> roles) {}
  private record Root(List<Transition> transitions) {}
  private List<Transition> transitions;
  @PostConstruct void load(){try(InputStream in=new ClassPathResource("config/workflow.json").getInputStream()){transitions=new ObjectMapper().readValue(in,Root.class).transitions();}catch(Exception e){throw new IllegalStateException("Could not load workflow configuration",e);}}
  public Transition find(String action,String from,String role){return transitions.stream().filter(t->t.action().equals(action)&&t.from().equals(from)&&t.roles().contains(role)).findFirst().orElseThrow(()->new com.example.rcp.exception.ApiException(400,"ILLEGAL_TRANSITION","Action "+action+" is not allowed from "+from+" for role "+role));}
  public List<String> actionsFor(String status,List<String> roles){return transitions.stream().filter(t->t.from().equals(status)&&t.roles().stream().anyMatch(roles::contains)).map(Transition::action).collect(Collectors.toList());}
}
