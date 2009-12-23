/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.marble.magma;

import org.obiba.magma.ValueSet;
import org.obiba.magma.Variable;
import org.obiba.magma.beans.NoSuchBeanException;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.magma.AbstractOnyxBeanResolver;
import org.obiba.onyx.marble.core.service.ConsentService;
import org.obiba.onyx.marble.domain.consent.Consent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * ValueSetBeanResolver for Consent beans.
 */
public class ConsentBeanResolver extends AbstractOnyxBeanResolver {
  //
  // Instance Variables
  //

  @Autowired(required = true)
  private ConsentService consentService;

  //
  // AbstractOnyxBeanResolver Methods
  //

  public boolean resolves(Class<?> type) {
    return Consent.class.equals(type);
  }

  public Object resolve(Class<?> type, ValueSet valueSet, Variable variable) throws NoSuchBeanException {
    if(type.equals(Consent.class)) {
      return getConsent(valueSet);
    }

    return null;
  }

  //
  // Methods
  //

  public void setConsentService(ConsentService consentService) {
    this.consentService = consentService;
  }

  protected ConsentService getConsentService() {
    return consentService;
  }

  protected Consent getConsent(ValueSet valueSet) {
    Participant participant = getParticipant(valueSet);
    return consentService.getConsent(participant.getInterview());
  }
}
