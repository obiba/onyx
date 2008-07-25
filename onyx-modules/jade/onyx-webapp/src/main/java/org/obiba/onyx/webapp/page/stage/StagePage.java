package org.obiba.onyx.webapp.page.stage;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.engine.Module;
import org.obiba.onyx.engine.ModuleRegistry;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.webapp.page.base.BasePage;
import org.obiba.wicket.markup.html.table.DetachableEntityModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StagePage extends BasePage {
  
  private static final Logger log = LoggerFactory.getLogger(StagePage.class);
      
  @SpringBean
  private ModuleRegistry registry;

  public StagePage(DetachableEntityModel stageModel) {
    super();

    Stage stage = (Stage) stageModel.getObject();
    Module module = registry.getModule(stage.getModule());
    Stage currentStage = module.getCurrentStageExecution().getStage(); 
    if(currentStage != null && currentStage.getName().equals(stage.getName())) {
      add(new Label("action", "resuming..."));
      add(module.resume(stage).createStageComponent("stage-component"));
    }
    else {
      add(new Label("action", "starting..."));
      add(module.start(stage).createStageComponent("stage-component"));
    }
    
  }

}
