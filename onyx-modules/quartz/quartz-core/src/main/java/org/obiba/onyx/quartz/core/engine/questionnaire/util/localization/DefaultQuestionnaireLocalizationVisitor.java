package org.obiba.onyx.quartz.core.engine.questionnaire.util.localization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.ILocalizable;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;
import org.obiba.onyx.util.data.Data;

/**
 * Default property set for each of the questionnaire element that will be visited for localization.
 * @author Yannick Marcon
 *
 */
public class DefaultQuestionnaireLocalizationVisitor implements IQuestionnaireLocalizationVisitor {

  private String property;

  private List<String> properties;

  public void setProperty(String property) {
    this.property = property;
  }

  public void visit(Questionnaire questionnaire) {
    properties = Arrays.asList("label", "description", "labelNext", "imageNext", "labelPrevious", "imagePrevious", "labelStart", "labelFinish", "labelInterrupt", "labelResume", "labelCancel");

    if(property != null && !properties.contains(property)) {
      throw invalidPropertyException(questionnaire);
    }
  }

  public void visit(Section section) {
    properties = Arrays.asList("label");

    if(property != null && !properties.contains(property)) {
      throw invalidPropertyException(section);
    }
  }

  public void visit(Page page) {
    properties = Arrays.asList("label");

    if(property != null && !properties.contains(property)) {
      throw invalidPropertyException(page);
    }
  }

  public void visit(Question question) {
    properties = Arrays.asList("label", "instructions", "caption", "help", "image" );

    if(property != null && !properties.contains(property)) {
      throw invalidPropertyException(question);
    }
  }

  public void visit(QuestionCategory questionCategory) {
    visit(questionCategory.getCategory());
  }

  public void visit(Category category) {
    properties = Arrays.asList("label", "image" );

    if(property != null && !properties.contains(property)) {
      throw invalidPropertyException(category);
    }
  }

  public void visit(OpenAnswerDefinition openAnswerDefinition) {
    properties = new ArrayList<String>(Arrays.asList("label", "unitLabel" ));
    for (Data value : openAnswerDefinition.getDefaultValues()) {
      properties.add(value.getValueAsString());
    }
    
    if(property != null && !properties.contains(property)) {
      throw invalidPropertyException(openAnswerDefinition);
    }
  }

  private IllegalArgumentException invalidPropertyException(ILocalizable localizable) {
    return new IllegalArgumentException("Invalid property for class " + localizable.getClass().getName() + ": " + property);
  }

  public List<String> getProperties() {
    return properties;
  }

}
