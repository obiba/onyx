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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameterCaptureMethod;
import org.obiba.onyx.jade.core.domain.instrument.validation.IntegrityCheck;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.domain.run.Measure;
import org.obiba.onyx.jade.core.domain.run.MeasureStatus;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.jade.core.wicket.run.InstrumentRunPanel;
import org.obiba.onyx.wicket.reusable.ConfirmationDialog;
import org.obiba.onyx.wicket.reusable.ConfirmationDialog.OnYesCallback;
import org.obiba.onyx.wicket.reusable.Dialog;
import org.obiba.onyx.wicket.util.DateModelUtils;
import org.obiba.wicket.model.MessageSourceResolvableStringModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;

/**
 * Panel that lists the existing measures of the current InstrumentRun.
 */
public abstract class MeasuresListPanel extends Panel {

  private static final Logger log = LoggerFactory.getLogger(MeasuresListPanel.class);

  private static final long serialVersionUID = 1L;

  private static final int DEFAULT_INITIAL_WIDTH = 34;

  private static final int DEFAULT_INITIAL_HEIGHT = 35;

  @SpringBean
  private ActiveInstrumentRunService activeInstrumentRunService;

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  @SpringBean
  private InstrumentService instrumentService;

  @SpringBean(name = "userSessionService")
  private UserSessionService userSessionService;

  private ConfirmationDialog confirmationDialog;

  private Dialog measuresDetailsDialog;

  private Dialog invalidMeasureDetailsDialog;

  public MeasuresListPanel(String id) {
    super(id);
    setOutputMarkupId(true);

    addViewMeasureDetailsDialog();
    addViewInvalidMeasureDetailsDialog();
    addMeasuresList();
    addMeasureCounts();
    addRefreshLink();
    addConfirmDeleteMeasureDialog();
    addNoMeasureAvailableMessage();

  }

  private void addViewMeasureDetailsDialog() {
    add(measuresDetailsDialog = makeDialog("measuresDetailsDialog"));
  }

  private void addViewInvalidMeasureDetailsDialog() {
    add(invalidMeasureDetailsDialog = makeDialog("invalidMeasureDetailsDialog"));
  }

  private Dialog makeDialog(String id) {
    Dialog dialog = new Dialog(id);
    dialog.setHeightUnit("em");
    dialog.setWidthUnit("em");
    dialog.setInitialHeight(DEFAULT_INITIAL_HEIGHT);
    dialog.setInitialWidth(DEFAULT_INITIAL_WIDTH);
    dialog.setOptions(Dialog.Option.CLOSE_OPTION);
    return dialog;
  }

  private void addMeasureCounts() {
    Label expectedMeasureCount = new Label("expectedMeasureCount", new PropertyModel<Integer>(this, "expectedMeasureCount"));
    add(expectedMeasureCount);
  }

