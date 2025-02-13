package uk.co.bbr.services.events.sql;

import jakarta.persistence.EntityManager;
import lombok.experimental.UtilityClass;
import uk.co.bbr.services.events.sql.dto.EventResultSqlDto;
import uk.co.bbr.services.events.sql.dto.EventUpDownLeftRightSqlDto;
import uk.co.bbr.services.events.sql.dto.ResultPieceSqlDto;
import uk.co.bbr.services.framework.sql.SqlExec;

import java.time.LocalDate;
import java.util.List;

@UtilityClass
public class EventSql {

    private static final String EVENT_RESULTS_SQL = """
            SELECT e.date_of_event, e.date_resolution, c.slug, r.result_position, r.result_position_type, r.band_name, b.name as actual_band_name, b.slug as band_slug, reg.name as region_name, reg.slug as region_slug, reg.country_code,
                                  r.draw, r.points_total,
                                  con1.slug as c1_slug, con1.first_names as c1_first_names, con1.surname as c1_surname,
                                  con2.slug as c2_slug, con2.first_names as c2_first_names, con2.surname as c2_surname,
                                  con3.slug as c3_slug, con3.first_names as c3_first_names, con3.surname as c3_surname,
                                  r.draw_second, r.draw_third,
                                  r.points_first, r.points_second, r.points_third, r.points_fourth, r.points_fifth, r.points_penalty,
                                  r.id, r.notes, e.name,
                                  g.name as group_name, g.slug as group_slug,
                                  b.latitude, b.longitude, b.status as band_status, sect.slug as section_slug, sect.translation_key as section_translation_key,
                                  b.created_by, r.result_award
                           FROM contest_result r
                           INNER JOIN contest_event e on e.id = r.contest_event_id
                           INNER JOIN contest c ON c.id = e.contest_id
                           INNER JOIN band b ON b.id = r.band_id
                           INNER JOIN region reg ON reg.id = b.region_id
                           LEFT OUTER JOIN section sect ON sect.id = b.section_id
                           LEFT OUTER JOIN person con1 ON con1.id = r.conductor_id
                           LEFT OUTER JOIN person con2 ON con2.id = r.conductor_two_id
                           LEFT OUTER JOIN person con3 ON con3.id = r.conductor_three_id
                           LEFT OUTER JOIN contest_group g ON g.id = c.contest_group_id
                           WHERE e.id = ?1
                           ORDER BY CASE
                             WHEN r.result_position_type = 'U' THEN CASE
                                   WHEN r.result_award = 'G' THEN 9000
                                   WHEN r.result_award = 'S' THEN 9005
                                   WHEN r.result_award = 'B' THEN 9010
                                   ELSE 10000
                               END
                             WHEN r.result_position_type = 'W' THEN 10010
                             WHEN r.result_position_type = 'D' THEN 10020
                             ELSE r.result_position
                           END, r.draw, r.band_name""";

    public static List<EventResultSqlDto> selectEventResults(EntityManager entityManager, Long eventId) {
        return SqlExec.execute(entityManager, EVENT_RESULTS_SQL, eventId, EventResultSqlDto.class);
    }

