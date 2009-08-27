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

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.value.ValueMap;
import org.apache.wicket.validation.validator.StringValidator;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameterCaptureMethod;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.wicket.behavior.ButtonDisableBehavior;
import org.obiba.onyx.wicket.model.SpringStringResourceModel;
import org.obiba.onyx.wicket.reusable.Dialog;
import org.obiba.onyx.wicket.reusable.DialogBuilder;
import org.obiba.onyx.wicket.reusable.Dialog.CloseButtonCallback;
import org.obiba.onyx.wicket.reusable.Dialog.Status;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;
import org.obiba.wicket.model.MessageSourceResolvableStringModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Get the input parameters that are from read-only sources and give the instructions to the operator:
 * <ul>
 * <li>General information with instrument launcher (if available)</li>
 * <li>instructions to enter manually captured input parameters (if needed)</li>
 * </ul>
 */
public abstract class InstrumentLaunchPanel extends Panel {

  private static final long serialVersionUID = 8250439838157103589L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(InstrumentLaunchPanel.class);

  @SpringBean
  private ActiveInstrumentRunService activeInstrumentRunService;

  @SpringBean
  private InstrumentService instrumentService;

  private MeasuresListPanel measuresList;

  private SkipMeasureFragment skipMeasure;

  private boolean skipMeasurement = false;

  @SuppressWarnings("serial")
  public InstrumentLaunchPanel(String id) {
    super(id);
    InstrumentRun currentRun = activeInstrumentRunService.getInstrumentRun();
    setModel(new Model(currentRun));
    setSkipMeasurement(currentRun.getSkipComment() != null);
    setOutputMarkupId(true);

    InstrumentType instrumentType = activeInstrumentRunService.getInstrumentType();
    String codebase = instrumentService.getInstrumentInstallPath(instrumentType);

    // general instructions and launcher
    add(new Label("general", new StringResourceModel("StartMeasurementWithInstrument", this, new Model(new ValueMap("name=" + instrumentType.getName())))));

    final InstrumentLauncher launcher = new InstrumentLauncher(instrumentType, codebase);

    IBehavior buttonDisableBehavior = new ButtonDisableBehavior();

    Link startLink = new Link("start") {

      @Override
      public void onClick() {
        launcher.launch();
        InstrumentLaunchPanel.this.onInstrumentLaunch();
      }

    };
    add(startLink);
    startLink.add(buttonDisableBehavior);
    startLink.setOutputMarkupId(true);
    setComponentEnabledOnSkip(startLink, false, null);

    final InstrumentManualOutputParameterPanel instrumentManualOutputParameterPanel = new InstrumentManualOutputParameterPanel("content", 340);
    final Dialog manualEntryDialog = DialogBuilder.buildDialog("manualEntryDialog", new ResourceModel("manualEntry"), instrumentManualOutputParameterPanel).setOptions(Dialog.Option.OK_CANCEL_OPTION).getDialog();
    add(manualEntryDialog);
    WebMarkupContainer manualButtonBlock = new WebMarkupContainer("manualButtonBlock");
    add(manualButtonBlock);

    AjaxLink manualButtonLink = new AjaxLink("manualButton") {

      @Override
      public void onClick(AjaxRequestTarget target) {
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
        manualEntryDialog.getForm().setEnabled(true);
        manualEntryDialog.show(target);
        // Note that "Manual" instrument has been launched.
        InstrumentLaunchPanel.this.onInstrumentLaunch();
      }

    };

    manualButtonBlock.add(manualButtonLink);
    manualButtonBlock.setVisible(instrumentType.isManualCaptureAllowed());
    manualButtonLink.setOutputMarkupId(true);
    manualButtonLink.add(buttonDisableBehavior);
    setComponentEnabledOnSkip(manualButtonLink, false, null);

    String errMessage = activeInstrumentRunService.updateReadOnlyInputParameterRunValue();
    if(errMessage != null) error(errMessage);

    RepeatingView repeat = new RepeatingView("repeat");
    add(repeat);

    // get all the input run values that requires manual capture
    boolean manualCaptureRequired = false;
    for(InstrumentInputParameter param : instrumentType.getInputParameters(InstrumentParameterCaptureMethod.MANUAL)) {

      final String paramCode = param.getCode();

      // We don't want to display parameters that were manually entered by the user in the previous step.
      // These will be automatically sent to the instrument.
      if(param.getDataSource() != null) {

        manualCaptureRequired = true;

        WebMarkupContainer item = new WebMarkupContainer(repeat.newChildId());
        repeat.add(item);

        item.add(new Label("instruction", new StringResourceModel("TypeTheValueInTheInstrument", InstrumentLaunchPanel.this, new Model() {
          public Object getObject() {
            InstrumentInputParameter param = (InstrumentInputParameter) activeInstrumentRunService.getInstrumentType().getInstrumentParameter(paramCode);
            InstrumentRunValue runValue = activeInstrumentRunService.getInstrumentRunValue(paramCode);
            ValueMap map = new ValueMap();
            map.put("description", new MessageSourceResolvableStringModel(param.getLabel()).getObject());
            Data data = runValue.getData(param.getDataType());
            if(data != null && data.getValue() != null) {
              map.put("value", new SpringStringResourceModel(data.getValueAsString()).getString());
              String unit = param.getMeasurementUnit();
              if(unit == null) {
                unit = "";
              }
              map.put("unit", unit);
            }
            return map;
          }
        })));
      }
    }

    Label instructions = new Label("instructions", new StringResourceModel("Instructions", InstrumentLaunchPanel.this, null));
    instructions.setVisible(manualCaptureRequired);
    add(instructions);

    add(measuresList = new MeasuresListPanel("measuresList") {

      @Override
      public void onRefresh(AjaxRequestTarget target) {
        WizardForm form = (WizardForm) InstrumentLaunchPanel.this.findParent(WizardForm.class);
        WizardStepPanel step = (WizardStepPanel) form.get("step");
        step.handleWizardState(form, target);
      }

    });
    measuresList.setVisible(activeInstrumentRunService.getInstrumentType().isRepeatable());

    skipMeasure = new SkipMeasureFragment("skipMeasure");
    skipMeasure.setVisible(activeInstrumentRunService.getInstrumentType().isRepeatable());
    skipMeasure.setOutputMarkupId(true);
    add(skipMeasure);

  }

