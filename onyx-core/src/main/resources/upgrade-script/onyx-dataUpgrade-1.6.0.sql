-- Set export_date to current system time for already exported interviews (ONYX-821)
update participant
set export_date = sysdate()
where exported = true
and export_date is null;

-- For all consent rows, update the time_start column to the parent interview's start_date (ONYX-1003)
UPDATE consent c, interview i SET c.time_start = i.start_date WHERE c.interview_id = i.id;