  @SuppressWarnings("serial")
  private void addMeasuresList() {
    ListView<Measure> repeater = new ListView<Measure>("measure", new PropertyModel<List<Measure>>(this, "measures")) {

      private void deleteMeasure(final Measure measure, AjaxRequestTarget target, int measureNo) {
        Fragment measureDetailsFragment = new Fragment("content", "measureDetails", MeasuresListPanel.this);
        addMeasureDetails(measureDetailsFragment, measure, measureNo);
        measureDetailsFragment.add(new AttributeModifier("class", true, new Model<String>("long-confirmation-dialog-content")));
        confirmationDialog.setContent(measureDetailsFragment);
        confirmationDialog.setYesButtonCallback(new OnYesCallback() {

          public void onYesButtonClicked(AjaxRequestTarget target) {
            activeInstrumentRunService.deleteMeasure(measure);
            refresh(target);
          }

        });
        confirmationDialog.show(target);
      }

      private void viewMeasure(final Measure measure, AjaxRequestTarget target) {
        InstrumentRunPanel instrumentRunPanel = new InstrumentRunPanel(measuresDetailsDialog.getContentId(), measuresDetailsDialog, measure);
        instrumentRunPanel.add(new AttributeModifier("class", true, new Model<String>("obiba-content instrument-run-panel-content")));
        measuresDetailsDialog.setContent(instrumentRunPanel);
        measuresDetailsDialog.show(target);
      }

      private Fragment addMeasureActions(final Measure measure, final int measureNo) {
        Fragment measureActionsFragment;
        measureActionsFragment = new Fragment("measureActions", "measureActionsFragment", MeasuresListPanel.this);

        measureActionsFragment.add(new AjaxLink<Void>("deleteMeasure") {

          @Override
          public void onClick(AjaxRequestTarget target) {
            deleteMeasure(measure, target, measureNo);
          }

        });

        measureActionsFragment.add(new AjaxLink<Void>("view") {

          @Override
          public void onClick(AjaxRequestTarget target) {
            viewMeasure(measure, target);
          }

        });
        return measureActionsFragment;
      }

      private Fragment addInvalidMeasureMessage(final Measure measure, final ListItem<Measure> item, final int measureNo) {
        Fragment measureActionsFragment;
        item.add(new AttributeAppender("class", true, new Model<String>("ui-state-error"), " "));
        measureActionsFragment = new Fragment("measureActions", "measureInvalidFragment", MeasuresListPanel.this);
        measureActionsFragment.add(getErrorDetailsLink(measure, measureNo));

        return measureActionsFragment;
      }

      private AjaxLink<Object> getErrorDetailsLink(final Measure measure, final int measureNo) {
        return new AjaxLink<Object>("errorDetails") {

          @Override
          public void onClick(AjaxRequestTarget target) {

            Fragment invalidMeasureDetailsFragment = new Fragment("content", "invalidMeasureDetailsFragment", MeasuresListPanel.this);

            ListView<String> repeater = new ListView<String>("invalidMeasureMessage", new PropertyModel<List<String>>(this, "invalidMeasureMessages")) {

              @Override
              protected void populateItem(ListItem<String> item) {
                String errorMessage = item.getModelObject();
                item.add(new Label("errorMessage", errorMessage));
              }

            };
            invalidMeasureDetailsFragment.add(repeater);
            invalidMeasureDetailsFragment.add(new AttributeModifier("class", true, new Model<String>("obiba-content invalid-measure-details-content")));
            invalidMeasureDetailsDialog.setContent(invalidMeasureDetailsFragment);
            invalidMeasureDetailsDialog.setTitle(new StringResourceModel("ErrorDetailsForMeasure", null, null, new Object[] { measureNo }));

            invalidMeasureDetailsDialog.show(target);

          }

          @SuppressWarnings("unused")
          @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS", justification = "Referenced by PropertyModel")
          public List<String> getInvalidMeasureMessages() {
            List<InstrumentOutputParameter> outputParams = activeInstrumentRunService.getInstrumentType().getOutputParameters(true);

            Map<IntegrityCheck, InstrumentOutputParameter> failedChecks = activeInstrumentRunService.checkIntegrity(outputParams, measure);

            List<String> errorMessages = new ArrayList<String>();
            for(Map.Entry<IntegrityCheck, InstrumentOutputParameter> entry : failedChecks.entrySet()) {

              MessageSourceResolvable resolvable = entry.getKey().getDescription((InstrumentParameter) entry.getValue(), activeInstrumentRunService);
              errorMessages.add((String) new MessageSourceResolvableStringModel(resolvable).getObject());
            }

            return errorMessages;
          }

          private List<InstrumentOutputParameter> getMeasureOutputParams(Measure measure) {
            String instrumentTypeName = activeInstrumentRunService.getInstrumentType().getName();
            List<InstrumentOutputParameter> outputParams = new ArrayList<InstrumentOutputParameter>();
            InstrumentParameter instrumentParam;
            for(InstrumentRunValue runValue : measure.getInstrumentRunValues()) {
              instrumentParam = instrumentService.getInstrumentType(instrumentTypeName).getInstrumentParameter(runValue.getInstrumentParameter());
              if(instrumentParam instanceof InstrumentOutputParameter && instrumentParam.getCaptureMethod() == InstrumentParameterCaptureMethod.AUTOMATIC) {
                outputParams.add((InstrumentOutputParameter) instrumentParam);
              }
            }
            return outputParams;
          }

        };
      }

      @Override
      protected void populateItem(final ListItem<Measure> item) {
        final Measure measure = item.getModelObject();

        final int measureNo = item.getIndex() + 1;
        addMeasureDetails(item, measure, measureNo);

        item.add(new AttributeAppender("class", true, new Model<String>(getOddEvenCssClass(measureNo - 1)), " "));

        boolean measureIsValid = measure.getStatus() == MeasureStatus.VALID ? true : false;
        Fragment measureActionsFragment;

        if(measureIsValid) {
          measureActionsFragment = addMeasureActions(measure, measureNo);
        } else {
          measureActionsFragment = addInvalidMeasureMessage(measure, item, measureNo);
        }

        item.add(measureActionsFragment);

      }

    };

    add(repeater);
  }

