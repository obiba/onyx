package org.obiba.onyx.mica.engine.state;

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
public class ModuleStageDependencyCondition extends StageDependencyCondition {

  private static final long serialVersionUID = 1L;

  private ModuleRegistry moduleRegistry;

  private String moduleName;

  public ModuleStageDependencyCondition() {
  }

  /**
   * Returns a Boolean depending on the fact that the step is completed and also on its result 
   * Null if not completed
   * True if completed 
   */
  @SuppressWarnings("static-access")
  @Override
  public Boolean isDependencySatisfied(ActiveInterviewService activeInterviewService) {

    IStageExecution stageExecution;
    
    Module module = moduleRegistry.getModule(moduleName);

    System.out.println("*******mica******" + module + "*************");
    List<Stage> moduleStage = module.getStages();

    Boolean isDepSatisfied = true;

    for(Stage oneStage : moduleStage) {
      stageExecution = activeInterviewService.getStageExecution(oneStage.getName());

      System.out.println("******mica***dependency***before***" +isDepSatisfied+"*********************");
      
      System.out.println("******mica*******" + oneStage.getName() + "*************");
      System.out.println("******mica*******" + stageExecution.isCompleted() + "*************");
      
      
      if(!stageExecution.isCompleted()) {
        isDepSatisfied = null;
      } 
      else {
        if(isDepSatisfied.equals(true)) {
          isDepSatisfied = true;
        }
      }
      System.out.println("******mica***dependency***after***" +isDepSatisfied+"*********************");
    }
    return isDepSatisfied;
  }

  @Override
  public boolean isDependentOn(String stageName) {
    
    
    return this.moduleName.equals(moduleName);
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