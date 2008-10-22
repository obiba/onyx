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

import java.util.List;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.marble.core.service.ActiveConsentService;
import org.obiba.onyx.marble.domain.consent.Consent;
import org.obiba.onyx.marble.domain.consent.ConsentMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultActiveConsentServiceImpl extends PersistenceManagerAwareService implements ActiveConsentService {

  private static final Logger log = LoggerFactory.getLogger(DefaultActiveConsentServiceImpl.class);

  private ActiveInterviewService activeInterviewService;

  private Consent consent;

  public void setConsent(Consent consent) {
    this.consent = consent;
  }

  public Consent getConsent() {
    Consent template = null;
    if(consent == null) {
      template = new Consent();
      template.setInterview(activeInterviewService.getInterview());
      List<Consent> consents = getPersistenceManager().match(template);
      for(Consent oneConsent : consents) {
        this.consent = oneConsent;
      }
      return consent;
    }
    return consent;
  }

  public ConsentMode getMode() {

    return consent.getMode();
  }

  @Override
  public void update() {
    getPersistenceManager().save(consent);
  }

  @Override
  public boolean validateConsent() {
    if(consent.getPdfForm() != null) {
      return true;
    } else {
      return false;
    }
  }

  public ActiveInterviewService getActiveInterviewService() {
    return activeInterviewService;
  }

  public void setActiveInterviewService(ActiveInterviewService activeInterviewService) {
    this.activeInterviewService = activeInterviewService;
  }

}
