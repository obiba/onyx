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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.marble.core.service.ConsentService;
import org.obiba.onyx.marble.domain.consent.Consent;
import org.obiba.onyx.marble.domain.consent.ConsentMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsentServiceImpl extends PersistenceManagerAwareService implements ConsentService {

  private static final Logger log = LoggerFactory.getLogger(ConsentServiceImpl.class);

  private EnumSet<ConsentMode> supportedConsentModes;

  public Consent getConsent(Interview interview) {
    Consent template = new Consent();
    template.setInterview(interview);
    template.setDeleted(false);
    return getPersistenceManager().matchOne(template);
  }

  public void deletePreviousConsent(Interview interview) {
    Consent template = new Consent();
    template.setInterview(interview);
    template.setDeleted(false);

    // Mark existing consent as deleted
    Consent previousConsent = getPersistenceManager().matchOne(template);
    if(previousConsent != null) {
      previousConsent.setDeleted(true);
      getPersistenceManager().save(previousConsent);
    }
  }

  public void saveConsent(Consent consent) {
    getPersistenceManager().save(consent);
  }

  public EnumSet<ConsentMode> getSupportedConsentModes() {
    return supportedConsentModes;
  }

  public void setSupportedConsentModes(EnumSet<ConsentMode> supportedConsentModes) {
    this.supportedConsentModes = supportedConsentModes;
  }

  /**
   * Set the supported consent mode using a comma separated set of consent mode.
   * @param supportedConsentModes.
   */
  public void setSupportedConsentModesString(String supportedConsentModes) {
    if(supportedConsentModes != null && supportedConsentModes.length() > 0) {

      List<ConsentMode> consentModesList = new ArrayList<ConsentMode>();

      String supportedConsentModeArray[] = supportedConsentModes.split(",");
      for(String mode : supportedConsentModeArray) {
        consentModesList.add(ConsentMode.valueOf(mode));
      }

      this.setSupportedConsentModes(EnumSet.copyOf(consentModesList));
    }
  }

  public void validateInstance() {
    if(!supportedConsentModes.contains(ConsentMode.MANUAL)) {
      IllegalArgumentException ex = new IllegalArgumentException();
      log.error("Consent mode electronic only is not supported yet", ex);
      throw ex;
    }
  }

  public void purgeConsent(Interview interview) {
    Consent template = new Consent();
    template.setInterview(interview);
    getPersistenceManager().delete(template);
  }
}
