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

import static org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionType.ARRAY_CHECKBOX;
import static org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionType.LIST_CHECKBOX;
import static org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionType.LIST_DROP_DOWN;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.extensions.markup.html.tabs.PanelCachingTab;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionType;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.wicket.layout.impl.simplified.SimplifiedQuestionPanelFactory;
import org.obiba.onyx.quartz.core.wicket.layout.impl.standard.DefaultQuestionPanelFactory;
import org.obiba.onyx.quartz.core.wicket.layout.impl.standard.DropDownQuestionPanelFactory;
import org.obiba.onyx.quartz.editor.category.CategoriesPanel;
import org.obiba.onyx.quartz.editor.category.CategoryListPanel;
import org.obiba.onyx.quartz.editor.locale.LocaleProperties;
import org.obiba.onyx.quartz.editor.locale.LocalePropertiesUtils;
import org.obiba.onyx.quartz.editor.openAnswer.OpenAnswerPanel;
import org.obiba.onyx.quartz.editor.question.array.ArrayRowsPanel;
import org.obiba.onyx.quartz.editor.question.condition.ConditionPanel;
import org.obiba.onyx.quartz.editor.questionnaire.utils.QuestionnairePersistenceUtils;
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

  private final SavableHidableTab openAnswerTab;

  private final HidableTab categoriesTab;

  private final HidableTab rowsTab;

  private final HidableTab columnsTab;

  public EditQuestionPanel(String id, final IModel<Question> questionModel, final IModel<Questionnaire> questionnaireModel) {
    super(id);
    this.questionnaireModel = questionnaireModel;

    final Question question = questionModel.getObject();

    // TODO maybe merge STANDARD_UI with *QuestionPanelFactory class
    if(StringUtils.isBlank(question.getUIFactoryName())) {
      if(Questionnaire.SIMPLIFIED_UI.equals(questionnaireModel.getObject().getUiType())) {
        question.setUIFactoryName(new SimplifiedQuestionPanelFactory().getBeanName());
      } else {
        question.setUIFactoryName(new DefaultQuestionPanelFactory().getBeanName());
      }
    }

    EditedQuestion editedQuestion = null;
    if(StringUtils.isBlank(question.getName())) {
      editedQuestion = new EditedQuestion(null);
      editedQuestion.setElement(question);
    } else {
      editedQuestion = new EditedQuestion(question);
    }

    final IModel<EditedQuestion> model = new Model<EditedQuestion>(editedQuestion);

    setDefaultModel(model);
    List<IQuestionnaireElement> listElement = new ArrayList<IQuestionnaireElement>();
    listElement.add(question);
    listElement.addAll(question.getQuestionCategories());
    localePropertiesModel = new Model<LocaleProperties>(localePropertiesUtils.load(questionnaireModel.getObject(), listElement.toArray(new IQuestionnaireElement[listElement.size()])));

    feedbackPanel = new FeedbackPanel("content");
    feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);

    add(feedbackWindow);

    add(form = new Form<EditedQuestion>("form", model));

    final List<ITab> tabs = new ArrayList<ITab>();

    openAnswerTab = new SavableHidableTab(new ResourceModel("OpenAnswer")) {

      private OpenAnswerPanel panel;

      @Override
      public Panel getPanel(String panelId) {
        final OpenAnswerDefinition openAnswerDefinition;
        if(panel == null) {
          final List<Category> categories = question.getCategories();
          if(categories.isEmpty()) {
            openAnswerDefinition = new OpenAnswerDefinition();
          } else {
            openAnswerDefinition = categories.get(0).getOpenAnswerDefinition();
          }
          panel = new OpenAnswerPanel(panelId, new Model<OpenAnswerDefinition>(openAnswerDefinition), questionModel, questionnaireModel, localePropertiesModel, feedbackPanel, feedbackWindow) {
            @Override
            public void onSave(AjaxRequestTarget target) {
              if(categories.isEmpty()) {
                Category category = new Category(openAnswerDefinition.getName());
                category.setOpenAnswerDefinition(openAnswerDefinition);
                QuestionCategory questionCategory = new QuestionCategory();
                questionCategory.setCategory(category);
                question.addQuestionCategory(questionCategory);
              }
            }
          };
        }
        return panel;
      }

      @Override
      public void save(AjaxRequestTarget target) {
        if(panel != null) panel.onSave(target);
      }
    };
    openAnswerTab.setVisible(false);

    categoriesTab = new HidableTab(new ResourceModel("Categories")) {
      private CategoriesPanel panel;

      @Override
      public Panel getPanel(String panelId) {
        if(panel == null) {
          panel = new CategoriesPanel(panelId, model, questionnaireModel, localePropertiesModel, feedbackPanel, feedbackWindow);
        }
        return panel;
      }

    };
    categoriesTab.setVisible(false);

    rowsTab = new HidableTab(new ResourceModel("Rows(questions)")) {
      private ArrayRowsPanel panel;

      @Override
      public Panel getPanel(String panelId) {
        if(panel == null) {
          panel = new ArrayRowsPanel(panelId, model, questionnaireModel, localePropertiesModel, feedbackPanel, feedbackWindow);
        }
        return panel;
      }
    };
    rowsTab.setVisible(false);

    columnsTab = new HidableTab(new ResourceModel("Columns(categories)")) {
      private CategoryListPanel panel;

      @Override
      public Panel getPanel(String panelId) {
        if(panel == null) {
          panel = new CategoryListPanel(panelId, model, questionnaireModel, localePropertiesModel, feedbackPanel, feedbackWindow);
        }
        return panel;
      }
    };
    columnsTab.setVisible(false);

    ITab questionTab = new PanelCachingTab(new AbstractTab(new ResourceModel("Question")) {
      @Override
      public Panel getPanel(String panelId) {
        return new QuestionPanel(panelId, model, questionnaireModel, localePropertiesModel, feedbackPanel, feedbackWindow, true) {
          @Override
          public void onQuestionTypeChange(AjaxRequestTarget target, QuestionType questionType) {
            setTabsVisibility(questionType);
            if(tabbedPanel != null && target != null) {
              target.addComponent(tabbedPanel);
            }
          }
        };
      }
    });

    tabs.add(questionTab);
    tabs.add(openAnswerTab);
    tabs.add(categoriesTab);
    tabs.add(rowsTab);
    tabs.add(columnsTab);
    final SavableHidableTab conditionTab = new SavableHidableTab(new ResourceModel("Conditions")) {
      private ConditionPanel panel;

      @Override
      public Panel getPanel(String panelId) {
        if(panel == null) {
          panel = new ConditionPanel(panelId, questionModel, questionnaireModel);
        }
        return panel;
      }

      @Override
      public void save(AjaxRequestTarget target) {
        if(panel != null) panel.onSave(target);
      }

      @Override
      public boolean isVisible() {
        return true;
      }
    };
    tabs.add(conditionTab);
    // tabs.add(new AbstractTab(new ResourceModel("Preview")) {
    // @Override
    // public Panel getPanel(String panelId) {
    // return new QuestionPreviewPanel(panelId, model, questionnaireModel);
    // }
    // });

    setTabsVisibility(editedQuestion.getQuestionType());

    form.add(tabbedPanel = new AjaxSubmitTabbedPanel("tabs", feedbackPanel, feedbackWindow, tabs));

    form.add(new AjaxButton("save", form) {
      @Override
      public void onSubmit(AjaxRequestTarget target, Form<?> form2) {
        QuestionType questionType = form.getModelObject().getQuestionType();
        if(questionType != null) {
          int nbCategories = question.getCategories().size();
          switch(questionType) {
          case SINGLE_OPEN_ANSWER:
            openAnswerTab.save(target);
            if(nbCategories == 0 || question.getCategories().get(0).getOpenAnswerDefinition() == null) {
              tabbedPanel.setSelectedTab(tabs.indexOf(openAnswerTab));
              target.addComponent(tabbedPanel);
              form.error(new StringResourceModel("Validator.SingleOpenAnswerNotDefined", EditQuestionPanel.this, null).getObject());
            }
            break;

          case LIST_CHECKBOX:
          case LIST_RADIO:
          case LIST_DROP_DOWN:
            question.setUIFactoryName(questionType == LIST_DROP_DOWN ? new DropDownQuestionPanelFactory().getBeanName() : new DefaultQuestionPanelFactory().getBeanName());
            question.setMultiple(questionType == LIST_CHECKBOX);
            if(nbCategories < 2) {
              tabbedPanel.setSelectedTab(tabs.indexOf(categoriesTab));
              target.addComponent(tabbedPanel);
              form.error(new StringResourceModel("Validator.ListNotDefined", EditQuestionPanel.this, null).getObject());
            }
            break;

          case ARRAY_CHECKBOX:
          case ARRAY_RADIO:
            question.setMultiple(questionType == ARRAY_CHECKBOX);
            if(question.getQuestions().size() < 2 || nbCategories < 1) {
              form.error(new StringResourceModel("Validator.ArrayNotDefined", EditQuestionPanel.this, null).getObject());
              tabbedPanel.setSelectedTab(tabs.indexOf(question.getQuestions().size() < 2 ? rowsTab : columnsTab));
              target.addComponent(tabbedPanel);
            }
            break;

          case BOILER_PLATE:
            break;
          }
        }

        conditionTab.save(target);

        if(!form.hasError()) {
          onSave(target, form.getModelObject().getElement());
        }
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
        onCancel(target);
      }
    }.setDefaultFormProcessing(false));

  }

  public abstract void onSave(AjaxRequestTarget target, Question question);

  public abstract void onCancel(AjaxRequestTarget target);

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
