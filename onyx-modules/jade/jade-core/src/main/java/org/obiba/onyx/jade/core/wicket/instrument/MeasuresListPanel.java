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

import java.util.List;

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
import org.obiba.onyx.jade.core.domain.run.Measure;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.wicket.reusable.ConfirmationDialog;
import org.obiba.onyx.wicket.reusable.ConfirmationDialog.OnYesCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Panel that lists the existing measures of the current InstrumentRun.
 */
public class MeasuresListPanel extends Panel {

  private static final Logger log = LoggerFactory.getLogger(MeasuresListPanel.class);

  private static final long serialVersionUID = 1L;

  @SpringBean
  private ActiveInstrumentRunService activeInstrumentRunService;

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  ConfirmationDialog confirmationDialog;

  @SuppressWarnings("serial")
  public MeasuresListPanel(String id) {
    super(id);
    setOutputMarkupId(true);

    addMeasuresList();
    addMeasureCounts();
    addRefreshLink();
    addConfirmDeleteMeasureDialog();

  }

  private void addMeasureCounts() {
    Label expectedMeasureCount = new Label("expectedMeasureCount", new PropertyModel(this, "expectedMeasureCount"));
    add(expectedMeasureCount);
  }

  @SuppressWarnings("serial")
  private ListView addMeasuresList() {
    ListView repeater = new ListView("measure", new PropertyModel(this, "measures")) {

      private void deleteMeasure(final Measure measure, AjaxRequestTarget target) {
        Fragment measureDetailsFragment = new Fragment("content", "measureDetails", MeasuresListPanel.this);
        addMeasureDetails(measureDetailsFragment, measure);
        measureDetailsFragment.add(new AttributeModifier("class", true, new Model("long-confirmation-dialog-content")));
        confirmationDialog.setContent(measureDetailsFragment);
        confirmationDialog.setYesButtonCallback(new OnYesCallback() {
          public void onYesButtonClicked(AjaxRequestTarget target) {
            activeInstrumentRunService.deleteMeasure(measure);
            target.addComponent(MeasuresListPanel.this);
          }
        });
        confirmationDialog.show(target);
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
      }

    };
    add(repeater);
    return repeater;
  }

  @SuppressWarnings("serial")
  private void addRefreshLink() {
    AjaxLink refresh = new AjaxLink("refresh") {

      @Override
      public void onClick(AjaxRequestTarget target) {
        target.addComponent(MeasuresListPanel.this);
      }

    };
    add(refresh);
  }

  private void addConfirmDeleteMeasureDialog() {
    confirmationDialog = new ConfirmationDialog("confirmDeleteMeasureDialog");
    confirmationDialog.setTitle(new ResourceModel("ConfirmDeleteMeasure"));
    confirmationDialog.setInitialHeight(140);
    add(confirmationDialog);
  }

  private void addMeasureDetails(MarkupContainer component, Measure measure) {
    component.add(new Label("measureDate", measure.getTime().toString()));
    component.add(new Label("measureUser", measure.getUser().getFullName()));
    component.add(new Label("measureMethod", new StringResourceModel(measure.getCaptureMethod().toString(), this, null).getString()));
  }

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

}
