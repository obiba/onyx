package org.obiba.onyx.quartz.core.engine.questionnaire.question;

/**
 * Questionnaire element visitor.
 * @author Yannick Marcon
 *
 */
public interface IQuestionnaireVisitor {

  public void visit(Questionnaire questionnaire);
  
  public void visit(Section section);
  
  public void visit(Page page);
  
  public void visit(Question question);
  
  public void visit(QuestionCategory questionCategory);
  
  public void visit(Category category);
  
  public void visit(OpenAnswerDefinition openAnswerDefinition);
  
}
