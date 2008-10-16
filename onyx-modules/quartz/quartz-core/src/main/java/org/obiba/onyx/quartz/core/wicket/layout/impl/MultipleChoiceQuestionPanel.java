package org.obiba.onyx.quartz.core.wicket.layout.impl;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.wicket.layout.QuestionPanel;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;

public class MultipleChoiceQuestionPanel extends QuestionPanel {

  private static final long serialVersionUID = 2951128797454847260L;

  public MultipleChoiceQuestionPanel(String id, IModel model) {
    super(id, model);
    
    Question question = (Question)model.getObject();
    add(new Label("label", new QuestionnaireStringResourceModel(question, "label", null)));
  }
  
  public void onNext(AjaxRequestTarget target) {
    // TODO Auto-generated method stub

  }

  public void onPrevious(AjaxRequestTarget target) {
    // TODO Auto-generated method stub

  }

}
