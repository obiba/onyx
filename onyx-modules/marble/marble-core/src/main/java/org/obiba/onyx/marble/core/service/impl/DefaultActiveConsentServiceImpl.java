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
import java.util.ArrayList;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.marble.core.service.ActiveConsentService;
import org.obiba.onyx.marble.domain.consent.Consent;

import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfReader;

public class DefaultActiveConsentServiceImpl extends PersistenceManagerAwareService implements ActiveConsentService {

  private ActiveInterviewService activeInterviewService;

  private Consent consent;

  public ActiveInterviewService getActiveInterviewService() {
    return activeInterviewService;
  }

  public void setActiveInterviewService(ActiveInterviewService activeInterviewService) {
    this.activeInterviewService = activeInterviewService;
  }

  public void setConsent(Consent consent) {
    this.consent = consent;
  }

  public Consent getConsent() {
    Consent template = null;

    // Attempt to retrieve from database.
    if(consent == null) {
      template = new Consent();
      template.setInterview(activeInterviewService.getInterview());
      template.setDeleted(false);
      consent = getPersistenceManager().matchOne(template);
    }

    return consent;
  }

  @Override
  public void update() {
    if(consent != null) {
      getPersistenceManager().save(consent);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean validateElectronicConsent() {

    if(isPdfFormSubmitted()) {

      // Access PDF content with IText library.
      PdfReader reader;
      try {
        reader = new PdfReader(consent.getPdfForm());
      } catch(IOException ex) {
        throw new RuntimeException("Could not read PDF document", ex);
      }

      // Get the PDF form fields.
      AcroFields form = reader.getAcroFields();

      // Make sure all signature fields have been signed.
      ArrayList list = form.getBlankSignatureNames();
      if(list.isEmpty()) {
        return true;
      } else {
        return false;
      }

      // Invalid if form not submitted.
    } else {
      return false;
    }
  }

  @Override
  public void deletePreviousConsent() {
    Consent template = new Consent();
    template.setInterview(activeInterviewService.getInterview());
    template.setDeleted(false);
    Consent previousConsent = getPersistenceManager().matchOne(template);
    if(previousConsent != null) {
      previousConsent.setDeleted(true);
      getPersistenceManager().save(previousConsent);
    }
  }

  @Override
  public boolean isPdfFormSubmitted() {
    if(consent.getPdfForm() != null) {
      return true;
    }
    return false;
  }

}
