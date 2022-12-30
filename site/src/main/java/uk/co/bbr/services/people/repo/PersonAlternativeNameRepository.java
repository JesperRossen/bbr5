package uk.co.bbr.services.people.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.co.bbr.services.people.dao.PersonAliasDao;

import java.util.List;

public interface PersonAlternativeNameRepository extends JpaRepository<PersonAliasDao, Long> {
    @Query("SELECT a FROM PersonAliasDao a WHERE a.person.id = ?1")
    List<PersonAliasDao> findForPersonId(Long personId);
}
