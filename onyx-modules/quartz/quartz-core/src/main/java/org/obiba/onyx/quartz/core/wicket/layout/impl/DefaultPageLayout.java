package org.obiba.onyx.quartz.core.wicket.layout.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.wicket.layout.IQuestionPanelFactory;
import org.obiba.onyx.quartz.core.wicket.layout.PageLayout;
import org.obiba.onyx.quartz.core.wicket.layout.QuestionPanel;
import org.obiba.onyx.quartz.core.wicket.layout.PageQuestionsProvider;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;

public class DefaultPageLayout extends PageLayout {

  private static final long serialVersionUID = -1757316578083924986L;

  @SpringBean
  private IQuestionPanelFactory questionPanelFactory;
  
  private List<QuestionPanel> questionPanels = new ArrayList<QuestionPanel>();

  @SuppressWarnings("serial")
  public DefaultPageLayout(String id, IModel model) {
    super(id, model);
    
    Page page = (Page) model.getObject();
    add(new Label(id, new QuestionnaireStringResourceModel(page, "label", null)));
    
    add(new DataView("questions", new PageQuestionsProvider(page)) {

      @Override
      protected void populateItem(Item item) {
        QuestionPanel panel = questionPanelFactory.createPanel("question", (Question)item.getModelObject());
        questionPanels.add(panel);
        item.add(panel);
      }
      
    });
  }

  public void onNext() {
    for (QuestionPanel panel : questionPanels) {
      panel.onNext();
    }
  }

  public void onPrevious() {
    for (QuestionPanel panel : questionPanels) {
      panel.onPrevious();
    }
  }

}
