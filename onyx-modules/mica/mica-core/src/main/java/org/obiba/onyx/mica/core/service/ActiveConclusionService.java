/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.mica.core.service;

import org.obiba.onyx.marble.domain.consent.Consent;
import org.obiba.onyx.mica.domain.conclusion.Conclusion;

public interface ActiveConclusionService {

  public Conclusion getConclusion();

  public Conclusion getConclusion(boolean newInstance);

  public void save();

  public boolean isBalsacConfirmationRequired();

  public Consent getParticipantConsent();

}
