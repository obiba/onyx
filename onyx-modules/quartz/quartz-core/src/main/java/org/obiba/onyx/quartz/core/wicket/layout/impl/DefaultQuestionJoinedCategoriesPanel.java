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
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.DataGridView;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeaderlessColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.layout.impl.array.AbstractQuestionArray;
import org.obiba.onyx.quartz.core.wicket.layout.impl.array.QuestionCategoriesColumn;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.AbstractDataListProvider;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireModel;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultQuestionJoinedCategoriesPanel extends Panel {

  private static final long serialVersionUID = 5144933183339704600L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(DefaultQuestionJoinedCategoriesPanel.class);

  private AbstractQuestionArray array;

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  @SuppressWarnings("serial")
  public DefaultQuestionJoinedCategoriesPanel(String id, IModel questionModel) {
    super(id, questionModel);
    setOutputMarkupId(true);

    // provider of question's categories
    IDataProvider questionsProvider = new AbstractDataListProvider<QuestionCategory>() {
      @Override
      public List<QuestionCategory> getDataList() {
        return getQuestion().getQuestionCategories();
      }

      @Override
      public IModel model(Object object) {
        return new QuestionnaireModel((QuestionCategory) object);
      }
    };

    List<IColumn> columns = new ArrayList<IColumn>();

    // first column: labels of question's categories
    columns.add(new HeaderlessColumn() {

      public void populateItem(Item cellItem, String componentId, IModel rowModel) {
        QuestionCategory parentQuestionCategory = (QuestionCategory) rowModel.getObject();
        cellItem.add(new Label(componentId, new QuestionnaireStringResourceModel(parentQuestionCategory, "label")));
        cellItem.add(new AttributeAppender("class", new Model("label"), " "));
      }

    });

    // following columns: the question's children
    Question parentQuestion = (Question) getModelObject();
    for(Question question : parentQuestion.getQuestions()) {
      if(question.isToBeAnswered(activeQuestionnaireAdministrationService)) {
        columns.add(new QuestionCategoriesColumn(new QuestionnaireModel(question)));
      }
    }

    add(array = new AbstractQuestionArray("array", getModel(), columns, questionsProvider) {

      @Override
      public Component getRowsContent(String id, List<IColumn> columns, IDataProvider rows) {
        return new DataGridView(id, columns, rows);
      }

    });

    array.setOutputMarkupId(true);
  }

  /**
   * Get the parent question.
   * @return
   */
  public Question getQuestion() {
    return (Question) getModelObject();
  }

}
