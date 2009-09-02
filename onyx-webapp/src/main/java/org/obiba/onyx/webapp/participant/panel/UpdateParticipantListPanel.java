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

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;

/**
 *
 */
public class UpdateParticipantListPanel extends Panel {

  private static final long serialVersionUID = 1L;

  //
  // Instance variables
  //

  private ConfirmationFragment confirmationFragment;

  private ProgressFragment progressFragment;

  private ResultFragment resultFragment;

  //
  // Constructors
  //

  public UpdateParticipantListPanel(String id) {
    super(id);

    confirmationFragment = new ConfirmationFragment("contentFragment", new ResourceModel("ConfirmParticipantListUpdate"));
    progressFragment = new ProgressFragment("contentFragment", new ResourceModel("ParticipantListUpdateInProgress"));
    resultFragment = new ResultFragment("contentFragment", new ResourceModel("ParticipantsListSuccessfullyUpdated"));

  }

  public void showConfirmation() {
    replaceOrAddFragment(confirmationFragment);
  }

  public void showProgress() {
    replaceOrAddFragment(progressFragment);
  }

  public void showResult(boolean updateSucceeded) {
    String messageKey = updateSucceeded ? "ParticipantsListSuccessfullyUpdated" : "ParticipantListUpdateFailed";
    IModel messageModel = new ResourceModel(messageKey, messageKey);
    resultFragment.resultLabel.setModel(messageModel);

    replaceOrAddFragment(resultFragment);
  }

  private void replaceOrAddFragment(Fragment fragment) {
    Fragment currentFragment = (Fragment) get("contentFragment");

    if(currentFragment != null) {
      replace(fragment);
    } else {
      add(fragment);
    }
  }

  //
  // Inner Classes
  //

  public class ConfirmationFragment extends Fragment {

    private static final long serialVersionUID = 1L;

    private Image icon;

    private MultiLineLabel messageLabel;

    public ConfirmationFragment(String id, IModel messageModel) {
      super(id, "confirmationFragment", UpdateParticipantListPanel.this);

      icon = new Image("confirmIcon");
      add(icon);

      messageLabel = new MultiLineLabel("confirmMessage", messageModel);
      add(messageLabel);
    }
  }

  public class ProgressFragment extends Fragment {

    private static final long serialVersionUID = 1L;

    private Label progressLabel;

    private Image progressImage;

    public ProgressFragment(String id, IModel messageModel) {
      super(id, "progressFragment", UpdateParticipantListPanel.this);

      progressLabel = new Label("progressLabel", messageModel);
      add(progressLabel);

      progressImage = new Image("progressImage");
      add(progressImage);
    }
  }

  public class ResultFragment extends Fragment {

    private static final long serialVersionUID = 1L;

    private Label resultLabel;

    public ResultFragment(String id, IModel messageModel) {
      super(id, "resultFragment", UpdateParticipantListPanel.this);

      resultLabel = new Label("resultLabel", messageModel);
      add(resultLabel);

    }
  }
}
