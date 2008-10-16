/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
/**
 * 
 */
package org.obiba.onyx.wicket;

import org.apache.wicket.model.LoadableDetachableModel;
import org.obiba.onyx.engine.ModuleRegistry;
import org.obiba.onyx.engine.Stage;

final public class StageModel extends LoadableDetachableModel {

  private static final long serialVersionUID = 1L;

  private ModuleRegistry moduleRegistry;

  private String stageName;

  public StageModel(ModuleRegistry registry, Stage stage) {
    super(stage);
    this.moduleRegistry = registry;
    this.stageName = stage.getName();
  }

  public StageModel(ModuleRegistry registry, String stageName) {
    super(registry.getStage(stageName));
    this.moduleRegistry = registry;
    this.stageName = stageName;
  }

  @Override
  protected Object load() {
    return this.moduleRegistry.getStage(stageName);
  }
}
