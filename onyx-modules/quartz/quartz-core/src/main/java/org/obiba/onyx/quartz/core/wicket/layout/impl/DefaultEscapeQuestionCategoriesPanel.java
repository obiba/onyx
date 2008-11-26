/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.GridView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.IDataListFilter;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.QuestionCategoriesToMatrixPermutator;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultEscapeQuestionCategoriesPanel extends Panel {

  private static final long serialVersionUID = 5144933183339704600L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(DefaultEscapeQuestionCategoriesPanel.class);

  private RadioGroup radioGroup;

  @SuppressWarnings("serial")
  public DefaultEscapeQuestionCategoriesPanel(String id, IModel questionModel) {
    super(id, questionModel);
    setOutputMarkupId(true);

    Question question = (Question) getModelObject();

    radioGroup = new RadioGroup("categories", new Model());
    radioGroup.setLabel(new QuestionnaireStringResourceModel(question, "label"));
    add(radioGroup);

    GridView repeater = new AbstractQuestionCategoriesView("category", getModel(), new IDataListFilter<QuestionCategory>() {

      public boolean accept(QuestionCategory questionCategory) {
        return questionCategory.getCategory().isEscape();
      }

    }, new QuestionCategoriesToMatrixPermutator(getModel())) {

      @Override
      protected void populateItem(Item item) {
        if(item.getModel() == null) {
          item.add(new EmptyPanel("input").setVisible(false));
        } else {
          item.add(new QuestionCategoryRadioPanel("input", item.getModel(), radioGroup) {

            @Override
            public void onOpenFieldSelection(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
              // update all
              target.addComponent(DefaultEscapeQuestionCategoriesPanel.this);
              DefaultEscapeQuestionCategoriesPanel.this.onSelection(target, questionModel, questionCategoryModel);
            }

            @Override
            public void onOpenFieldSubmit(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
              // update all
              target.addComponent(DefaultEscapeQuestionCategoriesPanel.this);
            }

            @Override
            public void onOpenFieldError(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
              // update all
              // target.addComponent(DefaultQuestionCategoriesPanel.this);
            }

            @Override
            public void onSelection(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
              // update all
              target.addComponent(DefaultEscapeQuestionCategoriesPanel.this);
              DefaultEscapeQuestionCategoriesPanel.this.onSelection(target, questionModel, questionCategoryModel);
            }

          });
        }
      }

    };
    radioGroup.add(repeater);
  }

  public void setNoSelection() {
    radioGroup.setModel(new Model());
  }

  public void onSelection(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
  }
}
