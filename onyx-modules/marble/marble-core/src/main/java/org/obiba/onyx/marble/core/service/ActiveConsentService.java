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

import org.obiba.onyx.marble.domain.consent.Consent;

/**
 * Session active participant's consent service.
 */
public interface ActiveConsentService {

  /**
   * Sets the {@link Consent} for the current interview.
   * @param consent The consent
   */
  public void setConsent(Consent consent);

  /**
   * Returns the {@link Consent} for the current interview.
   * @return The consent
   */
  public Consent getConsent();

  /**
   * Persists the {@link Consent} for the current interview.
   */
  public void update();

  /**
   * Marks the previous {@link Consent} (if exist) as deleted.
   */
  public void deletePreviousConsent();

  /**
   * Validates the content of the electronic consent form.
   * @return True if valid, false if not.
   */
  public boolean validateElectronicConsent();

  /**
   * Checks if the consent form has been submitted.
   * @return True if submitted, false if not.
   */
  public boolean isConsentFormSubmitted();
}
