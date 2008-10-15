package org.obiba.onyx.quartz.core.engine.questionnaire.util.finder;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;

public class QuestionFinder extends AbstractFinderVisitor<Question> {

  public QuestionFinder(String name) {
    super(name);
  }

  public QuestionFinder(String name, boolean stopAtFirst) {
    super(name, stopAtFirst);
  }

  public void visit(Questionnaire questionnaire) {
  }

  public void visit(Section section) {
  }

  public void visit(Page page) {
  }

  public void visit(Question question) {
    visitElement(question);
  }

  public void visit(QuestionCategory questionCategory) {
  }

  public void visit(Category category) {
  }

  public void visit(OpenAnswerDefinition openAnswerDefinition) {
  }

}
