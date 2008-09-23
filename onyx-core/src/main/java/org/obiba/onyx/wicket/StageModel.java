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