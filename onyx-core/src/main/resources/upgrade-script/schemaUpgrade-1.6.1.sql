--
-- Schema upgrade to add many-to-many association between instrument and instrument_type (ONYX-1115).
--

CREATE TABLE instrument_measurement_type (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  instrument_id bigint(20) NOT NULL,
  type varchar(200) NOT NULL,
  PRIMARY KEY (id),
  KEY FK37EDA55E6434F6C7 (instrument_id),
  CONSTRAINT FK37EDA55E6434F6C7 FOREIGN KEY (instrument_id) REFERENCES instrument (id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO instrument_measurement_type (instrument_id, type) (SELECT id, type from instrument);

ALTER TABLE instrument DROP COLUMN type;