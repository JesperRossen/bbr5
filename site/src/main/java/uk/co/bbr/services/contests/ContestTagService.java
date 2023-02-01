package uk.co.bbr.services.contests;

import uk.co.bbr.services.contests.dao.ContestTagDao;

import java.util.List;
import java.util.Optional;

public interface ContestTagService {
    ContestTagDao create(String name);
    ContestTagDao create(ContestTagDao contestTag);
    ContestTagDao migrate(ContestTagDao contestTag);

    Optional<ContestTagDao> fetchByName(String name);

    List<ContestTagDao> listTagsStartingWith(String prefix);
}
