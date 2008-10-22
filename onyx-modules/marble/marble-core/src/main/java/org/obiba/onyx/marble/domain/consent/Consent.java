/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.marble.domain.consent;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
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

  @Lob
  @Column(length = Integer.MAX_VALUE)
  private byte[] pdfForm;

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

  public byte[] getPdfForm() {
    return pdfForm;
  }

  public void setPdfForm(byte[] pdfForm) {
    this.pdfForm = pdfForm;
  }

}
