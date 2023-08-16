package uk.co.bbr.web.results.forms;

import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import uk.co.bbr.services.events.types.ContestEventDateResolution;

import java.time.DateTimeException;
import java.time.LocalDate;

@Getter
@Setter
public class AddResultsDateForm {
    private String eventDate;

    public void validate(BindingResult bindingResult) {
        if (this.eventDate == null || this.eventDate.trim().length() < 4) {
            bindingResult.addError(new ObjectError("eventDate", "page.add-results.errors.date-invalid"));
            return;
        }

        int slashCount = (int) this.eventDate.chars().filter(ch -> ch == '/').count();
        LocalDate eventDate = null;
        try {
            switch (slashCount) {
                case 0 -> {
                    int year1 = Integer.parseInt(this.eventDate);
                    eventDate = LocalDate.of(year1, 1, 1);
                }
                case 1 -> {
                    String[] dateSections2 = this.eventDate.split("/");
                    int month2 = Integer.parseInt(dateSections2[0]);
                    int year2 = Integer.parseInt(dateSections2[1]);
                    eventDate = LocalDate.of(year2, month2, 1);
                }
                case 2 -> {
                    String[] dateSections3 = this.eventDate.split("/");
                    int day3 = Integer.parseInt(dateSections3[0]);
                    int month3 = Integer.parseInt(dateSections3[1]);
                    int year3 = Integer.parseInt(dateSections3[2]);
                    eventDate = LocalDate.of(year3, month3, day3);
                }
            }
        }
        catch (DateTimeException ex) {
            bindingResult.addError(new ObjectError("eventDate", "page.add-results.errors.date-invalid"));
            return;
        }

        if (eventDate == null || eventDate.getYear() < 1800 || eventDate.getYear() > 2200) {
            bindingResult.addError(new ObjectError("eventDate", "page.add-results.errors.date-invalid"));
            return;
        }
    }
}
