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

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.tester.TestPanelSource;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.wicket.test.ExtendedApplicationContextMock;
import org.obiba.wicket.test.MockSpringApplication;

/**
 * 
 */
public class ConfirmationDialogTest implements Serializable {

  private static final long serialVersionUID = 213154615275428692L;

  private transient WicketTester tester;

  @Before
  public void setUp() {
    ExtendedApplicationContextMock mockCtx = new ExtendedApplicationContextMock();

    MockSpringApplication application = new MockSpringApplication();
    application.setApplicationContext(mockCtx);
    application.setHomePage(Page.class);

    tester = new WicketTester(application);
    // tester = new WicketTester();
  }

  @Test
  public void testConfirmationDialogYesNoButtonsAreVisible() {

    tester.startPanel(new TestPanelSource() {

      private static final long serialVersionUID = 1L;

      public Panel getTestPanel(String panelId) {
        return new ConfirmationDialogTestPanel(panelId);
      }
    });

    tester.clickLink("panel:openConfirmationDialogLink");

    tester.assertVisible("panel:confirmationDialog:content:form:yes");
    tester.assertVisible("panel:confirmationDialog:content:form:no");
  }

  @Test
  public void testConfirmationMessageisVisible() {

    tester.startPanel(new TestPanelSource() {

      private static final long serialVersionUID = 1L;

      public Panel getTestPanel(String panelId) {
        ConfirmationDialogTestPanel confirmationDialogTestPanel = new ConfirmationDialogTestPanel(panelId);
        ConfirmationDialog confirmationDialog = confirmationDialogTestPanel.getConfirmationDialog();

        Label content = new Label("content", "Are you sure? This action has dangerous consequences.");

        confirmationDialog.setContent(content);
        return confirmationDialogTestPanel;
      }
    });

    tester.clickLink("panel:openConfirmationDialogLink");

    tester.assertLabel("panel:confirmationDialog:content:form:content", "Are you sure? This action has dangerous consequences.");
  }

}
