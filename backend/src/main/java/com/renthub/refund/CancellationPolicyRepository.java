package com.renthub.refund;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CancellationPolicyRepository extends JpaRepository<CancellationPolicy,Long> {
  List<CancellationPolicy> findByStatusOrderByPriorityAsc(String status);
}
