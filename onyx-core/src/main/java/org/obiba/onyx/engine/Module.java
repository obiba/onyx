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

import org.apache.wicket.Component;
import org.apache.wicket.protocol.http.WebApplication;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.engine.state.IStageExecution;

/**
 * Definition of an Onyx module.
 * <p>
 * Modules contribute {@link Stage}s to the Onyx runtime engine. An {@link Interview} is an ordered sequence of
 * {@link Stage}s. When a {@link Stage} needs to be executed for an {@link Interview} the <code>Module</code> that
 * contributes the {@link Stage} is responsible for creating a {@link IStageExecution}.
 * 
 */
public interface Module {

  /**
   * Unique name that identifies the module.
   * @return the module name
   */
  public String getName();

  /**
   * Creates a {@link IStageExecution} for the specified {@link Stage} for the specified {@link Interview}. The module
   * implementation is responsible for creating an instance of {@link IStageExecution} that is in the proper state for
   * the specified {@link Interview}.
   * 
   * @param interview the {@link Interview} instance in which the {@link Stage} is executed.
   * @param stage the {@link Stage} instance to be executed.
   * @return an instance of {@link IStageExecution}
   */
  public IStageExecution createStageExecution(Interview interview, Stage stage);

  /**
   * Called at module registration.
   * 
   * @param application
   * @see ModuleRegistry
   */
  public void initialize(WebApplication application);

  /**
   * Called at module unregistration.
   * 
   * @param application
   * @see ModuleRegistry
   */
  public void shutdown(WebApplication application);

  /**
   * Returns the list of {@link Stage}s that are contributed by this <code>Module</code>.
   * @return an unmodifiable list of {@link Stage} instances.
   */
  public List<Stage> getStages();

  /**
   * Get the {@link Component} to be displayed.
   * @see #isInteractive()
   * @param id
   * @return
   */
  public Component getWidget(String id);

  /**
   * Say if {@link #getWidget(String)} will return a non null value. This decides if te module is part of the
   * configurable ones.
   * @return
   */
  public boolean isInteractive();
}
