/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.reusable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.tester.TestPanelSource;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;

public class FeedbackWindowTest {

  private transient WicketTester wicketTester;

  @Before
  public void setUp() {
    wicketTester = new WicketTester();
  }

  /**
   * Tests the initial size (height and weight) of a <code>FeedbackWindow</code>.
   */
  @Test
  public void testInitialSize() {
    FeedbackWindow feedbackWindow = new FeedbackWindow("test");
    int expectedInitialHeight = 100;
    int expectedInitialWidth = 400;

    assertEquals(expectedInitialHeight, feedbackWindow.getInitialHeight());
    assertEquals(expectedInitialWidth, feedbackWindow.getInitialWidth());
  }

  /**
   * Tests the <code>FeedbackWindow</code>'s options (i.e., button set).
   * 
   * In a <code>FeedbackWindow</code>, only the "Close" button should be visible.
   */
  @Test
  public void testOptions() {
    FeedbackWindow feedbackWindow = new FeedbackWindow("test");

    Form form = (Form) feedbackWindow.get("content:form");
    assertTrue(form.get("close").isVisible());
    assertFalse(form.get("ok").isVisible());
    assertFalse(form.get("cancel").isVisible());
    assertFalse(form.get("yes").isVisible());
    assertFalse(form.get("no").isVisible());
  }

  @Test
  public void testHasCorrectCssClass() {
    wicketTester.startPanel(new TestPanelSource() {

      private static final long serialVersionUID = 1L;

      public Panel getTestPanel(String panelId) {
        return new DialogPanel(panelId, true);
      }
    });
    wicketTester.clickLink("panel:openDialog");

    // TODO: Find a better (more accurate) way to check this.
    wicketTester.assertContains("class=\"onyx-feedback\"");
  }
}