/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.instrument.ndd;

public class ParticipantData {
  private String identifier;

  private String lastName;

  private String firstName;

  private Double height;

  private Long weight;

  private String ethnicity;

  private String smoker;

  private String asthma;

  private String gender;

  private String dateOfBirth;

  public ParticipantData() {
    super();
  }

  public String getIdentifier() {
    return identifier;
  }

  public String getLastName() {
    return lastName;
  }

  public String getFirstName() {
    return firstName;
  }

  public Double getHeight() {
    return height;
  }

  public Long getWeight() {
    return weight;
  }

  public String getEthnicity() {
    return ethnicity;
  }

  public String getSmoker() {
    return smoker;
  }

  public String getAsthma() {
    return asthma;
  }

  public String getGender() {
    return gender;
  }

  public String getDateOfBirth() {
    return dateOfBirth;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public void setHeight(Double height) {
    this.height = height;
  }

  public void setWeight(Long weight) {
    this.weight = weight;
  }

  public void setEthnicity(String ethnicity) {
    this.ethnicity = ethnicity;
  }

  public void setSmoker(String smoker) {
    this.smoker = smoker;
  }

  public void setAsthma(String asthma) {
    this.asthma = asthma;
  }

  public void setGender(String gender) {
    this.gender = gender;
  }

  public void setDateOfBirth(String dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
  }

}