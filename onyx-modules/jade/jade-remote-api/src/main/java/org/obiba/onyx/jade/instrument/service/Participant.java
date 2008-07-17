package org.obiba.onyx.jade.instrument.service;

import java.io.Serializable;
import java.util.Date;

public class Participant implements Serializable {

  private static final long serialVersionUID = -1662840189070073386L;

  private String code;

  private Date birthdate;

  private Gender gender;

  final public String getCode() {
    return code;
  }

  final public Date getBirthdate() {
    return birthdate;
  }

  final public Gender getGender() {
    return gender;
  }

  final Participant setCode(String code) {
    this.code = code;
    return this;
  }

  final Participant setBirthdate(Date birthdate) {
    this.birthdate = birthdate;
    return this;
  }

  final Participant setGender(Gender gender) {
    this.gender = gender;
    return this;
  }

}
