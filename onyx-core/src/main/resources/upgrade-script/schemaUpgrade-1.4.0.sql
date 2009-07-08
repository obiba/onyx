--
-- Schema upgrade to add uniqueness constraints (ONYX-672).
--
-- Note: This upgrade step must be executed AFTER the step that removes
--       unnecessary instrument_run rows. (Prior to Onyx 1.4.0, multiple
--       instrument_run rows were persisted for a given participant and
--       instrument type.)
--

ALTER TABLE participant DROP INDEX barcode;

ALTER TABLE participant_attribute_value ADD UNIQUE (participant_id, attribute_name);

ALTER TABLE appointment ADD UNIQUE (participant_id);

ALTER TABLE interview ADD UNIQUE (participant_id);

ALTER TABLE instrument_run ADD UNIQUE (participant_id, instrument_type);

ALTER TABLE question_answer ADD UNIQUE (question_name, questionnaire_participant_id);

ALTER TABLE registered_participant_tube ADD UNIQUE (barcode, participant_tube_registration_id);

ALTER TABLE conclusion ADD UNIQUE (interview_id);