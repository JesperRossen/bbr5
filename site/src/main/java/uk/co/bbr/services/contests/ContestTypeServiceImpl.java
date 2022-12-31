package uk.co.bbr.services.contests;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.co.bbr.services.contests.dao.ContestTagDao;
import uk.co.bbr.services.contests.dao.ContestTypeDao;
import uk.co.bbr.services.contests.repo.ContestTagRepository;
import uk.co.bbr.services.contests.repo.ContestTypeRepository;
import uk.co.bbr.services.framework.ValidationException;
import uk.co.bbr.services.framework.mixins.SlugTools;
import uk.co.bbr.services.security.SecurityService;
import uk.co.bbr.web.security.annotations.IsBbrMember;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ContestTypeServiceImpl implements ContestTypeService, SlugTools {

    private static final String DEFAULT_SLUG = "test-piece-contest";

    private final SecurityService securityService;

    private final ContestTypeRepository contestTypeRepository;

    @Override
    public ContestTypeDao fetchDefaultContestType() {
        return this.contestTypeRepository.fetchBySlug(DEFAULT_SLUG);
    }
}
