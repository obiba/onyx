/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.standard;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
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
import org.obiba.onyx.quartz.core.wicket.layout.IQuestionCategorySelectionListener;
import org.obiba.onyx.quartz.core.wicket.layout.PageLayout;
import org.obiba.onyx.quartz.core.wicket.layout.QuestionPanel;
import org.obiba.onyx.quartz.core.wicket.layout.QuestionPanelFactoryRegistry;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.PageQuestionsProvider;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;

/**
 * Default page layout implementation, as a list of questions to be answered, presented in their self-provided UI, and
 * able to deal with in-page conditions resolution updates.
 * @see IQuestionAnswerChangedListener
 * @see Condition
 */
public class DefaultPageLayout extends PageLayout implements IQuestionCategorySelectionListener {

  private static final long serialVersionUID = -1757316578083924986L;

  @SpringBean
  private QuestionPanelFactoryRegistry questionPanelFactoryRegistry;

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  /**
   * Constructor, given a questionnaire page.
   * @param id
   * @param pageModel
   */
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
        item.add(panel);
      }

    });
    questionsView.setItemReuseStrategy(ReuseIfModelsEqualStrategy.getInstance());
  }

  /**
   * Called when going next page, forward it to page questions.
   */
  public void onNext(AjaxRequestTarget target) {
    for(QuestionPanel panel : getQuestionPanels()) {
      panel.onNext(target);
    }
  }

  /**
   * Called when going previous page, forward it to page questions.
   */
  public void onPrevious(AjaxRequestTarget target) {
    for(QuestionPanel panel : getQuestionPanels()) {
      panel.onPrevious(target);
    }
  }

  /**
   * Get by introspection the question panels (at all depth) of the page.
   * @return
   */
  public List<QuestionPanel> getQuestionPanels() {
    final List<QuestionPanel> questionPanels = new ArrayList<QuestionPanel>();

    visitChildren(QuestionPanel.class, new Component.IVisitor() {

      public Object component(Component component) {
        questionPanels.add((QuestionPanel) component);
        return CONTINUE_TRAVERSAL;
      }

    });

    return questionPanels;
  }

  public void onQuestionAnswerChanged(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {

  }

  @Override
  public void onStepInNext(AjaxRequestTarget target) {
  }

  @Override
  public void onStepInPrevious(AjaxRequestTarget target) {
  }

  /**
   * Called when an answer is given to a question and then requires to update the resolution of in-page conditions.
   */
  public void onQuestionCategorySelection(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel, boolean isSelected) {
    for(QuestionPanel panel : getQuestionPanels()) {
      Question question = (Question) panel.getModelObject();
      if(!question.isToBeAnswered(activeQuestionnaireAdministrationService)) {
        // a question is not to be answered any more due to in-page conditions, make sure subsequent conditions will be
        // correctly resolved.
        panel.setActiveAnswers(false);
      }
    }
    // update the whole layout because some questions can (dis)appear.
    target.addComponent(this);
    target.appendJavascript("Resizer.resizeWizard();");
  }

}
