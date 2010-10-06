/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.form;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;

@SuppressWarnings("serial")
public abstract class AbstractQuestionnaireElementPanel<T extends IQuestionnaireElement> extends AbstractLocalePropertiesPanel<T> {

  protected final ModalWindow modalWindow;

  /**
   * 
   * @param id
   * @param model
   * @param questionnaireModel
   * @param modalWindow
   */
  public AbstractQuestionnaireElementPanel(String id, IModel<T> model, IModel<Questionnaire> questionnaireModel, ModalWindow modalWindow) {
    super(id, model, questionnaireModel);

    this.modalWindow = modalWindow;

    form.add(new AjaxButton("save", form) {

      @SuppressWarnings("unchecked")
      @Override
      public void onSubmit(AjaxRequestTarget target, Form<?> form2) {
        onSave(target, (T) form2.getModelObject());
        AbstractQuestionnaireElementPanel.this.modalWindow.close(target);
      }

      @Override
      protected void onError(AjaxRequestTarget target, Form<?> form2) {
        feedbackWindow.setContent(feedbackPanel);
        feedbackWindow.show(target);
      }
    });

    form.add(new AjaxButton("cancel", form) {

      @Override
      public void onSubmit(AjaxRequestTarget target, Form<?> form2) {
        AbstractQuestionnaireElementPanel.this.modalWindow.close(target);
      }
    }.setDefaultFormProcessing(false));
  }
}
