/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.ruby.engine.state;

import org.obiba.onyx.engine.state.AbstractStageState;
import org.obiba.onyx.ruby.core.service.ActiveTubeRegistrationService;

/**
 * Base class for Ruby states.
 */
public abstract class AbstractRubyStageState extends AbstractStageState {
  //
  // Instance Variables
  //

  protected ActiveTubeRegistrationService activeTubeRegistrationService;

  //
  // Methods
  //

  public void setActiveTubeRegistrationService(ActiveTubeRegistrationService activeTubeRegistrationService) {
    this.activeTubeRegistrationService = activeTubeRegistrationService;
  }
}
