/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.administration.panel;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.obiba.onyx.core.domain.participant.InterviewStatus;
import org.obiba.onyx.wicket.model.OnyxDataPurgeModel;
import org.obiba.onyx.wicket.model.SpringStringResourceModel;

/**
 * Displays details on which Participants will be purged, breaking things down by exported and unexported as well as by
 * {@link InterviewStatus}.
 */
@SuppressWarnings("serial")
public class PurgeDetailsPanel extends Panel {

  public PurgeDetailsPanel(String id, OnyxDataPurgeModel model) {
    super(id, model);

    WebMarkupContainer purgeDetails = new WebMarkupContainer("PurgeDetails") {
      @Override
      public boolean isVisible() {
        return getPurgeInfo().hasInterviewsToPurge();
      }
    };
    WebMarkupContainer noPurgeMessage = new WebMarkupContainer("NoPurgeMessage") {
      @Override
      public boolean isVisible() {
        return !getPurgeInfo().hasInterviewsToPurge();
      }
    };

    add(purgeDetails);
    add(noPurgeMessage);

    purgeDetails.add(new Label("configurablePurgeMessage", new SpringStringResourceModel("ConfigurablePurgeMessage", "ConfigurablePurgeMessage")));
    purgeDetails.add(new Label("interviewToBeDeletedMessage", new StringResourceModel("NumberInterviewsToDelete", PurgeDetailsPanel.this, null, new Object[] { new PropertyModel<String>(this, "totalInterviewsToPurge"), new PropertyModel<String>(this, "totalInterviews") })));
  }

  private OnyxDataPurgeModel getPurgeInfo() {
    return (OnyxDataPurgeModel) getDefaultModel();
  }

  public String getTotalInterviewsToPurge() {
    return getPurgeInfo().getTotalInterviewsToPurge();
  }

  public String getTotalInterviews() {
    return getPurgeInfo().getTotalInterviews();
  }
}
