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
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;

@SuppressWarnings("serial")
public abstract class AbstractQuestionnaireElementPanelForm<T> extends Panel {

  protected FeedbackPanel feedbackPanel;

  protected FeedbackWindow feedbackWindow;

  protected final ModalWindow modalWindow;

  protected Form<T> form;

  public AbstractQuestionnaireElementPanelForm(String id, IModel<T> model, ModalWindow modalWindow) {
    super(id, model);

    this.modalWindow = modalWindow;

    feedbackPanel = new FeedbackPanel("content");
    feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);

    add(feedbackWindow);
    add(form = new Form<T>("form", model));
    form.add(new AjaxButton("save", form) {

      @SuppressWarnings("unchecked")
      @Override
      public void onSubmit(AjaxRequestTarget target, Form<?> form2) {
        onSave(target, (T) form2.getModelObject());
        AbstractQuestionnaireElementPanelForm.this.modalWindow.close(target);
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
        AbstractQuestionnaireElementPanelForm.this.modalWindow.close(target);
      }
    }.setDefaultFormProcessing(false));
  }

  /**
   * 
   * @param target
   * @param t
   */
  public void onSave(AjaxRequestTarget target, T t) {

  }

  public Form<T> getForm() {
    return form;
  }

}
