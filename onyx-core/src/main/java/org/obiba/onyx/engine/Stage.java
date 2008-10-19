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
import org.obiba.onyx.core.service.UserSessionService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * A <code>Stage</code> is a step of an {@link Interview}. They are contributed by {@link Module}s. A
 * <code>Stage</code>'s name must be unique throughout all contributed stages.
 * <p>
 * Some <code>Stage</code>s have dependencies on other <code>Stage</code>s. The actual dependency logic is
 * encapsulated in its {@link StageDependencyCondition} instance.
 * 
 * @see Module
 */
public class Stage implements ApplicationContextAware {

  private static final long serialVersionUID = 8309472904104798783L;

  private String name;

  private String module;

  private String description;

  private Integer displayOrder;

  private StageDependencyCondition stageDependencyCondition;

  private ApplicationContext context;

  private UserSessionService userSessionService;

  public void setApplicationContext(ApplicationContext context) {
    this.context = context;
  }

  public void setUserSessionService(UserSessionService userSessionService) {
    this.userSessionService = userSessionService;
  }

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

  public String getDescription() {
    String retVal = description;

    if(context != null && userSessionService != null) {
      retVal = context.getMessage(description, null, userSessionService.getLocale());
    }

    return retVal;
  }

  public void setDescription(String description) {
    this.description = description;
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
