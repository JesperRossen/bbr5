package uk.co.bbr.services.people;

import uk.co.bbr.services.bands.dao.BandDao;
import uk.co.bbr.services.events.dao.ContestAdjudicatorDao;
import uk.co.bbr.services.events.dao.ContestResultDao;
import uk.co.bbr.services.people.dao.PersonAliasDao;
import uk.co.bbr.services.people.dao.PersonDao;
import uk.co.bbr.services.people.dto.ConductorCompareDto;
import uk.co.bbr.services.people.dto.PeopleListDto;
import uk.co.bbr.services.people.sql.dto.PeopleBandsSqlDto;
import uk.co.bbr.services.people.sql.dto.PeopleWinnersSqlDto;
import uk.co.bbr.services.security.dao.SiteUserDao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PersonService {
    PersonDao create(PersonDao person);
    PersonDao create(String surname, String firstNames);
    PersonDao update(PersonDao person);

    Optional<PersonDao> fetchBySlug(String personSlug);

    Optional<PersonDao> fetchById(long personId);

    PeopleListDto listPeopleStartingWith(String prefix);

    int fetchAdjudicationCount(PersonDao person);

    List<ContestAdjudicatorDao> fetchAdjudications(PersonDao person);

    int fetchComposerCount(PersonDao person);

    int fetchArrangerCount(PersonDao person);

    int fetchUserAdjudicationsCount(SiteUserDao user, PersonDao person);

    List<PeopleWinnersSqlDto> fetchContestWinningPeople();

    List<PeopleWinnersSqlDto> fetchContestWinningPeopleBefore(int year);

    List<PeopleWinnersSqlDto> fetchContestWinningPeopleAfter(int year);

    List<PeopleBandsSqlDto> fetchBandsConductedList();

    List<PeopleBandsSqlDto> fetchBandsConductedListBefore(int year);

    List<PeopleBandsSqlDto> fetchBandsConductedListAfter(int year);

    ConductorCompareDto compareConductors(PersonDao leftPerson, PersonDao rightPerson);

    List<ContestResultDao> fetchPersonalAdjudications(SiteUserDao currentUser, PersonDao person);

    void delete(PersonDao person);
}
