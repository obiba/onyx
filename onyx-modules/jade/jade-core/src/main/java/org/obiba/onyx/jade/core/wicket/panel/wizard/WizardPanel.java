package org.obiba.onyx.jade.core.wicket.panel.wizard;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.calldecorator.AjaxCallDecorator;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;

public abstract class WizardPanel extends Panel {

  private static final long serialVersionUID = 1L;

  @SpringBean
  protected EntityQueryService queryService;

  /**
   * Indicates whether the action was canceled or not. TODO: should probably go
   * in an Action interface or abstract class of some sort
   */
  private boolean canceled = false;

  public WizardPanel(String id, IModel instrument) {
    super(id);
    setModel(instrument);

    final WizardForm form = getForm("form");
    add(form);
    
    IBehavior buttonBehavior = new WizardButtonBehavior();
    // finish button
    AjaxButton finish = new AjaxButton("finish", form) {

      private static final long serialVersionUID = 0L;

      @Override
      protected IAjaxCallDecorator getAjaxCallDecorator() {
        return new AjaxCallDecorator() {

          private static final long serialVersionUID = -6689010365115709867L;

          @Override
          public CharSequence decorateScript(CharSequence script) {
            return "jQuery('#" + form.getMarkupId() + " input').addClass('disabled').attr('disabled','disabled');" + script;
          }
        };

      }

      @Override
      protected void onSubmit(AjaxRequestTarget target, Form form) {
        // TODO
      }

    };
    finish.add(new AttributeModifier("value", true, new StringResourceModel("Finish", WizardPanel.this, null)));
    finish.add(buttonBehavior);
    finish.setEnabled(false);
    finish.setOutputMarkupId(true);
    form.add(finish);

    // previous button
    AjaxLink link = new AjaxLink("previousLink") {
      private static final long serialVersionUID = 0L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        form.gotoPrevious(target);
      }

    };
    link.add(new AttributeModifier("value", true, new StringResourceModel("wizard.previous", WizardPanel.this, null)));
    link.setEnabled(false);
    link.setOutputMarkupId(true);
    link.add(buttonBehavior);
    form.add(link);

    // next button
    link = new AjaxLink("nextLink") {
      private static final long serialVersionUID = 0L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        form.gotoNext(target);
      }

    };
    link.add(new AttributeModifier("value", true, new StringResourceModel("wizard.next", WizardPanel.this, null)));
    link.setOutputMarkupId(true);
    link.add(buttonBehavior);
    add(link);

    // cancel button
    link = new AjaxLink("cancelLink") {
      private static final long serialVersionUID = 0L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        canceled = true;
        // TODO
      }

    };
    link.add(new AttributeModifier("value", true, new StringResourceModel("Cancel", WizardPanel.this, null)));
    form.add(link);

    
  }

  public abstract WizardForm getForm(String componentId);

  public boolean wasCanceled() {
    return canceled;
  }

  public EntityQueryService getQueryService() {
    return queryService;
  }

  private class WizardButtonBehavior extends AttributeAppender {

    private static final long serialVersionUID = -2793180600410649652L;

    public WizardButtonBehavior() {
      super("class", new Model("disabled"), " ");
    }

    /**
     * Overriden to enable the behaviour if the component is disabled. We want
     * to append the attribute when the component is disabled.
     */
    @Override
    public boolean isEnabled(Component component) {
      return component.isEnabled() == false;
    }
  }
}
