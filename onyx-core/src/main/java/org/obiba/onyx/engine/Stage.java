package org.obiba.onyx.engine;

import org.obiba.onyx.core.service.UserSessionService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Stage is associated to a module, through its name.
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
