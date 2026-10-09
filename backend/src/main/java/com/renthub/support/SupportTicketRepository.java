package com.renthub.support;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SupportTicketRepository extends JpaRepository<SupportTicket,Long>{
 List<SupportTicket> findByUserIdOrderByUpdatedAtDesc(Long userId);
 List<SupportTicket> findByStatusOrderByUpdatedAtAsc(String status);
}
