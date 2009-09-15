--  Set export_date to current system time for already exported interviews (ONYX-821)
update participant
set export_date = sysdate()
where exported = true
and export_date is null;