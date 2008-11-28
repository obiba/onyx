/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.ReuseIfModelsEqualStrategy;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.layout.PageLayout;
import org.obiba.onyx.quartz.core.wicket.layout.QuestionPanel;
import org.obiba.onyx.quartz.core.wicket.layout.QuestionPanelFactoryRegistry;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.PageQuestionsProvider;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;

public class DefaultPageLayout extends PageLayout {

  private static final long serialVersionUID = -1757316578083924986L;

  @SpringBean
  private QuestionPanelFactoryRegistry questionPanelFactoryRegistry;

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  private List<QuestionPanel> questionPanels = new ArrayList<QuestionPanel>();

  @SuppressWarnings("serial")
  public DefaultPageLayout(String id, IModel pageModel) {
    super(id, pageModel);
    setOutputMarkupId(true);

    Page page = (Page) getModelObject();
    Section section = page.getSection();
    String sectionString = (new QuestionnaireStringResourceModel(section, "label")).getString();
    section = section.getParentSection();
    while(section != null) {
      String str = (new QuestionnaireStringResourceModel(section, "label")).getString();
      if(str != null && str.trim().length() != 0) {
        sectionString = str + " / " + sectionString;
      }
      section = section.getParentSection();
    }
    add(new Label("section", sectionString));

    add(new Label("label", new QuestionnaireStringResourceModel(page, "label")));

    DataView questionsView;
    add(questionsView = new DataView("questions", new PageQuestionsProvider(page)) {

      @Override
      protected void populateItem(Item item) {
        Question question = (Question) item.getModelObject();

        QuestionPanel panel = questionPanelFactoryRegistry.get(question.getUIFactoryName()).createPanel("question", item.getModel());
        questionPanels.add(panel);
        item.add(panel);
      }

    });
    questionsView.setItemReuseStrategy(ReuseIfModelsEqualStrategy.getInstance());

    // TODO get it from questionnaire bundle ?
    add(HeaderContributor.forCss("css/questionnaire.css"));
  }

  public void onNext(AjaxRequestTarget target) {
    for(QuestionPanel panel : questionPanels) {
      panel.onNext(target);
    }
  }

  public void onPrevious(AjaxRequestTarget target) {
    for(QuestionPanel panel : questionPanels) {
      panel.onPrevious(target);
    }
  }

}
