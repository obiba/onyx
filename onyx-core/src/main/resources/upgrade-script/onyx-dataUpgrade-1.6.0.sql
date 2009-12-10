-- Set the status to VALID and workstation to Unknown for all existing measure rows (ONYX-1004)
update measure set status = 'VALID', workstation = 'Unknown';

-- For all consent rows, update the time_start column to the parent interview start_date (ONYX-1003)
update consent c, interview i SET c.time_start = i.start_date where c.interview_id = i.id;

-- In the improbable case that an instrument contains an empty string as barcode, which should not be possible (ONYX-1049)
update instrument set barcode = CONCAT(type, '_barcode') where barcode = '';