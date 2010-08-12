/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.question;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.validator.StringValidator;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;

public class QuestionPropertiesPanel extends Panel {

  private static final long serialVersionUID = 1L;

  private final ModalWindow modalWindow;

  private final FeedbackWindow feedbackWindow;

  private final FeedbackPanel feedbackPanel;

  public QuestionPropertiesPanel(String id, IModel<Question> model, final ModalWindow modalWindow) {
    super(id, model);

    this.modalWindow = modalWindow;

    feedbackPanel = new FeedbackPanel("content");
    feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);
    add(feedbackWindow);

    add(new QuestionForm("questionForm", model));
  }

  private class QuestionForm extends Form<Question> {

    private static final long serialVersionUID = 1L;

    public QuestionForm(String id, IModel<Question> model) {
      super(id, model);

      TextField<String> name = new TextField<String>("name", new PropertyModel<String>(getModel(), "name"));
      name.add(new RequiredFormFieldBehavior());
      name.add(new StringValidator.MaximumLengthValidator(20));
      add(name);

      TextField<String> variableName = new TextField<String>("variableName", new PropertyModel<String>(getModel(), "variableName"));
      variableName.add(new StringValidator.MaximumLengthValidator(20));
      add(variableName);

      add(new CheckBox("multiple", new PropertyModel<Boolean>(getModel(), "multiple")));

      add(new AjaxButton("save", this) {

        private static final long serialVersionUID = 1L;

        @Override
        public void onSubmit(AjaxRequestTarget target, Form<?> form) {
          super.onSubmit();
          // Question question = QuestionForm.this.getModelObject();
          // TODO process this question
          modalWindow.close(target);
        }

        @Override
        protected void onError(AjaxRequestTarget target, Form<?> form) {
          feedbackWindow.setContent(feedbackPanel);
          feedbackWindow.show(target);
        }
      });

      // A cancel button: use an AjaxButton and disable the "defaultFormProcessing". Seems that this is the best
      // practice for this type of behavior
      add(new AjaxButton("cancel", this) {

        private static final long serialVersionUID = 1L;

        @Override
        public void onSubmit(AjaxRequestTarget target, Form<?> form) {
          modalWindow.close(target);
        }
      }.setDefaultFormProcessing(false));
    }

  }

}
