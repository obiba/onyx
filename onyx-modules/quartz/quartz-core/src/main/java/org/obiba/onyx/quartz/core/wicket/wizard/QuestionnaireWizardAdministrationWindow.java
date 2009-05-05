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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.markup.html.form.Form;
import org.obiba.onyx.wicket.reusable.Dialog;

/**
 * Container of action buttons based on the ones defined by the wizard form.
 */
public class QuestionnaireWizardAdministrationWindow extends Dialog {

  private static final long serialVersionUID = 1L;

  private AjaxButton finishLink;

  private AjaxLink interruptLink;

  private AjaxLink cancelLink;

  /**
   * @param id
   */
  public QuestionnaireWizardAdministrationWindow(String id) {
    super(id);
    setMinimalHeight(50);
    setInitialHeight(50);
    setInitialWidth(450);

    setOptions(Dialog.Option.CANCEL_OPTION);

    finishLink = new AjaxButton("finish") {

      private static final long serialVersionUID = 1L;

      @Override
      protected void onSubmit(AjaxRequestTarget target, Form form) {
        setStatus(Status.SUCCESS);
        if(getCloseButtonCallback() == null || (getCloseButtonCallback() != null && getCloseButtonCallback().onCloseButtonClicked(target, getStatus()))) QuestionnaireWizardAdministrationWindow.this.close(target);
      }

      @Override
      protected void onError(AjaxRequestTarget target, Form form) {
        setStatus(Status.ERROR);
        if(getCloseButtonCallback() == null || (getCloseButtonCallback() != null && getCloseButtonCallback().onCloseButtonClicked(target, getStatus()))) QuestionnaireWizardAdministrationWindow.this.close(target);
      }
    };
    addSubmitOption("Finish", OptionSide.RIGHT, finishLink, "finish");

    interruptLink = new AjaxLink("interrupt") {

      private static final long serialVersionUID = 1L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        setStatus(Status.OTHER);
        if(getCloseButtonCallback() == null || (getCloseButtonCallback() != null && getCloseButtonCallback().onCloseButtonClicked(target, getStatus()))) QuestionnaireWizardAdministrationWindow.this.close(target);
      }

    };
    addOption("Interrupt", OptionSide.RIGHT, interruptLink, "interrupt");

    cancelLink = new AjaxLink("cancelQuestionnaire") {

      private static final long serialVersionUID = 1L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        setStatus(Status.CLOSED);
        if(getCloseButtonCallback() == null || (getCloseButtonCallback() != null && getCloseButtonCallback().onCloseButtonClicked(target, getStatus()))) QuestionnaireWizardAdministrationWindow.this.close(target);
      }
    };
    addOption("CancelQuestionnaire", OptionSide.RIGHT, cancelLink, "cancelQuestionnaire");

  }

  @Override
  public void show(AjaxRequestTarget target) {
    target.appendJavascript("$('.separator').css('border-top', '0');");
    super.show(target);
  };

  public QuestionnaireWizardAdministrationWindow setInterruptState(boolean enabled, boolean visible, IBehavior buttonDisableBehavior) {
    interruptLink.add(buttonDisableBehavior);
    interruptLink.setEnabled(enabled);
    interruptLink.setVisible(visible);
    return this;
  }

  public QuestionnaireWizardAdministrationWindow setFinishState(boolean enabled, boolean visible, IBehavior buttonDisableBehavior) {
    finishLink.add(buttonDisableBehavior);
    finishLink.setEnabled(enabled);
    finishLink.setVisible(visible);
    return this;
  }

  public QuestionnaireWizardAdministrationWindow setCancelState(boolean enabled, boolean visible, IBehavior buttonDisableBehavior) {
    cancelLink.add(buttonDisableBehavior);
    cancelLink.setEnabled(enabled);
    cancelLink.setVisible(visible);
    return this;
  }

  @Override
  public Locale getLocale() {
    return getPage().getLocale();
  }
}