  private void setComponentEnabledOnSkip(Component component, boolean enabled, AjaxRequestTarget target) {
    InstrumentRun currentRun = (InstrumentRun) InstrumentLaunchPanel.this.getModelObject();
    if(currentRun != null && getSkipMeasurement()) {
      component.setEnabled(enabled);

      // Disable measures list autorefresh when "skip remaining measure" is selected.
      if(measuresList != null) measuresList.disableAutoRefresh();
    } else {
      component.setEnabled(!enabled);

      // Reactivate measures list autorefresh when "skip remaining measure" is deselected.
      if(target != null && measuresList != null) {
        measuresList.enableAutoRefresh(target);
      }
    }
    if(target != null) target.addComponent(component);
  }

  public class SkipMeasureFragment extends Fragment {
    private static final long serialVersionUID = 1L;

    public SkipMeasureFragment(String id) {
      super(id, "skipMeasureFragment", InstrumentLaunchPanel.this);
      Object modelObject = InstrumentLaunchPanel.this.getModelObject();

      CheckBox box = new CheckBox("skipMeasurements", new PropertyModel(InstrumentLaunchPanel.this, "skipMeasurement"));
      box.add(new AjaxFormComponentUpdatingBehavior("onchange") {
        private static final long serialVersionUID = 1L;

        @Override
        protected void onUpdate(AjaxRequestTarget target) {
          setComponentEnabledOnSkip(get("comment"), true, target);
          if(!get("comment").isEnabled()) {
            get("comment").setModelObject(null);
            activeInstrumentRunService.removeSkipMeasurementForInstrumentRun();
          }

          WizardForm form = (WizardForm) InstrumentLaunchPanel.this.findParent(WizardForm.class);
          WizardStepPanel step = (WizardStepPanel) form.get("step");
          step.handleWizardState(form, target);

          setComponentEnabledOnSkip(InstrumentLaunchPanel.this.get("start"), false, target);
          setComponentEnabledOnSkip(InstrumentLaunchPanel.this.get("manualButtonBlock:manualButton"), false, target);
        }

      });
      box.setEnabled(((InstrumentRun) modelObject).getValidMeasureCount() > 0);
      add(box);

      final TextArea comment = new TextArea("comment", new PropertyModel(modelObject, "skipComment"));
      comment.add(new StringValidator.MaximumLengthValidator(2000));
      comment.setRequired(true);
      comment.add(new AjaxFormComponentUpdatingBehavior("onchange") {
        private static final long serialVersionUID = 1L;

        @Override
        protected void onUpdate(AjaxRequestTarget target) {
          if(comment.getModelObject() != null) {
            activeInstrumentRunService.setSkipMeasurementForInstrumentRun(comment.getModelObject().toString());
          } else {
            activeInstrumentRunService.removeSkipMeasurementForInstrumentRun();
          }
        }

      });
      setComponentEnabledOnSkip(comment, true, null);
      add(comment);
      comment.setOutputMarkupId(true);
    }

    // refresh the skipMeasurement fragment and the action buttons
    public void refresh(AjaxRequestTarget target) {
      MeasuresListPanel measuresList = InstrumentLaunchPanel.this.measuresList;

      Component startButton = InstrumentLaunchPanel.this.get("start");
      startButton.setEnabled(measuresList.getMeasureCount() < measuresList.getExpectedMeasureCount());
      target.addComponent(startButton);

      Component addButton = InstrumentLaunchPanel.this.get("manualButtonBlock:manualButton");
      addButton.setEnabled(measuresList.getMeasureCount() < measuresList.getExpectedMeasureCount());
      target.addComponent(addButton);

      Component skipCheckbox = get("skipMeasurements");
      skipCheckbox.setEnabled(measuresList.getMeasureCount() > 0 && measuresList.getMeasureCount() < measuresList.getExpectedMeasureCount());
      if(!skipCheckbox.isEnabled()) {
        skipCheckbox.setModelObject(false);
        get("comment").setModelObject(null);
        activeInstrumentRunService.removeSkipMeasurementForInstrumentRun();
      }
      get("comment").setEnabled(getSkipMeasurement() == true && skipCheckbox.isEnabled());
      target.addComponent(SkipMeasureFragment.this);
    }
  }

  /**
   * Called when instrument launcher is clicked.
   */
  public abstract void onInstrumentLaunch();

  public boolean getSkipMeasurement() {
    return skipMeasurement;
  }

  public void setSkipMeasurement(boolean skipMeasurement) {
    this.skipMeasurement = skipMeasurement;
  }

  public SkipMeasureFragment getSkipMeasure() {
    return skipMeasure;
  }

}
