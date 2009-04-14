/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.wicket.wizard;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.obiba.onyx.core.reusable.FeedbackWindow;
import org.obiba.onyx.wicket.behavior.LanguageStyleBehavior;
import org.obiba.onyx.wicket.behavior.ajaxbackbutton.HistoryAjaxBehavior;
import org.obiba.onyx.wicket.behavior.ajaxbackbutton.IHistoryAjaxBehaviorOwner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class WizardForm extends Form {

  private static final long serialVersionUID = 8829452703870884599L;

  private static final Logger log = LoggerFactory.getLogger(WizardForm.class);

  /**
   * Indicates whether the action was canceled or not. TODO: should probably go in an Action interface or abstract class
   * of some sort
   */
  private boolean canceled = false;

  public WizardForm(String id) {
    this(id, null);
  }

  public WizardForm(String id, IModel model) {
    super(id, model);

    setOutputMarkupId(true);
    setMultiPart(true);

    IBehavior buttonStyleBehavior = new AttributeAppender("class", new Model("ui-corner-all"), " ");
    IBehavior buttonDisableBehavior = new WizardButtonDisableBehavior();

    // finish button
    AjaxButton finish = createFinish();
    finish.add(buttonStyleBehavior).add(buttonDisableBehavior);
    finish.setEnabled(false);
    finish.setOutputMarkupId(true);
    add(finish);

    // previous button
    AjaxLink link = createPrevious();
    link.setEnabled(false);
    link.setOutputMarkupId(true);
    link.add(buttonStyleBehavior).add(buttonDisableBehavior);
    add(link);

    // next button
    AjaxButton button = createNext();
    button.setOutputMarkupId(true);
    button.add(buttonStyleBehavior).add(buttonDisableBehavior);
    add(button);

    // cancel button
    AjaxLink cancelLink = createCancel();
    cancelLink.add(buttonStyleBehavior);
    add(cancelLink);

    add(new LanguageStyleBehavior());
  }

  //
  //
  //

  private AjaxButton createNext() {
    AjaxButton button = new AjaxButton("nextLink") {
      private static final long serialVersionUID = 0L;

      @Override
      protected void onSubmit(AjaxRequestTarget target, Form form) {
        onNextSubmit(target, form);
      }

      @Override
      protected void onError(AjaxRequestTarget target, Form form) {
        onNextError(target, form);
      }

    };
    button.add(new AttributeModifier("value", true, getLabelModel("Next")));

    return button;
  }

  private AjaxLink createPrevious() {
    AjaxLink link = new AjaxLink("previousLink") {
      private static final long serialVersionUID = 0L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        onPreviousClick(target);
      }

    };
    link.add(new AttributeModifier("value", true, getLabelModel("Previous")));

    return link;
  }

  private AjaxButton createFinish() {
    AjaxButton finish = new AjaxButton("finish", this) {

      private static final long serialVersionUID = 0L;

      @Override
      protected void onSubmit(AjaxRequestTarget target, Form form) {
        onFinishSubmit(target, form);
      }

      @Override
      protected void onError(AjaxRequestTarget target, Form form) {
        onFinishError(target, form);
      }

    };
    finish.add(new AttributeModifier("value", true, getLabelModel("Finish")));

    return finish;
  }

  private AjaxLink createCancel() {
    AjaxLink link = new AjaxLink("cancelLink") {
      private static final long serialVersionUID = 0L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        onCancelClick(target);
      }

    };
    link.add(new AttributeModifier("value", true, getLabelModel("Cancel")));

    return link;
  }

  //
  // Event form triggers
  // 

  protected void onFinishSubmit(AjaxRequestTarget target, Form form) {
    log.debug("finish.onSubmit");
    onFinish(target, form);
  }

  protected void onFinishError(AjaxRequestTarget target, Form form) {
    log.debug("finish.onError");
    if(getFeedbackWindow() != null) showFeedbackWindow(target);
    WizardForm.this.onError(target, form);
    target.appendJavascript("Resizer.resizeWizard();");
  }

  protected void onPreviousClick(AjaxRequestTarget target) {
    HistoryAjaxBehavior historyAjaxBehavior = getHistoryAjaxBehavior();
    if(historyAjaxBehavior != null) {
      historyAjaxBehavior.registerAjaxEvent(target, this);
    }
    WizardForm.this.gotoPrevious(target);
  }

  protected void onNextSubmit(AjaxRequestTarget target, Form form) {
    log.debug("next.onSubmit");
    HistoryAjaxBehavior historyAjaxBehavior = getHistoryAjaxBehavior();
    if(historyAjaxBehavior != null) {
      historyAjaxBehavior.registerAjaxEvent(target, this);
    }
    WizardForm.this.gotoNext(target);
  }

  protected void onNextError(AjaxRequestTarget target, Form form) {
    log.debug("next.onError");
    if(getFeedbackWindow() != null) showFeedbackWindow(target);
    WizardForm.this.onError(target, form);
    WizardStepPanel currentStep = (WizardStepPanel) WizardForm.this.get("step");
    currentStep.onStepOutNextError(WizardForm.this, target);
    target.appendJavascript("Resizer.resizeWizard();");
  }

  protected void onCancelClick(AjaxRequestTarget target) {
    canceled = true;
    onCancel(target);
  }

  /**
   * Called after wizard form submission generates an error (on next or finish click).
   * @param target
   * @param form
   */
  public abstract void onError(AjaxRequestTarget target, Form form);

  /**
   * Called when finish is clicked.
   * @param target
   * @param form
   */
  public abstract void onFinish(AjaxRequestTarget target, Form form);

  /**
   * Called when cancel is clicked.
   * @param target
   */
  public abstract void onCancel(AjaxRequestTarget target);

  /**
   * Get the "next" component.
   * @return
   */
  public Component getNextLink() {
    return get("nextLink");
  }

  /**
   * Get the "previous" component.
   * @return
   */
  public Component getPreviousLink() {
    return get("previousLink");
  }

  /**
   * Get the "finish" component.
   * @return
   */
  public Component getFinishLink() {
    return get("finish");
  }

  /**
   * Get the "cancel" component.
   * @return
   */
  public Component getCancelLink() {
    return get("cancelLink");
  }

  /**
   * Warn the current step panel we are going out by next, and ask which is the next step.
   * @param target
   */
  protected void gotoNext(AjaxRequestTarget target) {
    WizardStepPanel currentStep = (WizardStepPanel) get("step");
    log.debug("gotoNext.currentStep={}", currentStep.getClass().getName());
    currentStep.onStepOutNext(WizardForm.this, target);

    WizardStepPanel next = currentStep.getNextStep();
    if(next != null) {
      currentStep.replaceWith(next);
      next.onStepInNext(this, target);
      next.handleWizardState(this, target);
    }
    target.addComponent(this);
  }

  /**
   * Warn the current step panel we are going out by previous, and ask which is the previous step.
   * @param target
   */
  protected void gotoPrevious(AjaxRequestTarget target) {
    WizardStepPanel currentStep = (WizardStepPanel) get("step");
    log.debug("gotoPrevious.currentStep={}", currentStep.getClass().getName());
    currentStep.onStepOutPrevious(WizardForm.this, target);

    WizardStepPanel previous = currentStep.getPreviousStep();
    if(previous != null) {
      currentStep.replaceWith(previous);
      previous.onStepInPrevious(this, target);
      previous.handleWizardState(this, target);
    }
    target.addComponent(this);
  }

  public boolean isCanceled() {
    return canceled;
  }

  public void setCanceled(boolean canceled) {
    this.canceled = canceled;
  }

  protected void showFeedbackWindow(AjaxRequestTarget target) {
    getFeedbackWindow().setContent(new FeedbackPanel("content"));
    getFeedbackWindow().show(target);
  }

  /**
   * Accessor to the feedback panel if any.
   * @return null by default.
   */
  public FeedbackWindow getFeedbackWindow() {
    return null;
  }

  public HistoryAjaxBehavior getHistoryAjaxBehavior() {
    HistoryAjaxBehavior behavior = (HistoryAjaxBehavior) visitParents(IHistoryAjaxBehaviorOwner.class, new Component.IVisitor() {

      public Object component(Component component) {
        return ((IHistoryAjaxBehaviorOwner) component).getHistoryAjaxBehavior();
      }

    });
    return behavior;
  }

  public static String getStepId() {
    return "step";
  }

  private class WizardButtonDisableBehavior extends AttributeAppender {

    private static final long serialVersionUID = -2793180600410649652L;

    public WizardButtonDisableBehavior() {
      super("class", new Model("ui-state-disabled"), " ");
    }

    /**
     * Overriden to enable the behaviour if the component is disabled. We want to append the attribute when the
     * component is disabled.
     */
    @Override
    public boolean isEnabled(Component component) {
      return component.isEnabled() == false;
    }
  }

  public LoadableDetachableModel getLabelModel(String label) {
    return new StringResourceModel(label, WizardForm.this, null);
  }

  public void changeWizardFormStyle(String cssClassName) {
    add(new AttributeModifier("class", new Model(cssClassName)));
  }
}
