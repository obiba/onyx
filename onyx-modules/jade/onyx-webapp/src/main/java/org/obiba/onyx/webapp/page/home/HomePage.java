package org.obiba.onyx.webapp.page.home;

import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.engine.Module;
import org.obiba.onyx.engine.ModuleRegistry;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.webapp.page.base.BasePage;

public class HomePage extends BasePage {

  @SpringBean
  private ModuleRegistry registry;

  public HomePage() {
    super();

    // TODO make this part dynamic: the list of stages should probably come from the registered modules...
    Stage stage = new Stage();
    stage.setName("Bioimpedence");
    stage.setModule("jade");
    // end TODO

    Module module = registry.getModule(stage.getModule());
    add(module.startStage(stage).createStageComponent("stage-component"));
  }

}
