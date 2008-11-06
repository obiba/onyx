package org.obiba.onyx.quartz.core.wicket.layout.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
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

  private DefaultOpenAnswerDefinitionPanel[] currentOpenFields;

  private RadioGroupView radioGroupView;

  @SuppressWarnings("serial")
  public DefaultQuestionSharedCategoriesPanel(String id, IModel questionModel) {
    super(id, questionModel);
    setOutputMarkupId(true);

    Question question = (Question) getModelObject();

    List<IColumn> columns = new ArrayList<IColumn>();
    // first column: labels of question's children
    columns.add(new HeaderlessColumn() {

      public void populateItem(Item cellItem, String componentId, IModel rowModel) {
        Question question = (Question) rowModel.getObject();
        cellItem.add(new Label(componentId, new QuestionnaireStringResourceModel(question, "label")));
        cellItem.add(new AttributeAppender("class", new Model("label"), " "));
      }

    });
    // following columns: the question's categories
    for(final QuestionCategory questionCategory : question.getQuestionCategories()) {
      columns.add(new AbstractColumn(new QuestionnaireStringResourceModel(questionCategory, "label")) {

        public void populateItem(final Item cellItem, String componentId, IModel rowModel) {
          cellItem.setModel(new QuestionnaireModel(questionCategory));
          Question question = (Question) rowModel.getObject();
          final int index = question.getParentQuestion().getQuestions().indexOf(question);
          log.debug("question.index={}", index);
          log.debug("radioGroupView.radioGroups.length={}", radioGroupView.getRadioGroups().length);

          final RadioGroup radioGroup = radioGroupView.getRadioGroup(index);
          radioGroup.setRequired(question.isRequired() ? true : false);
          radioGroup.setLabel(new QuestionnaireStringResourceModel(question, "label"));

          // TODO check if parent question is multiple
          RadioQuestionCategoryPanel radio;
          cellItem.add(radio = new RadioQuestionCategoryPanel(componentId, rowModel, cellItem.getModel(), false) {

            @Override
            public void onOpenFieldSelection(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
              // ignore if multiple click in the same open field
              if(this.getOpenField().equals(currentOpenFields[index])) return;

              Question question = (Question) questionModel.getObject();
              log.info("question={} questionCategory={} onOpenFieldSelection", question.getName(), questionCategory.getName());

              // make sure a previously selected open field in the same row is not asked for
              if(currentOpenFields[index] != null) {
                currentOpenFields[index].setRequired(false);
              }
              // make the open field active
              currentOpenFields[index] = this.getOpenField();
              currentOpenFields[index].setRequired(question.isRequired() ? true : false);
              // make sure radio selection does not conflict with open field selection
              radioGroup.setModel(new Model());
              radioGroup.setRequired(false);
              // update all
              target.addComponent(DefaultQuestionSharedCategoriesPanel.this.get("array"));
              // persist selection
              super.onOpenFieldSelection(target, questionModel, questionCategoryModel);
            }

            @Override
            public void onRadioSelection(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
              Question question = (Question) questionModel.getObject();
              // make the radio group active for the selection
              radioGroup.setModel(questionCategoryModel);
              radioGroup.setRequired(question.isRequired() ? true : false);
              target.addComponent(DefaultQuestionSharedCategoriesPanel.this.get("array"));
              // persist selection
              super.onRadioSelection(target, questionModel, questionCategoryModel);
            }

          });

          // previous answer or default selection
          CategoryAnswer previousAnswer = activeQuestionnaireAdministrationService.findAnswer(question, questionCategory);
          if(radio.getOpenField() != null) {
            if(previousAnswer != null) {
              radio.getOpenField().setRequired(question.isRequired() ? true : false);
              radioGroup.setRequired(false);
              currentOpenFields[index] = radio.getOpenField();
            } else if(questionCategory.isSelected()) {
              radio.getOpenField().setRequired(question.isRequired() ? true : false);
              activeQuestionnaireAdministrationService.answer(questionCategory, null);
            } else {
              // make sure it is not asked for as it is not selected at creation time
              radio.getOpenField().setRequired(false);
            }
          } else {
            if(previousAnswer != null) {
              radioGroup.setModel(cellItem.getModel());
            } else if(questionCategory.isSelected()) {
              radioGroup.setModel(cellItem.getModel());
              activeQuestionnaireAdministrationService.answer(question, questionCategory, null);
            }
          }
        }

        @Override
        public String getCssClass() {
          return "category";
        }

      });
    }

    // provider of question's children
    IDataProvider questionsProvider = new AbstractDataListProvider<Question>() {
      @Override
      public List<Question> getDataList() {
        return ((Question) DefaultQuestionSharedCategoriesPanel.this.getModelObject()).getQuestions();
      }

      @Override
      public IModel model(Object object) {
        return new QuestionnaireModel((Question) object);
      }
    };

    currentOpenFields = new DefaultOpenAnswerDefinitionPanel[questionsProvider.size()];

    // the question array
    add(new AbstractQuestionArray("array", getModel(), columns, questionsProvider) {

      @Override
      public Component getRowsContent(String id, List<IColumn> columns, IDataProvider rows) {
        return new RadioGroupRows(id, columns, rows);
      }

    }.setOutputMarkupId(true));

  }

  private class RadioGroupRows extends Fragment {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings( { "serial", "unchecked" })
    public RadioGroupRows(String id, List<IColumn> columns, IDataProvider rows) {
      super(id, "radioRows", DefaultQuestionSharedCategoriesPanel.this);
      add(radioGroupView = new RadioGroupView(id, (List) columns, rows));
    }

  }
}
