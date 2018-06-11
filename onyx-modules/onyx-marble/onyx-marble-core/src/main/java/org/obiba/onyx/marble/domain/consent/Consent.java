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

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfReader;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Table;
import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.core.domain.participant.Interview;

@Entity
@Table(appliesTo = "consent", indexes = { @Index(name = "deleted_index", columnNames = { "deleted" }) })
public class Consent extends AbstractEntity {

  private static final long serialVersionUID = 1L;

  @ManyToOne(optional = false)
  @JoinColumn(name = "interview_id")
  private Interview interview;

  @Column(nullable = false)
  private String consentName;

  @Column(nullable = false)
  private ConsentMode mode;

  @Column(nullable = false)
  private Locale locale;

  private Boolean accepted;

  @Column(nullable = false)
  private Boolean deleted;

  @Lob
  @Column(length = Integer.MAX_VALUE)
  private byte[] pdfForm;

  @Transient
  private PdfReader cachedPdfReader;

  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date timeStart;

  @Temporal(TemporalType.TIMESTAMP)
  private Date timeEnd;

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

  public Boolean getAccepted() {
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

  @SuppressWarnings("unchecked")
  @Transient
  public Set<String> pdfFormFieldNames() {
    return getPdfReader().getAcroFields().getFields().keySet();
  }

  @Transient
  public Map<String, String> getPdfFormFields() {
    Map<String, String> pdfFormFields = new HashMap<String, String>();

    if(pdfForm != null) {
      PdfReader reader = getPdfReader();
      AcroFields pdfForm = reader.getAcroFields();
      for(String fieldName : pdfFormFieldNames()) {
        pdfFormFields.put(fieldName, pdfForm.getField(fieldName));
      }
    }

    return pdfFormFields;
  }

  public Boolean isDeleted() {
    return deleted;
  }

  public void setDeleted(Boolean deleted) {
    this.deleted = deleted;
  }

  public Date getTimeStart() {
    return timeStart;
  }

  public void setTimeStart(Date timeStart) {
    this.timeStart = timeStart;
  }

  public Date getTimeEnd() {
    return timeEnd;
  }

  public void setTimeEnd(Date timeEnd) {
    this.timeEnd = timeEnd;
  }

  private PdfReader getPdfReader() {
    if(cachedPdfReader == null) {
      byte[] pdfForm = getPdfForm();

      // Access PDF content with IText library.
      try {
        cachedPdfReader = new PdfReader(pdfForm);
      } catch(IOException ex) {
        throw new RuntimeException(ex);
      }
    }

    return cachedPdfReader;
  }

  public String getConsentName() {
    return consentName;
  }

  public void setConsentName(String consentName) {
    this.consentName = consentName;
  }
}
