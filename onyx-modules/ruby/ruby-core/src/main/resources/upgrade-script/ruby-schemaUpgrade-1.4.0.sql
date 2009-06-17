--
-- Schema migration for ONYX-587.
--

-- Add the tube_set_name column to participant_tube_registration.
ALTER TABLE participant_tube_registration
  ADD COLUMN tube_set_name VARCHAR(255) DEFAULT NULL;