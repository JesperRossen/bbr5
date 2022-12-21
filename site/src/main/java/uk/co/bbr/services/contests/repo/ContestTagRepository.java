package uk.co.bbr.services.contests.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.co.bbr.services.contests.dao.ContestDao;
import uk.co.bbr.services.contests.dao.ContestTagDao;

public interface ContestTagRepository extends JpaRepository<ContestTagDao, Long> {
}
