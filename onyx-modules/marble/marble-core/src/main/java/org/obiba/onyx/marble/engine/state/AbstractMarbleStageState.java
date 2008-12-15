/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.marble.engine.state;

import org.obiba.onyx.engine.state.AbstractStageState;
import org.obiba.onyx.marble.core.service.ActiveConsentService;
import org.obiba.onyx.marble.core.service.ConsentService;
import org.obiba.onyx.marble.domain.consent.Consent;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;

public abstract class AbstractMarbleStageState extends AbstractStageState {

  protected ActiveConsentService activeConsentService;

  protected ConsentService consentService;

  @Override
  public Data getData(String key) {
    Consent interviewConsent;

    if(key.equalsIgnoreCase("Consent")) {
      interviewConsent = consentService.getConsent(activeInterviewService.getInterview());
      if(interviewConsent == null) {
        interviewConsent = activeConsentService.getConsent();
      }
      return DataBuilder.buildBoolean(interviewConsent.isAccepted());
    }
    return null;
  }

  public void setConsentService(ConsentService consentService) {
    this.consentService = consentService;
  }

  public void setActiveConsentService(ActiveConsentService activeConsentService) {
    this.activeConsentService = activeConsentService;
  }
}
