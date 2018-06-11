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
import org.obiba.onyx.marble.core.service.ConsentService;

public abstract class AbstractMarbleStageState extends AbstractStageState {

  protected ConsentService consentService;

  public void setConsentService(ConsentService consentService) {
    this.consentService = consentService;
  }

  @Override
  public Boolean areDependenciesCompleted() {
    return super.areDependenciesCompleted();
  }

}
