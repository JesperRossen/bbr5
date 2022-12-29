package uk.co.bbr.services.contests.dao;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.co.bbr.services.framework.AbstractDao;
import uk.co.bbr.services.framework.mixins.NameTools;

import javax.persistence.*;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name="contest_alternative_name")
public class ContestAlternativeNameDao extends AbstractDao implements NameTools {
    @Column(name="name", nullable=false)
    private String name;

    @Column(name="old_id")
    private String oldId;

    @ManyToOne(fetch= FetchType.EAGER, optional=false)
    @JoinColumn(name="contest_id")
    private ContestDao contest;

    public void setName(){
        String nameToSet = simplifyName(name);
        this.name = nameToSet;
    }
}
