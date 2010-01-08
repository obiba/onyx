/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.wicket.reusable;

import java.util.Locale;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.IBehavior;

/**
 * 
 */
public class WizardAdministrationWindow extends Dialog {

  private static final long serialVersionUID = -8541405625420712641L;

  private AjaxLink interruptLink;

  private AjaxLink cancelLink;

  private String cancelLabel = "CancelStage";

  /**
   * @param id
   */
  public WizardAdministrationWindow(String id) {
    super(id);
    setMinimalHeight(50);
    setInitialHeight(50);
    setInitialWidth(450);
    setTitle("Administration");

    setOptions(Dialog.Option.CANCEL_OPTION);

    interruptLink = new AjaxLink("interrupt") {

      private static final long serialVersionUID = 1L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        setStatus(Status.OTHER);
        if(getCloseButtonCallback() == null || getCloseButtonCallback().onCloseButtonClicked(target, getStatus())) {
          WizardAdministrationWindow.this.close(target);
        }
      }

    };

    cancelLink = new AjaxLink("cancelStage") {

      private static final long serialVersionUID = 1L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        setStatus(Status.CLOSED);
        if(getCloseButtonCallback() == null || getCloseButtonCallback().onCloseButtonClicked(target, getStatus())) {
          WizardAdministrationWindow.this.close(target);
        }
      }
    };
  }

  public void setCancelLink(String label) {
    setCancelLink(label, null);
  }

  public void setCancelLink(String label, AjaxLink cancelLink) {
    if(cancelLink != null) this.cancelLink = cancelLink;
    this.cancelLabel = label;
  }

  public void setOptionsToShow() {
    addOption("Interrupt", OptionSide.RIGHT, interruptLink, "interrupt");
    addOption(cancelLabel, OptionSide.RIGHT, cancelLink, "cancelStage");
  }

  @Override
  public void show(AjaxRequestTarget target) {
    setOptionsToShow();
    target.appendJavascript("$('.separator').css('border-top', '0');");
    target.appendJavascript("$('.modal-footer').removeClass('modal-footer').addClass('wizard-modal-footer');");
    super.show(target);
  };

  public WizardAdministrationWindow setInterruptState(boolean enabled, boolean visible, IBehavior buttonDisableBehavior) {
    interruptLink.add(buttonDisableBehavior);
    interruptLink.setEnabled(enabled);
    interruptLink.setVisible(visible);
    return this;
  }

  public WizardAdministrationWindow setCancelState(boolean enabled, boolean visible, IBehavior buttonDisableBehavior) {
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
