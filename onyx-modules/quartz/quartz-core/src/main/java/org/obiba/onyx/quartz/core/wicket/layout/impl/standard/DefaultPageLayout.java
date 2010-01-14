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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxRequestTarget.IJavascriptResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Radio;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default page layout implementation, as a list of questions to be answered, presented in their self-provided UI, and
 * able to deal with in-page conditions resolution updates.
 * @see IQuestionAnswerChangedListener
 * @see Condition
 */
public class DefaultPageLayout extends PageLayout implements IQuestionCategorySelectionListener {

  private static final long serialVersionUID = -1757316578083924986L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(DefaultPageLayout.class);

  @SpringBean
  private QuestionPanelFactoryRegistry questionPanelFactoryRegistry;

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  private DataView<QuestionPanel> questionsView;

  /**
   * Constructor, given a questionnaire page.
   * @param id
   * @param pageModel
   */
  @SuppressWarnings("serial")
  public DefaultPageLayout(String id, IModel pageModel) {
    super(id, pageModel);
    setOutputMarkupId(true);

    Page page = (Page) getDefaultModelObject();
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
    add(new Label("section", sectionString).setVisible(!isEmptyString(sectionString)));

    QuestionnaireStringResourceModel pageLabelModel = new QuestionnaireStringResourceModel(page, "label");
    add(new Label("label", pageLabelModel).setVisible(!isEmptyString(pageLabelModel.getString())));

    add(questionsView = new DataView<QuestionPanel>("questions", new PageQuestionsProvider(page)) {

      @Override
      protected void populateItem(Item item) {
        Question question = (Question) item.getModelObject();

        QuestionPanel panel = questionPanelFactoryRegistry.get(question.getUIFactoryName()).createPanel("question", item.getModel());
        item.add(panel);
      }

    });
    questionsView.setItemReuseStrategy(ReuseIfModelsEqualStrategy.getInstance());
  }

  private boolean isEmptyString(String str) {
    return str == null || str.trim().equals("");
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

  @Override
  public void onStepInNext(AjaxRequestTarget target) {
    setFocus(target);
  }

  @Override
  public void onStepInPrevious(AjaxRequestTarget target) {
    setFocus(target);
  }

  /**
   * Set the focus to the first input of the first question.
   * @param target
   */
  private void setFocus(final AjaxRequestTarget target) {
    // must do that after rendering, otherwise there is nothing populated in the views.
    target.addListener(new AjaxRequestTarget.IListener() {

      public void onAfterRespond(Map<String, Component> map, IJavascriptResponse response) {
        Iterator<Item<QuestionPanel>> iter = questionsView.getItems();
        if(iter.hasNext()) {
          setFocus(iter.next(), target);
        }
      }

      public void onBeforeRespond(Map<String, Component> map, AjaxRequestTarget target) {

      }

    });
  }

  /**
   * Look for an input component to be focused by looking in the children component recursively.
   * @param parent
   * @param target
   */
  private void setFocus(MarkupContainer parent, final AjaxRequestTarget target) {
    parent.visitChildren(new IVisitor<Component>() {

      public Object component(Component component) {
        if(component instanceof Radio || component instanceof CheckBox || component instanceof DropDownChoice<?>) {
          target.focusComponent(component);
          return STOP_TRAVERSAL;
        }
        return null;
      }
    });
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

  /**
   * Called when an answer is given to a question and then requires to update the resolution of in-page conditions.
   * In-page conditions on sub-questions is not supported yet.
   */
  public void onQuestionCategorySelection(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel, boolean isSelected) {
    boolean questionListChanged = false;
    List<QuestionPanel> questionPanels = getQuestionPanels();
    for(QuestionPanel panel : questionPanels) {
      Question question = (Question) panel.getDefaultModelObject();
      if(!question.isToBeAnswered(activeQuestionnaireAdministrationService)) {
        // a question is not to be answered any more due to in-page conditions, make sure subsequent conditions will be
        // correctly resolved.
        panel.setActiveAnswers(false);
        questionListChanged = true;
      }
    }
    log.debug("questionListChanged={}", questionListChanged);

    if(!questionListChanged) {

      if(getQuestionToBeAnsweredCount() > questionPanels.size()) {
        questionListChanged = true;
      }
    }
    log.debug("questionListChanged={}", questionListChanged);

    // update the whole layout because some questions can (dis)appear.
    if(questionListChanged) {
      log.debug("Page update");
      target.addComponent(this);
      target.appendJavascript("Resizer.resizeWizard();");
    }

  }

  /**
   * Get the count of question to be answered, resolving conditions (does not check conditions on sub questions).
   * @return
   */
  private int getQuestionToBeAnsweredCount() {
    int count = 0;
    Page page = (Page) getDefaultModelObject();

    for(Question question : page.getQuestions()) {
      if(!question.hasDataSource() && question.isToBeAnswered(activeQuestionnaireAdministrationService)) {
        count++;
      }
    }

    return count;
  }

}
