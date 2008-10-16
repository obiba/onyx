/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine;

import java.util.List;

import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.Module;
import org.obiba.onyx.engine.ModuleRegistry;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.StageDependencyCondition;
import org.obiba.onyx.engine.state.IStageExecution;

/**
 * Mica specific dependency condition depending on the presence of runValues for the stage
 * @author acarey
 */
public class ModuleDependencyCondition extends StageDependencyCondition {

  private static final long serialVersionUID = 1L;

  private ModuleRegistry moduleRegistry;

  private String moduleName;

  public ModuleDependencyCondition() {
  }

  /**
   * Returns true if dependency is satisfied, false if it is not,
   * null if it's impossible to know whether it's right or wrong (step not done yet)
   * @param activeInterviewService
   * @return
   */
  @SuppressWarnings("static-access")
  @Override
  public Boolean isDependencySatisfied(ActiveInterviewService activeInterviewService) {

    Module module = moduleRegistry.getModule(moduleName);

    List<Stage> moduleStage = module.getStages();

    for(Stage oneStage : moduleStage) {
      IStageExecution stageExecution = activeInterviewService.getStageExecution(oneStage.getName());

      if(!stageExecution.isCompleted()) {
        return null;
      } 
    }
    
    return true;
  }

  @Override
  public boolean isDependentOn(String stageName) {
    
    Module module = moduleRegistry.getModule(moduleName);

    List<Stage> moduleStage = module.getStages();

    for(Stage oneStage : moduleStage) {

      if(oneStage.getName().equals(stageName)) {
        return true;
      } 
    }
    
    return false;
  }
  
  public ModuleRegistry getModuleRegistry() {
    return moduleRegistry;
  }

  public void setModuleRegistry(ModuleRegistry moduleRegistry) {
    this.moduleRegistry = moduleRegistry;
  }

  public String getModuleName() {
    return moduleName;
  }

  public void setModuleName(String moduleName) {
    this.moduleName = moduleName;
  }
}
