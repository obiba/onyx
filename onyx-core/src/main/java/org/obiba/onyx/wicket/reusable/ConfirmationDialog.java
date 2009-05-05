/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.wicket.reusable;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.StringResourceModel;

public class ConfirmationDialog extends Dialog {

  private static final long serialVersionUID = 3542344658891842366L;

  private static final int DEFAULT_CONFIRMATION_DIALOG_HEIGHT = 100;

  private static final int DEFAULT_CONFIRMATION_DIALOG_WIDTH = 400;

  private OnYesCallback onYesCallback;

  private OnNoCallback onNoCallback;

  public ConfirmationDialog(String id) {
    super(id);
    setOptions(Option.YES_NO_OPTION);
    setInitialHeight(DEFAULT_CONFIRMATION_DIALOG_HEIGHT);
    setInitialWidth(DEFAULT_CONFIRMATION_DIALOG_WIDTH);
    setType(Dialog.Type.WARNING);
    setTitle(new StringResourceModel("Confirmation", this, null));

    setWindowClosedCallback(new WindowClosedCallback() {

      private static final long serialVersionUID = -5700767289429133941L;

      public void onClose(AjaxRequestTarget target, Status status) {
        if(status.equals(Status.YES)) {
          if(onYesCallback != null) onYesCallback.onYesButtonClicked(target);
        } else if(status.equals(Status.NO)) {
          if(onNoCallback != null) onNoCallback.onNoButtonClicked(target);
        }
        // Ensure parent dialog doesn't close.
        Dialog parent = (Dialog) ConfirmationDialog.this.findParent(Dialog.class);
        if(parent != null) parent.setStatus(null);
      }
    });
  }

  public static interface OnYesCallback extends Serializable {
    public void onYesButtonClicked(AjaxRequestTarget target);
  }

  public void setYesButtonCallback(OnYesCallback yesButtonCallback) {
    this.onYesCallback = yesButtonCallback;
  }

  public static interface OnNoCallback extends Serializable {
    public void onNoButtonClicked(AjaxRequestTarget target);
  }

  public void setNoButtonCallback(OnNoCallback noButtonCallback) {
    this.onNoCallback = noButtonCallback;
  }

}
