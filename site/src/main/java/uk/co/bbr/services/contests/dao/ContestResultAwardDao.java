package uk.co.bbr.services.contests.dao;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.co.bbr.services.framework.AbstractDao;
import uk.co.bbr.services.framework.mixins.NameTools;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name="contest_result_award")
public class ContestResultAwardDao extends AbstractDao implements NameTools {

    @ManyToOne(fetch= FetchType.EAGER, optional=false)
    @JoinColumn(name="contest_result_id")
    private ContestResultDao contestResult;

    @ManyToOne(fetch= FetchType.EAGER, optional=false)
    @JoinColumn(name="award_type_id")
    private ContestResultAwardTypeDao awardType;

    @Column(name="description", nullable=false)
    private String description;

}
