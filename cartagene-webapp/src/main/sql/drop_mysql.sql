alter table action drop foreign key FKAB2F7E36400A25DB;
alter table action drop foreign key FKAB2F7E3662335EF5;
alter table action drop foreign key FKAB2F7E36FEB07DB7;
alter table appointment drop foreign key FKA8155B9FF42CD537;
alter table input_source drop foreign key FK43929630576DC9EC;
alter table instrument drop foreign key FK532D63E7576DC9EC;
alter table instrument_computed_output drop foreign key FK593A0A1B8970BEE6;
alter table instrument_computed_output drop foreign key FK593A0A1B97854635;
alter table instrument_parameter drop foreign key FKE62A9D511A9AC81D;
alter table instrument_parameter drop foreign key FKE62A9D511E8A5EAC;
alter table instrument_run drop foreign key FK74A802331A9AC81D;
alter table instrument_run drop foreign key FK74A8023362335EF5;
alter table instrument_run drop foreign key FK74A8023347B0649A;
alter table instrument_run_value drop foreign key FK55C544656434F6C4;
alter table instrument_run_value drop foreign key FK55C54465AF8DC1E8;
alter table instrument_type_dependencies drop foreign key FK64ABA7B618ABE994;
alter table instrument_type_dependencies drop foreign key FK64ABA7B6576DC9EC;
alter table interview drop foreign key FK1DFCD181F42CD537;
alter table interview drop foreign key FK1DFCD18162335EF5;
alter table participant_interview drop foreign key FKD72C9775F42CD537;
alter table stage_dependencies drop foreign key FK3734F0CACFD43783;
alter table stage_dependencies drop foreign key FK3734F0CA400A25DB;
alter table stage_execution_memento drop foreign key FKA9968B11400A25DB;
alter table stage_execution_memento drop foreign key FKA9968B11FEB07DB7;
alter table stage_interview drop foreign key FKB1A769809CC9BF1A;
alter table stage_interview drop foreign key FKB1A76980A1F0E478;
drop table if exists action;
drop table if exists app_configuration;
drop table if exists appointment;
drop table if exists input_source;
drop table if exists instrument;
drop table if exists instrument_computed_output;
drop table if exists instrument_parameter;
drop table if exists instrument_run;
drop table if exists instrument_run_value;
drop table if exists instrument_type;
drop table if exists instrument_type_dependencies;
drop table if exists interview;
drop table if exists participant;
drop table if exists participant_interview;
drop table if exists stage;
drop table if exists stage_dependencies;
drop table if exists stage_execution_memento;
drop table if exists stage_interview;
drop table if exists user;
