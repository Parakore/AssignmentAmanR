package com.example.rcp.repository;
import com.example.rcp.entity.ApplicationTransition;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ApplicationTransitionRepository extends JpaRepository<ApplicationTransition,Long> {
    List<ApplicationTransition> findByApplicationIdOrderByCreatedTimeAsc(Long applicationId);
}
