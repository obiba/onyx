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

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeaderlessColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.ReuseIfModelsEqualStrategy;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.layout.impl.array.AbstractDataListProvider;
import org.obiba.onyx.quartz.core.wicket.layout.impl.array.AbstractQuestionArray;
import org.obiba.onyx.quartz.core.wicket.layout.impl.array.CheckGroupView;
import org.obiba.onyx.quartz.core.wicket.layout.impl.array.QuestionCategoryCheckBoxColumn;
import org.obiba.onyx.quartz.core.wicket.layout.impl.array.QuestionCategoryRadioColumn;
import org.obiba.onyx.quartz.core.wicket.layout.impl.array.RadioGroupView;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireModel;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultQuestionSharedCategoriesPanel extends Panel {

  private static final long serialVersionUID = 5144933183339704600L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(DefaultQuestionSharedCategoriesPanel.class);

  private RadioGroupView radioGroupView;

  private CheckGroupView checkGroupView;

  private AbstractQuestionArray array;

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  @SuppressWarnings("serial")
  public DefaultQuestionSharedCategoriesPanel(String id, IModel questionModel) {
    super(id, questionModel);
    setOutputMarkupId(true);

    // provider of question's children
    IDataProvider questionsProvider = new AbstractDataListProvider<Question>() {
      @Override
      public List<Question> getDataList() {
        List<Question> questionToAnswer = new ArrayList<Question>();
        for(Question question : ((Question) DefaultQuestionSharedCategoriesPanel.this.getModelObject()).getQuestions()) {
          if(question.isToBeAnswered(activeQuestionnaireAdministrationService)) questionToAnswer.add(question);
        }
        return questionToAnswer;
      }

      @Override
      public IModel model(Object object) {
        return new QuestionnaireModel((Question) object);
      }
    };

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
    Question parentQuestion = (Question) getModelObject();
    if(!parentQuestion.isMultiple()) {
      for(QuestionCategory questionCategory : parentQuestion.getQuestionCategories()) {
        columns.add(new QuestionCategoryRadioColumn(new QuestionnaireModel(questionCategory), new PropertyModel(this, "radioGroupView.groups")) {
          @Override
          public void onEvent(AjaxRequestTarget target) {
            log.info("radioColumn.onEvent()");
            target.addComponent(array);
          }
        });
      }

      add(array = new AbstractQuestionArray("array", getModel(), columns, questionsProvider) {

        @Override
        public Component getRowsContent(String id, List<IColumn> columns, IDataProvider rows) {
          return new RadioGroupRows(id, columns, rows);
        }

      });
    } else {
      for(QuestionCategory questionCategory : parentQuestion.getQuestionCategories()) {
        columns.add(new QuestionCategoryCheckBoxColumn(new QuestionnaireModel(questionCategory), new PropertyModel(this, "checkGroupView.groups")) {
          @Override
          public void onEvent(AjaxRequestTarget target) {
            log.info("checkboxColumn.onEvent()");
            target.addComponent(array);
          }
        });
      }

      add(array = new AbstractQuestionArray("array", getModel(), columns, questionsProvider) {

        @Override
        public Component getRowsContent(String id, List<IColumn> columns, IDataProvider rows) {
          return new CheckGroupRows(id, columns, rows);
        }

      });
    }

    array.setOutputMarkupId(true);
  }

  private class RadioGroupRows extends Fragment {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings( { "serial", "unchecked" })
    public RadioGroupRows(String id, List<IColumn> columns, IDataProvider rows) {
      super(id, "groupRows", DefaultQuestionSharedCategoriesPanel.this);
      add(radioGroupView = new RadioGroupView(id, (List) columns, rows));
      radioGroupView.setItemReuseStrategy(ReuseIfModelsEqualStrategy.getInstance());
    }

  }

  private class CheckGroupRows extends Fragment {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings( { "serial", "unchecked" })
    public CheckGroupRows(String id, List<IColumn> columns, IDataProvider rows) {
      super(id, "groupRows", DefaultQuestionSharedCategoriesPanel.this);
      add(checkGroupView = new CheckGroupView(id, (List) columns, rows));
      checkGroupView.setItemReuseStrategy(ReuseIfModelsEqualStrategy.getInstance());
    }

  }

  /**
   * Get the radio group view that gives access to inner {@link RadioGroup} for each row.
   * @return null if question is multiple
   */
  public RadioGroupView getRadioGroupView() {
    return radioGroupView;
  }

  /**
   * Get the check group view that gives access to inner {@link CheckGroup} for each row.
   * @return null if question is not multiple
   */
  public CheckGroupView getCheckGroupView() {
    return checkGroupView;
  }

}
