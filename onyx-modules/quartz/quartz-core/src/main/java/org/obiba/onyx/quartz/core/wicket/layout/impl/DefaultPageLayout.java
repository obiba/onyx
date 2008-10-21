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
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.wicket.layout.PageLayout;
import org.obiba.onyx.quartz.core.wicket.layout.PageQuestionsProvider;
import org.obiba.onyx.quartz.core.wicket.layout.QuestionPanel;
import org.obiba.onyx.quartz.core.wicket.layout.QuestionPanelFactoryRegistry;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;

public class DefaultPageLayout extends PageLayout {

  private static final long serialVersionUID = -1757316578083924986L;

  @SpringBean
  private QuestionPanelFactoryRegistry questionPanelFactoryRegistry;

  private List<QuestionPanel> questionPanels = new ArrayList<QuestionPanel>();

  @SuppressWarnings("serial")
  public DefaultPageLayout(String id, Page page) {
    super(id, page);

    add(new Label("section", new QuestionnaireStringResourceModel(page.getSection(), "label")));

    add(new Label("label", new QuestionnaireStringResourceModel(page, "label")));

    add(new DataView("questions", new PageQuestionsProvider(page)) {

      @Override
      protected void populateItem(Item item) {
        Question question = (Question) item.getModelObject();
        QuestionPanel panel = questionPanelFactoryRegistry.getFactory(question.getUIFactoryName()).createPanel("question", question);
        questionPanels.add(panel);
        item.add(panel);
      }

    });

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
