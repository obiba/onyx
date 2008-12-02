/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.marble.core.service;

import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.marble.domain.consent.Consent;

public interface ConsentService {

  /**
   * Retrieves the {@link Consent} for a specific interview.
   * @param interview Interview for which the consent will be retrieve.
   * @return The consent.
   */
  public Consent getConsent(Interview interview);

  /**
   * Marks the previous {@link Consent} of a specific interview (if exist) as deleted.
   * @param interview Interview for which the consent will be retrieve.
   */
  public void deletePreviousConsent(Interview interview);

  /**
   * Persists the {@link Consent}.
   * @param consent
   */
  public void saveConsent(Consent consent);

}
