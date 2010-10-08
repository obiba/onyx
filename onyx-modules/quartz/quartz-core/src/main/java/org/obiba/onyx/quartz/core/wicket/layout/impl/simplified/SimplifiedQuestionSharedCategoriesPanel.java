/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.simplified;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeaderlessColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.ReuseIfModelsEqualStrategy;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.layout.IQuestionCategorySelectionListener;
import org.obiba.onyx.quartz.core.wicket.layout.IQuestionCategorySelectionStateHolder;
import org.obiba.onyx.quartz.core.wicket.layout.impl.array.AbstractQuestionArray;
import org.obiba.onyx.quartz.core.wicket.layout.impl.array.AbstractQuestionCategoryColumn;
import org.obiba.onyx.quartz.core.wicket.layout.impl.array.RowView;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.QuestionCategoriesProvider;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;
import org.obiba.onyx.wicket.link.AjaxImageLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Panel containing the question categories in a grid view of image buttons to be selected (multiple selection or not),
 * without open answers.
 */
public class SimplifiedQuestionSharedCategoriesPanel extends Panel implements IQuestionCategorySelectionListener {

  private static final long serialVersionUID = 5144933183339704600L;

  private static final Logger log = LoggerFactory.getLogger(SimplifiedQuestionSharedCategoriesPanel.class);

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  /**
   * Constructor for a shared categories question.
   * @param id
   * @param questionModel
   */
  @SuppressWarnings("serial")
  public SimplifiedQuestionSharedCategoriesPanel(String id, IModel<Question> questionModel, IDataProvider<Question> questionsProvider) {
    super(id, questionModel);
    setOutputMarkupId(true);

    // seams like ugly but we need a form component to run the answer count validator
    add(new AnswerValidatorField("hidden", getQuestionModel()));

    List<IColumn> columns = new ArrayList<IColumn>();

    // first column: labels of question's children
    columns.add(new HeaderlessColumn() {

      public void populateItem(Item cellItem, String componentId, IModel rowModel) {
        Question question = (Question) rowModel.getObject();
        cellItem.add(new Label(componentId, new QuestionnaireStringResourceModel(question, "label")).setEscapeModelStrings(false));
        cellItem.add(new AttributeAppender("class", new Model<String>("obiba-label"), " "));
      }

    });

    // following columns: the question's categories
    QuestionCategoriesProvider provider = new QuestionCategoriesProvider(getDefaultModel());
    for(IModel questionCategoryModel : provider.getDataList()) {
      columns.add(new AbstractQuestionCategoryColumn(questionCategoryModel) {

        @Override
        public void populateItem(Item cellItem, String componentId, IModel rowModel, int index) {
          // if category has an open answer, show only the open box
          // show a link for check marking
          QuestionCategory questionCategory = (QuestionCategory) cellItem.getModel().getObject();
          if(questionCategory.getOpenAnswerDefinition() != null) {
            cellItem.add(new QuestionCategoryOpenAnswerDefinitionPanel(componentId, rowModel, cellItem.getModel(), new Model<String>("&nbsp;"), null));
          } else {
            cellItem.add(new QuestionCategoryLink(componentId, rowModel, cellItem.getModel(), new Model<String>("&nbsp;"), null));
          }
        }

      });
    }

    add(new AbstractQuestionArray("array", getDefaultModel(), columns, questionsProvider) {

      @Override
      public Component getRowsContent(String id, List<IColumn> columns, IDataProvider rows) {
        return new QuestionChildRows(id, columns, rows);
      }

    });

    add(new AjaxImageLink("clearAll", new QuestionnaireStringResourceModel(activeQuestionnaireAdministrationService.getQuestionnaire(), "clearAll")) {

      @Override
      public void onClick(AjaxRequestTarget target) {
        for(Question child : getQuestion().getQuestions()) {
          activeQuestionnaireAdministrationService.deleteAnswers(child);
        }
        updateSelections(target);
      }

    });

  }

  private IModel getQuestionModel() {
    return getDefaultModel();
  }

  private Question getQuestion() {
    return (Question) getDefaultModelObject();
  }

  public void onQuestionCategorySelection(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel, boolean isSelected) {
    log.debug("onQuestionCategorySelection({}, {}, {})", new Object[] { questionModel, questionCategoryModel, isSelected });
    updateSelections(target);
  }

  private void updateSelections(final AjaxRequestTarget target) {
    // optimize by updating only the selection state that have changed
    visitChildren(new Component.IVisitor() {

      public Object component(Component component) {
        if(IQuestionCategorySelectionStateHolder.class.isInstance(component)) {
          IQuestionCategorySelectionStateHolder stateHolder = (IQuestionCategorySelectionStateHolder) component;
          if(stateHolder.wasSelected() != stateHolder.isSelected()) {
            log.debug("{}:{} selection was {}, is {}", new Object[] { stateHolder.getQuestion(), stateHolder.getQuestionCategory(), stateHolder.wasSelected(), stateHolder.isSelected() });
            target.addComponent(component);
          }
        }
        return null;
      }

    });
  }

  private class QuestionChildRows extends Fragment {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    public QuestionChildRows(String id, List<IColumn> columns, IDataProvider rows) {
      super(id, "questionChildRows", SimplifiedQuestionSharedCategoriesPanel.this);
      RowView view = new RowView(id, (List) columns, rows);
      add(view);
      view.setItemReuseStrategy(ReuseIfModelsEqualStrategy.getInstance());
    }

  }

}
