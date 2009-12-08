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
import org.apache.wicket.model.IModel;
import org.obiba.onyx.core.domain.participant.InterviewStatus;
import org.obiba.onyx.wicket.model.OnyxDataPurgeModel;

/**
 * Displays details on which Participants will be purged, breaking things down by exported and unexported as well as by
 * {@link InterviewStatus}.
 */
public class PurgeDetailsPanel extends Panel {
  private static final long serialVersionUID = 1L;

  public PurgeDetailsPanel(String id, final IModel<OnyxDataPurgeModel> model) {
    super(id, model);
    add(new Label("totalInterviewsToPurge", model.getObject().getTotalInterviewsToPurge()));
    WebMarkupContainer purgeDetails = new WebMarkupContainer("PurgeDetails");
    add(purgeDetails);
    purgeDetails.add(new Label("totalExportedInterviewsToPurge", model.getObject().getTotalExportedInterviewsToPurge()) {
      private static final long serialVersionUID = 1L;

      @Override
      public boolean isVisible() {
        String number = model.getObject().getTotalExportedInterviewsToPurge();
        return number != null && !number.equals("") && !number.equals("0");
      }

    });
    purgeDetails.add(new Label("totalExportedInProgressToPurge", model.getObject().getTotalExportedInterviewsWithStatus(InterviewStatus.IN_PROGRESS)) {
      private static final long serialVersionUID = 1L;

      @Override
      public boolean isVisible() {
        String number = model.getObject().getTotalExportedInterviewsWithStatus(InterviewStatus.IN_PROGRESS);
        return number != null && !number.equals("") && !number.equals("0");
      }

    });
    purgeDetails.add(new Label("totalExportedCompletedToPurge", model.getObject().getTotalExportedInterviewsWithStatus(InterviewStatus.COMPLETED)) {
      private static final long serialVersionUID = 1L;

      @Override
      public boolean isVisible() {
        String number = model.getObject().getTotalExportedInterviewsWithStatus(InterviewStatus.COMPLETED);
        return number != null && !number.equals("") && !number.equals("0");
      }

    });
    purgeDetails.add(new Label("totalExportedCancelledToPurge", model.getObject().getTotalExportedInterviewsWithStatus(InterviewStatus.CANCELLED)) {
      private static final long serialVersionUID = 1L;

      @Override
      public boolean isVisible() {
        String number = model.getObject().getTotalExportedInterviewsWithStatus(InterviewStatus.CANCELLED);
        return number != null && !number.equals("") && !number.equals("0");
      }

    });
    purgeDetails.add(new Label("totalExportedClosedToPurge", model.getObject().getTotalExportedInterviewsWithStatus(InterviewStatus.CLOSED)) {
      private static final long serialVersionUID = 1L;

      @Override
      public boolean isVisible() {
        String number = model.getObject().getTotalExportedInterviewsWithStatus(InterviewStatus.CLOSED);
        return number != null && !number.equals("") && !number.equals("0");
      }

    });

    purgeDetails.add(new Label("totalUnexportedInterviewsToPurge", model.getObject().getTotalUnexportedInterviewsToPurge()) {
      private static final long serialVersionUID = 1L;

      @Override
      public boolean isVisible() {
        String number = model.getObject().getTotalUnexportedInterviewsToPurge();
        return number != null && !number.equals("") && !number.equals("0");
      }

    });
    purgeDetails.add(new Label("totalUnexportedInProgressToPurge", model.getObject().getTotalUnexportedInterviewsWithStatus(InterviewStatus.IN_PROGRESS)) {
      private static final long serialVersionUID = 1L;

      @Override
      public boolean isVisible() {
        String number = model.getObject().getTotalUnexportedInterviewsWithStatus(InterviewStatus.IN_PROGRESS);
        return number != null && !number.equals("") && !number.equals("0");
      }

    });
    purgeDetails.add(new Label("totalUnexportedCompletedToPurge", model.getObject().getTotalUnexportedInterviewsWithStatus(InterviewStatus.COMPLETED)) {
      private static final long serialVersionUID = 1L;

      @Override
      public boolean isVisible() {
        String number = model.getObject().getTotalUnexportedInterviewsWithStatus(InterviewStatus.COMPLETED);
        return number != null && !number.equals("") && !number.equals("0");
      }

    });
    purgeDetails.add(new Label("totalUnexportedCancelledToPurge", model.getObject().getTotalUnexportedInterviewsWithStatus(InterviewStatus.CANCELLED)) {
      private static final long serialVersionUID = 1L;

      @Override
      public boolean isVisible() {
        String number = model.getObject().getTotalUnexportedInterviewsWithStatus(InterviewStatus.CANCELLED);
        return number != null && !number.equals("") && !number.equals("0");
      }

    });
    purgeDetails.add(new Label("totalUnexportedClosedToPurge", model.getObject().getTotalUnexportedInterviewsWithStatus(InterviewStatus.CLOSED)) {
      private static final long serialVersionUID = 1L;

      @Override
      public boolean isVisible() {
        String number = model.getObject().getTotalUnexportedInterviewsWithStatus(InterviewStatus.CLOSED);
        return number != null && !number.equals("") && !number.equals("0");
      }

    });
    if(model.getObject().getTotalInterviewsToPurge().equals("0")) purgeDetails.setVisible(false);

  }
}
