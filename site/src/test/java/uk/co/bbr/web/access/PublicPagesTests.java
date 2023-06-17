package uk.co.bbr.web.access;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.co.bbr.services.bands.BandAliasService;
import uk.co.bbr.services.bands.BandRelationshipService;
import uk.co.bbr.services.bands.BandService;
import uk.co.bbr.services.events.ContestEventService;
import uk.co.bbr.services.groups.ContestGroupService;
import uk.co.bbr.services.events.ResultService;
import uk.co.bbr.services.contests.ContestService;
import uk.co.bbr.services.people.PersonAliasService;
import uk.co.bbr.services.tags.ContestTagService;
import uk.co.bbr.services.people.PersonRelationshipService;
import uk.co.bbr.services.people.PersonService;
import uk.co.bbr.services.pieces.PieceService;
import uk.co.bbr.services.regions.RegionService;
import uk.co.bbr.services.security.JwtService;
import uk.co.bbr.services.security.SecurityService;
import uk.co.bbr.services.security.ex.AuthenticationFailedException;
import uk.co.bbr.services.venues.VenueService;
import uk.co.bbr.web.LoginMixin;
import uk.co.bbr.web.security.support.TestUser;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest(properties = { "spring.config.name=public-pages-web-tests-admin-h2", "spring.datasource.url=jdbc:h2:mem:public-pages-web-tests-admin-h2;DB_CLOSE_DELAY=-1;MODE=MSSQLServer;DATABASE_TO_LOWER=TRUE", "spring.jpa.database-platform=org.hibernate.dialect.SQLServerDialect"},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PublicPagesTests extends PageSets implements LoginMixin {

    @Autowired private SecurityService securityService;
    @Autowired private BandService bandService;
    @Autowired private BandAliasService bandAliasService;
    @Autowired private BandRelationshipService bandRelationshipService;
    @Autowired private ContestService contestService;
    @Autowired private ContestEventService contestEventService;
    @Autowired private ContestGroupService contestGroupService;
    @Autowired private ContestTagService contestTagService;
    @Autowired private ResultService contestResultService;
    @Autowired private PersonService personService;
    @Autowired private PersonRelationshipService personRelationshipService;
    @Autowired private PersonAliasService personAliasService;
    @Autowired private RegionService regionService;
    @Autowired private PieceService pieceService;
    @Autowired private VenueService venueService;
    @Autowired private JwtService jwtService;
    @Autowired private RestTemplate restTemplate;
    @LocalServerPort private int port;

    @BeforeAll
    void setupBands() throws AuthenticationFailedException {
        loginTestUser(this.securityService, this.jwtService, TestUser.TEST_MEMBER);

        this.setupData(this.regionService, this.bandService, this.bandAliasService, this.bandRelationshipService, this.personService, this.personRelationshipService, this.personAliasService, this.venueService, this.pieceService, this.contestGroupService, this.contestService, this.contestEventService, this.contestResultService, this.contestTagService);

        logoutTestUser();
    }

    @ParameterizedTest
    @MethodSource("notFoundPages")
    void testInvalidSlugPagesFailAsExpectedWhenNotLoggedIn(String offset) {
        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> this.restTemplate.getForObject("http://localhost:" + this.port + offset, String.class));
        assertTrue(Objects.requireNonNull(ex.getMessage()).contains("404"));  // Page Doesn't Exist
    }

    @ParameterizedTest
    @MethodSource("publicPages")
    void testPagesReturnSuccessfullyWhenNotLoggedIn(String offset) {
        String response = this.restTemplate.getForObject("http://localhost:" + this.port + offset, String.class);
        assertNotNull(response);
        assertFalse(response.contains("<h2>Sign In</h2>")); // Page doesn't require login
        assertFalse(response.contains("Page not found"));  // Page Exists
    }

    @ParameterizedTest
    @MethodSource("memberPages")
    void testMemberPagesFailWhenNotLoggedIn(String offset) {
        String response = this.restTemplate.getForObject("http://localhost:" + this.port + offset, String.class);
        assertNotNull(response);
        assertTrue(response.contains("<h2>Sign In</h2>")); // page requires login
        assertFalse(response.contains("Page not found")); // page exists
    }

    @ParameterizedTest
    @MethodSource("proPages")
    void testProPagesFailWhenNotLoggedIn(String offset) {
        String response = this.restTemplate.getForObject("http://localhost:" + this.port + offset, String.class);
        assertNotNull(response);
        assertTrue(response.contains("<h2>Sign In</h2>")); // page requires login
        assertFalse(response.contains("Page not found")); // page exists
    }

    @ParameterizedTest
    @MethodSource("superuserPages")
    void testSuperuserPagesFailWhenNotLoggedIn(String offset) {
        String response = this.restTemplate.getForObject("http://localhost:" + this.port + offset, String.class);
        assertNotNull(response);
        assertTrue(response.contains("<h2>Sign In</h2>")); // page requires login
        assertFalse(response.contains("Page not found")); // page exists
    }

    @ParameterizedTest
    @MethodSource("adminPages")
    void testAdminPagesFailWhenNotLoggedIn(String offset) {
        String response = this.restTemplate.getForObject("http://localhost:" + this.port + offset, String.class);
        assertNotNull(response);
        assertTrue(response.contains("<h2>Sign In</h2>")); // page requires login
        assertFalse(response.contains("Page not found")); // page exists
    }
}

