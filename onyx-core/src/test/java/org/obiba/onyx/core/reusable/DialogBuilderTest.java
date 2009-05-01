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

import junit.framework.Assert;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.wicket.reusable.Dialog;
import org.obiba.onyx.wicket.reusable.DialogBuilder;

public class DialogBuilderTest {

  private transient WicketTester tester;

  @Before
  public void setUp() {
    tester = new WicketTester();
  }

  @Test
  public void testBuildDialog() {
    Dialog dialog = DialogBuilder.buildDialog("dialog", "title test", new Label("content", "test content")).setOptions(Dialog.Option.OK_CANCEL_OPTION).getDialog();

    Assert.assertEquals(Dialog.Type.PLAIN, dialog.getType());
    Assert.assertEquals("title test", dialog.getTitle().getObject().toString());

    Assert.assertTrue(dialog.getForm().get("ok").isVisible());
    Assert.assertTrue(dialog.getForm().get("cancel").isVisible());
    Assert.assertFalse(dialog.getForm().get("yes").isVisible());
    Assert.assertFalse(dialog.getForm().get("no").isVisible());
    Assert.assertFalse(dialog.getForm().get("close").isVisible());
  }

  @Test
  public void testBuildDialogTitleModel() {
    Dialog dialog = DialogBuilder.buildDialog("dialog", new Model("title test"), new Label("content", "test content")).setOptions(Dialog.Option.OK_CANCEL_OPTION).getDialog();

    Assert.assertEquals(Dialog.Type.PLAIN, dialog.getType());
    Assert.assertEquals("title test", dialog.getTitle().getObject().toString());

    Assert.assertTrue(dialog.getForm().get("ok").isVisible());
    Assert.assertTrue(dialog.getForm().get("cancel").isVisible());
    Assert.assertFalse(dialog.getForm().get("yes").isVisible());
    Assert.assertFalse(dialog.getForm().get("no").isVisible());
    Assert.assertFalse(dialog.getForm().get("close").isVisible());
  }

  @Test
  public void testBuildWarningDialog() {
    Dialog dialog = DialogBuilder.buildWarningDialog("dialog", "title test", new Label("content", "test content")).setOptions(Dialog.Option.OK_CANCEL_OPTION).getDialog();

    Assert.assertEquals(Dialog.Type.WARNING, dialog.getType());

    Assert.assertTrue(dialog.getForm().get("ok").isVisible());
    Assert.assertTrue(dialog.getForm().get("cancel").isVisible());
    Assert.assertFalse(dialog.getForm().get("yes").isVisible());
    Assert.assertFalse(dialog.getForm().get("no").isVisible());
    Assert.assertFalse(dialog.getForm().get("close").isVisible());
  }

  @Test
  public void testBuildWarningDialogTitleModel() {
    Dialog dialog = DialogBuilder.buildWarningDialog("dialog", new Model("title test"), new Label("content", "test content")).setOptions(Dialog.Option.OK_CANCEL_OPTION).getDialog();

    Assert.assertEquals(Dialog.Type.WARNING, dialog.getType());

    Assert.assertTrue(dialog.getForm().get("ok").isVisible());
    Assert.assertTrue(dialog.getForm().get("cancel").isVisible());
    Assert.assertFalse(dialog.getForm().get("yes").isVisible());
    Assert.assertFalse(dialog.getForm().get("no").isVisible());
    Assert.assertFalse(dialog.getForm().get("close").isVisible());
  }

  @Test
  public void testBuildInfoDialog() {
    Dialog dialog = DialogBuilder.buildInfoDialog("dialog", "Information", new Label("content", "test content")).setOptions(Dialog.Option.YES_NO_OPTION).getDialog();

    Assert.assertEquals(Dialog.Type.INFO, dialog.getType());

    Assert.assertFalse(dialog.getForm().get("ok").isVisible());
    Assert.assertFalse(dialog.getForm().get("cancel").isVisible());
    Assert.assertTrue(dialog.getForm().get("yes").isVisible());
    Assert.assertTrue(dialog.getForm().get("no").isVisible());
    Assert.assertFalse(dialog.getForm().get("close").isVisible());
  }

  @Test
  public void testBuildInfoDialogTitleModel() {
    Dialog dialog = DialogBuilder.buildInfoDialog("dialog", new Model("Information"), new Label("content", "test content")).setOptions(Dialog.Option.YES_NO_CANCEL_OPTION).getDialog();

    Assert.assertEquals(Dialog.Type.INFO, dialog.getType());

    Assert.assertFalse(dialog.getForm().get("ok").isVisible());
    Assert.assertTrue(dialog.getForm().get("cancel").isVisible());
    Assert.assertTrue(dialog.getForm().get("yes").isVisible());
    Assert.assertTrue(dialog.getForm().get("no").isVisible());
    Assert.assertFalse(dialog.getForm().get("close").isVisible());
  }

  @Test
  public void testBuildErrorDialog() {
    Dialog dialog = DialogBuilder.buildErrorDialog("dialog", "Error", new Label("content", "test content")).setOptions(Dialog.Option.CLOSE_OPTION).getDialog();

    Assert.assertEquals(Dialog.Type.ERROR, dialog.getType());

    Assert.assertFalse(dialog.getForm().get("ok").isVisible());
    Assert.assertFalse(dialog.getForm().get("cancel").isVisible());
    Assert.assertFalse(dialog.getForm().get("yes").isVisible());
    Assert.assertFalse(dialog.getForm().get("no").isVisible());
    Assert.assertTrue(dialog.getForm().get("close").isVisible());
  }

  @Test
  public void testBuildErrorDialogTitleModel() {
    Dialog dialog = DialogBuilder.buildErrorDialog("dialog", new Model("Error"), new Label("content", "test content")).setOptions(Dialog.Option.CLOSE_OPTION).getDialog();

    Assert.assertEquals(Dialog.Type.ERROR, dialog.getType());

    Assert.assertFalse(dialog.getForm().get("ok").isVisible());
    Assert.assertFalse(dialog.getForm().get("cancel").isVisible());
    Assert.assertFalse(dialog.getForm().get("yes").isVisible());
    Assert.assertFalse(dialog.getForm().get("no").isVisible());
    Assert.assertTrue(dialog.getForm().get("close").isVisible());
  }
}
