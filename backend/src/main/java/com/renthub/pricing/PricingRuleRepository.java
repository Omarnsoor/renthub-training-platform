package com.renthub.pricing;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PricingRuleRepository extends JpaRepository<PricingRule,Long>{
 List<PricingRule> findByStatusOrderByPriorityAsc(String status);
}
