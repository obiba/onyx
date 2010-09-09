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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.validator.StringValidator;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;

@SuppressWarnings("serial")
public class CategoryPropertiesPanel extends Panel {

  private FeedbackPanel feedbackPanel;

  private FeedbackWindow feedbackWindow;

  private final ModalWindow modalWindow;

  public CategoryPropertiesPanel(String id, Model<Category> model, ModalWindow modalWindow) {
    super(id, model);
    this.modalWindow = modalWindow;

    feedbackPanel = new FeedbackPanel("content");
    feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);

    add(feedbackWindow);
    add(new CategoryForm("categoryForm", model));
  }

  public class CategoryForm extends Form<Category> {

    public CategoryForm(String id, Model<Category> model) {
      super(id, model);

      TextField<String> name = new TextField<String>("name", new PropertyModel<String>(getModel(), "name"));
      name.add(new RequiredFormFieldBehavior());
      name.add(new StringValidator.MaximumLengthValidator(20));
      add(name);
      add(new CheckBox("escape", new PropertyModel<Boolean>(getModel(), "escape")));
      add(new CheckBox("noAnswer", new PropertyModel<Boolean>(getModel(), "noAnswer")));

      add(new AjaxButton("save", this) {

        @Override
        public void onSubmit(AjaxRequestTarget target, Form<?> form) {
          modalWindow.close(target);
          onSave(target, (Category) form.getModelObject());
        }

        @Override
        protected void onError(AjaxRequestTarget target, Form<?> form) {
          feedbackWindow.setContent(feedbackPanel);
          feedbackWindow.show(target);
        }
      });

      add(new AjaxButton("cancel", this) {

        @Override
        public void onSubmit(AjaxRequestTarget target, Form<?> form) {
          modalWindow.close(target);
        }
      }.setDefaultFormProcessing(false));
    }

  }

  /**
   * 
   * @param target
   * @param category
   */
  public void onSave(AjaxRequestTarget target, Category category) {

  }

}
