package com.renthub.document;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserDocumentRepository extends JpaRepository<UserDocument,Long>{
 List<UserDocument> findByUserIdOrderByCreatedAtDesc(Long userId);
}
