CREATE UNIQUE INDEX idx_adjudicator_prevent_duplicate ON contest_event_adjudicator(contest_event_id, person_id);
