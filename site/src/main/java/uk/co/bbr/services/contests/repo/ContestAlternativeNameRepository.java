package uk.co.bbr.services.contests.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.co.bbr.services.contests.dao.ContestAliasDao;

public interface ContestAlternativeNameRepository extends JpaRepository<ContestAliasDao, Long> {
}
