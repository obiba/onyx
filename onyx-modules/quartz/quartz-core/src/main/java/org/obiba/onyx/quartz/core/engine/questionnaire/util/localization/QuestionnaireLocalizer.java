package org.obiba.onyx.quartz.core.engine.questionnaire.util.localization;

import java.util.List;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.ILocalizable;

public class QuestionnaireLocalizer implements IVisitingQuestionnaireLocalizer {

  private IQuestionnaireLocalizationVisitor questionnaireVisitor;

  public void setQuestionnaireVisitor(IQuestionnaireLocalizationVisitor questionnaireVisitor) {
    this.questionnaireVisitor = questionnaireVisitor;
  }

  public String getPropertyKey(ILocalizable localizable, String property) {
    questionnaireVisitor.setProperty(property);
    localizable.accept(questionnaireVisitor);

    return localizable.getClass().getSimpleName() + "." + localizable.getName() + "." + property;
  }

  public List<String> getProperties(ILocalizable localizable) {
    questionnaireVisitor.setProperty(null);
    localizable.accept(questionnaireVisitor);
    
    return questionnaireVisitor.getProperties();
  }

}
