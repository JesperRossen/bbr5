package uk.co.bbr.web.migrate;

import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.hibernate.annotations.NotFound;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import uk.co.bbr.services.bands.BandService;
import uk.co.bbr.services.bands.dao.BandDao;
import uk.co.bbr.services.bands.dao.BandPreviousNameDao;
import uk.co.bbr.services.bands.dao.BandRelationshipDao;
import uk.co.bbr.services.bands.types.BandStatus;
import uk.co.bbr.services.bands.types.RehearsalDay;
import uk.co.bbr.services.framework.NotFoundException;
import uk.co.bbr.services.regions.RegionService;
import uk.co.bbr.services.regions.dao.RegionDao;
import uk.co.bbr.services.sections.SectionService;
import uk.co.bbr.services.sections.dao.SectionDao;
import uk.co.bbr.services.security.SecurityService;
import uk.co.bbr.web.security.annotations.IsBbrAdmin;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class BandsMigrateController extends AbstractMigrateController {

    private final RegionService regionService;
    private final BandService bandService;
    private final SectionService sectionService;
    private final SecurityService securityService;

    @GetMapping("/migrate/bands")
    @IsBbrAdmin
    public String home(Model model)  {
        List<String> messages = new ArrayList<>();
        messages.add("Cloning bands repository...");

        model.addAttribute("Messages", messages);
        model.addAttribute("Next", "/migrate/bands/clone");

        return "migrate/migrate";
    }

    @GetMapping("/migrate/bands/clone")
    @IsBbrAdmin
    public String clone(Model model) throws GitAPIException {
        if (!new File(BASE_PATH).exists()) {

            Git.cloneRepository()
                    .setURI("https://github.com/BrassBandResults/bbr-data.git")
                    .setDirectory(new File(BASE_PATH))
                    .call();
        }

        List<String> messages = new ArrayList<>();
        messages.add("Repository clone complete...");

        model.addAttribute("Messages", messages);
        model.addAttribute("Next", "/migrate/bands/0");

        return "migrate/migrate";
    }

    @GetMapping("/migrate/bands/{index}")
    @IsBbrAdmin
    public String clone(Model model, @PathVariable("index") int index) throws GitAPIException, IOException, JDOMException {

        List<String> messages = new ArrayList<>();
        String[] directories = this.fetchDirectories();
        try {
            String indexLetter = directories[index];
            this.importBands(indexLetter);
            messages.add("Processing letter " + indexLetter + "...");
        }
        catch (IndexOutOfBoundsException ex) {
            return "redirect:/";
        }
        
        int nextIndex = index + 1;
        
        model.addAttribute("Messages", messages);
        model.addAttribute("Next", "/migrate/bands/" + nextIndex);

        return "migrate/migrate";
    }

    private void importBands(String indexLetter) throws JDOMException, IOException {
        File letterLevel = new File(BASE_PATH + "/Bands/" + indexLetter);
        if (letterLevel.exists()) {
            String[] files = Arrays.stream(letterLevel.list((current, name) -> new File(current, name).isFile())).sorted().toArray(String[]::new);

            for (String eachFile : files) {
                File eachBandFile = new File(BASE_PATH + "/Bands/" + indexLetter + "/" + eachFile);
                String filename = eachBandFile.getAbsolutePath();
                System.out.println(filename);

                Document doc = null;
                SAXBuilder sax = new SAXBuilder();
                doc = sax.build(new File(filename));
                Element rootNode = doc.getRootElement();

                BandDao newBand = new BandDao();
                newBand.setOldId(rootNode.getAttributeValue("id"));
                newBand.setName(rootNode.getChildText("name"));
                newBand.setSlug(rootNode.getChildText("slug"));
                if (!rootNode.getChildText("website").equals("http://")){
                    newBand.setWebsite(this.notBlank(rootNode, "website"));
                }

                newBand.setTwitterName(this.notBlank(rootNode, "twitter"));

                Optional<RegionDao> region = this.regionService.fetchBySlug(rootNode.getChild("region").getAttributeValue("slug"));
                if (region.isEmpty()) {
                    throw new NotFoundException("Region not found");
                }
                newBand.setRegion(region.get());

                newBand.setLatitude(this.notBlank(rootNode, "latitude"));
                newBand.setLongitude(this.notBlank(rootNode, "longitude"));

                newBand.setStartDate(this.notBlankDate(rootNode, "start"));
                newBand.setEndDate(this.notBlankDate(rootNode, "end"));

                String statusText = this.notBlank(rootNode, "status");
                if (statusText != null && statusText.length() > 0) {
                    newBand.setStatus(BandStatus.fromDescription(statusText));
                }
                if ("True".equalsIgnoreCase(this.notBlank(rootNode, "scratch_band"))){
                    newBand.setStatus(BandStatus.SCRATCH);
                }

                String gradingName = this.notBlank(rootNode, "grading");
                if (gradingName != null) {
                    Optional<SectionDao> section = this.sectionService.fetchByName(gradingName);
                    if (section.isEmpty()) {
                        throw new NotFoundException("Section not found " + gradingName);
                    }
                    newBand.setSection(section.get());
                }

                newBand.setMapper(this.createUser(this.notBlank(rootNode, "mapper"), this.securityService));
                newBand.setCreatedBy(this.createUser(this.notBlank(rootNode, "owner"), this.securityService));
                newBand.setUpdatedBy(this.createUser(this.notBlank(rootNode, "lastChangedBy"), this.securityService));

                newBand.setCreated(this.notBlankDateTime(rootNode, "created"));
                newBand.setUpdated(this.notBlankDateTime(rootNode, "lastModified"));

                newBand.setNotes(this.notBlank(rootNode, "notes"));

                // notes
                newBand = this.bandService.migrate(newBand);

                this.createBandRehearsalNight(newBand, this.notBlank(rootNode,"rehearsal1"));
                this.createBandRehearsalNight(newBand, this.notBlank(rootNode,"rehearsal2"));

                Element previousNames = rootNode.getChild("previous_names");
                List<Element> previousNameNodes = previousNames.getChildren();
                for (Element eachOldName : previousNameNodes) {
                    this.createPreviousName(newBand, eachOldName);
                }

                System.out.println(newBand.getName());
            }
        }
    }

    private void linkBands(String indexLetter) {
        // go through list again and do bank links
        File letterLevel = new File(BASE_PATH + "/Bands/" + indexLetter);
        String[] files = Arrays.stream(letterLevel.list((current, name) -> new File(current, name).isFile())).sorted().toArray(String[]::new);

        for (String eachFile : files) {
            File eachBandFile = new File(BASE_PATH + "/Bands/" + indexLetter + "/" + eachFile);
            String filename = eachBandFile.getAbsolutePath();

            Document doc = null;
            SAXBuilder sax = new SAXBuilder();
            try {
                doc = sax.build(new File(filename));
            } catch (JDOMException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Element rootNode = doc.getRootElement();

            String bandOldId = rootNode.getAttributeValue("id");

            Element parent1Element = rootNode.getChild("parent1");
            Element parent2Element = rootNode.getChild("parent2");

            this.createBandLink(bandOldId, parent1Element);
            this.createBandLink(bandOldId, parent2Element);
        }
    }

    private void createBandLink(String bandOldId, Element parentElement) {
        if (parentElement == null) {
            return;
        }
        Optional<BandDao> fromBandOptional = this.bandService.fetchBandByOldId(bandOldId);

        String toBandName = parentElement.getText();
        String toBandOldId = parentElement.getAttributeValue("id");
        Optional<BandDao> toBandOptional = this.bandService.fetchBandByOldId(toBandOldId);

        if (fromBandOptional.isEmpty() || toBandOptional.isEmpty()) {
            throw new NotFoundException("Can't find bands to link");
        }

        BandDao fromBand = fromBandOptional.get();
        BandDao toBand = toBandOptional.get();

        BandRelationshipDao relationship = new BandRelationshipDao();
        relationship.setRightBand(fromBand);
        relationship.setRightBandName(fromBand.getName());
        relationship.setLeftBand(toBand);
        relationship.setLeftBandName(toBandName);
        relationship.setRelationship(this.bandService.fetchIsParentOfRelationship());

        relationship.setCreatedBy(this.createUser("tjs", this.securityService));
        relationship.setUpdatedBy(this.createUser("tjs", this.securityService));
        relationship.setCreated(LocalDateTime.now());
        relationship.setUpdated(LocalDateTime.now());

        this.bandService.migrateRelationship(relationship);
    }

    private void createPreviousName(BandDao band, Element oldNameElement) {
        String name = oldNameElement.getChildText("name");
        // does it already exist?

        Optional<BandPreviousNameDao> existingAlias = this.bandService.aliasExists(band, name);
        if (existingAlias.isEmpty()) {
            BandPreviousNameDao previousName = new BandPreviousNameDao();
            previousName.setCreatedBy(this.createUser(this.notBlank(oldNameElement, "owner"), this.securityService));
            previousName.setUpdatedBy(this.createUser(this.notBlank(oldNameElement, "lastChangedBy"), this.securityService));
            previousName.setCreated(this.notBlankDateTime(oldNameElement, "created"));
            previousName.setUpdated(this.notBlankDateTime(oldNameElement, "lastModified"));
            previousName.setOldName(name);
            previousName.setStartDate(this.notBlankDate(oldNameElement, "start"));
            previousName.setEndDate(this.notBlankDate(oldNameElement, "end"));
            previousName.setHidden(this.notBlankBoolean(oldNameElement, "hidden"));

            if (previousName.getCreatedBy() == null) {
                previousName.setCreatedBy(band.getCreatedBy());
            }
            if (previousName.getUpdatedBy() == null) {
                previousName.setUpdatedBy(band.getUpdatedBy());
            }

            this.bandService.migratePreviousName(band, previousName);
        }
    }

    private void createBandRehearsalNight(BandDao band, String rehearsalNight) {
        if (rehearsalNight != null) {
            this.bandService.migrateRehearsalNight(band, RehearsalDay.fromName(rehearsalNight));
        }
    }
}
