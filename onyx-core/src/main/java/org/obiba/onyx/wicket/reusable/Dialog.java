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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.obiba.onyx.wicket.model.SpringStringResourceModel;

/**
 * Reusable class extending ModalWindow to predefine dialog boxes Title with possibility of adding an icon, scrollable
 * content, predefined customizable buttons
 * 
 * To determine which button has been pressed, implement the {@code closeButtonCallback}. Inside the callback check the
 * value of {@code status} using {@code getStatus()} to determine which button the user clicked. If the user actions
 * does not cause the {@code Dialog} to close then the calling client must call {@code resetStatus()} after handling the
 * mouse click in order to accurately read the next mouse click.
 */
public class Dialog extends ModalWindow {

  private static final long serialVersionUID = 928252608995728804L;

  /** The user's most recent mouse click action. */
  private Status status;

  private Type type;

  private Form form;

  private CloseButtonCallback closeButtonCallback;

  private WindowClosedCallback windowClosedCallback;

  private List<Object> customOptionsLeft = new ArrayList<Object>();

  private List<Object> customOptionsRight = new ArrayList<Object>();

  public enum Option {
    YES_OPTION, NO_OPTION, OK_OPTION, CANCEL_OPTION, CLOSE_OPTION, YES_NO_OPTION, YES_NO_CANCEL_OPTION, OK_CANCEL_OPTION
  }

  public enum Type {
    WARNING, INFO, ERROR, PLAIN
  }

  public enum OptionSide {
    LEFT, RIGHT
  }

  /**
   * Mouse click actions the {@code Dialog} keeps track of.
   */
  public enum Status {
    /** Window dressing "X" button clicked. User closed window. */
    WINDOW_CLOSED,
    /** "OK" button clicked. Form submission without errors. */
    SUCCESS,
    /** "OK" button clicked. Form submission with errors. */
    ERROR,
    /** "Yes" button clicked. */
    YES,
    /** "No" button clicked. */
    NO,
    /** "Cancel" button clicked. */
    CANCELLED,
    /** "Close" button clicked. */
    CLOSED,
    /** Other option clicked */
    OTHER
  }

  public Dialog(String id) {
    super(id);

    setCssClassName("onyx");
    setResizable(false);

    form = new Form("form");
    form.add(new WebMarkupContainer(getContentId()));

    AjaxButton okButton = new AjaxButton("ok", form) {

      @Override
      protected void onSubmit(AjaxRequestTarget target, Form form) {
        Dialog.this.setStatus(Status.SUCCESS);
        if(closeButtonCallback == null || closeButtonCallback.onCloseButtonClicked(target, Dialog.this.getStatus())) {
          ModalWindow.closeCurrent(target);
        }
      }

      @Override
      protected void onError(AjaxRequestTarget target, Form form) {
        Dialog.this.setStatus(Status.ERROR);
        if(closeButtonCallback == null || closeButtonCallback.onCloseButtonClicked(target, Dialog.this.getStatus())) {
          ModalWindow.closeCurrent(target);
        }
      }

    };
    okButton.add(new AttributeModifier("value", true, new StringResourceModel("Dialog.Ok", this, null)));
    form.add(okButton);

    AjaxLink cancelButton = new AjaxLink("cancel") {

      @Override
      public void onClick(AjaxRequestTarget target) {
        Dialog.this.setStatus(Status.CANCELLED);
        if(closeButtonCallback == null || closeButtonCallback.onCloseButtonClicked(target, Dialog.this.getStatus())) {
          ModalWindow.closeCurrent(target);
        }
      }

    };
    cancelButton.add(new AttributeModifier("value", true, new StringResourceModel("Dialog.Cancel", this, null)));
    form.add(cancelButton);

    AjaxLink yesButton = new AjaxLink("yes") {

      @Override
      public void onClick(AjaxRequestTarget target) {
        Dialog.this.setStatus(Status.YES);
        if(closeButtonCallback == null || closeButtonCallback.onCloseButtonClicked(target, Dialog.this.getStatus())) {
          ModalWindow.closeCurrent(target);
        }
      }

    };
    yesButton.add(new AttributeModifier("value", true, new StringResourceModel("Dialog.Yes", this, null)));
    form.add(yesButton);

    AjaxLink noButton = new AjaxLink("no") {

      @Override
      public void onClick(AjaxRequestTarget target) {
        Dialog.this.setStatus(Status.NO);
        if(closeButtonCallback == null || closeButtonCallback.onCloseButtonClicked(target, Dialog.this.getStatus())) {
          ModalWindow.closeCurrent(target);
        }
      }

    };
    noButton.add(new AttributeModifier("value", true, new StringResourceModel("Dialog.No", this, null)));
    form.add(noButton);

    AjaxLink closeButton = new AjaxLink("close") {

      @Override
      public void onClick(AjaxRequestTarget target) {
        Dialog.this.setStatus(Status.CLOSED);
        if(closeButtonCallback == null || closeButtonCallback.onCloseButtonClicked(target, Dialog.this.getStatus())) {
          ModalWindow.closeCurrent(target);
        }
      }

    };
    closeButton.add(new AttributeModifier("value", true, new StringResourceModel("Dialog.Close", this, null)));
    form.add(closeButton);

    this.setWindowClosedCallback(new WindowClosedCallback() {
      private static final long serialVersionUID = 1L;

      public void onClose(AjaxRequestTarget target, Status status) {
        // TODO Auto-generated method stub
      }
    });

    WebMarkupContainer modalContent = new WebMarkupContainer(getContentId());
    modalContent.setOutputMarkupId(true);
    modalContent.add(form);
    super.setContent(modalContent);
  }

