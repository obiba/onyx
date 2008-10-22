/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.mica.engine.state;

import org.obiba.onyx.engine.state.AbstractStageState;
import org.obiba.onyx.mica.core.service.ActiveConclusionService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMicaStageState extends AbstractStageState {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(AbstractMicaStageState.class);
  
  private ActiveConclusionService activeConclusionService;

  public void setActiveConclusionService(ActiveConclusionService activeConclusionService) {
    this.activeConclusionService = activeConclusionService;
  }

  protected ActiveConclusionService getActiveConclusionService() {
    return activeConclusionService;
  }

  @Override
  public Data getData(String key) {
    if(key.equals("Conclusion")) {
      return DataBuilder.buildBoolean(activeConclusionService.getConclusion().isAccepted());
    }
    return null;
  }
  
  @Override
  protected Boolean areDependenciesCompleted() {
    return super.areDependenciesCompleted();
  }
}
