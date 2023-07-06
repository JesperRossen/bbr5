package uk.co.bbr.web.venues.forms;

import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import uk.co.bbr.services.contests.dao.ContestDao;
import uk.co.bbr.services.venues.dao.VenueDao;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;

@Getter
@Setter
public class VenueEditForm {
    private String name;
    private Long region;
    private String latitude;
    private String longitude;
    private String notes;
    private String parentRegion;

    public VenueEditForm() {
        super();
    }

    public VenueEditForm(VenueDao venue) {
        assertNotNull(venue);

        this.name = venue.getName();
        this.region = venue.getRegion().getId();
        this.latitude = venue.getLatitude();
        this.longitude = venue.getLongitude();
        this.notes = venue.getNotes();
        if (venue.getParent() != null) {
            this.parentRegion = venue.getParent().getName();
        }
    }

    public void validate(BindingResult bindingResult) {
        if (this.name == null || this.name.trim().length() == 0) {
            bindingResult.addError(new ObjectError("name", "page.contest-edit.errors.name-required"));
        }
    }
}
