package org.obiba.onyx.quartz.core.engine.questionnaire.util.localization;

import java.util.List;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.ILocalizable;

public interface IQuestionnaireLocalizer {
  
  public String getPropertyKey(ILocalizable localizable, String property);
  
  public List<String> getProperties(ILocalizable localizable);
  
}
