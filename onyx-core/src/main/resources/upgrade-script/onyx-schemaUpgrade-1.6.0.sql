-- Add a status column to Measure table (ONYX-701) 
ALTER TABLE measure ADD COLUMN status VARCHAR(50);

-- Add an export_date column to Participant table (ONYX-812)
ALTER TABLE participant ADD COLUMN export_date DATETIME;
