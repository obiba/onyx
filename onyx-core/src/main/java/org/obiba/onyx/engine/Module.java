package org.obiba.onyx.engine;

import java.util.List;

import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.engine.state.IStageExecution;

/**
 * Module interface.
 * @author Yannick Marcon
 * 
 */
public interface Module {

  /**
   * Unique name that identifies the module.
   * @return
   */
  public String getName();

  /**
   * Create the corresponding stage execution.
   * @param interview
   * @param stage
   * @return
   */
  public IStageExecution createStageExecution(Interview interview, Stage stage);

  /**
   * Called at module registration.
   * @see ModuleRegistry
   */
  public void initialize();

  /**
   * Called at module deregistration.
   * @see ModuleRegistry
   */
  public void shutdown();

  public List<Stage> getStages();
}
