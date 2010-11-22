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
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.wicket.layout.IQuestionCategorySelectionListener;
import org.obiba.onyx.quartz.core.wicket.layout.impl.array.AbstractQuestionArray;
import org.obiba.onyx.quartz.core.wicket.layout.impl.array.CheckGroupView;
import org.obiba.onyx.quartz.core.wicket.layout.impl.array.QuestionCategoryCheckBoxColumn;
import org.obiba.onyx.quartz.core.wicket.layout.impl.array.QuestionCategoryRadioColumn;
import org.obiba.onyx.quartz.core.wicket.layout.impl.array.RadioGroupView;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.QuestionCategoriesProvider;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shared categories question array UI. Children questions are presented in rows, parent question categories in columns.
 */
public class DefaultQuestionSharedCategoriesPanel extends Panel implements IQuestionCategorySelectionListener {

  private static final long serialVersionUID = 5144933183339704600L;

  private static final Logger log = LoggerFactory.getLogger(DefaultQuestionSharedCategoriesPanel.class);

  private RadioGroupView radioGroupView;

  private CheckGroupView checkGroupView;

  private AbstractQuestionArray array;

  /**
   * Constructor, given the question array (holding the categories, and the children question).
   * @param id
   * @param questionModel
   */
  @SuppressWarnings("serial")
  public DefaultQuestionSharedCategoriesPanel(String id, IModel<Question> questionModel, IDataProvider<Question> questionsProvider) {
    super(id, questionModel);
    setOutputMarkupId(true);
    List<IColumn> columns = new ArrayList<IColumn>();

    // first column: labels of question's children
    columns.add(new HeaderlessColumn() {

      public void populateItem(Item cellItem, String componentId, IModel rowModel) {
        Question question = (Question) rowModel.getObject();
        cellItem.add(new Label(componentId, new QuestionnaireStringResourceModel(question, "label")).setEscapeModelStrings(false));
        cellItem.add(new AttributeAppender("class", new Model("label"), " "));
      }

    });

    // following columns: the question's categories
    Question parentQuestion = (Question) getDefaultModelObject();
    QuestionCategoriesProvider provider = new QuestionCategoriesProvider(getDefaultModel());
    if(!parentQuestion.isMultiple()) {
      for(IModel questionCategoryModel : provider.getDataList()) {
        columns.add(new QuestionCategoryRadioColumn(questionCategoryModel, new PropertyModel(this, "radioGroupView.groups")));
      }

      add(array = new AbstractQuestionArray("array", getDefaultModel(), columns, questionsProvider) {

        @Override
        public Component getRowsContent(String id, @SuppressWarnings("hiding") List<IColumn> columns, IDataProvider rows) {
          return new RadioGroupRows(id, columns, rows);
        }

      });
    } else {
      for(IModel questionCategoryModel : provider.getDataList()) {
        QuestionCategory questionCategory = (QuestionCategory) questionCategoryModel.getObject();
        if(!questionCategory.isEscape()) {
          columns.add(new QuestionCategoryCheckBoxColumn(questionCategoryModel, new PropertyModel(this, "checkGroupView.groups")));
        } else {
          log.error("Escape categories in arrays with multiple choices is not supported: {}.", questionCategory.getName());
        }
      }

      add(array = new AbstractQuestionArray("array", getDefaultModel(), columns, questionsProvider) {

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

    @SuppressWarnings({ "serial", "unchecked" })
    public RadioGroupRows(String id, List<IColumn> columns, IDataProvider rows) {
      super(id, "groupRows", DefaultQuestionSharedCategoriesPanel.this);
      add(radioGroupView = new RadioGroupView(id, (List) columns, rows));
      radioGroupView.setItemReuseStrategy(ReuseIfModelsEqualStrategy.getInstance());
    }

  }

  private class CheckGroupRows extends Fragment {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings({ "serial", "unchecked" })
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

  public void onQuestionCategorySelection(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel, boolean isSelected) {
    target.addComponent(array);

    IQuestionCategorySelectionListener parentListener = findParent(IQuestionCategorySelectionListener.class);
    if(parentListener != null) {
      parentListener.onQuestionCategorySelection(target, questionModel, questionCategoryModel, isSelected);
    }
  }

}
