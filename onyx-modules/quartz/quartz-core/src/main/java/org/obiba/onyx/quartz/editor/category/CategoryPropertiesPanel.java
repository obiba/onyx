/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.category;

import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.editor.form.AbstractQuestionnaireElementPanel;
import org.obiba.onyx.quartz.editor.locale.ui.LocalesPropertiesAjaxTabbedPanel;
import org.obiba.onyx.quartz.editor.questionCategory.QuestionCategoryPropertiesPanel;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;

@SuppressWarnings("serial")
public class CategoryPropertiesPanel extends AbstractQuestionnaireElementPanel<Category> {

  private VariableNamesPanel variableNamesPanel;

  private IModel<QuestionCategory> questionCategoryModel;

  private QuestionCategoryPropertiesPanel questionCategoryPropertiesPanel;

  public CategoryPropertiesPanel(String id, IModel<QuestionCategory> questionCategoryModel, IModel<Questionnaire> questionnaireModel, ModalWindow modalWindow) {
    super(id, new Model<Category>(questionCategoryModel.getObject().getCategory()), questionnaireModel, modalWindow);
    this.questionCategoryModel = questionCategoryModel;
    createComponent();
  }

  public void createComponent() {
    TextField<String> name = new TextField<String>("name", new PropertyModel<String>(form.getModel(), "name"));
    name.add(new RequiredFormFieldBehavior());
    form.add(name);

    Category modelObject = form.getModelObject();
    form.add(new LocalesPropertiesAjaxTabbedPanel("localesPropertiesTabs", form.getModel(), localePropertiesModel));
    questionCategoryPropertiesPanel = new QuestionCategoryPropertiesPanel("questionCategoryPropertiesPanel", questionCategoryModel, questionnaireModel);
    form.add(questionCategoryPropertiesPanel);

    form.add(new CheckBox("escape", new PropertyModel<Boolean>(form.getModel(), "escape")));
    form.add(new CheckBox("noAnswer", new PropertyModel<Boolean>(form.getModel(), "noAnswer")));
    form.add(variableNamesPanel = new VariableNamesPanel("variableNamesPanel", modelObject.getVariableNames()));
  }

  @Override
  public void onSave(AjaxRequestTarget target, Category category) {
    for(Map.Entry<String, String> entries : variableNamesPanel.getNewMapData().entrySet()) {
      category.addVariableName(entries.getKey(), entries.getValue());
    }
  }

  @Override
  public void persist(AjaxRequestTarget target) {
    super.persist(target);
    questionCategoryPropertiesPanel.persist(target);
  }

  public QuestionCategory getQuestionCategory() {
    return questionCategoryModel.getObject();
  }
}
