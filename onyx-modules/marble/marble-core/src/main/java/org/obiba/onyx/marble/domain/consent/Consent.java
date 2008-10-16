package org.obiba.onyx.marble.domain.consent;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.core.domain.participant.Interview;

@Entity
public class Consent extends AbstractEntity {

  private static final long serialVersionUID = 1L;

  @OneToOne
  @JoinColumn(name = "interview_id")
  private Interview interview;
  
  private ConsentMode mode;
  
  private String language;
  
  private Boolean accepted;

  public ConsentMode getMode() {
    return mode;
  }

  public void setMode(ConsentMode mode) {
    this.mode = mode;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public Boolean isAccepted() {
    return accepted;
  }

  public void setAccepted(Boolean accepted) {
    this.accepted = accepted;
  }

  public Interview getInterview() {
    return interview;
  }

  public void setInterview(Interview interview) {
    this.interview = interview;
  }
  
}
