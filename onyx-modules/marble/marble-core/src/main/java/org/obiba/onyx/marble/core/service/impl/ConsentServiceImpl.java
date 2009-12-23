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
import java.util.Locale;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.marble.core.service.ConsentService;
import org.obiba.onyx.marble.domain.consent.Consent;
import org.obiba.onyx.marble.domain.consent.ConsentMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsentServiceImpl extends PersistenceManagerAwareService implements ConsentService {

  private static final Logger log = LoggerFactory.getLogger(ConsentServiceImpl.class);

  private boolean allowElectronicMode;

  private List<Locale> supportedConsentLocales;

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

  public boolean isElectronicModeAllowed() {
    return allowElectronicMode;
  }

  public void setAllowElectronicMode(boolean allowElectronicMode) {
    this.allowElectronicMode = allowElectronicMode;
  }

  public EnumSet<ConsentMode> getSupportedConsentModes() {
    List<ConsentMode> consentModesList = new ArrayList<ConsentMode>();

    consentModesList.add(ConsentMode.MANUAL);
    if(isElectronicModeAllowed()) {
      consentModesList.add(ConsentMode.ELECTRONIC);
    }

    return EnumSet.copyOf(consentModesList);
  }

  public List<Locale> getSupportedConsentLocales() {
    return supportedConsentLocales;
  }

  public void setSupportedConsentLocales(List<Locale> supportedConsentLocales) {
    this.supportedConsentLocales = supportedConsentLocales;
  }

  /**
   * Set the supported consent locales using a comma separated set of consent locale.
   * @param supportedConsentLocales.
   */
  public void setSupportedConsentLocalesString(String supportedConsentLocales) {
    if(supportedConsentLocales != null && supportedConsentLocales.length() > 0) {

      List<Locale> consentLocaleList = new ArrayList<Locale>();

      String supportedConsentLocaleArray[] = supportedConsentLocales.split(",");
      for(String locale : supportedConsentLocaleArray) {
        consentLocaleList.add(new Locale(locale));
      }

      this.setSupportedConsentLocales(consentLocaleList);
    }
  }

  public void purgeConsent(Interview interview) {
    Consent template = new Consent();
    template.setInterview(interview);
    List<Consent> consents = getPersistenceManager().match(template);
    for(Consent consent : consents) {
      getPersistenceManager().delete(consent);
    }
  }
}
