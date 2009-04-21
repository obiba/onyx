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

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

/**
 * Builder created to make easier the use of Dialog class in the application
 */
public class DialogBuilder {

  private Dialog dialog;

  private DialogBuilder(String id, Dialog.Type type, Component content, Dialog.Option option, String... labels) {
    dialog = new Dialog(id);
    dialog.setType(type);
    dialog.setOptions(option, labels);
    dialog.setContent(content);
  }

  public static DialogBuilder buildWarningDialog(String id, String title, Component content) {
    return new DialogBuilder(id, Dialog.Type.WARNING, content, null).setTitle(title);
  }

  public static DialogBuilder buildWarningDialog(String id, IModel title, Component content) {
    return new DialogBuilder(id, Dialog.Type.WARNING, content, null).setTitle(title);
  }

  public static DialogBuilder buildInfoDialog(String id, String title, Component content) {
    return new DialogBuilder(id, Dialog.Type.INFO, content, null).setTitle(title);
  }

  public static DialogBuilder buildInfoDialog(String id, IModel title, Component content) {
    return new DialogBuilder(id, Dialog.Type.INFO, content, null).setTitle(title);
  }

  public static DialogBuilder buildErrorDialog(String id, String title, Component content) {
    return new DialogBuilder(id, Dialog.Type.ERROR, content, null).setTitle(title);
  }

  public static DialogBuilder buildErrorDialog(String id, IModel title, Component content) {
    return new DialogBuilder(id, Dialog.Type.ERROR, content, null).setTitle(title);
  }

  public static DialogBuilder buildDialog(String id, String title, Component content) {
    return new DialogBuilder(id, Dialog.Type.PLAIN, content, null).setTitle(title);
  }

  public static DialogBuilder buildDialog(String id, IModel title, Component content) {
    return new DialogBuilder(id, Dialog.Type.PLAIN, content, null).setTitle(title);
  }

  public DialogBuilder setOptions(Dialog.Option option, String... labels) {
    dialog.setOptions(option, labels);
    return this;
  }

  public DialogBuilder setWindowClosedCallback(Dialog.WindowClosedCallback callback) {
    dialog.setWindowClosedCallback(callback);
    return this;
  }

  public DialogBuilder setCloseButtonCallback(Dialog.CloseButtonCallback callback) {
    dialog.setCloseButtonCallback(callback);
    return this;
  }

  private DialogBuilder setTitle(String title) {
    dialog.setTitle(title);
    return this;
  }

  private DialogBuilder setTitle(IModel title) {
    dialog.setTitle(title);
    return this;
  }

  public Dialog getDialog() {
    return dialog;
  }

}
