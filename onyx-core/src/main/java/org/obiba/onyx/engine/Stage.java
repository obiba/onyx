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

import org.obiba.onyx.core.domain.participant.Interview;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;

/**
 * A <code>Stage</code> is a step of an {@link Interview}. They are contributed by {@link Module}s. A
 * <code>Stage</code>'s name must be unique throughout all contributed stages.
 * <p>
 * Some <code>Stage</code>s have dependencies on other <code>Stage</code>s. The actual dependency logic is
 * encapsulated in its {@link StageDependencyCondition} instance.
 * 
 * @see Module
 */
public class Stage {

  private static final String DESCRIPTION_KEY = ".description";

  private String name;

  private String module;

  private Integer displayOrder;

  private StageDependencyCondition stageDependencyCondition;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getModule() {
    return module;
  }

  public void setModule(String module) {
    this.module = module;
  }

  public MessageSourceResolvable getDescription() {
    // Codes are <module>.<name>.description, <name>.description
    return new DefaultMessageSourceResolvable(new String[] { getModule() + "." + getName() + DESCRIPTION_KEY, getName() + DESCRIPTION_KEY }, getName());
  }

  public Integer getDisplayOrder() {
    return displayOrder;
  }

  public void setDisplayOrder(Integer displayOrder) {
    this.displayOrder = displayOrder;
  }

  public StageDependencyCondition getStageDependencyCondition() {
    return stageDependencyCondition;
  }

  public void setStageDependencyCondition(StageDependencyCondition stageDependencyCondition) {
    this.stageDependencyCondition = stageDependencyCondition;
  }

  @Override
  public String toString() {
    return module + ":" + name;
  }
}
