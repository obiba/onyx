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

import java.util.EnumSet;

import org.obiba.onyx.marble.domain.consent.Consent;
import org.obiba.onyx.marble.domain.consent.ConsentMode;

/**
 * Session active participant's consent service.
 */
public interface ActiveConsentService {

  /**
   * Returns the {@link Consent} for the current interview.
   * @return The consent
   */
  public Consent getConsent();

  /**
   * Returns the {@link Consent} for the current interview.
   * @param newInstance If true, create a new consent object and return it.
   * @return
   */
  public Consent getConsent(boolean newInstance);

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

  /**
   * Returns the value for the consent mode variable
   * @return
   */
  public EnumSet<ConsentMode> getSupportedConsentModes();

}