    private static final String EVENTS_AND_RESULTS_FOR_DATE_RANGE = """
            SELECT e.date_of_event, e.date_resolution, c.slug, r.result_position, r.result_position_type, r.band_name, b.name as actual_band_name, b.slug as band_slug, reg.name as region_name, reg.slug as region_slug, reg.country_code,
                  r.draw, r.points_total,
                  con1.slug as c1_slug, con1.first_names as c1_first_names, con1.surname as c1_surname,
                  con2.slug as c2_slug, con2.first_names as c2_first_names, con2.surname as c2_surname,
                  con3.slug as c3_slug, con3.first_names as c3_first_names, con3.surname as c3_surname,
                  r.draw_second, r.draw_third,
                  r.points_first, r.points_second, r.points_third, r.points_fourth, r.points_fifth, r.points_penalty,
                  r.id, r.notes, e.name,
                  g.name as group_name, g.slug as group_slug,
                  b.latitude, b.longitude, b.status as band_status, sect.slug as section_slug, sect.translation_key as section_translation_key,
                  r.created_by, r.result_award
           FROM contest_event e
           LEFT OUTER JOIN contest_result r ON r.contest_event_id = e.id AND r.result_position_type = 'R' AND r.result_position = 1
           INNER JOIN contest c ON c.id = e.contest_id
           LEFT OUTER JOIN band b ON b.id = r.band_id
           LEFT OUTER JOIN region reg ON reg.id = b.region_id
           LEFT OUTER JOIN section sect ON sect.id = b.section_id
           LEFT OUTER JOIN person con1 ON con1.id = r.conductor_id
           LEFT OUTER JOIN person con2 ON con2.id = r.conductor_two_id
           LEFT OUTER JOIN person con3 ON con3.id = r.conductor_three_id
           LEFT OUTER JOIN contest_group g ON g.id = c.contest_group_id
           WHERE e.date_of_event >= ?1
           AND e.date_of_event <= ?2
           ORDER BY e.date_of_event, g.id, c.ordering, c.slug""";

    public static List<EventResultSqlDto> eventsForDateRange(EntityManager entityManager, LocalDate start, LocalDate end) {
        return SqlExec.execute(entityManager, EVENTS_AND_RESULTS_FOR_DATE_RANGE, start, end, EventResultSqlDto.class);
    }

    private static final String EVENT_RESULT_PIECES_SQL = """
            SELECT p.name as piece_name, p.slug as piece_slug, p.piece_year as piece_year, crtp.contest_result_id as result_id, crtp.ordering as ordering
            FROM contest_result_test_piece crtp
            INNER JOIN piece p ON p.id = crtp.piece_id
            INNER JOIN contest_result r ON r.id = crtp.contest_result_id
            WHERE r.contest_event_id = ?1
            ORDER BY crtp.ordering""";

    public static List<ResultPieceSqlDto> selectEventResultPieces(EntityManager entityManager, Long eventId) {
        return SqlExec.execute(entityManager, EVENT_RESULT_PIECES_SQL, eventId, ResultPieceSqlDto.class);
    }

    private static final String EVENT_UP_SQL = """
            SELECT TOP 1 c.slug, e.name, e.date_of_event
                FROM contest_event e
                         INNER JOIN contest c ON c.id = e.contest_id
                         INNER JOIN contest_group g ON g.id = c.contest_group_id
                WHERE g.slug = ?1
                  AND c.ordering < ?2
                  AND YEAR(e.date_of_event) = ?3
                ORDER BY c.ordering DESC""";

    public static EventUpDownLeftRightSqlDto selectLinkedUpEvent(EntityManager entityManager, String groupSlug, Integer ordering, Integer year) {
        List<EventUpDownLeftRightSqlDto> returnValue = SqlExec.execute(entityManager, EVENT_UP_SQL, groupSlug, ordering, year, EventUpDownLeftRightSqlDto.class);
        if (returnValue.isEmpty()) {
            return null;
        }
        return returnValue.get(0);
    }

    private static final String EVENT_DOWN_SQL = """
            SELECT TOP 1 c.slug, e.name, e.date_of_event
                  FROM contest_event e
                           INNER JOIN contest c ON c.id = e.contest_id
                           INNER JOIN contest_group g ON g.id = c.contest_group_id
                  WHERE g.slug = ?1
                    AND c.ordering > ?2
                    AND YEAR(e.date_of_event) = ?3
                  ORDER BY c.ordering""";

    public static EventUpDownLeftRightSqlDto selectLinkedDownEvent(EntityManager entityManager, String groupSlug, Integer ordering, Integer year) {
        List<EventUpDownLeftRightSqlDto> returnValue = SqlExec.execute(entityManager, EVENT_DOWN_SQL, groupSlug, ordering, year, EventUpDownLeftRightSqlDto.class);
        if (returnValue.isEmpty()) {
            return null;
        }
        return returnValue.get(0);
    }

