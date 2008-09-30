package org.obiba.onyx.jade.core.wicket.instrument;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.SpringWebApplication;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameterCaptureMethod;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.wicket.instrument.validation.IntegrityCheckValidator;
import org.obiba.onyx.wicket.data.DataField;
import org.obiba.wicket.markup.html.table.DetachableEntityModel;
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

  @SpringBean(name = "userSessionService")
  private UserSessionService userSessionService;

  private List<IModel> outputRunValueModels = new ArrayList<IModel>();

  @SuppressWarnings("serial")
  public InstrumentOutputParameterPanel(String id) {
    super(id);
    setOutputMarkupId(true);

    InstrumentOutputParameter template = new InstrumentOutputParameter();
    template.setInstrument(activeInstrumentRunService.getInstrument());
    template.setCaptureMethod(InstrumentParameterCaptureMethod.MANUAL);

    if(queryService.count(template) == 0) {
      add(new EmptyPanel("manualOutputs"));
    } else {

      RepeatingView repeat = new RepeatingView("repeat");
      add(repeat);

      for(final InstrumentOutputParameter param : queryService.match(template)) {
        WebMarkupContainer item = new WebMarkupContainer(repeat.newChildId());
        repeat.add(item);

        // Inject the Spring application context and the user session service
        // into the instrument parameter. NOTE: These are dependencies of
        // InstrumentParameter.getDescription().
        param.setApplicationContext(((SpringWebApplication) getApplication()).getSpringContextLocator().getSpringContext());
        param.setUserSessionService(userSessionService);

        Label label = new Label("label", new PropertyModel(param, "description"));
        item.add(label);

        InstrumentRunValue runValue = activeInstrumentRunService.getOutputInstrumentRunValue(param.getName());
        final IModel runValueModel = new DetachableEntityModel(queryService, runValue);
        outputRunValueModels.add(runValueModel);

        DataField field = new DataField("field", new PropertyModel(runValueModel, "data"), runValue.getDataType(), param.getMeasurementUnit());
        field.setRequired(true);
        field.setLabel(new PropertyModel(param, "description"));
        field.add(new AjaxFormComponentUpdatingBehavior("onblur") {
          protected void onUpdate(AjaxRequestTarget target) {
            activeInstrumentRunService.update((InstrumentRunValue) runValueModel.getObject());
          }
        });
        IntegrityCheckValidator.addChecks(field, param.getIntegrityChecks());
        item.add(field);
      }
    }
  }

  public void saveOutputInstrumentRunValues() {
    for(IModel runValueModel : outputRunValueModels) {
      activeInstrumentRunService.update((InstrumentRunValue) runValueModel.getObject());
    }
  }

}