  public void setOptions(Option option, String... labels) {
    if(option == null) {
      setEnabledOptions(true, false, false, false, false);
      return;
    }

    switch(option) {
    case YES_NO_CANCEL_OPTION:
      setEnabledOptions(false, true, true, true, false);
      if(labels != null) setOptionLabels(new String[] { "yes", "no", "cancel" }, labels);
      break;

    case YES_NO_OPTION:
      setEnabledOptions(false, false, true, true, false);
      if(labels != null) setOptionLabels(new String[] { "yes", "no" }, labels);
      break;

    case YES_OPTION:
      setEnabledOptions(false, false, true, false, false);
      if(labels != null) setOptionLabels(new String[] { "yes" }, labels);
      break;

    case NO_OPTION:
      setEnabledOptions(false, false, false, true, false);
      if(labels != null) setOptionLabels(new String[] { "no" }, labels);
      break;

    case OK_CANCEL_OPTION:
      setEnabledOptions(true, true, false, false, false);
      if(labels != null) setOptionLabels(new String[] { "ok", "cancel" }, labels);
      break;

    case OK_OPTION:
      setEnabledOptions(true, false, false, false, false);
      if(labels != null) setOptionLabels(new String[] { "ok" }, labels);
      break;

    case CANCEL_OPTION:
      setEnabledOptions(false, true, false, false, false);
      if(labels != null) setOptionLabels(new String[] { "cancel" }, labels);
      break;

    case CLOSE_OPTION:
      setEnabledOptions(false, false, false, false, true);
      if(labels != null) setOptionLabels(new String[] { "close" }, labels);
      break;

    default:
      setEnabledOptions(false, false, false, false, true);
    }
  }

  private void setEnabledOptions(boolean ok, boolean cancel, boolean yes, boolean no, boolean close) {
    form.get("ok").setVisible(ok);
    form.get("cancel").setVisible(cancel);
    form.get("yes").setVisible(yes);
    form.get("no").setVisible(no);
    form.get("close").setVisible(close);
  }

  private void setOptionLabels(String[] componentIds, String... labels) {
    int i = 0;
    for(String label : labels) {
      if(i >= componentIds.length) break;
      form.get(componentIds[i]).add(new AttributeModifier("value", true, new SpringStringResourceModel("Dialog." + label)));
      i++;
    }
  }

  @Override
  public void show(AjaxRequestTarget target) {
    super.show(target);
    resetStatus();

    if(customOptionsLeft.size() > 0) {
      form.addOrReplace(new OptionListFragment("customOptionsLeft", customOptionsLeft));
    } else {
      form.addOrReplace(new EmptyPanel("customOptionsLeft"));
    }

    if(customOptionsRight.size() > 0) {
      form.addOrReplace(new OptionListFragment("customOptionsRight", customOptionsRight));
    } else {
      form.addOrReplace(new EmptyPanel("customOptionsRight"));
    }

    if(type != null && type.equals(Type.ERROR)) {
      target.appendJavascript("$(document).ready(function () {$('.onyx-feedback .w_captionText').each(function() {$(this).addClass('ui-state-error');});});");
    } else if(type != null && type.equals(Type.WARNING)) {
      target.appendJavascript("$(document).ready(function () {$('.onyx-feedback .w_captionText').each(function() {$(this).addClass('ui-state-warning');});});");
    } else if(type != null && type.equals(Type.INFO)) {
      target.appendJavascript("$(document).ready(function () {$('.onyx-feedback .w_captionText').each(function() {$(this).addClass('ui-state-high');});});");
    }
  }

