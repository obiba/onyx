/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.utils;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.obiba.onyx.wicket.Images;

/**
 *
 */
@SuppressWarnings("serial")
public abstract class SaveCancelPanel extends Panel {

  public SaveCancelPanel(String id, Form<?> form) {
    super(id);

    add(CSSPackageResource.getHeaderContribution(SaveCancelPanel.class, "SaveCancelPanel.css"));

    add(new AjaxButton("save", form) {
      @Override
      public void onSubmit(AjaxRequestTarget target, @SuppressWarnings("hiding") Form<?> form) {
        onSave(target, form);
      }

      @Override
      protected void onError(AjaxRequestTarget target, @SuppressWarnings("hiding") Form<?> form) {
        SaveCancelPanel.this.onError(target, form);
      }
    }.add(new Image("saveImg", Images.SAVE)));

    add(new AjaxButton("cancel", form) {
      @Override
      public void onSubmit(AjaxRequestTarget target, @SuppressWarnings("hiding") Form<?> form) {
        onCancel(target, form);
      }
    }.setDefaultFormProcessing(false).add(new Image("cancelImg", Images.CANCEL)));
  }

  protected abstract void onSave(AjaxRequestTarget target, Form<?> form);

  protected abstract void onCancel(AjaxRequestTarget target, Form<?> form);

  protected abstract void onError(AjaxRequestTarget target, Form<?> form);

}
