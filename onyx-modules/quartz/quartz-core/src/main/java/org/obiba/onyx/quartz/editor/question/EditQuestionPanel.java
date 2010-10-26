/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.question;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionType;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.editor.category.CategoriesPanel;
import org.obiba.onyx.quartz.editor.locale.LocaleProperties;
import org.obiba.onyx.quartz.editor.locale.LocalePropertiesUtils;
import org.obiba.onyx.quartz.editor.openAnswerDefinition.OpenAnswerPanel;
import org.obiba.onyx.quartz.editor.questionnaire.QuestionnairePersistenceUtils;
import org.obiba.onyx.quartz.editor.utils.tab.AjaxSubmitTabbedPanel;
import org.obiba.onyx.quartz.editor.utils.tab.HidableTab;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
@SuppressWarnings("serial")
public abstract class EditQuestionPanel extends Panel {

  private transient Logger logger = LoggerFactory.getLogger(getClass());

  @SpringBean
  private LocalePropertiesUtils localePropertiesUtils;

  @SpringBean
  private QuestionnairePersistenceUtils questionnairePersistenceUtils;

  private final FeedbackPanel feedbackPanel;

  private final FeedbackWindow feedbackWindow;

  private final Form<EditedQuestion> form;

  private final IModel<LocaleProperties> localePropertiesModel;

  private final AjaxSubmitTabbedPanel tabbedPanel;

  private final IModel<Questionnaire> questionnaireModel;

  private HidableTab openAnswerTab;

  private HidableTab categoriesTab;

  private HidableTab rowsTab;

  private HidableTab columnsTab;

  public EditQuestionPanel(String id, final IModel<Question> questionModel, final IModel<Questionnaire> questionnaireModel, final ModalWindow questionWindow) {
    super(id);
    this.questionnaireModel = questionnaireModel;

    Question question = questionModel.getObject();

    logger.info("question: " + question);

    EditedQuestion editedQuestion = null;
    if(StringUtils.isBlank(question.getName())) {
      editedQuestion = new EditedQuestion(null);
      editedQuestion.setElement(question);
    } else {
      editedQuestion = new EditedQuestion(question);
    }
    final IModel<EditedQuestion> model = new Model<EditedQuestion>(editedQuestion);

    setDefaultModel(model);

    localePropertiesModel = new Model<LocaleProperties>(localePropertiesUtils.load(questionnaireModel.getObject(), question));

    feedbackPanel = new FeedbackPanel("content");
    feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);

    add(feedbackWindow);

    add(form = new Form<EditedQuestion>("form", model));

    final List<ITab> tabs = new ArrayList<ITab>();

    openAnswerTab = new HidableTab(new ResourceModel("OpenAnswer")) {
      @Override
      public Panel getPanel(String panelId) {
        return new OpenAnswerPanel(panelId, new Model<OpenAnswerDefinition>(new OpenAnswerDefinition()), questionModel, questionnaireModel, localePropertiesModel, feedbackPanel, feedbackWindow);
      }
    };
    openAnswerTab.setVisible(false);

    categoriesTab = new HidableTab(new ResourceModel("Categories")) {
      @Override
      public Panel getPanel(String panelId) {
        return new CategoriesPanel(panelId, model, questionnaireModel, localePropertiesModel, feedbackPanel, feedbackWindow);
      }
    };
    categoriesTab.setVisible(false);

    rowsTab = new HidableTab(new ResourceModel("Rows(questions)")) {
      @Override
      public Panel getPanel(String panelId) {
        return new Panel(panelId, model);
      }
    };
    rowsTab.setVisible(false);

    columnsTab = new HidableTab(new ResourceModel("Columns(categories)")) {
      @Override
      public Panel getPanel(String panelId) {
        return new Panel(panelId, model);
      }
    };
    columnsTab.setVisible(false);

    ITab questionTab = new AbstractTab(new ResourceModel("Question")) {
      @Override
      public Panel getPanel(String panelId) {
        return new QuestionPanel(panelId, model, questionnaireModel, localePropertiesModel, feedbackPanel, feedbackWindow) {
          @Override
          public void onQuestionTypeChange(AjaxRequestTarget target, QuestionType questionType) {
            setTabsVisibility(questionType);
            if(tabbedPanel != null && target != null) {
              target.addComponent(tabbedPanel);
            }
          }
        };
      }
    };

