/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.wicket.instrument;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.StringValidator;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.wicket.behavior.ButtonDisableBehavior;
import org.obiba.onyx.wicket.reusable.Dialog;
import org.obiba.onyx.wicket.reusable.Dialog.CloseButtonCallback;
import org.obiba.onyx.wicket.reusable.Dialog.Status;
import org.obiba.onyx.wicket.reusable.DialogBuilder;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Display of a 'Add' button to open a dialog that captures manually the values (either repeated manual ones or
 * automatic but allowed for manual capture ones), a list of measures (if repeatable), and a skip checkbox (if
 * incomplete number of measures is allowed).
 */
public class MeasuresPanel extends Panel {

  private static final long serialVersionUID = 1L;

  private static final Logger log = LoggerFactory.getLogger(MeasuresPanel.class);

  @SpringBean
  private ActiveInstrumentRunService activeInstrumentRunService;

  private MeasuresListPanel measuresList;

  private AjaxLink<Object> addButton;

  private SkipMeasureFragment skipMeasure;

  private boolean skipMeasurement = false;

  public MeasuresPanel(String id) {
    super(id);

    InstrumentType instrumentType = activeInstrumentRunService.getInstrumentType();

    skipMeasurement = (getInstrumentRun().getSkipComment() != null);

    addMeasuresList(instrumentType);
    addAddButton(instrumentType);
    addSkipMeasure(instrumentType);
  }

  private void addMeasuresList(InstrumentType instrumentType) {
    add(measuresList = new MeasuresListPanel("measuresList") {

      private static final long serialVersionUID = 1L;

      @Override
      public void onRefresh(AjaxRequestTarget target) {
        addButton.setEnabled(measuresList.getMeasureCount() < measuresList.getExpectedMeasureCount());
        target.addComponent(addButton);

        skipMeasure.refresh(target);

        MeasuresPanel.this.onRefresh(target);

        WizardForm form = findParent(WizardForm.class);
        WizardStepPanel step = (WizardStepPanel) form.get("step");
        step.handleWizardState(form, target);
      }

    });
    measuresList.setVisible(instrumentType.isRepeatable());
  }

  private Dialog addManualEntryDialog() {
    final InstrumentManualOutputParameterPanel instrumentManualOutputParameterPanel = new InstrumentManualOutputParameterPanel("content", 340);

    final Dialog manualEntryDialog = DialogBuilder.buildDialog("manualEntryDialog", new ResourceModel("manualEntry"), instrumentManualOutputParameterPanel).setOptions(Dialog.Option.OK_CANCEL_OPTION).getDialog();
    manualEntryDialog.setHeightUnit("em");
    manualEntryDialog.setWidthUnit("em");
    manualEntryDialog.setInitialHeight(26);
    manualEntryDialog.setInitialWidth(34);
    manualEntryDialog.setCloseButtonCallback(new CloseButtonCallback() {
      private static final long serialVersionUID = 1L;

      public boolean onCloseButtonClicked(AjaxRequestTarget target, Status status) {
        if(status == null || status != null && status.equals(Status.WINDOW_CLOSED) || status != null && status.equals(Status.CANCELLED)) {
          manualEntryDialog.getForm().clearInput();
          manualEntryDialog.getForm().setEnabled(false);
          return true;
        } else if(status.equals(Status.SUCCESS)) {
          manualEntryDialog.resetStatus();
          instrumentManualOutputParameterPanel.saveOutputInstrumentRunValues();
          measuresList.refresh(target);
          return true;
        } else if(status.equals(Status.ERROR)) {
          FeedbackPanel feedbackPanel = new FeedbackPanel("content");
          instrumentManualOutputParameterPanel.getFeedbackWindow().setContent(feedbackPanel);
          instrumentManualOutputParameterPanel.getFeedbackWindow().show(target);
          return false;
        }
        return true;
      }
    });
    add(manualEntryDialog);

    return manualEntryDialog;
  }

  private void addAddButton(InstrumentType instrumentType) {
    final Dialog manualEntryDialog = addManualEntryDialog();

    addButton = new AjaxLink<Object>("manualButton") {

      private static final long serialVersionUID = 1L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        manualEntryDialog.getForm().setEnabled(true);
        manualEntryDialog.show(target);
        onAddClick(target);
      }

    };
    addButton.setOutputMarkupId(true);
    addButton.add(new ButtonDisableBehavior());
    add(addButton);

