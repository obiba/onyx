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
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.value.ValueMap;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameterCaptureMethod;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.wicket.model.SpringStringResourceModel;
import org.obiba.onyx.wicket.reusable.Dialog;
import org.obiba.onyx.wicket.reusable.DialogBuilder;
import org.obiba.onyx.wicket.reusable.Dialog.CloseButtonCallback;
import org.obiba.onyx.wicket.reusable.Dialog.Status;
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

  @SuppressWarnings("serial")
  public InstrumentLaunchPanel(String id) {
    super(id);
    setOutputMarkupId(true);

    InstrumentType instrumentType = activeInstrumentRunService.getInstrumentType();
    String codebase = instrumentService.getInstrumentInstallPath(instrumentType);

    // general instructions and launcher
    add(new Label("general", new StringResourceModel("StartMeasurementWithInstrument", this, new Model(new ValueMap("name=" + instrumentType.getName())))));

    final InstrumentLauncher launcher = new InstrumentLauncher(instrumentType, codebase);

    add(new Link("start") {

      @Override
      public void onClick() {
        launcher.launch();
        InstrumentLaunchPanel.this.onInstrumentLaunch();
      }

    });

    final InstrumentManualOutputParameterPanel instrumentManualOutputParameterPanel = new InstrumentManualOutputParameterPanel("content");
    final Dialog manualEntryDialog = DialogBuilder.buildDialog("manualEntryDialog", new ResourceModel("manualEntry"), instrumentManualOutputParameterPanel).setOptions(Dialog.Option.OK_CANCEL_OPTION).getDialog();
    add(manualEntryDialog);
    WebMarkupContainer manualButtonBlock = new WebMarkupContainer("manualButtonBlock");
    add(manualButtonBlock);
    manualButtonBlock.add(new AjaxLink("manualButton") {

      @Override
      public void onClick(AjaxRequestTarget target) {
        manualEntryDialog.setCloseButtonCallback(new CloseButtonCallback() {
          private static final long serialVersionUID = 1L;

          public boolean onCloseButtonClicked(AjaxRequestTarget target, Status status) {
            switch(status) {
            case SUCCESS:
              manualEntryDialog.resetStatus();
              instrumentManualOutputParameterPanel.saveOutputInstrumentRunValues();
              return true;
            case CANCELLED:
            case WINDOW_CLOSED:
            default:
              return true;
            }
          }
        });
        manualEntryDialog.show(target);
        // Note that "Manual" instrument has been launched.
        InstrumentLaunchPanel.this.onInstrumentLaunch();
      }

    });
    manualButtonBlock.setVisible(instrumentType.isManualCaptureAllowed());

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
  }

  /**
   * Called when instrument launcher is clicked.
   */
  public abstract void onInstrumentLaunch();

}
