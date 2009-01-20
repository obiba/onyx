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

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.marble.core.service.ConsentService;
import org.obiba.onyx.marble.domain.consent.Consent;

public class ConsentServiceImpl extends PersistenceManagerAwareService implements ConsentService {

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
    previousConsent.setDeleted(true);
    getPersistenceManager().save(previousConsent);
  }

  public void saveConsent(Consent consent) {
    getPersistenceManager().save(consent);
  }

}
