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

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.tester.TestPanelSource;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.wicket.reusable.Dialog.OptionSide;
import org.obiba.onyx.wicket.test.ExtendedApplicationContextMock;
import org.obiba.wicket.test.MockSpringApplication;

/**
 * 
 */
public class DialogTest {

  private transient WicketTester tester;

  @Before
  public void setUp() {
    ExtendedApplicationContextMock mockCtx = new ExtendedApplicationContextMock();

    MockSpringApplication application = new MockSpringApplication();
    application.setApplicationContext(mockCtx);
    application.setHomePage(Page.class);

    tester = new WicketTester(application);
  }

  @Test
  public void testShowDialog() {

    tester.startPanel(new TestPanelSource() {

      private static final long serialVersionUID = 1L;

      public Panel getTestPanel(String panelId) {
        return new DialogPanel(panelId);
      }
    });
    tester.clickLink("panel:openDialog");

    tester.assertVisible("panel:dialog:content:form:ok");
    tester.assertVisible("panel:dialog:content:form:cancel");
    tester.assertVisible("panel:dialog:content:form:yes");
    tester.assertVisible("panel:dialog:content:form:no");
    tester.assertVisible("panel:dialog:content:form:close");
  }

  @Test
  public void testShowDialogOptions() {

    tester.startPanel(new TestPanelSource() {

      private static final long serialVersionUID = 1L;

      public Panel getTestPanel(String panelId) {
        return new DialogPanel(panelId, Dialog.Option.OK_CANCEL_OPTION);
      }
    });
    tester.clickLink("panel:openDialog");

    tester.assertVisible("panel:dialog:content:form:ok");
    tester.assertVisible("panel:dialog:content:form:cancel");
    tester.assertInvisible("panel:dialog:content:form:yes");
    tester.assertInvisible("panel:dialog:content:form:no");
    tester.assertInvisible("panel:dialog:content:form:close");
  }

  @Test
  public void testShowDialogContent() {

    tester.startPanel(new TestPanelSource() {

      private static final long serialVersionUID = 1L;

      public Panel getTestPanel(String panelId) {
        return new DialogPanel(panelId, new Label("content", "test content label"));
      }
    });
    tester.clickLink("panel:openDialog");

    tester.assertComponent("panel:dialog:content:form:content", Label.class);
  }

  @Test
  public void testShowDialogCustomOption() {

    tester.startPanel(new TestPanelSource() {

      private static final long serialVersionUID = 1L;

      public Panel getTestPanel(String panelId) {
        return new DialogPanel(panelId, "Show", OptionSide.LEFT, new AjaxLink("showLink") {

          private static final long serialVersionUID = 1L;

          @Override
          public void onClick(AjaxRequestTarget target) {
          }

        });
      }
    });
    tester.clickLink("panel:openDialog");
    tester.dumpPage();

    // tester.assertVisible("panel:dialog:content:form:ok");
  }
}
