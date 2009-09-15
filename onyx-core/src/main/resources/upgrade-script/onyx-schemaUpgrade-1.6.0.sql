-- Add a status column to Measure table (ONYX-701) 
ALTER TABLE MEASURE ADD COLUMN STATUS VARCHAR(50);

-- Add an export_date column to Participant table (ONYX-812)
ALTER TABLE PARTICIPANT ADD COLUMN EXPORT_DATE DATETIME;
