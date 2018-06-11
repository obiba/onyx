/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.simplified;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.layout.IQuestionCategorySelectionListener;
import org.obiba.onyx.quartz.core.wicket.layout.IQuestionCategorySelectionStateHolder;
import org.obiba.onyx.quartz.core.wicket.layout.impl.QuestionCategoryComponentsView;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.IDataListFilter;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.IDataListPermutator;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.MultipleDataListFilter;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.QuestionCategoryEscapeFilter;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.QuestionCategoryImageFilter;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.QuestionCategoryListToGridPermutator;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.QuestionCategoryOpenAnswerFilter;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireModel;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Panel containing the question categories in a grid view of image buttons to be selected (multiple selection or not),
 * without open answers.
 */
public class SimplifiedQuestionCategoriesPanel extends Panel implements IQuestionCategorySelectionListener {

  private static final long serialVersionUID = 5144933183339704600L;

  private static final Logger log = LoggerFactory.getLogger(SimplifiedQuestionCategoriesPanel.class);

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  /**
   * Constructor for a stand-alone question.
   * @param id
   * @param questionModel
   */
  public SimplifiedQuestionCategoriesPanel(String id, IModel<Question> questionModel) {
    super(id, questionModel);
    setOutputMarkupId(true);

    // seams like ugly but we need a form component to run the answer count validator
    add(new AnswerValidatorField("hidden", getQuestionModel()));

    addOpenAnswerCategoriesView();
    addRegularCategoriesView();
    addRegularImageCategoriesView();
    addEscapeCategoriesView();
  }

  /**
   * Open answers in one row.
   */
  @SuppressWarnings("serial")
  private void addOpenAnswerCategoriesView() {
    MultipleDataListFilter<QuestionCategory> filter = new MultipleDataListFilter<QuestionCategory>();
    filter.addFilter(new QuestionCategoryEscapeFilter(false));
    filter.addFilter(new QuestionCategoryOpenAnswerFilter(true));
    add(new QuestionCategoryComponentsView("openCategories", getQuestionModel(), filter, new QuestionCategoryListToGridPermutator(getQuestionModel(), 1)) {

      @Override
      protected Component newQuestionCategoryComponent(String id, IModel<QuestionCategory> questionCategoryModel, int index) {
        return new OpenFragment(id, questionCategoryModel, index);
      }

    });
  }

  /**
   * Regular category choice in potentially multiple columns.
   */
  private void addRegularCategoriesView() {
    MultipleDataListFilter<QuestionCategory> filter = new MultipleDataListFilter<QuestionCategory>();
    filter.addFilter(new QuestionCategoryImageFilter(false));
    filter.addFilter(new QuestionCategoryEscapeFilter(false));
    filter.addFilter(new QuestionCategoryOpenAnswerFilter(false));
    QuestionCategoryLinksView view = new QuestionCategoryLinksView("regularCategories", getQuestionModel(), filter, new QuestionCategoryListToGridPermutator(getQuestionModel()));
    add(view);
  }

  /**
   * Regular image categories (i.e., categories represented by images rather than text), in a single row.
   */
  private void addRegularImageCategoriesView() {
    QuestionCategoryImageLinksView view = new QuestionCategoryImageLinksView("regularImageCategories", getQuestionModel(), new QuestionCategoryImageFilter(true), new QuestionCategoryListToGridPermutator(getQuestionModel(), 1));
    add(view);
  }

  /**
   * Escape categories in one row.
   */
  private void addEscapeCategoriesView() {
    QuestionCategoryLinksView view = new QuestionCategoryLinksView("escapeCategories", getQuestionModel(), new QuestionCategoryEscapeFilter(true), new QuestionCategoryListToGridPermutator(getQuestionModel(), 1));
    add(view);
  }

  @SuppressWarnings("unchecked")
  private IModel<Question> getQuestionModel() {
    return (IModel<Question>) getDefaultModel();
  }

  private Question getQuestion() {
    return (Question) getDefaultModelObject();
  }

  @Override
  public void onQuestionCategorySelection(final AjaxRequestTarget target, IModel<Question> questionModel, IModel<QuestionCategory> questionCategoryModel, boolean isSelected) {
    log.debug("onQuestionCategorySelection({}, {}, {})", new Object[] { questionModel, questionCategoryModel, isSelected });
    // optimize by updating only the selection state that have changed
    visitChildren(new Component.IVisitor() {

      public Object component(Component component) {
        if(IQuestionCategorySelectionStateHolder.class.isInstance(component)) {
          IQuestionCategorySelectionStateHolder stateHolder = (IQuestionCategorySelectionStateHolder) component;
          if(stateHolder.wasSelected() != stateHolder.isSelected()) {
            log.debug("{}:{} selection was {}, is {}", new Object[] { stateHolder.getQuestionCategory(), stateHolder.getOpenAnswerDefinition(), stateHolder.wasSelected(), stateHolder.isSelected() });
            target.addComponent(component);
          }
        }
        return null;
      }

    });
  }

  /**
   * Display the category simply as a {@link QuestionCategoryLink}.
   */
  @SuppressWarnings("serial")
  private static class QuestionCategoryLinksView extends QuestionCategoryComponentsView {

    public QuestionCategoryLinksView(String id, IModel<Question> questionModel, IDataListFilter<QuestionCategory> filter, IDataListPermutator<IModel<QuestionCategory>> permutator) {
      super(id, questionModel, filter, permutator);
    }

    @Override
    protected Component newQuestionCategoryComponent(String id, IModel questionCategoryModel, int index) {
      return new QuestionCategoryLink(id, questionCategoryModel, new QuestionnaireStringResourceModel(questionCategoryModel, "label"), new QuestionnaireStringResourceModel(questionCategoryModel, "description"));
    }
  }

  /**
   * Display the category simply as a {@link QuestionCategoryImageLink}.
   */
  @SuppressWarnings("serial")
  private static class QuestionCategoryImageLinksView extends QuestionCategoryComponentsView {

    public QuestionCategoryImageLinksView(String id, IModel<Question> questionModel, IDataListFilter<QuestionCategory> filter, IDataListPermutator<IModel<QuestionCategory>> permutator) {
      super(id, questionModel, filter, permutator);
    }

    @Override
    protected Component newQuestionCategoryComponent(String id, IModel questionCategoryModel, int index) {
      return new QuestionCategoryImageLink(id, questionCategoryModel, new QuestionnaireStringResourceModel(questionCategoryModel, "label"));
    }
  }

  @SuppressWarnings("serial")
  private class OpenFragment extends Fragment {
    public OpenFragment(String id, IModel<QuestionCategory> questionCategoryModel, int index) {
      super(id, "openFragment", SimplifiedQuestionCategoriesPanel.this);

      // do not display the or before the first open answer item or if the question is multiple
      if(getQuestion().isMultiple() || index == 0) {
        add(new EmptyPanel("or").setVisible(index != 0));
      } else {
        add(new Label("or", new QuestionnaireStringResourceModel(activeQuestionnaireAdministrationService.getQuestionnaire(), "or")));
      }

      OpenAnswerDefinition openAnswerDefinition = questionCategoryModel.getObject().getOpenAnswerDefinition();
      if(openAnswerDefinition.getOpenAnswerDefinitions().size() > 0) {
        add(new MultipleSimplifiedOpenAnswerDefinitionPanel("open", getQuestionModel(), questionCategoryModel));
      } else {
        add(new SimplifiedOpenAnswerDefinitionPanel("open", getQuestionModel(), questionCategoryModel, new QuestionnaireModel(openAnswerDefinition)));
      }
    }
  }

}
