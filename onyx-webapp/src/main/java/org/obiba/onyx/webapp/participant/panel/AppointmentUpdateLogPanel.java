/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.participant.panel;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.Loop;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.obiba.onyx.core.domain.statistics.AppointmentUpdateLog;

/**
 * 
 */
public class AppointmentUpdateLogPanel extends Panel {

  private static final long serialVersionUID = 6231563190329420046L;

  /** Component to hold the list of log items. */
  private Loop logItemLoop;

  /**
   * @param id
   * @param model
   */
  public AppointmentUpdateLogPanel(String id, List<AppointmentUpdateLog> appointmentUpdateLogs) {
    super(id);

    sortAppointmentUpdateLogList(appointmentUpdateLogs);
    addLogComponent(appointmentUpdateLogs);

  }

  @SuppressWarnings("unchecked")
  public void addLogComponent(final List<AppointmentUpdateLog> appointmentUpdateLogs) {

    if(logItemLoop != null) {
      remove(logItemLoop);
    }
    logItemLoop = new Loop("table", appointmentUpdateLogs.size()) {

      private static final long serialVersionUID = 5173436167390888581L;

      @Override
      protected void populateItem(LoopItem item) {
        item.setRenderBodyOnly(true);
        AppointmentUpdateLog appointmentUpdateLog = appointmentUpdateLogs.get(item.getIteration());
        item.add(new RowFragment("rows", "appointmentUpdateLogRow", AppointmentUpdateLogPanel.this, new Model(appointmentUpdateLog), item.getIteration()));
      }
    };

    addOrReplace(logItemLoop);
  }

  public class RowFragment extends Fragment {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    public RowFragment(String id, String markupId, MarkupContainer markupContainer, IModel model, int iteration) {
      super(id, markupId, markupContainer, model);
      setRenderBodyOnly(true);
      AppointmentUpdateLog appointmentUpdateLog = (AppointmentUpdateLog) getDefaultModelObject();
      WebMarkupContainer webMarkupContainer = new WebMarkupContainer("logEntryRow");
      add(webMarkupContainer);
      webMarkupContainer.add(new AttributeAppender("class", true, new Model(getOddEvenCssClass(iteration)), " "));

      webMarkupContainer.add(new Label("date", new Model(appointmentUpdateLog.getFormatedDate())));
      webMarkupContainer.add(new Label("time", new Model(appointmentUpdateLog.getFormatedTime())));
      webMarkupContainer.add(new Label("level", new PropertyModel(appointmentUpdateLog, "level")));
      webMarkupContainer.add(new Label("participantId", new PropertyModel(appointmentUpdateLog, "participantId")));
      webMarkupContainer.add(new Label("message", new PropertyModel(appointmentUpdateLog, "message")));

    }
  }

  private String getOddEvenCssClass(int row) {
    return row % 2 == 1 ? "odd" : "even";
  }

  private void sortAppointmentUpdateLogList(List<AppointmentUpdateLog> appointmentUpdateLogs) {

    Collections.sort(appointmentUpdateLogs, new Comparator<AppointmentUpdateLog>() {
      public int compare(AppointmentUpdateLog log1, AppointmentUpdateLog log2) {
        return (Long.valueOf(log1.getDate().getTime()).compareTo(Long.valueOf(log2.getDate().getTime())));
      }
    });
  }
}
