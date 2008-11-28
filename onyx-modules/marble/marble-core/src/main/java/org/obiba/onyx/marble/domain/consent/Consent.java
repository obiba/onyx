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

import java.io.ByteArrayOutputStream;
import java.util.Locale;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;

import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.core.domain.participant.Interview;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

@Entity
public class Consent extends AbstractEntity {

  private static final long serialVersionUID = 1L;

  @OneToOne
  @JoinColumn(name = "interview_id")
  private Interview interview;

  private ConsentMode mode;

  private Locale locale;

  private Boolean accepted;

  private Boolean deleted;

  @Lob
  @Column(length = Integer.MAX_VALUE)
  private byte[] pdfForm;

  public ConsentMode getMode() {
    return mode;
  }

  public void setMode(ConsentMode mode) {
    this.mode = mode;
  }

  public Locale getLocale() {
    return locale;
  }

  public void setLocale(Locale locale) {
    this.locale = locale;
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

  /**
   * Return a PDF form that all fields are converted to non editable
   */
  public byte[] getNonEditablePdfForm() {
    try {
      PdfReader pdfReader = new PdfReader(pdfForm);
      ByteArrayOutputStream output = new ByteArrayOutputStream();

      PdfStamper stamper = new PdfStamper(pdfReader, output);
      stamper.setFormFlattening(true);

      stamper.close();
      output.close();
      pdfReader.close();

      return output.toByteArray();

    } catch(Exception e) {
      return pdfForm;
    }
  }

  public Boolean isDeleted() {
    return deleted;
  }

  public void setDeleted(Boolean deleted) {
    this.deleted = deleted;
  }

}
