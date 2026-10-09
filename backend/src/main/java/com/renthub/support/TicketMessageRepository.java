package com.renthub.support;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketMessageRepository extends JpaRepository<TicketMessage,Long>{
 List<TicketMessage> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}
