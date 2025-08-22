package de.hs_esslingen.besy.repositories;

import de.hs_esslingen.besy.models.Approval;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalRepository extends JpaRepository<Approval, Long> {
}