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

  public AbstractQuestionnaireElementPanelForm(String id, IModel<T> model, ModalWindow modalWindow) {
    super(id, model);

    this.modalWindow = modalWindow;

    feedbackPanel = new FeedbackPanel("content");
    feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);

    add(feedbackWindow);
    add(new FormT("form", model) {

    });
  }

  public class FormT extends Form<T> {

    public FormT(String id, IModel<T> model) {
      super(id, model);
      onInit(this);

      add(new AjaxButton("save", this) {

        @SuppressWarnings("unchecked")
        @Override
        public void onSubmit(AjaxRequestTarget target, Form<?> form) {
          onSave(target, (T) form.getModelObject());
          modalWindow.close(target);
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
   * Use to add new formComponents on form
   * @param form
   */
  public abstract void onInit(Form<T> form);

  /**
   * 
   * @param target
   * @param t
   */
  public void onSave(AjaxRequestTarget target, T t) {

  }
}
