package uk.co.bbr.services.contests;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.co.bbr.services.contests.dao.ContestEventDao;
import uk.co.bbr.services.contests.dao.ContestGroupDao;
import uk.co.bbr.services.contests.dao.ContestResultDao;
import uk.co.bbr.services.contests.repo.ContestEventRepository;
import uk.co.bbr.services.contests.repo.ContestGroupRepository;
import uk.co.bbr.services.contests.repo.ContestResultRepository;
import uk.co.bbr.services.contests.types.ContestGroupType;
import uk.co.bbr.services.contests.types.ResultPositionType;
import uk.co.bbr.services.framework.ValidationException;
import uk.co.bbr.services.framework.mixins.SlugTools;
import uk.co.bbr.services.security.SecurityService;
import uk.co.bbr.web.security.annotations.IsBbrMember;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ContestResultServiceImpl implements ContestResultService {

    private final ContestResultRepository contestResultRepository;

    @Override
    @IsBbrMember
    public ContestResultDao addResult(ContestEventDao event, ContestResultDao result) {
        result.setContestEvent(event);

        // default in band name if not specified
        if (StringUtils.isBlank(result.getBandName())) {
            result.setBandName(result.getBand().getName());
        }

        // default in conductor name if not specified
        if (StringUtils.isBlank(result.getConductorName())) {
            result.setConductorName(result.getConductor().getName());
        }

        // default in the result position type
        if (result.getResultPositionType() == null) {
            if (result.getPosition() != null && result.getPosition() > 0) {
                result.setResultPositionType(ResultPositionType.RESULT);
            } else {
                result.setResultPositionType(ResultPositionType.UNKNOWN);
            }
        }

        return this.contestResultRepository.saveAndFlush(result);
    }
}
