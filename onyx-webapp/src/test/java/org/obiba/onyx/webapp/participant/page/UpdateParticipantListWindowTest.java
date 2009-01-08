/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.participant.page;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class UpdateParticipantListWindowTest {
  //
  // Instance Variables
  //

  private transient WicketTester wicketTester;

  //
  // Fixture Methods (setUp / tearDown)
  //

  @Before
  public void setUp() {
    wicketTester = new WicketTester();
  }

  //
  // Test Methods
  //

  @Test
  public void testShowConfirmationOnCreation() {
    UpdateParticipantListWindow window = new UpdateParticipantListWindow("window");

    Component confirmationFragment = window.get("content:contentFragment");
    Assert.assertTrue(confirmationFragment instanceof UpdateParticipantListWindow.ConfirmationFragment);
  }

  @Test
  public void testShowConfirmation() {
    UpdateParticipantListWindow window = new UpdateParticipantListWindow("window");
    window.showConfirmation();

    Component confirmationFragment = getContentFragment(window);
    Assert.assertTrue(confirmationFragment instanceof UpdateParticipantListWindow.ConfirmationFragment);
  }

  @Test
  public void testShowProgress() {
    UpdateParticipantListWindow window = new UpdateParticipantListWindow("window");
    window.showProgress();

    Component progressFragment = getContentFragment(window);
    Assert.assertTrue(progressFragment instanceof UpdateParticipantListWindow.ProgressFragment);
  }

  @Test
  public void testShowResultSucceeded() {
    UpdateParticipantListWindow window = new UpdateParticipantListWindow("window");
    window.showResult(true);

    Fragment resultFragment = getContentFragment(window);
    Assert.assertTrue(resultFragment instanceof UpdateParticipantListWindow.ResultFragment);

    Label resultLabel = (Label) resultFragment.get("resultLabel");
    Assert.assertEquals("ParticipantsListSuccessfullyUpdated", resultLabel.getModelObject());
  }

  @Test
  public void testShowResultFailed() {
    UpdateParticipantListWindow window = new UpdateParticipantListWindow("window");
    window.showResult(false);

    Fragment resultFragment = getContentFragment(window);
    Assert.assertTrue(resultFragment instanceof UpdateParticipantListWindow.ResultFragment);

    Label resultLabel = (Label) resultFragment.get("resultLabel");
    Assert.assertEquals("ParticipantListUpdateFailed", resultLabel.getModelObject());
  }

  //
  // Helper Methods
  //

  private Fragment getContentFragment(MarkupContainer container) {
    return (Fragment) container.get("content:contentFragment");
  }
}
