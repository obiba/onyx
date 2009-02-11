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

import java.awt.TextField;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.StringValidator;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameterCaptureMethod;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.wicket.InstrumentRunValueDataModel;
import org.obiba.onyx.jade.core.wicket.instrument.validation.IntegrityCheckValidator;
import org.obiba.onyx.util.data.DataType;
import org.obiba.onyx.wicket.data.DataField;
import org.obiba.onyx.wicket.data.DataValidator;
import org.obiba.wicket.markup.html.table.DetachableEntityModel;
import org.obiba.wicket.model.MessageSourceResolvableStringModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstrumentOutputParameterPanel extends Panel {

  private static final long serialVersionUID = 3008363510160516288L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(InstrumentOutputParameterPanel.class);

  @SpringBean
  private EntityQueryService queryService;

  @SpringBean
  private ActiveInstrumentRunService activeInstrumentRunService;

  private List<IModel> outputRunValueModels = new ArrayList<IModel>();

  @SuppressWarnings("serial")
  public InstrumentOutputParameterPanel(String id) {
    super(id);
    setOutputMarkupId(true);

    if(!activeInstrumentRunService.hasOutputParameter(InstrumentParameterCaptureMethod.MANUAL)) {
      add(new EmptyPanel("manualOutputs"));
    } else {

      String errMessage = activeInstrumentRunService.updateReadOnlyInputParameterRunValue();
      if(errMessage != null) error(errMessage);

      RepeatingView repeat = new RepeatingView("repeat");
      add(repeat);

      for(InstrumentOutputParameter param : activeInstrumentRunService.getOutputParameters(InstrumentParameterCaptureMethod.MANUAL)) {
        WebMarkupContainer item = new WebMarkupContainer(repeat.newChildId());
        repeat.add(item);

        InstrumentRunValue runValue = activeInstrumentRunService.getInstrumentRunValue(param);
        final IModel runValueModel = new DetachableEntityModel(queryService, runValue);
        outputRunValueModels.add(runValueModel);

        DataField field = new DataField("field", new InstrumentRunValueDataModel(runValueModel, param.getDataType()), param.getDataType(), param.getMeasurementUnit());
        field.setRequired(true);
        field.setLabel(new MessageSourceResolvableStringModel(new PropertyModel(param, "label")));
        field.add(new AjaxFormComponentUpdatingBehavior("onblur") {
          protected void onUpdate(AjaxRequestTarget target) {
            activeInstrumentRunService.update((InstrumentRunValue) runValueModel.getObject());
          }
        });

        if(param.getDataType().equals(DataType.TEXT) && (field.getField().getClass().equals(TextField.class) || field.getField().getClass().equals(TextArea.class))) {
          field.getField().add(new DataValidator(new StringValidator.MaximumLengthValidator(2000), param.getDataType()));
        }

        IntegrityCheckValidator.addChecks(field, param.getIntegrityChecks());
        item.add(field);

        FormComponentLabel label = new FormComponentLabel("label", field.getField());
        item.add(label);

        Label labelText = new Label("labelText", new MessageSourceResolvableStringModel(new PropertyModel(param, "label")));
        label.add(labelText);
      }
    }
  }

  public void saveOutputInstrumentRunValues() {
    for(IModel runValueModel : outputRunValueModels) {
      activeInstrumentRunService.update((InstrumentRunValue) runValueModel.getObject());
    }
  }

}
