package org.obiba.onyx.quartz.core.engine.questionnaire.question;

import java.io.Serializable;

public class Category implements Serializable, ILocalizable {

  private static final long serialVersionUID = -1722883141794376906L;

  private String name;

  private OpenAnswerDefinition openAnswerDefinition;

  public Category(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public OpenAnswerDefinition getOpenAnswerDefinition() {
    return openAnswerDefinition;
  }

  public void setOpenAnswerDefinition(OpenAnswerDefinition openAnswerDefinition) {
    this.openAnswerDefinition = openAnswerDefinition;
  }

  private static final String[] PROPERTIES = { "label", "image" };

  public String getPropertyKey(String property) {
    for(String key : PROPERTIES) {
      if(key.equals(property)) {
        return getClass().getSimpleName() + "." + getName() + "." + property;
      }
    }
    throw new IllegalArgumentException("Invalid property for class " + getClass().getName() + ": " + property);
  }

  public String[] getProperties() {
    return PROPERTIES;
  }

}
