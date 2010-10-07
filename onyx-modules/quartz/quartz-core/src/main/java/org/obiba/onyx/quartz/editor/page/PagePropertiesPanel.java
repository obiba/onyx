/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.page;

import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;
import org.obiba.onyx.quartz.editor.form.AbstractQuestionnaireElementPanel;
import org.obiba.onyx.quartz.editor.locale.ui.LocalesPropertiesAjaxTabbedPanel;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;

@SuppressWarnings("serial")
public abstract class PagePropertiesPanel extends AbstractQuestionnaireElementPanel<Page> {

  private final IModel<Section> parentModel;

  public PagePropertiesPanel(String id, IModel<Page> model, IModel<Section> parentModel, IModel<Questionnaire> questionnaireModel, ModalWindow modalWindow) {
    super(id, model, questionnaireModel, modalWindow);
    this.parentModel = parentModel;
    createComponent();
  }

  public void createComponent() {
    TextField<String> name = new TextField<String>("name", new PropertyModel<String>(form.getModel(), "name"), String.class);
    name.add(new RequiredFormFieldBehavior());
    name.add(new PageUnicityValidator());
    form.add(name);
    form.add(new LocalesPropertiesAjaxTabbedPanel("localesPropertiesTabs", form.getModel(), localePropertiesModel));
  }

  private class PageUnicityValidator extends AbstractValidator<String> {

    @Override
    protected void onValidate(IValidatable<String> validatable) {
      for(Page page : parentModel.getObject().getPages()) {
        if(page != form.getModelObject() && page.getName().equalsIgnoreCase(validatable.getValue())) {
          error(validatable, "PageAlreadyExists");
          return;
        }
      }
    }
  }

}