package uk.co.bbr.services.band.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.co.bbr.services.band.dao.BandRehearsalDayDao;

import java.util.List;

public interface BandRehearsalNightRepository extends JpaRepository<BandRehearsalDayDao, Long> {
    @Query("SELECT rd FROM BandRehearsalDayDao rd WHERE rd.band.id = ?1")
    List<BandRehearsalDayDao> fetchForBand(Long bandId);
}
