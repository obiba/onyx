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
import org.obiba.onyx.engine.state.IStageExecution;

/**
 * An implementation of {@link StageDependencyCondition} that requires that all {@link Stage}s of a {@link Module} be
 * complete. This class requires the {@link ModuleRegistry} instance.
 * <p>
 * This allows a stage to be executed only after all the stages of a particular module have been completed.
 */
public class ModuleDependencyCondition implements StageDependencyCondition {

  private static final long serialVersionUID = 1L;

  private ModuleRegistry moduleRegistry;

  private String moduleName;

  public ModuleDependencyCondition() {
  }

  public Boolean isDependencySatisfied(ActiveInterviewService activeInterviewService) {

    Module module = moduleRegistry.getModule(moduleName);

    List<Stage> moduleStage = module.getStages();

    for(Stage oneStage : moduleStage) {
      IStageExecution stageExecution = activeInterviewService.getStageExecution(oneStage.getName());

      if(!stageExecution.isCompleted()) {
        // At least one stage is not complete, return null since
        return null;
      }
    }

    return true;
  }

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

  @Override
  public String toString() {
    return "[" + getClass().getSimpleName() + ":" + moduleName + "]";
  }
}
