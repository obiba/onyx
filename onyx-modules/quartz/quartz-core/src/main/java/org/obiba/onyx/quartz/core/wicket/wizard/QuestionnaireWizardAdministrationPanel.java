/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.wizard;

import java.util.Locale;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.StringResourceModel;

/**
 * Container of action buttons based on the ones defined by the wizard form.
 */
public class QuestionnaireWizardAdministrationPanel extends Panel {

  private static final long serialVersionUID = 1L;

  public enum Action {
    CANCEL, FINISH_SUBMIT, FINISH_ERROR, INTERRUPT, NAVIGUATE
  }

  private Action actionSelected;

  /**
   * @param id
   */
  public QuestionnaireWizardAdministrationPanel(String id, QuestionnaireWizardForm wizardForm, final ModalWindow adminWindow) {
    super(id);

    AjaxLink link = new AjaxLink("interrupt") {
      private static final long serialVersionUID = 0L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        actionSelected = Action.INTERRUPT;
        adminWindow.close(target);
      }

    };
    link.add(new AttributeModifier("value", true, new StringResourceModel("Interrupt", this, null)));
    add(link);

    link = new AjaxLink("cancelLink") {
      private static final long serialVersionUID = 0L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        actionSelected = Action.CANCEL;
        adminWindow.close(target);
      }

    };
    link.add(new AttributeModifier("value", true, new StringResourceModel("Cancel", this, null)));
    add(link);

    AjaxButton finish = new AjaxButton("finish", wizardForm) {

      private static final long serialVersionUID = 0L;

      @Override
      protected void onSubmit(AjaxRequestTarget target, Form form) {
        actionSelected = Action.FINISH_SUBMIT;
        adminWindow.close(target);
      }

      @Override
      protected void onError(AjaxRequestTarget target, Form form) {
        actionSelected = Action.FINISH_ERROR;
        adminWindow.close(target);
      }

    };
    finish.add(new AttributeModifier("value", true, new StringResourceModel("Finish", this, null)));
    add(finish);
  }

  public Action getActionSelected() {
    return actionSelected;
  }

  public QuestionnaireWizardAdministrationPanel setInterruptState(boolean enabled, boolean visible) {
    get("interrupt").setEnabled(enabled);
    get("interrupt").setVisible(visible);
    return this;
  }

  public QuestionnaireWizardAdministrationPanel setFinishState(boolean enabled, boolean visible) {
    get("finish").setEnabled(enabled);
    get("finish").setVisible(visible);
    return this;
  }

  public QuestionnaireWizardAdministrationPanel setCancelState(boolean enabled, boolean visible) {
    get("cancelLink").setEnabled(enabled);
    get("cancelLink").setVisible(visible);
    return this;
  }

  @Override
  public Locale getLocale() {
    return getPage().getLocale();
  }
}
