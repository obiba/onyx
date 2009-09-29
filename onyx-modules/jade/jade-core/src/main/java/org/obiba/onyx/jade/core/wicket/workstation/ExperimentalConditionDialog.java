/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.wicket.workstation;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalConditionLog;
import org.obiba.onyx.wicket.reusable.Dialog;
import org.obiba.onyx.wicket.reusable.DialogBuilder;
import org.obiba.onyx.wicket.reusable.Dialog.CloseButtonCallback;
import org.obiba.onyx.wicket.reusable.Dialog.Option;
import org.obiba.onyx.wicket.reusable.Dialog.Status;

public abstract class ExperimentalConditionDialog extends Panel {

  private static final long serialVersionUID = 1L;

  private ExperimentalConditionForm experimentalConditionForm;

  private final Dialog logDialog = DialogBuilder.buildDialog("experimentalConditionDialog", "Report Dialog", experimentalConditionForm = new ExperimentalConditionForm("content", null)).setOptions(Option.OK_CANCEL_OPTION, "Save").getDialog();

  public ExperimentalConditionDialog(String id) {
    this(id, null, null);
  }

  public ExperimentalConditionDialog(String id, IModel model, final IModel<Instrument> instrumentModel) {
    super(id, model);
    logDialog.setCloseButtonCallback(new CloseButtonCallback() {
      private static final long serialVersionUID = 1L;

      public boolean onCloseButtonClicked(AjaxRequestTarget target, Status status) {
        if(status == null || status != null && status.equals(Status.WINDOW_CLOSED)) {
          return true;
        } else if(status.equals(Status.SUCCESS)) {
          if(instrumentModel != null && instrumentModel.getObject() != null) {
            experimentalConditionForm.addBarcode(instrumentModel.getObject().getBarcode());
          }
          experimentalConditionForm.save();
          logDialog.close(target);
          return true;
        } else if(status.equals(Status.ERROR)) {
          experimentalConditionForm.getFeedbackWindow().setContent(new FeedbackPanel("content"));
          experimentalConditionForm.getFeedbackWindow().show(target);
          return false;
        }
        return true;
      }

    });

    add(logDialog);
  }

  protected Dialog getExperimentalConditionDialog() {
    return logDialog;
  }

  protected void setExperimentalConditionLog(ExperimentalConditionLog experimentalConditionLog) {
    experimentalConditionForm.setDefaultModel(new Model<ExperimentalConditionLog>(experimentalConditionLog));
    experimentalConditionForm.addComponents();
  }
}
