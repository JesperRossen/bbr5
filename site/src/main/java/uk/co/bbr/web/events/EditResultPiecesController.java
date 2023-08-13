package uk.co.bbr.web.events;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import uk.co.bbr.services.contests.ContestService;
import uk.co.bbr.services.contests.ContestTypeService;
import uk.co.bbr.services.events.ContestEventService;
import uk.co.bbr.services.events.ResultService;
import uk.co.bbr.services.events.dao.ContestEventDao;
import uk.co.bbr.services.events.dao.ContestEventTestPieceDao;
import uk.co.bbr.services.events.dao.ContestResultDao;
import uk.co.bbr.services.events.dao.ContestResultPieceDao;
import uk.co.bbr.services.events.types.TestPieceAndOr;
import uk.co.bbr.services.framework.NotFoundException;
import uk.co.bbr.services.pieces.PieceService;
import uk.co.bbr.services.pieces.dao.PieceDao;
import uk.co.bbr.services.venues.VenueService;
import uk.co.bbr.web.events.forms.AddEventSetTestForm;
import uk.co.bbr.web.events.forms.AddResultPieceForm;
import uk.co.bbr.web.security.annotations.IsBbrMember;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class EditResultPiecesController {

    private final ContestService contestService;
    private final ContestEventService contestEventService;
    private final ResultService resultService;
    private final ContestTypeService contestTypeService;
    private final PieceService pieceService;
    private final VenueService venueService;

    @IsBbrMember
    @GetMapping("/contests/{contestSlug:[\\-a-z\\d]{2,}}/{contestEventDate:\\d{4}-\\d{2}-\\d{2}}/result/{resultId:\\d+}/edit-pieces")
    public String editSetTests(Model model, @PathVariable String contestSlug, @PathVariable String contestEventDate, @PathVariable Long resultId) {
        String[] dateSplit = contestEventDate.split("-");
        LocalDate eventDate = LocalDate.of(Integer.parseInt(dateSplit[0]), Integer.parseInt(dateSplit[1]), Integer.parseInt(dateSplit[2]));
        Optional<ContestEventDao> contestEvent = this.contestEventService.fetchEvent(contestSlug, eventDate);

        if (contestEvent.isEmpty()) {
            throw NotFoundException.eventNotFound(contestSlug, contestEventDate);
        }

        Optional<ContestResultDao> result = this.resultService.fetchById(resultId);
        if (result.isEmpty()) {
            throw NotFoundException.resultNotFoundById(resultId);
        }

        AddResultPieceForm editForm = new AddResultPieceForm();

        List<ContestResultPieceDao> resultPieces = this.resultService.listResultPieces(result.get());

        model.addAttribute("ContestEvent", contestEvent.get());
        model.addAttribute("ContestResult", result.get());
        model.addAttribute("Form", editForm);
        model.addAttribute("Pieces", resultPieces);

        return "events/edit-result-pieces";
    }

    @IsBbrMember
    @PostMapping("/contests/{contestSlug:[\\-a-z\\d]{2,}}/{contestEventDate:\\d{4}-\\d{2}-\\d{2}}/result/{resultId:\\d+}/edit-pieces")
    public String editSetTestsAdd(Model model, @Valid @ModelAttribute("Form") AddResultPieceForm submittedForm, BindingResult bindingResult, @PathVariable String contestSlug, @PathVariable String contestEventDate, @PathVariable Long resultId) {
        String[] dateSplit = contestEventDate.split("-");
        LocalDate eventDate = LocalDate.of(Integer.parseInt(dateSplit[0]), Integer.parseInt(dateSplit[1]), Integer.parseInt(dateSplit[2]));
        Optional<ContestEventDao> contestEvent = this.contestEventService.fetchEvent(contestSlug, eventDate);

        if (contestEvent.isEmpty()) {
            throw NotFoundException.eventNotFound(contestSlug, contestEventDate);
        }

        Optional<ContestResultDao> result = this.resultService.fetchById(resultId);
        if (result.isEmpty()) {
            throw NotFoundException.resultNotFoundById(resultId);
        }

        submittedForm.validate(bindingResult);

        if (bindingResult.hasErrors()) {
            List<ContestResultPieceDao> resultPieces = this.resultService.listResultPieces(result.get());

            model.addAttribute("ContestEvent", contestEvent.get());
            model.addAttribute("ContestResult", result.get());
            model.addAttribute("Pieces", resultPieces);
            return "events/edit-result-pieces";
        }

        Optional<PieceDao> piece = this.pieceService.fetchBySlug(submittedForm.getPieceSlug());
        if (piece.isEmpty()) {
            List<ContestResultPieceDao> resultPieces = this.resultService.listResultPieces(result.get());

            model.addAttribute("ContestEvent", contestEvent.get());
            model.addAttribute("ContestResult", result.get());
            model.addAttribute("Pieces", resultPieces);
            return "events/edit-result-pieces";
        }

        String suffix = submittedForm.getSuffix();
        this.resultService.addPieceToResult(result.get(), piece.get(), suffix);

        return "redirect:/contests/{contestSlug}/{contestEventDate}/result/{resultId}/edit-pieces";
    }

    @IsBbrMember
    @GetMapping("/contests/{contestSlug:[\\-a-z\\d]{2,}}/{contestEventDate:\\d{4}-\\d{2}-\\d{2}}/result/{resultId:\\d+}/{resultPieceId:\\d+}/delete")
    public String removeSetTest(Model model, @PathVariable String contestSlug, @PathVariable String contestEventDate, @PathVariable Long resultId, @PathVariable Long resultPieceId) {
        String[] dateSplit = contestEventDate.split("-");
        LocalDate eventDate = LocalDate.of(Integer.parseInt(dateSplit[0]), Integer.parseInt(dateSplit[1]), Integer.parseInt(dateSplit[2]));
        Optional<ContestEventDao> contestEvent = this.contestEventService.fetchEvent(contestSlug, eventDate);
        if (contestEvent.isEmpty()) {
            throw NotFoundException.eventNotFound(contestSlug, contestEventDate);
        }

        Optional<ContestResultDao> result = this.resultService.fetchById(resultId);
        if (result.isEmpty()) {
            throw NotFoundException.resultNotFoundById(resultId);
        }

        Optional<ContestResultPieceDao> piece = this.resultService.fetchResultPieceById(result.get(), resultPieceId);
        if (piece.isEmpty()) {
            throw NotFoundException.setTestPieceNotFoundById();
        }

        this.resultService.removePiece(contestEvent.get(), result.get(), piece.get());

        return "redirect:/contests/{contestSlug}/{contestEventDate}/result/{resultId}/edit-pieces";
    }
}
