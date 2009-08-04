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
import java.util.Date;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
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
import org.apache.wicket.util.time.Duration;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.jade.core.domain.run.Measure;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.wicket.run.InstrumentRunPanel;
import org.obiba.onyx.wicket.reusable.ConfirmationDialog;
import org.obiba.onyx.wicket.reusable.Dialog;
import org.obiba.onyx.wicket.reusable.ConfirmationDialog.OnYesCallback;
import org.obiba.onyx.wicket.util.DateModelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Panel that lists the existing measures of the current InstrumentRun.
 */
public abstract class MeasuresListPanel extends Panel {

  private static final Logger log = LoggerFactory.getLogger(MeasuresListPanel.class);

  private static final long serialVersionUID = 1L;

  private static final int DEFAULT_INITIAL_WIDTH = 400;

  private static final int DEFAULT_INITIAL_HEIGHT = 420;

  @SpringBean
  private ActiveInstrumentRunService activeInstrumentRunService;

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  @SpringBean
  private UserSessionService userSessionService;

  ConfirmationDialog confirmationDialog;

  Dialog measuresDetailsDialog;

  AbstractAjaxTimerBehavior autoRefreshBehavior;

  private Duration autoRefreshInterval = Duration.seconds(10);

  @SuppressWarnings("serial")
  public MeasuresListPanel(String id) {
    super(id);
    setOutputMarkupId(true);

    addViewMeasureDetailsDialog();
    addMeasuresList();
    addMeasureCounts();
    addRefreshLink();
    addConfirmDeleteMeasureDialog();
    addNoMeasureAvailableMessage();
    addAutoRefreshBehavior();

  }

  private void addViewMeasureDetailsDialog() {
    add(measuresDetailsDialog = new Dialog("measuresDetailsDialog"));
    measuresDetailsDialog.setInitialHeight(DEFAULT_INITIAL_HEIGHT);
    measuresDetailsDialog.setInitialWidth(DEFAULT_INITIAL_WIDTH);
    measuresDetailsDialog.setOptions(Dialog.Option.CLOSE_OPTION);
  }

  private void addMeasureCounts() {
    Label expectedMeasureCount = new Label("expectedMeasureCount", new PropertyModel(this, "expectedMeasureCount"));
    add(expectedMeasureCount);
  }

  @SuppressWarnings("serial")
  private void addMeasuresList() {
    ListView repeater = new ListView("measure", new PropertyModel(this, "measures")) {

      private void deleteMeasure(final Measure measure, AjaxRequestTarget target) {
        Fragment measureDetailsFragment = new Fragment("content", "measureDetails", MeasuresListPanel.this);
        addMeasureDetails(measureDetailsFragment, measure);
        measureDetailsFragment.add(new AttributeModifier("class", true, new Model("long-confirmation-dialog-content")));
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
        instrumentRunPanel.add(new AttributeModifier("class", true, new Model("obiba-content instrument-run-panel-content")));
        measuresDetailsDialog.setContent(instrumentRunPanel);
        measuresDetailsDialog.show(target);
      }

      @Override
      protected void populateItem(ListItem item) {
        final Measure measure = (Measure) item.getModelObject();

        addMeasureDetails(item, measure);

        item.add(new AjaxLink("deleteMeasure") {

          @Override
          public void onClick(AjaxRequestTarget target) {
            deleteMeasure(measure, target);
          }

        });
        item.add(new AttributeAppender("class", true, new Model(getOddEvenCssClass(item.getIndex())), " "));

        item.add(new AjaxLink("view") {

          @Override
          public void onClick(AjaxRequestTarget target) {
            viewMeasure(measure, target);
          }

        });
      }

    };

    add(repeater);
  }

  @SuppressWarnings("serial")
  private void addNoMeasureAvailableMessage() {
    MarkupContainer noMeasureAvailable = new MarkupContainer("noMeasureAvailable", new PropertyModel(this, "measures")) {
      @SuppressWarnings("unchecked")
      @Override
      public boolean isVisible() {
        if(((List<Measure>) getModelObject()).size() == 0) {
          return true;
        }
        return false;
      }

    };
    noMeasureAvailable.setOutputMarkupPlaceholderTag(true);
    add(noMeasureAvailable);
  }

  @SuppressWarnings("serial")
  private void addRefreshLink() {
    AjaxLink refreshLink = new AjaxLink("refresh") {

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
    confirmationDialog.setInitialHeight(140);
    add(confirmationDialog);
  }

  private void addMeasureDetails(MarkupContainer component, Measure measure) {

    DateFormat dateTimeFormat = userSessionService.getDateTimeFormat();
    Date date = measure.getTime();
    component.add(new Label("measureDate", DateModelUtils.getDateTimeModel(new Model(dateTimeFormat), new Model(date)).getObject().toString()));

    component.add(new Label("measureUser", measure.getUser().getFullName()));
    component.add(new Label("measureMethod", new StringResourceModel(measure.getCaptureMethod().toString(), this, null).getString()));

  }

  /**
   * Add a behavior that will refresh the panel at regular time intervals.
   */
  @SuppressWarnings("serial")
  private void addAutoRefreshBehavior() {
    add(autoRefreshBehavior = new AbstractAjaxTimerBehavior(getAutoRefreshInterval()) {

      @Override
      protected void onTimer(AjaxRequestTarget target) {
        refresh(target);
      }

    });
  }

  public void enableAutoRefresh() {
    // To be implemented
  }

  public void disableAutoRefresh() {
    // To be implemented
  }

  public void refresh(AjaxRequestTarget target) {
    log.debug("Refreshing MeasureListPanel...");
    target.addComponent(MeasuresListPanel.this);
    onRefresh(target);
  }

  public abstract void onRefresh(AjaxRequestTarget target);

  public List<Measure> getMeasures() {
    return activeInstrumentRunService.getInstrumentRun().getMeasures();
  }

  public int getRemainingMeasureCount() {
    return getExpectedMeasureCount() - activeInstrumentRunService.getInstrumentRun().getMeasureCount();
  }

  public int getExpectedMeasureCount() {
    return activeInstrumentRunService.getInstrumentType().getExpectedMeasureCount(activeInterviewService.getParticipant());
  }

  private String getOddEvenCssClass(int row) {
    return row % 2 == 1 ? "odd" : "even";
  }

  public Duration getAutoRefreshInterval() {
    return autoRefreshInterval;
  }

  public void setAutoRefreshInterval(Duration autoRefreshInterval) {
    this.autoRefreshInterval = autoRefreshInterval;
  }

}