  @SuppressWarnings("serial")
  private void addNoMeasureAvailableMessage() {
    MarkupContainer noMeasureAvailable = new MarkupContainer("noMeasureAvailable") {

      @Override
      public boolean isVisible() {
        // Only visible when no measures
        return getMeasures().size() == 0;
      }

    };
    noMeasureAvailable.setOutputMarkupPlaceholderTag(true);
    add(noMeasureAvailable);
  }

  @SuppressWarnings("serial")
  private void addRefreshLink() {
    AjaxLink<Void> refreshLink = new AjaxLink<Void>("refresh") {

      @Override
      public void onClick(AjaxRequestTarget target) {
        refresh(target);
      }

    };
    add(refreshLink);
  }

  private void addConfirmDeleteMeasureDialog() {
    confirmationDialog = new ConfirmationDialog("confirmDeleteMeasureDialog");
    confirmationDialog.setTitle(new ResourceModel("ConfirmDeleteMeasure"));
    confirmationDialog.setHeightUnit("em");
    confirmationDialog.setInitialHeight(15);
    add(confirmationDialog);
  }

  private void addMeasureDetails(MarkupContainer component, Measure measure, int measureNo) {

    component.add(new Label("measureNo", String.valueOf(measureNo)));

    DateFormat dateTimeFormat = userSessionService.getDateTimeFormat();
    Date date = measure.getTime();
    component.add(new Label("measureDate", DateModelUtils.getDateTimeModel(new Model<DateFormat>(dateTimeFormat), new Model<Date>(date)).getObject().toString()));

    component.add(new Label("measureUser", measure.getUserName()));
    component.add(new Label("measureMethod", new StringResourceModel(measure.getCaptureMethod().toString(), this, null).getString()));

  }

  public void refresh(AjaxRequestTarget target) {
    log.debug("Refreshing MeasureListPanel...");
    target.addComponent(MeasuresListPanel.this);
    onRefresh(target);
  }

  public abstract void onRefresh(AjaxRequestTarget target);

  public List<Measure> getMeasures() {
    InstrumentRun run = activeInstrumentRunService.getInstrumentRun();
    if(run != null) {
      return run.getMeasures();
    } else {
      return new ArrayList<Measure>();
    }
  }

  public int getRemainingMeasureCount() {
    return getExpectedMeasureCount() - getMeasureCount();
  }

  public int getMeasureCount() {
    InstrumentRun run = activeInstrumentRunService.getInstrumentRun();
    if(run != null) {
      return run.getValidMeasureCount();
    } else {
      return 0;
    }
  }

  public int getExpectedMeasureCount() {
    return activeInstrumentRunService.getInstrumentType().getExpectedMeasureCount(activeInterviewService.getParticipant());
  }

  private String getOddEvenCssClass(int row) {
    return row % 2 == 1 ? "odd" : "even";
  }

}
