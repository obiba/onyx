package org.obiba.onyx.mica.domain.conclusion;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.core.domain.participant.Interview;

@Entity
public class Conclusion extends AbstractEntity {

  private static final long serialVersionUID = 1L;

  @OneToOne
  @JoinColumn(name = "interview_id", unique = true)
  private Interview interview;

  private Boolean accepted;

  private String barcode;

  public Interview getInterview() {
    return interview;
  }

  public void setInterview(Interview interview) {
    this.interview = interview;
  }

  public Boolean isAccepted() {
    return accepted;
  }

  public void setAccepted(Boolean accepted) {
    this.accepted = accepted;
  }

  public String getBarcode() {
    return barcode;
  }

  public void setBarcode(String barcode) {
    this.barcode = barcode;
  }
}