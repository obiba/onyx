CREATE TABLE measure (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  time datetime DEFAULT NULL,
  instrument_run_id bigint(20) DEFAULT NULL,
  user_id bigint(20) DEFAULT NULL,
  instrumentBarcode varchar(200) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY FK37EDA55E6434F6C4 (instrument_run_id),
  KEY FK37EDA55E62335EF5 (user_id),
  CONSTRAINT FK37EDA55E62335EF5 FOREIGN KEY (user_id) REFERENCES user (id),
  CONSTRAINT FK37EDA55E6434F6C4 FOREIGN KEY (instrument_run_id) REFERENCES instrument_run (id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

ALTER TABLE instrument_run_value ADD COLUMN measure_id bigint(20) DEFAULT NULL;
ALTER TABLE instrument_run_value ADD KEY FK55C54465D9A292B3 (measure_id);
ALTER TABLE instrument_run_value ADD CONSTRAINT FK55C54465D9A292B3 FOREIGN KEY (measure_id) REFERENCES measure (id);