  /**
   * Sets the content of the dialog box.
   * 
   * @param component
   */
  @Override
  public void setContent(Component component) {
    if(component.getId().equals(getContentId()) == false) {
      throw new WicketRuntimeException("Dialog box content id is wrong.");
    }
    component.setOutputMarkupPlaceholderTag(true);
    component.setVisible(true);
    form.replace(component);
  }

  public static interface CloseButtonCallback extends Serializable {

    public boolean onCloseButtonClicked(AjaxRequestTarget target, Status status);

  }

  public static interface WindowClosedCallback extends Serializable {

    public void onClose(AjaxRequestTarget target, Status status);

  }

  /**
   * Returns the id of formContent component.
   * @return Id of formContent component.
   */
  public String getContentId() {
    return "content";
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public void resetStatus() {
    status = Status.WINDOW_CLOSED;
  }

  public void setWindowClosedCallback(WindowClosedCallback windowClosedCallback) {
    this.windowClosedCallback = windowClosedCallback;

    super.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {

      private static final long serialVersionUID = 1L;

      public void onClose(AjaxRequestTarget target) {
        Dialog.this.windowClosedCallback.onClose(target, Dialog.this.status);
      }

    });
  }

  public WindowClosedCallback getWindowClosedCallback() {
    return windowClosedCallback;
  }

  public void setCloseButtonCallback(CloseButtonCallback closeButtonCallback) {
    this.closeButtonCallback = closeButtonCallback;

    super.setCloseButtonCallback(new ModalWindow.CloseButtonCallback() {

      private static final long serialVersionUID = 1L;

      public boolean onCloseButtonClicked(AjaxRequestTarget target) {
        return Dialog.this.closeButtonCallback.onCloseButtonClicked(target, Dialog.this.status);
      }

    });
  }

  public CloseButtonCallback getCloseButtonCallback() {
    return closeButtonCallback;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public Form getForm() {
    return form;
  }

  /**
   * Set a css class that will added to the form within the dialog.
   * @param formCssClass css class name
   */
  public void setFormCssClass(String formCssClass) {
    if(form != null) {
      form.add(new AttributeModifier("class", true, new Model(formCssClass)));
    }
  }

  public void addOption(String label, OptionSide side, AjaxLink link, String... name) {
    link.add(new AttributeModifier("value", true, new SpringStringResourceModel("Dialog." + label)));
    if(name.length > 0) link.add(new AttributeModifier("name", true, new Model(name[0])));

    if(side.equals(OptionSide.LEFT)) {
      link.add(new AttributeModifier("class", true, new Model("obiba-button ui-corner-all left")));
      customOptionsLeft.add(link);
    } else {
      link.add(new AttributeModifier("class", true, new Model("obiba-button ui-corner-all right")));
      customOptionsRight.add(link);
    }
  }

  public void addSubmitOption(String label, OptionSide side, AjaxButton button, String... name) {
    button.add(new AttributeModifier("value", true, new SpringStringResourceModel("Dialog." + label)));
    if(name.length > 0) button.add(new AttributeModifier("name", true, new Model(name[0])));

    if(side.equals(OptionSide.LEFT)) {
      button.add(new AttributeModifier("class", true, new Model("obiba-button ui-corner-all left")));
      customOptionsLeft.add(button);
    } else {
      customOptionsRight.add(button);
    }
  }

  private class OptionListFragment extends Fragment {
    private static final long serialVersionUID = 1L;

    public OptionListFragment(String id, List<Object> customOptions) {
      super(id, "optionListFragment", Dialog.this);
      RepeatingView repeater = new RepeatingView("link");

      for(Object option : customOptions) {
        if(option instanceof AjaxLink) repeater.add((AjaxLink) option);
        if(option instanceof AjaxButton) repeater.add((AjaxButton) option);
      }

      add(repeater);
    }
  }
}
