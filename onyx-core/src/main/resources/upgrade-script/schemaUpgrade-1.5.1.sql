--
-- Schema upgrade to add indexes to various tables (ONYX-811).
--

ALTER TABLE consent ADD INDEX deleted_index (deleted);

ALTER TABLE instrument_run_value ADD INDEX instrument_parameter_index (instrument_parameter);

ALTER TABLE category_answer ADD INDEX category_name_index (category_name);

ALTER TABLE open_answer ADD INDEX open_answer_definition_name_index (open_answer_definition_name);