    private static final String EVENT_PREVIOUS_SQL = """
            SELECT TOP 1 c.slug, e.name, e.date_of_event
                      FROM contest_event e
                      INNER JOIN contest c ON c.id = e.contest_id
                      WHERE c.slug = ?1
                        AND YEAR(e.date_of_event) < ?2
                      ORDER BY e.date_of_event DESC""";

    public static EventUpDownLeftRightSqlDto selectLinkedPreviousEvent(EntityManager entityManager, String contestSlug, Integer year) {
        List<EventUpDownLeftRightSqlDto> returnValue = SqlExec.execute(entityManager, EVENT_PREVIOUS_SQL, contestSlug, year, EventUpDownLeftRightSqlDto.class);
        if (returnValue.isEmpty()) {
            return null;
        }
        return returnValue.get(0);
    }

    private static final String EVENT_NEXT_SQL = """
            SELECT TOP 1 c.slug, e.name, e.date_of_event
                  FROM contest_event e
                  INNER JOIN contest c ON c.id = e.contest_id
                  WHERE c.slug = ?1
                    AND YEAR(e.date_of_event) > ?2
                  ORDER BY e.date_of_event""";

    public static EventUpDownLeftRightSqlDto selectLinkedNextEvent(EntityManager entityManager, String contestSlug, Integer year) {
        List<EventUpDownLeftRightSqlDto> returnValue = SqlExec.execute(entityManager, EVENT_NEXT_SQL, contestSlug, year, EventUpDownLeftRightSqlDto.class);
        if (returnValue.isEmpty()) {
            return null;
        }
        return returnValue.get(0);
    }

    private static final String EVENT_RESULTS_RECENT_THIS_CONTEST_BAND_SQL = """
            SELECT e.date_of_event, e.date_resolution, c.slug, r.result_position, r.result_position_type, r.band_name, b.name as actual_band_name, b.slug as band_slug, reg.name as region_name, reg.slug as region_slug, reg.country_code,
                                  r.draw, r.points_total,
                                  con1.slug as c1_slug, con1.first_names as c1_first_names, con1.surname as c1_surname,
                                  con2.slug as c2_slug, con2.first_names as c2_first_names, con2.surname as c2_surname,
                                  con3.slug as c3_slug, con3.first_names as c3_first_names, con3.surname as c3_surname,
                                  r.draw_second, r.draw_third,
                                  r.points_first, r.points_second, r.points_third, r.points_fourth, r.points_fifth, r.points_penalty,
                                  r.id, r.notes, e.name,
                                  g.name as group_name, g.slug as group_slug,
                                  b.latitude, b.longitude, b.status as band_status, sect.slug as section_slug, sect.translation_key as section_translation_key,
                                  b.created_by, r.result_award
                           FROM contest_result r
                           INNER JOIN contest_event e on e.id = r.contest_event_id
                           INNER JOIN contest c ON c.id = e.contest_id
                           INNER JOIN band b ON b.id = r.band_id
                           INNER JOIN region reg ON reg.id = b.region_id
                           LEFT OUTER JOIN section sect ON sect.id = b.section_id
                           LEFT OUTER JOIN person con1 ON con1.id = r.conductor_id
                           LEFT OUTER JOIN person con2 ON con2.id = r.conductor_two_id
                           LEFT OUTER JOIN person con3 ON con3.id = r.conductor_three_id
                           LEFT OUTER JOIN contest_group g ON g.id = c.contest_group_id
                           WHERE e.date_of_event < ?1
                           AND e.date_of_event > ?2
                           AND c.slug = ?3
                           AND b.slug = ?4
                           ORDER BY e.date_of_event DESC""";

    public static List<EventResultSqlDto> selectLastTenYearsForBandThisContest(EntityManager entityManager, LocalDate eventDateFrom, LocalDate eventDateTo, String contestSlug, String bandSlug) {
        return SqlExec.execute(entityManager, EVENT_RESULTS_RECENT_THIS_CONTEST_BAND_SQL, eventDateFrom, eventDateTo, contestSlug, bandSlug, EventResultSqlDto.class);
    }

