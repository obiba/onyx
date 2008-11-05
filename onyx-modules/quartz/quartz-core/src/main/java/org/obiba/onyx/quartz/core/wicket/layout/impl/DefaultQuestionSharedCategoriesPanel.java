package org.obiba.onyx.quartz.core.wicket.layout.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeaderlessColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.layout.impl.array.AbstractDataListProvider;
import org.obiba.onyx.quartz.core.wicket.layout.impl.array.AbstractQuestionArray;
import org.obiba.onyx.quartz.core.wicket.layout.impl.array.RadioGroupView;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireModel;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultQuestionSharedCategoriesPanel extends Panel {

  private static final long serialVersionUID = 5144933183339704600L;

  private static final Logger log = LoggerFactory.getLogger(DefaultQuestionSharedCategoriesPanel.class);

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  private DefaultOpenAnswerDefinitionPanel currentOpenField;

  private List<RadioGroup> radioGroups = new ArrayList<RadioGroup>();

  @SuppressWarnings("serial")
  public DefaultQuestionSharedCategoriesPanel(String id, IModel questionModel) {
    super(id, questionModel);
    setOutputMarkupId(true);

    Question question = (Question) getModelObject();

    List<IColumn> columns = new ArrayList<IColumn>();
    columns.add(new HeaderlessColumn() {

      public void populateItem(Item cellItem, String componentId, IModel rowModel) {
        Question question = (Question) rowModel.getObject();
        cellItem.add(new Label(componentId, new QuestionnaireStringResourceModel(question, "label")));
        cellItem.add(new AttributeAppender("class", new Model("label"), " "));
      }

    });
    for(final QuestionCategory questionCategory : question.getQuestionCategories()) {
      columns.add(new AbstractColumn(new QuestionnaireStringResourceModel(questionCategory, "label")) {

        public void populateItem(final Item cellItem, String componentId, IModel rowModel) {
          cellItem.setModel(new QuestionnaireModel(questionCategory));
          final Question question = (Question) rowModel.getObject();
          int index = question.getParentQuestion().getQuestions().indexOf(question);
          log.info("question.index={}", index);
          log.info("radioGroups.size={}", radioGroups.size());
          final RadioGroup radioGroup = radioGroups.get(index);

          // TODO check if parent question is multiple
          cellItem.add(new RadioQuestionCategoryPanel(componentId, cellItem.getModel(), false) {

            @Override
            public void onOpenFieldSelection(AjaxRequestTarget target) {
              log.info("question={} questionCategory={} onOpenFieldSelection", question.getName(), questionCategory.getName());
            }

            @Override
            public void onRadioSelection(AjaxRequestTarget target) {
              log.info("question={} questionCategory={} onRadioSelection", question.getName(), questionCategory.getName());
              // make the radio group active for the selection
              radioGroup.setModel(cellItem.getModel());
              radioGroup.setRequired(question.isRequired() ? true : false);
              // exclusive choice, only one answer per question
              activeQuestionnaireAdministrationService.deleteAnswers(question);
              activeQuestionnaireAdministrationService.answer(question, questionCategory, null);
            }

          });

          // previous answer or default selection
          CategoryAnswer previousAnswer = activeQuestionnaireAdministrationService.findAnswer(question, questionCategory);
          if(previousAnswer != null) {
            radioGroup.setModel(cellItem.getModel());
          } else if(questionCategory.isSelected()) {
            radioGroup.setModel(cellItem.getModel());
            activeQuestionnaireAdministrationService.answer(question, questionCategory, null);
          }
        }

        @Override
        public String getCssClass() {
          return "category";
        }

      });
    }

    IDataProvider questionsProvider = new AbstractDataListProvider<Question>() {
      @Override
      public List<Question> getDataList() {
        return ((Question) DefaultQuestionSharedCategoriesPanel.this.getModelObject()).getQuestions();
      }
    };
    add(new AbstractQuestionArray("array", getModel(), columns, questionsProvider) {

      @Override
      public Component getRowsContent(String id, List<IColumn> columns, IDataProvider rows) {
        return new RadioGroupRows(id, columns, rows);
      }

    });

  }

  private class RadioGroupRows extends Fragment {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings( { "serial", "unchecked" })
    public RadioGroupRows(String id, List<IColumn> columns, IDataProvider rows) {
      super(id, "radioRows", DefaultQuestionSharedCategoriesPanel.this);
      add(new RadioGroupView(id, (List) columns, rows) {

        @Override
        protected RadioGroup newRadioGroup(String id, final int index) {
          RadioGroup group = super.newRadioGroup(id, index);
          radioGroups.add(group);
          // add ajax call back on group
          group.add(new AjaxFormChoiceComponentUpdatingBehavior() {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
              log.info("radioGroup[{}] onUpdate", index);
            }

          });
          return group;
        }
      });
    }

  }
}
