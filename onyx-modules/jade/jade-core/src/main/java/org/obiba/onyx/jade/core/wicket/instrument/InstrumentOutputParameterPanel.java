package org.obiba.onyx.jade.core.wicket.instrument;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.SpringWebApplication;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.time.Duration;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameterCaptureMethod;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.wicket.data.DataField;
import org.obiba.wicket.markup.html.panel.KeyValueDataPanel;
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
  private InstrumentService instrumentService;

  @SpringBean
  private ActiveInstrumentRunService activeInstrumentRunService;

  @SpringBean(name = "userSessionService")
  private UserSessionService userSessionService;

  private List<IModel> runValueModels;

  private boolean manual = false;

  private ManualFragment manualFragment = new ManualFragment("manual");

  @SuppressWarnings("serial")
  public InstrumentOutputParameterPanel(String id) {
    super(id);
    setOutputMarkupId(true);

    runValueModels = new ArrayList<IModel>();

    initManualOutputs();

    if(instrumentService.isInteractiveInstrument(activeInstrumentRunService.getInstrument())) {
      initAutomaticOutputs(null);
      add(manualFragment);
    } else {
      add(new EmptyPanel("manual"));
      add(new EmptyPanel("automaticOutputs"));
    }
  }

  public boolean isManual() {
    return manual;
  }

  public void setManual(boolean manual) {
    this.manual = manual;
  }

  private void initManualOutputs() {
    InstrumentOutputParameter template = new InstrumentOutputParameter();
    template.setInstrument(activeInstrumentRunService.getInstrument());
    template.setCaptureMethod(InstrumentParameterCaptureMethod.MANUAL);

    if(queryService.count(template) == 0) {
      add(new EmptyPanel("manualOutputs"));
    } else {
      KeyValueDataPanel outputs = new KeyValueDataPanel("manualOutputs", new StringResourceModel("ManualDataOutputs", this, null));
      for(final InstrumentOutputParameter param : queryService.match(template)) {
        // Inject the Spring application context and the user session service
        // into the instrument parameter. NOTE: These are dependencies of
        // InstrumentParameter.getDescription().
        param.setApplicationContext(((SpringWebApplication) getApplication()).getSpringContextLocator().getSpringContext());
        param.setUserSessionService(userSessionService);

        Label label = new Label(KeyValueDataPanel.getRowKeyId(), new Model() {
          public Object getObject() {
            return param.getDescription();
          }
        });

        InstrumentRunValue runValue = activeInstrumentRunService.getOutputInstrumentRunValue(param.getName());
        IModel runValueModel = new DetachableEntityModel(queryService, runValue);
        runValueModels.add(runValueModel);

        DataField field = new DataField(KeyValueDataPanel.getRowValueId(), new PropertyModel(runValueModel, "data"), runValue.getDataType(), param.getMeasurementUnit());
        field.setRequired(true);
        field.setLabel(new Model() {
          public Object getObject() {
            return param.getDescription();
          }
        });

        outputs.addRow(label, field);
      }
      add(outputs);
    }
  }

  private void initAutomaticOutputs(AjaxRequestTarget target) {
    manualFragment.setVisible(false);
    InstrumentOutputParameter template = new InstrumentOutputParameter();
    template.setInstrument((Instrument) getModelObject());
    template.setCaptureMethod(InstrumentParameterCaptureMethod.AUTOMATIC);

    Component newOutputs = null;
    if(queryService.count(template) == 0) {
      newOutputs = new EmptyPanel("automaticOutputs");
    } else {
      KeyValueDataPanel outputs = new KeyValueDataPanel("automaticOutputs", new StringResourceModel("AutomaticDataOutputs", this, null));
      for(final InstrumentOutputParameter param : queryService.match(template)) {
        // Inject the Spring application context and the user session service
        // into the instrument parameter. NOTE: These are dependencies of
        // InstrumentParameter.getDescription().
        param.setApplicationContext(((SpringWebApplication) getApplication()).getSpringContextLocator().getSpringContext());
        param.setUserSessionService(userSessionService);

        Label label = new Label(KeyValueDataPanel.getRowKeyId(), new Model() {
          public Object getObject() {
            return param.getDescription();
          }
        });
        Component output = null;

        // case we going through this multiple times
        InstrumentRunValue runValue = activeInstrumentRunService.getOutputInstrumentRunValue(param.getName());
        IModel runValueModel = new DetachableEntityModel(queryService, runValue);
        runValueModels.add(runValueModel);

        if(manual) {
          manualFragment.setVisible(true);

          if(runValue.getData().getValueAsString() == null) runValue.setCaptureMethod(InstrumentParameterCaptureMethod.MANUAL);

          DataField field = new DataField(KeyValueDataPanel.getRowValueId(), new PropertyModel(runValueModel, "data"), runValue.getDataType());
          field.setRequired(true);
          if(runValue.getCaptureMethod().equals(InstrumentParameterCaptureMethod.AUTOMATIC)) field.setFieldEnabled(false);
          field.setLabel(new Model() {
            public Object getObject() {
              return param.getDescription();
            }
          });
          output = field;

        } else if(runValue != null && runValue.getData() != null && runValue.getData().getValueAsString() != null) {
          output = new Label(KeyValueDataPanel.getRowValueId(), new RunValueLabelModel(runValue));
        } else {
          manualFragment.setVisible(true);
          output = new Label(KeyValueDataPanel.getRowValueId(), "");
        }

        if(output != null) {
          outputs.addRow(label, output);
        }
      }
      outputs.setOutputMarkupId(true);
      newOutputs = outputs;
    }

    Component currentOutputs = get("automaticOutputs");
    if(currentOutputs != null) {
      currentOutputs.replaceWith(newOutputs);
    } else {
      add(newOutputs);
    }

    if(target != null) {
      target.addComponent(newOutputs);
    }
  }

  public void saveOutputParameterValues() {
    for(IModel runValueModel : runValueModels) {
      activeInstrumentRunService.update((InstrumentRunValue) runValueModel.getObject());
    }
  }

  @SuppressWarnings("serial")
  private class ManualFragment extends Fragment {

    private AbstractAjaxTimerBehavior timer;

    @SuppressWarnings("serial")
    public ManualFragment(String id) {
      super(id, "manualFragment", InstrumentOutputParameterPanel.this);
      setOutputMarkupId(true);
      timer = new AbstractAjaxTimerBehavior(Duration.seconds(5)) {

        @Override
        protected void onTimer(AjaxRequestTarget target) {
          if(!isManual()) initAutomaticOutputs(target);
        }

      };

      CheckBox cb = new CheckBox("manual", new PropertyModel(InstrumentOutputParameterPanel.this, "manual"));
      cb.setOutputMarkupId(true);
      cb.add(new OnChangeAjaxBehavior() {

        @Override
        protected void onUpdate(AjaxRequestTarget target) {
          initAutomaticOutputs(target);
        }

      });
      add(timer);
      add(cb);
    }

  }

}