    private static final String EVENT_RESULTS_RECENT_THIS_GROUP_BAND_SQL = """
            SELECT e.date_of_event, e.date_resolution, c.slug, r.result_position, r.result_position_type, r.band_name, b.name as actual_band_name, b.slug as band_slug, reg.name as region_name, reg.slug as region_slug, reg.country_code,
                                  r.draw, r.points_total,
                                  con1.slug as c1_slug, con1.first_names as c1_first_names, con1.surname as c1_surname,
                                  con2.slug as c2_slug, con2.first_names as c2_first_names, con2.surname as c2_surname,
                                  con3.slug as c3_slug, con3.first_names as c3_first_names, con3.surname as c3_surname,
                                  r.draw_second, r.draw_third,
                                  r.points_first, r.points_second, r.points_third, r.points_fourth, r.points_fifth, r.points_penalty,
                                  r.id, r.notes, e.name,
                                  g.name as group_name, g.slug as group_slug,
                                  b.latitude, b.longitude, b.status as band_status, sect.slug as section_slug, sect.translation_key as section_translation_key,
                                  b.created_by, r.result_award
                           FROM contest_result r
                           INNER JOIN contest_event e on e.id = r.contest_event_id
                           INNER JOIN contest c ON c.id = e.contest_id
                           INNER JOIN band b ON b.id = r.band_id
                           INNER JOIN region reg ON reg.id = b.region_id
                           INNER JOIN contest_group g ON g.id = c.contest_group_id
                           LEFT OUTER JOIN section sect ON sect.id = b.section_id
                           LEFT OUTER JOIN person con1 ON con1.id = r.conductor_id
                           LEFT OUTER JOIN person con2 ON con2.id = r.conductor_two_id
                           LEFT OUTER JOIN person con3 ON con3.id = r.conductor_three_id
                           WHERE e.date_of_event < ?1
                           AND e.date_of_event > ?2
                           AND g.slug = ?3
                           AND b.slug = ?4
                           ORDER BY e.date_of_event DESC""";

    public static List<EventResultSqlDto> selectLastTenYearsForBandThisGroup(EntityManager entityManager, LocalDate eventDateFrom, LocalDate eventDateTo, String groupSlug, String bandSlug) {
        return SqlExec.execute(entityManager, EVENT_RESULTS_RECENT_THIS_GROUP_BAND_SQL, eventDateFrom, eventDateTo, groupSlug, bandSlug, EventResultSqlDto.class);
    }

    private static final String EVENT_RESULTS_RECENT_OTHER_CONTEST_BAND_SQL = """
            SELECT e.date_of_event, e.date_resolution, c.slug, r.result_position, r.result_position_type, r.band_name, b.name as actual_band_name, b.slug as band_slug, reg.name as region_name, reg.slug as region_slug, reg.country_code,
                                  r.draw, r.points_total,
                                  con1.slug as c1_slug, con1.first_names as c1_first_names, con1.surname as c1_surname,
                                  con2.slug as c2_slug, con2.first_names as c2_first_names, con2.surname as c2_surname,
                                  con3.slug as c3_slug, con3.first_names as c3_first_names, con3.surname as c3_surname,
                                  r.draw_second, r.draw_third,
                                  r.points_first, r.points_second, r.points_third, r.points_fourth, r.points_fifth, r.points_penalty,
                                  r.id, r.notes, e.name,
                                  g.name as group_name, g.slug as group_slug,
                                  b.latitude, b.longitude, b.status as band_status, sect.slug as section_slug, sect.translation_key as section_translation_key,
                                  b.created_by, r.result_award
                           FROM contest_result r
                           INNER JOIN contest_event e on e.id = r.contest_event_id
                           INNER JOIN contest c ON c.id = e.contest_id
                           INNER JOIN band b ON b.id = r.band_id
                           INNER JOIN region reg ON reg.id = b.region_id
                           INNER JOIN person con1 ON con1.id = r.conductor_id
                           LEFT OUTER JOIN section sect ON sect.id = b.section_id
                           LEFT OUTER JOIN person con2 ON con2.id = r.conductor_two_id
                           LEFT OUTER JOIN person con3 ON con3.id = r.conductor_three_id
                           LEFT OUTER JOIN contest_group g ON g.id = c.contest_group_id
                           WHERE e.date_of_event < ?1
                           AND e.date_of_event > ?2
                           AND c.slug <> ?3
                           AND b.slug = ?4
                           ORDER BY e.date_of_event DESC""";

