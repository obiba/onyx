/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.marble.core.service.impl;

import java.io.IOException;
import java.util.Collection;

import org.apache.wicket.util.string.Strings;
import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.marble.core.service.ActiveConsentService;
import org.obiba.onyx.marble.domain.consent.Consent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfReader;

public class DefaultActiveConsentServiceImpl extends PersistenceManagerAwareService implements ActiveConsentService {

  private static final Logger log = LoggerFactory.getLogger(DefaultActiveConsentServiceImpl.class);

  private ActiveInterviewService activeInterviewService;

  private Consent consent;

  private String consentMode;

  public ActiveInterviewService getActiveInterviewService() {
    return activeInterviewService;
  }

  public void setActiveInterviewService(ActiveInterviewService activeInterviewService) {
    this.activeInterviewService = activeInterviewService;
  }

  public Consent getConsent() {
    return getConsent(false);
  }

  public Consent getConsent(boolean newInstance) {

    Interview currentInterview = activeInterviewService.getInterview();

    // Consent doesn't exist or new object requested.
    if(consent == null || newInstance) {
      consent = new Consent();
      consent.setInterview(currentInterview);
      consent.setDeleted(false);

      log.debug("Created new consent object for interview id = {}", currentInterview.getId());

    } else {
      log.debug("Returning consent object found in current session = {}", currentInterview.getId());
    }

    return consent;
  }

  @SuppressWarnings("unchecked")
  public boolean validateElectronicConsent() {

    byte[] pdfForm = consent.getPdfForm();
    if(pdfForm != null) {

      // Access PDF content with IText library.
      PdfReader reader;
      try {
        reader = new PdfReader(pdfForm);
      } catch(IOException ex) {
        log.error("Could not read PDF consent form", ex);
        throw new RuntimeException(ex);
      }

      // Get the PDF form fields.
      AcroFields form = reader.getAcroFields();

      // Make sure that all mandatory fields have been filled.
      Collection<String> fieldNameList = form.getFields().keySet();
      for(String fieldName : fieldNameList) {
        if(isMandatoryField(form, fieldName)) {
          if(Strings.isEmpty(form.getField(fieldName))) {
            log.debug("The following mandatory field was not filled in : {}", fieldName);
            return false;
          }
        }
      }

      return true;

      // Invalid if PDF form does not exist.
    } else {
      return false;
    }
  }

  private boolean isMandatoryField(AcroFields form, String fieldName) {
    int fieldType = form.getFieldType(fieldName);

    // Fields name ending with ".mandatoryFields" are required.
    return fieldName.contains("_mandatoryField") && fieldType != AcroFields.FIELD_TYPE_PUSHBUTTON;
  }

  public boolean isConsentFormSubmitted() {
    if(consent.isAccepted() != null) {
      return true;
    }
    return false;
  }

  public String getConsentMode() {
    return consentMode;
  }

  public void setConsentMode(String consentMode) {
    this.consentMode = consentMode;
  }
}
