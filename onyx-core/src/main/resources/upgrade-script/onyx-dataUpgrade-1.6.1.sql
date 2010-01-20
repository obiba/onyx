--
-- Data upgrade for (ONYX-1115).
--

INSERT INTO instrument_measurement_type (instrument_id, type) (SELECT id, type from instrument);

ALTER TABLE instrument DROP COLUMN type;