    public static List<EventResultSqlDto> selectLastYearOtherContestForBand(EntityManager entityManager, LocalDate eventDateFrom, LocalDate eventDateTo, String contestSlug, String bandSlug) {
        return SqlExec.execute(entityManager, EVENT_RESULTS_RECENT_OTHER_CONTEST_BAND_SQL, eventDateFrom, eventDateTo, contestSlug, bandSlug, EventResultSqlDto.class);
    }

    private static final String EVENT_RESULTS_RECENT_THIS_CONTEST_CONDUCTOR_SQL = """
            SELECT e.date_of_event, e.date_resolution, c.slug, r.result_position, r.result_position_type, r.band_name, b.name as actual_band_name, b.slug as band_slug, reg.name as region_name, reg.slug as region_slug, reg.country_code,
                                  r.draw, r.points_total,
                                  con1.slug as c1_slug, con1.first_names as c1_first_names, con1.surname as c1_surname,
                                  con2.slug as c2_slug, con2.first_names as c2_first_names, con2.surname as c2_surname,
                                  con3.slug as c3_slug, con3.first_names as c3_first_names, con3.surname as c3_surname,
                                  r.draw_second, r.draw_third,
                                  r.points_first, r.points_second, r.points_third, r.points_fourth, r.points_fifth, r.points_penalty,
                                  r.id, r.notes, e.name,
                                  g.name as group_name, g.slug as group_slug,
                                  b.latitude, b.longitude, b.status as band_status, sect.slug as section_slug, sect.translation_key as section_translation_key,
                                  b.created_by, r.result_award
                           FROM contest_result r
                           INNER JOIN contest_event e on e.id = r.contest_event_id
                           INNER JOIN contest c ON c.id = e.contest_id
                           INNER JOIN band b ON b.id = r.band_id
                           INNER JOIN person con1 ON con1.id = r.conductor_id
                           INNER JOIN region reg ON reg.id = b.region_id
                           LEFT OUTER JOIN section sect ON sect.id = b.section_id
                           LEFT OUTER JOIN person con2 ON con2.id = r.conductor_two_id
                           LEFT OUTER JOIN person con3 ON con3.id = r.conductor_three_id
                           LEFT OUTER JOIN contest_group g ON g.id = c.contest_group_id
                           WHERE e.date_of_event < ?1
                           AND e.date_of_event > ?2
                           AND c.slug = ?3
                           AND con1.slug = ?4
                           ORDER BY e.date_of_event DESC""";

    public static List<EventResultSqlDto> selectLastTenYearsForConductorThisContest(EntityManager entityManager, LocalDate eventDateFrom, LocalDate eventDateTo, String contestSlug, String conductorSlug) {
        return SqlExec.execute(entityManager, EVENT_RESULTS_RECENT_THIS_CONTEST_CONDUCTOR_SQL, eventDateFrom, eventDateTo, contestSlug, conductorSlug, EventResultSqlDto.class);
    }