    tabs.add(questionTab);
    tabs.add(openAnswerTab);
    tabs.add(categoriesTab);
    tabs.add(rowsTab);
    tabs.add(columnsTab);
    tabs.add(new AbstractTab(new ResourceModel("Conditions")) {
      @Override
      public Panel getPanel(String panelId) {
        return new Panel(panelId, model);
      }
    });
    tabs.add(new AbstractTab(new ResourceModel("Preview")) {
      @Override
      public Panel getPanel(String panelId) {
        return new QuestionPreviewPanel(panelId, model, questionnaireModel);
      }
    });

    setTabsVisibility(editedQuestion.getQuestionType());

    form.add(tabbedPanel = new AjaxSubmitTabbedPanel("tabs", feedbackPanel, feedbackWindow, tabs));

    form.add(new AjaxButton("save", form) {
      @Override
      public void onSubmit(AjaxRequestTarget target, Form<?> form2) {
        // save each tab
        switch(form.getModelObject().getQuestionType()) {
        case SINGLE_OPEN_ANSWER:
          // openAnswerTab.save();
          break;

        case LIST_CHECKBOX:
        case LIST_RADIO:
        case LIST_DROP_DOWN:
          // categoriesTab.save();
          break;

        case ARRAY_CHECKBOX:
        case ARRAY_RADIO:
          // rowsTab.save();
          // columnsTab.save();
          break;

        case BOILER_PLATE:
          break;
        }

        onSave(target, form.getModelObject());
        questionWindow.close(target);
      }

      @Override
      protected void onError(AjaxRequestTarget target, Form<?> form2) {
        feedbackWindow.setContent(feedbackPanel);
        feedbackWindow.show(target);
      }
    });

    form.add(new AjaxButton("cancel", form) {
      @Override
      public void onSubmit(AjaxRequestTarget target, Form<?> form2) {
        questionWindow.close(target);
      }
    }.setDefaultFormProcessing(false));
  }

  private void setTabsVisibility(QuestionType questionType) {
    if(questionType == null) return;
    switch(questionType) {
    case SINGLE_OPEN_ANSWER:
      openAnswerTab.setVisible(true);
      categoriesTab.setVisible(false);
      rowsTab.setVisible(false);
      columnsTab.setVisible(false);
      break;

    case LIST_CHECKBOX:
    case LIST_RADIO:
    case LIST_DROP_DOWN:
      categoriesTab.setVisible(true);
      openAnswerTab.setVisible(false);
      rowsTab.setVisible(false);
      columnsTab.setVisible(false);
      break;

    case ARRAY_CHECKBOX:
    case ARRAY_RADIO:
      rowsTab.setVisible(true);
      columnsTab.setVisible(true);
      openAnswerTab.setVisible(false);
      categoriesTab.setVisible(false);
      break;

    case BOILER_PLATE:
      break;
    }
  }

  /**
   * 
   * @param target
   * @param editedQuestion
   */
  public abstract void onSave(AjaxRequestTarget target, EditedQuestion editedQuestion);

  // {
  // final Question question = editedQuestion.getElement();
  //
  // if(form.getModelObject().getElement().getParentQuestion() == null) {
  // categoryList.save(target, new SortableListCallback<QuestionCategory>() {
  // @Override
  // public void onSave(List<QuestionCategory> orderedItems, AjaxRequestTarget target1) {
  // question.getQuestionCategories().clear();
  // for(QuestionCategory questionCategory : orderedItems) {
  // question.getQuestionCategories().add(questionCategory);
  // }
  // }
  // });
  // }
  //
  // // Layout single or grid: make sure that the categories are added before this...
  // String layoutSelection = layoutRadioGroup.getModelObject();
  // if(SINGLE_COLUMN_LAYOUT.equals(layoutSelection)) {
  // question.clearUIArguments();
  // question.addUIArgument(ROW_COUNT_KEY, question.getCategories().size() + "");
  // } else if(GRID_LAYOUT.equals(layoutSelection)) {
  // question.clearUIArguments();
  // question.addUIArgument(ROW_COUNT_KEY, nbRowsField.getModelObject() + "");
  // }
  //
  // editedQuestion.setConditions(((Conditions) conditionPanel.getDefaultModelObject()));
  // }

  public void persist(AjaxRequestTarget target) {
    try {
      questionnairePersistenceUtils.persist(questionnaireModel.getObject(), localePropertiesModel.getObject());
    } catch(Exception e) {
      logger.error("Cannot persist questionnaire", e);
      error(e.getMessage());
      feedbackWindow.setContent(feedbackPanel);
      feedbackWindow.show(target);
    }
  }
}