    addButton.setVisible(instrumentType.hasManualCaptureOutputParameters());
  }

  private void addSkipMeasure(InstrumentType instrumentType) {
    skipMeasure = new SkipMeasureFragment("skipMeasure");
    skipMeasure.setVisible(instrumentType.isRepeatable() && instrumentType.isAllowPartial());
    skipMeasure.setOutputMarkupId(true);
    add(skipMeasure);
  }

  /**
   * Callback when Add button is clicked.
   * @param target
   */

  public void onAddClick(AjaxRequestTarget target) {

  }

  /**
   * Callback when refresh method is clicked.
   * @param target
   */
  public void onRefresh(AjaxRequestTarget target) {

  }

  /**
   * Callback when Skip checkbox is updated.
   * @param target
   */
  public void onSkipUpdate(AjaxRequestTarget target) {

  }

  public void refresh(AjaxRequestTarget target) {
    measuresList.refresh(target);
  }

  public int getExpectedMeasureCount() {
    return measuresList.getExpectedMeasureCount();
  }

  public boolean isSkipMeasurement() {
    return skipMeasurement;
  }

  public void setSkipMeasurement(boolean skipMeasurement) {
    this.skipMeasurement = skipMeasurement;
  }

  public boolean getSkipMeasurement() {
    return skipMeasurement;
  }

  public int getMeasureCount() {
    return measuresList.getMeasureCount();
  }

  public boolean isMeasureComplete() {
    return (getInstrumentRun().getValidMeasureCount() >= getExpectedMeasureCount());
  }

  private InstrumentRun getInstrumentRun() {
    return activeInstrumentRunService.getInstrumentRun();
  }

  /**
   * Ski measurement widget: a checkbox and a text area for comments.
   */
  public class SkipMeasureFragment extends Fragment {
    private static final long serialVersionUID = 1L;

    private TextArea<String> comment;

    private String skipComment;

    private CheckBox skipCheckbox;

    public SkipMeasureFragment(String id) {
      super(id, "skipMeasureFragment", MeasuresPanel.this);

      skipCheckbox = new CheckBox("skipMeasurements", new PropertyModel<Boolean>(MeasuresPanel.this, "skipMeasurement"));
      skipCheckbox.add(new AjaxFormComponentUpdatingBehavior("onchange") {
        private static final long serialVersionUID = 1L;

        @Override
        protected void onUpdate(AjaxRequestTarget target) {
          comment.setEnabled(isSkipMeasurement());
          target.addComponent(comment);
          if(!comment.isEnabled()) {
            comment.setDefaultModelObject(null);
            activeInstrumentRunService.removeSkipRemainingMeasuresCommentFromInstrumentRun();
          }

          WizardForm form = (WizardForm) MeasuresPanel.this.findParent(WizardForm.class);
          WizardStepPanel step = (WizardStepPanel) form.get("step");
          step.handleWizardState(form, target);

          addButton.setEnabled(!isSkipMeasurement());
          target.addComponent(addButton);

          MeasuresPanel.this.onSkipUpdate(target);
        }
      });
      skipCheckbox.setEnabled(getMeasureCount() > 0 && !isMeasureComplete());
      add(skipCheckbox);

      comment = new TextArea<String>("comment", new PropertyModel<String>(this, "skipComment"));
      comment.add(new StringValidator.MaximumLengthValidator(2000));
      comment.setRequired(true);
      comment.add(new AjaxFormComponentUpdatingBehavior("onchange") {
        private static final long serialVersionUID = 1L;

        @Override
        protected void onUpdate(AjaxRequestTarget target) {
          if(comment.getModelObject() != null) {
            activeInstrumentRunService.setSkipRemainingMeasuresCommentFromInstrumentRun(comment.getModelObject().toString());
          } else {
            activeInstrumentRunService.removeSkipRemainingMeasuresCommentFromInstrumentRun();
          }
        }

      });
      comment.setEnabled(isSkipMeasurement());
      add(comment);
      comment.setOutputMarkupId(true);
    }

    public void setSkipComment(String skipComment) {
      this.skipComment = skipComment;
    }

    public String getSkipComment() {
      return skipComment;
    }

    // refresh the skipMeasurement fragment and the action buttons
    public void refresh(AjaxRequestTarget target) {
      skipCheckbox.setEnabled(getMeasureCount() > 0 && getMeasureCount() < getExpectedMeasureCount());
      if(!skipCheckbox.isEnabled()) {
        skipCheckbox.setDefaultModelObject(false);
        comment.setDefaultModelObject(null);
        activeInstrumentRunService.removeSkipRemainingMeasuresCommentFromInstrumentRun();
      }
      comment.setEnabled(isSkipMeasurement() == true && skipCheckbox.isEnabled());
      target.addComponent(SkipMeasureFragment.this);
    }

  }

}