    private static final String EVENT_RESULTS_RECENT_THIS_GROUP_CONDUCTOR_SQL = """
            SELECT e.date_of_event, e.date_resolution, c.slug, r.result_position, r.result_position_type, r.band_name, b.name as actual_band_name, b.slug as band_slug, reg.name as region_name, reg.slug as region_slug, reg.country_code,
                                  r.draw, r.points_total,
                                  con1.slug as c1_slug, con1.first_names as c1_first_names, con1.surname as c1_surname,
                                  con2.slug as c2_slug, con2.first_names as c2_first_names, con2.surname as c2_surname,
                                  con3.slug as c3_slug, con3.first_names as c3_first_names, con3.surname as c3_surname,
                                  r.draw_second, r.draw_third,
                                  r.points_first, r.points_second, r.points_third, r.points_fourth, r.points_fifth, r.points_penalty,
                                  r.id, r.notes, e.name,
                                  g.name as group_name, g.slug as group_slug,
                                  b.latitude, b.longitude, b.status as band_status, sect.slug as section_slug, sect.translation_key as section_translation_key,
                                  b.created_by, r.result_award
                           FROM contest_result r
                           INNER JOIN contest_event e on e.id = r.contest_event_id
                           INNER JOIN contest c ON c.id = e.contest_id
                           INNER JOIN band b ON b.id = r.band_id
                           INNER JOIN region reg ON reg.id = b.region_id
                           INNER JOIN contest_group g ON g.id = c.contest_group_id
                           INNER JOIN person con1 ON con1.id = r.conductor_id
                           LEFT OUTER JOIN section sect ON sect.id = b.section_id
                           LEFT OUTER JOIN person con2 ON con2.id = r.conductor_two_id
                           LEFT OUTER JOIN person con3 ON con3.id = r.conductor_three_id
                           WHERE e.date_of_event < ?1
                           AND e.date_of_event > ?2
                           AND g.slug = ?3
                           AND con1.slug = ?4
                           ORDER BY e.date_of_event DESC""";

    public static List<EventResultSqlDto> selectLastTenYearsForConductorThisGroup(EntityManager entityManager, LocalDate eventDateFrom, LocalDate eventDateTo, String groupSlug, String conductorSlug) {
        return SqlExec.execute(entityManager, EVENT_RESULTS_RECENT_THIS_GROUP_CONDUCTOR_SQL, eventDateFrom, eventDateTo, groupSlug, conductorSlug, EventResultSqlDto.class);
    }

    private static final String EVENT_RESULTS_RECENT_OTHER_CONTEST_CONDUCTOR_SQL = """
            SELECT e.date_of_event, e.date_resolution, c.slug, r.result_position, r.result_position_type, r.band_name, b.name as actual_band_name, b.slug as band_slug, reg.name as region_name, reg.slug as region_slug, reg.country_code,
                                  r.draw, r.points_total,
                                  con1.slug as c1_slug, con1.first_names as c1_first_names, con1.surname as c1_surname,
                                  con2.slug as c2_slug, con2.first_names as c2_first_names, con2.surname as c2_surname,
                                  con3.slug as c3_slug, con3.first_names as c3_first_names, con3.surname as c3_surname,
                                  r.draw_second, r.draw_third,
                                  r.points_first, r.points_second, r.points_third, r.points_fourth, r.points_fifth, r.points_penalty,
                                  r.id, r.notes, e.name,
                                  g.name as group_name, g.slug as group_slug,
                                  b.latitude, b.longitude, b.status as band_status, sect.slug as section_slug, sect.translation_key as section_translation_key,
                                  b.created_by, r.result_award
                           FROM contest_result r
                           INNER JOIN contest_event e on e.id = r.contest_event_id
                           INNER JOIN contest c ON c.id = e.contest_id
                           INNER JOIN band b ON b.id = r.band_id
                           INNER JOIN region reg ON reg.id = b.region_id
                           LEFT OUTER JOIN section sect ON sect.id = b.section_id
                           LEFT OUTER JOIN person con1 ON con1.id = r.conductor_id
                           LEFT OUTER JOIN person con2 ON con2.id = r.conductor_two_id
                           LEFT OUTER JOIN person con3 ON con3.id = r.conductor_three_id
                           LEFT OUTER JOIN contest_group g ON g.id = c.contest_group_id
                           WHERE e.date_of_event < ?1
                           AND e.date_of_event > ?2
                           AND c.slug <> ?3
                           AND con1.slug = ?4
                           ORDER BY e.date_of_event DESC""";

    public static List<EventResultSqlDto> selectLastYearOtherContestForConductor(EntityManager entityManager, LocalDate eventDateFrom, LocalDate eventDateTo, String contestSlug, String conductorSlug) {
        return SqlExec.execute(entityManager, EVENT_RESULTS_RECENT_OTHER_CONTEST_CONDUCTOR_SQL, eventDateFrom, eventDateTo, contestSlug, conductorSlug, EventResultSqlDto.class);
    }
}
